import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DashboardOverview {
  totalReviews: number;
  activeRepositories: number;
  averageQualityScore: number;
  criticalIssues: number;
  connectionLeaks: number;
  completionRate: number;
  lastUpdate: string;
  quickStats: QuickStat[];
}

export interface QuickStat {
  name: string;
  value: any;
  unit: string;
  trend: string;
  changePercent: number;
}

export interface QualityMetrics {
  averageScore: number;
  medianScore: number;
  scoreDistribution: number;
  totalFiles: number;
  analyzedFiles: number;
  issuesByType: { [key: string]: number };
  scoresByRepository: { [key: string]: number };
  trends: QualityTrend[];
}

export interface QualityTrend {
  date: string;
  score: number;
  issues: number;
  reviews: number;
}

export interface RepositoryRanking {
  repositoryId?: number;
  repositoryName: string;
  averageScore: number;
  totalReviews: number;
  criticalIssues: number;
  trend: number;
  status: string;
}

export interface DeveloperRanking {
  developer: string;
  averageScore: number;
  totalReviews: number;
  criticalIssues: number;
  improvement: number;
  level: string;
}

export interface ChartData {
  chartType: string;
  labels: string[];
  datasets: DataSeries[];
  options: { [key: string]: any };
}

export interface DataSeries {
  label: string;
  data: any[];
  backgroundColor: string;
  borderColor: string;
  styling: { [key: string]: any };
}

export interface SystemAlert {
  id: string;
  type: string;
  severity: string;
  message: string;
  details: string;
  timestamp: string;
  resolved: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  /**
   * Obter overview geral
   */
  getOverview(days: number = 30): Observable<DashboardOverview> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<DashboardOverview>(`${this.apiUrl}/overview`, { params });
  }

  /**
   * Obter métricas de qualidade
   */
  getQualityMetrics(days: number = 30, repositoryId?: number): Observable<QualityMetrics> {
    let params = new HttpParams().set('days', days.toString());
    if (repositoryId) {
      params = params.set('repositoryId', repositoryId.toString());
    }
    return this.http.get<QualityMetrics>(`${this.apiUrl}/quality-metrics`, { params });
  }

  /**
   * Obter análise de tendências
   */
  getTrends(days: number = 30, period: string = 'DAILY'): Observable<any> {
    const params = new HttpParams()
      .set('days', days.toString())
      .set('period', period);
    return this.http.get(`${this.apiUrl}/trends`, { params });
  }

  /**
   * Obter ranking de repositórios
   */
  getRepositoryRanking(days: number = 30, limit: number = 10): Observable<RepositoryRanking[]> {
    const params = new HttpParams()
      .set('days', days.toString())
      .set('limit', limit.toString());
    return this.http.get<RepositoryRanking[]>(`${this.apiUrl}/repository-ranking`, { params });
  }

  /**
   * Obter ranking de desenvolvedores
   */
  getDeveloperRanking(days: number = 30, limit: number = 10): Observable<DeveloperRanking[]> {
    const params = new HttpParams()
      .set('days', days.toString())
      .set('limit', limit.toString());
    return this.http.get<DeveloperRanking[]>(`${this.apiUrl}/developer-ranking`, { params });
  }

  /**
   * Obter estatísticas de problemas
   */
  getIssueStatistics(days: number = 30, repositoryId?: number): Observable<any> {
    let params = new HttpParams().set('days', days.toString());
    if (repositoryId) {
      params = params.set('repositoryId', repositoryId.toString());
    }
    return this.http.get(`${this.apiUrl}/issue-statistics`, { params });
  }

  /**
   * Obter métricas de performance
   */
  getPerformanceMetrics(days: number = 7): Observable<any> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get(`${this.apiUrl}/performance-metrics`, { params });
  }

  /**
   * Obter alertas ativos
   */
  getActiveAlerts(): Observable<SystemAlert[]> {
    return this.http.get<SystemAlert[]>(`${this.apiUrl}/alerts`);
  }

  /**
   * Obter dados para gráficos
   */
  getChartData(chartType: string, days: number = 30, repositoryId?: number): Observable<ChartData> {
    let params = new HttpParams().set('days', days.toString());
    if (repositoryId) {
      params = params.set('repositoryId', repositoryId.toString());
    }
    return this.http.get<ChartData>(`${this.apiUrl}/charts/${chartType}`, { params });
  }

  /**
   * Exportar dados do dashboard
   */
  exportDashboard(format: string, sections: string[], days: number = 30, options?: any): Observable<any> {
    const request = {
      format,
      sections,
      days,
      options: options || {}
    };
    return this.http.post(`${this.apiUrl}/export`, request);
  }
}
