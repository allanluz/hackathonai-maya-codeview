import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ReviewExport {
  id: number;
  exportFormat: 'PDF' | 'MARKDOWN' | 'HTML' | 'JSON' | 'EXCEL' | 'CSV';
  fileName: string;
  filePath: string;
  fileSize: number;
  formattedFileSize: string;
  downloadCount: number;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'EXPIRED';
  errorMessage?: string;
  createdAt: string;
  exportedAt?: string;
  expiresAt?: string;
}

export interface ExportReviewRequest {
  format: ReviewExport['exportFormat'];
  includeSourceCode: boolean;
  includeAnalysisDetails: boolean;
  includeCharts: boolean;
  includeRecommendations: boolean;
  customOptions?: { [key: string]: any };
}

export interface UploadToRepositoryRequest {
  targetBranch: string;
  commitMessage: string;
  createPullRequest: boolean;
  reviewers: string[];
  options?: { [key: string]: any };
}

export interface ShareReviewRequest {
  expiresIn: number; // dias
  requiresPassword: boolean;
  password?: string;
  allowedEmails?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ReviewExportService {
  private apiUrl = `${environment.apiUrl}/reviews`;

  constructor(private http: HttpClient) {}

  /**
   * Exportar review
   */
  exportReview(reviewId: number, request: ExportReviewRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/${reviewId}/export`, request);
  }

  /**
   * Download de exportação
   */
  downloadExport(exportId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/exports/${exportId}/download`, {
      responseType: 'blob'
    });
  }

  /**
   * Obter status de exportação
   */
  getExportStatus(exportId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/exports/${exportId}/status`);
  }

  /**
   * Listar exportações de um review
   */
  getReviewExports(reviewId: number): Observable<ReviewExport[]> {
    return this.http.get<ReviewExport[]>(`${this.apiUrl}/${reviewId}/exports`);
  }

  /**
   * Listar exportações do usuário
   */
  getUserExports(page: number = 0, size: number = 20, status?: string): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get(`${this.apiUrl}/exports`, { params });
  }

  /**
   * Deletar exportação
   */
  deleteExport(exportId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/exports/${exportId}`);
  }

  /**
   * Upload para repositório
   */
  uploadToRepository(reviewId: number, request: UploadToRepositoryRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/${reviewId}/upload-to-repository`, request);
  }

  /**
   * Compartilhar review
   */
  shareReview(reviewId: number, request: ShareReviewRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/${reviewId}/share`, request);
  }

  /**
   * Preview de exportação
   */
  previewExport(exportId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/exports/${exportId}/preview`);
  }

  /**
   * Exportação em lote
   */
  batchExport(reviewIds: number[], format: ReviewExport['exportFormat'], options?: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/batch-export`, {
      reviewIds,
      format,
      zipFileName: `maya-reviews-${Date.now()}.zip`,
      options: options || {}
    });
  }

  /**
   * Download de arquivo (helper para trigger do download)
   */
  downloadFile(exportId: number, fileName: string): void {
    this.downloadExport(exportId).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    });
  }
}
