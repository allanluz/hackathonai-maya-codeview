# Script PowerShell para build e execução do Sistema MAYA

param(
    [Parameter(Position=0)]
    [string]$Command = "help",
    
    [Parameter(Position=1)]
    [string]$Service = ""
)

# Funções auxiliares para output colorido
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Verificar se Docker está instalado
function Test-Docker {
    try {
        $dockerVersion = docker --version 2>$null
        $composeVersion = docker-compose --version 2>$null
        
        if (-not $dockerVersion -or -not $composeVersion) {
            Write-Error "Docker ou Docker Compose não estão instalados!"
            exit 1
        }
        
        Write-Success "Docker e Docker Compose estão disponíveis"
        return $true
    }
    catch {
        Write-Error "Erro ao verificar Docker: $($_.Exception.Message)"
        return $false
    }
}

# Build da aplicação
function Build-App {
    Write-Info "Iniciando build da aplicação MAYA..."
    
    Push-Location backend
    
    try {
        Write-Info "Compilando backend com Maven..."
        
        if ($IsWindows -or $env:OS -eq "Windows_NT") {
            .\mvnw.cmd clean package -DskipTests
        } else {
            ./mvnw clean package -DskipTests
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Build do backend concluído com sucesso!"
        } else {
            Write-Error "Falha no build do backend"
            Pop-Location
            exit 1
        }
    }
    catch {
        Write-Error "Erro durante build: $($_.Exception.Message)"
        Pop-Location
        exit 1
    }
    
    Pop-Location
}

# Executar testes
function Invoke-Tests {
    Write-Info "Executando testes..."
    
    Push-Location backend
    
    try {
        if ($IsWindows -or $env:OS -eq "Windows_NT") {
            .\mvnw.cmd test
        } else {
            ./mvnw test
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Todos os testes passaram!"
        } else {
            Write-Warning "Alguns testes falharam"
        }
    }
    catch {
        Write-Error "Erro durante execução dos testes: $($_.Exception.Message)"
    }
    
    Pop-Location
}

# Iniciar serviços com Docker Compose
function Start-Services {
    Write-Info "Iniciando serviços com Docker Compose..."
    
    try {
        # Criar diretórios necessários
        if (-not (Test-Path "logs")) {
            New-Item -ItemType Directory -Path "logs" -Force | Out-Null
        }
        
        # Iniciar SQL Server primeiro
        docker-compose up -d sqlserver
        
        Write-Info "Aguardando SQL Server inicializar..."
        Start-Sleep -Seconds 30
        
        # Iniciar backend
        docker-compose up -d maya-backend
        
        Write-Success "Serviços iniciados!"
        Write-Info "Backend disponível em: http://localhost:8080"
        Write-Info "Health check: http://localhost:8080/actuator/health"
        Write-Info "Swagger UI: http://localhost:8080/swagger-ui.html"
    }
    catch {
        Write-Error "Erro ao iniciar serviços: $($_.Exception.Message)"
    }
}

# Parar serviços
function Stop-Services {
    Write-Info "Parando serviços..."
    
    try {
        docker-compose down
        Write-Success "Serviços parados!"
    }
    catch {
        Write-Error "Erro ao parar serviços: $($_.Exception.Message)"
    }
}

# Limpar volumes e imagens
function Clear-All {
    Write-Warning "Removendo todos os containers, volumes e imagens do MAYA..."
    
    try {
        docker-compose down -v --rmi all
        docker system prune -f
        Write-Success "Limpeza concluída!"
    }
    catch {
        Write-Error "Erro durante limpeza: $($_.Exception.Message)"
    }
}

# Ver logs
function Show-Logs {
    param([string]$ServiceName)
    
    try {
        if ($ServiceName -eq "backend") {
            docker-compose logs -f maya-backend
        }
        elseif ($ServiceName -eq "db") {
            docker-compose logs -f sqlserver
        }
        else {
            docker-compose logs -f
        }
    }
    catch {
        Write-Error "Erro ao mostrar logs: $($_.Exception.Message)"
    }
}

# Status dos serviços
function Show-Status {
    Write-Info "Status dos serviços:"
    
    try {
        docker-compose ps
        
        Write-Info "Testando conectividade..."
        
        # Testar backend
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                Write-Success "Backend está respondendo"
            }
        }
        catch {
            Write-Error "Backend não está respondendo"
        }
    }
    catch {
        Write-Error "Erro ao verificar status: $($_.Exception.Message)"
    }
}

# Mostrar ajuda
function Show-Help {
    Write-Host @"
Script de gerenciamento do Sistema MAYA

Uso: .\maya.ps1 [COMANDO] [SERVIÇO]

Comandos disponíveis:
  build         - Compilar a aplicação
  test          - Executar testes
  start         - Iniciar todos os serviços
  stop          - Parar todos os serviços
  restart       - Reiniciar todos os serviços
  logs          - Ver logs de todos os serviços
  logs backend  - Ver logs apenas do backend
  logs db       - Ver logs apenas do banco
  status        - Ver status dos serviços
  clean         - Limpar containers e volumes
  help          - Mostrar esta ajuda

Exemplos:
  .\maya.ps1 build         # Compilar aplicação
  .\maya.ps1 start         # Iniciar serviços
  .\maya.ps1 logs backend  # Ver logs do backend
  .\maya.ps1 clean         # Limpeza completa

Pré-requisitos:
  - Docker Desktop para Windows
  - PowerShell 5.1 ou superior
  - Java 17+ (para build local)
"@
}

# Script principal
switch ($Command.ToLower()) {
    "build" {
        if (Test-Docker) {
            Build-App
        }
    }
    "test" {
        Invoke-Tests
    }
    "start" {
        if (Test-Docker) {
            Start-Services
        }
    }
    "stop" {
        Stop-Services
    }
    "restart" {
        Stop-Services
        Start-Sleep -Seconds 5
        if (Test-Docker) {
            Start-Services
        }
    }
    "logs" {
        Show-Logs -ServiceName $Service
    }
    "status" {
        Show-Status
    }
    "clean" {
        Clear-All
    }
    "help" {
        Show-Help
    }
    default {
        Write-Error "Comando '$Command' não reconhecido"
        Show-Help
        exit 1
    }
}
