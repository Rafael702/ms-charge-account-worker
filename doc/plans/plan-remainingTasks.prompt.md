# Plan: Tarefas Restantes - ms-charge-account-worker

**Status:** 48% Completo | 26 arquivos principais | 46 testes passando ✅  
**Data:** 2026-04-18  
**Versão:** 1.0

---

## 📊 STATUS ATUAL - O QUE JÁ FOI FEITO

### ✅ CAMADA DE DOMÍNIO (Domain Layer) - 100%
- ✅ Charge.java - Entidade principal com lógica de negócio
- ✅ ChargeId, AccountId, ChargeType, ChargeStatus, AccountStatus - Value Objects
- ✅ DomainEvent, ChargeReceivedEvent, ChargeProcessedEvent, AccountValidatedEvent - Eventos
- ✅ ChargeRepository (Port) - Interface de persistência
- ✅ ChargeProcessingDomainService - Lógica de domínio
- ✅ Testes: ChargeTest, ValueObjectTests, DomainServiceTest (39 testes)

### ✅ CAMADA DE APLICAÇÃO (Application Layer) - 100%
- ✅ ChargeDTO, AccountStatusDTO, ProcessingResultDTO - DTOs
- ✅ EventPublisherPort, FileReaderPort - Portas de saída
- ✅ ChargeProcessingApplicationService, ChargeConsultationApplicationService - Services
- ✅ Testes: ApplicationServiceTests (7 testes)

### ⏳ CAMADA DE INFRAESTRUTURA (Infrastructure Layer) - 60%
- ✅ ChargeEntity, ChargeMapper - Mapeamento JPA
- ✅ ChargeJpaRepository - Spring Data JPA
- ✅ ChargeJpaAdapter - Implementação da port ChargeRepository
- ✅ JpaConfiguration - Configuração de banco de dados
- ✅ Testes: ChargeJpaAdapterTest (6 testes)
- ❌ KafkaPublisherAdapter (Event Publisher implementação)
- ❌ KafkaConfiguration e tópicos
- ❌ KafkaListenerAdapter para consumir eventos
- ❌ CsvFileReaderAdapter para leitura de arquivos
- ❌ ProcessamentoScheduler (@Scheduled)

### ❌ CAMADA DE APRESENTAÇÃO (Presentation Layer) - 0%
- ❌ ChargeProcessingController (POST /api/charges/process)
- ❌ ChargeConsultationController (GET /api/charges/{id}, GET /api/charges/account/{accountId})
- ❌ GlobalExceptionHandler (@ControllerAdvice)
- ❌ Exceções customizadas (ChargeNotFoundException, InvalidChargeException)
- ❌ DTOs de Request/Response

### ❌ TESTES DE INTEGRAÇÃO (Integration Tests) - 0%
- ❌ KafkaPublisherAdapterIT (com EmbeddedKafka)
- ❌ KafkaListenerAdapterIT
- ❌ CsvFileReaderAdapterIT
- ❌ ProcessamentoSchedulerIT
- ❌ End-to-End Tests

### ❌ DOCKER & DEVOPS - 0%
- ❌ Dockerfile
- ❌ docker-compose.yml
- ❌ .env e variáveis de ambiente
- ❌ CI/CD (.github/workflows)

### ❌ DOCUMENTAÇÃO FINAL - 0%
- ❌ Atualizar README com instruções
- ❌ Criar SETUP.md
- ❌ Criar DEPLOYMENT.md
- ❌ ADR (Architecture Decision Records)
- ❌ OpenAPI/Swagger

---

## 🎯 TAREFAS RESTANTES - PRIORIZAÇÃO

### FASE 2-A: Kafka Event Publisher (3-4 horas | ~2.5K tokens)
**Prioridade:** 🔴 ALTA (depende o scheduler)

#### Subtasks:
1. **Criar KafkaPublisherAdapter.java**
   - Implementar EventPublisherPort
   - Usar KafkaTemplate para publicar
   - Métodos: publishToAccount(), publishToAccountingSystem()
   - Adicionar @CircuitBreaker e @Retry (Resilience4j)

2. **Criar KafkaConfiguration.java**
   - Definir tópicos (charge.received, account.status.request, charge.processed)
   - ProducerFactory, ConsumerFactory
   - ObjectMapper customizado para JSON

3. **Criar KafkaListenerAdapter.java**
   - Consumir AccountValidatedEvent
   - Chamar ChargeProcessingApplicationService.finalizarProcessamento()
   - Dead Letter Queue para erros

4. **Testes: KafkaPublisherAdapterIT.java**
   - @EmbeddedKafka
   - Mock de publicação de eventos
   - Validar retentativas e circuit breaker

---

### FASE 2-B: File Reader CSV (2-3 horas | ~1.5K tokens)
**Prioridade:** 🔴 ALTA

#### Subtasks:
1. **Criar CsvFileReaderAdapter.java**
   - Implementar FileReaderPort
   - Usar Jackson CSV Parser ou CsvMapper
   - Retornar Stream<ChargeDTO> (sem carregar tudo em memória)
   - Cabeçalhos esperados: chargeId, accountId, chargeType, amount

2. **Criar ChargeCSVRecord.java**
   - DTO para mapeamento de linha CSV
   - Annotations: @CsvBindByName

3. **Testes: CsvFileReaderAdapterIT.java**
   - Mock arquivo CSV
   - Validar parsing correto
   - Testar linhas inválidas

---

### FASE 2-C: Scheduler (2-3 horas | ~1.5K tokens)
**Prioridade:** 🔴 ALTA

#### Subtasks:
1. **Criar ProcessamentoScheduler.java**
   - @Component + @EnableScheduling
   - @Scheduled(cron = "0 4 * * *") - 04:00 todos os dias
   - Orquestração: Ler arquivo → Publicar eventos em batch
   - Logs com timestamps de início/fim
   - Tratamento de erros

2. **Adicionar Resilience**
   - @Retry com backoff exponencial
   - @CircuitBreaker para falhas em cascata
   - Logging de tentativas

3. **Testes: ProcessamentoSchedulerIT.java**
   - Mock de arquivo
   - Trigger manualmente
   - Validar eventos publicados

---

### FASE 3-A: REST Controllers (3-4 horas | ~2K tokens)
**Prioridade:** 🟠 MÉDIA

#### Subtasks:
1. **Criar ChargeProcessingController.java**
   - POST /api/v1/charges/process
   - Validação com @Valid
   - Chamar ChargeProcessingApplicationService
   - Retornar 201 Created com Location header

2. **Criar ChargeConsultationController.java**
   - GET /api/v1/charges/{id}
   - GET /api/v1/charges/account/{accountId}?page=0&size=10
   - Paginação com Spring Data

3. **Criar DTOs**
   - ChargeProcessRequest
   - ChargeResponse
   - PaginatedResponse

4. **Testes: ControllerTests**
   - MockMvc
   - Validar status HTTP
   - Validar JSON response

---

### FASE 3-B: Exception Handling (1-2 horas | ~1K tokens)
**Prioridade:** 🟠 MÉDIA

#### Subtasks:
1. **Criar exceções customizadas**
   - ChargeNotFoundException extends RuntimeException
   - InvalidChargeException
   - AccountStatusException

2. **Criar GlobalExceptionHandler.java**
   - @ControllerAdvice
   - @ExceptionHandler para cada exceção
   - Retornar ApiErrorResponse

3. **Testes: ExceptionHandlerTest**
   - Validar mapeamento de exceções

---

### FASE 4: Integration Tests - End-to-End (3-4 horas | ~2K tokens)
**Prioridade:** 🟠 MÉDIA

#### Subtasks:
1. **Testes com TestContainers + EmbeddedKafka**
   - PostgreSQL container
   - Kafka embedded
   - Fluxo completo: arquivo → BD → API

2. **Validar fluxo completo**
   - Ler CSV
   - Publicar ChargeReceived
   - Receber AccountValidated
   - Persistir resultado
   - Consultar via API

---

### FASE 5: Docker & DevOps (2-3 horas | ~1.5K tokens)
**Prioridade:** 🟡 BAIXA

#### Subtasks:
1. **Criar Dockerfile**
   - Multi-stage: build + runtime
   - Base: eclipse-temurin:17
   - Expose 8080
   - Healthcheck

2. **Criar docker-compose.yml**
   - App (Spring Boot)
   - PostgreSQL
   - Kafka + Zookeeper
   - Networks e volumes

3. **CI/CD básico**
   - GitHub Actions workflow
   - Build e test em push

---

### FASE 6: Documentação (2-3 horas | ~1.5K tokens)
**Prioridade:** 🟡 BAIXA

#### Subtasks:
1. **Atualizar README.md**
   - Como rodar
   - Requisitos
   - Estrutura do projeto

2. **Criar SETUP.md**
   - Instruções de ambiente

3. **Criar DEPLOYMENT.md**
   - Como fazer deploy

---

## 📊 RESUMO DE TAREFAS

| Fase | Componente | Horas | Tokens | Status |
|------|-----------|-------|--------|--------|
| 2-A | Kafka Publisher | 3-4 | 2.5K | ⏳ |
| 2-B | File Reader CSV | 2-3 | 1.5K | ⏳ |
| 2-C | Scheduler | 2-3 | 1.5K | ⏳ |
| 3-A | REST Controllers | 3-4 | 2K | ⏳ |
| 3-B | Exception Handling | 1-2 | 1K | ⏳ |
| 4 | Integration Tests | 3-4 | 2K | ⏳ |
| 5 | Docker/DevOps | 2-3 | 1.5K | ⏳ |
| 6 | Documentação | 2-3 | 1.5K | ⏳ |
| **TOTAL** | | **18-27h** | **~13.5K** | |

---

## 🚀 PRÓXIMOS PASSOS

### Recomendação de Ordem:
1. **FASE 2-A** (Kafka) - Fundação para scheduler
2. **FASE 2-B** (File Reader) - Simples, sem dependências
3. **FASE 2-C** (Scheduler) - Orquestra os dois acima
4. **FASE 3-A** (Controllers) - API REST
5. **FASE 3-B** (Exception Handling) - Tratamento global
6. **FASE 4** (Integration Tests) - Validar fluxo
7. **FASE 5** (Docker) - Containerização
8. **FASE 6** (Docs) - Finalizar

---

## ✅ CHECKLIST ANTES DE COMEÇAR CADA FASE

- [ ] Ler instruções da fase (este arquivo)
- [ ] `mvn clean test` - validar testes anteriores passando
- [ ] Criar branch: `git checkout -b feature/fase-xx`
- [ ] Ao terminar cada subtask, executar testes
- [ ] Fazer commit com padrão: `feat(fase-xx): descrição`
- [ ] Push ao final da fase
- [ ] PR para main

---

**Próxima ação:** Começar FASE 2-A (Kafka Event Publisher) 🚀


