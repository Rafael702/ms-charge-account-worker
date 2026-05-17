## Plan: Implementar ms-charge-account-worker com Arquitetura Hexagonal

Desenvolvimento de um sistema Java/Spring Boot para processamento de encargos em conta corrente, seguindo Arquitetura Hexagonal, DDD e Clean Architecture. O sistema processará até 20M registros/dia (04:00 às 06:00), validando contas via eventos e persistindo resultados em API REST.

### Steps

1. **Estruturação do Projeto Maven**
   - Criar estrutura base com pom.xml
   - Configurar dependências: Spring Boot, Spring Data JPA, Kafka, Lombok, Jackson, H2/PostgreSQL
   - Definir profiles (dev, test, prod)
   - Organizar módulos/diretórios segundo Arquitetura Hexagonal

2. **Camada Domain (DDD)**
   - Entidades de negócio: `Lancamento`, `Conta`, `ProcessamentoEncargo`
   - Value Objects: `StatusConta`, `TipoLancamento`, `Resultado`
   - Agregados com raiz agregada bem definida
   - Eventos de Domínio: `LancamentoRecebidoEvent`, `ContaValidadaEvent`, `LancamentoProcessadoEvent`
   - Repositórios como interfaces (ports)
   - Services de Domínio para lógica complexa

3. **Camada Application (Use Cases)**
   - `ProcessarLancamentoUseCase`: orquestração do fluxo
   - `ValidarContaUseCase`: validação de status da conta
   - `PublicarEventoUseCase`: publicação de eventos
   - DTOs de entrada/saída
   - Application Services com tratamento de exceções
   - Conversão de DTOs para entidades de domínio

4. **Ports (Interfaces)**
   - `LancamentoRepository`: CRUD de lançamentos
   - `ContaServicePort`: consulta status da conta (evento)
   - `PublishEventPort`: publicação de eventos para message broker
   - `FileReaderPort`: leitura do arquivo de lançamentos
   - `PersistResultPort`: persistência de resultados
   - `QueryLancamentoPort`: consulta de lançamentos processados

5. **Adapters (Implementações Concretas)**
   - **Repository Adapters**: JPA repositories, Spring Data
   - **Event Adapters**: Kafka publisher, Kafka consumer listeners
   - **File Adapters**: CSV reader, batch processing
   - **REST Adapters**: Spring MVC controllers para API
   - **Scheduler Adapters**: agendador (04:00) com @Scheduled

6. **Orquestração e Processamento**
   - Agendador para iniciar processamento às 04:00
   - Leitor de arquivo em batch/stream
   - Publicação de eventos de validação de conta
   - Consumidor de eventos de resposta de conta
   - Processamento condicional baseado no status
   - Publicação de evento para sistema contábil (sucesso)
   - Persistência de resultados

7. **Resiliência e Escalabilidade**
   - Circuit Breaker para chamadas a externos
   - Retry com backoff exponencial
   - Dead Letter Queue para mensagens com falha
   - Tratamento de idempotência (eventos duplicados)
   - Logging estruturado
   - Monitoramento com métricas (Micrometer)

8. **Testes**
   - Testes unitários para domínio (JUnit 5, Mockito)
   - Testes de use cases (testes de aplicação)
   - Testes de integração para adapters
   - Testes end-to-end da API
   - Cobertura de casos: conta ativa, cancelada, bloqueio judicial, arquivo vazio, erros de validação

### Estrutura de Diretórios (Arquitetura Hexagonal)

```
src/main/java/com/itau/chargeaccount/
├── domain/                          # Camada de Domínio (DDD)
│   ├── entity/                      # Entidades
│   │   ├── Lancamento.java
│   │   ├── Conta.java
│   │   └── ProcessamentoEncargo.java
│   ├── valueobject/                 # Value Objects
│   │   ├── StatusConta.java
│   │   ├── TipoLancamento.java
│   │   └── ResultadoProcessamento.java
│   ├── event/                       # Eventos de Domínio
│   │   ├── DomainEvent.java
│   │   ├── LancamentoRecebidoEvent.java
│   │   ├── ContaValidadaEvent.java
│   │   └── LancamentoProcessadoEvent.java
│   ├── repository/                  # Ports (Interfaces)
│   │   ├── LancamentoRepository.java
│   │   └── ContaServicePort.java
│   └── service/                     # Services de Domínio
│       └── ProcessamentoEncargoDomainService.java
├── application/                     # Camada de Aplicação
│   ├── usecase/                     # Use Cases
│   │   ├── ProcessarLancamentoUseCase.java
│   │   ├── ValidarContaUseCase.java
│   │   └── ConsultarLancamentoUseCase.java
│   ├── service/                     # Application Services
│   │   ├── ProcessamentoApplicationService.java
│   │   └── ConsultaApplicationService.java
│   ├── dto/                         # DTOs
│   │   ├── LancamentoDTO.java
│   │   ├── ContaStatusDTO.java
│   │   └── ResultadoProcessamentoDTO.java
│   └── port/                        # Ports (Interfaces)
│       ├── PublishEventPort.java
│       ├── FileReaderPort.java
│       └── PersistResultPort.java
├── infrastructure/                  # Camada de Infraestrutura (Adapters)
│   ├── persistence/                 # Adapters de Persistência
│   │   ├── jpa/
│   │   │   ├── LancamentoJpaRepository.java
│   │   │   ├── LancamentoJpaAdapter.java
│   │   │   └── LancamentoEntity.java
│   │   └── repository/
│   │       └── LancamentoRepositoryImpl.java
│   ├── event/                       # Adapters de Eventos
│   │   ├── KafkaPublisherAdapter.java
│   │   ├── KafkaListenerAdapter.java
│   │   └── KafkaConfiguration.java
│   ├── file/                        # Adapters de Leitura de Arquivo
│   │   ├── CsvFileReaderAdapter.java
│   │   └── FileConfiguration.java
│   ├── scheduler/                   # Adapters de Agendamento
│   │   └── ProcessamentoScheduler.java
│   └── config/                      # Configurações Spring
│       ├── ApplicationConfig.java
│       └── JpaConfig.java
├── presentation/                    # Camada de Apresentação (REST API)
│   ├── controller/
│   │   ├── ProcessamentoController.java
│   │   └── ConsultaController.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   └── ApiException.java
│   └── mapper/
│       └── DtoMapper.java
└── main/
    └── ChargeAccountApplication.java
```

### Dependencies (pom.xml)

```xml
<!-- Spring Boot -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-validation

<!-- Message Broker -->
spring-kafka

<!-- Database -->
h2 (dev)
postgresql (prod)

<!-- Utilities -->
lombok
jackson-dataformat-csv

<!-- Testing -->
junit-jupiter
mockito
spring-boot-starter-test
testcontainers
embedded-kafka

<!-- Monitoring -->
micrometer-core
```

### Further Considerations

1. **Message Broker**: Kafka é recomendado para 20M registros/dia. Oferece:
   - Escalabilidade horizontal
   - Particionamento automático
   - Replay de mensagens (debugging)
   - Dead Letter Topic nativo

2. **Persistência**: 
   - H2 para testes (em memória)
   - PostgreSQL para produção
   - Usar Spring Profiles para trocar facilmente

3. **Processamento em Batch**:
   - Kafka Streams ou Spring Batch?
   - Recomendação: Kafka Streams (streaming processamento em tempo real)
   - Alternativa: Spring Batch se arquivo muito grande

4. **Resiliência**:
   - Spring Retry para retries automáticas
   - Circuit Breaker com Resilience4j
   - Timeout nas chamadas externas
   - Dead Letter Queue para eventos que falharem

5. **Idempotência**:
   - Usar ID único de Lancamento como chave
   - Verificar duplicatas antes de processar
   - Usar transações com isolamento apropriado

6. **Monitoramento e Observabilidade**:
   - Logs estruturados (SLF4J + Logback)
   - Métricas com Micrometer (contador de processados, tempo)
   - Health checks (/actuator/health)
   - Jaeger/Zipkin para tracing distribuído (opcional)

### Checklist para Aprovação no Desafio

- [ ] Arquitetura Hexagonal bem definida (domain, application, infrastructure, presentation)
- [ ] DDD implementado: entidades, value objects, eventos, agregados, repositórios
- [ ] SOLID respeitado: dependency injection, single responsibility, open/closed
- [ ] Clean Architecture: independência de frameworks, testabilidade
- [ ] Separação de responsabilidades clara entre camadas
- [ ] Eventos como principal meio de comunicação
- [ ] API REST para consulta de lançamentos processados
- [ ] Persistência em banco de dados (JPA)
- [ ] Processamento escalável (Kafka)
- [ ] Tratamento de resiliência (retry, circuit breaker, DLQ)
- [ ] Testes unitários (>80% coverage domínio)
- [ ] Testes de integração
- [ ] README atualizado com instruções de execução
- [ ] Docker (obrigatório): Dockerfile + docker-compose
- [ ] CI/CD (opcional): GitHub Actions ou equivalente

