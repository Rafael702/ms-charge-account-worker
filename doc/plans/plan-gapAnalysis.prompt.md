# Plan: Gap Analysis - Desafio Técnico vs Implementação Atual
Análise completa comparando os requisitos do README com o que foi implementado.
Identifica lacunas reais e prioriza as entregas faltantes para conclusão do desafio.
**Data:** 2026-04-20
**Status:** Em andamento
---
## 📊 ESTADO ATUAL DA IMPLEMENTAÇÃO
### ✅ IMPLEMENTADO (completo)
#### Domínio
- `Charge.java` — entidade principal com lógica de processamento e rejeição
- `ChargeId`, `AccountId`, `ChargeType`, `ChargeStatus`, `AccountStatus`, `ProcessingResult` — Value Objects
- `DomainEvent`, `ChargeReceivedEvent`, `ChargeProcessedEvent`, `AccountValidatedEvent` — Eventos de domínio
- `ChargeRepository` — Port de persistência
- `ChargeProcessingDomainService` — Lógica de domínio pura
#### Aplicação
- `ChargeDTO`, `AccountStatusDTO`, `ProcessingResultDTO` — DTOs
- `EventPublisherPort`, `FileReaderPort` — Ports de saída
- `ChargeProcessingApplicationService` — Orquestração do processamento
- `ChargeConsultationApplicationService` — Consulta de cobranças
- `TriggerChargeProcessingUseCase` + `TriggerChargeProcessingService` — Trigger manual
#### Infraestrutura
- `CsvFileReaderAdapter` + `ChargeCSVRecord` — Leitura de arquivo CSV com stream
- `KafkaPublisherAdapter` — Publicação de eventos com Retry + CircuitBreaker
- `KafkaListenerAdapter` — Consumo do evento `AccountValidatedEvent`
- `ChargeJpaAdapter`, `ChargeEntity`, `ChargeMapper` — Persistência JPA
- `ProcessamentoScheduler` — Agendamento diário às 04:00 com Resilience4j
- `ChargeProcessingController` — POST `/processing/trigger` (trigger manual via HTTP)
- `KafkaConfiguration`, `JpaConfiguration`, `CsvConfiguration` — Configurações
#### Testes
- Unit: `ChargeTest`, `AccountStatusTest`, `ChargeProcessingDomainServiceTest`, `ChargeProcessingApplicationServiceTest`, `ChargeJpaAdapterTest`
- Integration: `KafkaPublisherAdapterIT`, `CsvFileReaderAdapterIT`
---
## ❌ GAPS vs REQUISITOS DO README
### GAP 1 — API de Consulta de Lançamentos (🔴 CRÍTICO)
> Requisito: *"Exposição de API para consulta online dos lançamentos processados"*
**Problema:** `ChargeConsultationApplicationService` existe, mas **não há controller REST** expondo os endpoints de consulta.
**O que falta:**
- `ChargeConsultationController` com `GET /charges/{id}` e `GET /charges/account/{accountId}`
- `ChargeRepository.findByAccountId()` retorna `Optional<Charge>`, deveria retornar `List<Charge>` (uma conta pode ter N cobranças)
- Testes do controller (`ChargeConsultationControllerTest`)
---
### GAP 2 — Tratamento de Erros na API (🔴 CRÍTICO)
> Sem `GlobalExceptionHandler`, erros expõem stack traces e retornam status HTTP incorretos.
**O que falta:**
- `GlobalExceptionHandler` (`@ControllerAdvice`) com `@ExceptionHandler` para cada exceção
- Exceções customizadas: `ChargeNotFoundException`, `InvalidChargeException`
- DTO de erro padronizado: `ApiErrorResponse`
- Testes do handler
---
### GAP 3 — `filePath` ignorado no Trigger Manual (🟠 MÉDIO)
> Requisito: *"Implementar controller para chamadas manuais de leitura"*
**Problema:** `TriggerChargeProcessingService.execute(filePath)` recebe o `filePath` mas o ignora completamente — sempre usa o caminho padrão configurado.
**O que falta:**
- Passar `filePath` para `ProcessamentoScheduler.triggerManualProcessing(filePath)`
- Atualizar `ProcessamentoScheduler` para aceitar filePath opcional
- Teste cobrindo o cenário com filePath customizado
---
### GAP 4 — Dead Letter Queue (DLQ) (🟠 MÉDIO)
> `KafkaListenerAdapter` tem comentário *"In a real scenario, this would be sent to a Dead Letter Queue"* mas não implementa.
**O que falta:**
- Configurar tópico DLQ no `KafkaConfiguration`
- Publicar mensagem no DLQ em caso de erro no listener em vez de `throw RuntimeException`
- Propriedade `app.kafka.topics.dlq` já existe em `application-dev.yml` mas não é usada
- Teste validando envio para DLQ após N falhas
---
### GAP 5 — Perfil de Produção com PostgreSQL (🟠 MÉDIO)
> Dev usa H2 em memória. Sem `application-prod.yml`, a aplicação não conecta ao PostgreSQL real.
**O que falta:**
- `application-prod.yml` com datasource PostgreSQL, Kafka real, logging produtivo
- Validar se `schema.sql` é compatível com PostgreSQL (dialeto H2 vs Postgres pode diferir)
- Variáveis de ambiente documentadas (credenciais, hosts)
---
### GAP 6 — Documentação OpenAPI/Swagger (🟡 BAIXO)
> Não há documentação automática dos endpoints.
**O que falta:**
- Dependência `springdoc-openapi-starter-webmvc-ui` no `pom.xml`
- Anotações `@Operation`, `@ApiResponse` nos controllers
- Configuração de `springdoc` no `application-dev.yml`
---
### GAP 7 — Testes de Integração E2E e Scheduler (🟡 BAIXO)
> Fluxo completo CSV → Kafka → DB → API não tem teste automatizado.
**O que falta:**
- `ProcessamentoSchedulerIT` — trigger manual + validar persistência no DB
- `ChargeConsultationControllerTest` (MockMvc)
- Teste E2E com TestContainers (Kafka + PostgreSQL) cobrindo fluxo completo
---
## 🎯 Steps
### Step 1 — Corrigir `ChargeRepository` e adicionar `ChargeConsultationController`
- Alterar `ChargeRepository.findByAccountId()` para retornar `List<Charge>` em vez de `Optional<Charge>`
- Atualizar `ChargeJpaAdapter` e `ChargeConsultationApplicationService` conforme
- Criar `ChargeConsultationController` em `infrastructure/web/` com:
  - `GET /charges/{chargeId}` → delega para `ChargeConsultationApplicationService.consultCharge()`
  - `GET /charges/account/{accountId}` → delega para `consultChargeByAccount()`
- Criar `ChargeConsultationControllerTest` com MockMvc
### Step 2 — Criar `GlobalExceptionHandler` e exceções customizadas
- Criar `ChargeNotFoundException`, `InvalidChargeException` em `application/exception/`
- Criar `ApiErrorResponse` DTO em `application/dto/`
- Criar `GlobalExceptionHandler` em `infrastructure/web/` com `@ControllerAdvice`
- Atualizar `ChargeConsultationApplicationService` para lançar `ChargeNotFoundException`
- Criar `GlobalExceptionHandlerTest`
### Step 3 — Implementar o `filePath` no trigger manual
- Atualizar `ProcessamentoScheduler.triggerManualProcessing(String filePath)` para aceitar filePath
- Atualizar `TriggerChargeProcessingService` para repassar o parâmetro
- Adicionar teste unitário cobrindo o caminho customizado
### Step 4 — Implementar DLQ no `KafkaListenerAdapter`
- Injetar `KafkaTemplate` no `KafkaListenerAdapter`
- Substituir `throw RuntimeException` por publicação no tópico `${app.kafka.topics.dlq}`
- Adicionar teste de integração validando mensagem no DLQ após falha
### Step 5 — Criar `application-prod.yml`
- Configurar datasource PostgreSQL (via variáveis de ambiente: `DB_URL`, `DB_USER`, `DB_PASS`)
- Configurar Kafka com bootstrap-servers via env: `KAFKA_BOOTSTRAP_SERVERS`
- Ajustar `ddl-auto: validate` (sem create-drop)
- Logging em nível `INFO` (sem debug SQL)
### Step 6 — Adicionar Swagger/OpenAPI
- Adicionar dependência `springdoc-openapi-starter-webmvc-ui` no `pom.xml`
- Anotar controllers com `@Operation` e `@ApiResponse`
- Configurar `springdoc.swagger-ui.path=/docs` no `application-dev.yml`
### Step 7 — Testes de integração E2E e Scheduler
- Criar `ProcessamentoSchedulerIT` com arquivo CSV temporário e validação de persistência
- Criar teste E2E com TestContainers (Kafka + H2/Postgres) cobrindo fluxo completo
---
## 📋 Matriz de Rastreabilidade — README vs Código
| Requisito (README) | Status | Classe(s) |
|--------------------|--------|-----------|
| Leitura do arquivo de lançamentos | ✅ | `CsvFileReaderAdapter` |
| Publicação de evento solicitando status da conta | ✅ | `KafkaPublisherAdapter.publish()` |
| Recebimento do evento de resposta | ✅ | `KafkaListenerAdapter` |
| Validação do status (ATIVA, CANCELADA, BLOQUEIO_JUDICIAL) | ✅ | `AccountStatus`, `ChargeProcessingDomainService` |
| Processamento ou recusa do lançamento | ✅ | `Charge.process()` |
| Envio de evento ao sistema contábil (sucesso) | ✅ | `KafkaPublisherAdapter.publishToAccountingSystem()` |
| Persistência do resultado | ✅ | `ChargeJpaAdapter` |
| **API para consulta dos lançamentos** | ❌ | `ChargeConsultationController` **falta** |
| Agendamento às 04:00 | ✅ | `ProcessamentoScheduler` |
| Limite de 2h (conclusão às 06:00) | ⚠️ | Sem mecanismo de timeout/alerta |
| Resiliência (retry + circuit breaker) | ✅ | Resilience4j em Kafka e Scheduler |
| DLQ para falhas | ⚠️ | Comentado, não implementado |
| Trigger manual via API | ⚠️ | Existe, mas filePath é ignorado |
---
## Estrutura de Diretórios
```
src/main/java/com/itau/chargeaccount/
├── application/
│   ├── dto/
│   │   └── ApiErrorResponse.java          (novo - GAP 2)
│   ├── exception/
│   │   ├── ChargeNotFoundException.java   (novo - GAP 2)
│   │   └── InvalidChargeException.java    (novo - GAP 2)
│   └── service/
│       └── ChargeConsultationApplicationService.java  (alterar - GAP 1)
├── domain/
│   └── repository/
│       └── ChargeRepository.java          (alterar - GAP 1: List<Charge>)
└── infrastructure/
    ├── event/
    │   └── KafkaListenerAdapter.java      (alterar - GAP 4: DLQ)
    ├── persistence/jpa/adapter/
    │   └── ChargeJpaAdapter.java          (alterar - GAP 1: List<Charge>)
    ├── scheduler/
    │   └── ProcessamentoScheduler.java    (alterar - GAP 3: filePath)
    └── web/
        ├── ChargeConsultationController.java  (novo - GAP 1)
        └── GlobalExceptionHandler.java        (novo - GAP 2)
src/main/resources/
└── application-prod.yml                   (novo - GAP 5)
```
---
## Dependências
```xml
<!-- GAP 6: Swagger/OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```
---
## Further Considerations
1. **`findByAccountId` retorna `Optional`**: Semanticamente errado — uma conta pode ter múltiplos lançamentos no mesmo arquivo. Deve ser `List<Charge>` para refletir o modelo real.
2. **Janela de 2h sem monitoramento**: O README exige conclusão até 06:00. Considerar log de `WARN` se o processamento ultrapassar 90 min, ou um `@Scheduled` de watchdog.
3. **`application-prod.yml` com `ddl-auto: validate`**: Em produção jamais usar `create-drop`. Avaliar adoção de Flyway/Liquibase para migrations seguras.
4. **`ChargeConsultationController` vs `ChargeProcessingController`**: Atualmente em `/processing`. Padronizar prefixo de rota: `/charges` para consulta e `/processing` para operações.
---
## ✅ Checklist de Conclusão
- [ ] `ChargeConsultationController` criado e testado
- [ ] `ChargeRepository.findByAccountId` retorna `List<Charge>`
- [ ] `ChargeConsultationApplicationService` atualizado para `List<ChargeDTO>`
- [ ] `ChargeJpaAdapter.findAllByAccountId` implementado
- [ ] `GlobalExceptionHandler` criado com `ChargeNotFoundException` e `InvalidChargeException`
- [ ] `ApiErrorResponse` DTO criado
- [ ] `filePath` funcional no trigger manual (scheduler + service)
- [ ] DLQ implementado no `KafkaListenerAdapter`
- [ ] `application-prod.yml` criado com variáveis de ambiente
- [ ] Swagger/OpenAPI configurado (`/docs`)
- [ ] `ProcessamentoSchedulerIT` implementado
- [ ] `ChargeConsultationControllerTest` implementado (MockMvc)
- [ ] `mvn clean test` — todos os testes passando ✅
