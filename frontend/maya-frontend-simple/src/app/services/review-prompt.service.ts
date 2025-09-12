import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ReviewPrompt {
  id: number;
  name: string;
  description: string;
  promptTemplate: string;
  systemInstructions: string;
  type: 'GENERAL' | 'JAVA_SPECIFIC' | 'JAVASCRIPT_SPECIFIC' | 'SECURITY_FOCUSED' | 'PERFORMANCE_FOCUSED' | 'SINQIA_STANDARDS' | 'CONNECTION_LEAKS' | 'CUSTOM';
  isActive: boolean;
  isDefault: boolean;
  projectPattern?: string;
  fileExtensions: string;
  focusAreas?: string;
  severityLevels: string;
  maxTokens: number;
  temperature: number;
  createdAt: string;
  usageCount: number;
}

export interface CreatePromptRequest {
  name: string;
  description: string;
  promptTemplate: string;
  systemInstructions?: string;
  type: ReviewPrompt['type'];
  repositoryId?: number;
  projectPattern?: string;
  fileExtensions?: string;
  focusAreas?: string[];
  severityLevels?: string[];
  maxTokens?: number;
  temperature?: number;
}

export interface AuxiliaryFile {
  id: number;
  name: string;
  fileType: 'DOCUMENTATION' | 'CODING_STANDARDS' | 'CONFIGURATION' | 'TEMPLATE' | 'REFERENCE' | 'OTHER';
  description: string;
  fileSize: number;
  isActive: boolean;
  createdAt: string;
  contentPreview?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReviewPromptService {
  private apiUrl = `${environment.apiUrl}/review-prompts`;

  constructor(private http: HttpClient) {}

  /**
   * Listar prompts de revisão
   */
  getPrompts(
    page: number = 0,
    size: number = 20,
    type?: string,
    repositoryId?: number,
    active?: boolean
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (type) params = params.set('type', type);
    if (repositoryId) params = params.set('repositoryId', repositoryId.toString());
    if (active !== undefined) params = params.set('active', active.toString());

    return this.http.get(`${this.apiUrl}`, { params });
  }

  /**
   * Obter prompt por ID
   */
  getPrompt(id: number): Observable<ReviewPrompt> {
    return this.http.get<ReviewPrompt>(`${this.apiUrl}/${id}`);
  }

  /**
   * Criar novo prompt
   */
  createPrompt(request: CreatePromptRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}`, request);
  }

  /**
   * Atualizar prompt
   */
  updatePrompt(id: number, updates: Partial<CreatePromptRequest>): Observable<ReviewPrompt> {
    return this.http.put<ReviewPrompt>(`${this.apiUrl}/${id}`, updates);
  }

  /**
   * Deletar prompt
   */
  deletePrompt(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  /**
   * Definir como prompt padrão
   */
  setDefault(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/set-default`, {});
  }

  /**
   * Testar prompt
   */
  testPrompt(id: number, sampleCode: string, filename: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/test`, {
      sampleCode,
      filename
    });
  }

  /**
   * Clonar prompt
   */
  clonePrompt(id: number, newName: string, description?: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/clone`, {
      newName,
      description
    });
  }

  /**
   * Obter prompts aplicáveis para repositório
   */
  getApplicablePrompts(repositoryId: number): Observable<ReviewPrompt[]> {
    return this.http.get<ReviewPrompt[]>(`${this.apiUrl}/repository/${repositoryId}/applicable`);
  }

  /**
   * Upload de arquivo auxiliar
   */
  uploadAuxiliaryFile(
    promptId: number, 
    file: File, 
    fileType: AuxiliaryFile['fileType'], 
    description?: string
  ): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileType', fileType);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post(`${this.apiUrl}/${promptId}/auxiliary-files`, formData);
  }

  /**
   * Listar arquivos auxiliares
   */
  getAuxiliaryFiles(promptId: number): Observable<AuxiliaryFile[]> {
    return this.http.get<AuxiliaryFile[]>(`${this.apiUrl}/${promptId}/auxiliary-files`);
  }

  /**
   * Obter prompts mais utilizados
   */
  getMostUsed(size: number = 10): Observable<any> {
    const params = new HttpParams().set('size', size.toString());
    return this.http.get(`${this.apiUrl}/most-used`, { params });
  }
}
