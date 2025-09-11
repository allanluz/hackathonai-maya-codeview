import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';

interface MenuItem {
  name: string;
  icon: string;
  route: string;
  badge?: number;
}

interface AdditionalMetric {
  icon: string;
  value: string;
  label: string;
  color: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, 
    RouterOutlet,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatGridListModule,
    MatProgressBarModule,
    MatChipsModule,
    MatBadgeModule,
    MatMenuModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'Sistema de Revisão de Código MAYA - Evertec';
  sidenavOpened = true;

  metrics = {
    totalAnalyses: 1247,
    linesAnalyzed: '2.8M',
    issuesFound: 89,
    qualityScore: 87,
    criticalIssues: 5,
    resolvedIssues: 342,
    pendingReviews: 12
  };

  menuItems: MenuItem[] = [
    { name: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { name: 'Análise de Código', icon: 'analytics', route: '/analyses', badge: 3 },
    { name: 'Regras de Qualidade', icon: 'rule', route: '/rules' },
    { name: 'Relatórios', icon: 'assessment', route: '/reports', badge: 7 },
    { name: 'Equipes', icon: 'groups', route: '/teams' },
    { name: 'Integrações', icon: 'integration_instructions', route: '/integrations' },
    { name: 'Segurança', icon: 'security', route: '/security', badge: 2 },
    { name: 'Configurações', icon: 'settings', route: '/settings' }
  ];

  analysisData = [
    { 
      fileName: 'UserService.java', 
      score: 92, 
      issues: 2, 
      type: 'Java',
      lastAnalysis: '2 horas atrás',
      author: 'João Silva'
    },
    { 
      fileName: 'payment-component.tsx', 
      score: 88, 
      issues: 4, 
      type: 'TypeScript',
      lastAnalysis: '4 horas atrás',
      author: 'Maria Santos'
    },
    { 
      fileName: 'api-controller.py', 
      score: 95, 
      issues: 1, 
      type: 'Python',
      lastAnalysis: '6 horas atrás',
      author: 'Carlos Rodriguez'
    },
    { 
      fileName: 'database-config.sql', 
      score: 76, 
      issues: 8, 
      type: 'SQL',
      lastAnalysis: '8 horas atrás',
      author: 'Ana Costa'
    },
    { 
      fileName: 'security-utils.cs', 
      score: 90, 
      issues: 3, 
      type: 'C#',
      lastAnalysis: '1 dia atrás',
      author: 'Pedro Lima'
    }
  ];

  recentActivities = [
    { action: 'Nova análise executada', file: 'UserService.java', time: '2 min atrás', type: 'analysis' },
    { action: 'Issue crítica corrigida', file: 'PaymentProcessor.java', time: '15 min atrás', type: 'fix' },
    { action: 'Review de código finalizado', file: 'AuthController.ts', time: '1 hora atrás', type: 'review' },
    { action: 'Alerta de segurança detectado', file: 'SecurityConfig.java', time: '2 horas atrás', type: 'alert' },
    { action: 'Integração com GitLab atualizada', file: 'gitlab-config.yml', time: '3 horas atrás', type: 'integration' }
  ];

  additionalMetrics: AdditionalMetric[] = [
    {
      icon: 'priority_high',
      value: '5',
      label: 'Issues Críticas',
      color: '#ff3366'
    },
    {
      icon: 'check_circle',
      value: '342',
      label: 'Issues Resolvidas',
      color: '#0066cc'
    },
    {
      icon: 'rate_review',
      value: '12',
      label: 'Reviews Pendentes',
      color: '#00cc66'
    },
    {
      icon: 'speed',
      value: '2.3s',
      label: 'Tempo Médio',
      color: '#4caf50'
    },
    {
      icon: 'groups',
      value: '15',
      label: 'Usuários Ativos',
      color: '#ff9800'
    },
    {
      icon: 'security',
      value: '98.5%',
      label: 'Uptime Sistema',
      color: '#9c27b0'
    }
  ];

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    // Tentar carregar dados do backend
    this.http.get('http://localhost:8080/api/dashboard').subscribe({
      next: (data: any) => {
        console.log('Dados do backend carregados:', data);
        // Atualizar métricas com dados reais se disponível
      },
      error: (error) => {
        console.log('Backend não disponível, usando dados mockados:', error);
        // Manter dados mockados
      }
    });
  }

  toggleSidenav() {
    this.sidenavOpened = !this.sidenavOpened;
  }

  navigateTo(route: string) {
    console.log('Navegando para:', route);
    // Implementar navegação quando necessário
  }

  getScoreClass(score: number): string {
    if (score >= 90) return 'score-excellent';
    if (score >= 80) return 'score-good';
    if (score >= 70) return 'score-fair';
    return 'score-poor';
  }

  getActivityIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'analysis': 'analytics',
      'review': 'rate_review',
      'fix': 'build',
      'alert': 'warning',
      'integration': 'integration_instructions'
    };
    return icons[type] || 'info';
  }

  getActivityColor(type: string): string {
    const colors: { [key: string]: string } = {
      'analysis': 'primary',
      'review': 'primary',
      'fix': 'accent',
      'alert': 'warn',
      'integration': 'primary'
    };
    return colors[type] || 'primary';
  }
}
