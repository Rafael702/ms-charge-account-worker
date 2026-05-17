#!/bin/bash

# ============================================
# Container Validation Script (Agnóstico)
# ============================================
# Detecta e valida Docker, Podman ou Colima
# Funciona com: docker-compose, podman-compose, colima

set -e

echo "╔══════════════════════════════════════════════╗"
echo "║  🐳 Container Platform Validation Script     ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# ============================================
# Detectar plataforma de container disponível
# ============================================
detect_container_platform() {
    # Preferência: Docker > Podman > Colima

    if command -v docker &> /dev/null; then
        CONTAINER_CLI="docker"
        CONTAINER_TYPE="Docker"
        return 0
    fi

    if command -v podman &> /dev/null; then
        CONTAINER_CLI="podman"
        CONTAINER_TYPE="Podman"
        return 0
    fi

    if command -v colima &> /dev/null; then
        CONTAINER_CLI="docker"  # Colima usa CLI compatível com Docker
        CONTAINER_TYPE="Colima (Docker-compatible)"
        return 0
    fi

    echo "❌ Nenhuma plataforma de container encontrada"
    echo "   Instale um de: Docker, Podman ou Colima"
    exit 1
}

detect_compose_tool() {
    # Detectar ferramenta de compose
    if command -v docker-compose &> /dev/null; then
        COMPOSE_CLI="docker-compose"
        return 0
    fi

    if command -v podman-compose &> /dev/null; then
        COMPOSE_CLI="podman-compose"
        return 0
    fi

    # Fallback: usar docker compose (nova sintaxe)
    if $CONTAINER_CLI compose version &> /dev/null 2>&1; then
        COMPOSE_CLI="$CONTAINER_CLI compose"
        return 0
    fi

    echo "❌ Nenhuma ferramenta de compose encontrada"
    exit 1
}

# Detectar plataformas
echo "1️⃣  Detectando plataforma de container..."
detect_container_platform
echo "✅ Plataforma detectada: $CONTAINER_TYPE"
echo "   Usando: $CONTAINER_CLI"
echo ""

# Check Container CLI
echo "2️⃣  Verificando $CONTAINER_CLI..."
if ! command -v $CONTAINER_CLI &> /dev/null; then
    echo "❌ $CONTAINER_CLI não encontrado"
    exit 1
fi
echo "✅ $CONTAINER_CLI OK: $($CONTAINER_CLI --version)"
echo ""

# Detect Compose Tool
echo "3️⃣  Detectando ferramenta de compose..."
detect_compose_tool
echo "✅ Compose detectado: $COMPOSE_CLI"
echo ""

# Check Container Daemon
echo "4️⃣  Verificando daemon do container..."

# Para Colima, precisamos verificar se está rodando
if [ "$CONTAINER_TYPE" = "Colima (Docker-compatible)" ]; then
    if ! colima status &> /dev/null; then
        echo "❌ Colima não está rodando"
        echo "   Execute: colima start"
        exit 1
    fi
    echo "✅ Colima está rodando"
elif ! $CONTAINER_CLI ps &> /dev/null; then
    echo "❌ $CONTAINER_TYPE daemon não está rodando"
    echo "   Inicie o daemon e tente novamente"
    exit 1
else
    echo "✅ $CONTAINER_TYPE daemon OK"
fi
echo ""

# Validar arquivo docker-compose.yml
echo "5️⃣  Validando docker-compose.yml..."
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ docker-compose.yml não encontrado"
    exit 1
fi
$COMPOSE_CLI config > /dev/null 2>&1 || {
    echo "❌ docker-compose.yml inválido"
    exit 1
}
echo "✅ docker-compose.yml OK"
echo ""

# Parar containers antigos
echo "6️⃣  Verificando containers antigos..."
if $COMPOSE_CLI ps -q 2>/dev/null | grep -q .; then
    echo "⚠️  Containers antigos encontrados"
    read -p "Remover? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        $COMPOSE_CLI down -v
        echo "✅ Containers removidos"
    fi
fi
echo ""

# Iniciar containers
echo "7️⃣  Iniciando containers..."
$COMPOSE_CLI up --build
echo "✅ Containers iniciados"
echo ""

# Aguardar inicialização
echo "8️⃣  Aguardando inicialização (15 segundos)..."
for i in {15..1}; do
    printf "\r   ⏳ Aguardando... %2d segundos" "$i"
    sleep 1
done
echo ""
echo "✅ Aguardo concluído"
echo ""

# Verificar PostgreSQL
echo "9️⃣  Verificando PostgreSQL..."
if $CONTAINER_CLI exec charge-account-postgres pg_isready -U chargeuser 2>/dev/null; then
    echo "✅ PostgreSQL OK"
else
    echo "❌ PostgreSQL não está pronto"
    $COMPOSE_CLI logs postgres | tail -20
    exit 1
fi
echo ""

# Verificar Kafka
echo "🔟 Verificando Kafka..."
if $CONTAINER_CLI exec charge-account-kafka kafka-broker-api-versions.sh --bootstrap-server=localhost:9092 > /dev/null 2>&1; then
    echo "✅ Kafka OK"
else
    echo "❌ Kafka não está pronto"
    $COMPOSE_CLI logs kafka | tail -20
    exit 1
fi
echo ""

# Listar topics
echo "1️⃣1️⃣  Listando Kafka Topics..."
$CONTAINER_CLI exec charge-account-kafka kafka-topics.sh --list --bootstrap-server=localhost:9092 | while read topic; do
    echo "   ✅ $topic"
done
echo ""

# Exibir informações de acesso
echo "╔══════════════════════════════════════════════╗"
echo "║         ✅ TUDO FUNCIONANDO! ✅              ║"
echo "╚══════════════════════════════════════════════╝"
echo ""
echo "🔧 Plataforma: $CONTAINER_TYPE"
echo "📦 Compose: $COMPOSE_CLI"
echo ""
echo "📊 Kafka UI:  http://localhost:8080"
echo "🗄️  PgAdmin:   http://localhost:5050"
echo ""
echo "Credenciais PostgreSQL:"
echo "  Host:     localhost:5432"
echo "  Database: chargedb"
echo "  User:     chargeuser"
echo "  Password: chargepass123"
echo ""
echo "Credenciais PgAdmin:"
echo "  Email:    admin@example.com"
echo "  Password: admin123"
echo ""
echo "Próximos passos:"
echo "  1. Acessar Kafka UI: http://localhost:8080"
echo "  2. Acessar PgAdmin: http://localhost:5050"
echo "  3. Rodar testes: make docker-test"
echo "  4. Ver logs: make docker-logs"
echo ""

