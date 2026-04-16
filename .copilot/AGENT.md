# 🤖 AGENT.md - Instruções para GitHub Copilot

Este documento define os padrões e diretrizes para que o GitHub Copilot trabalhe de forma consistente e profissional neste projeto.

---

## 📋 Índice

1. [Visão Geral do Projeto](#visão-geral-do-projeto)
2. [Padrão de Commits](#padrão-de-commits)
3. [Padrão de Code Review](#padrão-de-code-review)
4. [Padrão de Documentação e Planos](#padrão-de-documentação-e-planos)
5. [Guia de Estilo de Código](#guia-de-estilo-de-código)
6. [Checklist de Qualidade](#checklist-de-qualidade)

---

## 🎯 Visão Geral do Projeto

**Projeto:** `ms-charge-account-worker`  
**Propósito:** Sistema de processamento de encargos em contas correntes do Itaú  
**Arquitetura:** Hexagonal (Ports & Adapters) + DDD + Clean Architecture + SOLID  
**Stack:** Java 17+, Spring Boot 3.x, Kafka, PostgreSQL, JUnit 5

**Requisitos Técnicos:**
- Processamento de até 20M registros/dia (04:00 às 06:00)
- Validação de contas via eventos assíncronos
- Persistência de resultados em banco de dados
- API REST para consulta de lançamentos processados
- Comunicação baseada em eventos (Kafka)

---

## 📝 Padrão de Commits

### Formato: Conventional Commits

Todos os commits devem seguir o padrão **Conventional Commits**:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Tipos de Commit (`<type>`)

| Tipo | Descrição | Exemplo |
|------|-----------|---------|
| `feat` | Nova feature/funcionalidade | `feat(lancamento): adicionar validação de account` |
| `fix` | Correção de bug | `fix(processor): corrigir cálculo de encargos` |
| `refactor` | Refatoração de código sem mudança de comportamento | `refactor(domain): simplificar lógica de ProcessamentoEncargo` |
| `test` | Adição ou atualização de testes | `test(account-service): adicionar testes para StatusConta` |
| `docs` | Alterações em documentação | `docs(readme): atualizar instruções de setup` |
| `chore` | Mudanças de build, dependências, CI/CD | `chore(pom): atualizar versão do Spring Boot` |
| `perf` | Melhorias de performance | `perf(kafka): otimizar batch size para processamento` |
| `ci` | Alterações em CI/CD (GitHub Actions, etc) | `ci(workflow): adicionar testes automatizados` |

### Escopo (`<scope>`)

O escopo refere-se à camada ou componente afetado:

- **Domínio:** `domain`, `entity`, `valueobject`, `event`
- **Aplicação:** `application`, `usecase`, `service`
- **Infraestrutura:** `infrastructure`, `kafka`, `jpa`, `persistence`
- **Apresentação:** `presentation`, `controller`, `rest-api`
- **Configuração:** `config`, `pom`, `properties`

### Subject (`<subject>`)

- Usar imperativo, presente: "adicionar" não "adicionado" ou "adiciona"
- Não terminar com ponto (.)
- Máximo 50 caracteres
- Começar com letra minúscula
- Ser descritivo e conciso

### Body

- Explicar **o quê** e **por quê**, não como
- Envolver em 72 caracteres
- Separado do subject por uma linha em branco
- Opcional, mas recomendado para commits complexos

### Footer

Referências e breaking changes:

```
Closes #123
Refs: #456
BREAKING CHANGE: descrição da mudança que quebra compatibilidade
```

### Exemplos Completos

**Exemplo 1 - Feature simples:**
```
feat(domain): criar value object StatusConta

Implementa o value object StatusConta com os estados:
- ATIVA
- CANCELADA
- BLOQUEIO_JUDICIAL

Closes #15
```

**Exemplo 2 - Fix com breaking change:**
```
fix(kafka): corrigir mapeamento de mensagens de evento

O listener estava ignorando eventos com campos extras.
Agora utiliza @JsonAnySetter para ignorar campos desconhecidos.

BREAKING CHANGE: mudança na assinatura do evento ContaValidadaEvent
Refs: #42
```

**Exemplo 3 - Refactor:**
```
refactor(processor): extrair lógica de validação para domain service

Move a lógica complexa de validação para ProcessamentoEncargoDomainService,
seguindo os princípios de DDD e melhorando testabilidade.
```

---

## 🔍 Padrão de Code Review

### Checklist de Code Review

Todo código deve passar pelos seguintes critérios antes de ser mergeado:

#### ✅ Arquitetura & Design

- [ ] **Arquitetura Hexagonal respeitada:** Code segue separação entre domain/application/infrastructure/presentation?
- [ ] **Responsabilidade única (SRP):** Classe/função tem apenas uma razão para mudar?
- [ ] **Dependency Injection:** Usa DI ao invés de criar instâncias diretamente?
- [ ] **Interfaces (Ports):** Portas estão bem definidas? Adapters implementam contratos?
- [ ] **DDD aplicado:** Entidades, agregados, value objects estão corretamente modelados?
- [ ] **Sem violação de camadas:** Domain não importa de infrastructure? Presentation não contém lógica de negócio?

#### ✅ Qualidade de Código

- [ ] **Segue convenções Java:** Nomes de classes/métodos/variáveis claros e descritivos?
- [ ] **Sem magic numbers:** Constantes nominadas ao invés de valores hardcoded?
- [ ] **Sem duplicação:** Código repetido foi extraído para método/classe reutilizável?
- [ ] **Tratamento de exceções:** Try-catch apropriado? Lança exceções significativas?
- [ ] **Sem TODO/FIXME:** Ou está associado a issue aberta?
- [ ] **Logs apropriados:** Debug, info, warn, error usados corretamente?

#### ✅ Testes

- [ ] **Testes adicionados:** Cobertura de novos casos? Testes cobrem happy path e edge cases?
- [ ] **Testes passam:** CI verde? Nenhum teste ignorado (@Ignore)?
- [ ] **Assertions significativas:** Testes verificam o comportamento esperado claramente?
- [ ] **Sem testes frágeis:** Testes não são flaky, repetem sempre o mesmo resultado?
- [ ] **Nomenclatura:** Nomes descrevem o comportamento testado? (ex: `shouldThrowExceptionWhenAccountIsInactive`)

#### ✅ Performance & Resiliência

- [ ] **N+1 Queries evitado:** Lazy loading/eager loading apropriado?
- [ ] **Timeout configurado:** Chamadas externas têm timeout?
- [ ] **Retry logic:** Falhas transitórias têm retry logic?
- [ ] **Circuit breaker:** Proteção contra cascata de falhas?
- [ ] **Memory efficient:** Processamento batch não carrega tudo em memória?

#### ✅ Segurança

- [ ] **Sem secrets em código:** Variáveis sensíveis em `.env` ou properties?
- [ ] **SQL Injection:** Usando prepared statements, não string concatenation?
- [ ] **Validação de input:** Dados de entrada são validados?
- [ ] **CORS/Auth:** Endpoints protegidos apropriadamente?

#### ✅ Documentação

- [ ] **JavaDoc para public APIs:** Métodos/classes públicas têm comentários?
- [ ] **README atualizado:** Se adicionou feature, README foi atualizado?
- [ ] **Changelog:** Mudanças significativas no CHANGELOG.md?
- [ ] **Explicações de código complexo:** Lógica não óbvia tem comentários?

#### ✅ Git & Commits

- [ ] **Commits seguem padrão:** Formato Conventional Commits?
- [ ] **Histórico limpo:** Commits bem organizados, não mistura múltiplas features?
- [ ] **Branch nomeada:** Segue padrão `feature/xxx`, `fix/xxx`, `refactor/xxx`?
- [ ] **Sem merge conflicts:** Rebase/merge resolvidos apropriadamente?

### Critérios de Aprovação

**APROVADO** quando:
- ✅ Todos os checks acima foram avaliados e passaram
- ✅ Pelo menos 1 code review aprovado
- ✅ CI/CD pipeline passou (testes + linting)
- ✅ Sem conflitos com branch principal

**SOLICITADAS MUDANÇAS** quando:
- ❌ Violações de arquitetura ou design detectadas
- ❌ Cobertura de testes < 70% nas classes novas
- ❌ Commits não seguem padrão Conventional Commits
- ❌ Performance ou segurança são preocupações

---

## 📚 Padrão de Documentação e Planos

### Organização de Documentação

```
projeto-root/
├── doc/
│   ├── architecture/
│   │   ├── HEXAGONAL.md          # Explicação de Arquitetura Hexagonal
│   │   ├── DDD.md                # Domain-Driven Design neste projeto
│   │   └── DOMAIN_MODEL.md       # Modelo de domínio visual
│   ├── api/
│   │   ├── openapi.yaml          # Especificação OpenAPI/Swagger
│   │   └── endpoints.md          # Documentação de endpoints
│   ├── guides/
│   │   ├── SETUP.md              # Como configurar ambiente
│   │   ├── TESTING.md            # Estratégia de testes
│   │   └── DEPLOYMENT.md         # Como fazer deploy
│   └── decisions/
│       └── ADR_001_*.md          # Architecture Decision Records
├── .copilot/
│   └── AGENT.md                  # Este arquivo
├── README.md                      # Visão geral do projeto
├── CHANGELOG.md                   # Histórico de versões
└── CONTRIBUTING.md               # Guia de contribuição
```

### Padrão de Arquivos de Plano

**Nomenclatura:** `plan-${camelCaseName}.prompt.md`

**Localização:** `/doc/plans/` ou `.copilot/plans/`

**Estrutura Padrão:**

```markdown
# Plan: [Nome Descritivo do Plano]

[Uma a duas linhas descrevendo o plano em alto nível]

## Steps

1. **Passo Um**
   - Descrição detalhada
   - Sub-passos se necessário

2. **Passo Dois**
   - ...

## Estrutura de Diretórios (se aplicável)

```
src/
├── ...
```

## Dependências

```xml
<!-- lista de dependências -->
```

## Further Considerations

1. **Consideração Um**: Explicação e recomendação
2. **Consideração Dois**: ...

## Checklist de Conclusão

- [ ] Item 1
- [ ] Item 2
```

### Exemplos de Planos

- `plan-hexagonalArchitecture.prompt.md` - Implementação da Arquitetura Hexagonal
- `plan-kafkaIntegration.prompt.md` - Integração com Kafka
- `plan-testStrategy.prompt.md` - Estratégia de testes completa
- `plan-databaseMigration.prompt.md` - Migração de dados
- `plan-performanceTuning.prompt.md` - Otimização de performance

### Padrão de Architecture Decision Records (ADR)

Localização: `/doc/decisions/`

Nomenclatura: `ADR_NNN_${description_kebab_case}.md`

Exemplo:
```
ADR_001_use_kafka_over_rabbitmq.md
ADR_002_spring_data_jpa_for_persistence.md
ADR_003_hexagonal_architecture.md
```

**Template:**

```markdown
# ADR-NNN: [Título da Decisão]

**Date:** YYYY-MM-DD  
**Status:** Proposed | Accepted | Deprecated | Superseded by ADR-XXX

## Context

Descrever o contexto e problema que motivou a decisão.

## Decision

Descrição da decisão tomada.

## Rationale

Por que essa decisão foi escolhida. Quais foram as alternativas consideradas?

## Consequences

Consequências positivas e negativas da decisão.

## References

Links ou documentos relacionados.
```

---

## 🎨 Guia de Estilo de Código

### Java & Spring Boot

#### Convenção de Nomes

**Packages:**
```java
com.itau.chargeaccount.domain.entity
com.itau.chargeaccount.application.usecase
com.itau.chargeaccount.infrastructure.persistence
```

**Classes:**
- Entidades: `Lancamento`, `Conta`, `ProcessamentoEncargo`
- DTOs: `LancamentoDTO`, `ContaStatusDTO` (sufixo DTO)
- Exceções: `LancamentoInvalidoException`, `ContaNaoEncontradaException`
- Interfaces (Ports): `LancamentoRepository`, `PublishEventPort`
- Implementações (Adapters): `LancamentoJpaAdapter`, `KafkaPublisherAdapter`
- Services: `ProcessamentoApplicationService`, `ConsultaApplicationService`

**Métodos:**
```java
// Use verbos no infinitivo
processarLancamento()
validarConta()
publicarEvento()
consultarPorId()
salvar()
atualizar()
deletar()
```

**Variáveis:**
```java
// Descritivas e em camelCase
long contaId = 123L;
BigDecimal valorEncargo = new BigDecimal("100.50");
StatusConta statusConta = StatusConta.ATIVA;
List<Lancamento> lancamentosProcessados = new ArrayList<>();
```

#### Formatação

- **Indentação:** 4 espaços (ou equivalente em tabs)
- **Linha máxima:** 120 caracteres
- **Imports:** Organizados (java.*, javax.*, org.*, com.*)
- **Chaves:** Estilo K&R (abertura na mesma linha)

```java
public class Lancamento {
    
    private Long id;
    
    public void processar() {
        if (isValid()) {
            execute();
        }
    }
}
```

#### Anotações

- Usar Lombok: `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Builder`
- Usar `@NonNull` para campos obrigatórios
- Usar `@Validated` em classes de configuração
- Documentar com `@Deprecated` quando remover features

```java
@RequiredArgsConstructor
@Getter
public class Lancamento {
    
    @NonNull
    private final Long id;
    
    @NonNull
    private final BigDecimal valor;
}
```

#### Tratamento de Exceções

```java
// ✅ BOM - Específico e significativo
try {
    conta = servicoConta.obterStatus(contaId);
} catch (ContaNaoEncontradaException e) {
    logger.warn("Conta não encontrada: {}", contaId, e);
    lancamento.rejeitar("Conta não localizada");
}

// ❌ RUIM - Genérico e sem contexto
try {
    // ...
} catch (Exception e) {
    e.printStackTrace();
}
```

#### Logging

```java
// ✅ BOM
logger.debug("Processando lancamento com ID: {}", lancamento.getId());
logger.info("Lancamento processado com sucesso: {}", lancamento.getId());
logger.warn("Conta em bloqueio judicial. Lancamento rejeitado: {}", lancamentoId);
logger.error("Erro ao publicar evento para conta: {}", contaId, exception);

// ❌ RUIM
System.out.println("Processing: " + lancamento);
logger.info("" + lancamento);
```

#### Constants

```java
// ✅ BOM
public static final int BATCH_SIZE = 1000;
public static final Duration TIMEOUT = Duration.ofSeconds(30);
public static final String TOPIC_CONTA_STATUS = "conta.status.request";

// ❌ RUIM
int batchSize = 1000;
// ...
if (items.size() > 1000) { // Magic number!
```

### Estrutura de Classes

```java
@RequiredArgsConstructor
@Getter
public class Lancamento {
    
    // 1. Constantes
    private static final Logger logger = LoggerFactory.getLogger(Lancamento.class);
    
    // 2. Campos finais (imutáveis)
    @NonNull
    private final Long id;
    
    // 3. Campos mutáveis
    private StatusLancamento status;
    
    // 4. Construtor(es)
    private Lancamento(Long id, BigDecimal valor) {
        this.id = id;
    }
    
    // 5. Métodos públicos (comportamento)
    public void processar() {
        // ...
    }
    
    // 6. Métodos privados (helpers)
    private boolean isValid() {
        return true;
    }
    
    // 7. toString, equals, hashCode
    @Override
    public String toString() {
        return "Lancamento{" + "id=" + id + '}';
    }
}
```

---

## ✅ Checklist de Qualidade

### Antes de Commitar

- [ ] Código segue o guia de estilo
- [ ] Sem `System.out.println()` ou `e.printStackTrace()`
- [ ] Sem `TODO` sem issue associada
- [ ] Imports desnecessários removidos
- [ ] Sem código comentado (exceto comentários explicativos)

### Antes de Push

- [ ] Testes passam localmente: `mvn clean test`
- [ ] Build completo passa: `mvn clean package`
- [ ] Sem warnings de build
- [ ] Código não tem `@Ignore` tests

### Antes de Abrir PR

- [ ] Branch atualizado com `main`: `git rebase origin/main`
- [ ] Commits seguem padrão Conventional Commits
- [ ] PR description explica o quê foi feito e por quê
- [ ] README/documentação atualizado se necessário
- [ ] CHANGELOG.md atualizado

### Critérios de Feature Completa

- [ ] Feature implementada conforme requisitos
- [ ] Testes de unidade escritos (>70% coverage)
- [ ] Testes de integração passam
- [ ] Code review aprovado
- [ ] Documentação atualizada
- [ ] Commits squashados e semânticos

---

## 🔗 Referências

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spring Boot Best Practices](https://spring.io/guides)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)

---

**Última atualização:** 2026-04-16  
**Versão:** 1.0

