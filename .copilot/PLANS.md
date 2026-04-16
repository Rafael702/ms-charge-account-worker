# 📋 Plans - Guia de Uso

## O que são Plans?

**Plans** são documentos estruturados em Markdown que descrevem como implementar features, correções ou refatorações complexas no projeto. Eles servem como **blueprints** para o GitHub Copilot executar tarefas de forma organizada e profissional.

## 📁 Localização

Plans devem estar localizados em:

```
projeto-root/
├── .copilot/
│   └── plans/
│       ├── plan-hexagonalArchitecture.prompt.md
│       ├── plan-kafkaIntegration.prompt.md
│       └── plan-testStrategy.prompt.md
└── doc/
    └── plans/ (alternativa)
        └── ...
```

## 🎯 Nomenclatura

**Padrão:** `plan-${camelCaseName}.prompt.md`

**Exemplos:**
- `plan-msChargeAccountWorker.prompt.md` - Plan principal
- `plan-kafkaIntegration.prompt.md` - Integração com Kafka
- `plan-persistenceLayer.prompt.md` - Camada de persistência
- `plan-apiEndpoints.prompt.md` - Endpoints REST
- `plan-testStrategy.prompt.md` - Estratégia de testes
- `plan-databaseMigration.prompt.md` - Migração de dados
- `plan-performanceTuning.prompt.md` - Otimização de performance
- `plan-securityHardening.prompt.md` - Endurecimento de segurança

## 📝 Estrutura Padrão de um Plan

```markdown
# Plan: [Nome Descritivo]

[1-2 linhas descrevendo o plano em alto nível]

## Steps

1. **Passo Um**
   - Descrição detalhada
   - Sub-passos se necessário
   - Referências a arquivos/classes

2. **Passo Dois**
   - ...

3. **Passo Três**
   - ...

## Estrutura de Diretórios (se aplicável)

Mostre a estrutura de arquivos que será criada/modificada.

```
src/main/java/com/itau/chargeaccount/
├── domain/
│   └── ...
├── application/
│   └── ...
└── ...
```

## Dependências

```xml
<!-- Adicione dependências necessárias aqui -->
```

## Further Considerations

1. **Consideração Um**: Explicação e recomendação
2. **Consideração Dois**: Alternativas e tradeoffs
3. **Consideração Três**: Riscos e mitigações

## Checklist de Conclusão

- [ ] Item 1
- [ ] Item 2
- [ ] Item 3
```

## 📖 Elementos Essenciais

### ✅ Steps

- Numerados e claros
- Descrição detalhada do que fazer
- Referências a arquivos e classes específicas
- Sub-passos se necessário

**Exemplo bom:**
```markdown
1. **Criar Value Objects**
   - Criar `src/main/java/com/itau/chargeaccount/domain/valueobject/StatusConta.java`
   - Implementar enumeração: ATIVA, CANCELADA, BLOQUEIO_JUDICIAL
   - Adicionar método `podeProcessar()` que retorna true apenas para ATIVA
   - Adicionar método `ehBloqueada()` que retorna true para CANCELADA ou BLOQUEIO_JUDICIAL
```

### ✅ Estrutura de Diretórios

Mostre a árvore de arquivos que será criada/modificada:

```
src/main/java/com/itau/chargeaccount/
├── domain/
│   ├── entity/
│   │   └── Lancamento.java (criar)
│   ├── valueobject/
│   │   └── StatusConta.java (criar)
│   ├── event/
│   │   └── LancamentoProcessadoEvent.java (criar)
│   └── service/
│       └── ProcessamentoEncargoDomainService.java (criar)
├── application/
│   ├── service/
│   │   └── ProcessamentoApplicationService.java (criar)
│   └── dto/
│       └── LancamentoDTO.java (criar)
└── infrastructure/
    ├── persistence/
    │   └── jpa/
    │       └── LancamentoJpaAdapter.java (criar)
    └── event/
        └── KafkaPublisherAdapter.java (criar)
```

### ✅ Dependências

Liste dependências Maven necessárias:

```xml
<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### ✅ Further Considerations

Mostre alternativas e tradeoffs:

```markdown
## Further Considerations

1. **Escolha de Message Broker**:
   - Kafka: Escalável, persistência, replay. Mais complexo.
   - RabbitMQ: Simples, confiável. Menos escalável.
   - Recomendação: Kafka para 20M registros/dia

2. **Persistência**:
   - H2: Para testes (em memória)
   - PostgreSQL: Para produção
   - Estratégia: Usar Spring Profiles para trocar

3. **Processamento em Batch**:
   - Spring Batch: Bom para arquivos
   - Kafka Streams: Bom para streaming
   - Recomendação: Kafka Streams para tempo real
```

### ✅ Checklist de Conclusão

Liste verificações finais:

```markdown
## Checklist de Conclusão

- [ ] Todas as classes criadas
- [ ] Testes unitários implementados
- [ ] Testes cobrem >70% das linhas
- [ ] Sem warnings de build
- [ ] Documentação atualizada
- [ ] Commits seguem Conventional Commits
- [ ] Code review aprovado
- [ ] Feature mergeada para main
```

## 🚀 Como Usar Plans com Copilot

### Scenario 1: Usar um Plan Existente

```
Usuário: "Implemente o plan-kafkaIntegration.prompt.md"
Copilot: Lê o arquivo, entende os steps e executa cada um
```

### Scenario 2: Refinamento de Plan

```
Usuário: "Refine o plan-msChargeAccountWorker.prompt.md com mais detalhes"
Copilot: Analisa plan existente e adiciona mais contexto/steps
```

### Scenario 3: Criar Novo Plan

```
Usuário: "Crie um plan para implementar autenticação JWT"
Copilot: Gera plan-jwtAuthentication.prompt.md com steps, dependências, etc
```

## 📚 Plans Já Criados

- ✅ `plan-msChargeAccountWorker.prompt.md` - Plan principal do projeto
- 📝 `plan-kafkaIntegration.prompt.md` - A criar
- 📝 `plan-persistenceLayer.prompt.md` - A criar
- 📝 `plan-apiEndpoints.prompt.md` - A criar
- 📝 `plan-testStrategy.prompt.md` - A criar

## 💡 Dicas

1. **Seja específico nos Steps**: Quanto mais detalhado, melhor o Copilot pode executar
2. **Referencie arquivos**: Sempre diga o caminho completo do arquivo
3. **Mostre exemplos**: Se possível, mostre trechos de código esperados
4. **Considere dependências**: Mencione se um step depende de outro
5. **Atualize plans**: Conforme aprende sobre o projeto, refine os plans

## 📖 Exemplo Real: Plan Completo

Veja `plan-msChargeAccountWorker.prompt.md` como exemplo de plan bem estruturado para este projeto.

---

**Última atualização:** 2026-04-16

