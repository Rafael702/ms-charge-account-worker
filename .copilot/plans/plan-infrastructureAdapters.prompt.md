## Plan: Implementar Infrastructure Layer com Adapters

Implementação completa da camada de infraestrutura com Adapters para persistência (JPA), eventos (Kafka), leitura de arquivos (CSV), agendamento e configurações. Esta fase conecta o domínio ao mundo externo mantendo a Arquitetura Hexagonal.

### Steps

1. **Criar Adapters de Persistência (JPA)**
   - Criar `LancamentoEntity.java` com mapeamento JPA
   - Criar `LancamentoJpaRepository.java` (Spring Data JPA)
   - Criar `LancamentoJpaAdapter.java` implementando `LancamentoRepository`
   - Configurar relacionamentos e constraints no banco
   - Adicionar índices para performance (ID da conta para queries)

2. **Criar Adapters de Eventos (Kafka)**
   - Criar `KafkaPublisherAdapter.java` implementando `PublishEventPort`
   - Criar `KafkaListenerAdapter.java` para consumir eventos
   - Criar `KafkaConfiguration.java` com tópicos e configurações
   - Implementar serialização/desserialização de eventos
   - Configurar Dead Letter Topic para falhas
   - Adicionar retry logic com Resilience4j

3. **Criar Adapters de Leitura de Arquivo**
   - Criar `CsvFileReaderAdapter.java` implementando `FileReaderPort`
   - Implementar stream processing para grandes arquivos
   - Adicionar validação de formato e campos obrigatórios
   - Configurar tratamento de erros (linhas inválidas)
   - Implementar batchprocessing eficiente

4. **Criar Scheduler Adapter**
   - Criar `ProcessamentoScheduler.java` com `@Scheduled(cron="0 4 * * *")`
   - Implementar lógica de leitura de arquivo
   - Orquestrar o fluxo: LER → PUBLICAR EVENTOS → PROCESSAR
   - Adicionar logging de início/fim
   - Implementar monitoramento com métricas

5. **Criar Configurações**
   - Criar `ApplicationConfig.java` com `@Configuration`
   - Criar `JpaConfig.java` para configuração de JPA
   - Criar `KafkaConfiguration.java` para Kafka
   - Criar `FileConfiguration.java` para File Reader
   - Usar properties injetadas via `@Value` ou `@ConfigurationProperties`

6. **Implementar Circuit Breaker e Retry**
   - Adicionar `@CircuitBreaker` ao Kafka Publisher
   - Adicionar `@Retry` para chamadas ao repositório
   - Implementar Dead Letter Queue para falhas permanentes
   - Configurar timeouts apropriados
   - Adicionar métricas de resiliência

7. **Testes de Integração**
   - Criar testes de integração com TestContainers (PostgreSQL)
   - Criar testes de integração com EmbeddedKafka
   - Criar testes E2E do fluxo completo
   - Validar persistência e recuperação de dados
   - Testar retry logic e Dead Letter Queue

8. **Configurações de Performance**
   - Configurar batch size para Kafka (16KB)
   - Configurar linger-ms para otimizar latência
   - Configurar pool de conexões do banco (20 conexões)
   - Adicionar índices nas queries frequentes
   - Implementar cache layer se necessário

### Estrutura de Diretórios

```
src/main/java/com/itau/chargeaccount/infrastructure/
├── persistence/
│   ├── jpa/
│   │   ├── LancamentoEntity.java
│   │   ├── LancamentoJpaRepository.java
│   │   └── LancamentoJpaAdapter.java
│   └── repository/
│       └── LancamentoRepositoryImpl.java (se usar múltiplas impl)
├── event/
│   ├── KafkaPublisherAdapter.java
│   ├── KafkaListenerAdapter.java
│   ├── KafkaConfiguration.java
│   └── event/
│       └── (mapeadores de eventos)
├── file/
│   ├── CsvFileReaderAdapter.java
│   └── FileConfiguration.java
├── scheduler/
│   └── ProcessamentoScheduler.java
└── config/
    ├── ApplicationConfig.java
    ├── JpaConfig.java
    └── FileConfiguration.java
```

### Dependencies (pom.xml)

```xml
<!-- Já presentes no pom.xml -->
spring-boot-starter-data-jpa
postgresql (runtime)
spring-kafka
jackson-dataformat-csv
resilience4j-spring-boot3
resilience4j-circuitbreaker
resilience4j-retry

<!-- Para testes de integração (já presente) -->
testcontainers
testcontainers/postgresql
spring-kafka-test

<!-- Novo (considerar adicionar) -->
org.springframework.cloud:spring-cloud-config-client (opcional)
```

### Further Considerations

1. **Persistência**: 
   - Usar Spring Data JPA para reduzir boilerplate
   - PostgreSQL em produção, H2 em testes/dev
   - Usar migrations com Flyway (opcional)
   - Implementar soft delete se necessário

2. **Kafka**:
   - Tópicos: `lancamento.recebido`, `conta.validada`, `lancamento.processado`, `lancamento.dlq`
   - Partitions: Usar contaId como partition key para ordering
   - Consumer group: `charge-account-group`
   - Offset: latest para produção, earliest para testes

3. **File Reader**:
   - Suportar streaming para arquivos > 1GB
   - Usar Apache Commons CSV ou Jackson CSV
   - Implementar validação lazy (durante stream)
   - Considerar S3/blob storage em produção

4. **Scheduler**:
   - Cron: `0 4 * * *` (04:00 todos os dias)
   - Usar `@EnableScheduling` na aplicação
   - Implementar DistributedLock se múltiplas instâncias
   - Registrar timestamp de início/fim para monitoring

5. **Resiliência**:
   - Circuit Breaker: failureRateThreshold 50%, waitDuration 30s
   - Retry: maxAttempts 3, waitDuration 1s
   - Timeout: 30 segundos para Kafka
   - Dead Letter: retenção de 7 dias

6. **Monitoramento**:
   - Métricas: contador de lançamentos processados, tempo de processamento
   - Logs: structured logging com SLF4J
   - Health checks: `/actuator/health` incluir status Kafka/DB
   - Jaeger: tracing distribuído (opcional)

### Checklist de Conclusão

- [ ] LancamentoEntity mapeada com JPA
- [ ] LancamentoJpaRepository criado (Spring Data)
- [ ] LancamentoJpaAdapter implementa LancamentoRepository
- [ ] KafkaPublisherAdapter implementa PublishEventPort
- [ ] KafkaListenerAdapter consome eventos
- [ ] CsvFileReaderAdapter implementa FileReaderPort
- [ ] ProcessamentoScheduler agendador configurado
- [ ] ApplicationConfig com @Configuration
- [ ] Circuit Breaker + Retry implementados
- [ ] Testes de integração com TestContainers
- [ ] Testes com EmbeddedKafka
- [ ] Performance: batch size, linger-ms configurados
- [ ] Índices criados no banco de dados
- [ ] Dead Letter Queue configurada
- [ ] Métricas com Micrometer adicionadas
- [ ] Health checks implementados
- [ ] Logs estruturados com MDC
- [ ] Documentação de cada adapter
- [ ] Code review realizado
- [ ] Testes de integração > 80% coverage


