import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="reports-container">
      <div class="container">
        <!-- Header -->
        <div class="page-header">
          <h1 class="page-title">
            <i class="pi pi-chart-bar"></i>
            Reports
          </h1>
          <p class="page-subtitle">
            Analyze code quality trends and generate detailed reports
          </p>
        </div>

        <!-- Filters -->
        <div class="filters-section">
          <div class="filters-card">
            <h3>Report Filters</h3>
            <div class="filters-grid">
              <div class="filter-group">
                <label>Date Range</label>
                <select class="filter-select">
                  <option>Last 7 days</option>
                  <option>Last 30 days</option>
                  <option>Last 3 months</option>
                  <option>Last year</option>
                  <option>Custom range</option>
                </select>
              </div>
              
              <div class="filter-group">
                <label>Repository</label>
                <select class="filter-select">
                  <option>All Repositories</option>
                  <option>DriveAMnet</option>
                  <option>Core-API</option>
                  <option>Frontend-Portal</option>
                </select>
              </div>
              
              <div class="filter-group">
                <label>Author</label>
                <select class="filter-select">
                  <option>All Authors</option>
                  <option>allan.luz</option>
                  <option>maria.silva</option>
                  <option>joao.santos</option>
                </select>
              </div>
              
              <div class="filter-group">
                <label>Status</label>
                <select class="filter-select">
                  <option>All Status</option>
                  <option>Completed</option>
                  <option>In Progress</option>
                  <option>Failed</option>
                </select>
              </div>
            </div>
            
            <div class="filter-actions">
              <button class="btn-secondary btn-small">Reset Filters</button>
              <button class="btn-primary btn-small">Apply Filters</button>
            </div>
          </div>
        </div>

        <!-- Charts Grid -->
        <div class="charts-grid">
          <!-- Code Quality Trend -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>Code Quality Trend</h3>
              <div class="chart-actions">
                <button class="btn-small btn-secondary">
                  <i class="pi pi-download"></i>
                  Export
                </button>
              </div>
            </div>
            <div class="chart-container">
              <div class="chart-placeholder">
                <i class="pi pi-chart-line chart-icon"></i>
                <p>Line chart showing code quality score over time</p>
                <small>Chart.js integration pending</small>
              </div>
            </div>
          </div>

          <!-- Issues by Type -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>Issues by Type</h3>
              <div class="chart-actions">
                <button class="btn-small btn-secondary">
                  <i class="pi pi-download"></i>
                  Export
                </button>
              </div>
            </div>
            <div class="chart-container">
              <div class="chart-placeholder">
                <i class="pi pi-chart-pie chart-icon"></i>
                <p>Pie chart showing distribution of issue types</p>
                <small>Chart.js integration pending</small>
              </div>
            </div>
          </div>

          <!-- Reviews by Author -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>Reviews by Author</h3>
              <div class="chart-actions">
                <button class="btn-small btn-secondary">
                  <i class="pi pi-download"></i>
                  Export
                </button>
              </div>
            </div>
            <div class="chart-container">
              <div class="chart-placeholder">
                <i class="pi pi-chart-bar chart-icon"></i>
                <p>Bar chart showing review counts by author</p>
                <small>Chart.js integration pending</small>
              </div>
            </div>
          </div>

          <!-- Repository Performance -->
          <div class="chart-card">
            <div class="chart-header">
              <h3>Repository Performance</h3>
              <div class="chart-actions">
                <button class="btn-small btn-secondary">
                  <i class="pi pi-download"></i>
                  Export
                </button>
              </div>
            </div>
            <div class="chart-container">
              <div class="chart-placeholder">
                <i class="pi pi-chart-line chart-icon"></i>
                <p>Comparison chart of repository performance metrics</p>
                <small>Chart.js integration pending</small>
              </div>
            </div>
          </div>
        </div>

        <!-- Summary Table -->
        <div class="summary-section">
          <div class="summary-card">
            <div class="summary-header">
              <h3>Detailed Report Summary</h3>
              <div class="summary-actions">
                <button class="btn-secondary">
                  <i class="pi pi-file-pdf"></i>
                  Export PDF
                </button>
                <button class="btn-secondary">
                  <i class="pi pi-file-excel"></i>
                  Export Excel
                </button>
              </div>
            </div>
            
            <div class="summary-table">
              <table>
                <thead>
                  <tr>
                    <th>Repository</th>
                    <th>Total Reviews</th>
                    <th>Avg Score</th>
                    <th>Critical Issues</th>
                    <th>Warning Issues</th>
                    <th>Last Review</th>
                    <th>Trend</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>DriveAMnet</td>
                    <td>45</td>
                    <td>
                      <span class="score good">87%</span>
                    </td>
                    <td>
                      <span class="issue-count critical">3</span>
                    </td>
                    <td>
                      <span class="issue-count warning">12</span>
                    </td>
                    <td>2 hours ago</td>
                    <td>
                      <span class="trend up">
                        <i class="pi pi-arrow-up"></i>
                        +5%
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td>Core-API</td>
                    <td>32</td>
                    <td>
                      <span class="score excellent">92%</span>
                    </td>
                    <td>
                      <span class="issue-count critical">1</span>
                    </td>
                    <td>
                      <span class="issue-count warning">8</span>
                    </td>
                    <td>5 hours ago</td>
                    <td>
                      <span class="trend up">
                        <i class="pi pi-arrow-up"></i>
                        +2%
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td>Frontend-Portal</td>
                    <td>28</td>
                    <td>
                      <span class="score average">76%</span>
                    </td>
                    <td>
                      <span class="issue-count critical">7</span>
                    </td>
                    <td>
                      <span class="issue-count warning">18</span>
                    </td>
                    <td>1 day ago</td>
                    <td>
                      <span class="trend down">
                        <i class="pi pi-arrow-down"></i>
                        -3%
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./reports.component.scss']
})
export class ReportsComponent {
  
}