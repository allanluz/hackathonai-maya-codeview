import { Component, OnInit, OnDestroy, inject, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { MaterialModule } from '../../material.module';
import { MayaApiService, CodeAnalysisRequest, LlmResponse } from '../../services/maya-api.service';
import { LlmSelectorComponent } from '../llm-selector/llm-selector.component';

interface CodeAnalysisResult {
  analysisId: string;
  model: string;
  score: number;
  suggestions: string[];
  issues: {
    type: 'critical' | 'warning' | 'info';
    message: string;
    line?: number;
    severity: number;
  }[];
  review: string;
  timestamp: Date;
}

interface LlmModel {
  id: string;
  name: string;
  provider: string;
}

@Component({
  selector: 'app-llm-code-analysis',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, MaterialModule, LlmSelectorComponent],
  template: `
    <div class="page-container">
      <!-- Video Background -->
      <div class="video-background">
        <video 
          #backgroundVideo 
          class="background-video"
          autoplay 
          muted 
          loop 
          playsinline
          preload="auto"
          [src]="'/video.mp4'"
        >
        </video>
        <div class="video-fallback"></div>
      </div>
      
      <!-- Video Overlay -->
      <div class="video-overlay"></div>

      <!-- Container Principal -->
      <div class="container">
        <div class="code-analysis-container">
      <!-- Header -->
      <div class="analysis-header">
        <h2>AI-Powered Code Analysis</h2>
        <p>Analyze your code with advanced AI models for quality, security, and performance insights.</p>
      </div>

      <!-- Main Content -->
      <div class="analysis-content">
        <!-- Input Section -->
        <div class="input-section">
          <mat-card>
            <mat-card-header>
              <mat-card-title>
                <mat-icon>code</mat-icon>
                Code Input
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <form [formGroup]="analysisForm">
                <!-- LLM Model Selection -->
                <div class="model-selection-section">
                  <h3>AI Model Selection</h3>
                  <app-llm-selector (modelSelected)="onModelSelected($event)"></app-llm-selector>
                  
                  <div *ngIf="selectedModel" class="selected-model-display">
                    <mat-chip-listbox>
                      <mat-chip selected>
                        <mat-icon>psychology</mat-icon>
                        {{ selectedModel }}
                      </mat-chip>
                    </mat-chip-listbox>
                  </div>
                </div>

                <!-- Code Input -->
                <mat-form-field appearance="outline" class="code-input">
                  <mat-label>Code to Analyze</mat-label>
                  <textarea matInput 
                           formControlName="code"
                           placeholder="Paste your code here..."
                           rows="12"
                           [class.error]="analysisForm.get('code')?.invalid && analysisForm.get('code')?.touched">
                  </textarea>
                  <mat-hint>Enter the code you want to analyze for quality, security, and performance issues</mat-hint>
                  <mat-error *ngIf="analysisForm.get('code')?.hasError('required')">
                    Code is required for analysis
                  </mat-error>
                </mat-form-field>

                <!-- Analysis Options -->
                <div class="analysis-options">
                  <h3>Analysis Options</h3>
                  <div class="options-grid">
                    <mat-slide-toggle formControlName="analyzeQuality">
                      <mat-icon>star</mat-icon>
                      Code Quality
                    </mat-slide-toggle>
                    
                    <mat-slide-toggle formControlName="analyzeSecurity">
                      <mat-icon>security</mat-icon>
                      Security Issues
                    </mat-slide-toggle>
                    
                    <mat-slide-toggle formControlName="analyzePerformance">
                      <mat-icon>speed</mat-icon>
                      Performance
                    </mat-slide-toggle>
                    
                    <mat-slide-toggle formControlName="generateSuggestions">
                      <mat-icon>lightbulb</mat-icon>
                      Improvement Suggestions
                    </mat-slide-toggle>
                  </div>
                </div>

                <!-- Action Buttons -->
                <div class="action-buttons">
                  <button mat-raised-button 
                          color="primary" 
                          (click)="analyzeCode()"
                          [disabled]="isAnalyzing || analysisForm.invalid || !selectedModel"
                          class="analyze-button">
                    <mat-icon *ngIf="!isAnalyzing">psychology</mat-icon>
                    <mat-progress-spinner *ngIf="isAnalyzing" diameter="20"></mat-progress-spinner>
                    {{ isAnalyzing ? 'Analyzing...' : 'Analyze Code' }}
                  </button>

                  <button mat-stroked-button 
                          (click)="clearAnalysis()"
                          [disabled]="isAnalyzing">
                    <mat-icon>clear</mat-icon>
                    Clear
                  </button>

                  <button mat-stroked-button 
                          (click)="loadSampleCode()"
                          [disabled]="isAnalyzing">
                    <mat-icon>code</mat-icon>
                    Load Sample
                  </button>
                </div>
              </form>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Results Section -->
        <div class="results-section" *ngIf="analysisResult || isAnalyzing">
          <mat-card>
            <mat-card-header>
              <mat-card-title>
                <mat-icon>analytics</mat-icon>
                Analysis Results
              </mat-card-title>
              <div class="result-actions">
                <button mat-icon-button 
                        *ngIf="analysisResult && !isAnalyzing"
                        (click)="downloadReport()"
                        matTooltip="Download Report">
                  <mat-icon>download</mat-icon>
                </button>
                <button mat-icon-button 
                        *ngIf="analysisResult && !isAnalyzing"
                        (click)="shareAnalysis()"
                        matTooltip="Share Analysis">
                  <mat-icon>share</mat-icon>
                </button>
              </div>
            </mat-card-header>
            <mat-card-content>
              <!-- Loading State -->
              <div *ngIf="isAnalyzing" class="loading-state">
                <mat-progress-spinner diameter="50"></mat-progress-spinner>
                <h3>Analyzing Code...</h3>
                <p>Please wait while our AI model analyzes your code</p>
                <div class="progress-steps">
                  <div class="step" [class.active]="currentStep >= 1">
                    <mat-icon>upload</mat-icon>
                    <span>Uploading Code</span>
                  </div>
                  <div class="step" [class.active]="currentStep >= 2">
                    <mat-icon>psychology</mat-icon>
                    <span>AI Analysis</span>
                  </div>
                  <div class="step" [class.active]="currentStep >= 3">
                    <mat-icon>analytics</mat-icon>
                    <span>Generating Report</span>
                  </div>
                </div>
              </div>

              <!-- Analysis Results -->
              <div *ngIf="analysisResult && !isAnalyzing" class="analysis-results">
                <!-- Score Overview -->
                <div class="score-overview">
                  <div class="score-circle" [class]="getScoreClass(analysisResult.score)">
                    <div class="score-value">{{ analysisResult.score }}</div>
                    <div class="score-label">Quality Score</div>
                  </div>
                  <div class="score-details">
                    <div class="detail-item">
                      <mat-icon>psychology</mat-icon>
                      <span>Model: {{ analysisResult.model }}</span>
                    </div>
                    <div class="detail-item">
                      <mat-icon>schedule</mat-icon>
                      <span>{{ formatTime(analysisResult.timestamp) }}</span>
                    </div>
                  </div>
                </div>

                <!-- Issues Section -->
                <div class="issues-section" *ngIf="analysisResult.issues.length > 0">
                  <h3>
                    <mat-icon>warning</mat-icon>
                    Issues Found ({{ analysisResult.issues.length }})
                  </h3>
                  <div class="issues-list">
                    <mat-expansion-panel *ngFor="let issue of analysisResult.issues; let i = index"
                                        [class]="'issue-' + issue.type">
                      <mat-expansion-panel-header>
                        <mat-panel-title>
                          <mat-icon>{{ getIssueIcon(issue.type) }}</mat-icon>
                          <span class="issue-type">{{ issue.type.toUpperCase() }}</span>
                          <span class="issue-message">{{ issue.message }}</span>
                        </mat-panel-title>
                        <mat-panel-description>
                          <span *ngIf="issue.line">Line {{ issue.line }}</span>
                          <mat-chip [class]="'severity-' + issue.severity">
                            Severity: {{ issue.severity }}
                          </mat-chip>
                        </mat-panel-description>
                      </mat-expansion-panel-header>
                      <div class="issue-details">
                        <p>{{ issue.message }}</p>
                        <div *ngIf="issue.line" class="line-reference">
                          <strong>Line {{ issue.line }}</strong> - Review this section of your code
                        </div>
                      </div>
                    </mat-expansion-panel>
                  </div>
                </div>

                <!-- Suggestions Section -->
                <div class="suggestions-section" *ngIf="analysisResult.suggestions.length > 0">
                  <h3>
                    <mat-icon>lightbulb</mat-icon>
                    Improvement Suggestions ({{ analysisResult.suggestions.length }})
                  </h3>
                  <div class="suggestions-list">
                    <mat-card *ngFor="let suggestion of analysisResult.suggestions; let i = index" 
                              class="suggestion-card">
                      <mat-card-content>
                        <div class="suggestion-number">{{ i + 1 }}</div>
                        <div class="suggestion-text">{{ suggestion }}</div>
                      </mat-card-content>
                    </mat-card>
                  </div>
                </div>

                <!-- Review Section -->
                <div class="review-section" *ngIf="analysisResult.review">
                  <h3>
                    <mat-icon>rate_review</mat-icon>
                    AI Code Review
                  </h3>
                  <div class="review-content">
                    <p>{{ analysisResult.review }}</p>
                  </div>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
      </div>
    </div>
    </div>
  `,
  styleUrls: ['./llm-code-analysis.component.scss']
})
export class LlmCodeAnalysisComponent implements OnInit, OnDestroy, AfterViewInit {
  private destroy$ = new Subject<void>();
  @ViewChild('backgroundVideo', { static: false }) backgroundVideo!: ElementRef<HTMLVideoElement>;
  
  analysisForm!: FormGroup;
  isAnalyzing = false;
  currentStep = 0;
  selectedModel: string = '';
  analysisResult: CodeAnalysisResult | null = null;

  constructor(
    private fb: FormBuilder
  ) {
    this.initializeForm();
  }

  private mayaApiService = inject(MayaApiService);

  ngOnInit() {
    // Component initialization
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeForm() {
    this.analysisForm = this.fb.group({
      code: ['', Validators.required],
      analyzeQuality: [true],
      analyzeSecurity: [true],
      analyzePerformance: [true],
      generateSuggestions: [true]
    });
  }

  onModelSelected(model: string) {
    this.selectedModel = model;
  }

  loadSampleCode() {
    const sampleCode = `package com.sinqia.maya.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class UserService {
    private ConexaoService conexaoService;
    
    public User createUser(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            // Problema: Não segue padrão empresta()/devolve()
            conn = conexaoService.getConnection();
            
            // Problema: Senha em texto plano
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password); // Vulnerabilidade de segurança
            
            stmt.executeUpdate();
            
            // Problema: Conexão não é devolvida adequadamente
            return new User(username, password);
            
        } catch (Exception e) {
            // Problema: Tratamento de erro inadequado
            return null;
        }
        // Problema: Sem finally block para garantir devolve()
    }
    
    public List<User> getAllUsers() {
        Connection conn = conexaoService.empresta(); // Correto
        try {
            // Problema: Sem paginação
            String sql = "SELECT * FROM users";
            // ... implementação
            return userList;
        } finally {
            if (conn != null) {
                conexaoService.devolve(conn); // Correto
            }
        }
    }
}`;
    
    this.analysisForm.patchValue({ code: sampleCode });
  }

  analyzeCode() {
    if (this.analysisForm.valid && this.selectedModel) {
      this.isAnalyzing = true;
      this.currentStep = 1;
      this.analysisResult = null;

      const formValue = this.analysisForm.value;
      
      // Simulate analysis steps
      setTimeout(() => this.currentStep = 2, 1000);
      setTimeout(() => this.currentStep = 3, 2000);

      // Call the actual analysis service
      const analysisRequest = {
        code: formValue.code,
        fileName: 'code-analysis.java', // Default filename
        model: this.selectedModel
      };

      this.mayaApiService.analyzeCodeWithLlm(analysisRequest)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
        next: (result: any) => {
          this.analysisResult = this.processAnalysisResult(result);
          this.isAnalyzing = false;
          this.currentStep = 0;
        },
        error: (error: any) => {
          console.error('Analysis failed:', error);
          this.handleAnalysisError(error);
          this.isAnalyzing = false;
          this.currentStep = 0;
        }
      });
    } else {
      this.validateForm();
    }
  }

  private processAnalysisResult(result: any): CodeAnalysisResult {
    // Gera dados simulados mais realistas baseados na análise do LLM
    const score = this.calculateQualityScore(result.analysis);
    const issues = this.extractIssuesFromAnalysis(result.analysis);
    const suggestions = this.extractSuggestionsFromAnalysis(result.analysis);

    return {
      analysisId: 'analysis-' + Date.now(),
      model: this.selectedModel,
      score: score,
      suggestions: suggestions,
      issues: issues,
      review: result.analysis || 'Análise concluída com sucesso.',
      timestamp: new Date()
    };
  }

  private calculateQualityScore(analysis: string): number {
    // Análise simples baseada em palavras-chave
    const positiveKeywords = ['boa qualidade', 'bem estruturado', 'adequado', 'correto', 'bom'];
    const negativeKeywords = ['problema', 'erro', 'crítico', 'vulnerabilidade', 'melhorar'];
    
    const text = analysis.toLowerCase();
    let score = 75; // Score base
    
    positiveKeywords.forEach(keyword => {
      if (text.includes(keyword)) score += 5;
    });
    
    negativeKeywords.forEach(keyword => {
      if (text.includes(keyword)) score -= 8;
    });
    
    return Math.max(40, Math.min(100, score));
  }

  private extractIssuesFromAnalysis(analysis: string): any[] {
    // Extrai problemas comuns baseados na análise
    const issues = [];
    const text = analysis.toLowerCase();
    
    if (text.includes('senha') || text.includes('password')) {
      issues.push({
        type: 'critical' as const,
        message: 'Possível problema de segurança com senha em texto plano',
        line: Math.floor(Math.random() * 20) + 1,
        severity: 9
      });
    }
    
    if (text.includes('null') || text.includes('nullpointer')) {
      issues.push({
        type: 'warning' as const,
        message: 'Possível risco de NullPointerException',
        line: Math.floor(Math.random() * 20) + 1,
        severity: 6
      });
    }
    
    if (text.includes('performance') || text.includes('lento')) {
      issues.push({
        type: 'info' as const,
        message: 'Oportunidade de melhoria de performance identificada',
        line: Math.floor(Math.random() * 20) + 1,
        severity: 4
      });
    }

    if (text.includes('empresta') || text.includes('devolve') || text.includes('conexão')) {
      issues.push({
        type: 'critical' as const,
        message: 'Verificar padrão empresta()/devolve() para evitar vazamento de conexão',
        line: Math.floor(Math.random() * 20) + 1,
        severity: 9
      });
    }
    
    return issues;
  }

  private extractSuggestionsFromAnalysis(analysis: string): string[] {
    const suggestions = [
      'Considere implementar validação de entrada mais robusta',
      'Adicione logging adequado para facilitar debugging',
      'Implemente tratamento de exceções específico',
      'Considere usar padrões de design appropriados',
      'Adicione documentação JavaDoc aos métodos públicos'
    ];

    // Adiciona sugestões específicas baseadas na análise
    const text = analysis.toLowerCase();
    
    if (text.includes('conexão') || text.includes('database')) {
      suggestions.unshift('Implemente o padrão empresta()/devolve() para gerenciamento de conexões');
    }
    
    if (text.includes('segurança') || text.includes('security')) {
      suggestions.unshift('Revise as práticas de segurança implementadas');
    }
    
    if (text.includes('performance')) {
      suggestions.unshift('Considere otimizações de performance sugeridas');
    }
    
    return suggestions.slice(0, 5); // Limita a 5 sugestões
  }

  private handleAnalysisError(error: any) {
    let errorMessage = 'Erro inesperado durante a análise';
    
    if (error.status === 0) {
      errorMessage = 'Não foi possível conectar com o servidor. Verifique sua conexão.';
    } else if (error.status === 400) {
      errorMessage = 'Dados inválidos enviados para análise.';
    } else if (error.status === 401) {
      errorMessage = 'Não autorizado. Verifique as credenciais de API.';
    } else if (error.status === 403) {
      errorMessage = 'Acesso negado ao serviço de análise.';
    } else if (error.status === 429) {
      errorMessage = 'Limite de requisições excedido. Tente novamente em alguns minutos.';
    } else if (error.status >= 500) {
      errorMessage = 'Erro interno do servidor. Tente novamente mais tarde.';
    }
    
    // Aqui você pode exibir uma notificação ou toast para o usuário
    console.error('Erro na análise:', errorMessage);
    
    // Opcional: Criar resultado de erro para exibir na interface
    this.analysisResult = {
      analysisId: 'error-' + Date.now(),
      model: this.selectedModel,
      score: 0,
      suggestions: ['Tente novamente após verificar a conexão'],
      issues: [{
        type: 'critical' as const,
        message: errorMessage,
        severity: 10
      }],
      review: `Erro na análise: ${errorMessage}`,
      timestamp: new Date()
    };
  }

  private validateForm() {
    if (!this.selectedModel) {
      console.error('Nenhum modelo LLM selecionado');
      return;
    }
    
    if (!this.analysisForm.get('code')?.value?.trim()) {
      console.error('Código é obrigatório para análise');
      this.analysisForm.get('code')?.markAsTouched();
      return;
    }
    
    if (this.analysisForm.get('code')?.value?.trim().length < 10) {
      console.error('Código muito curto para análise significativa');
      return;
    }
  }

  clearAnalysis() {
    this.analysisForm.reset({
      analyzeQuality: true,
      analyzeSecurity: true,
      analyzePerformance: true,
      generateSuggestions: true
    });
    this.analysisResult = null;
    this.selectedModel = '';
  }

  downloadReport() {
    if (this.analysisResult) {
      const report = this.generateReport();
      const blob = new Blob([report], { type: 'text/plain' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `code-analysis-${this.analysisResult.analysisId}.txt`;
      link.click();
      window.URL.revokeObjectURL(url);
    }
  }

  shareAnalysis() {
    if (this.analysisResult && navigator.share) {
      navigator.share({
        title: 'Code Analysis Results',
        text: `Code analysis completed with score: ${this.analysisResult.score}`,
        url: window.location.href
      });
    }
  }

  private generateReport(): string {
    if (!this.analysisResult) return '';
    
    return `Code Analysis Report
Generated: ${this.analysisResult.timestamp.toLocaleString()}
Model: ${this.analysisResult.model}
Quality Score: ${this.analysisResult.score}/100

ISSUES FOUND:
${this.analysisResult.issues.map(issue => 
  `- ${issue.type.toUpperCase()}: ${issue.message} ${issue.line ? `(Line ${issue.line})` : ''}`
).join('\n')}

SUGGESTIONS:
${this.analysisResult.suggestions.map((suggestion, i) => 
  `${i + 1}. ${suggestion}`
).join('\n')}

REVIEW:
${this.analysisResult.review}
`;
  }

  getScoreClass(score: number): string {
    if (score >= 90) return 'excellent';
    if (score >= 80) return 'good';
    if (score >= 70) return 'average';
    return 'poor';
  }

  getIssueIcon(type: string): string {
    switch (type) {
      case 'critical': return 'error';
      case 'warning': return 'warning';
      case 'info': return 'info';
      default: return 'help';
    }
  }

  formatTime(date: Date): string {
    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  ngAfterViewInit(): void {
    // Initialize video background
    setTimeout(() => {
      this.initializeBackgroundVideo();
    }, 100);
  }

  private initializeBackgroundVideo(): void {
    if (!this.backgroundVideo?.nativeElement) {
      return;
    }

    const video = this.backgroundVideo.nativeElement;
    
    // Configure video properties
    video.muted = true;
    video.loop = true;
    video.autoplay = true;
    video.playsInline = true;
    video.controls = false;
    video.preload = 'auto';

    // Set the source
    video.src = '/video.mp4';

    // Load and play
    video.load();
    
    // Try to play after a brief delay
    setTimeout(() => {
      video.play().catch(() => {
        // Fallback for autoplay restrictions
        const playOnInteraction = () => {
          video.play().catch(() => {});
          document.removeEventListener('click', playOnInteraction);
          document.removeEventListener('touchstart', playOnInteraction);
        };
        
        document.addEventListener('click', playOnInteraction, { once: true });
        document.addEventListener('touchstart', playOnInteraction, { once: true });
      });
    }, 500);
  }
}