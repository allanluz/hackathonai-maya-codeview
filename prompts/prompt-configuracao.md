# MAYA Code Review System - Configuração e Deploy

## 📋 Visão Geral da Configuração

Este documento detalha todas as configurações necessárias para implantar o sistema MAYA em ambiente de produção, incluindo configuração de banco de dados, integração com APIs externas, e configurações de segurança.

## 🗄️ Configuração do Banco de Dados

### SQL Server (Produção)

#### 1. Criação do Banco de Dados

```sql
-- Script de criação do banco MAYA
USE master;
GO

-- Criar database
CREATE DATABASE MayaCodeReview
ON (
    NAME = 'MayaCodeReview',
    FILENAME = 'C:\Database\MayaCodeReview.mdf',
    SIZE = 100MB,
    MAXSIZE = 1GB,
    FILEGROWTH = 10MB
)
LOG ON (
    NAME = 'MayaCodeReview_Log',
    FILENAME = 'C:\Database\MayaCodeReview_Log.ldf',
    SIZE = 10MB,
    MAXSIZE = 100MB,
    FILEGROWTH = 1MB
);
GO

USE MayaCodeReview;
GO

-- Criar usuário para aplicação
CREATE LOGIN maya_user WITH PASSWORD = 'Maya2024!@#$';
CREATE USER maya_user FOR LOGIN maya_user;

-- Conceder permissões
EXEC sp_addrolemember 'db_datareader', 'maya_user';
EXEC sp_addrolemember 'db_datawriter', 'maya_user';
EXEC sp_addrolemember 'db_ddladmin', 'maya_user';
GO
```

#### 2. Schema do Banco de Dados

```sql
-- Tabela principal de revisões de código
CREATE TABLE code_reviews (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    commit_sha NVARCHAR(100) NOT NULL,
    author NVARCHAR(100) NOT NULL,
    message NVARCHAR(MAX),
    commit_date DATETIME2 NOT NULL,
    analysis_date DATETIME2 DEFAULT GETDATE(),
    status NVARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'ERROR')),
    project_name NVARCHAR(100),
    repository_name NVARCHAR(100),
    tfs_url NVARCHAR(500),
    total_files INTEGER DEFAULT 0,
    issues_found INTEGER DEFAULT 0,
    critical_issues INTEGER DEFAULT 0,
    high_issues INTEGER DEFAULT 0,
    medium_issues INTEGER DEFAULT 0,
    low_issues INTEGER DEFAULT 0,
    analysis_duration_ms BIGINT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- Índices para performance
CREATE INDEX IX_code_reviews_commit_sha ON code_reviews(commit_sha);
CREATE INDEX IX_code_reviews_status ON code_reviews(status);
CREATE INDEX IX_code_reviews_analysis_date ON code_reviews(analysis_date);

-- Tabela de análise de arquivos
CREATE TABLE file_analyses (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    code_review_id UNIQUEIDENTIFIER NOT NULL,
    file_path NVARCHAR(500) NOT NULL,
    file_name NVARCHAR(255) NOT NULL,
    file_extension NVARCHAR(10),
    file_content NVARCHAR(MAX),
    language NVARCHAR(50),
    lines_of_code INTEGER DEFAULT 0,
    analysis_date DATETIME2 DEFAULT GETDATE(),
    has_connection_leaks BIT DEFAULT 0,
    connection_leak_count INTEGER DEFAULT 0,
    cyclomatic_complexity FLOAT DEFAULT 0,
    security_issues INTEGER DEFAULT 0,
    code_quality_score FLOAT DEFAULT 0,
    ai_analysis_used BIT DEFAULT 0,
    ai_model_used NVARCHAR(100),
    processing_time_ms BIGINT,
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_file_analyses_code_review 
        FOREIGN KEY (code_review_id) REFERENCES code_reviews(id) 
        ON DELETE CASCADE
);

-- Índices para file_analyses
CREATE INDEX IX_file_analyses_code_review_id ON file_analyses(code_review_id);
CREATE INDEX IX_file_analyses_language ON file_analyses(language);
CREATE INDEX IX_file_analyses_extension ON file_analyses(file_extension);

-- Tabela de issues encontrados
CREATE TABLE analysis_issues (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    file_analysis_id UNIQUEIDENTIFIER NOT NULL,
    type NVARCHAR(100) NOT NULL,
    severity NVARCHAR(20) NOT NULL CHECK (severity IN ('INFO', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    category NVARCHAR(50),
    description NVARCHAR(MAX) NOT NULL,
    recommendation NVARCHAR(MAX),
    line_number INTEGER,
    column_number INTEGER,
    code_snippet NVARCHAR(MAX),
    rule_violated NVARCHAR(100),
    auto_fixable BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT FK_analysis_issues_file_analysis 
        FOREIGN KEY (file_analysis_id) REFERENCES file_analyses(id) 
        ON DELETE CASCADE
);

-- Índices para analysis_issues
CREATE INDEX IX_analysis_issues_file_analysis_id ON analysis_issues(file_analysis_id);
CREATE INDEX IX_analysis_issues_severity ON analysis_issues(severity);
CREATE INDEX IX_analysis_issues_type ON analysis_issues(type);

-- Tabela de configurações do sistema
CREATE TABLE configuration_settings (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    key_name NVARCHAR(100) NOT NULL UNIQUE,
    value NVARCHAR(MAX),
    data_type NVARCHAR(20) DEFAULT 'STRING' CHECK (data_type IN ('STRING', 'INTEGER', 'BOOLEAN', 'JSON')),
    category NVARCHAR(50),
    description NVARCHAR(500),
    is_sensitive BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- Configurações padrão
INSERT INTO configuration_settings (key_name, value, data_type, category, description) VALUES
('tfs.server.url', 'https://tfs.sinqia.com.br', 'STRING', 'TFS', 'URL do servidor TFS'),
('tfs.collection', 'GestaoRecursos', 'STRING', 'TFS', 'Collection do TFS'),
('maya.analysis.enabled', 'true', 'BOOLEAN', 'MAYA', 'Habilitar análise MAYA'),
('maya.ai.enabled', 'true', 'BOOLEAN', 'AI', 'Habilitar integração com IA'),
('maya.complexity.threshold.high', '15', 'INTEGER', 'ANALYSIS', 'Limite para complexidade alta'),
('maya.connection-leak.severity', 'CRITICAL', 'STRING', 'ANALYSIS', 'Severidade para vazamento de conexão');

-- Tabela de arquivos auxiliares
CREATE TABLE auxiliary_files (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    content NVARCHAR(MAX),
    file_type NVARCHAR(50),
    description NVARCHAR(500),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- Inserir arquivos auxiliares padrão
INSERT INTO auxiliary_files (name, content, file_type, description) VALUES
('revisao-codigo.md', '# Padrões de Revisão de Código SINQIA...', 'MARKDOWN', 'Documento de padrões de código'),
('padrao-codigo.md', '# Padrões de Desenvolvimento...', 'MARKDOWN', 'Padrões de desenvolvimento'),
('codereview.chatmode.md', '# Modo Chat para Code Review...', 'MARKDOWN', 'Instruções para modo chat');
```

#### 3. Procedures e Functions

```sql
-- Procedure para limpeza de dados antigos
CREATE PROCEDURE sp_cleanup_old_reviews
    @days_to_keep INTEGER = 90
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @cutoff_date DATETIME2 = DATEADD(day, -@days_to_keep, GETDATE());
    
    -- Deletar análises antigas (cascata para issues)
    DELETE FROM code_reviews 
    WHERE created_at < @cutoff_date;
    
    -- Log da limpeza
    PRINT 'Limpeza concluída. Removidos reviews anteriores a ' + CAST(@cutoff_date AS NVARCHAR(50));
END;
GO

-- Function para estatísticas de qualidade
CREATE FUNCTION fn_get_quality_stats(@review_id UNIQUEIDENTIFIER)
RETURNS TABLE
AS
RETURN (
    SELECT 
        COUNT(*) as total_files,
        AVG(CAST(cyclomatic_complexity AS FLOAT)) as avg_complexity,
        SUM(CASE WHEN has_connection_leaks = 1 THEN 1 ELSE 0 END) as files_with_leaks,
        AVG(code_quality_score) as avg_quality_score
    FROM file_analyses 
    WHERE code_review_id = @review_id
);
GO

-- View para dashboard
CREATE VIEW vw_dashboard_stats AS
SELECT 
    CAST(analysis_date AS DATE) as analysis_day,
    COUNT(*) as total_reviews,
    SUM(critical_issues) as total_critical,
    SUM(high_issues) as total_high,
    SUM(issues_found) as total_issues,
    AVG(CAST(analysis_duration_ms AS FLOAT)) as avg_duration_ms
FROM code_reviews 
WHERE analysis_date >= DATEADD(day, -30, GETDATE())
GROUP BY CAST(analysis_date AS DATE);
GO
```

### H2 Database (Desenvolvimento)

#### application-dev.properties

```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:maya;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (apenas desenvolvimento)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.h2.console.settings.web-allow-others=false

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

#### data.sql (Dados de Teste)

```sql
-- Dados de teste para desenvolvimento
INSERT INTO configuration_settings (id, key_name, value, data_type, category, description) VALUES 
(NEWID(), 'tfs.server.url', 'https://dev.azure.com/sinqia-dev', 'STRING', 'TFS', 'URL do servidor TFS de desenvolvimento'),
(NEWID(), 'maya.analysis.enabled', 'true', 'BOOLEAN', 'MAYA', 'Habilitar análise MAYA'),
(NEWID(), 'maya.ai.enabled', 'false', 'BOOLEAN', 'AI', 'Desabilitar IA em desenvolvimento');

-- Code Review de exemplo
INSERT INTO code_reviews (id, commit_sha, author, message, commit_date, status, project_name) VALUES
(NEWID(), 'abc12345', 'joão.silva', 'Implementação inicial do serviço de usuários', '2024-01-15 10:30:00', 'COMPLETED', 'Sistema Exemplo');
```

## 🔧 Configuração do Spring Boot

### application.properties (Base)

```properties
# Server Configuration
server.port=8081
server.servlet.context-path=/
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
server.compression.min-response-size=1024

# Application Info
spring.application.name=Maya Code Review System
spring.application.version=1.0.0
info.app.name=${spring.application.name}
info.app.version=${spring.application.version}

# Jackson Configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=America/Sao_Paulo
spring.jackson.default-property-inclusion=NON_NULL

# Multipart Configuration
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true

# Logging Configuration
logging.level.com.sinqia.maya=INFO
logging.level.org.springframework.security=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.file.name=logs/maya.log
logging.file.max-size=10MB
logging.file.max-history=30

# CORS Configuration
maya.cors.allowed-origins=http://localhost:4200,https://maya.sinqia.com.br
maya.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
maya.cors.allowed-headers=*
maya.cors.max-age=3600

# Cache Configuration
spring.cache.type=simple
spring.cache.cache-names=models,configurations,file-analyses

# Async Configuration
maya.async.core-pool-size=10
maya.async.max-pool-size=20
maya.async.queue-capacity=500
```

### application-prod.properties

```properties
# Production Database
spring.datasource.url=jdbc:sqlserver://sqlserver.sinqia.local:1433;databaseName=MayaCodeReview;encrypt=true;trustServerCertificate=true
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA Production Settings
spring.jpa.database-platform=org.hibernate.dialect.SQLServerDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# Connection Pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.validation-timeout=5000
spring.datasource.hikari.leak-detection-threshold=60000

# TFS Configuration
tfs.server.url=${TFS_SERVER_URL:https://tfs.sinqia.com.br}
tfs.collection=${TFS_COLLECTION:GestaoRecursos}
tfs.username=${TFS_USERNAME}
tfs.password=${TFS_PAT_TOKEN}
tfs.timeout.connection=30000
tfs.timeout.read=60000

# Sinqia AI Configuration
maya.ai.base-url=${SINQIA_AI_URL:http://everai.sinqia.com.br}
maya.ai.timeout=30000
maya.ai.enabled=true
maya.ai.api-key=${SINQIA_AI_API_KEY}

# Security
maya.security.jwt.secret=${JWT_SECRET}
maya.security.jwt.expiration=86400000
maya.security.cors.enabled=true

# Logging Production
logging.level.com.sinqia.maya=INFO
logging.level.org.springframework.security=WARN
logging.level.org.hibernate.SQL=WARN
logging.file.name=/var/log/maya/maya.log

# Monitoring
management.endpoints.web.exposure.include=health,metrics,prometheus
management.metrics.export.prometheus.enabled=true

# Performance
maya.analysis.parallel-processing=true
maya.analysis.max-threads=10
maya.analysis.batch-size=50
```

## 🐳 Configuração Docker

### Dockerfile (Backend)

```dockerfile
FROM openjdk:17-jdk-slim

# Instalar dependências
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Criar diretório da aplicação
WORKDIR /app

# Copiar arquivo JAR
COPY target/maya-backend-*.jar app.jar

# Criar diretório para logs
RUN mkdir -p /var/log/maya

# Configurar usuário não-root
RUN useradd -m -u 1001 maya && \
    chown -R maya:maya /app /var/log/maya
USER maya

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Expor porta
EXPOSE 8081

# Comando de inicialização
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", \
    "-Xmx1g", \
    "-XX:+UseG1GC", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/var/log/maya/", \
    "-jar", "app.jar"]
```

### Dockerfile (Frontend)

```dockerfile
# Build stage
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build:prod

# Production stage
FROM nginx:alpine

# Copiar arquivos buildados
COPY --from=builder /app/dist/maya-frontend /usr/share/nginx/html

# Configuração do Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Health check
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost/ || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### nginx.conf

```nginx
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied expired no-cache no-store private auth;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/javascript
        application/xml+rss
        application/json;

    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;

        # Security headers
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header Referrer-Policy "no-referrer-when-downgrade" always;
        add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;

        # Angular routes
        location / {
            try_files $uri $uri/ /index.html;
            expires 1h;
            add_header Cache-Control "public, immutable, must-revalidate";
        }

        # Static assets
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }

        # API proxy
        location /api/ {
            proxy_pass http://maya-backend:8081;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Health check
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
    }
}
```

### docker-compose.yml (Completo)

```yaml
version: '3.8'

services:
  # SQL Server
  sqlserver:
    image: mcr.microsoft.com/mssql/server:2019-latest
    container_name: maya-sqlserver
    environment:
      ACCEPT_EULA: Y
      SA_PASSWORD: ${SQL_SA_PASSWORD:-Maya2024!@#$}
      MSSQL_PID: Express
    ports:
      - "1433:1433"
    volumes:
      - sqlserver_data:/var/opt/mssql
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
    networks:
      - maya-network
    restart: unless-stopped

  # Backend
  maya-backend:
    build: 
      context: ./backend
      dockerfile: Dockerfile
    container_name: maya-backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_USERNAME: maya_user
      DB_PASSWORD: ${DB_PASSWORD:-Maya2024!@#$}
      TFS_SERVER_URL: ${TFS_SERVER_URL}
      TFS_COLLECTION: ${TFS_COLLECTION:-GestaoRecursos}
      TFS_USERNAME: ${TFS_USERNAME}
      TFS_PAT_TOKEN: ${TFS_PAT_TOKEN}
      SINQIA_AI_URL: ${SINQIA_AI_URL:-http://everai.sinqia.com.br}
      SINQIA_AI_API_KEY: ${SINQIA_AI_API_KEY}
      JWT_SECRET: ${JWT_SECRET:-maya-jwt-secret-key-2024}
    ports:
      - "8081:8081"
    depends_on:
      - sqlserver
    volumes:
      - ./logs:/var/log/maya
      - ./reports:/app/reports
    networks:
      - maya-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Frontend
  maya-frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: maya-frontend
    ports:
      - "80:80"
    depends_on:
      - maya-backend
    networks:
      - maya-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health"]
      interval: 30s
      timeout: 5s
      retries: 3

volumes:
  sqlserver_data:
    driver: local

networks:
  maya-network:
    driver: bridge
```

### .env (Template)

```bash
# Database Configuration
SQL_SA_PASSWORD=Maya2024!@#$
DB_PASSWORD=Maya2024!@#$

# TFS Configuration
TFS_SERVER_URL=https://tfs.sinqia.com.br
TFS_COLLECTION=GestaoRecursos
TFS_USERNAME=seu.usuario
TFS_PAT_TOKEN=seu-personal-access-token

# Sinqia AI Configuration
SINQIA_AI_URL=http://everai.sinqia.com.br
SINQIA_AI_API_KEY=sua-api-key

# Security
JWT_SECRET=maya-jwt-secret-super-seguro-2024-sinqia

# Environment
ENVIRONMENT=production
LOG_LEVEL=INFO
```

## 🚀 Scripts de Deploy

### deploy.sh (Linux/Mac)

```bash
#!/bin/bash

set -e

echo "🚀 Iniciando deploy do MAYA Code Review System..."

# Configurações
DEPLOY_ENV=${1:-production}
BACKUP_DB=${2:-true}

echo "📋 Ambiente: $DEPLOY_ENV"

# 1. Verificar pré-requisitos
echo "1️⃣ Verificando pré-requisitos..."
command -v docker >/dev/null 2>&1 || { echo "❌ Docker não encontrado"; exit 1; }
command -v docker-compose >/dev/null 2>&1 || { echo "❌ Docker Compose não encontrado"; exit 1; }

# 2. Backup do banco (se solicitado)
if [ "$BACKUP_DB" = "true" ] && [ -f "docker-compose.yml" ]; then
    echo "2️⃣ Fazendo backup do banco de dados..."
    mkdir -p ./backups
    docker-compose exec sqlserver /opt/mssql-tools/bin/sqlcmd \
        -S localhost -U sa -P "${SQL_SA_PASSWORD}" \
        -Q "BACKUP DATABASE MayaCodeReview TO DISK = '/tmp/maya_backup_$(date +%Y%m%d_%H%M%S).bak'"
fi

# 3. Parar serviços existentes
echo "3️⃣ Parando serviços existentes..."
docker-compose down --remove-orphans

# 4. Build das imagens
echo "4️⃣ Building aplicação..."
docker-compose build --no-cache

# 5. Iniciar serviços
echo "5️⃣ Iniciando serviços..."
docker-compose up -d

# 6. Aguardar inicialização
echo "6️⃣ Aguardando inicialização dos serviços..."
sleep 30

# 7. Health check
echo "7️⃣ Verificando saúde dos serviços..."
backend_health=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health || echo "000")
frontend_health=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/health || echo "000")

if [ "$backend_health" = "200" ]; then
    echo "✅ Backend: Saudável"
else
    echo "❌ Backend: Problema (HTTP $backend_health)"
fi

if [ "$frontend_health" = "200" ]; then
    echo "✅ Frontend: Saudável"
else
    echo "❌ Frontend: Problema (HTTP $frontend_health)"
fi

# 8. Mostrar logs se houver problemas
if [ "$backend_health" != "200" ] || [ "$frontend_health" != "200" ]; then
    echo "📋 Logs dos serviços:"
    docker-compose logs --tail=50
    exit 1
fi

echo "🎉 Deploy concluído com sucesso!"
echo "📊 Dashboard: http://localhost"
echo "🔧 API: http://localhost:8081"
echo "📋 Logs: docker-compose logs -f"
```

### deploy.ps1 (Windows)

```powershell
# Deploy script para Windows
param(
    [string]$Environment = "production",
    [bool]$BackupDB = $true
)

Write-Host "🚀 Iniciando deploy do MAYA Code Review System..." -ForegroundColor Yellow
Write-Host "📋 Ambiente: $Environment" -ForegroundColor Cyan

try {
    # 1. Verificar Docker
    Write-Host "1️⃣ Verificando Docker..." -ForegroundColor Blue
    $dockerVersion = docker --version 2>$null
    if (-not $dockerVersion) {
        throw "Docker não encontrado. Instale o Docker Desktop."
    }
    Write-Host "✅ Docker OK: $dockerVersion" -ForegroundColor Green

    # 2. Parar serviços
    Write-Host "2️⃣ Parando serviços existentes..." -ForegroundColor Blue
    docker-compose down --remove-orphans

    # 3. Build
    Write-Host "3️⃣ Building aplicação..." -ForegroundColor Blue
    docker-compose build --no-cache
    if ($LASTEXITCODE -ne 0) {
        throw "Erro no build da aplicação"
    }

    # 4. Iniciar
    Write-Host "4️⃣ Iniciando serviços..." -ForegroundColor Blue
    docker-compose up -d
    if ($LASTEXITCODE -ne 0) {
        throw "Erro ao iniciar serviços"
    }

    # 5. Health check
    Write-Host "5️⃣ Verificando saúde dos serviços..." -ForegroundColor Blue
    Start-Sleep -Seconds 30

    $backendHealth = try { 
        (Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing).StatusCode 
    } catch { 0 }

    $frontendHealth = try { 
        (Invoke-WebRequest -Uri "http://localhost/health" -UseBasicParsing).StatusCode 
    } catch { 0 }

    if ($backendHealth -eq 200) {
        Write-Host "✅ Backend: Saudável" -ForegroundColor Green
    } else {
        Write-Host "❌ Backend: Problema" -ForegroundColor Red
    }

    if ($frontendHealth -eq 200) {
        Write-Host "✅ Frontend: Saudável" -ForegroundColor Green
    } else {
        Write-Host "❌ Frontend: Problema" -ForegroundColor Red
    }

    if ($backendHealth -eq 200 -and $frontendHealth -eq 200) {
        Write-Host "🎉 Deploy concluído com sucesso!" -ForegroundColor Green
        Write-Host "📊 Dashboard: http://localhost" -ForegroundColor Cyan
        Write-Host "🔧 API: http://localhost:8081" -ForegroundColor Cyan
    } else {
        Write-Host "📋 Exibindo logs..." -ForegroundColor Yellow
        docker-compose logs --tail=50
    }

} catch {
    Write-Host "❌ Erro no deploy: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
```

## 🔒 Configuração de Segurança

### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${maya.security.cors.enabled:true}")
    private boolean corsEnabled;

    @Value("${maya.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        if (corsEnabled) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(Arrays.asList("*"));
            configuration.setAllowCredentials(true);
        }
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().permitAll() // Para desenvolvimento
            )
            .headers().frameOptions().disable(); // Para H2 console

        return http.build();
    }
}
```

## 📊 Monitoramento e Observabilidade

### Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'maya-backend'
    static_configs:
      - targets: ['maya-backend:8081']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s

  - job_name: 'maya-frontend'
    static_configs:
      - targets: ['maya-frontend:80']
    metrics_path: '/health'
    scrape_interval: 60s
```

### Grafana Dashboard (JSON)

```json
{
  "dashboard": {
    "title": "MAYA Code Review System",
    "panels": [
      {
        "title": "Análises por Hora",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(maya_code_reviews_total[1h])",
            "legendFormat": "Reviews/hora"
          }
        ]
      },
      {
        "title": "Issues Críticos",
        "type": "stat",
        "targets": [
          {
            "expr": "sum(maya_critical_issues_total)",
            "legendFormat": "Issues Críticos"
          }
        ]
      }
    ]
  }
}
```

## 📋 Checklist de Configuração

### ✅ Banco de Dados

- [ ] SQL Server instalado e configurado
- [ ] Database MayaCodeReview criado
- [ ] Usuário maya_user criado com permissões
- [ ] Schema aplicado com sucesso
- [ ] Dados iniciais inseridos
- [ ] Backup automatizado configurado

### ✅ Aplicação

- [ ] Variáveis de ambiente configuradas
- [ ] Conexão com banco testada
- [ ] TFS/Azure DevOps configurado
- [ ] Sinqia AI configurado (se disponível)
- [ ] Logs configurados
- [ ] Health checks funcionando

### ✅ Docker/Deploy

- [ ] Dockerfile otimizado
- [ ] Docker Compose configurado
- [ ] Volumes persistentes configurados
- [ ] Health checks implementados
- [ ] Scripts de deploy testados

### ✅ Segurança

- [ ] CORS configurado apropriadamente
- [ ] Senhas seguras configuradas
- [ ] Headers de segurança implementados
- [ ] Logs de auditoria ativados

### ✅ Monitoramento

- [ ] Actuator endpoints habilitados
- [ ] Prometheus metrics configuradas
- [ ] Alertas configurados
- [ ] Dashboard de monitoramento criado

## 🎯 Próximos Passos

1. Execute os scripts SQL para configurar o banco
2. Configure as variáveis de ambiente
3. Execute o deploy usando docker-compose
4. Teste todas as integrações
5. Configure monitoramento e alertas
6. Documente procedimentos operacionais

Este guia fornece uma base sólida para configuração completa do sistema MAYA em ambiente de produção.
