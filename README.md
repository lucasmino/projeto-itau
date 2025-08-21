# projeto-itau
Projeto criado para processo seletivo vaga de dev pleno
Arquitetura utilizada:
Optei por hexagonal por ser uma com qual eu nunca havia usado criando desde o zero, achei interessante utiliza-la por conta de uma premissa importante da aplicação ser testavel e o uso de ports and adapters facilita o desacoplamento a nivel de testes (sem depender excessivamente de tecnologia e sim abstrações)
apesar de não ter conseguido ajustar os testes e escrever para todos os cenarios senti um feedback interessante ao não precisar mockar recursos de libs em testes unitarios que devem ser simples, isso é bom
e dessa maneira considero que a aplicação seria facilmente escalavel a nivel de adaptar novos recursos, sentir que o dominio ficou intocado foi um feeling interessante também.
Deixei uma collection com chamadas validas em apiitau.postman_collection.json


comandos uteis docker compose up -d : sobe toda a infra
docker compose down -v : derruba toda a infra
sudo docker logs -f projetoitau : os logs da aplicação principal onde podemos ver eventos de messageria sendo publicados e consumidos

sudo docker exec -it db bash
root@a49906dca1fe:/# env | grep POSTGRES : acessa o container do banco
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" : acessa o banco de dados postgres

o arquivo [fraud-customer-7c2a27ba.json](fraud-mock/__files/fraud-customer-7c2a27ba.json) define o que o mock da api de fraudes irá responder, então 
para simular o cenário de policy recusada por fraude altere o atributo
"classification": "REGULAR" - > para uma solicitação ir pra pending (cenario feliz)
ou "HIGH_RISK" - > para o status ir para REJECTED

Publicar eventos de pagamento e subscrições aprovados
acessar: http://localhost:15672/#/queues/%2F/subscription-results e http://localhost:15672/#/queues/%2F/payment-results -> publish message
example de payment confirmation-> {
"requestId": "996802375-7f96-4bb7-885b-0ed07c455ca6",
"approved": true,
"decidedAt": "2025-08-21T00:42:00Z"
}

subscription -> {
"requestId": "96802375-7f96-4bb7-885b-0ed07c455ca6",
"authorized": true,
"decidedAt": "2025-08-21T00:42:00Z"
} não consegui corrigir o fluxo completo (o status não muda mediante a emissão do evento continua : PENDING)


Justificativa de algumas funcionalidades

optei por validar antifraude de forma assíncrona via eventos logo após a criação da solicitação primeiro porque a aplicação toda 
aparenta gerenciar seus principais fluxos por emissão de eventos de maneira assincrona reaproveitar a ideia para a chamada para a api
de fraudes me pareceu um bom adicional em performance para aplicação.

Essa abordagem traz benefícios concretos:

Baixa latência na criação — o POST /requests/policy não bloqueia esperando terceiros; o cliente recebe o requestId imediatamente e pode acompanhar a evolução do status.

Resiliência a falhas externas — indisponibilidade do serviço de fraudes não derruba o fluxo principal. Podemos reprocessar com retry/backoff sem impactar a experiência de criação.

Acoplamento fraco — comunicação por eventos reduz dependência direta entre serviços e facilita substituição/escala de integrações (fraudes, pagamentos, notificações).

Auditabilidade — toda transição de status é registrada no history e publicada em tópico, permitindo rastreabilidade ponta a ponta (quem mudou, quando e por quê).

Escalabilidade horizontal — consumidores do tópico (PolicyRequestCreated, PolicyRequestStatusChanged) podem escalar independentemente do serviço de API.

Evolução incremental — a mesma malha de eventos permitirá introduzir, no futuro, validações adicionais (ex.: crédito) e integração com “pagamentos & subscrição” sem reescrever o core.

Justificativa de validação de status para -> pending ao inves de validated, eu entendi via enunciado que o status validated era transitório ou seja após a api de fraudes retornar uma classificação que se encaixe em validated o status apos validated seria imediatamente pending nao seria preciso realizar uma operação a mais no banco para duas querys de updated seguidos, por isso optei por lançar para pending direto