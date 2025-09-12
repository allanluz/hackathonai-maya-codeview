import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { DashboardService, DashboardOverview, RepositoryRanking, DeveloperRanking, SystemAlert } from '../../services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatGridListModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatChipsModule
  ],
  template: `
    <div class="dashboard-container">
      <h1>Dashboard MAYA</h1>
      
      <!-- Overview Cards -->
      <div class="overview-grid" *ngIf="overview">
        <mat-card class="metric-card">
          <mat-card-header>
            <mat-icon>analytics</mat-icon>
            <mat-card-title>Total de Reviews</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="metric-value">{{ overview.totalReviews }}</div>
            <div class="metric-subtitle">Últimos 30 dias</div>
          </mat-card-content>
        </mat-card>

        <mat-card class="metric-card">
          <mat-card-header>
            <mat-icon>folder</mat-icon>
            <mat-card-title>Repositórios Ativos</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="metric-value">{{ overview.activeRepositories }}</div>
            <div class="metric-subtitle">Conectados</div>
          </mat-card-content>
        </mat-card>

        <mat-card class="metric-card">
          <mat-card-header>
            <mat-icon>star</mat-icon>
            <mat-card-title>Score Médio</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="metric-value">{{ overview.averageQualityScore | number:'1.1-1' }}</div>
            <div class="metric-subtitle">Qualidade do código</div>
          </mat-card-content>
        </mat-card>

        <mat-card class="metric-card alert" *ngIf="overview.criticalIssues > 0">
          <mat-card-header>
            <mat-icon>warning</mat-icon>
            <mat-card-title>Issues Críticos</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="metric-value">{{ overview.criticalIssues }}</div>
            <div class="metric-subtitle">Requer atenção</div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Loading -->
      <div class="loading-container" *ngIf="loading">
        <mat-spinner></mat-spinner>
        <p>Carregando dados do dashboard...</p>
      </div>

      <!-- Repository Ranking -->
      <mat-card class="ranking-card" *ngIf="repositoryRanking.length > 0">
        <mat-card-header>
          <mat-icon>leaderboard</mat-icon>
          <mat-card-title>Ranking de Repositórios</mat-card-title>
          <mat-card-subtitle>Top repositórios por qualidade</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div class="ranking-list">
            <div class="ranking-item" *ngFor="let repo of repositoryRanking; let i = index">
              <div class="rank">{{ i + 1 }}</div>
              <div class="info">
                <div class="name">{{ repo.repositoryName }}</div>
                <div class="stats">
                  Score: {{ repo.averageScore | number:'1.1-1' }} | 
                  Reviews: {{ repo.totalReviews }} | 
                  Issues: {{ repo.criticalIssues }}
                </div>
              </div>
              <mat-chip [class]="getStatusClass(repo.status)">{{ repo.status }}</mat-chip>
            </div>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Developer Ranking -->
      <mat-card class="ranking-card" *ngIf="developerRanking.length > 0">
        <mat-card-header>
          <mat-icon>person</mat-icon>
          <mat-card-title>Ranking de Desenvolvedores</mat-card-title>
          <mat-card-subtitle>Top desenvolvedores por qualidade</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div class="ranking-list">
            <div class="ranking-item" *ngFor="let dev of developerRanking; let i = index">
              <div class="rank">{{ i + 1 }}</div>
              <div class="info">
                <div class="name">{{ dev.developer }}</div>
                <div class="stats">
                  Score: {{ dev.averageScore | number:'1.1-1' }} | 
                  Reviews: {{ dev.totalReviews }} | 
                  Nível: {{ dev.level }}
                </div>
              </div>
              <div class="improvement" [class]="dev.improvement > 0 ? 'positive' : 'neutral'">
                {{ dev.improvement > 0 ? '+' : '' }}{{ dev.improvement | number:'1.1-1' }}%
              </div>
            </div>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- System Alerts -->
      <mat-card class="alerts-card" *ngIf="alerts.length > 0">
        <mat-card-header>
          <mat-icon>notifications</mat-icon>
          <mat-card-title>Alertas do Sistema</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="alert-item" *ngFor="let alert of alerts" [class]="alert.severity.toLowerCase()">
            <mat-icon>{{ getAlertIcon(alert.type) }}</mat-icon>
            <div class="alert-content">
              <div class="alert-message">{{ alert.message }}</div>
              <div class="alert-details">{{ alert.details }}</div>
              <div class="alert-time">{{ alert.timestamp | date:'short' }}</div>
            </div>
            <button mat-icon-button *ngIf="!alert.resolved" (click)="resolveAlert(alert.id)">
              <mat-icon>check</mat-icon>
            </button>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Quick Actions -->
      <mat-card class="actions-card">
        <mat-card-header>
          <mat-icon>rocket_launch</mat-icon>
          <mat-card-title>Ações Rápidas</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="action-buttons">
            <button mat-raised-button color="primary" (click)="navigateTo('/repositories')">
              <mat-icon>folder_open</mat-icon>
              Gerenciar Repositórios
            </button>
            <button mat-raised-button color="accent" (click)="navigateTo('/review-prompts')">
              <mat-icon>edit</mat-icon>
              Configurar Prompts
            </button>
            <button mat-raised-button (click)="navigateTo('/code-reviews')">
              <mat-icon>code</mat-icon>
              Ver Reviews
            </button>
            <button mat-raised-button (click)="refreshDashboard()">
              <mat-icon>refresh</mat-icon>
              Atualizar
            </button>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: var(--space-2xl);
      max-width: 1400px;
      margin: 0 auto;
      background: var(--bg-secondary);
    }

    h1 {
      margin-bottom: var(--space-2xl);
      color: var(--text-primary);
      font-size: 2.5rem;
      font-weight: 800;
      background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      text-align: center;
      letter-spacing: -0.02em;
    }

    .overview-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: var(--space-xl);
      margin-bottom: var(--space-3xl);
    }

    .metric-card {
      background: var(--bg-surface);
      border: 1px solid var(--color-gray-200);
      border-radius: var(--radius-2xl);
      box-shadow: var(--shadow-lg);
      transition: all var(--animation-duration-normal) var(--animation-easing);
      overflow: hidden;
      position: relative;
      min-height: 180px;
    }

    .metric-card::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: linear-gradient(90deg, var(--color-primary), var(--color-secondary));
    }

    .metric-card:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-2xl);
      border-color: var(--color-primary);
    }

    .metric-card.alert {
      background: linear-gradient(135deg, #fef7ed 0%, #fed7aa 100%);
      border-color: var(--color-warning);
      animation: pulse 2s infinite;
    }

    .metric-card.alert::before {
      background: var(--color-warning);
    }

    .metric-card mat-card-header {
      display: flex;
      align-items: center;
      gap: var(--space-md);
      padding: var(--space-xl) var(--space-xl) var(--space-md);
    }

    .metric-card mat-icon {
      width: 48px;
      height: 48px;
      font-size: 2rem;
      background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
      color: var(--text-inverse);
      border-radius: var(--radius-xl);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: var(--shadow-md);
    }

    .metric-card mat-card-title {
      font-size: 1.125rem;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }

    .metric-value {
      font-size: 3rem;
      font-weight: 900;
      background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin: var(--space-md) 0;
      line-height: 1;
      letter-spacing: -0.02em;
    }

    .metric-subtitle {
      color: var(--text-secondary);
      font-size: 0.875rem;
      font-weight: 500;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: var(--space-3xl);
      background: var(--bg-surface);
      border-radius: var(--radius-2xl);
      box-shadow: var(--shadow-lg);
    }

    .loading-container p {
      margin-top: var(--space-lg);
      color: var(--text-secondary);
      font-weight: 500;
    }

    .ranking-card,
    .alerts-card,
    .actions-card {
      background: var(--bg-surface);
      border: 1px solid var(--color-gray-200);
      border-radius: var(--radius-2xl);
      box-shadow: var(--shadow-lg);
      margin-bottom: var(--space-2xl);
      overflow: hidden;
      transition: all var(--animation-duration-normal) var(--animation-easing);
    }

    .ranking-card:hover,
    .alerts-card:hover,
    .actions-card:hover {
      box-shadow: var(--shadow-xl);
      border-color: var(--color-primary-light);
    }

    .ranking-card mat-card-header,
    .alerts-card mat-card-header,
    .actions-card mat-card-header {
      background: linear-gradient(135deg, var(--color-gray-50), var(--color-gray-100));
      border-bottom: 1px solid var(--color-gray-200);
      padding: var(--space-xl);
    }

    .ranking-card mat-icon,
    .alerts-card mat-icon,
    .actions-card mat-icon {
      color: var(--color-primary);
      margin-right: var(--space-md);
    }

    .ranking-list {
      display: flex;
      flex-direction: column;
      gap: var(--space-md);
      padding: var(--space-lg);
    }

    .ranking-item {
      display: flex;
      align-items: center;
      padding: var(--space-lg);
      background: linear-gradient(135deg, var(--bg-surface), var(--color-gray-50));
      border: 1px solid var(--color-gray-200);
      border-radius: var(--radius-xl);
      gap: var(--space-lg);
      transition: all var(--animation-duration-normal) var(--animation-easing);
    }

    .ranking-item:hover {
      transform: translateX(8px);
      border-color: var(--color-primary);
      box-shadow: var(--shadow-md);
    }

    .rank {
      background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
      color: var(--text-inverse);
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 1.125rem;
      box-shadow: var(--shadow-md);
    }

    .info {
      flex: 1;
    }

    .name {
      font-weight: 600;
      font-size: 1.125rem;
      color: var(--text-primary);
      margin-bottom: var(--space-xs);
    }

    .stats {
      color: var(--text-secondary);
      font-size: 0.875rem;
      font-weight: 500;
    }

    .improvement.positive {
      color: var(--color-success);
      font-weight: 700;
      background: var(--color-gray-100);
      padding: var(--space-xs) var(--space-sm);
      border-radius: var(--radius-md);
    }

    .improvement.neutral {
      color: var(--text-tertiary);
    }

    .alert-item {
      display: flex;
      align-items: flex-start;
      gap: var(--space-lg);
      padding: var(--space-lg);
      border-radius: var(--radius-xl);
      margin-bottom: var(--space-md);
      transition: all var(--animation-duration-normal) var(--animation-easing);
    }

    .alert-item:hover {
      transform: translateX(4px);
    }

    .alert-item.warning {
      background: linear-gradient(135deg, #fffbeb, #fef3c7);
      border: 1px solid var(--color-warning);
    }

    .alert-item.error {
      background: linear-gradient(135deg, #fef2f2, #fecaca);
      border: 1px solid var(--color-error);
    }

    .alert-content {
      flex: 1;
    }

    .alert-message {
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: var(--space-xs);
    }

    .alert-details {
      color: var(--text-secondary);
      font-size: 0.875rem;
      margin-bottom: var(--space-xs);
    }

    .alert-time {
      color: var(--text-tertiary);
      font-size: 0.8rem;
    }

    .action-buttons {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: var(--space-lg);
      padding: var(--space-xl);
    }

    .action-buttons button {
      height: 64px;
      border-radius: var(--radius-xl);
      font-weight: 600;
      font-size: 1rem;
      transition: all var(--animation-duration-normal) var(--animation-easing);
      box-shadow: var(--shadow-md);
    }

    .action-buttons button:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-xl);
    }

    .action-buttons mat-icon {
      margin-right: var(--space-md);
      font-size: 1.25rem;
    }

    .status-active { 
      background: linear-gradient(135deg, var(--color-success), #059669);
      color: var(--text-inverse);
    }
    
    .status-inactive { 
      background: linear-gradient(135deg, var(--color-gray-400), var(--color-gray-500));
      color: var(--text-inverse);
    }
    
    .status-warning { 
      background: linear-gradient(135deg, var(--color-warning), #d97706);
      color: var(--text-inverse);
    }

    /* Animations */
    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.8; }
    }

    @keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(30px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .metric-card {
      animation: fadeInUp var(--animation-duration-slow) var(--animation-easing);
    }

    .metric-card:nth-child(1) { animation-delay: 0ms; }
    .metric-card:nth-child(2) { animation-delay: 100ms; }
    .metric-card:nth-child(3) { animation-delay: 200ms; }
    .metric-card:nth-child(4) { animation-delay: 300ms; }

    /* Responsive Design */
    @media (max-width: 1200px) {
      .dashboard-container {
        padding: var(--space-xl);
      }
    }

    @media (max-width: 768px) {
      .dashboard-container {
        padding: var(--space-lg);
      }

      h1 {
        font-size: 2rem;
      }

      .overview-grid {
        grid-template-columns: 1fr;
        gap: var(--space-lg);
      }

      .action-buttons {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 480px) {
      .dashboard-container {
        padding: var(--space-md);
      }

      h1 {
        font-size: 1.75rem;
      }

      .metric-value {
        font-size: 2.5rem;
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  overview: DashboardOverview | null = null;
  repositoryRanking: RepositoryRanking[] = [];
  developerRanking: DeveloperRanking[] = [];
  alerts: SystemAlert[] = [];
  loading = true;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;

    // Carregar overview
    this.dashboardService.getOverview(30).subscribe({
      next: (data) => {
        this.overview = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar overview:', error);
        this.loading = false;
      }
    });

    // Carregar rankings
    this.dashboardService.getRepositoryRanking(30, 5).subscribe({
      next: (data) => this.repositoryRanking = data,
      error: (error) => console.error('Erro ao carregar ranking de repositórios:', error)
    });

    this.dashboardService.getDeveloperRanking(30, 5).subscribe({
      next: (data) => this.developerRanking = data,
      error: (error) => console.error('Erro ao carregar ranking de desenvolvedores:', error)
    });

    // Carregar alertas
    this.dashboardService.getActiveAlerts().subscribe({
      next: (data) => this.alerts = data,
      error: (error) => console.error('Erro ao carregar alertas:', error)
    });
  }

  refreshDashboard(): void {
    this.loadDashboardData();
  }

  navigateTo(route: string): void {
    // TODO: Implementar navegação
    console.log('Navegando para:', route);
  }

  resolveAlert(alertId: string): void {
    // TODO: Implementar resolução de alertas
    console.log('Resolvendo alerta:', alertId);
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }

  getAlertIcon(type: string): string {
    switch (type.toLowerCase()) {
      case 'performance':
        return 'speed';
      case 'security':
        return 'security';
      case 'error':
        return 'error';
      default:
        return 'info';
    }
  }
}
