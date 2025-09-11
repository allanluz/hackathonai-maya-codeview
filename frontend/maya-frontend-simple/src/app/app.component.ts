import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatIconModule,
    MatButtonModule,
    MatBadgeModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'MAYA Dashboard';

  metrics = {
    totalAnalyses: 1247,
    linesAnalyzed: '2.4M',
    issuesFound: 89,
    qualityScore: 94,
    criticalIssues: 12,
    resolvedIssues: 156,
    pendingReviews: 23
  };

  analysisData = [
    {
      fileName: 'AuthService.java',
      type: 'Java',
      author: 'Maria Silva',
      lastAnalysis: '2 mins ago',
      score: 95,
      issues: 2
    },
    {
      fileName: 'UserController.ts',
      type: 'TypeScript',
      author: 'João Santos',
      lastAnalysis: '15 mins ago',
      score: 88,
      issues: 5
    },
    {
      fileName: 'PaymentService.py',
      type: 'Python',
      author: 'Ana Costa',
      lastAnalysis: '1 hour ago',
      score: 92,
      issues: 3
    },
    {
      fileName: 'DatabaseUtils.cs',
      type: 'C#',
      author: 'Pedro Lima',
      lastAnalysis: '2 hours ago',
      score: 76,
      issues: 8
    }
  ];

  recentActivities = [
    {
      type: 'analysis',
      action: 'Code analysis completed',
      file: 'AuthService.java',
      time: '2 minutes ago'
    },
    {
      type: 'review',
      action: 'Code review approved',
      file: 'UserController.ts',
      time: '15 minutes ago'
    },
    {
      type: 'issue',
      action: 'Critical issue found',
      file: 'PaymentService.py',
      time: '1 hour ago'
    },
    {
      type: 'fix',
      action: 'Issue resolved',
      file: 'DatabaseUtils.cs',
      time: '2 hours ago'
    },
    {
      type: 'analysis',
      action: 'New analysis started',
      file: 'SecurityModule.java',
      time: '3 hours ago'
    }
  ];

  constructor(private http: HttpClient) {
    this.loadDashboardData();
  }

  navigateTo(route: string) {
    console.log('Navigating to:', route);
    // Implement navigation logic here
  }

  getScoreClass(score: number): string {
    if (score >= 90) return 'score-excellent';
    if (score >= 75) return 'score-good';
    if (score >= 60) return 'score-fair';
    return 'score-poor';
  }

  getActivityClass(type: string): string {
    switch (type) {
      case 'analysis': return 'activity-analysis';
      case 'review': return 'activity-review';
      case 'issue': return 'activity-issue';
      case 'fix': return 'activity-fix';
      case 'alert': return 'activity-alert';
      case 'integration': return 'activity-integration';
      default: return 'activity-default';
    }
  }

  getActivityIcon(type: string): string {
    switch (type) {
      case 'analysis': return 'analytics';
      case 'review': return 'rate_review';
      case 'issue': return 'error';
      case 'fix': return 'build';
      case 'alert': return 'warning';
      case 'integration': return 'integration_instructions';
      default: return 'info';
    }
  }

  private loadDashboardData() {
    this.http.get<any>('http://localhost:8080/api/dashboard').subscribe({
      next: (data) => {
        console.log('Dashboard data loaded:', data);
        if (data) {
          this.metrics = { ...this.metrics, ...data };
        }
      },
      error: (error) => {
        console.warn('Could not load dashboard data, using mock data:', error);
      }
    });
  }
}
