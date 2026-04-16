# 📚 README - Instruções para GitHub Copilot

Bem-vindo à pasta `.copilot`! Este diretório contém instruções especializadas para o GitHub Copilot trabalhar efetivamente neste projeto.

## 📂 Conteúdo da Pasta

### 1. 🤖 **AGENT.md** (Este é o arquivo principal)

Instruções detalhadas sobre:
- ✅ Padrão de Commits (Conventional Commits)
- ✅ Padrão de Code Review (checklist completo)
- ✅ Padrão de Documentação e Plans
- ✅ Guia de Estilo de Código (Java/Spring)
- ✅ Checklist de Qualidade

**Quando usar:** Sempre! Antes de fazer qualquer commit ou code review.

### 2. 📋 **PLANS.md**

Guia completo sobre Plans (blueprints para features):
- Como criar plans
- Nomenclatura padrão: `plan-${camelCaseName}.prompt.md`
- Estrutura padrão de plans
- Onde armazenar plans
- Exemplos práticos

**Quando usar:** Ao criar ou refinar plans para tarefas complexas.

## 🚀 Quick Start

### Você precisa...

| Tarefa | Leia | Arquivo |
|--------|------|---------|
| Fazer um commit | AGENT.md → Padrão de Commits | `/.copilot/AGENT.md` |
| Revisar código | AGENT.md → Padrão de Code Review | `/.copilot/AGENT.md` |
| Implementar uma feature | PLANS.md → Estrutura de Plans | `/.copilot/PLANS.md` |
| Entender a arquitetura | HEXAGONAL.md | `/doc/architecture/HEXAGONAL.md` |
| Aprender sobre DDD | DDD.md | `/doc/architecture/DDD.md` |
| Escrever testes | TESTING.md | `/doc/guides/TESTING.md` |
| Contribuir ao projeto | CONTRIBUTING.md | `/CONTRIBUTING.md` |

## 🎯 Fluxo Típico de Trabalho

### 1. **Receber Tarefa**

```
Usuário: "Implemente a autenticação JWT"
```

### 2. **Consultar PLANS.md**

Entender como plans funcionam:
```
- Nome: plan-jwtAuthentication.prompt.md
- Localização: .copilot/plans/
- Estrutura: Steps, Estrutura de Diretórios, Dependências, Considerações
```

### 3. **Criar um Plan** (se não existir)

```markdown
# Plan: Implementar Autenticação JWT

Desenvolvimento de autenticação JWT para secured endpoints.

## Steps

1. **Adicionar dependências** ...
2. **Criar token provider** ...
3. **Criar filtro de validação** ...
...
```

### 4. **Implementar conforme Plan**

Para cada step, criar código seguindo:
- `.copilot/AGENT.md` → Guia de Estilo
- `/doc/architecture/HEXAGONAL.md` → Arquitetura
- `/doc/architecture/DDD.md` → Domain-Driven Design

### 5. **Testes**

Seguir `/doc/guides/TESTING.md`:
- Testes unitários para domain
- Testes de aplicação para use cases
- Testes de integração para adapters

### 6. **Commit com Padrão**

```bash
git commit -m "feat(security): implementar autenticação JWT

Adiciona suporte a JWT para autenticação de usuários.

Closes #42"
```

Seguir `.copilot/AGENT.md` → Padrão de Commits

### 7. **Code Review**

Verificar checklist em `.copilot/AGENT.md` → Padrão de Code Review

## 📖 Documentação Relacionada

Além da pasta `.copilot`, consulte:

```
doc/
├── architecture/
│   ├── HEXAGONAL.md          → Arquitetura Hexagonal
│   └── DDD.md                → Domain-Driven Design
├── guides/
│   └── TESTING.md            → Estratégia de Testes
└── plans/
    └── (plans salvos aqui)

README.md                      → Visão geral do projeto
CONTRIBUTING.md               → Guia de contribuição
```

## 🔄 Workflow de Submissão (PR)

```
┌─────────────────────────────────────────┐
│ 1. Crie branch (ex: feature/xxx)        │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 2. Implemente seguindo:                 │
│    - AGENT.md (estilo + arquitetura)    │
│    - TESTING.md (cobertura de testes)   │
│    - DDD.md + HEXAGONAL.md (design)     │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 3. Commit com Conventional Commits      │
│    - Consulte AGENT.md → Padrão         │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 4. Push para remote                     │
│    git push origin feature/xxx          │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 5. Abra PR com descrição clara          │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 6. Code Review                          │
│    - Verificar AGENT.md checklist       │
│    - Aprovar ou solicitar mudanças      │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 7. Merge para main                      │
└─────────────────────────────────────────┘
```

## 💡 Dicas Importantes

### ✨ Ao Começar uma Feature

1. Leia `HEXAGONAL.md` para entender onde colocar código
2. Leia `DDD.md` para modelar entidades e value objects
3. Crie um plan em `.copilot/plans/` se for complexo
4. Implemente testes primeiro (TDD) - veja `TESTING.md`

### 🔍 Ao Revisar Código

1. Use checklist em `AGENT.md` → Padrão de Code Review
2. Verifique arquitetura hexagonal em `HEXAGONAL.md`
3. Verifique DDD em `DDD.md`
4. Verifique testes em `TESTING.md`

### 📝 Ao Fazer Commits

1. Seguir `AGENT.md` → Padrão de Commits
2. Use verbos no infinitivo: "adicionar", "corrigir", "refatorar"
3. Primeira linha < 50 caracteres
4. Tipo: feat, fix, refactor, test, docs, chore

### 🧪 Ao Escrever Testes

1. Seguir `TESTING.md` → Estratégia de Testes
2. Usar padrão AAA (Arrange-Act-Assert)
3. `@DisplayName` para descrever comportamento
4. Testar casos feliz, infeliz e edge cases

## ❓ Precisa de Ajuda?

| Dúvida | Consulte |
|--------|----------|
| "Como commitar?" | `AGENT.md` → Padrão de Commits |
| "Qual padrão de nomenclatura?" | `AGENT.md` → Guia de Estilo |
| "Como estruturar código?" | `HEXAGONAL.md` |
| "Como modelar domínio?" | `DDD.md` |
| "Como testar?" | `TESTING.md` |
| "Como criar plans?" | `PLANS.md` |
| "Como contribuir?" | `/CONTRIBUTING.md` |

## 🔗 Estrutura Completa de Documentação

```
ms-charge-account-worker/
│
├── .copilot/                          # ← Você está aqui
│   ├── AGENT.md                       # Diretrizes principais ⭐
│   ├── PLANS.md                       # Guia de plans
│   ├── README.md                      # Este arquivo
│   └── plans/                         # Plans salvos aqui
│
├── doc/
│   ├── architecture/
│   │   ├── HEXAGONAL.md              # Arquitetura Hexagonal
│   │   └── DDD.md                    # Domain-Driven Design
│   ├── guides/
│   │   ├── TESTING.md                # Estratégia de Testes
│   │   └── SETUP.md                  # Setup de Ambiente (a criar)
│   ├── decisions/
│   │   └── ADR_001_*.md             # Architecture Decision Records
│   └── img.png
│
├── src/main/java/...                 # Código fonte
├── src/test/java/...                 # Testes
│
├── README.md                          # Visão geral do projeto
├── CONTRIBUTING.md                    # Guia de contribuição
├── CHANGELOG.md                       # Histórico de versões
├── pom.xml                           # Dependências Maven
└── .gitignore
```

## 📅 Última Atualização

- **Data:** 2026-04-16
- **Versão:** 1.0
- **Responsável:** GitHub Copilot + Rafael (Desenvolvedor)

---

**Próximo passo:** Leia `AGENT.md` para detalhes sobre padrões de commits, code review e estilo de código!


