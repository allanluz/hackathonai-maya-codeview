import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
  selector: 'app-code-analysis',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatProgressBarModule
  ],
  template: `
    <div class="analysis-container">
      <h2>Análise Detalhada de Código</h2>
      
      <div class="analysis-grid">
        <mat-card class="analysis-detail-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>code</mat-icon>
              Qualidade do Código
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="metric-row">
              <span>Complexidade Ciclomática</span>
              <div class="metric-bar">
                <mat-progress-bar mode="determinate" [value]="75" color="primary"></mat-progress-bar>
                <span class="metric-value">7.5/10</span>
              </div>
            </div>
            <div class="metric-row">
              <span>Cobertura de Testes</span>
              <div class="metric-bar">
                <mat-progress-bar mode="determinate" [value]="89" color="accent"></mat-progress-bar>
                <span class="metric-value">89%</span>
              </div>
            </div>
            <div class="metric-row">
              <span>Duplicação de Código</span>
              <div class="metric-bar">
                <mat-progress-bar mode="determinate" [value]="15" color="warn"></mat-progress-bar>
                <span class="metric-value">1.5%</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="issues-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>bug_report</mat-icon>
              Issues por Categoria
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="issue-category" *ngFor="let category of issueCategories">
              <div class="category-header">
                <mat-icon [color]="category.color">{{ category.icon }}</mat-icon>
                <span class="category-name">{{ category.name }}</span>
                <mat-chip class="category-count" [color]="category.color">
                  {{ category.count }}
                </mat-chip>
              </div>
              <div class="category-issues">
                <div class="issue-item" *ngFor="let issue of category.issues">
                  <span class="issue-title">{{ issue.title }}</span>
                  <span class="issue-severity" [class]="'severity-' + issue.severity">
                    {{ issue.severity }}
                  </span>
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .analysis-container {
      padding: 2rem;
    }
    
    .analysis-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 2rem;
      margin-top: 2rem;
    }
    
    .metric-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }
    
    .metric-bar {
      display: flex;
      align-items: center;
      gap: 1rem;
      flex: 1;
      margin-left: 1rem;
    }
    
    .metric-value {
      font-weight: bold;
      min-width: 60px;
    }
    
    .issue-category {
      margin-bottom: 1.5rem;
    }
    
    .category-header {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 0.5rem;
    }
    
    .category-name {
      flex: 1;
      font-weight: 500;
    }
    
    .category-count {
      font-size: 0.8rem;
    }
    
    .category-issues {
      margin-left: 2rem;
    }
    
    .issue-item {
      display: flex;
      justify-content: space-between;
      padding: 0.5rem;
      border-bottom: 1px solid #eee;
    }
    
    .issue-title {
      font-size: 0.9rem;
    }
    
    .issue-severity {
      font-size: 0.8rem;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      color: white;
    }
    
    .severity-high {
      background-color: #f44336;
    }
    
    .severity-medium {
      background-color: #ff9800;
    }
    
    .severity-low {
      background-color: #4caf50;
    }
    
    @media (max-width: 768px) {
      .analysis-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class CodeAnalysisComponent {
  issueCategories = [
    {
      name: 'Segurança',
      icon: 'security',
      color: 'warn' as const,
      count: 3,
      issues: [
        { title: 'SQL Injection vulnerability', severity: 'high' },
        { title: 'Weak password validation', severity: 'medium' },
        { title: 'Missing HTTPS redirect', severity: 'low' }
      ]
    },
    {
      name: 'Performance',
      icon: 'speed',
      color: 'accent' as const,
      count: 5,
      issues: [
        { title: 'N+1 query problem', severity: 'high' },
        { title: 'Large JSON response', severity: 'medium' },
        { title: 'Unoptimized image loading', severity: 'low' }
      ]
    },
    {
      name: 'Maintainability',
      icon: 'build',
      color: 'primary' as const,
      count: 7,
      issues: [
        { title: 'Long method detected', severity: 'medium' },
        { title: 'Missing documentation', severity: 'low' },
        { title: 'Complex conditional logic', severity: 'medium' }
      ]
    }
  ];
}
