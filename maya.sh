#!/bin/bash

# Script para build e execução do Sistema MAYA

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funções auxiliares
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verificar se Docker está instalado
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker não está instalado!"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose não está instalado!"
        exit 1
    fi
    
    print_success "Docker e Docker Compose estão disponíveis"
}

# Build da aplicação
build_app() {
    print_info "Iniciando build da aplicação MAYA..."
    
    cd backend
    
    print_info "Compilando backend com Maven..."
    ./mvnw clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_success "Build do backend concluído com sucesso!"
    else
        print_error "Falha no build do backend"
        exit 1
    fi
    
    cd ..
}

# Executar testes
run_tests() {
    print_info "Executando testes..."
    
    cd backend
    ./mvnw test
    
    if [ $? -eq 0 ]; then
        print_success "Todos os testes passaram!"
    else
        print_warning "Alguns testes falharam"
    fi
    
    cd ..
}

# Iniciar serviços com Docker Compose
start_services() {
    print_info "Iniciando serviços com Docker Compose..."
    
    # Criar diretórios necessários
    mkdir -p logs
    
    docker-compose up -d sqlserver
    
    print_info "Aguardando SQL Server inicializar..."
    sleep 30
    
    docker-compose up -d maya-backend
    
    print_success "Serviços iniciados!"
    print_info "Backend disponível em: http://localhost:8080"
    print_info "Health check: http://localhost:8080/actuator/health"
    print_info "Swagger UI: http://localhost:8080/swagger-ui.html"
}

# Parar serviços
stop_services() {
    print_info "Parando serviços..."
    docker-compose down
    print_success "Serviços parados!"
}

# Limpar volumes e imagens
clean_all() {
    print_warning "Removendo todos os containers, volumes e imagens do MAYA..."
    docker-compose down -v --rmi all
    docker system prune -f
    print_success "Limpeza concluída!"
}

# Ver logs
show_logs() {
    if [ "$1" == "backend" ]; then
        docker-compose logs -f maya-backend
    elif [ "$1" == "db" ]; then
        docker-compose logs -f sqlserver
    else
        docker-compose logs -f
    fi
}

# Status dos serviços
show_status() {
    print_info "Status dos serviços:"
    docker-compose ps
    
    print_info "Testando conectividade..."
    
    # Testar backend
    if curl -s http://localhost:8080/actuator/health > /dev/null; then
        print_success "Backend está respondendo"
    else
        print_error "Backend não está respondendo"
    fi
}

# Mostrar ajuda
show_help() {
    echo "Script de gerenciamento do Sistema MAYA"
    echo ""
    echo "Uso: $0 [COMANDO]"
    echo ""
    echo "Comandos disponíveis:"
    echo "  build         - Compilar a aplicação"
    echo "  test          - Executar testes"
    echo "  start         - Iniciar todos os serviços"
    echo "  stop          - Parar todos os serviços"
    echo "  restart       - Reiniciar todos os serviços"
    echo "  logs          - Ver logs de todos os serviços"
    echo "  logs backend  - Ver logs apenas do backend"
    echo "  logs db       - Ver logs apenas do banco"
    echo "  status        - Ver status dos serviços"
    echo "  clean         - Limpar containers e volumes"
    echo "  help          - Mostrar esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0 build && $0 start    # Build e start"
    echo "  $0 logs backend         # Ver logs do backend"
    echo "  $0 clean               # Limpeza completa"
}

# Main script
case "$1" in
    "build")
        check_docker
        build_app
        ;;
    "test")
        run_tests
        ;;
    "start")
        check_docker
        start_services
        ;;
    "stop")
        stop_services
        ;;
    "restart")
        stop_services
        sleep 5
        start_services
        ;;
    "logs")
        show_logs $2
        ;;
    "status")
        show_status
        ;;
    "clean")
        clean_all
        ;;
    "help"|"")
        show_help
        ;;
    *)
        print_error "Comando '$1' não reconhecido"
        show_help
        exit 1
        ;;
esac
