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
      padding: 24px;
      max-width: 1200px;
      margin: 0 auto;
    }

    h1 {
      margin-bottom: 24px;
      color: #333;
    }

    .overview-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }

    .metric-card {
      min-height: 140px;
    }

    .metric-card.alert {
      background: #fff3cd;
      border-left: 4px solid #ffc107;
    }

    .metric-card mat-card-header {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .metric-value {
      font-size: 2.5rem;
      font-weight: bold;
      color: #2196f3;
      margin: 8px 0;
    }

    .metric-subtitle {
      color: #666;
      font-size: 0.9rem;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 48px;
    }

    .ranking-card,
    .alerts-card,
    .actions-card {
      margin-bottom: 24px;
    }

    .ranking-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .ranking-item {
      display: flex;
      align-items: center;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 8px;
      gap: 16px;
    }

    .rank {
      background: #2196f3;
      color: white;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
    }

    .info {
      flex: 1;
    }

    .name {
      font-weight: 500;
      margin-bottom: 4px;
    }

    .stats {
      color: #666;
      font-size: 0.9rem;
    }

    .improvement.positive {
      color: #4caf50;
      font-weight: bold;
    }

    .improvement.neutral {
      color: #666;
    }

    .alert-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 12px;
      border-radius: 8px;
      margin-bottom: 8px;
    }

    .alert-item.warning {
      background: #fff3cd;
      border-left: 4px solid #ffc107;
    }

    .alert-item.error {
      background: #f8d7da;
      border-left: 4px solid #dc3545;
    }

    .alert-content {
      flex: 1;
    }

    .alert-message {
      font-weight: 500;
      margin-bottom: 4px;
    }

    .alert-details {
      color: #666;
      font-size: 0.9rem;
      margin-bottom: 4px;
    }

    .alert-time {
      color: #999;
      font-size: 0.8rem;
    }

    .action-buttons {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 12px;
    }

    .action-buttons button {
      height: 48px;
    }

    .action-buttons mat-icon {
      margin-right: 8px;
    }

    .status-active { background: #4caf50; color: white; }
    .status-inactive { background: #9e9e9e; color: white; }
    .status-warning { background: #ff9800; color: white; }
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
