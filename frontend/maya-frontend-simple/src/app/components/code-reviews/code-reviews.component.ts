import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { CodeReviewService, CodeReview } from '../../services/code-review.service';

@Component({
  selector: 'app-code-reviews',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  template: `
    <div class="reviews-container">
      <div class="header">
        <h1>Code Reviews</h1>
        <button mat-raised-button color="primary" (click)="exportReviews()">
          <mat-icon>download</mat-icon>
          Exportar Reviews
        </button>
      </div>

      <!-- Filters -->
      <mat-card class="filters-card">
        <mat-card-header>
          <mat-card-title>Filtros</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="filters-grid">
            <mat-form-field>
              <mat-label>Repositório</mat-label>
              <mat-select [(value)]="filters.repositoryId" (selectionChange)="applyFilters()">
                <mat-option value="">Todos</mat-option>
                <mat-option *ngFor="let repo of repositories" [value]="repo.id">
                  {{ repo.name }}
                </mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field>
              <mat-label>Status</mat-label>
              <mat-select [(value)]="filters.status" (selectionChange)="applyFilters()">
                <mat-option value="">Todos</mat-option>
                <mat-option value="PENDING">Pendente</mat-option>
                <mat-option value="IN_PROGRESS">Em Progresso</mat-option>
                <mat-option value="COMPLETED">Concluído</mat-option>
                <mat-option value="FAILED">Falhou</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field>
              <mat-label>Data Início</mat-label>
              <input matInput [matDatepicker]="startPicker" [(ngModel)]="filters.startDate" (dateChange)="applyFilters()">
              <mat-datepicker-toggle matSuffix [for]="startPicker"></mat-datepicker-toggle>
              <mat-datepicker #startPicker></mat-datepicker>
            </mat-form-field>

            <mat-form-field>
              <mat-label>Data Fim</mat-label>
              <input matInput [matDatepicker]="endPicker" [(ngModel)]="filters.endDate" (dateChange)="applyFilters()">
              <mat-datepicker-toggle matSuffix [for]="endPicker"></mat-datepicker-toggle>
              <mat-datepicker #endPicker></mat-datepicker>
            </mat-form-field>

            <mat-form-field>
              <mat-label>Buscar</mat-label>
              <input matInput [(ngModel)]="filters.search" (input)="applyFilters()" placeholder="Arquivo, branch, desenvolvedor...">
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>

            <button mat-button (click)="clearFilters()">
              <mat-icon>clear</mat-icon>
              Limpar Filtros
            </button>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Reviews Table -->
      <mat-card class="table-card">
        <mat-card-content>
          <table mat-table [dataSource]="reviews" class="reviews-table" matSort>
            <!-- File Column -->
            <ng-container matColumnDef="fileName">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Arquivo</th>
              <td mat-cell *matCellDef="let review">
                <div class="file-info">
                  <mat-icon>description</mat-icon>
                  <span>{{ review.fileName }}</span>
                </div>
              </td>
            </ng-container>

            <!-- Repository Column -->
            <ng-container matColumnDef="repository">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Repositório</th>
              <td mat-cell *matCellDef="let review">{{ review.repositoryName }}</td>
            </ng-container>

            <!-- Branch Column -->
            <ng-container matColumnDef="branch">
              <th mat-header-cell *matHeaderCellDef>Branch</th>
              <td mat-cell *matCellDef="let review">{{ review.branch }}</td>
            </ng-container>

            <!-- Developer Column -->
            <ng-container matColumnDef="developer">
              <th mat-header-cell *matHeaderCellDef>Desenvolvedor</th>
              <td mat-cell *matCellDef="let review">{{ review.developer }}</td>
            </ng-container>

            <!-- Status Column -->
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
              <td mat-cell *matCellDef="let review">
                <mat-chip [class]="getStatusClass(review.status)">
                  {{ getStatusLabel(review.status) }}
                </mat-chip>
              </td>
            </ng-container>

            <!-- Quality Score Column -->
            <ng-container matColumnDef="qualityScore">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Score</th>
              <td mat-cell *matCellDef="let review">
                <div class="score-container">
                  <span class="score-value" [class]="getScoreClass(review.qualityScore)">
                    {{ review.qualityScore || 'N/A' }}
                  </span>
                </div>
              </td>
            </ng-container>

            <!-- Issues Column -->
            <ng-container matColumnDef="issuesFound">
              <th mat-header-cell *matHeaderCellDef>Issues</th>
              <td mat-cell *matCellDef="let review">
                <mat-chip [color]="review.issuesFound > 5 ? 'warn' : 'primary'" selected>
                  {{ review.issuesFound || 0 }}
                </mat-chip>
              </td>
            </ng-container>

            <!-- Created Date Column -->
            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Data</th>
              <td mat-cell *matCellDef="let review">{{ review.createdAt | date:'short' }}</td>
            </ng-container>

            <!-- Actions Column -->
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Ações</th>
              <td mat-cell *matCellDef="let review">
                <button mat-icon-button (click)="viewReview(review)" matTooltip="Ver detalhes">
                  <mat-icon>visibility</mat-icon>
                </button>
                <button mat-icon-button (click)="downloadReview(review)" matTooltip="Download">
                  <mat-icon>download</mat-icon>
                </button>
                <button mat-icon-button *ngIf="canRetryReview(review)" (click)="retryReview(review)" matTooltip="Tentar novamente">
                  <mat-icon>refresh</mat-icon>
                </button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;" (click)="viewReview(row)" class="review-row"></tr>
          </table>

          <mat-paginator [pageSizeOptions]="[10, 25, 50, 100]" showFirstLastButtons></mat-paginator>
        </mat-card-content>
      </mat-card>

      <!-- Statistics -->
      <div class="stats-grid">
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-header">
              <mat-icon>analytics</mat-icon>
              <h3>Total de Reviews</h3>
            </div>
            <div class="stat-value">{{ totalReviews }}</div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-header">
              <mat-icon>trending_up</mat-icon>
              <h3>Score Médio</h3>
            </div>
            <div class="stat-value">{{ averageScore | number:'1.1-1' }}</div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-header">
              <mat-icon>bug_report</mat-icon>
              <h3>Issues Encontrados</h3>
            </div>
            <div class="stat-value">{{ totalIssues }}</div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-header">
              <mat-icon>check_circle</mat-icon>
              <h3>Reviews Concluídos</h3>
            </div>
            <div class="stat-value">{{ completedReviews }}</div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .reviews-container {
      padding: var(--space-2xl);
      max-width: 1600px;
      margin: 0 auto;
      background: var(--bg-secondary);
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--space-2xl);
      padding-bottom: var(--space-lg);
      border-bottom: 2px solid var(--color-gray-200);
    }

    .header h1 {
      font-size: 2.25rem;
      font-weight: 800;
      background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      letter-spacing: -0.02em;
    }

    .header button {
      height: 48px;
      border-radius: var(--radius-xl);
      font-weight: 600;
      box-shadow: var(--shadow-md);
      transition: all var(--animation-duration-normal) var(--animation-easing);
    }

    .header button:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-lg);
    }

    .filters-card {
      background: var(--bg-surface);
      border: 1px solid var(--color-gray-200);
      border-radius: var(--radius-2xl);
      box-shadow: var(--shadow-lg);
      margin-bottom: var(--space-2xl);
      overflow: hidden;
    }

    .filters-card mat-card-header {
      background: linear-gradient(135deg, var(--color-gray-50), var(--color-gray-100));
      border-bottom: 1px solid var(--color-gray-200);
    }

    .filters-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: var(--space-lg);
      align-items: end;
      padding: var(--space-xl);
    }

    .filters-grid mat-form-field {
      font-weight: 500;
    }

    .filters-grid button {
      height: 48px;
      border-radius: var(--radius-lg);
      font-weight: 600;
      transition: all var(--animation-duration-normal) var(--animation-easing);
    }

    .table-card {
      background: var(--bg-surface);
      border: 1px solid var(--color-gray-200);
      border-radius: var(--radius-2xl);
      box-shadow: var(--shadow-lg);
      margin-bottom: var(--space-2xl);
      overflow: hidden;
    }

    .reviews-table {
      width: 100%;
      background: var(--bg-surface);
    }

    .reviews-table th {
      background: linear-gradient(135deg, var(--color-gray-50), var(--color-gray-100));
      color: var(--text-primary);
      font-weight: 600;
      border-bottom: 2px solid var(--color-gray-200);
      padding: var(--space-lg);
    }

    .review-row {
      cursor: pointer;
      transition: all var(--animation-duration-normal) var(--animation-easing);
      border-bottom: 1px solid var(--color-gray-200);
    }

    .review-row:hover {
      background: linear-gradient(135deg, var(--color-gray-50), var(--color-gray-100));
      transform: scale(1.01);
      box-shadow: var(--shadow-md);
    }

    .review-row td {
      padding: var(--space-lg);
      vertical-align: middle;
    }

    .file-info {
      display: flex;
      align-items: center;
      gap: var(--space-md);
    }

    .file-info mat-icon {
      color: var(--color-primary);
      background: var(--color-gray-100);
      padding: var(--space-sm);
      border-radius: var(--radius-md);
    }

    .file-info span {
      font-weight: 600;
      color: var(--text-primary);
    }

    .score-container {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .score-value {
      font-weight: 700;
      padding: var(--space-sm) var(--space-md);
      border-radius: var(--radius-lg);
      font-size: 1.125rem;
      text-align: center;
      min-width: 60px;
      box-shadow: var(--shadow-sm);
    }

    .score-excellent {
      background: linear-gradient(135deg, var(--color-success), #059669);
      color: var(--text-inverse);
    }

    .score-good {
      background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
      color: var(--text-inverse);
    }

    .score-fair {
      background: linear-gradient(135deg, var(--color-warning), #d97706);
      color: var(--text-inverse);
    }

    .score-poor {
      background: linear-gradient(135deg, var(--color-error), #dc2626);
      color: var(--text-inverse);
    }

    .status-pending {
      background: linear-gradient(135deg, #fef3c7, var(--color-warning));
      color: #92400e;
    }

    .status-in_progress {
      background: linear-gradient(135deg, #dbeafe, var(--color-primary));
      color: #1e40af;
    }

    .status-completed {
      background: linear-gradient(135deg, #d1fae5, var(--color-success));
      color: #065f46;
    }

    .status-failed {
      background: linear-gradient(135deg, #fee2e2, var(--color-error));
      color: #991b1b;
    }

    mat-chip {
      border-radius: var(--radius-lg) !important;
      font-weight: 600 !important;
      border: 1px solid transparent !important;
      transition: all var(--animation-duration-normal) var(--animation-easing) !important;
    }

    mat-chip:hover {
      transform: scale(1.05) !important;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: var(--space-xl);
    }

    .stat-card {
      background: var(--bg-surface);
      border: 1px solid var(--color-gray-200);
      border-radius: var(--radius-2xl);
      box-shadow: var(--shadow-lg);
      min-height: 160px;
      transition: all var(--animation-duration-normal) var(--animation-easing);
      position: relative;
      overflow: hidden;
    }

    .stat-card::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: linear-gradient(90deg, var(--color-primary), var(--color-secondary));
    }

    .stat-card:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-2xl);
      border-color: var(--color-primary);
    }

    .stat-header {
      display: flex;
      align-items: center;
      gap: var(--space-md);
      margin-bottom: var(--space-md);
      padding: var(--space-xl) var(--space-xl) 0;
    }

    .stat-header mat-icon {
      width: 48px;
      height: 48px;
      font-size: 1.5rem;
      background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
      color: var(--text-inverse);
      border-radius: var(--radius-xl);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: var(--shadow-md);
    }

    .stat-header h3 {
      margin: 0;
      font-size: 1.125rem;
      font-weight: 600;
      color: var(--text-primary);
    }

    .stat-value {
      font-size: 2.5rem;
      font-weight: 900;
      background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      line-height: 1;
      padding: 0 var(--space-xl) var(--space-xl);
    }

    .empty-state {
      text-align: center;
      padding: var(--space-3xl);
      color: var(--text-secondary);
      background: var(--bg-surface);
      border-radius: var(--radius-2xl);
      border: 2px dashed var(--color-gray-300);
    }

    .empty-state mat-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      margin-bottom: var(--space-lg);
      color: var(--color-gray-400);
    }

    .empty-state h3 {
      color: var(--text-primary);
      margin-bottom: var(--space-md);
    }

    .empty-state button {
      margin-top: var(--space-lg);
      height: 48px;
      border-radius: var(--radius-xl);
      font-weight: 600;
    }

    /* Action Buttons */
    button[mat-icon-button] {
      border-radius: var(--radius-lg);
      transition: all var(--animation-duration-normal) var(--animation-easing);
      margin: 0 var(--space-xs);
    }

    button[mat-icon-button]:hover {
      background: var(--color-gray-100);
      transform: scale(1.1);
      box-shadow: var(--shadow-sm);
    }

    /* Enhanced Responsive Design */
    @media (max-width: 1400px) {
      .reviews-container {
        padding: var(--space-xl);
      }
    }

    @media (max-width: 768px) {
      .reviews-container {
        padding: var(--space-lg);
      }

      .header {
        flex-direction: column;
        gap: var(--space-lg);
        align-items: stretch;
      }

      .header h1 {
        font-size: 1.875rem;
        text-align: center;
      }

      .filters-grid {
        grid-template-columns: 1fr;
        gap: var(--space-md);
      }

      .stats-grid {
        grid-template-columns: 1fr;
      }

      .reviews-table {
        font-size: 0.875rem;
      }

      .score-value {
        font-size: 1rem;
        padding: var(--space-xs) var(--space-sm);
      }
    }

    @media (max-width: 480px) {
      .reviews-container {
        padding: var(--space-md);
      }

      .header h1 {
        font-size: 1.5rem;
      }

      .stat-value {
        font-size: 2rem;
      }

      .file-info {
        flex-direction: column;
        align-items: flex-start;
        gap: var(--space-sm);
      }
    }

    /* Animations */
    @keyframes slideInFromLeft {
      from {
        opacity: 0;
        transform: translateX(-30px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }

    .review-row {
      animation: slideInFromLeft var(--animation-duration-slow) var(--animation-easing) forwards;
    }

    .stat-card {
      animation: slideInFromLeft var(--animation-duration-slow) var(--animation-easing) forwards;
    }

    .stat-card:nth-child(1) { animation-delay: 0ms; }
    .stat-card:nth-child(2) { animation-delay: 100ms; }
    .stat-card:nth-child(3) { animation-delay: 200ms; }
    .stat-card:nth-child(4) { animation-delay: 300ms; }

    /* Loading states */
    @keyframes shimmer {
      0% { background-position: -200px 0; }
      100% { background-position: calc(200px + 100%) 0; }
    }

    .loading-shimmer {
      background: linear-gradient(90deg, var(--color-gray-200) 25%, var(--color-gray-300) 50%, var(--color-gray-200) 75%);
      background-size: 200px 100%;
      animation: shimmer 1.5s infinite;
    }
  `]
})
export class CodeReviewsComponent implements OnInit {
  displayedColumns: string[] = [
    'fileName', 'repository', 'branch', 'developer', 
    'status', 'qualityScore', 'issuesFound', 'createdAt', 'actions'
  ];

  reviews: CodeReview[] = [];
  repositories: any[] = [];
  
  filters = {
    repositoryId: '',
    status: '',
    startDate: undefined as Date | undefined,
    endDate: undefined as Date | undefined,
    search: ''
  };

  // Statistics
  totalReviews = 0;
  averageScore = 0;
  totalIssues = 0;
  completedReviews = 0;

  constructor(private codeReviewService: CodeReviewService) {}

  ngOnInit(): void {
    this.loadReviews();
    this.loadRepositories();
    this.loadStatistics();
  }

  loadReviews(): void {
    this.codeReviewService.getCodeReviews().subscribe({
      next: (reviews: any[]) => {
        this.reviews = reviews;
        this.updateStatistics();
      },
      error: (error: any) => console.error('Erro ao carregar reviews:', error)
    });
  }

  loadRepositories(): void {
    // TODO: Implementar carregamento de repositórios
    this.repositories = [
      { id: '1', name: 'frontend-app' },
      { id: '2', name: 'backend-api' },
      { id: '3', name: 'mobile-app' }
    ];
  }

  loadStatistics(): void {
    // TODO: Implementar carregamento de estatísticas do backend
    this.updateStatistics();
  }

  updateStatistics(): void {
    this.totalReviews = this.reviews.length;
    this.completedReviews = this.reviews.filter(r => r.status === 'COMPLETED').length;
    this.totalIssues = this.reviews.reduce((sum, r) => sum + (r.issuesFound || 0), 0);
    
    const scoresSum = this.reviews
      .filter(r => r.qualityScore)
      .reduce((sum, r) => sum + r.qualityScore!, 0);
    
    this.averageScore = scoresSum / this.reviews.filter(r => r.qualityScore).length || 0;
  }

  applyFilters(): void {
    // TODO: Implementar filtros do backend
    console.log('Aplicando filtros:', this.filters);
  }

  clearFilters(): void {
    this.filters = {
      repositoryId: '',
      status: '',
      startDate: undefined,
      endDate: undefined,
      search: ''
    };
    this.applyFilters();
  }

  viewReview(review: CodeReview): void {
    // TODO: Implementar navegação para detalhes do review
    console.log('Visualizando review:', review);
  }

  downloadReview(review: CodeReview): void {
    this.codeReviewService.downloadReview(review.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `review-${review.fileName}-${review.id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => console.error('Erro ao baixar review:', error)
    });
  }

  retryReview(review: CodeReview): void {
    this.codeReviewService.retryReview(review.id).subscribe({
      next: () => {
        console.log('Review reagendado:', review.id);
        this.loadReviews();
      },
      error: (error: any) => console.error('Erro ao reagendar review:', error)
    });
  }

  exportReviews(): void {
    this.codeReviewService.exportReviews('CSV', this.filters).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reviews-export-${new Date().toISOString().split('T')[0]}.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => console.error('Erro ao exportar reviews:', error)
    });
  }

  canRetryReview(review: CodeReview): boolean {
    return review.status === 'FAILED';
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'PENDING': 'Pendente',
      'IN_PROGRESS': 'Em Progresso',
      'COMPLETED': 'Concluído',
      'FAILED': 'Falhou'
    };
    return labels[status] || status;
  }

  getScoreClass(score: number | null): string {
    if (!score) return '';
    if (score >= 90) return 'score-excellent';
    if (score >= 75) return 'score-good';
    if (score >= 60) return 'score-fair';
    return 'score-poor';
  }
}
