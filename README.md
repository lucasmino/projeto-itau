# projeto-itau
Projeto criado para processo seletivo vaga de dev pleno
b9f397ff-5cad-47ad-b136-58545cbc552a

para rodar todos os testes da classe E2EVALIDATIONTest (teste ponta a ponta) é imprescindivel que seja subido a infra de docker

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