# Plano de Implementação - ms-charge-account-worker

## 📊 STATUS ATUAL

**CONCLUÍDO (40%)**
- ✅ Domain Layer (Charge, Events, Value Objects, Service)
- ✅ Application Layer (DTOs, Services, Ports)
- ✅ Testes Unitários (~40 testes)
- ✅ Documentação padrões

**PENDENTE (60%)**
- ⏳ Infrastructure (JPA, Kafka, File Reader)
- ⏳ Presentation (Controllers)
- ⏳ Scheduler
- ⏳ Integration Tests
- ⏳ Performance & Resiliência

---

## 🎯 TASKS PRIORITIZADAS

### BLOCO 1: INFRASTRUCTURE - JPA/DATABASE
*Estimado: 3.5K-4.5K tokens | 4-6 horas*

**1.1 - ChargeEntity & Mapper**
- Criar `ChargeEntity.java` (@Entity, @Table)
- Criar `ChargeMapper.java` (Charge ↔ ChargeEntity)
- Adicionar @Column, @Id, @Temporal annotations
- Includes getters/setters

**1.2 - JPA Repository & Adapter**
- Criar `ChargeJpaRepository.java` (extends JpaRepository)
- Criar `ChargeJpaAdapter.java` (implementa ChargeRepository Port)
- Métodos: save(), update(), findById(), findByAccountId()

**1.3 - Database Configuration**
- Atualizar `application-dev.yml` (H2)
- Atualizar `application-test.yml` (H2 in-memory)
- Atualizar `application-prod.yml` (PostgreSQL)
- Criar script SQL com CREATE TABLE charge + índices

**1.4 - Teste JPA**
- Criar `ChargeJpaAdapterTest.java`
- Validar conversão Entity ↔ Charge
- Mock de ChargeJpaRepository

---

### BLOCO 2: INFRASTRUCTURE - KAFKA
*Estimado: 2.5K-3.5K tokens | 3-4 horas*

**2.1 - Event Publisher Adapter**
- Criar `KafkaEventPublisher.java` (implementa EventPublisherPort)
- Usar KafkaTemplate
- Métodos: publish(), publishToAccountingSystem()

**2.2 - Kafka Configuration**
- Criar `KafkaConfiguration.java`
- Definir tópicos, ProducerFactory, ConsumerFactory
- ObjectMapper para serialização JSON

**2.3 - Event Listener**
- Criar `AccountStatusEventListener.java` (@KafkaListener)
- Consome AccountValidatedEvent
- Chama ChargeProcessingApplicationService.finalizeProcessingWithAccountStatus()

**2.4 - Teste Kafka**
- Criar `KafkaEventPublisherTest.java`
- Mock com EmbeddedKafka
- Validar mensagens publicadas

---

### BLOCO 3: INFRASTRUCTURE - FILE READER
*Estimado: 1.5K-2K tokens | 2-3 horas*

**3.1 - CSV File Reader**
- Criar `CsvChargeFileReader.java` (implementa FileReaderPort)
- Usar Jackson CSV Parser
- Retornar Stream<ChargeDTO> (sem carregar tudo em memória)

**3.2 - CSV Record DTO**
- Criar `ChargeCSVRecord.java`
- Mapear colunas do arquivo CSV
- Validações básicas

**3.3 - Teste CSV**
- Criar `CsvChargeFileReaderTest.java`
- Mock arquivo CSV
- Validar parsing

---

### BLOCO 4: INFRASTRUCTURE - CONFIGURATION
*Estimado: 1K-1.5K tokens | 1-2 horas*

**4.1 - Spring Configurations**
- Criar `JpaConfiguration.java` (@EnableJpaRepositories)
- Criar `KafkaConfiguration.java` (@EnableKafka)
- Criar beans de adapters

**4.2 - Application Properties**
- Configurar datasource (dev/test/prod)
- Configurar Kafka topics
- Configurar logging

---

### BLOCO 5: PRESENTATION - REST CONTROLLERS
*Estimado: 2K-2.5K tokens | 3-4 horas*

**5.1 - Processing Controller**
- Criar `ChargeProcessingController.java`
- POST `/api/charges/process` (iniciar processamento)
- Chamar ChargeProcessingApplicationService

**5.2 - Consultation Controller**
- Criar `ChargeConsultationController.java`
- GET `/api/charges/{id}`
- GET `/api/charges/account/{accountId}`

**5.3 - DTOs de Request/Response**
- Criar `ChargeProcessRequest.java`
- Criar `ChargeResponse.java`
- Adicionar @Valid, @NotNull annotations

**5.4 - Teste Controllers**
- Criar `ChargeProcessingControllerTest.java` (MockMvc)
- Criar `ChargeConsultationControllerTest.java`

---

### BLOCO 6: PRESENTATION - EXCEPTION HANDLING
*Estimado: 1.5K-2K tokens | 2-3 horas*

**6.1 - Custom Exceptions**
- Criar `ChargeNotFoundException`
- Criar `InvalidChargeException`
- Criar `AccountStatusException`

**6.2 - Global Exception Handler**
- Criar `GlobalExceptionHandler.java` (@ControllerAdvice)
- @ExceptionHandler para cada exceção
- Criar `ErrorResponse.java` DTO

**6.3 - Teste Exceptions**
- Criar `GlobalExceptionHandlerTest.java`

---

### BLOCO 7: SCHEDULER
*Estimado: 2K-2.5K tokens | 3-4 horas*

**7.1 - Processing Scheduler**
- Criar `ChargeProcessingScheduler.java`
- @Scheduled(cron = "0 4 * * *") - 04:00 todo dia
- Ler arquivo via FileReaderPort
- Processar charges em lotes (10K por batch)

**7.2 - Resiliência**
- Adicionar @Retry (Resilience4j)
- Adicionar @CircuitBreaker
- Adicionar exponential backoff

**7.3 - Teste Scheduler**
- Criar `ChargeProcessingSchedulerTest.java`
- Mock arquivo e repositories

---

### BLOCO 8: INTEGRATION TESTS
*Estimado: 2.5K-3.5K tokens | 4-6 horas*

**8.1 - TestContainers Setup**
- Adicionar dependência `testcontainers-kafka`
- Criar `PostgreSQLTestContainer.java`
- Criar `KafkaTestContainer.java`

**8.2 - End-to-End Tests**
- Criar `ChargeProcessingIntegrationTest.java`
- Ler arquivo → Publicar evento → Processar → Validar BD

**8.3 - Kafka Integration**
- Criar `KafkaIntegrationTest.java`
- Publicar evento → Listener → Processamento

**8.4 - API Integration**
- Criar `ChargeApiIntegrationTest.java`
- Testar endpoints REST completos

---

### BLOCO 9: PERFORMANCE & ESCALABILIDADE
*Estimado: 2K-2.5K tokens | 3-4 horas*

**9.1 - Batch Processing**
- Otimizar leitura CSV (chunks de 50K)
- Bulk insert via JPA
- Limpar recursos entre batches

**9.2 - Resiliência**
- Dead Letter Queue (Kafka) para falhas
- Retry logic com backoff exponencial
- Circuit breaker para EventPublisherPort

**9.3 - Monitoring**
- Configurar Micrometer metrics
- Adicionar logging estruturado (MDC)
- Endpoints Actuator (/metrics, /health)

**9.4 - Performance Tests**
- Criar `ChargeProcessingPerformanceTest.java`
- Testar com 100K registros
- Medir tempo/memória

---

### BLOCO 10: DOCUMENTATION & DEPLOYMENT
*Estimado: 1.5K-2K tokens | 2-3 horas*

**10.1 - Documentação**
- Atualizar README.md
- Criar `SETUP.md`
- Criar `DEPLOYMENT.md`
- Atualizar `CHANGELOG.md`

**10.2 - Docker & CI/CD**
- Criar `Dockerfile`
- Criar `docker-compose.yml` (PostgreSQL, Kafka, App)
- Criar `.github/workflows/ci.yml`

---

## 📊 RESUMO CONSOLIDADO

| BLOCO | Sub-tasks | Tokens | Tempo |
|-------|-----------|--------|-------|
| 1. JPA | 4 | 3.5-4.5K | 4-6h |
| 2. Kafka | 4 | 2.5-3.5K | 3-4h |
| 3. File Reader | 3 | 1.5-2K | 2-3h |
| 4. Config | 2 | 1-1.5K | 1-2h |
| 5. Controllers | 4 | 2-2.5K | 3-4h |
| 6. Exceptions | 3 | 1.5-2K | 2-3h |
| 7. Scheduler | 3 | 2-2.5K | 3-4h |
| 8. Int Tests | 4 | 2.5-3.5K | 4-6h |
| 9. Performance | 4 | 2-2.5K | 3-4h |
| 10. Docs/Deploy | 2 | 1.5-2K | 2-3h |

**TOTAL: 33 sub-tasks | ~21-27K tokens | ~32-42 horas**

---

## 🚀 SEQUÊNCIA RECOMENDADA

**Semana 1:**
- BLOCO 1 → 2 → 3 → 4 (Infrastructure) = 11-16 horas

**Semana 2:**
- BLOCO 5 → 6 → 7 (Presentation + Scheduler) = 8-11 horas

**Semana 3:**
- BLOCO 8 → 9 (Tests + Performance) = 7-10 horas

**Semana 4:**
- BLOCO 10 (Docs/Deploy) + testes finais = 2-3 horas

---

## ✅ INSTRUÇÕES OBRIGATÓRIAS

### Antes de cada BLOCO
1. Ler documentação do BLOCO
2. Executar: `mvn clean test` (validar estado anterior)

### Durante desenvolvimento
1. Sempre executar: `mvn clean test` após mudanças
2. Respeitar padrão de idioma (Código: Inglês, Docs: Português)
3. Seguir Conventional Commits
4. Sem código comentado

### Após cada BLOCO
1. Validar: `mvn clean test` (todos os testes passam)
2. Validar: `mvn clean compile` (sem erros)
3. Commit com padrão: `feat(bloco-name): descrição`

---

## 📚 Referências de Documentação

- `.copilot/QUICK_REFERENCE.md` - Referência rápida
- `.copilot/TESTING_GUIDE.md` - Guia de testes
- `.copilot/AGENT.md` - Padrões do projeto
- `.copilot/LANGUAGE_STANDARD.md` - Padrão de idioma

---

## 🎯 Próximo Passo

**Começar por:** BLOCO 1 (JPA/Database) ou BLOCO 2 (Kafka)?

Recomendação: BLOCO 1 primeiro (fundação da persistência)

