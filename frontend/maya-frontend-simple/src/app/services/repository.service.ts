import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Repository {
  id: number;
  name: string;
  url: string;
  type: 'GITHUB' | 'TFS' | 'AZURE_DEVOPS';
  organizationName: string;
  projectName: string;
  defaultBranch: string;
  isActive: boolean;
  autoReviewEnabled: boolean;
  createdAt: string;
  lastSyncAt?: string;
}

export interface ConnectRepositoryRequest {
  name: string;
  url: string;
  type: 'GITHUB' | 'TFS' | 'AZURE_DEVOPS';
  organizationName: string;
  projectName: string;
  accessToken: string;
  defaultBranch: string;
  autoReviewEnabled: boolean;
}

export interface ConnectRepositoryResponse {
  success: boolean;
  message: string;
  repository?: Repository;
}

export interface DiscoverRepositoriesRequest {
  type: 'GITHUB' | 'TFS' | 'AZURE_DEVOPS';
  organization: string;
  accessToken: string;
}

export interface GitHubRepository {
  id: number;
  name: string;
  fullName: string;
  description: string;
  htmlUrl: string;
  cloneUrl: string;
  defaultBranch: string;
  isPrivate: boolean;
  language: string;
}

@Injectable({
  providedIn: 'root'
})
export class RepositoryService {
  private apiUrl = `${environment.apiUrl}/repositories`;

  constructor(private http: HttpClient) {}

  /**
   * Listar repositórios conectados
   */
  getRepositories(
    page: number = 0, 
    size: number = 20, 
    type?: string, 
    organization?: string, 
    active?: boolean
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (type) params = params.set('type', type);
    if (organization) params = params.set('organization', organization);
    if (active !== undefined) params = params.set('active', active.toString());

    return this.http.get(`${this.apiUrl}`, { params });
  }

  /**
   * Obter repositório por ID
   */
  getRepository(id: number): Observable<Repository> {
    return this.http.get<Repository>(`${this.apiUrl}/${id}`);
  }

  /**
   * Conectar novo repositório
   */
  connectRepository(request: ConnectRepositoryRequest): Observable<ConnectRepositoryResponse> {
    return this.http.post<ConnectRepositoryResponse>(`${this.apiUrl}`, request);
  }

  /**
   * Atualizar repositório
   */
  updateRepository(id: number, updates: Partial<Repository>): Observable<Repository> {
    return this.http.put<Repository>(`${this.apiUrl}/${id}`, updates);
  }

  /**
   * Desconectar repositório
   */
  disconnectRepository(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  /**
   * Testar conexão com repositório
   */
  testConnection(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/test-connection`, {});
  }

  /**
   * Descobrir repositórios disponíveis
   */
  discoverRepositories(request: DiscoverRepositoriesRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/discover`, request);
  }

  /**
   * Sincronizar repositório
   */
  syncRepository(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/sync`, {});
  }
}
