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

interface MenuItem {
  name: string;
  icon: string;
  route: string;
  badge?: number;
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
    MatBadgeModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'MAYA Code Review System';
  sidenavOpened = true;

  metrics = {
    totalAnalyses: 1247,
    linesAnalyzed: '2.3M',
    issuesFound: 183,
    qualityScore: 94,
    criticalIssues: 12,
    resolvedIssues: 856,
    pendingReviews: 23
  };

  menuItems: MenuItem[] = [
    { name: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { name: 'Análises', icon: 'analytics', route: '/analyses', badge: 5 },
    { name: 'Repositórios', icon: 'folder', route: '/repositories' },
    { name: 'Code Review', icon: 'rate_review', route: '/reviews', badge: 12 },
    { name: 'Relatórios', icon: 'assessment', route: '/reports' },
    { name: 'Configurações', icon: 'settings', route: '/settings' },
    { name: 'Usuários', icon: 'people', route: '/users' },
    { name: 'Integrações', icon: 'extension', route: '/integrations' }
  ];

  analysisData = [
    { 
      fileName: 'UserService.java', 
      score: 96, 
      issues: 2, 
      type: 'Service',
      lastAnalysis: '2 min ago',
      author: 'João Silva'
    },
    { 
      fileName: 'PaymentController.java', 
      score: 89, 
      issues: 5, 
      type: 'Controller',
      lastAnalysis: '5 min ago',
      author: 'Maria Santos'
    },
    { 
      fileName: 'DatabaseConfig.java', 
      score: 92, 
      issues: 3, 
      type: 'Configuration',
      lastAnalysis: '10 min ago',
      author: 'Pedro Oliveira'
    },
    { 
      fileName: 'SecurityFilter.java', 
      score: 88, 
      issues: 7, 
      type: 'Filter',
      lastAnalysis: '15 min ago',
      author: 'Ana Costa'
    },
    { 
      fileName: 'ApiResponse.java', 
      score: 95, 
      issues: 1, 
      type: 'DTO',
      lastAnalysis: '20 min ago',
      author: 'Carlos Lima'
    }
  ];

  recentActivities = [
    { action: 'Nova análise iniciada', file: 'UserService.java', time: '2 min ago', type: 'analysis' },
    { action: 'Code review aprovado', file: 'PaymentController.java', time: '5 min ago', type: 'review' },
    { action: 'Issue corrigida', file: 'DatabaseConfig.java', time: '8 min ago', type: 'fix' },
    { action: 'Nova branch detectada', file: 'feature/user-auth', time: '12 min ago', type: 'branch' },
    { action: 'Deploy realizado', file: 'production', time: '30 min ago', type: 'deploy' }
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
    // Para agora, apenas mostrar o dashboard
    console.log('Navegando para:', route);
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
      'branch': 'call_split',
      'deploy': 'rocket_launch'
    };
    return icons[type] || 'info';
  }

  getActivityColor(type: string): string {
    const colors: { [key: string]: string } = {
      'analysis': 'primary',
      'review': 'accent',
      'fix': 'warn',
      'branch': 'primary',
      'deploy': 'accent'
    };
    return colors[type] || 'primary';
  }
}
