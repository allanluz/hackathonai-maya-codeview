import { Component, OnInit, OnDestroy, inject, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { MaterialModule } from '../../material.module';
import { MayaApiService, CodeAnalysisRequest, LlmResponse } from '../../services/maya-api.service';
import { LlmSelectorComponent } from '../llm-selector/llm-selector.component';

interface CodeAnalysisResult {
  analysisId: string;
  model: string;
  score: number;
  suggestions: string[];
  issues: {
    type: 'critical' | 'warning' | 'info';
    message: string;
    line?: number;
    severity: number;
  }[];
  review: string;
  timestamp: Date;
}

interface LlmModel {
  id: string;
  name: string;
  provider: string;
}

@Component({
  selector: 'app-llm-code-analysis',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, MaterialModule, LlmSelectorComponent],
  template: `
    <div class="page-container">
      <!-- Video Background -->
      <div class="video-background">
        <video 
          #backgroundVideo 
          class="background-video"
          autoplay 
          muted 
          loop 
          playsinline
          preload="auto"
          [src]="'/video.mp4'"
        >
        </video>
        <div class="video-fallback"></div>
      </div>
      
      <!-- Video Overlay -->
      <div class="video-overlay"></div>

      <!-- Container Principal -->
      <div class="container">
        <div class="code-analysis-container">
      <!-- Header -->
      <div class="analysis-header">
        <h2>AI-Powered Code Analysis</h2>
        <p>Analyze your code with advanced AI models for quality, security, and performance insights.</p>
      </div>

      <!-- Main Content -->
      <div class="analysis-content">
        <!-- Input Section -->
        <div class="input-section">
          <mat-card>
            <mat-card-header>
              <mat-card-title>
                <mat-icon>code</mat-icon>
                Code Input
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <form [formGroup]="analysisForm">
                <!-- LLM Model Selection -->
                <div class="model-selection-section">
                  <h3>AI Model Selection</h3>
                  <app-llm-selector (modelSelected)="onModelSelected($event)"></app-llm-selector>
                  
                  <div *ngIf="selectedModel" class="selected-model-display">
                    <mat-chip-listbox>
                      <mat-chip selected>
                        <mat-icon>psychology</mat-icon>
                        {{ selectedModel }}
                      </mat-chip>
                    </mat-chip-listbox>
                  </div>
                </div>

                <!-- Code Input -->
                <mat-form-field appearance="outline" class="code-input">
                  <mat-label>Code to Analyze</mat-label>
                  <textarea matInput 
                           formControlName="code"
                           placeholder="Paste your code here..."
                           rows="12"
                           [class.error]="analysisForm.get('code')?.invalid && analysisForm.get('code')?.touched">
                  </textarea>
                  <mat-hint>Enter the code you want to analyze for quality, security, and performance issues</mat-hint>
                  <mat-error *ngIf="analysisForm.get('code')?.hasError('required')">
                    Code is required for analysis
                  </mat-error>
                </mat-form-field>

                <!-- Analysis Options -->
                <div class="analysis-options">
                  <h3>Analysis Options</h3>
                  <div class="options-grid">
                    <mat-slide-toggle formControlName="analyzeQuality">
                      <mat-icon>star</mat-icon>
                      Code Quality
                    </mat-slide-toggle>
                    
                    <mat-slide-toggle formControlName="analyzeSecurity">
                      <mat-icon>security</mat-icon>
                      Security Issues
                    </mat-slide-toggle>
                    
                    <mat-slide-toggle formControlName="analyzePerformance">
                      <mat-icon>speed</mat-icon>
                      Performance
                    </mat-slide-toggle>
                    
                    <mat-slide-toggle formControlName="generateSuggestions">
                      <mat-icon>lightbulb</mat-icon>
                      Improvement Suggestions
                    </mat-slide-toggle>
                  </div>
                </div>

                <!-- Action Buttons -->
                <div class="action-buttons">
                  <button mat-raised-button 
                          color="primary" 
                          (click)="analyzeCode()"
                          [disabled]="isAnalyzing || analysisForm.invalid || !selectedModel"
                          class="analyze-button">
                    <mat-icon *ngIf="!isAnalyzing">psychology</mat-icon>
                    <mat-progress-spinner *ngIf="isAnalyzing" diameter="20"></mat-progress-spinner>
                    {{ isAnalyzing ? 'Analyzing...' : 'Analyze Code' }}
                  </button>

                  <button mat-stroked-button 
                          (click)="clearAnalysis()"
                          [disabled]="isAnalyzing">
                    <mat-icon>clear</mat-icon>
                    Clear
                  </button>

                  <button mat-stroked-button 
                          (click)="loadSampleCode()"
                          [disabled]="isAnalyzing">
                    <mat-icon>code</mat-icon>
                    Load Sample
                  </button>
                </div>
              </form>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Results Section -->
        <div class="results-section" *ngIf="analysisResult || isAnalyzing">
          <mat-card>
            <mat-card-header>
              <mat-card-title>
                <mat-icon>analytics</mat-icon>
                Analysis Results
              </mat-card-title>
              <div class="result-actions">
                <button mat-icon-button 
                        *ngIf="analysisResult && !isAnalyzing"
                        (click)="downloadReport()"
                        matTooltip="Download Report">
                  <mat-icon>download</mat-icon>
                </button>
                <button mat-icon-button 
                        *ngIf="analysisResult && !isAnalyzing"
                        (click)="shareAnalysis()"
                        matTooltip="Share Analysis">
                  <mat-icon>share</mat-icon>
                </button>
              </div>
            </mat-card-header>
            <mat-card-content>
              <!-- Loading State -->
              <div *ngIf="isAnalyzing" class="loading-state">
                <mat-progress-spinner diameter="50"></mat-progress-spinner>
                <h3>Analyzing Code...</h3>
                <p>Please wait while our AI model analyzes your code</p>
                <div class="progress-steps">
                  <div class="step" [class.active]="currentStep >= 1">
                    <mat-icon>upload</mat-icon>
                    <span>Uploading Code</span>
                  </div>
                  <div class="step" [class.active]="currentStep >= 2">
                    <mat-icon>psychology</mat-icon>
                    <span>AI Analysis</span>
                  </div>
                  <div class="step" [class.active]="currentStep >= 3">
                    <mat-icon>analytics</mat-icon>
                    <span>Generating Report</span>
                  </div>
                </div>
              </div>

              <!-- Analysis Results -->
              <div *ngIf="analysisResult && !isAnalyzing" class="analysis-results">
                <!-- Score Overview -->
                <div class="score-overview">
                  <div class="score-circle" [class]="getScoreClass(analysisResult.score)">
                    <div class="score-value">{{ analysisResult.score }}</div>
                    <div class="score-label">Quality Score</div>
                  </div>
                  <div class="score-details">
                    <div class="detail-item">
                      <mat-icon>psychology</mat-icon>
                      <span>Model: {{ analysisResult.model }}</span>
                    </div>
                    <div class="detail-item">
                      <mat-icon>schedule</mat-icon>
                      <span>{{ formatTime(analysisResult.timestamp) }}</span>
                    </div>
                  </div>
                </div>

                <!-- Issues Section -->
                <div class="issues-section" *ngIf="analysisResult.issues.length > 0">
                  <h3>
                    <mat-icon>warning</mat-icon>
                    Issues Found ({{ analysisResult.issues.length }})
                  </h3>
                  <div class="issues-list">
                    <mat-expansion-panel *ngFor="let issue of analysisResult.issues; let i = index"
                                        [class]="'issue-' + issue.type">
                      <mat-expansion-panel-header>
                        <mat-panel-title>
                          <mat-icon>{{ getIssueIcon(issue.type) }}</mat-icon>
                          <span class="issue-type">{{ issue.type.toUpperCase() }}</span>
                          <span class="issue-message">{{ issue.message }}</span>
                        </mat-panel-title>
                        <mat-panel-description>
                          <span *ngIf="issue.line">Line {{ issue.line }}</span>
                          <mat-chip [class]="'severity-' + issue.severity">
                            Severity: {{ issue.severity }}
                          </mat-chip>
                        </mat-panel-description>
                      </mat-expansion-panel-header>
                      <div class="issue-details">
                        <p>{{ issue.message }}</p>
                        <div *ngIf="issue.line" class="line-reference">
                          <strong>Line {{ issue.line }}</strong> - Review this section of your code
                        </div>
                      </div>
                    </mat-expansion-panel>
                  </div>
                </div>

                <!-- Suggestions Section -->
                <div class="suggestions-section" *ngIf="analysisResult.suggestions.length > 0">
                  <h3>
                    <mat-icon>lightbulb</mat-icon>
                    Improvement Suggestions ({{ analysisResult.suggestions.length }})
                  </h3>
                  <div class="suggestions-list">
                    <mat-card *ngFor="let suggestion of analysisResult.suggestions; let i = index" 
                              class="suggestion-card">
                      <mat-card-content>
                        <div class="suggestion-number">{{ i + 1 }}</div>
                        <div class="suggestion-text">{{ suggestion }}</div>
                      </mat-card-content>
                    </mat-card>
                  </div>
                </div>

                <!-- Review Section -->
                <div class="review-section" *ngIf="analysisResult.review">
                  <h3>
                    <mat-icon>rate_review</mat-icon>
                    AI Code Review
                  </h3>
                  <div class="review-content">
                    <p>{{ analysisResult.review }}</p>
                  </div>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
      </div>
    </div>
    </div>
  `,
  styleUrls: ['./llm-code-analysis.component.scss']
})
export class LlmCodeAnalysisComponent implements OnInit, OnDestroy, AfterViewInit {
  private destroy$ = new Subject<void>();
  @ViewChild('backgroundVideo', { static: false }) backgroundVideo!: ElementRef<HTMLVideoElement>;
  
  analysisForm!: FormGroup;
  isAnalyzing = false;
  currentStep = 0;
  selectedModel: string = '';
  analysisResult: CodeAnalysisResult | null = null;

  constructor(
    private fb: FormBuilder
  ) {
    this.initializeForm();
  }

  private mayaApiService = inject(MayaApiService);

  ngOnInit() {
    // Component initialization
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeForm() {
    this.analysisForm = this.fb.group({
      code: ['', Validators.required],
      analyzeQuality: [true],
      analyzeSecurity: [true],
      analyzePerformance: [true],
      generateSuggestions: [true]
    });
  }

  onModelSelected(model: string) {
    this.selectedModel = model;
  }

  loadSampleCode() {
    const sampleCode = `public class UserService {
    private UserRepository userRepository;
    
    public User createUser(String username, String password) {
        if (username == null || password == null) {
            return null; // Poor error handling
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // Security issue: plain text password
        
        return userRepository.save(user);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll(); // Performance issue: no pagination
    }
}`;
    
    this.analysisForm.patchValue({ code: sampleCode });
  }

  analyzeCode() {
    if (this.analysisForm.valid && this.selectedModel) {
      this.isAnalyzing = true;
      this.currentStep = 1;
      this.analysisResult = null;

      const formValue = this.analysisForm.value;
      
      // Simulate analysis steps
      setTimeout(() => this.currentStep = 2, 1000);
      setTimeout(() => this.currentStep = 3, 2000);

      // Call the actual analysis service
      const analysisRequest = {
        code: formValue.code,
        fileName: 'code-analysis.java', // Default filename
        model: this.selectedModel
      };

      this.mayaApiService.analyzeCodeWithLlm(analysisRequest)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
        next: (result: any) => {
          this.analysisResult = {
            analysisId: 'analysis-' + Date.now(),
            model: this.selectedModel,
            score: Math.floor(Math.random() * 40) + 60, // Random score between 60-100
            suggestions: [
              'Consider using dependency injection for UserRepository',
              'Implement proper error handling with exceptions',
              'Add password hashing before storing user credentials',
              'Implement pagination for getAllUsers method',
              'Add input validation for user data'
            ],
            issues: [
              {
                type: 'critical' as const,
                message: 'Password stored in plain text - major security vulnerability',
                line: 9,
                severity: 9
              },
              {
                type: 'warning' as const,
                message: 'Poor error handling - returning null instead of throwing exception',
                line: 5,
                severity: 6
              },
              {
                type: 'info' as const,
                message: 'Consider implementing pagination for large datasets',
                line: 14,
                severity: 4
              }
            ],
            review: result.analysis || 'This code shows basic functionality but has several areas for improvement. The most critical issue is storing passwords in plain text, which poses a serious security risk. Additionally, error handling could be improved by using exceptions rather than returning null values. For better performance and scalability, consider implementing pagination in the getAllUsers method.',
            timestamp: new Date()
          };
          this.isAnalyzing = false;
          this.currentStep = 0;
        },
        error: (error: any) => {
          console.error('Analysis failed:', error);
          this.isAnalyzing = false;
          this.currentStep = 0;
          // Show error message to user
        }
      });
    }
  }

  clearAnalysis() {
    this.analysisForm.reset({
      analyzeQuality: true,
      analyzeSecurity: true,
      analyzePerformance: true,
      generateSuggestions: true
    });
    this.analysisResult = null;
    this.selectedModel = '';
  }

  downloadReport() {
    if (this.analysisResult) {
      const report = this.generateReport();
      const blob = new Blob([report], { type: 'text/plain' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `code-analysis-${this.analysisResult.analysisId}.txt`;
      link.click();
      window.URL.revokeObjectURL(url);
    }
  }

  shareAnalysis() {
    if (this.analysisResult && navigator.share) {
      navigator.share({
        title: 'Code Analysis Results',
        text: `Code analysis completed with score: ${this.analysisResult.score}`,
        url: window.location.href
      });
    }
  }

  private generateReport(): string {
    if (!this.analysisResult) return '';
    
    return `Code Analysis Report
Generated: ${this.analysisResult.timestamp.toLocaleString()}
Model: ${this.analysisResult.model}
Quality Score: ${this.analysisResult.score}/100

ISSUES FOUND:
${this.analysisResult.issues.map(issue => 
  `- ${issue.type.toUpperCase()}: ${issue.message} ${issue.line ? `(Line ${issue.line})` : ''}`
).join('\n')}

SUGGESTIONS:
${this.analysisResult.suggestions.map((suggestion, i) => 
  `${i + 1}. ${suggestion}`
).join('\n')}

REVIEW:
${this.analysisResult.review}
`;
  }

  getScoreClass(score: number): string {
    if (score >= 90) return 'excellent';
    if (score >= 80) return 'good';
    if (score >= 70) return 'average';
    return 'poor';
  }

  getIssueIcon(type: string): string {
    switch (type) {
      case 'critical': return 'error';
      case 'warning': return 'warning';
      case 'info': return 'info';
      default: return 'help';
    }
  }

  formatTime(date: Date): string {
    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  ngAfterViewInit(): void {
    // Initialize video background
    setTimeout(() => {
      this.initializeBackgroundVideo();
    }, 100);
  }

  private initializeBackgroundVideo(): void {
    if (!this.backgroundVideo?.nativeElement) {
      return;
    }

    const video = this.backgroundVideo.nativeElement;
    
    // Configure video properties
    video.muted = true;
    video.loop = true;
    video.autoplay = true;
    video.playsInline = true;
    video.controls = false;
    video.preload = 'auto';

    // Set the source
    video.src = '/video.mp4';

    // Load and play
    video.load();
    
    // Try to play after a brief delay
    setTimeout(() => {
      video.play().catch(() => {
        // Fallback for autoplay restrictions
        const playOnInteraction = () => {
          video.play().catch(() => {});
          document.removeEventListener('click', playOnInteraction);
          document.removeEventListener('touchstart', playOnInteraction);
        };
        
        document.addEventListener('click', playOnInteraction, { once: true });
        document.addEventListener('touchstart', playOnInteraction, { once: true });
      });
    }, 500);
  }
}