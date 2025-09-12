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
      padding: 24px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .filters-card {
      margin-bottom: 24px;
    }

    .filters-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      align-items: end;
    }

    .table-card {
      margin-bottom: 24px;
    }

    .reviews-table {
      width: 100%;
    }

    .review-row {
      cursor: pointer;
    }

    .review-row:hover {
      background: #f5f5f5;
    }

    .file-info {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .score-container {
      display: flex;
      align-items: center;
    }

    .score-value {
      font-weight: bold;
      padding: 4px 8px;
      border-radius: 4px;
    }

    .score-excellent {
      background: #e8f5e8;
      color: #4caf50;
    }

    .score-good {
      background: #e3f2fd;
      color: #2196f3;
    }

    .score-fair {
      background: #fff3e0;
      color: #ff9800;
    }

    .score-poor {
      background: #ffebee;
      color: #f44336;
    }

    .status-pending {
      background: #fff3e0;
      color: #ff9800;
    }

    .status-in_progress {
      background: #e3f2fd;
      color: #2196f3;
    }

    .status-completed {
      background: #e8f5e8;
      color: #4caf50;
    }

    .status-failed {
      background: #ffebee;
      color: #f44336;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;
    }

    .stat-card {
      min-height: 120px;
    }

    .stat-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
    }

    .stat-header h3 {
      margin: 0;
      font-size: 1rem;
      color: #666;
    }

    .stat-value {
      font-size: 2rem;
      font-weight: bold;
      color: #2196f3;
    }

    @media (max-width: 768px) {
      .reviews-container {
        padding: 16px;
      }

      .header {
        flex-direction: column;
        gap: 16px;
        align-items: stretch;
      }

      .filters-grid {
        grid-template-columns: 1fr;
      }
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
      next: (reviews) => {
        this.reviews = reviews;
        this.updateStatistics();
      },
      error: (error) => console.error('Erro ao carregar reviews:', error)
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
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `review-${review.fileName}-${review.id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => console.error('Erro ao baixar review:', error)
    });
  }

  retryReview(review: CodeReview): void {
    this.codeReviewService.retryReview(review.id).subscribe({
      next: () => {
        console.log('Review reagendado:', review.id);
        this.loadReviews();
      },
      error: (error) => console.error('Erro ao reagendar review:', error)
    });
  }

  exportReviews(): void {
    this.codeReviewService.exportReviews('CSV', this.filters).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reviews-export-${new Date().toISOString().split('T')[0]}.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => console.error('Erro ao exportar reviews:', error)
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
