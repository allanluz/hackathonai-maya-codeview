import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'MAYA Code Review System';

  metrics = {
    totalAnalyses: 1247,
    linesAnalyzed: '2.3M',
    issuesFound: 183,
    qualityScore: 94
  };

  analysisData = [
    { fileName: 'UserService.java', score: 96 },
    { fileName: 'PaymentController.java', score: 89 },
    { fileName: 'DatabaseConfig.java', score: 92 },
    { fileName: 'SecurityFilter.java', score: 88 },
    { fileName: 'ApiResponse.java', score: 95 }
  ];

  constructor(private http: HttpClient) {}

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

  getScoreClass(score: number): string {
    if (score >= 90) return 'score-excellent';
    if (score >= 80) return 'score-good';
    if (score >= 70) return 'score-fair';
    return 'score-poor';
  }
}
