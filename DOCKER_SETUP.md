# 🐳 DOCKER SETUP - Charge Account Worker

**Data:** 18/04/2026  
**Objetivo:** Guia completo para usar o Docker Compose

---

## 📋 O Que Está Incluído

```
✅ PostgreSQL 15       (Banco de dados)
✅ PgAdmin            (GUI para PostgreSQL)
✅ Kafka              (Message broker)
✅ Zookeeper          (Kafka coordinator)
✅ Kafka UI           (GUI para Kafka)
✅ Kafka Init         (Cria topics automaticamente)
```

---

## 🚀 Quick Start

### 1️⃣ Iniciar Ambiente

```bash
# Opção A: Usando make (recomendado)
make docker-up

# Opção B: Usando docker-compose direto
docker-compose up -d
```

**Aguarde 10-15 segundos para tudo inicializar.**

### 2️⃣ Verificar Status

```bash
make docker-status

# Ou
docker-compose ps
```

### 3️⃣ Acessar Interfaces

```
📊 Kafka UI:  http://localhost:8080
🗄️  PgAdmin:   http://localhost:5050
📡 PostgreSQL: localhost:5432
🎯 Kafka:      localhost:9092
```

### 4️⃣ Parar Ambiente

```bash
make docker-down
```

---

## 📚 Comandos Disponíveis

### Gerenciamento

```bash
# Iniciar
make docker-up

# Parar
make docker-down

# Reiniciar
make docker-restart

# Limpar containers e volumes
make docker-clean

# Listar todos os comandos
make help
```

### Logs

```bash
# Todos os logs
make docker-logs

# Apenas Kafka
make docker-logs-kafka

# Apenas PostgreSQL
make docker-logs-postgres

# Apenas aplicação
make docker-logs-app
```

### PostgreSQL

```bash
# Conectar direto ao psql
make docker-postgres

# Exemplo: listar databases
\l

# Conectar à database
\c chargedb

# Listar tabelas
\dt

# Sair
\q
```

### Kafka

```bash
# Listar topics
make docker-kafka-list

# Descrever um topic
make docker-kafka-describe

# Consumer (ler mensagens)
make docker-kafka-consume

# Producer (enviar mensagens)
make docker-kafka-produce
```

### Testes

```bash
# Executar testes com Docker up
make docker-test

# Apenas testes de integração
make docker-test-integration

# Todos os testes com cobertura
make docker-test-all
```

### Health Check

```bash
# Verificar saúde dos serviços
make docker-health
```

---

## 🔐 Credenciais Padrão

### PostgreSQL
```
Host:     localhost:5432
Database: chargedb
User:     chargeuser
Password: chargepass123
```

### PgAdmin
```
Email:    admin@example.com
Password: admin123
```

---

## 📝 Configuração Personalizada

### Mudar Credenciais

Edite `.env.docker`:

```bash
# PostgreSQL
POSTGRES_DB=meu_banco
POSTGRES_USER=meu_usuario
POSTGRES_PASSWORD=minha_senha

# PgAdmin
PGADMIN_EMAIL=meu_email@example.com
PGADMIN_PASSWORD=minha_senha
```

Depois reinicie:

```bash
make docker-down
make docker-up
```

---

## 🗄️ PostgreSQL via PgAdmin

### 1️⃣ Acessar PgAdmin

```
http://localhost:5050
Email: admin@example.com
Password: admin123
```

### 2️⃣ Conectar ao Servidor

1. Right-click "Servers"
2. Create → Server
3. Name: `charge-account-local`
4. Connection tab:
   - Host: `postgres`
   - Port: `5432`
   - Maintenance database: `chargedb`
   - Username: `chargeuser`
   - Password: `chargepass123`
5. Save

### 3️⃣ Executar Queries

1. Expandir servidor
2. Expandir database `chargedb`
3. Clique em "Query Tool"
4. Escreva sua query

---

## 📊 Kafka UI

### 1️⃣ Acessar

```
http://localhost:8080
```

### 2️⃣ Ver Topics

```
Topics → Selecionar topic
```

### 3️⃣ Ver Mensagens

```
Topics → charge.events → Messages
```

### 4️⃣ Produzir Mensagem

```
Topics → charge.events → Produce Message
```

---

## 🧪 Executar Testes

### Com Ambiente Docker

```bash
# Inicia Docker, executa testes, gera relatório
make docker-test
```

### Apenas Testes de Integração

```bash
make docker-test-integration
```

### Todos os Testes com Cobertura

```bash
make docker-test-all
```

---

## 📋 Kafka Topics

Criados automaticamente:

| Topic | Partições | Retenção | Compressão |
|-------|-----------|----------|-----------|
| `charge.events` | 10 | 24h | Snappy |
| `account.status.response` | 10 | 24h | Snappy |
| `accounting.events` | 10 | 24h | Snappy |
| `charge.events.dlq` | 3 | 7d | Nenhum |

---

## 🐛 Troubleshooting

### Portas em Uso

**Problema:** Port 5432 já está em uso

**Solução:** 
```bash
# Mude a porta em docker-compose.yml
# De: "5432:5432"
# Para: "5433:5432"

# Ou parar container conflitante:
docker ps
docker stop <container-id>
```

### Kafka Não Inicia

**Problema:** Kafka container falha

**Solução:**
```bash
# Verificar logs
make docker-logs-kafka

# Limpar volumes
make docker-clean

# Reiniciar
make docker-up
```

### Testes Falham com "Connection Refused"

**Problema:** Aplicação não consegue conectar ao banco/Kafka

**Solução:**
```bash
# Aguardar mais tempo
sleep 20

# Verificar status
make docker-status

# Se ainda falhar, verificar logs
make docker-logs
```

### PgAdmin Não Conecta

**Problema:** Connection refused ao servidor PostgreSQL

**Solução:**
```bash
# Verificar se postgres está rodando
docker ps | grep postgres

# Se não, reiniciar
make docker-restart

# Reconectar no PgAdmin
```

---

## 🗑️ Limpeza

### Parar Tudo

```bash
make docker-down
```

### Remover Tudo (Dados Perdidos)

```bash
make docker-clean
```

### Remover Tudo Incluindo Imagens

```bash
make docker-destroy
```

---

## 📊 Arquitetura

```
┌─────────────────────────────────────┐
│      Docker Network: bridge         │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────┐                   │
│  │ PostgreSQL  │                   │
│  │  :5432      │                   │
│  └────────┬────┘                   │
│           │                        │
│  ┌────────▼─────────┐             │
│  │   PgAdmin        │             │
│  │   :5050          │             │
│  └──────────────────┘             │
│                                     │
│  ┌──────────────┐   ┌──────────┐  │
│  │  Zookeeper   │   │  Kafka   │  │
│  │  :2181       │───│  :9092   │  │
│  └──────────────┘   └────┬─────┘  │
│                           │        │
│  ┌─────────────────────────▼──┐  │
│  │    Kafka UI                 │  │
│  │    :8080                    │  │
│  └─────────────────────────────┘  │
│                                     │
└─────────────────────────────────────┘
```

---

## 🚀 Próximas Etapas

### 1️⃣ Iniciar Ambiente
```bash
make docker-up
```

### 2️⃣ Verificar Status
```bash
make docker-status
```

### 3️⃣ Rodando Testes
```bash
make docker-test
```

### 4️⃣ Ver Kafka Topics
```bash
make docker-kafka-list
```

### 5️⃣ Conectar ao Banco
```bash
make docker-postgres
```

---

## 📞 Referência Rápida

```
┌─────────────────────────────────────┐
│     QUICK REFERENCE                 │
├─────────────────────────────────────┤
│ Iniciar:     make docker-up         │
│ Parar:       make docker-down       │
│ Logs:        make docker-logs       │
│ Status:      make docker-status     │
│ Testes:      make docker-test       │
│ Saúde:       make docker-health     │
│ Banco:       make docker-postgres   │
│ Kafka:       make docker-kafka-list │
│ Help:        make help              │
│ Limpar:      make docker-clean      │
└─────────────────────────────────────┘

URLS:
┌──────────────────────────────────────┐
│ Kafka UI:   http://localhost:8080    │
│ PgAdmin:    http://localhost:5050    │
│ PostgreSQL: localhost:5432           │
│ Kafka:      localhost:9092           │
└──────────────────────────────────────┘
```

---

**Data:** 18/04/2026  
**Status:** ✅ Pronto para Uso  
**Próximo:** `make docker-up`

