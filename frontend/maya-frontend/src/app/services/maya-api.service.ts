import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DashboardData {
  totalReviews: number;
  activeReviews: number;
  completedToday: number;
  averageScore: number;
  connectionLeaksDetected: number;
  evertecStandardsCompliance: number;
  lastUpdate: string;
  services: {
    database: string;
    tfs: string;
    evertecAI: string;
  };
}

export interface AnalysisSummary {
  totalFiles: number;
  filesAnalyzed: number;
  issuesFound: number;
  criticalIssues: number;
  connectionLeaks: number;
  evertecPatternViolations: number;
  topIssues: { [key: string]: number };
  generatedAt: string;
}

export interface CodeReview {
  id: string;
  projectName: string;
  branch: string;
  status: string;
  score: number;
  issuesCount: number;
  createdAt: string;
  completedAt?: string;
  author: string;
}

export interface FileAnalysis {
  fileName: string;
  filePath: string;
  language: string;
  linesOfCode: number;
  complexity: number;
  issues: Issue[];
  score: number;
}

export interface Issue {
  id: string;
  type: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  message: string;
  lineNumber: number;
  rule: string;
  suggestion?: string;
}

export interface LlmModel {
  id: string;
  name: string;
  family: string;
  description: string;
}

export interface CodeAnalysisRequest {
  code: string;
  fileName: string;
  model: string;
}

export interface CodeReviewRequest extends CodeAnalysisRequest {
  criteria?: string;
}

export interface LlmResponse {
  status: string;
  message?: string;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class MayaApiService {
  private readonly baseUrl = 'http://localhost:8080/api';
  private readonly httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    })
  };

  // Estado reativo para dashboard
  private dashboardSubject = new BehaviorSubject<DashboardData | null>(null);
  public dashboard$ = this.dashboardSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadDashboard();
  }

  /**
   * Carrega dados do dashboard
   */
  loadDashboard(): void {
    this.getDashboard().subscribe({
      next: (data) => this.dashboardSubject.next(data),
      error: (error) => console.error('Erro ao carregar dashboard:', error)
    });
  }

  /**
   * Verifica saúde do sistema
   */
  getHealth(): Observable<any> {
    return this.http.get(`${this.baseUrl}/health`);
  }

  /**
   * Obtém dados do dashboard
   */
  getDashboard(): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.baseUrl}/dashboard`);
  }

  /**
   * Obtém resumo de análises
   */
  getAnalysisSummary(): Observable<AnalysisSummary> {
    return this.http.get<AnalysisSummary>(`${this.baseUrl}/analysis/summary`);
  }

  /**
   * Lista code reviews
   */
  getCodeReviews(page: number = 0, size: number = 10): Observable<CodeReview[]> {
    return this.http.get<CodeReview[]>(`${this.baseUrl}/reviews?page=${page}&size=${size}`);
  }

  /**
   * Obtém detalhes de um code review específico
   */
  getCodeReview(id: string): Observable<CodeReview> {
    return this.http.get<CodeReview>(`${this.baseUrl}/reviews/${id}`);
  }

  /**
   * Obtém análises de arquivos de um review
   */
  getFileAnalyses(reviewId: string): Observable<FileAnalysis[]> {
    return this.http.get<FileAnalysis[]>(`${this.baseUrl}/reviews/${reviewId}/files`);
  }

  /**
   * Inicia nova análise de code review
   */
  startCodeReview(projectName: string, branch: string): Observable<CodeReview> {
    const payload = { projectName, branch };
    return this.http.post<CodeReview>(`${this.baseUrl}/reviews`, payload, this.httpOptions);
  }

  /**
   * Re-executa análise de um review
   */
  rerunAnalysis(reviewId: string): Observable<CodeReview> {
    return this.http.post<CodeReview>(`${this.baseUrl}/reviews/${reviewId}/rerun`, {}, this.httpOptions);
  }

  /**
   * Obtém configurações do sistema
   */
  getConfigurations(): Observable<any> {
    return this.http.get(`${this.baseUrl}/configurations`);
  }

  /**
   * Atualiza configuração
   */
  updateConfiguration(key: string, value: any): Observable<any> {
    const payload = { key, value };
    return this.http.put(`${this.baseUrl}/configurations`, payload, this.httpOptions);
  }

  /**
   * Integração com TFS - lista projetos
   */
  getTfsProjects(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/tfs/projects`);
  }

  /**
   * Integração com TFS - lista branches de um projeto
   */
  getTfsBranches(projectId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/tfs/projects/${projectId}/branches`);
  }

  /**
   * Exporta relatório de análise
   */
  exportReport(reviewId: string, format: 'pdf' | 'excel' = 'pdf'): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/reports/${reviewId}?format=${format}`, {
      responseType: 'blob'
    });
  }

  // ===================================================================
  // MÉTODOS LLM (EverAI Integration)
  // ===================================================================

  /**
   * Obtém modelos LLM disponíveis
   */
  getLlmModels(): Observable<LlmResponse & { models: string[] }> {
    return this.http.get<LlmResponse & { models: string[] }>(`${this.baseUrl}/llm/models`);
  }

  /**
   * Analisa código usando LLM
   */
  analyzeCodeWithLlm(request: CodeAnalysisRequest): Observable<LlmResponse & { analysis: string }> {
    return this.http.post<LlmResponse & { analysis: string }>(`${this.baseUrl}/llm/analyze`, request, this.httpOptions);
  }

  /**
   * Gera sugestões de código usando LLM
   */
  generateCodeSuggestions(request: CodeAnalysisRequest): Observable<LlmResponse & { suggestions: string }> {
    return this.http.post<LlmResponse & { suggestions: string }>(`${this.baseUrl}/llm/suggestions`, request, this.httpOptions);
  }

  /**
   * Realiza code review usando LLM
   */
  performLlmCodeReview(request: CodeReviewRequest): Observable<LlmResponse & { review: string }> {
    return this.http.post<LlmResponse & { review: string }>(`${this.baseUrl}/llm/review`, request, this.httpOptions);
  }
}
