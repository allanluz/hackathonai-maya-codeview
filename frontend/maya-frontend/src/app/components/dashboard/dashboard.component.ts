import { Component, OnInit, OnDestroy, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
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
export class DashboardComponent implements OnInit, OnDestroy, AfterViewInit {
  private destroy$ = new Subject<void>();
  
  @ViewChild('backgroundVideo', { static: false }) backgroundVideo!: ElementRef<HTMLVideoElement>;

  // Dashboard data
  dashboardData: DashboardData | null = null;
  analysisSummary: AnalysisSummary | null = null;
  chartData: ChartData[] | null = null;
  isLoading = true;
  lastUpdate: Date | null = null;

  constructor(
    private mayaApiService: MayaApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
    this.generateMockChartData();
    this.generateMockSummary();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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

  private loadDashboardData(): void {
    this.isLoading = true;
    
    this.mayaApiService.getDashboard()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: any) => {
          this.dashboardData = data;
          this.lastUpdate = new Date();
          this.isLoading = false;
        },
        error: (error: any) => {
          console.error('Error loading dashboard data:', error);
          this.generateMockData();
          this.isLoading = false;
        }
      });
  }

  private generateMockData(): void {
    this.dashboardData = {
      totalReviews: 145,
      activeReviews: 23,
      completedToday: 8,
      averageScore: 87,
      connectionLeaksDetected: 3,
      evertecStandardsCompliance: 92,
      lastUpdate: new Date().toISOString(),
      services: {
        database: 'online',
        tfs: 'online',
        evertecAI: 'online'
      }
    };
    this.lastUpdate = new Date();
  }

  private generateMockChartData(): void {
    this.chartData = [
      { label: 'Seg', value: 75, count: 12 },
      { label: 'Ter', value: 45, count: 8 },
      { label: 'Qua', value: 90, count: 15 },
      { label: 'Qui', value: 60, count: 10 },
      { label: 'Sex', value: 100, count: 18 },
      { label: 'Sáb', value: 30, count: 5 },
      { label: 'Dom', value: 20, count: 3 }
    ];
  }

  private generateMockSummary(): void {
    this.analysisSummary = {
      filesAnalyzed: 248,
      totalFiles: 312,
      issuesFound: 45,
      criticalIssues: 3,
      evertecPatternViolations: 12,
      connectionLeaks: 3,
      topIssues: {},
      generatedAt: new Date().toISOString()
    };
  }

  getServicesList(): ServiceStatus[] {
    return [
      {
        name: 'TFS Integration',
        status: 'Online',
        icon: 'cloud',
        iconClass: 'success',
        statusClass: 'online'
      },
      {
        name: 'AI Analysis',
        status: 'Online', 
        icon: 'psychology',
        iconClass: 'success',
        statusClass: 'online'
      },
      {
        name: 'Maya Engine',
        status: 'Online',
        icon: 'settings',
        iconClass: 'success', 
        statusClass: 'online'
      }
    ];
  }

  getStandardsIcon(): string {
    const compliance = this.dashboardData?.evertecStandardsCompliance || 0;
    return compliance >= 80 ? 'check_circle' : compliance >= 60 ? 'warning' : 'error';
  }

  getStandardsIconClass(): string {
    const compliance = this.dashboardData?.evertecStandardsCompliance || 0;
    return compliance >= 80 ? 'success' : compliance >= 60 ? 'warning' : 'error';
  }

  navigateToReviews(): void {
    this.router.navigate(['/reviews']);
  }

  startNewReview(): void {
    // Implementar lógica para iniciar novo review
    console.log('Starting new review...');
  }
}