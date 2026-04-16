# 🧪 Estratégia de Testes

## 📋 Visão Geral

Testes são **parte essencial** do projeto. Garantem qualidade, facilitam refatorações e documentam o comportamento esperado.

**Objetivo:** >80% coverage no Domain, >70% na Application, >60% na Infrastructure.

## 🏗️ Pirâmide de Testes

```
        △
       / \
      /   \                  E2E
     /-----\      Testes de Integração
    /       \
   /         \      Testes de Aplicação
  /-----------\
 /             \   Testes Unitários
/_______________\
  (Maioria)
```

### 1️⃣ Testes Unitários (Base da Pirâmide)

**O que:** Testam unidades pequenas de código isoladamente (funções, métodos, classes).

**Onde:** `src/test/java/com/itau/chargeaccount/domain/`, `src/test/java/com/itau/chargeaccount/application/`

**Tecnologia:** JUnit 5, Mockito, AssertJ

**Cobertura esperada:** >80% (especialmente domain)

**Exemplo:**

```java
@DisplayName("Lancamento - Validação de Valor Mínimo")
class LancamentoTest {
    
    @DisplayName("Deve aceitar valor >= 0.01")
    @Test
    void deveAceitarValorMinimo() {
        // Arrange
        BigDecimal valor = new BigDecimal("0.01");
        Lancamento lancamento = new Lancamento(
            LancamentoId.gerar(),
            ContaId.valueOf(123L),
            valor,
            TipoLancamento.DEBITO
        );
        
        // Act & Assert
        assertDoesNotThrow(lancamento::validar);
    }
    
    @DisplayName("Deve rejeitar valor < 0.01")
    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-1.00", "0.001"})
    void deveRejeitar(String valor) {
        // Arrange
        Lancamento lancamento = new Lancamento(
            LancamentoId.gerar(),
            ContaId.valueOf(123L),
            new BigDecimal(valor),
            TipoLancamento.DEBITO
        );
        
        // Act & Assert
        assertThatThrownBy(lancamento::validar)
            .isInstanceOf(LancamentoInvalidoException.class)
            .hasMessageContaining("Valor mínimo");
    }
}
```

### 2️⃣ Testes de Aplicação

**O que:** Testam Use Cases e Application Services com suas dependências mockadas.

**Onde:** `src/test/java/com/itau/chargeaccount/application/`

**Tecnologia:** JUnit 5, Mockito, Spring (opcional: @SpringBootTest)

**Cobertura esperada:** >70%

**Exemplo:**

```java
@DisplayName("ProcessamentoApplicationService")
class ProcessamentoApplicationServiceTest {
    
    private ProcessamentoApplicationService service;
    private LancamentoRepository lancamentoRepository;
    private PublishEventPort publishEventPort;
    private ProcessamentoEncargoDomainService domainService;
    
    @BeforeEach
    void setup() {
        lancamentoRepository = mock(LancamentoRepository.class);
        publishEventPort = mock(PublishEventPort.class);
        domainService = mock(ProcessamentoEncargoDomainService.class);
        
        service = new ProcessamentoApplicationService(
            lancamentoRepository,
            publishEventPort,
            domainService
        );
    }
    
    @DisplayName("Deve processar lancamento com sucesso")
    @Test
    void deveProcessarComSucesso() {
        // Arrange
        LancamentoDTO dto = new LancamentoDTO(
            ContaId.valueOf(123L),
            BigDecimal.TEN,
            TipoLancamento.DEBITO
        );
        
        // Act
        ResultadoDTO resultado = service.processar(dto);
        
        // Assert
        assertThat(resultado.getStatus()).isEqualTo(StatusLancamento.PROCESSADO);
        
        // Verificar que repositório foi chamado
        verify(lancamentoRepository, times(1)).salvar(any(Lancamento.class));
        
        // Verificar que evento foi publicado
        verify(publishEventPort, times(1)).publicar(any(DomainEvent.class));
    }
}
```

### 3️⃣ Testes de Integração

**O que:** Testam a integração entre componentes (Controllers, Repositories, Event Listeners).

**Onde:** `src/test/java/com/itau/chargeaccount/infrastructure/`

**Tecnologia:** JUnit 5, TestContainers, Spring Test, Embedded Kafka

**Cobertura esperada:** >60%

**Exemplo:**

```java
@SpringBootTest
@DirtiesContext
@DisplayName("Processamento API Integration Tests")
class ProcessamentoControllerIT {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private LancamentoJpaAdapter lancamentoAdapter;
    
    @Autowired
    private LancamentoJpaRepository jpaRepository;
    
    @BeforeEach
    void setup() {
        jpaRepository.deleteAll();
    }
    
    @DisplayName("POST /api/v1/lancamentos - Deve processar lancamento")
    @Test
    void deveProcessarViaAPI() throws Exception {
        // Arrange
        LancamentoDTO dto = new LancamentoDTO(
            ContaId.valueOf(123L),
            BigDecimal.TEN,
            TipoLancamento.DEBITO
        );
        
        String json = new ObjectMapper().writeValueAsString(dto);
        
        // Act & Assert
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/lancamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PROCESSADO"));
    }
    
    @DisplayName("GET /api/v1/lancamentos/123 - Deve retornar lancamento")
    @Test
    void deveConsultarLancamento() throws Exception {
        // Arrange
        LancamentoEntity entity = new LancamentoEntity(
            null,
            123L,
            BigDecimal.TEN,
            "DEBITO",
            "PROCESSADO"
        );
        LancamentoEntity salvo = jpaRepository.save(entity);
        
        // Act & Assert
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/lancamentos/" + salvo.getId())
        )
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.valor").value("10"));
    }
}
```

### 4️⃣ Testes End-to-End (E2E)

**O que:** Testam fluxo completo com sistema real (banco de dados, broker, etc).

**Onde:** `src/test/java/com/itau/chargeaccount/e2e/`

**Tecnologia:** TestContainers, Testfixtures, Spring Boot Test

**Cobertura esperada:** Casos críticos apenas

**Exemplo:**

```java
@SpringBootTest
@DisplayName("Processamento End-to-End")
class ProcessamentoE2ETest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    
    @Test
    @DisplayName("Deve processar lancamento completo: recebimento -> validação -> processamento")
    void deveProcessarFluxoCompleto() throws Exception {
        // 1. Enviar lancamento
        LancamentoDTO lancamentoDTO = new LancamentoDTO(
            ContaId.valueOf(123L),
            BigDecimal.TEN,
            TipoLancamento.DEBITO
        );
        
        MvcResult result = mockMvc.perform(
            post("/api/v1/lancamentos")
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(lancamentoDTO))
        )
        .andExpect(status().isOk())
        .andReturn();
        
        // 2. Publicar evento de resposta de conta
        // (simulando resposta do sistema de contas)
        enviarEventoContaValidada(123L, "ATIVA");
        
        // Aguardar processamento
        Thread.sleep(1000);
        
        // 3. Verificar persistência
        LancamentoDTO processado = obterLancamento(lancamentoDTO.getId());
        assertThat(processado.getStatus()).isEqualTo("PROCESSADO");
    }
}
```

## 📐 Padrão de Nomenclatura

```
Test${ClassName}                       // Testes unitários
${ClassName}Test                       // Alternativa
${ClassName}IT (Integration Test)      // Testes de integração
${ClassName}E2ETest                    // Testes E2E
```

**Exemplos:**
- `LancamentoTest.java` - Testes do entity Lancamento
- `ProcessamentoApplicationServiceTest.java` - Testes de application service
- `LancamentoJpaAdapterIT.java` - Testes de integração de repository
- `ProcessamentoControllerIT.java` - Testes de integração de controller

## 📝 Estrutura de Teste (AAA Pattern)

Todo teste deve seguir o padrão **Arrange-Act-Assert**:

```java
@Test
@DisplayName("Deve fazer algo específico")
void deveAcao() {
    // 1. ARRANGE - Preparar dados
    Lancamento lancamento = new Lancamento(...);
    
    // 2. ACT - Executar ação
    lancamento.processar();
    
    // 3. ASSERT - Verificar resultado
    assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.PROCESSADO);
}
```

## 🎯 Boas Práticas

### ✅ DO's

- ✅ Use `@DisplayName` para descrever o comportamento testado
- ✅ Use `@ParameterizedTest` para testar múltiplos valores
- ✅ Use AssertJ para assertions fluentes: `assertThat(...).isEqualTo(...)`
- ✅ Mockize dependências externas em testes unitários
- ✅ Teste casos feliz, infeliz, e edge cases
- ✅ Use `@BeforeEach` para setup compartilhado
- ✅ Mantenha testes independentes (sem ordem de execução)
- ✅ Limpe dados de teste em `@AfterEach` se necessário

### ❌ DON'Ts

- ❌ Não ignore (`@Ignore`) testes sem motivo documentado
- ❌ Não teste detalhes de implementação
- ❌ Não crie testes com muitas asserções (< 5 por teste)
- ❌ Não use `Thread.sleep()` (use Awaitility)
- ❌ Não deixe testes frágeis (que falham intermitentemente)
- ❌ Não misture múltiplos comportamentos em um teste
- ❌ Não teste getters/setters triviais
- ❌ Não crie testes de testes

## 🔧 Ferramentas de Teste

### JUnit 5

Framework de teste padrão:

```java
@Test                          // Marca método como teste
@DisplayName("...")            // Nome legível
@BeforeEach                    // Setup antes de cada teste
@ParameterizedTest             // Teste com múltiplos parâmetros
@ValueSource(...)              // Fonte de parâmetros
```

### Mockito

Mock de dependências:

```java
Mock mockObject = mock(Mock.class);
when(mockObject.metodo()).thenReturn(valor);
verify(mockObject, times(1)).metodo();
```

### AssertJ

Assertions fluentes:

```java
assertThat(valor)
    .isNotNull()
    .isEqualTo(esperado)
    .isInstanceOf(String.class)
    .hasSize(5)
    .contains("a", "b");
```

### TestContainers

Contêineres para testes de integração:

```java
@Testcontainers
class IntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(...)
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");
}
```

### Embedded Kafka

Kafka embutido para testes:

```java
@EmbeddedKafka(partitions = 1, brokerProperties = {...})
class KafkaTest {
    // Testes com Kafka real
}
```

## 📊 Cobertura de Testes

### Medir Cobertura

```bash
# Gerar relatório de cobertura
mvn clean test jacoco:report

# Relatório em: target/site/jacoco/index.html
```

### Metas por Camada

| Camada | Cobertura Mínima |
|--------|-----------------|
| Domain | 80% |
| Application | 70% |
| Infrastructure | 60% |
| Presentation | 50% |

## ✅ Checklist para Submeter PR com Testes

- [ ] Testes escritos para nova funcionalidade
- [ ] Todos os testes passam: `mvn test`
- [ ] Cobertura atende metas por camada
- [ ] Testes seguem padrão AAA
- [ ] Nomes descrevem o comportamento
- [ ] Sem `@Ignore` sem motivo
- [ ] Sem `Thread.sleep()`
- [ ] Mocks foram utilizados corretamente
- [ ] Edge cases testados

## 🔗 Referências

- [JUnit 5 Documentation](https://junit.org/junit5/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/)
- [AssertJ Documentation](https://assertj.github.io/assertj-core/)
- [TestContainers Documentation](https://www.testcontainers.org/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)


