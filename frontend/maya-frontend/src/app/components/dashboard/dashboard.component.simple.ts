import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';

import { DashboardService } from '../../services/dashboard.service';
import { 
  CodeReview, 
  ReviewStatus, 
  DashboardStats, 
  TfsConnectionConfig, 
  CommitAnalysisRequest 
} from '../../models/code-review.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <!-- Hero Section -->
    <section class="hero">
      <div class="container">
        <div class="hero-content">
          <h1 class="hero-title">MAYA Code Review System</h1>
          <p class="hero-subtitle">
            Automated code analysis with AI-powered insights for better code quality and faster development cycles.
          </p>
          <div class="hero-actions">
            <button class="btn-primary" (click)="showTfsConnectionDialog()">
              <i class="pi pi-server"></i>
              Connect to TFS
            </button>
            <button class="btn-secondary" (click)="showCommitAnalysisDialog()">
              <i class="pi pi-download"></i>
              Import Commit
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats Grid -->
    <section class="stats">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-card" [class.loading]="isLoadingStats">
            <div class="stat-icon">
              <i class="pi pi-file-o"></i>
            </div>
            <div class="stat-info">
              <h3>{{ stats.totalReviews }}</h3>
              <p>Total Reviews</p>
            </div>
          </div>
          
          <div class="stat-card" [class.loading]="isLoadingStats">
            <div class="stat-icon pending">
              <i class="pi pi-clock"></i>
            </div>
            <div class="stat-info">
              <h3>{{ stats.pendingReviews }}</h3>
              <p>Pending Reviews</p>
            </div>
          </div>
          
          <div class="stat-card" [class.loading]="isLoadingStats">
            <div class="stat-icon critical">
              <i class="pi pi-exclamation-triangle"></i>
            </div>
            <div class="stat-info">
              <h3>{{ stats.criticalIssues }}</h3>
              <p>Critical Issues</p>
            </div>
          </div>
          
          <div class="stat-card" [class.loading]="isLoadingStats">
            <div class="stat-icon success">
              <i class="pi pi-chart-line"></i>
            </div>
            <div class="stat-info">
              <h3>{{ stats.averageScore }}%</h3>
              <p>Average Score</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Main Content -->
    <main class="main-content">
      <div class="container">
        <div class="content-header">
          <h2>Recent Reviews</h2>
          <div class="content-actions">
            <div class="search-box">
              <input type="text" 
                     placeholder="Search reviews..." 
                     [(ngModel)]="searchTerm"
                     (input)="filterReviews()"
                     class="search-input">
              <i class="pi pi-search search-icon"></i>
            </div>
            <select [(ngModel)]="statusFilter" (change)="filterReviews()" class="filter-select">
              <option value="">All Status</option>
              <option value="PENDING">Pending</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="COMPLETED">Completed</option>
              <option value="FAILED">Failed</option>
            </select>
            <button class="btn-secondary btn-small" (click)="refreshReviews()" [disabled]="isRefreshing">
              <i class="pi pi-refresh" [class.spinning]="isRefreshing"></i>
              Refresh
            </button>
          </div>
        </div>

        <!-- Reviews Section -->
        <div class="reviews-section">
          <!-- Loading State -->
          <div class="loading-state" *ngIf="isLoadingReviews">
            <div class="spinner"></div>
            <p>Loading reviews...</p>
          </div>

          <!-- Empty State -->
          <div class="empty-state" *ngIf="!isLoadingReviews && filteredReviews.length === 0 && !searchTerm && !statusFilter">
            <i class="pi pi-inbox empty-icon"></i>
            <h3>No Reviews Yet</h3>
            <p>Start by importing commits from TFS or analyzing new code changes.</p>
            <button class="btn-primary" (click)="showCommitAnalysisDialog()">
              <i class="pi pi-plus"></i>
              Import First Commit
            </button>
          </div>

          <!-- No Results State -->
          <div class="empty-state" *ngIf="!isLoadingReviews && filteredReviews.length === 0 && (searchTerm || statusFilter)">
            <i class="pi pi-search empty-icon"></i>
            <h3>No Results Found</h3>
            <p>Try adjusting your search criteria or filters.</p>
            <button class="btn-secondary" (click)="clearFilters()">
              <i class="pi pi-times"></i>
              Clear Filters
            </button>
          </div>

          <!-- Reviews Table -->
          <div class="reviews-table" *ngIf="!isLoadingReviews && filteredReviews.length > 0">
            <table>
              <thead>
                <tr>
                  <th>Commit</th>
                  <th>Repository</th>
                  <th>Author</th>
                  <th>Status</th>
                  <th>Score</th>
                  <th>Issues</th>
                  <th>Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let review of filteredReviews; trackBy: trackByReviewId" 
                    [class.highlight]="review.status === 'IN_PROGRESS'">
                  <td>
                    <div class="commit-info">
                      <code class="commit-sha">{{ review.commitSha.substring(0, 8) }}</code>
                      <span class="commit-message">{{ review.commitMessage }}</span>
                    </div>
                  </td>
                  <td>{{ review.repositoryName }}</td>
                  <td>
                    <div class="author-info">
                      <span class="author-name">{{ review.author }}</span>
                      <small class="author-email">{{ review.authorEmail }}</small>
                    </div>
                  </td>
                  <td>
                    <span class="tag" [class]="'tag-' + getStatusClass(review.status)">
                      <i class="pi" [class]="getStatusIcon(review.status)"></i>
                      {{ getStatusLabel(review.status) }}
                    </span>
                  </td>
                  <td>
                    <div class="score-info">
                      <span class="score" [class]="getScoreClass(review.analysisScore)" 
                            *ngIf="review.status === 'COMPLETED'">
                        {{ review.analysisScore }}%
                      </span>
                      <span class="score pending" *ngIf="review.status !== 'COMPLETED'">
                        <i class="pi pi-clock"></i>
                      </span>
                    </div>
                  </td>
                  <td>
                    <div class="issues-info">
                      <span class="issue-count critical" *ngIf="review.criticalIssues > 0">
                        <i class="pi pi-exclamation-triangle"></i>
                        {{ review.criticalIssues }}
                      </span>
                      <span class="issue-count warning" *ngIf="review.warningIssues > 0">
                        <i class="pi pi-exclamation-circle"></i>
                        {{ review.warningIssues }}
                      </span>
                      <span class="issue-count info" *ngIf="review.infoIssues > 0">
                        <i class="pi pi-info-circle"></i>
                        {{ review.infoIssues }}
                      </span>
                    </div>
                  </td>
                  <td>
                    <div class="date-info">
                      <span class="date">{{ formatDate(review.commitDate) }}</span>
                      <small class="time">{{ formatTime(review.commitDate) }}</small>
                    </div>
                  </td>
                  <td>
                    <div class="action-buttons">
                      <button class="btn-small btn-info" title="View Details">
                        <i class="pi pi-eye"></i>
                      </button>
                      <button class="btn-small btn-secondary" title="Download Report" 
                              *ngIf="review.status === 'COMPLETED'">
                        <i class="pi pi-download"></i>
                      </button>
                      <button class="btn-small btn-danger" title="Delete" (click)="deleteReview(review)">
                        <i class="pi pi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </main>

    <!-- TFS Connection Dialog -->
    <div class="modal-overlay" *ngIf="showTfsDialog" (click)="closeTfsDialog()">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h3>Connect to TFS</h3>
          <button class="close-btn" (click)="closeTfsDialog()">
            <i class="pi pi-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <form [formGroup]="tfsForm">
            <div class="form-group">
              <label>Server URL *</label>
              <input type="text" formControlName="serverUrl" placeholder="https://tfs.sinqia.com.br">
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>Project *</label>
                <input type="text" formControlName="projectName" placeholder="DriveAMnet">
              </div>
              
              <div class="form-group">
                <label>Repository *</label>
                <input type="text" formControlName="repositoryName" placeholder="DriveAMnet">
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>Username *</label>
                <input type="text" formControlName="username" placeholder="allan.luz">
              </div>
              
              <div class="form-group">
                <label>Personal Access Token *</label>
                <input type="password" formControlName="password" placeholder="Enter your PAT">
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" (click)="testTfsConnection()" [disabled]="testingConnection || tfsForm.invalid">
            <i class="pi pi-check" *ngIf="!testingConnection"></i>
            <i class="pi pi-spin pi-spinner" *ngIf="testingConnection"></i>
            Test Connection
          </button>
          <button class="btn-secondary" (click)="closeTfsDialog()">Cancel</button>
          <button class="btn-primary" (click)="connectTfs()" [disabled]="connecting || tfsForm.invalid">
            <i class="pi pi-check" *ngIf="!connecting"></i>
            <i class="pi pi-spin pi-spinner" *ngIf="connecting"></i>
            Connect
          </button>
        </div>
      </div>
    </div>

    <!-- Commit Analysis Dialog -->
    <div class="modal-overlay" *ngIf="showCommitDialog" (click)="closeCommitDialog()">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h3>Analyze Commit</h3>
          <button class="close-btn" (click)="closeCommitDialog()">
            <i class="pi pi-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <form [formGroup]="commitForm">
            <div class="form-group">
              <label>Commit SHA *</label>
              <input type="text" formControlName="commitSha" placeholder="074f45f6c9b88c4d9ac7d2c518b4e8dbc9523127">
              <small>Enter the full commit SHA hash</small>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>Project *</label>
                <input type="text" formControlName="projectName" placeholder="DriveAMnet">
              </div>
              
              <div class="form-group">
                <label>Repository *</label>
                <input type="text" formControlName="repositoryName" placeholder="DriveAMnet">
              </div>
            </div>
            
            <div class="form-group">
              <label>Analysis Options</label>
              <div class="checkbox-group">
                <input type="checkbox" formControlName="analyzeCode" id="analyzeCode">
                <label for="analyzeCode">Analyze Code Quality</label>
              </div>
              
              <div class="checkbox-group">
                <input type="checkbox" formControlName="postToTfs" id="postToTfs">
                <label for="postToTfs">Post Results to TFS</label>
              </div>
              
              <div class="checkbox-group">
                <input type="checkbox" formControlName="analyzeConnections" id="analyzeConnections">
                <label for="analyzeConnections">Analyze Database Connections</label>
              </div>
              
              <div class="checkbox-group">
                <input type="checkbox" formControlName="analyzeTypeChanges" id="analyzeTypeChanges">
                <label for="analyzeTypeChanges">Analyze Type Changes</label>
              </div>
              
              <div class="checkbox-group">
                <input type="checkbox" formControlName="analyzeValidations" id="analyzeValidations">
                <label for="analyzeValidations">Analyze Validations</label>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" (click)="importCommitOnly()" [disabled]="importing || commitForm.invalid">
            <i class="pi pi-download" *ngIf="!importing"></i>
            <i class="pi pi-spin pi-spinner" *ngIf="importing"></i>
            Import Only
          </button>
          <button class="btn-secondary" (click)="closeCommitDialog()">Cancel</button>
          <button class="btn-primary" (click)="analyzeCommit()" [disabled]="analyzing || commitForm.invalid">
            <i class="pi pi-cog" *ngIf="!analyzing"></i>
            <i class="pi pi-spin pi-spinner" *ngIf="analyzing"></i>
            Analyze
          </button>
        </div>
      </div>
    </div>

    <!-- Toast Notifications -->
    <div class="toast-container">
      <div class="toast" *ngFor="let toast of toasts" [class]="'toast-' + toast.type">
        <i class="pi" [class]="getToastIcon(toast.type)"></i>
        <div class="toast-content">
          <strong>{{ toast.title }}</strong>
          <p>{{ toast.message }}</p>
        </div>
        <button class="toast-close" (click)="removeToast(toast)">
          <i class="pi pi-times"></i>
        </button>
      </div>
    </div>
  `,
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Data
  reviews: CodeReview[] = [];
  filteredReviews: CodeReview[] = [];
  stats: DashboardStats = {
    totalReviews: 0,
    pendingReviews: 0,
    completedReviews: 0,
    failedReviews: 0,
    criticalIssues: 0,
    averageScore: 0,
    reviewsThisWeek: 0,
    reviewsThisMonth: 0
  };

  // Loading states
  isLoadingReviews = true;
  isLoadingStats = true;
  isRefreshing = false;
  testingConnection = false;
  connecting = false;
  importing = false;
  analyzing = false;

  // Dialog states
  showTfsDialog = false;
  showCommitDialog = false;

  // Forms
  tfsForm!: FormGroup;
  commitForm!: FormGroup;

  // Filters
  searchTerm = '';
  statusFilter = '';

  // Notifications
  toasts: Array<{id: number, type: string, title: string, message: string}> = [];
  private toastId = 0;

  constructor(
    private dashboardService: DashboardService,
    private fb: FormBuilder
  ) {
    this.initializeForms();
  }

  ngOnInit() {
    this.loadData();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeForms() {
    this.tfsForm = this.fb.group({
      serverUrl: ['https://tfs.sinqia.com.br', Validators.required],
      projectName: ['DriveAMnet', Validators.required],
      repositoryName: ['DriveAMnet', Validators.required],
      username: ['allan.luz', Validators.required],
      password: ['', Validators.required]
    });

    this.commitForm = this.fb.group({
      commitSha: ['', Validators.required],
      projectName: ['DriveAMnet', Validators.required],
      repositoryName: ['DriveAMnet', Validators.required],
      analyzeCode: [true],
      postToTfs: [false],
      analyzeConnections: [true],
      analyzeTypeChanges: [true],
      analyzeValidations: [true]
    });
  }

  private loadData() {
    // Load reviews
    this.dashboardService.getReviews()
      .pipe(takeUntil(this.destroy$))
      .subscribe(reviews => {
        this.reviews = reviews;
        this.filterReviews();
        this.isLoadingReviews = false;
      });

    // Load stats
    this.dashboardService.getStats()
      .pipe(takeUntil(this.destroy$))
      .subscribe(stats => {
        this.stats = stats;
        this.isLoadingStats = false;
      });
  }

  // Filter and search methods
  filterReviews() {
    let filtered = [...this.reviews];

    // Apply search filter
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(review => 
        review.commitSha.toLowerCase().includes(term) ||
        review.commitMessage.toLowerCase().includes(term) ||
        review.author.toLowerCase().includes(term) ||
        review.repositoryName.toLowerCase().includes(term)
      );
    }

    // Apply status filter
    if (this.statusFilter) {
      filtered = filtered.filter(review => review.status === this.statusFilter);
    }

    this.filteredReviews = filtered;
  }

  clearFilters() {
    this.searchTerm = '';
    this.statusFilter = '';
    this.filterReviews();
  }

  refreshReviews() {
    this.isRefreshing = true;
    this.dashboardService.refreshData()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.isRefreshing = false;
        this.showToast('success', 'Refreshed', 'Reviews updated successfully');
      });
  }

  // Dialog methods
  showTfsConnectionDialog() {
    this.showTfsDialog = true;
  }

  closeTfsDialog() {
    this.showTfsDialog = false;
    this.testingConnection = false;
    this.connecting = false;
  }

  showCommitAnalysisDialog() {
    this.showCommitDialog = true;
  }

  closeCommitDialog() {
    this.showCommitDialog = false;
    this.importing = false;
    this.analyzing = false;
  }

  // TFS methods
  testTfsConnection() {
    if (this.tfsForm.valid) {
      this.testingConnection = true;
      const config = this.tfsForm.value as TfsConnectionConfig;
      
      this.dashboardService.testTfsConnection(config)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (success) => {
            this.testingConnection = false;
            if (success) {
              this.showToast('success', 'Connection Successful', 'TFS connection established successfully');
            } else {
              this.showToast('error', 'Connection Failed', 'Unable to connect to TFS server');
            }
          },
          error: () => {
            this.testingConnection = false;
            this.showToast('error', 'Connection Error', 'Failed to test TFS connection');
          }
        });
    }
  }

  connectTfs() {
    if (this.tfsForm.valid) {
      this.connecting = true;
      const config = this.tfsForm.value as TfsConnectionConfig;
      
      this.dashboardService.connectToTfs(config)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (success) => {
            this.connecting = false;
            if (success) {
              this.closeTfsDialog();
              this.showToast('success', 'Connected', 'Successfully connected to TFS');
            } else {
              this.showToast('error', 'Connection Failed', 'Unable to establish TFS connection');
            }
          },
          error: () => {
            this.connecting = false;
            this.showToast('error', 'Connection Error', 'Failed to connect to TFS');
          }
        });
    }
  }

  // Analysis methods
  importCommitOnly() {
    if (this.commitForm.valid) {
      this.importing = true;
      const request = this.commitForm.value as CommitAnalysisRequest;
      request.analyzeCode = false; // Import only, no analysis
      
      this.dashboardService.analyzeCommit(request)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.importing = false;
            this.closeCommitDialog();
            this.showToast('success', 'Import Started', 'Commit imported successfully');
          },
          error: () => {
            this.importing = false;
            this.showToast('error', 'Import Failed', 'Failed to import commit');
          }
        });
    }
  }

  analyzeCommit() {
    if (this.commitForm.valid) {
      this.analyzing = true;
      const request = this.commitForm.value as CommitAnalysisRequest;
      
      this.dashboardService.analyzeCommit(request)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.analyzing = false;
            this.closeCommitDialog();
            this.showToast('success', 'Analysis Started', 'Commit analysis initiated successfully');
          },
          error: () => {
            this.analyzing = false;
            this.showToast('error', 'Analysis Failed', 'Failed to start commit analysis');
          }
        });
    }
  }

  deleteReview(review: CodeReview) {
    if (confirm(`Are you sure you want to delete the review for commit ${review.commitSha.substring(0, 8)}?`)) {
      this.dashboardService.deleteReview(review.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          this.showToast('success', 'Deleted', 'Review deleted successfully');
        });
    }
  }

  // Utility methods
  trackByReviewId(index: number, review: CodeReview): string {
    return review.id;
  }

  getStatusClass(status: ReviewStatus): string {
    switch (status) {
      case ReviewStatus.COMPLETED: return 'success';
      case ReviewStatus.FAILED: return 'danger';
      case ReviewStatus.IN_PROGRESS: return 'warning';
      case ReviewStatus.PENDING: return 'info';
      default: return 'secondary';
    }
  }

  getStatusIcon(status: ReviewStatus): string {
    switch (status) {
      case ReviewStatus.COMPLETED: return 'pi-check';
      case ReviewStatus.FAILED: return 'pi-times';
      case ReviewStatus.IN_PROGRESS: return 'pi-spin pi-spinner';
      case ReviewStatus.PENDING: return 'pi-clock';
      default: return 'pi-question';
    }
  }

  getStatusLabel(status: ReviewStatus): string {
    switch (status) {
      case ReviewStatus.COMPLETED: return 'Completed';
      case ReviewStatus.FAILED: return 'Failed';
      case ReviewStatus.IN_PROGRESS: return 'In Progress';
      case ReviewStatus.PENDING: return 'Pending';
      default: return 'Unknown';
    }
  }

  getScoreClass(score: number): string {
    if (score >= 90) return 'excellent';
    if (score >= 80) return 'good';
    if (score >= 70) return 'average';
    return 'poor';
  }

  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('pt-BR', { 
      day: '2-digit', 
      month: '2-digit', 
      year: 'numeric' 
    }).format(new Date(date));
  }

  formatTime(date: Date): string {
    return new Intl.DateTimeFormat('pt-BR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    }).format(new Date(date));
  }

  // Toast methods
  showToast(type: string, title: string, message: string) {
    const toast = {
      id: ++this.toastId,
      type,
      title,
      message
    };
    this.toasts.push(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
      this.removeToast(toast);
    }, 5000);
  }

  removeToast(toast: any) {
    const index = this.toasts.findIndex(t => t.id === toast.id);
    if (index > -1) {
      this.toasts.splice(index, 1);
    }
  }

  getToastIcon(type: string): string {
    switch (type) {
      case 'success': return 'pi-check-circle';
      case 'error': return 'pi-times-circle';
      case 'warning': return 'pi-exclamation-triangle';
      case 'info': return 'pi-info-circle';
      default: return 'pi-info-circle';
    }
  }
}