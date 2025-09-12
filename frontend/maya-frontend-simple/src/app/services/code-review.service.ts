import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CodeReview {
  id: string;
  fileName: string;
  filePath: string;
  fileContent: string;
  repositoryId: string;
  repositoryName?: string;
  branch: string;
  commitHash: string;
  developer: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  reviewPromptId?: string;
  aiResponse?: string;
  qualityScore?: number;
  issuesFound?: number;
  suggestions: string[];
  securityIssues: string[];
  performanceIssues: string[];
  createdAt: Date;
  completedAt?: Date;
  errorMessage?: string;
}

export interface CodeReviewFilters {
  repositoryId?: string;
  status?: string;
  startDate?: Date;
  endDate?: Date;
  search?: string;
}

export interface CreateReviewRequest {
  fileName: string;
  filePath: string;
  fileContent: string;
  repositoryId: string;
  branch: string;
  commitHash: string;
  developer: string;
  reviewPromptId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CodeReviewService {
  private readonly apiUrl = 'http://localhost:8081/api/code-reviews';

  constructor(private http: HttpClient) {}

  getCodeReviews(filters?: CodeReviewFilters): Observable<CodeReview[]> {
    let params = new HttpParams();
    
    if (filters) {
      if (filters.repositoryId) {
        params = params.set('repositoryId', filters.repositoryId);
      }
      if (filters.status) {
        params = params.set('status', filters.status);
      }
      if (filters.startDate) {
        params = params.set('startDate', filters.startDate.toISOString());
      }
      if (filters.endDate) {
        params = params.set('endDate', filters.endDate.toISOString());
      }
      if (filters.search) {
        params = params.set('search', filters.search);
      }
    }

    return this.http.get<CodeReview[]>(this.apiUrl, { params });
  }

  getCodeReview(id: string): Observable<CodeReview> {
    return this.http.get<CodeReview>(`${this.apiUrl}/${id}`);
  }

  createCodeReview(review: CreateReviewRequest): Observable<CodeReview> {
    return this.http.post<CodeReview>(this.apiUrl, review);
  }

  updateCodeReview(id: string, review: Partial<CodeReview>): Observable<CodeReview> {
    return this.http.put<CodeReview>(`${this.apiUrl}/${id}`, review);
  }

  deleteCodeReview(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  retryReview(id: string): Observable<CodeReview> {
    return this.http.post<CodeReview>(`${this.apiUrl}/${id}/retry`, {});
  }

  downloadReview(id: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/download`, {
      responseType: 'blob'
    });
  }

  exportReviews(format: 'CSV' | 'PDF' | 'JSON', filters?: CodeReviewFilters): Observable<Blob> {
    let params = new HttpParams().set('format', format);
    
    if (filters) {
      if (filters.repositoryId) {
        params = params.set('repositoryId', filters.repositoryId);
      }
      if (filters.status) {
        params = params.set('status', filters.status);
      }
      if (filters.startDate) {
        params = params.set('startDate', filters.startDate.toISOString());
      }
      if (filters.endDate) {
        params = params.set('endDate', filters.endDate.toISOString());
      }
      if (filters.search) {
        params = params.set('search', filters.search);
      }
    }

    return this.http.get(`${this.apiUrl}/export`, {
      params,
      responseType: 'blob'
    });
  }

  getReviewsByRepository(repositoryId: string): Observable<CodeReview[]> {
    return this.http.get<CodeReview[]>(`${this.apiUrl}/repository/${repositoryId}`);
  }

  getReviewsByDeveloper(developer: string): Observable<CodeReview[]> {
    return this.http.get<CodeReview[]>(`${this.apiUrl}/developer/${developer}`);
  }

  getReviewStatistics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/statistics`);
  }
}
