# 🤝 CONTRIBUTING.md - Guia de Contribuição

Bem-vindo ao projeto `ms-charge-account-worker`! Este guia descreve como contribuir de forma efetiva e profissional.

## 📋 Antes de Começar

1. Leia o [`README.md`](../README.md) - Entenda os objetivos do projeto
2. Leia o [`.copilot/AGENT.md`](../../.copilot/AGENT.md) - Padrões e diretrizes
3. Leia [`doc/architecture/HEXAGONAL.md`](../architecture/HEXAGONAL.md) - Arquitetura do projeto
4. Leia [`doc/architecture/DDD.md`](../architecture/DDD.md) - Conceitos de Domain-Driven Design

## 🚀 Como Configurar o Ambiente

### Pré-requisitos

- Java 17 ou superior
- Maven 3.8.1 ou superior
- Docker & Docker Compose (opcional, para Kafka/PostgreSQL)
- Git

### Setup Local

```bash
# 1. Clone o repositório
git clone https://github.com/itau/ms-charge-account-worker.git
cd ms-charge-account-worker

# 2. Instale dependências
mvn clean install

# 3. Inicie serviços auxiliares (opcional)
docker-compose up -d

# 4. Execute os testes
mvn test

# 5. Inicie a aplicação
mvn spring-boot:run
```

## 🔄 Workflow de Contribuição

### 1. Crie uma Branch

```bash
# Atualize a main
git checkout main
git pull origin main

# Crie uma branch seguindo o padrão
git checkout -b feature/nome-descritivo
# ou
git checkout -b fix/nome-descritivo
# ou
git checkout -b refactor/nome-descritivo
```

### 2. Faça as Alterações

- Siga os padrões em [`.copilot/AGENT.md`](../../.copilot/AGENT.md)
- Escreva código testável e bem documentado
- Rode os testes localmente: `mvn test`
- Verifique o build: `mvn clean package`

### 3. Commit com Mensagem Semântica

```bash
# Exemplo de bom commit
git commit -m "feat(domain): criar value object StatusConta

Implementa value object para representar estados de conta.
Estados: ATIVA, CANCELADA, BLOQUEIO_JUDICIAL

Closes #123"

# Outro exemplo
git commit -m "test(processor): adicionar testes para ProcessamentoEncargoDomainService"

# Refatoração
git commit -m "refactor(entity): simplificar lógica de validação em Lancamento"
```

Consulte [Conventional Commits](https://www.conventionalcommits.org/) se tiver dúvidas.

### 4. Push e Abra uma Pull Request

```bash
# Push para remoto
git push origin feature/nome-descritivo

# Abra PR no GitHub
# Título: mesmo da primeira linha do commit
# Descrição: explique o quê, por quê e como
```

### 5. Code Review

- Aguarde revisão da equipe
- Responda a comentários profissionalmente
- Faça ajustes solicitados
- Re-force após mudanças: `git push origin feature/nome-descritivo`

### 6. Merge

Após aprovação, a equipe fará merge à `main`.

## 📝 Checklist para PR

Antes de submeter sua PR, verifique:

### ✅ Código

- [ ] Segue padrões em [`.copilot/AGENT.md`](../../.copilot/AGENT.md)
- [ ] Sem magic numbers ou hardcoded values
- [ ] Sem `System.out.println()` ou `e.printStackTrace()`
- [ ] Sem `@Ignore` em testes
- [ ] Sem código comentado
- [ ] Imports organizados e limpos

### ✅ Testes

- [ ] Testes escritos para nova funcionalidade
- [ ] Todos os testes passam: `mvn test`
- [ ] Coverage > 70% (mínimo para novas classes)
- [ ] Nomes descrevem o comportamento testado

### ✅ Commits

- [ ] Seguem Conventional Commits
- [ ] Bem organizados (não mistura múltiplas features)
- [ ] Mensagens descrevem o quê e por quê

### ✅ Documentação

- [ ] README atualizado se necessário
- [ ] Documentação de código (JavaDoc para public APIs)
- [ ] Arquivo de plano criado se mudança complexa
- [ ] CHANGELOG.md atualizado

### ✅ Arquitetura

- [ ] Respeita Arquitetura Hexagonal
- [ ] DDD aplicado corretamente
- [ ] Sem violação de camadas
- [ ] Testes não precisam de Spring Context

## 🏗️ Padrões de Projeto

### Structure Hexagonal

```
src/main/java/com/itau/chargeaccount/
├── domain/
│   ├── entity/
│   ├── valueobject/
│   ├── event/
│   ├── repository/
│   └── service/
├── application/
│   ├── usecase/
│   ├── service/
│   ├── dto/
│   └── port/
├── infrastructure/
│   ├── persistence/
│   ├── event/
│   ├── file/
│   ├── scheduler/
│   └── config/
├── presentation/
│   ├── controller/
│   ├── exception/
│   └── mapper/
└── main/
    └── ChargeAccountApplication.java
```

### Nomeação de Classes

| Tipo | Sufixo | Exemplo |
|------|--------|---------|
| Entity | - | `Lancamento`, `Conta` |
| Value Object | - | `StatusConta`, `TipoLancamento` |
| Aggregate Root | - | `ProcessamentoEncargo` |
| Repository (interface) | `Repository` | `LancamentoRepository` |
| Repository (impl) | `Adapter` | `LancamentoJpaAdapter` |
| Service (domain) | `DomainService` | `ProcessamentoEncargoDomainService` |
| Service (app) | `ApplicationService` | `ProcessamentoApplicationService` |
| DTO | `DTO` | `LancamentoDTO` |
| Controller | `Controller` | `ProcessamentoController` |
| Event | `Event` | `LancamentoProcessadoEvent` |
| Port | `Port` | `PublishEventPort` |
| Adapter | `Adapter` | `KafkaPublisherAdapter` |

### Exemplo de Uma Feature Completa

**Objetivo:** Adicionar validação de valor mínimo para lançamentos

#### Step 1: Defina a Mudança no Domain

```java
// domain/entity/Lancamento.java
@Getter
@RequiredArgsConstructor
public class Lancamento {
    
    private static final BigDecimal VALOR_MINIMO = new BigDecimal("0.01");
    
    private final LancamentoId id;
    private final BigDecimal valor;
    
    public void validar() {
        if (valor.compareTo(VALOR_MINIMO) < 0) {
            throw new LancamentoInvalidoException(
                String.format("Valor mínimo é %.2f", VALOR_MINIMO)
            );
        }
    }
}
```

#### Step 2: Escreva Testes

```java
// src/test/java/com/itau/chargeaccount/domain/entity/LancamentoTest.java
@DisplayName("Lancamento - Validação")
class LancamentoTest {
    
    @DisplayName("Deve aceitar valor >= 0.01")
    @Test
    void deveAceitarValorMinimo() {
        Lancamento lancamento = new Lancamento(
            LancamentoId.gerar(),
            new BigDecimal("0.01")
        );
        
        assertDoesNotThrow(lancamento::validar);
    }
    
    @DisplayName("Deve rejeitar valor < 0.01")
    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-1.00", "0.001"})
    void deveRejeitar(String valor) {
        Lancamento lancamento = new Lancamento(
            LancamentoId.gerar(),
            new BigDecimal(valor)
        );
        
        assertThatThrownBy(lancamento::validar)
            .isInstanceOf(LancamentoInvalidoException.class)
            .hasMessageContaining("Valor mínimo");
    }
}
```

#### Step 3: Integre com Application Service

```java
// application/service/ProcessamentoApplicationService.java
public ResultadoDTO processar(LancamentoDTO dto) {
    Lancamento lancamento = mapper.toDomain(dto);
    lancamento.validar(); // Valida!
    // ... resto do código
}
```

#### Step 4: Commit

```bash
git commit -m "feat(domain): validar valor mínimo de lançamento

Lançamentos devem ter valor mínimo de R\$ 0,01.
Exceção LancamentoInvalidoException é lançada em caso de violação.

Testes adicionados para validação.

Closes #42"
```

## 🧪 Executando Testes

```bash
# Testes unitários
mvn test

# Testes com cobertura
mvn test jacoco:report
# Relatório em: target/site/jacoco/index.html

# Testes específicos
mvn test -Dtest=LancamentoTest

# Testes de integração
mvn verify

# Testes + build completo
mvn clean package
```

## 📊 Verificação de Qualidade

```bash
# Verificar estilo de código
mvn checkstyle:check

# Análise estática (SpotBugs)
mvn spotbugs:check

# Verificar vulnerabilidades
mvn dependency-check:check

# Tudo junto
mvn clean verify
```

## 🐛 Reportando Issues

Se encontrou um bug ou tem sugestão:

1. Verifique se já existe issue aberta
2. Crie uma issue com:
   - Título descritivo
   - Descrição do problema
   - Steps para reproduzir
   - Comportamento esperado vs atual
   - Logs (se aplicável)

## 📚 Documentação

Ao adicionar nova feature:

1. **Atualize README.md** se muda comportamento geral
2. **Crie JavaDoc** para public APIs
3. **Documente ADRs** (Architecture Decision Records) para decisões importantes
4. **Atualize CHANGELOG.md**

## 🤝 Código de Conduta

- Seja respeitoso
- Assuma boa intenção dos outros
- Critique o código, não as pessoas
- Aprenda com feedback

## ❓ Dúvidas?

Consulte:
- [`.copilot/AGENT.md`](../../.copilot/AGENT.md) - Diretrizes detalhadas
- [`doc/architecture/HEXAGONAL.md`](../architecture/HEXAGONAL.md) - Arquitetura
- [`doc/architecture/DDD.md`](../architecture/DDD.md) - Domain-Driven Design
- Issues abertas - Contexto do projeto

---

**Muito obrigado por contribuir! 🙏**

