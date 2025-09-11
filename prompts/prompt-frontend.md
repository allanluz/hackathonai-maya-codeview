# MAYA Code Review System - Frontend (Angular 18)

## 📋 Visão Geral do Frontend

O frontend é desenvolvido em **Angular 18** com Standalone Components, utilizando PrimeNG para componentes UI e design system moderno. A aplicação oferece uma interface intuitiva para visualização de code reviews, importação de commits e configuração do sistema.

## 🏗️ Estrutura do Projeto

```
frontend/
├── package.json                     # Dependências npm
├── angular.json                     # Configuração Angular
├── tsconfig.json                    # Configuração TypeScript
├── src/
│   ├── index.html                   # HTML principal
│   ├── main.ts                      # Bootstrap da aplicação
│   ├── styles.scss                  # Estilos globais
│   ├── app/
│   │   ├── app.component.ts         # Componente raiz
│   │   ├── app.config.ts            # Configuração da app
│   │   ├── app.routes.ts            # Rotas da aplicação
│   │   ├── components/
│   │   │   ├── dashboard/           # Dashboard principal
│   │   │   ├── review-list/         # Lista de revisões
│   │   │   ├── review-details/      # Detalhes da revisão
│   │   │   ├── commit-import/       # Importação de commits
│   │   │   ├── llm-selector/        # Seletor de modelo LLM
│   │   │   ├── configuration/       # Configurações do sistema
│   │   │   ├── analytics/           # Analytics e relatórios
│   │   │   └── theme-language-toggle/ # Toggle tema/idioma
│   │   ├── services/
│   │   │   ├── code-review.service.ts    # Serviço principal
│   │   │   ├── sinqia-ai.service.ts      # Integração IA
│   │   │   └── configuration.service.ts   # Configurações
│   │   ├── models/
│   │   │   ├── code-review.model.ts      # Modelos de review
│   │   │   ├── llm-model.ts              # Modelo LLM
│   │   │   └── configuration.model.ts     # Modelo configuração
│   │   └── pipes/
│   │       └── translate.pipe.ts         # Pipe de tradução
│   └── environments/
│       ├── environment.ts               # Config desenvolvimento
│       └── environment.prod.ts          # Config produção
└── public/
    └── favicon.ico                      # Favicon da aplicação
```

## 📦 Configuração de Dependências

### package.json

```json
{
  "name": "maya-frontend",
  "version": "1.0.0",
  "scripts": {
    "ng": "ng",
    "start": "ng serve",
    "build": "ng build",
    "build:prod": "ng build --configuration production",
    "watch": "ng build --watch --configuration development",
    "test": "ng test",
    "lint": "ng lint",
    "serve:ssr": "node dist/maya-frontend/server/server.mjs"
  },
  "dependencies": {
    "@angular/animations": "^18.2.0",
    "@angular/cdk": "^18.2.14",
    "@angular/common": "^18.2.0",
    "@angular/compiler": "^18.2.0",
    "@angular/core": "^18.2.0",
    "@angular/forms": "^18.2.0",
    "@angular/material": "^18.2.14",
    "@angular/platform-browser": "^18.2.0",
    "@angular/platform-browser-dynamic": "^18.2.0",
    "@angular/platform-server": "^18.2.0",
    "@angular/router": "^18.2.0",
    "@angular/ssr": "^18.2.20",
    "chart.js": "^4.5.0",
    "express": "^4.18.2",
    "primeflex": "^4.0.0",
    "primeicons": "^7.0.0",
    "primeng": "^17.18.15",
    "rxjs": "~7.8.0",
    "tslib": "^2.3.0",
    "zone.js": "~0.14.10"
  },
  "devDependencies": {
    "@angular-devkit/build-angular": "^18.2.20",
    "@angular/cli": "^18.2.20",
    "@angular/compiler-cli": "^18.2.0",
    "@types/express": "^4.17.17",
    "@types/jasmine": "~5.1.0",
    "@types/node": "^18.18.0",
    "jasmine-core": "~5.2.0",
    "karma": "~6.4.0",
    "karma-chrome-launcher": "~3.2.0",
    "karma-coverage": "~2.2.0",
    "karma-jasmine": "~5.1.0",
    "karma-jasmine-html-reporter": "~2.1.0",
    "typescript": "~5.5.2"
  }
}
```

## 🛠️ Configuração Angular

### app.config.ts

```typescript
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, withFetch } from '@angular/common/http';
import { provideClientHydration } from '@angular/platform-browser';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }), 
    provideRouter(routes), 
    provideClientHydration(),
    provideHttpClient(withInterceptorsFromDi(), withFetch()),
    provideAnimationsAsync()
  ]
};
```

### app.routes.ts

```typescript
import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { 
    path: 'dashboard', 
    loadComponent: () => import('./components/dashboard/dashboard.component')
      .then(m => m.DashboardComponent) 
  },
  { 
    path: 'reviews', 
    loadComponent: () => import('./components/review-list/review-list.component')
      .then(m => m.ReviewListComponent) 
  },
  { 
    path: 'reviews/:id', 
    loadComponent: () => import('./components/review-details/review-details.component')
      .then(m => m.ReviewDetailsComponent) 
  },
  { 
    path: 'import', 
    loadComponent: () => import('./components/commit-import/commit-import.component')
      .then(m => m.CommitImportComponent) 
  },
  { 
    path: 'analytics', 
    loadComponent: () => import('./components/analytics/analytics.component')
      .then(m => m.AnalyticsComponent) 
  },
  { 
    path: 'configuration', 
    loadComponent: () => import('./components/configuration/configuration.component')
      .then(m => m.ConfigurationComponent) 
  },
  { path: '**', redirectTo: '/dashboard' }
];
```

## 🧩 Modelos TypeScript

### code-review.model.ts

```typescript
export interface CodeReview {
  id: number;
  pullRequestId?: string;
  commitSha: string;
  repositoryName: string;
  projectName: string;
  author: string;
  title: string;
  status: ReviewStatus;
  criticalIssues: number;
  totalIssues: number;
  analysisScore: number;
  llmModel?: string;
  analysisOptionsJson?: string;
  reviewComment?: string;
  createdAt: Date;
  updatedAt?: Date;
  fileAnalyses?: FileAnalysis[];
}

export interface FileAnalysis {
  id: number;
  filePath: string;
  className?: string;
  language?: string;
  lineCount: number;
  complexityScore: number;
  connectionImbalance: number;
  score: number;
  connectionEmpresta: number;
  connectionDevolve: number;
  connectionBalanced: boolean;
  hasTypeChanges: boolean;
  hasMethodChanges: boolean;
  hasValidationChanges: boolean;
  analysisReport?: string;
  markdownReport?: string;
  createdAt: Date;
  issues?: AnalysisIssue[];
}

export interface AnalysisIssue {
  id: number;
  severity: IssueSeverity;
  type: IssueType;
  title: string;
  description: string;
  lineNumber?: number;
  columnNumber?: number;
  suggestion?: string;
  createdAt: Date;
}

export enum ReviewStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

export enum IssueSeverity {
  INFO = 'INFO',
  WARNING = 'WARNING',
  ERROR = 'ERROR',
  CRITICAL = 'CRITICAL'
}

export enum IssueType {
  CONNECTION_LEAK = 'CONNECTION_LEAK',
  CODE_QUALITY = 'CODE_QUALITY',
  SECURITY_ISSUE = 'SECURITY_ISSUE',
  PERFORMANCE_ISSUE = 'PERFORMANCE_ISSUE',
  STYLE_VIOLATION = 'STYLE_VIOLATION',
  TYPE_CHANGE = 'TYPE_CHANGE',
  METHOD_CHANGE = 'METHOD_CHANGE',
  VALIDATION_CHANGE = 'VALIDATION_CHANGE',
  COMPLEXITY = 'COMPLEXITY'
}
```

### llm-model.ts

```typescript
export interface LLMModel {
  id: string;
  name: string;
  description: string;
  provider: string;
  maxTokens: number;
  temperature: number;
  isActive: boolean;
  supportsFunctionCalling: boolean;
  multimodal: boolean;
  capabilities?: {
    codeAnalysis: boolean;
    security: boolean;
    performance: boolean;
    architecture: boolean;
  };
  pricing?: {
    inputCostPer1K: number;
    outputCostPer1K: number;
  };
  displayName?: string;
}
```

## 🎛️ Serviços Principais

### code-review.service.ts

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CodeReview, ReviewStatus } from '../models/code-review.model';

@Injectable({
  providedIn: 'root'
})
export class CodeReviewService {
  private apiUrl = `${environment.apiBaseUrl}/reviews`;

  constructor(private http: HttpClient) {}

  /**
   * Listar todas as revisões com filtros
   */
  getAllReviews(
    page: number = 0, 
    size: number = 20, 
    author?: string, 
    repository?: string, 
    status?: ReviewStatus
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (author) {
      params = params.set('author', author);
    }
    if (repository) {
      params = params.set('repository', repository);
    }
    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<any>(this.apiUrl, { params });
  }

  /**
   * Obter revisão por ID
   */
  getReviewById(id: number): Observable<CodeReview> {
    return this.http.get<CodeReview>(`${this.apiUrl}/${id}`);
  }

  /**
   * Criar revisão a partir de commit
   */
  createFromCommit(request: CreateReviewRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/create-from-commit`, request);
  }

  /**
   * Obter métricas do dashboard
   */
  getMetrics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/metrics`);
  }

  /**
   * Obter análises de arquivo por revisão
   */
  getFileAnalyses(reviewId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${reviewId}/files`);
  }

  /**
   * Helper para obter cor do status
   */
  getStatusColor(status: ReviewStatus): string {
    switch (status) {
      case ReviewStatus.PENDING:
        return 'orange';
      case ReviewStatus.IN_PROGRESS:
        return 'blue';
      case ReviewStatus.COMPLETED:
        return 'green';
      case ReviewStatus.FAILED:
        return 'red';
      case ReviewStatus.CANCELLED:
        return 'gray';
      default:
        return 'gray';
    }
  }

  /**
   * Helper para obter ícone do status
   */
  getStatusIcon(status: ReviewStatus): string {
    switch (status) {
      case ReviewStatus.PENDING:
        return 'pi pi-clock';
      case ReviewStatus.IN_PROGRESS:
        return 'pi pi-spin pi-spinner';
      case ReviewStatus.COMPLETED:
        return 'pi pi-check-circle';
      case ReviewStatus.FAILED:
        return 'pi pi-times-circle';
      case ReviewStatus.CANCELLED:
        return 'pi pi-ban';
      default:
        return 'pi pi-question-circle';
    }
  }
}

export interface CreateReviewRequest {
  commitSha: string;
  repositoryName: string;
  projectName: string;
  llmModel?: string;
  analysisOptionsJson?: string;
}
```

### sinqia-ai.service.ts

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { LLMModel } from '../models/llm-model';

@Injectable({
  providedIn: 'root'
})
export class SinqiaAiService {
  private apiUrl = `${environment.apiBaseUrl}/ai`;

  constructor(private http: HttpClient) {}

  /**
   * Obter modelos LLM disponíveis
   */
  getAvailableModels(): Observable<LLMModel[]> {
    return this.http.get<LLMModel[]>(`${this.apiUrl}/models`).pipe(
      catchError(error => {
        console.warn('Erro ao buscar modelos da API, usando fallback', error);
        return of(this.getFallbackModels());
      }),
      map(models => models.map(model => ({
        ...model,
        displayName: `${model.name} (${model.provider})`
      })))
    );
  }

  /**
   * Validar se um modelo está disponível
   */
  validateModel(modelId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/models/${modelId}/validate`);
  }

  /**
   * Testar conectividade com a API
   */
  testConnection(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/health`);
  }

  /**
   * Modelos de fallback quando API não está disponível
   */
  private getFallbackModels(): LLMModel[] {
    return [
      {
        id: 'gpt-4',
        name: 'GPT-4',
        description: 'Modelo avançado da OpenAI para análises complexas de código',
        provider: 'OpenAI',
        maxTokens: 8192,
        temperature: 0.2,
        isActive: true,
        supportsFunctionCalling: true,
        multimodal: false,
        capabilities: {
          codeAnalysis: true,
          security: true,
          performance: true,
          architecture: true
        },
        pricing: {
          inputCostPer1K: 0.03,
          outputCostPer1K: 0.06
        },
        displayName: 'GPT-4 (OpenAI)'
      },
      {
        id: 'gpt-3.5-turbo',
        name: 'GPT-3.5 Turbo',
        description: 'Modelo otimizado da OpenAI para análises rápidas',
        provider: 'OpenAI',
        maxTokens: 4096,
        temperature: 0.2,
        isActive: true,
        supportsFunctionCalling: true,
        multimodal: false,
        capabilities: {
          codeAnalysis: true,
          security: true,
          performance: false,
          architecture: false
        },
        pricing: {
          inputCostPer1K: 0.001,
          outputCostPer1K: 0.002
        },
        displayName: 'GPT-3.5 Turbo (OpenAI)'
      },
      {
        id: 'claude-3',
        name: 'Claude 3',
        description: 'Modelo da Anthropic especializado em análise detalhada',
        provider: 'Anthropic',
        maxTokens: 100000,
        temperature: 0.2,
        isActive: true,
        supportsFunctionCalling: false,
        multimodal: true,
        capabilities: {
          codeAnalysis: true,
          security: true,
          performance: true,
          architecture: true
        },
        pricing: {
          inputCostPer1K: 0.015,
          outputCostPer1K: 0.075
        },
        displayName: 'Claude 3 (Anthropic)'
      }
    ];
  }
}
```

## 🧩 Componentes Principais

### DashboardComponent

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ChartModule } from 'primeng/chart';

import { CodeReviewService } from '../../services/code-review.service';
import { CodeReview, ReviewStatus } from '../../models/code-review.model';
import { TranslatePipe } from '../../pipes/translate.pipe';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ButtonModule,
    CardModule,
    TableModule,
    TagModule,
    ChartModule,
    TranslatePipe
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  
  // Métricas
  metrics = {
    totalReviews: 0,
    pendingReviews: 0,
    completedReviews: 0,
    failedReviews: 0,
    totalCriticalIssues: 0,
    averageScore: 0
  };

  // Reviews recentes
  recentReviews: CodeReview[] = [];
  
  // Dados do gráfico
  chartData: any;
  chartOptions: any;
  
  loading = true;

  constructor(private codeReviewService: CodeReviewService) {}

  ngOnInit() {
    this.loadMetrics();
    this.loadRecentReviews();
    this.initializeChart();
  }

  /**
   * Carregar métricas do dashboard
   */
  loadMetrics() {
    this.codeReviewService.getMetrics().subscribe({
      next: (data) => {
        this.metrics = data;
        this.updateChartData();
        this.loading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar métricas:', error);
        this.loading = false;
      }
    });
  }

  /**
   * Carregar revisões recentes
   */
  loadRecentReviews() {
    this.codeReviewService.getAllReviews(0, 5).subscribe({
      next: (response) => {
        this.recentReviews = response.content || [];
      },
      error: (error) => {
        console.error('Erro ao carregar revisões:', error);
      }
    });
  }

  /**
   * Inicializar configuração do gráfico
   */
  initializeChart() {
    this.chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom'
        }
      }
    };
  }

  /**
   * Atualizar dados do gráfico
   */
  updateChartData() {
    this.chartData = {
      labels: ['Pending', 'Completed', 'Failed'],
      datasets: [
        {
          data: [
            this.metrics.pendingReviews,
            this.metrics.completedReviews,
            this.metrics.failedReviews
          ],
          backgroundColor: [
            '#FF9800',
            '#4CAF50', 
            '#F44336'
          ],
          hoverBackgroundColor: [
            '#FFB74D',
            '#66BB6A',
            '#EF5350'
          ]
        }
      ]
    };
  }

  /**
   * Obter cor do status
   */
  getStatusColor(status: ReviewStatus): string {
    return this.codeReviewService.getStatusColor(status);
  }

  /**
   * Obter ícone do status
   */
  getStatusIcon(status: ReviewStatus): string {
    return this.codeReviewService.getStatusIcon(status);
  }

  /**
   * Navegar para importação
   */
  navigateToImport() {
    // Implementado via routerLink no template
  }

  /**
   * Atualizar dashboard
   */
  refresh() {
    this.loading = true;
    this.loadMetrics();
    this.loadRecentReviews();
  }
}
```

### CommitImportComponent

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';

// PrimeNG Components
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageModule } from 'primeng/message';
import { StepsModule } from 'primeng/steps';
import { ToastModule } from 'primeng/toast';

// Custom Components
import { LlmSelectorComponent } from '../llm-selector/llm-selector.component';

// Services
import { CodeReviewService } from '../../services/code-review.service';
import { LLMModel } from '../../models/llm-model';
import { TranslatePipe } from '../../pipes/translate.pipe';

@Component({
  selector: 'app-commit-import',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardModule,
    ButtonModule,
    InputTextModule,
    InputTextareaModule,
    ProgressSpinnerModule,
    MessageModule,
    StepsModule,
    ToastModule,
    LlmSelectorComponent,
    TranslatePipe
  ],
  providers: [MessageService],
  templateUrl: './commit-import.component.html',
  styleUrls: ['./commit-import.component.scss']
})
export class CommitImportComponent implements OnInit {

  // Forms
  commitForm: FormGroup;
  
  // Wizard steps
  currentStep = 0;
  steps = [
    { label: 'Commit Info' },
    { label: 'LLM Selection' }, 
    { label: 'Analysis Options' }
  ];

  // LLM Selection
  selectedLlmModel: LLMModel | null = null;
  
  // Analysis Options
  analysisOptions = {
    deepAnalysis: true,
    securityCheck: true,
    performanceCheck: false,
    documentationCheck: false
  };

  // Import state
  isImporting = false;
  importProgress = '';
  currentImportStep = '';
  importResult: any = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private codeReviewService: CodeReviewService,
    private messageService: MessageService
  ) {
    this.commitForm = this.fb.group({
      commitId: ['', [Validators.required, Validators.minLength(7)]],
      branchName: ['main', Validators.required],
      repositoryName: ['', Validators.required],
      projectName: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit() {}

  /**
   * Navegar para próximo passo
   */
  nextStep() {
    if (this.currentStep < this.steps.length - 1) {
      if (this.validateCurrentStep()) {
        this.currentStep++;
      }
    }
  }

  /**
   * Navegar para passo anterior
   */
  previousStep() {
    if (this.currentStep > 0) {
      this.currentStep--;
    }
  }

  /**
   * Validar passo atual
   */
  validateCurrentStep(): boolean {
    switch (this.currentStep) {
      case 0:
        return this.commitForm.valid;
      case 1:
        return this.selectedLlmModel !== null;
      case 2:
        return true; // Analysis options são opcionais
      default:
        return true;
    }
  }

  /**
   * Handler para seleção de modelo LLM
   */
  onLlmModelSelected(model: LLMModel) {
    this.selectedLlmModel = model;
  }

  /**
   * Executar importação e análise
   */
  async executeImport() {
    if (!this.selectedLlmModel || this.commitForm.invalid) {
      return;
    }

    this.isImporting = true;
    this.importResult = null;

    try {
      // Simular progresso da importação
      await this.simulateImportProgress();

      // Criar request
      const request = {
        commitSha: this.commitForm.get('commitId')?.value,
        repositoryName: this.commitForm.get('repositoryName')?.value,
        projectName: this.commitForm.get('projectName')?.value,
        llmModel: this.selectedLlmModel.id,
        analysisOptionsJson: JSON.stringify(this.analysisOptions)
      };

      // Executar importação
      const response = await this.codeReviewService.createFromCommit(request).toPromise();

      this.importResult = {
        success: true,
        reviewId: response.reviewId,
        message: 'Commit importado e analisado com sucesso!'
      };

      this.messageService.add({
        severity: 'success',
        summary: 'Sucesso',
        detail: 'Commit importado e analisado com sucesso!'
      });

      // Navegar para a revisão criada após 2 segundos
      setTimeout(() => {
        this.router.navigate(['/reviews', response.reviewId]);
      }, 2000);

    } catch (error: any) {
      console.error('Erro na importação:', error);
      
      this.importResult = {
        success: false,
        message: `Erro durante a importação: ${error.message || 'Erro desconhecido'}`
      };

      this.messageService.add({
        severity: 'error',
        summary: 'Erro',
        detail: 'Falha na importação do commit. Verifique os dados e tente novamente.'
      });
    } finally {
      this.isImporting = false;
    }
  }

  /**
   * Simular progresso da importação
   */
  private async simulateImportProgress(): Promise<void> {
    const steps = [
      'Conectando com TFS/Azure DevOps...',
      'Buscando detalhes do commit...',
      'Analisando arquivos modificados...',
      'Executando análise MAYA...',
      'Processando com IA selecionada...',
      'Gerando relatório final...',
      'Salvando resultados...'
    ];

    for (let i = 0; i < steps.length; i++) {
      this.currentImportStep = steps[i];
      this.importProgress = `${i + 1}/${steps.length}`;
      await this.delay(800);
    }
  }

  /**
   * Utility para delay
   */
  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  /**
   * Resetar formulário
   */
  resetForm() {
    this.commitForm.reset({
      branchName: 'main'
    });
    this.currentStep = 0;
    this.selectedLlmModel = null;
    this.analysisOptions = {
      deepAnalysis: true,
      securityCheck: true,
      performanceCheck: false,
      documentationCheck: false
    };
    this.importResult = null;
  }

  /**
   * Voltar para dashboard
   */
  backToDashboard() {
    this.router.navigate(['/dashboard']);
  }
}
```

## 🎨 Estilos Globais

### styles.scss

```scss
// PrimeNG Theme Import
@import 'primeng/resources/themes/lara-light-blue/theme.css';
@import 'primeng/resources/primeng.css';
@import 'primeicons/primeicons.css';
@import 'primeflex/primeflex.css';

// Variables
:root {
  --primary-color: #0066cc;
  --secondary-color: #6c757d;
  --success-color: #28a745;
  --warning-color: #ffc107;
  --danger-color: #dc3545;
  --info-color: #17a2b8;
  --light-color: #f8f9fa;
  --dark-color: #343a40;
  
  --border-radius: 8px;
  --box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  --transition: all 0.3s ease;
}

// Global Styles
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 
               'Oxygen', 'Ubuntu', 'Cantarell', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f5f7fa;
}

// MAYA App Layout
.maya-app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.maya-header {
  background: linear-gradient(135deg, var(--primary-color), #004299);
  box-shadow: var(--box-shadow);
  z-index: 1000;
}

.main-content {
  flex: 1;
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

// Cards
.maya-card {
  background: white;
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  padding: 1.5rem;
  margin-bottom: 1rem;
  transition: var(--transition);

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }
}

// Stats Cards
.stat-card {
  display: flex;
  align-items: center;
  padding: 1.5rem;
  background: white;
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  transition: var(--transition);

  &:hover {
    transform: translateY(-2px);
  }

  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 1rem;

    i {
      font-size: 1.5rem;
      color: white;
    }

    &.primary { background: var(--primary-color); }
    &.success { background: var(--success-color); }
    &.warning { background: var(--warning-color); }
    &.danger { background: var(--danger-color); }
    &.info { background: var(--info-color); }
  }

  .stat-info {
    h3 {
      margin: 0 0 0.25rem 0;
      font-size: 2rem;
      font-weight: 700;
      color: var(--dark-color);
    }

    p {
      margin: 0;
      color: var(--secondary-color);
      font-weight: 500;
    }
  }
}

// Tables
.review-table {
  .p-datatable-header {
    background: white;
    border: none;
    padding: 1rem;
  }

  .p-datatable-tbody > tr:hover {
    background: rgba(0, 102, 204, 0.05);
  }
}

// Loading states
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;

  .loading-text {
    margin-left: 1rem;
    color: var(--secondary-color);
  }
}

// Empty states
.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: var(--secondary-color);

  .empty-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
    opacity: 0.5;
  }

  h3 {
    margin-bottom: 0.5rem;
    color: var(--dark-color);
  }

  p {
    margin-bottom: 1.5rem;
  }
}

// Responsive Design
@media (max-width: 768px) {
  .container {
    padding: 0 0.5rem;
  }

  .main-content {
    padding: 1rem 0;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;

    .stat-icon {
      margin-right: 0;
      margin-bottom: 1rem;
    }
  }
}

// Utility Classes
.text-center { text-align: center; }
.text-left { text-align: left; }
.text-right { text-align: right; }

.mt-1 { margin-top: 0.25rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-3 { margin-top: 1rem; }
.mt-4 { margin-top: 1.5rem; }
.mt-5 { margin-top: 3rem; }

.mb-1 { margin-bottom: 0.25rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-3 { margin-bottom: 1rem; }
.mb-4 { margin-bottom: 1.5rem; }
.mb-5 { margin-bottom: 3rem; }

.p-1 { padding: 0.25rem; }
.p-2 { padding: 0.5rem; }
.p-3 { padding: 1rem; }
.p-4 { padding: 1.5rem; }
.p-5 { padding: 3rem; }
```

## 🌐 Configuração de Ambiente

### environment.ts

```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8081/api'
};
```

### environment.prod.ts

```typescript
export const environment = {
  production: true,
  apiBaseUrl: '/api'
};
```

## 🚀 Scripts de Build e Deploy

### Comandos Angular

```bash
# Instalar dependências
npm install

# Executar em desenvolvimento
ng serve

# Build para produção
ng build --configuration production

# Executar testes
ng test

# Executar linting
ng lint

# Gerar componente
ng generate component components/nome-componente --standalone

# Gerar serviço
ng generate service services/nome-servico
```

### Build otimizado

```bash
# Build com otimizações máximas
ng build --configuration production --aot --build-optimizer --optimization

# Análise do bundle
ng build --source-map --stats-json
npx webpack-bundle-analyzer dist/maya-frontend/stats.json
```

## 🔧 Próximos Passos

1. Configure o ambiente Angular seguindo `prompt-configuracao.md`
2. Implemente os componentes principais (Dashboard, ImportCommit)
3. Desenvolva os serviços HTTP
4. Integre com o backend seguindo `prompt-integracoes.md`
5. Teste todas as funcionalidades
6. Otimize performance e acessibilidade

O frontend está preparado para oferecer uma experiência moderna e intuitiva para o sistema MAYA, com componentes reutilizáveis e arquitetura escalável.
