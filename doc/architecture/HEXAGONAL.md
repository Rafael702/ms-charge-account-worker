# Arquitetura Hexagonal (Ports & Adapters)

## 📐 Visão Geral

A Arquitetura Hexagonal, também conhecida como **Ports & Adapters**, é um padrão arquitetural que busca isolar a lógica de negócio (Domain) das dependências externas (frameworks, bibliotecas, sistemas externos).

## 🎯 Objetivos

- ✅ **Independência de Framework:** A lógica de negócio não conhece Spring, Hibernate, Kafka, etc.
- ✅ **Testabilidade:** Fácil escrever testes unitários sem dependências externas
- ✅ **Manutenibilidade:** Mudanças em frameworks não afetam regras de negócio
- ✅ **Flexibilidade:** Trocar implementações (ex: banco de dados) sem afetar domínio
- ✅ **Escalabilidade:** Cada camada pode evoluir independentemente

## 🏗️ Estrutura em 4 Camadas

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION (REST)                   │
│              Controllers, DTOs, Exception Handlers       │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────┐
│                   APPLICATION LAYER                      │
│          Use Cases, Application Services, DTOs           │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────┐
│                    DOMAIN LAYER (Core)                   │
│     Entities, Value Objects, Aggregates, Events, Rules   │
└──────────────────────────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────┐
│                INFRASTRUCTURE (Adapters)                 │
│    JPA, Kafka, REST Clients, File I/O, Scheduling       │
└──────────────────────────────────────────────────────────┘
```

### Fluxo de Dados (Inbound - Entrada)

```
HTTP Request
    ↓
Controller (Presentation)
    ↓
ApplicationService (Application)
    ↓
UseCase / Domain Service (Domain)
    ↓
Entities / Value Objects (Domain)
```

### Fluxo de Dados (Outbound - Saída)

```
Domain Service (Domain)
    ↓
Repository Port (Interface)
    ↓
RepositoryAdapter (Infrastructure)
    ↓
JPA / Kafka / File System
```

## 📍 Camadas Detalhadas

### 1️⃣ Domain Layer (Camada de Domínio)

**Responsabilidade:** Lógica de negócio pura

**Contém:**
- `Entities`: `Lancamento`, `Conta`, `ProcessamentoEncargo`
- `Value Objects`: `StatusConta`, `TipoLancamento`, `Moeda`
- `Aggregates`: Raízes de agregados bem definidas
- `Domain Events`: `LancamentoRecebidoEvent`, `ContaValidadaEvent`
- `Domain Services`: Lógica que não pertence a uma entidade
- `Repositories` (interfaces): `LancamentoRepository`, `ContaRepository`
- `Exceptions`: `ContaInativaException`, `LancamentoInvalidoException`

**Características:**
- ✅ Zero dependências externas (sem Spring, sem JPA)
- ✅ Testável sem Mock
- ✅ Independente de framework
- ✅ Princípio Single Responsibility

**Exemplo:**

```java
@Getter
@RequiredArgsConstructor
public class Lancamento {
    
    private final LancamentoId id;
    private final ContaId contaId;
    private final BigDecimal valor;
    private final TipoLancamento tipo;
    private StatusLancamento status = StatusLancamento.PENDENTE;
    
    public void processar(StatusConta statusConta) {
        if (!statusConta.podeProcessar()) {
            this.status = StatusLancamento.REJEITADO;
            throw new ContaInativaException("Conta não está ativa");
        }
        this.status = StatusLancamento.PROCESSADO;
    }
}
```

### 2️⃣ Application Layer (Camada de Aplicação)

**Responsabilidade:** Orquestração entre Domain e Presentation

**Contém:**
- `Application Services`: `ProcessamentoApplicationService`
- `Use Cases`: `ProcessarLancamentoUseCase`, `ValidarContaUseCase`
- `DTOs`: `LancamentoDTO`, `ContaStatusDTO`
- `Input/Output Ports` (interfaces): `PublishEventPort`, `FileReaderPort`
- `Mappers`: Conversão entre DTOs e Entities

**Características:**
- ✅ Orquestra o fluxo entre camadas
- ✅ Conversor de DTOs ↔ Entities
- ✅ Coordena transações
- ✅ Conhece Ports (interfaces), não Adapters (implementações)

**Exemplo:**

```java
@Service
@RequiredArgsConstructor
public class ProcessamentoApplicationService {
    
    private final ProcessamentoEncargoDomainService domainService;
    private final LancamentoRepository lancamentoRepository;
    private final PublishEventPort publishEventPort;
    
    public ResultadoDTO processar(LancamentoDTO dto) {
        // Converter DTO → Entity
        Lancamento lancamento = LancamentoMapper.toDomain(dto);
        
        // Chamar domínio
        domainService.processar(lancamento);
        
        // Persistir
        lancamentoRepository.salvar(lancamento);
        
        // Publicar evento
        publishEventPort.publicar(new LancamentoProcessadoEvent(lancamento.getId()));
        
        // Retornar DTO
        return LancamentoMapper.toDTO(lancamento);
    }
}
```

### 3️⃣ Presentation Layer (Camada de Apresentação)

**Responsabilidade:** Interface com o mundo exterior (HTTP, eventos, CLI, etc)

**Contém:**
- `Controllers`: Endpoints REST
- `DTOs**: Contrato de comunicação
- `Exception Handlers`: Tratamento de erros HTTP
- `Mappers`: Conversão entre DTO e Application Input/Output

**Características:**
- ✅ Converte HTTP → DTO → Application Service
- ✅ Converte Response → DTO → HTTP
- ✅ Conhece apenas Application Layer

**Exemplo:**

```java
@RestController
@RequestMapping("/api/v1/lancamentos")
@RequiredArgsConstructor
public class ProcessamentoController {
    
    private final ProcessamentoApplicationService applicationService;
    
    @PostMapping
    public ResponseEntity<ResultadoDTO> processar(@RequestBody LancamentoDTO dto) {
        ResultadoDTO resultado = applicationService.processar(dto);
        return ResponseEntity.ok(resultado);
    }
}
```

### 4️⃣ Infrastructure Layer (Camada de Infraestrutura)

**Responsabilidade:** Implementações concretas dos Ports (Adapters)

**Contém:**
- `Repositories`: JPA implementations
- `Event Publishers**: Kafka, RabbitMQ adapters
- `File Readers`: CSV readers, batch processors
- `External Clients`: REST clients
- `Schedulers`: Agendadores
- `Configuration`: Spring Configuration classes

**Características:**
- ✅ Implementa interfaces (Ports) do Domain/Application
- ✅ Conhece frameworks e bibliotecas
- ✅ Isolada da lógica de negócio

**Exemplo:**

```java
@Repository
@RequiredArgsConstructor
public class LancamentoJpaAdapter implements LancamentoRepository {
    
    private final LancamentoJpaRepository jpaRepository;
    
    @Override
    public void salvar(Lancamento lancamento) {
        LancamentoEntity entity = LancamentoMapper.toEntity(lancamento);
        jpaRepository.save(entity);
    }
    
    @Override
    public Optional<Lancamento> porId(LancamentoId id) {
        return jpaRepository.findById(id.valor())
                .map(LancamentoMapper::toDomain);
    }
}
```

## 🔌 Ports (Interfaces)

Ports são **contratos/interfaces** que a Application Layer usa. Elas definem o que é necessário, não como implementar.

**Exemplos:**

```java
// Domain Port
public interface LancamentoRepository {
    void salvar(Lancamento lancamento);
    Optional<Lancamento> porId(LancamentoId id);
}

// Application Port
public interface PublishEventPort {
    void publicar(DomainEvent evento);
}

// Aplicação Port
public interface FileReaderPort {
    Stream<LancamentoDTO> lerArquivo(String caminho);
}
```

## 🔗 Adapters (Implementações)

Adapters são as **implementações concretas** dos Ports. Lidam com detalhes técnicos.

**Exemplos:**

```java
// JPA Adapter
@Repository
public class LancamentoJpaAdapter implements LancamentoRepository { }

// Kafka Adapter
@Component
public class KafkaPublisherAdapter implements PublishEventPort { }

// CSV Adapter
@Component
public class CsvFileReaderAdapter implements FileReaderPort { }
```

## 📊 Fluxo Completo: Processamento de Lancamento

```
1. HTTP POST /api/v1/lancamentos
        ↓
2. ProcessamentoController recebe JSON
        ↓
3. Valida e converte para DTO
        ↓
4. Chama ProcessamentoApplicationService.processar(DTO)
        ↓
5. ApplicationService converte DTO → Lancamento (Entity)
        ↓
6. Chama ProcessamentoEncargoDomainService.processar()
        ↓
7. DomainService orquestra validações de negócio
        ↓
8. Usa PublishEventPort para solicitar status da conta
        ↓
9. Aguarda evento de resposta (Kafka listener)
        ↓
10. Atualiza Lancamento com status (PROCESSADO ou REJEITADO)
        ↓
11. ApplicationService persiste via LancamentoRepository (Port)
        ↓
12. LancamentoJpaAdapter (Adapter) implementa a persistência
        ↓
13. JPA/Hibernate persiste em banco de dados
        ↓
14. Publica evento "LancamentoProcessado" para sistema contábil
        ↓
15. Retorna DTO com resultado
        ↓
16. HTTP 200 OK com JSON de resposta
```

## 🧪 Testabilidade

Uma das maiores vantagens da Arquitetura Hexagonal é a testabilidade:

```java
@DisplayName("Deve processar lancamento com sucesso")
@Test
void testProcessarComSucesso() {
    // Arrange - Setup sem Spring!
    Lancamento lancamento = new Lancamento(
        LancamentoId.gerar(),
        ContaId.valueOf(123L),
        BigDecimal.TEN,
        TipoLancamento.DEBITO
    );
    
    // Act
    lancamento.processar(StatusConta.ATIVA);
    
    // Assert
    assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.PROCESSADO);
}

@DisplayName("Deve rejeitar lancamento com conta inativa")
@Test
void testRejeiarComContaInativa() {
    // Arrange
    Lancamento lancamento = new Lancamento(...);
    
    // Act & Assert
    assertThatThrownBy(() -> lancamento.processar(StatusConta.CANCELADA))
        .isInstanceOf(ContaInativaException.class);
}
```

## ✅ Checklist de Aplicação Correta

- [ ] Domain layer sem dependências externas (sem Spring, sem JPA)
- [ ] Domain services testáveis sem mock
- [ ] Ports como interfaces em domain/application
- [ ] Adapters implementam ports
- [ ] Presentation conhece apenas Application
- [ ] Application conhece apenas Domain
- [ ] Infrastructure implementa Application/Domain ports
- [ ] Fluxo de dados obedece as camadas
- [ ] Entities são imutáveis ou semi-imutáveis
- [ ] Value Objects modelam conceitos de negócio

## 📚 Referências

- [Alistair Cockburn - Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design - Eric Evans](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Spring Boot Best Practices](https://spring.io/guides)


