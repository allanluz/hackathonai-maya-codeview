import { Injectable } from '@angular/core';
import { Observable, of, delay, BehaviorSubject } from 'rxjs';
import { 
  CodeReview, 
  ReviewStatus, 
  DashboardStats, 
  ChartData, 
  TfsConnectionConfig, 
  CommitAnalysisRequest,
  IssueType,
  IssueSeverity 
} from '../models/code-review.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private reviewsSubject = new BehaviorSubject<CodeReview[]>([]);
  public reviews$ = this.reviewsSubject.asObservable();

  private statsSubject = new BehaviorSubject<DashboardStats>(this.getInitialStats());
  public stats$ = this.statsSubject.asObservable();

  constructor() {
    // Carregar dados mock iniciais
    this.loadMockData();
  }

  private loadMockData() {
    const mockReviews = this.generateMockReviews();
    this.reviewsSubject.next(mockReviews);
    this.updateStats(mockReviews);
  }

  private generateMockReviews(): CodeReview[] {
    const mockReviews: CodeReview[] = [
      {
        id: '1',
        commitSha: '074f45f6c9b88c4d9ac7d2c518b4e8dbc9523127',
        repositoryName: 'DriveAMnet',
        projectName: 'DriveAMnet',
        author: 'Allan Luz',
        authorEmail: 'allan.luz@sinqia.com.br',
        commitMessage: 'feat: Implementação do módulo de autenticação JWT',
        commitDate: new Date('2025-09-10T14:30:00'),
        status: ReviewStatus.COMPLETED,
        analysisScore: 92,
        criticalIssues: 0,
        warningIssues: 2,
        infoIssues: 5,
        linesAdded: 156,
        linesRemoved: 23,
        filesChanged: 8,
        createdAt: new Date('2025-09-10T14:35:00'),
        updatedAt: new Date('2025-09-10T14:45:00')
      },
      {
        id: '2',
        commitSha: 'a8f2e1d3b9c7e5f4a6b8c9d0e1f2a3b4c5d6e7f8',
        repositoryName: 'DriveAMnet',
        projectName: 'DriveAMnet',
        author: 'Maria Silva',
        authorEmail: 'maria.silva@sinqia.com.br',
        commitMessage: 'fix: Correção na validação de formulários',
        commitDate: new Date('2025-09-11T09:15:00'),
        status: ReviewStatus.IN_PROGRESS,
        analysisScore: 0,
        criticalIssues: 1,
        warningIssues: 4,
        infoIssues: 8,
        linesAdded: 89,
        linesRemoved: 45,
        filesChanged: 12,
        createdAt: new Date('2025-09-11T09:20:00'),
        updatedAt: new Date('2025-09-11T10:30:00')
      },
      {
        id: '3',
        commitSha: 'b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6',
        repositoryName: 'DriveAMnet',
        projectName: 'DriveAMnet',
        author: 'João Santos',
        authorEmail: 'joao.santos@sinqia.com.br',
        commitMessage: 'refactor: Reestruturação do módulo de relatórios',
        commitDate: new Date('2025-09-12T11:45:00'),
        status: ReviewStatus.PENDING,
        analysisScore: 0,
        criticalIssues: 0,
        warningIssues: 0,
        infoIssues: 0,
        linesAdded: 234,
        linesRemoved: 187,
        filesChanged: 15,
        createdAt: new Date('2025-09-12T11:50:00'),
        updatedAt: new Date('2025-09-12T11:50:00')
      },
      {
        id: '4',
        commitSha: 'c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5',
        repositoryName: 'DriveAMnet',
        projectName: 'DriveAMnet',
        author: 'Ana Costa',
        authorEmail: 'ana.costa@sinqia.com.br',
        commitMessage: 'feat: Nova funcionalidade de dashboard avançado',
        commitDate: new Date('2025-09-09T16:20:00'),
        status: ReviewStatus.COMPLETED,
        analysisScore: 88,
        criticalIssues: 1,
        warningIssues: 3,
        infoIssues: 12,
        linesAdded: 312,
        linesRemoved: 56,
        filesChanged: 18,
        createdAt: new Date('2025-09-09T16:25:00'),
        updatedAt: new Date('2025-09-09T17:15:00')
      },
      {
        id: '5',
        commitSha: 'd5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4',
        repositoryName: 'DriveAMnet',
        projectName: 'DriveAMnet',
        author: 'Carlos Oliveira',
        authorEmail: 'carlos.oliveira@sinqia.com.br',
        commitMessage: 'chore: Atualização de dependências e configurações',
        commitDate: new Date('2025-09-08T13:10:00'),
        status: ReviewStatus.FAILED,
        analysisScore: 0,
        criticalIssues: 3,
        warningIssues: 7,
        infoIssues: 2,
        linesAdded: 67,
        linesRemoved: 34,
        filesChanged: 9,
        createdAt: new Date('2025-09-08T13:15:00'),
        updatedAt: new Date('2025-09-08T13:25:00')
      }
    ];

    return mockReviews;
  }

  private getInitialStats(): DashboardStats {
    return {
      totalReviews: 0,
      pendingReviews: 0,
      completedReviews: 0,
      failedReviews: 0,
      criticalIssues: 0,
      averageScore: 0,
      reviewsThisWeek: 0,
      reviewsThisMonth: 0
    };
  }

  private updateStats(reviews: CodeReview[]) {
    const stats: DashboardStats = {
      totalReviews: reviews.length,
      pendingReviews: reviews.filter(r => r.status === ReviewStatus.PENDING).length,
      completedReviews: reviews.filter(r => r.status === ReviewStatus.COMPLETED).length,
      failedReviews: reviews.filter(r => r.status === ReviewStatus.FAILED).length,
      criticalIssues: reviews.reduce((sum, r) => sum + r.criticalIssues, 0),
      averageScore: this.calculateAverageScore(reviews),
      reviewsThisWeek: this.getReviewsInPeriod(reviews, 7),
      reviewsThisMonth: this.getReviewsInPeriod(reviews, 30)
    };

    this.statsSubject.next(stats);
  }

  private calculateAverageScore(reviews: CodeReview[]): number {
    const completedReviews = reviews.filter(r => r.status === ReviewStatus.COMPLETED);
    if (completedReviews.length === 0) return 0;
    
    const sum = completedReviews.reduce((total, review) => total + review.analysisScore, 0);
    return Math.round(sum / completedReviews.length);
  }

  private getReviewsInPeriod(reviews: CodeReview[], days: number): number {
    const now = new Date();
    const cutoff = new Date(now.getTime() - (days * 24 * 60 * 60 * 1000));
    return reviews.filter(r => r.createdAt >= cutoff).length;
  }

  // Métodos públicos
  getReviews(): Observable<CodeReview[]> {
    return this.reviews$;
  }

  getStats(): Observable<DashboardStats> {
    return this.stats$;
  }

  getReviewById(id: string): Observable<CodeReview | undefined> {
    const review = this.reviewsSubject.value.find(r => r.id === id);
    return of(review).pipe(delay(300));
  }

  getChartData(): Observable<ChartData> {
    const reviews = this.reviewsSubject.value;
    
    // Dados para gráfico de reviews por status
    const statusCounts = {
      [ReviewStatus.COMPLETED]: reviews.filter(r => r.status === ReviewStatus.COMPLETED).length,
      [ReviewStatus.PENDING]: reviews.filter(r => r.status === ReviewStatus.PENDING).length,
      [ReviewStatus.IN_PROGRESS]: reviews.filter(r => r.status === ReviewStatus.IN_PROGRESS).length,
      [ReviewStatus.FAILED]: reviews.filter(r => r.status === ReviewStatus.FAILED).length
    };

    const chartData: ChartData = {
      labels: ['Completed', 'Pending', 'In Progress', 'Failed'],
      datasets: [{
        label: 'Reviews by Status',
        data: [
          statusCounts[ReviewStatus.COMPLETED],
          statusCounts[ReviewStatus.PENDING],
          statusCounts[ReviewStatus.IN_PROGRESS],
          statusCounts[ReviewStatus.FAILED]
        ],
        backgroundColor: [
          '#22c55e', // green - completed
          '#3b82f6', // blue - pending
          '#f59e0b', // yellow - in progress
          '#ef4444'  // red - failed
        ],
        borderWidth: 2
      }]
    };

    return of(chartData).pipe(delay(500));
  }

  // Simulação de conexão TFS
  testTfsConnection(config: TfsConnectionConfig): Observable<boolean> {
    console.log('Testing TFS connection:', config);
    // Simular delay de rede
    return of(true).pipe(delay(2000));
  }

  connectToTfs(config: TfsConnectionConfig): Observable<boolean> {
    console.log('Connecting to TFS:', config);
    // Simular delay de rede
    return of(true).pipe(delay(3000));
  }

  // Simulação de análise de commit
  analyzeCommit(request: CommitAnalysisRequest): Observable<CodeReview> {
    console.log('Analyzing commit:', request);
    
    // Criar um novo review baseado na solicitação
    const newReview: CodeReview = {
      id: Date.now().toString(),
      commitSha: request.commitSha,
      repositoryName: request.repositoryName,
      projectName: request.projectName,
      author: 'Current User',
      authorEmail: 'user@sinqia.com.br',
      commitMessage: 'Imported commit for analysis',
      commitDate: new Date(),
      status: ReviewStatus.IN_PROGRESS,
      analysisScore: 0,
      criticalIssues: 0,
      warningIssues: 0,
      infoIssues: 0,
      linesAdded: Math.floor(Math.random() * 200) + 50,
      linesRemoved: Math.floor(Math.random() * 100) + 10,
      filesChanged: Math.floor(Math.random() * 15) + 5,
      createdAt: new Date(),
      updatedAt: new Date()
    };

    // Adicionar o novo review à lista
    const currentReviews = this.reviewsSubject.value;
    const updatedReviews = [newReview, ...currentReviews];
    this.reviewsSubject.next(updatedReviews);
    this.updateStats(updatedReviews);

    // Simular análise em andamento
    setTimeout(() => {
      this.completeAnalysis(newReview.id);
    }, 5000);

    return of(newReview).pipe(delay(1000));
  }

  private completeAnalysis(reviewId: string) {
    const currentReviews = this.reviewsSubject.value;
    const reviewIndex = currentReviews.findIndex(r => r.id === reviewId);
    
    if (reviewIndex !== -1) {
      const updatedReview = {
        ...currentReviews[reviewIndex],
        status: ReviewStatus.COMPLETED,
        analysisScore: Math.floor(Math.random() * 30) + 70, // Score entre 70-100
        criticalIssues: Math.floor(Math.random() * 3),
        warningIssues: Math.floor(Math.random() * 8) + 2,
        infoIssues: Math.floor(Math.random() * 15) + 5,
        updatedAt: new Date()
      };

      const updatedReviews = [...currentReviews];
      updatedReviews[reviewIndex] = updatedReview;
      
      this.reviewsSubject.next(updatedReviews);
      this.updateStats(updatedReviews);
    }
  }

  refreshData(): Observable<CodeReview[]> {
    // Simular refresh dos dados
    this.loadMockData();
    return this.reviews$.pipe(delay(1000));
  }

  deleteReview(id: string): Observable<boolean> {
    const currentReviews = this.reviewsSubject.value;
    const updatedReviews = currentReviews.filter(r => r.id !== id);
    this.reviewsSubject.next(updatedReviews);
    this.updateStats(updatedReviews);
    return of(true).pipe(delay(500));
  }
}