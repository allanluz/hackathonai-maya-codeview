import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { MayaApiService, DashboardData, AnalysisSummary } from '../../services/maya-api.service';
import { MaterialModule } from '../../material.module';
import { CommonModule } from '@angular/common';

interface ChartData {
  label: string;
  value: number;
  count: number;
}

interface ServiceStatus {
  name: string;
  status: string;
  icon: string;
  iconClass: string;
  statusClass: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MaterialModule, CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  dashboardData: DashboardData | null = null;
  analysisSummary: AnalysisSummary | null = null;
  lastUpdate: Date | null = null;
  isLoading = true;
  chartData: ChartData[] | null = null;

  constructor(
    private mayaApiService: MayaApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
    this.loadAnalysisSummary();
    this.generateChartData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadDashboardData(): void {
    this.mayaApiService.dashboard$
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.dashboardData = data;
          if (data) {
            this.lastUpdate = new Date(data.lastUpdate);
          }
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Erro ao carregar dashboard:', error);
          this.isLoading = false;
        }
      });
  }

  private loadAnalysisSummary(): void {
    this.mayaApiService.getAnalysisSummary()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (summary) => {
          this.analysisSummary = summary;
        },
        error: (error) => {
          console.error('Erro ao carregar resumo de análises:', error);
        }
      });
  }

  private generateChartData(): void {
    // Simular dados do gráfico - em produção viria da API
    setTimeout(() => {
      this.chartData = [
        { label: 'Seg', value: 75, count: 12 },
        { label: 'Ter', value: 60, count: 9 },
        { label: 'Qua', value: 90, count: 15 },
        { label: 'Qui', value: 45, count: 7 },
        { label: 'Sex', value: 80, count: 13 },
        { label: 'Sáb', value: 30, count: 4 },
        { label: 'Dom', value: 20, count: 2 }
      ];
    }, 1000);
  }

  getStandardsIcon(): string {
    const compliance = this.dashboardData?.evertecStandardsCompliance || 0;
    if (compliance >= 90) return 'check_circle';
    if (compliance >= 70) return 'warning';
    return 'error';
  }

  getStandardsIconClass(): string {
    const compliance = this.dashboardData?.evertecStandardsCompliance || 0;
    if (compliance >= 90) return 'standards-good';
    if (compliance >= 70) return 'standards-warning';
    return 'standards-error';
  }

  getServicesList(): ServiceStatus[] {
    const services = this.dashboardData?.services;
    if (!services) return [];

    return [
      {
        name: 'Banco de Dados',
        status: services.database,
        icon: services.database === 'CONNECTED' ? 'storage' : 'storage',
        iconClass: services.database === 'CONNECTED' ? 'service-online' : 'service-offline',
        statusClass: services.database === 'CONNECTED' ? 'status-online' : 'status-offline'
      },
      {
        name: 'TFS/Azure DevOps',
        status: services.tfs,
        icon: services.tfs === 'CONNECTED' ? 'cloud_done' : 'cloud_off',
        iconClass: services.tfs === 'CONNECTED' ? 'service-online' : 'service-offline',
        statusClass: services.tfs === 'CONNECTED' ? 'status-online' : 'status-offline'
      },
      {
        name: 'Evertec AI',
        status: services.evertecAI,
        icon: services.evertecAI === 'CONNECTED' ? 'psychology' : 'psychology',
        iconClass: services.evertecAI === 'CONNECTED' ? 'service-online' : 'service-offline',
        statusClass: services.evertecAI === 'CONNECTED' ? 'status-online' : 'status-offline'
      }
    ];
  }

  navigateToReviews(): void {
    this.router.navigate(['/reviews']);
  }

  startNewReview(): void {
    this.router.navigate(['/reviews/new']);
  }
}
