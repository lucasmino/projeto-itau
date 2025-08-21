package com.lucasmatheus.projetoitau.e2e

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("E2E - fluxo de criação, consulta e validação")
class E2EValidationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var rest: TestRestTemplate

    private val om = ObjectMapper().apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    // ===== DTOs usados pelo teste =====
    private data class CreateBody(
        val customerId: String,
        val productId: String,
        val category: String,
        val paymentMethod: String,
        val salesChannel: String,
        val totalMonthlyPremiumAmount: BigDecimal,
        val insuredAmount: BigDecimal,
        val coverages: Map<String, BigDecimal>,
        val assistances: List<String>
    )

    private data class CreatedResp(val id: String, val createdAt: String)

    private data class SummaryResp(
        val id: String,
        val status: String,
        val category: String,
        val paymentMethod: String,
        val salesChannel: String,
        val totalMonthlyPremiumAmount: String,
        val insuredAmount: String,
        val createdAt: String,
        val finishedAt: String?
    )

    private data class ValidateOut(
        val id: String,
        val previousStatus: String,
        val newStatus: String,
        val changed: Boolean
    )

    private fun baseUrl(path: String) = "http://localhost:$port$path"

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16")
            .withDatabaseName("itau")
            .withUsername("test")
            .withPassword("test")

        @JvmStatic
        private var wiremock: WireMockServer? = null

        @JvmStatic
        private fun wiremockEnsureStarted(): WireMockServer {
            var wm = wiremock
            if (wm == null) {
                wm = WireMockServer(options().dynamicPort()).also {
                    it.start()
                    wiremock = it
                }
            }
            return wm
        }

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            if (!postgres.isRunning) postgres.start()
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.jpa.hibernate.ddl-auto") { "update" }

            // publica a URL do mock de fraude para o bean FraudHttpClient
            registry.add("fraud.base-url") {
                val wm = wiremockEnsureStarted()
                "http://localhost:${wm.port()}"
            }
        }
    }

    // ===== helpers =====
    private fun uuid() = UUID.randomUUID().toString()
    private fun nowIso() = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()

    private fun dumpWiremockEvents(wm: WireMockServer, title: String) {
        val events: List<ServeEvent> = wm.getAllServeEvents()
        println("=== WIREMOCK EVENTS: $title (${events.size}) ===")
        events.forEachIndexed { idx, ev ->
            println("#$idx -> ${ev.request.method} ${ev.request.url}")
            println("   Request body: ${ev.request.bodyAsString}")
            println("   Response code: ${ev.response.status}")
            println("   Response body: ${ev.response.bodyAsString}")
        }
        println("=== END WIREMOCK EVENTS ($title) ===")
    }

    // Stubs padrão agora retornam REGULAR (no lugar de LOW_RISK)
    @BeforeEach
    fun setupDefaultStubs() {
        val wm = wiremockEnsureStarted()
        WireMock.configureFor("localhost", wm.port())

        val orderId = uuid()
        val customerId = uuid()
        val analyzedAt = nowIso()

        // POST usado pelo sistema
        wm.stubFor(
            post(urlPathEqualTo("/fraud/check"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                              "orderId": "$orderId",
                              "customerId": "$customerId",
                              "classification": "REGULAR",
                              "occurrences": [],
                              "analyzedAt": "$analyzedAt"
                            }
                            """.trimIndent()
                        )
                )
        )
        // GET apenas para "probe" de debug
        wm.stubFor(
            get(urlPathEqualTo("/fraud/check"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                              "orderId": "$orderId",
                              "customerId": "$customerId",
                              "classification": "REGULAR",
                              "occurrences": [],
                              "analyzedAt": "$analyzedAt"
                            }
                            """.trimIndent()
                        )
                )
        )
    }

    @AfterAll
    fun tearDownAll() {
        runCatching { wiremock?.stop() }
        runCatching { postgres.stop() }
    }

    @Test
    @DisplayName("HIGH_RISK -> REJECTED (integra Postgres + WireMock)")
    fun end_to_end_high_risk_flow() {
        val wm = wiremockEnsureStarted()
        WireMock.configureFor("localhost", wm.port())

        wm.resetAll()

        val orderId = uuid()
        val customerId = uuid()
        val analyzedAt = nowIso()

        wm.stubFor(
            post(urlPathEqualTo("/fraud/check"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                              "orderId": "$orderId",
                              "customerId": "$customerId",
                              "classification": "HIGH_RISK",
                              "occurrences": [],
                              "analyzedAt": "$analyzedAt"
                            }
                            """.trimIndent()
                        )
                )
        )
        wm.stubFor(
            get(urlPathEqualTo("/fraud/check")) // apenas probe
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                              "orderId": "$orderId",
                              "customerId": "$customerId",
                              "classification": "HIGH_RISK",
                              "occurrences": [],
                              "analyzedAt": "$analyzedAt"
                            }
                            """.trimIndent()
                        )
                )
        )

        val fraudProbeUrl = "http://localhost:${wm.port()}/fraud/check"
        val probe = rest.getForEntity(fraudProbeUrl, String::class.java)
        println("PROBE -> GET $fraudProbeUrl => ${probe.statusCode}")
        println("PROBE body: ${probe.body}")

        // 1) cria
        val create = CreateBody(
            customerId = uuid(),
            productId = uuid(),
            category = "AUTO",
            paymentMethod = "CREDIT_CARD",
            salesChannel = "ONLINE",
            totalMonthlyPremiumAmount = BigDecimal("150.00"),
            insuredAmount = BigDecimal("50000.00"),
            coverages = mapOf("BASIC" to BigDecimal("150.00")),
            assistances = listOf("TOW", "GLASS")
        )
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val req = HttpEntity(om.writeValueAsString(create), headers)

        val created = rest.postForEntity(baseUrl("/requests/policy"), req, CreatedResp::class.java)
        Assertions.assertEquals(HttpStatus.CREATED, created.statusCode, "esperava 201 ao criar a request")
        val id = created.body!!.id
        println("Created requestId=$id")

        val got1 = rest.getForEntity(baseUrl("/requests/$id"), SummaryResp::class.java)
        Assertions.assertEquals(HttpStatus.OK, got1.statusCode)
        println("Initial status=${got1.body!!.status}")
        Assertions.assertEquals("RECEIVED", got1.body!!.status)

        val validateResp = rest.postForEntity(baseUrl("/requests/$id/validate"), null, ValidateOut::class.java)
        println("Validate HTTP=${validateResp.statusCode} body=${validateResp.body}")
        val v = validateResp.body!!
        val expectedStatus = if (v.changed) HttpStatus.ACCEPTED else HttpStatus.OK
        Assertions.assertEquals(expectedStatus, validateResp.statusCode)
        println("ValidateOut -> id=${v.id}, previous=${v.previousStatus}, new=${v.newStatus}, changed=${v.changed}")
        Assertions.assertEquals("RECEIVED", v.previousStatus)
        Assertions.assertEquals("REJECTED", v.newStatus)
        Assertions.assertTrue(v.changed)

        val got2 = rest.getForEntity(baseUrl("/requests/$id"), SummaryResp::class.java)
        println("Final status=${got2.body!!.status} finishedAt=${got2.body!!.finishedAt}")
        Assertions.assertEquals(HttpStatus.OK, got2.statusCode)
        Assertions.assertEquals("REJECTED", got2.body!!.status)
        Assertions.assertNotNull(got2.body!!.finishedAt)


        dumpWiremockEvents(wm, title = "Após validação HIGH_RISK")
    }
}
