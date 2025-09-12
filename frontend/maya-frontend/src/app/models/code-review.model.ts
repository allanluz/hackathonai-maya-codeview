export interface CodeReview {
  id: string;
  commitSha: string;
  repositoryName: string;
  projectName: string;
  author: string;
  authorEmail: string;
  commitMessage: string;
  commitDate: Date;
  status: ReviewStatus;
  analysisScore: number;
  criticalIssues: number;
  warningIssues: number;
  infoIssues: number;
  linesAdded: number;
  linesRemoved: number;
  filesChanged: number;
  createdAt: Date;
  updatedAt: Date;
  analysisData?: AnalysisData;
}

export enum ReviewStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

export interface AnalysisData {
  codeQuality: {
    maintainability: number;
    reliability: number;
    security: number;
    performance: number;
  };
  issues: Issue[];
  metrics: {
    complexity: number;
    coverage: number;
    duplication: number;
  };
}

export interface Issue {
  id: string;
  type: IssueType;
  severity: IssueSeverity;
  message: string;
  file: string;
  line: number;
  column?: number;
  rule?: string;
}

export enum IssueType {
  CODE_SMELL = 'CODE_SMELL',
  BUG = 'BUG',
  VULNERABILITY = 'VULNERABILITY',
  SECURITY_HOTSPOT = 'SECURITY_HOTSPOT'
}

export enum IssueSeverity {
  CRITICAL = 'CRITICAL',
  MAJOR = 'MAJOR',
  MINOR = 'MINOR',
  INFO = 'INFO'
}

export interface DashboardStats {
  totalReviews: number;
  pendingReviews: number;
  completedReviews: number;
  failedReviews: number;
  criticalIssues: number;
  averageScore: number;
  reviewsThisWeek: number;
  reviewsThisMonth: number;
}

export interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    backgroundColor?: string[];
    borderColor?: string[];
    borderWidth?: number;
  }[];
}

export interface TfsConnectionConfig {
  serverUrl: string;
  projectName: string;
  repositoryName: string;
  username: string;
  password: string;
}

export interface CommitAnalysisRequest {
  commitSha: string;
  projectName: string;
  repositoryName: string;
  analyzeCode: boolean;
  postToTfs: boolean;
  analyzeConnections: boolean;
  analyzeTypeChanges: boolean;
  analyzeValidations: boolean;
}