# Domain-Driven Design (DDD)

## 📚 Visão Geral

**Domain-Driven Design (DDD)** é uma filosofia de desenvolvimento que coloca o domínio de negócio no centro do design de software. O objetivo é criar modelos que refletem a realidade do negócio e facilitam comunicação entre desenvolvedores e especialistas de negócio.

## 🎯 Linguagem Ubíqua

A **Linguagem Ubíqua** é a base do DDD. É um vocabulário compartilhado entre desenvolvedores e especialistas de negócio.

### Neste Projeto

| Termo | Significado |
|-------|------------|
| **Lancamento** | Um débito ou crédito a ser aplicado em uma conta corrente |
| **Encargo** | Sinônimo de lançamento (fee/charge) |
| **Conta** | Conta corrente do cliente |
| **StatusConta** | Estados possíveis: ATIVA, CANCELADA, BLOQUEIO_JUDICIAL |
| **Processamento** | Ato de validar e aplicar um lançamento |
| **Rejeição** | Quando um lançamento não pode ser processado |
| **Validação** | Verificação se a conta está elegível para processar |

## 🏗️ Blocos de Construção do DDD

### 1️⃣ Entities (Entidades)

Objetos que possuem **identidade única** e **continuidade ao longo do tempo**. A identidade persiste mesmo se os atributos mudem.

**Exemplo: Lancamento**

```java
@Getter
@RequiredArgsConstructor
public class Lancamento {
    
    private final LancamentoId id;  // Identidade única
    private final ContaId contaId;
    private final BigDecimal valor;
    private final TipoLancamento tipo;
    private StatusLancamento status;
    private Instant dataCriacao;
    private Instant dataProcessamento;
    
    // Dois lançamentos com mesmo ID são considerados iguais
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lancamento)) return false;
        Lancamento that = (Lancamento) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

**Características:**
- ✅ Identidade única
- ✅ Igualdade baseada em ID
- ✅ Ciclo de vida (nascimento, mudanças, morte)
- ✅ Pode ser mutável
- ✅ Encapsula comportamento e regras

### 2️⃣ Value Objects (Objetos de Valor)

Objetos **sem identidade única**. São iguais se todos os atributos são iguais. Imutáveis.

**Exemplo: StatusConta**

```java
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class StatusConta {
    
    private final String valor;
    
    // Factory methods
    public static StatusConta ATIVA = new StatusConta("ATIVA");
    public static StatusConta CANCELADA = new StatusConta("CANCELADA");
    public static StatusConta BLOQUEIO_JUDICIAL = new StatusConta("BLOQUEIO_JUDICIAL");
    
    // Comportamento
    public boolean podeProcessar() {
        return this.equals(ATIVA);
    }
    
    public boolean ehBloqueada() {
        return this.equals(BLOQUEIO_JUDICIAL) || this.equals(CANCELADA);
    }
}
```

**Características:**
- ✅ Sem identidade (ID)
- ✅ Imutáveis
- ✅ Igualdade por valor (todos os atributos)
- ✅ Reutilizáveis
- ✅ Encapsulam conceitos de negócio

**Mais Exemplos de Value Objects:**

```java
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ContaId {
    private final Long valor;
    
    public static ContaId valueOf(Long valor) {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("ContaId inválida");
        }
        return new ContaId(valor);
    }
}

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class Moeda {
    private final BigDecimal valor;
    private final String moeda; // "BRL", "USD"
    
    public Moeda adicionar(Moeda outra) {
        if (!this.moeda.equals(outra.moeda)) {
            throw new DominioException("Moedas diferentes");
        }
        return new Moeda(valor.add(outra.valor), moeda);
    }
}

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public enum TipoLancamento {
    DEBITO("D", "Débito"),
    CREDITO("C", "Crédito");
    
    private final String codigo;
    private final String descricao;
}
```

### 3️⃣ Aggregates (Agregados)

Grupos de entidades e value objects que funcionam como uma unidade. Possuem uma **raiz agregada**.

**Exemplo: Agregado de Processamento**

```java
@Getter
@RequiredArgsConstructor
public class ProcessamentoEncargo {
    
    // Raiz do agregado
    private final ProcessamentoId id;
    
    // Entidades internas
    private final Lancamento lancamento;
    private final Conta conta;
    
    // Value Objects
    private ResultadoProcessamento resultado;
    private List<EventoProcessamento> eventos = new ArrayList<>();
    
    // Invariantes de negócio
    public void processar() {
        if (!conta.statusEhValido()) {
            resultado = ResultadoProcessamento.REJEITADO("Conta inválida");
            registrarEvento(new LancamentoRejeitadoEvent(lancamento.getId()));
            return;
        }
        
        conta.aplicarLancamento(lancamento);
        resultado = ResultadoProcessamento.SUCESSO();
        registrarEvento(new LancamentoProcessadoEvent(lancamento.getId()));
    }
    
    // Apenas a raiz do agregado é acessada de fora
    public Lancamento obterLancamento() {
        return this.lancamento;
    }
    
    // Agregados publicam eventos
    public List<EventoProcessamento> obterEventos() {
        return Collections.unmodifiableList(eventos);
    }
    
    private void registrarEvento(EventoProcessamento evento) {
        this.eventos.add(evento);
    }
}
```

**Características:**
- ✅ Raiz agregada (entrada única)
- ✅ Encapsula entidades relacionadas
- ✅ Mantém invariantes (regras que sempre devem ser verdadeiras)
- ✅ Comunica via eventos
- ✅ Facilita transações

### 4️⃣ Domain Events (Eventos de Domínio)

Representam algo importante que aconteceu no domínio. **Imutáveis** e **reutilizáveis**.

```java
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public abstract class DomainEvent {
    
    protected final Instant ocorridoEm;
    protected final String nomeEvento;
    
    public DomainEvent() {
        this.ocorridoEm = Instant.now();
        this.nomeEvento = this.getClass().getSimpleName();
    }
}

@Getter
public class LancamentoRecebidoEvent extends DomainEvent {
    private final LancamentoId lancamentoId;
    private final ContaId contaId;
    private final BigDecimal valor;
    
    public LancamentoRecebidoEvent(LancamentoId lancamentoId, ContaId contaId, BigDecimal valor) {
        super();
        this.lancamentoId = lancamentoId;
        this.contaId = contaId;
        this.valor = valor;
    }
}

@Getter
public class ContaValidadaEvent extends DomainEvent {
    private final ContaId contaId;
    private final StatusConta status;
    
    public ContaValidadaEvent(ContaId contaId, StatusConta status) {
        super();
        this.contaId = contaId;
        this.status = status;
    }
}

@Getter
public class LancamentoProcessadoEvent extends DomainEvent {
    private final LancamentoId lancamentoId;
    private final ResultadoProcessamento resultado;
    
    public LancamentoProcessadoEvent(LancamentoId lancamentoId, ResultadoProcessamento resultado) {
        super();
        this.lancamentoId = lancamentoId;
        this.resultado = resultado;
    }
}
```

**Características:**
- ✅ Imutáveis
- ✅ Representam fatos históricos
- ✅ Comunicação assíncrona entre agregados
- ✅ Rastreabilidade de mudanças
- ✅ Facilitam auditing

### 5️⃣ Domain Services (Serviços de Domínio)

Lógica de negócio que não pertence a uma entidade específica. Orquestram operações entre múltiplos agregados.

```java
@RequiredArgsConstructor
public class ProcessamentoEncargoDomainService {
    
    private final ContaRepositorio contaRepositorio;
    private final NotificadorStatusContaPort notificador;
    
    public ProcessamentoEncargo processar(Lancamento lancamento) {
        
        // 1. Solicitam status da conta (evento assíncrono)
        notificador.solicitarStatus(lancamento.getContaId());
        
        // 2. Aguardam resposta (simulado aqui - em produção seria via evento)
        StatusConta status = aguardarStatusDaConta(lancamento.getContaId());
        
        // 3. Orquestram validação
        if (!status.podeProcessar()) {
            lancamento.rejeitar(String.format("Conta em status %s", status.getValor()));
            return criarProcessamentoRejeitado(lancamento);
        }
        
        // 4. Recuperam conta e aplicam lançamento
        Conta conta = contaRepositorio.obterPorId(lancamento.getContaId());
        conta.aplicarLancamento(lancamento);
        
        // 5. Retornam agregado com resultado
        return criarProcessamentoSucesso(lancamento, conta);
    }
    
    private ProcessamentoEncargo criarProcessamentoSucesso(Lancamento lancamento, Conta conta) {
        return new ProcessamentoEncargo(
            ProcessamentoId.gerar(),
            lancamento,
            conta,
            ResultadoProcessamento.SUCESSO()
        );
    }
    
    private ProcessamentoEncargo criarProcessamentoRejeitado(Lancamento lancamento) {
        return new ProcessamentoEncargo(
            ProcessamentoId.gerar(),
            lancamento,
            null,
            ResultadoProcessamento.REJEITADO(lancamento.getMotivoBloqueio())
        );
    }
}
```

### 6️⃣ Repositories (Repositórios)

Interfaces que abstraem a persistência. Simulam uma coleção de agregados em memória.

```java
// Interface no Domain
public interface LancamentoRepository {
    void salvar(Lancamento lancamento);
    Optional<Lancamento> porId(LancamentoId id);
    List<Lancamento> porConta(ContaId contaId);
    List<Lancamento> processos(StatusLancamento status);
}

public interface ContaRepository {
    void salvar(Conta conta);
    Optional<Conta> porId(ContaId id);
}

// Implementação na Infrastructure
@Repository
@RequiredArgsConstructor
public class LancamentoJpaAdapter implements LancamentoRepository {
    
    private final LancamentoJpaRepository jpaRepository;
    private final LancamentoMapper mapper;
    
    @Override
    public void salvar(Lancamento lancamento) {
        LancamentoEntity entity = mapper.toEntity(lancamento);
        jpaRepository.save(entity);
    }
    
    @Override
    public Optional<Lancamento> porId(LancamentoId id) {
        return jpaRepository.findById(id.valor())
            .map(mapper::toDomain);
    }
}
```

### 7️⃣ Bounded Contexts (Contextos Delimitados)

Definições claras de escopos de domínio. Cada contexto tem sua própria linguagem ubíqua.

**Exemplo neste projeto:**

```
┌─────────────────────────────────────┐
│   Contexto: Processamento           │
│   Entidades: Lancamento             │
│   Agregados: ProcessamentoEncargo   │
│   Eventos: LancamentoProcessado     │
└─────────────────────────────────────┘
           ↓ (evento)
┌─────────────────────────────────────┐
│   Contexto: Contabilidade           │
│   Entidades: LancamentoContabil     │
│   Agregados: RegistroContabil       │
│   Eventos: ContabilizacaoRealizada  │
└─────────────────────────────────────┘
```

## 🔍 Modelo de Domínio deste Projeto

### Entidades

- **Lancamento**: Débito/crédito a processar
- **Conta**: Conta corrente com histórico
- **ProcessamentoEncargo**: Execução de um processamento

### Value Objects

- **LancamentoId**: Identificador único
- **ContaId**: Identificador de conta
- **StatusConta**: ATIVA, CANCELADA, BLOQUEIO_JUDICIAL
- **TipoLancamento**: DEBITO, CREDITO
- **StatusLancamento**: PENDENTE, PROCESSADO, REJEITADO
- **ResultadoProcessamento**: Sucesso/Falha com motivos

### Agregados

- **ProcessamentoEncargo**: Raiz agregada contendo:
  - Lancamento (entidade)
  - Conta (entidade)
  - ResultadoProcessamento (value object)
  - Lista de eventos

### Domain Events

- `LancamentoRecebidoEvent`: Novo lançamento recebido
- `SolicitacaoValidacaoContaEvent`: Solicitação de validação
- `ContaValidadaEvent`: Resposta com status da conta
- `LancamentoProcessadoEvent`: Lançamento processado com sucesso
- `LancamentoRejeitadoEvent`: Lançamento rejeitado
- `LancamentoContabilizadoEvent`: Enviado para sistema contábil

### Domain Services

- `ProcessamentoEncargoDomainService`: Orquestra o fluxo completo

## 📐 Exemplo Completo: Fluxo de Processamento

```java
// 1. Controlador recebe requisição
@PostMapping("/lancamentos")
public ResponseEntity<ResultadoDTO> processar(@RequestBody LancamentoDTO dto) {
    // ...
}

// 2. Application Service prepara dados
@Service
public class ProcessamentoApplicationService {
    public ResultadoDTO processar(LancamentoDTO dto) {
        // Converte DTO → Entity
        Lancamento lancamento = mapper.toDomain(dto);
        
        // Delegado para domínio
        ProcessamentoEncargo resultado = domainService.processar(lancamento);
        
        // Publica eventos
        resultado.obterEventos().forEach(publishEventPort::publicar);
        
        // Persiste
        lancamentoRepository.salvar(resultado.getLancamento());
        
        // Retorna resultado
        return mapper.toDTO(resultado);
    }
}

// 3. Domain Service orquestra o negócio
public ProcessamentoEncargo processar(Lancamento lancamento) {
    // Valida invariante: lancamento com valor positivo
    lancamento.validar();
    
    // Solicita status da conta
    StatusConta status = contaService.obterStatus(lancamento.getContaId());
    
    // Processa conforme status
    if (status.podeProcessar()) {
        lancamento.processar();
        publicar(new LancamentoProcessadoEvent(lancamento.getId()));
    } else {
        lancamento.rejeitar("Conta indisponível");
        publicar(new LancamentoRejeitadoEvent(lancamento.getId()));
    }
    
    return new ProcessamentoEncargo(lancamento, status);
}
```

## ✅ Checklist de DDD Correto

- [ ] Linguagem ubíqua clara e consistente
- [ ] Entidades com identidade única
- [ ] Value Objects imutáveis e com igualdade por valor
- [ ] Agregados com raiz bem definida
- [ ] Eventos de domínio para comunicação assíncrona
- [ ] Domain Services para lógica cross-agregado
- [ ] Repositories como interfaces (sem detalhe técnico)
- [ ] Bounded Contexts claros e separados
- [ ] Invariantes de negócio encapsulados
- [ ] Domain layer testável sem dependências externas

## 📚 Referências

- [Domain-Driven Design - Eric Evans](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [DDD Quickly - Abel Avram & Floyd Marinescu](https://www.infoq.com/minibooks/domain-driven-design-quickly/)
- [Implementing Domain-Driven Design - Vaughn Vernon](https://vaughnvernon.com/implementing-domain-driven-design/)
