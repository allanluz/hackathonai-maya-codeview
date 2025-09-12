import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RepositoryService, Repository, ConnectRepositoryRequest } from '../../services/repository.service';

@Component({
  selector: 'app-connect-repository-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
    MatDialogModule
  ],
  template: `
    <h2 mat-dialog-title>Conectar Repositório</h2>
    <mat-dialog-content class="dialog-content-scroll">
      <form [formGroup]="connectForm" class="connect-form">
        <mat-form-field appearance="outline">
          <mat-label>Nome</mat-label>
          <input matInput formControlName="name" placeholder="Nome do repositório">
          <mat-error *ngIf="connectForm.get('name')?.hasError('required')">
            Nome é obrigatório
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Tipo</mat-label>
          <mat-select formControlName="type">
            <mat-option value="GITHUB">GitHub</mat-option>
            <mat-option value="TFS">TFS</mat-option>
            <mat-option value="AZURE_DEVOPS">Azure DevOps</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>URL do Repositório</mat-label>
          <input matInput formControlName="url" placeholder="https://github.com/org/repo">
          <mat-error *ngIf="connectForm.get('url')?.hasError('required')">
            URL é obrigatória
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Organização</mat-label>
          <input matInput formControlName="organizationName" placeholder="Nome da organização">
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Projeto</mat-label>
          <input matInput formControlName="projectName" placeholder="Nome do projeto">
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Token de Acesso</mat-label>
          <input matInput type="password" formControlName="accessToken" placeholder="Token de autenticação">
          <mat-error *ngIf="connectForm.get('accessToken')?.hasError('required')">
            Token é obrigatório
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Branch Padrão</mat-label>
          <input matInput formControlName="defaultBranch" placeholder="main" value="main">
        </mat-form-field>

        <div class="toggle-field">
          <mat-slide-toggle formControlName="autoReviewEnabled">
            Revisão Automática Habilitada
          </mat-slide-toggle>
        </div>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end" class="dialog-actions">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-raised-button color="primary" [disabled]="connectForm.invalid || connecting" (click)="connect()">
        <mat-icon *ngIf="connecting">hourglass_empty</mat-icon>
        {{ connecting ? 'Conectando...' : 'Conectar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-content-scroll {
      max-height: 70vh;
      overflow-y: auto;
      padding: 0 !important;
    }

    .connect-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
      width: 100%;
      max-width: 500px;
      min-width: 280px;
      padding: 16px 0;
    }

    .toggle-field {
      margin: 16px 0;
    }

    .dialog-actions {
      flex-wrap: wrap;
      gap: 8px;
    }

    .dialog-actions button {
      min-width: 80px;
    }

    @media (max-width: 768px) {
      .connect-form {
        min-width: 250px;
      }

      .dialog-content-scroll {
        max-height: 60vh;
      }

      .dialog-actions {
        flex-direction: column-reverse;
      }

      .dialog-actions button {
        width: 100%;
        margin: 4px 0;
      }
    }

    @media (max-width: 480px) {
      .connect-form {
        min-width: 200px;
      }

      .dialog-content-scroll {
        max-height: 50vh;
      }
    }

    /* Scrollbar customization */
    .dialog-content-scroll::-webkit-scrollbar {
      width: 6px;
    }

    .dialog-content-scroll::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 3px;
    }

    .dialog-content-scroll::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 3px;
    }

    .dialog-content-scroll::-webkit-scrollbar-thumb:hover {
      background: #a1a1a1;
    }
  `]
})
export class ConnectRepositoryDialogComponent {
  connectForm: FormGroup;
  connecting = false;

  constructor(
    private fb: FormBuilder,
    private repositoryService: RepositoryService,
    private snackBar: MatSnackBar
  ) {
    this.connectForm = this.fb.group({
      name: ['', [Validators.required]],
      type: ['GITHUB', [Validators.required]],
      url: ['', [Validators.required]],
      organizationName: [''],
      projectName: [''],
      accessToken: ['', [Validators.required]],
      defaultBranch: ['main'],
      autoReviewEnabled: [false]
    });
  }

  connect(): void {
    if (this.connectForm.valid) {
      this.connecting = true;
      const request: ConnectRepositoryRequest = this.connectForm.value;

      this.repositoryService.connectRepository(request).subscribe({
        next: (response) => {
          this.connecting = false;
          if (response.success) {
            this.snackBar.open('Repositório conectado com sucesso!', 'Fechar', { duration: 3000 });
            // TODO: Fechar dialog e recarregar lista
          } else {
            this.snackBar.open(`Erro: ${response.message}`, 'Fechar', { duration: 5000 });
          }
        },
        error: (error) => {
          this.connecting = false;
          this.snackBar.open('Erro ao conectar repositório', 'Fechar', { duration: 5000 });
        }
      });
    }
  }
}

@Component({
  selector: 'app-repositories',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule
  ],
  template: `
    <div class="repositories-container">
      <div class="header">
        <h1>Repositórios Conectados</h1>
        <button mat-raised-button color="primary" (click)="openConnectDialog()">
          <mat-icon>add</mat-icon>
          Conectar Repositório
        </button>
      </div>

      <mat-card>
        <mat-card-content>
          <div class="table-container">
            <table mat-table [dataSource]="repositories" class="repositories-table">
              <!-- Nome -->
              <ng-container matColumnDef="name">
                <th mat-header-cell *matHeaderCellDef>Nome</th>
                <td mat-cell *matCellDef="let repo">
                  <div class="repo-info">
                    <div class="repo-name">{{ repo.name }}</div>
                    <div class="repo-url">{{ repo.url }}</div>
                  </div>
                </td>
              </ng-container>

              <!-- Tipo -->
              <ng-container matColumnDef="type">
                <th mat-header-cell *matHeaderCellDef>Tipo</th>
                <td mat-cell *matCellDef="let repo">
                  <mat-chip [class]="'type-' + repo.type.toLowerCase()">
                    {{ getTypeLabel(repo.type) }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Organização -->
              <ng-container matColumnDef="organization">
                <th mat-header-cell *matHeaderCellDef>Organização</th>
                <td mat-cell *matCellDef="let repo">{{ repo.organizationName || '-' }}</td>
              </ng-container>

              <!-- Status -->
              <ng-container matColumnDef="status">
                <th mat-header-cell *matHeaderCellDef>Status</th>
                <td mat-cell *matCellDef="let repo">
                  <mat-chip [class]="repo.isActive ? 'status-active' : 'status-inactive'">
                    <mat-icon>{{ repo.isActive ? 'check_circle' : 'pause_circle' }}</mat-icon>
                    {{ repo.isActive ? 'Ativo' : 'Inativo' }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Auto Review -->
              <ng-container matColumnDef="autoReview">
                <th mat-header-cell *matHeaderCellDef>Revisão Auto</th>
                <td mat-cell *matCellDef="let repo">
                  <mat-icon [class]="repo.autoReviewEnabled ? 'enabled' : 'disabled'">
                    {{ repo.autoReviewEnabled ? 'smart_toy' : 'smart_toy_outline' }}
                  </mat-icon>
                </td>
              </ng-container>

              <!-- Última Sync -->
              <ng-container matColumnDef="lastSync">
                <th mat-header-cell *matHeaderCellDef>Última Sync</th>
                <td mat-cell *matCellDef="let repo">
                  {{ repo.lastSyncAt ? (repo.lastSyncAt | date:'short') : 'Nunca' }}
                </td>
              </ng-container>

              <!-- Ações -->
              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Ações</th>
                <td mat-cell *matCellDef="let repo">
                  <div class="action-buttons">
                    <button mat-icon-button [matTooltip]="'Testar Conexão'" (click)="testConnection(repo)">
                      <mat-icon>wifi</mat-icon>
                    </button>
                    <button mat-icon-button [matTooltip]="'Sincronizar'" (click)="syncRepository(repo)">
                      <mat-icon>sync</mat-icon>
                    </button>
                    <button mat-icon-button [matTooltip]="'Configurar'" (click)="editRepository(repo)">
                      <mat-icon>edit</mat-icon>
                    </button>
                    <button mat-icon-button [matTooltip]="'Desconectar'" color="warn" (click)="disconnectRepository(repo)">
                      <mat-icon>delete</mat-icon>
                    </button>
                  </div>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>
          </div>

          <!-- Empty State -->
          <div class="empty-state" *ngIf="repositories.length === 0 && !loading">
            <mat-icon>folder_open</mat-icon>
            <h3>Nenhum repositório conectado</h3>
            <p>Conecte seu primeiro repositório para começar a usar o MAYA</p>
            <button mat-raised-button color="primary" (click)="openConnectDialog()">
              <mat-icon>add</mat-icon>
              Conectar Repositório
            </button>
          </div>

          <!-- Loading -->
          <div class="loading-container" *ngIf="loading">
            <mat-progress-bar mode="indeterminate"></mat-progress-bar>
            <p>Carregando repositórios...</p>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .repositories-container {
      padding: var(--space-2xl);
      max-width: 1600px;
      margin: 0 auto;
      background: var(--bg-secondary);
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--space-2xl);
      padding-bottom: var(--space-lg);
      border-bottom: 2px solid var(--color-gray-200);
    }

    .header h1 {
      font-size: 2.25rem;
      font-weight: 800;
      background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      letter-spacing: -0.02em;
    }

    .header button {
      height: 48px;
      border-radius: var(--radius-xl);
      font-weight: 600;
      box-shadow: var(--shadow-md);
      transition: all var(--animation-duration-normal) var(--animation-easing);
    }

    .header button:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-lg);
    }

    .table-container {
      background: var(--bg-surface);
      border: 1px solid var(--color-gray-200);
      border-radius: var(--radius-2xl);
      box-shadow: var(--shadow-lg);
      overflow: hidden;
    }

    .repositories-table {
      width: 100%;
      background: var(--bg-surface);
    }

    .repositories-table th {
      background: linear-gradient(135deg, var(--color-gray-50), var(--color-gray-100));
      color: var(--text-primary);
      font-weight: 600;
      border-bottom: 2px solid var(--color-gray-200);
      padding: var(--space-lg);
      font-size: 0.875rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .repositories-table td {
      padding: var(--space-lg);
      vertical-align: middle;
      border-bottom: 1px solid var(--color-gray-200);
    }

    .repositories-table tr {
      transition: all var(--animation-duration-normal) var(--animation-easing);
    }

    .repositories-table tr:hover {
      background: linear-gradient(135deg, var(--color-gray-50), var(--color-gray-100));
      transform: scale(1.005);
    }

    .repo-info {
      display: flex;
      flex-direction: column;
      gap: var(--space-xs);
    }

    .repo-name {
      font-weight: 600;
      color: var(--text-primary);
      font-size: 1rem;
    }

    .repo-url {
      font-size: 0.875rem;
      color: var(--text-secondary);
      font-family: var(--font-mono);
      background: var(--color-gray-100);
      padding: var(--space-xs) var(--space-sm);
      border-radius: var(--radius-md);
      display: inline-block;
      max-width: fit-content;
    }

    .action-buttons {
      display: flex;
      gap: var(--space-xs);
    }

    .action-buttons button {
      border-radius: var(--radius-lg);
      transition: all var(--animation-duration-normal) var(--animation-easing);
      min-width: auto;
      width: 40px;
      height: 40px;
    }

    .action-buttons button:hover {
      transform: scale(1.1);
      box-shadow: var(--shadow-sm);
    }

    /* Repository Type Chips */
    mat-chip {
      border-radius: var(--radius-lg) !important;
      font-weight: 600 !important;
      font-size: 0.75rem !important;
      border: 1px solid transparent !important;
      transition: all var(--animation-duration-normal) var(--animation-easing) !important;
    }

    mat-chip:hover {
      transform: scale(1.05) !important;
    }

    .type-github {
      background: linear-gradient(135deg, #24292e, #1a1e22) !important;
      color: var(--text-inverse) !important;
      border-color: #24292e !important;
    }

    .type-tfs {
      background: linear-gradient(135deg, #0078d4, #005a9e) !important;
      color: var(--text-inverse) !important;
      border-color: #0078d4 !important;
    }

    .type-azure_devops {
      background: linear-gradient(135deg, #0078d4, #005a9e) !important;
      color: var(--text-inverse) !important;
      border-color: #0078d4 !important;
    }

    /* Status Chips */
    .status-active {
      background: linear-gradient(135deg, var(--color-success), #059669) !important;
      color: var(--text-inverse) !important;
      border-color: var(--color-success) !important;
    }

    .status-inactive {
      background: linear-gradient(135deg, var(--color-gray-500), var(--color-gray-600)) !important;
      color: var(--text-inverse) !important;
      border-color: var(--color-gray-500) !important;
    }

    /* Auto Review Status */
    .enabled {
      color: var(--color-success);
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: var(--space-xs);
    }

    .disabled {
      color: var(--color-gray-500);
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: var(--space-xs);
    }

    .enabled mat-icon,
    .disabled mat-icon {
      font-size: 1.25rem;
      width: 1.25rem;
      height: 1.25rem;
    }

    .empty-state {
      text-align: center;
      padding: var(--space-3xl);
      color: var(--text-secondary);
      background: var(--bg-surface);
      border-radius: var(--radius-2xl);
      border: 2px dashed var(--color-gray-300);
    }

    .empty-state mat-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      margin-bottom: var(--space-lg);
      color: var(--color-gray-400);
    }

    .empty-state h3 {
      color: var(--text-primary);
      margin-bottom: var(--space-md);
      font-size: 1.5rem;
      font-weight: 600;
    }

    .empty-state p {
      margin-bottom: var(--space-lg);
      line-height: 1.6;
    }

    .empty-state button {
      height: 48px;
      border-radius: var(--radius-xl);
      font-weight: 600;
    }

    .loading-container {
      padding: var(--space-3xl);
      text-align: center;
      background: var(--bg-surface);
      border-radius: var(--radius-2xl);
      border: 1px solid var(--color-gray-200);
    }

    .loading-container mat-progress-bar {
      margin-bottom: var(--space-lg);
      border-radius: var(--radius-sm);
    }

    .loading-container p {
      color: var(--text-secondary);
      font-weight: 500;
    }

    /* Enhanced Responsive Dialog Styles */
    :host ::ng-deep .connect-repository-dialog {
      padding: 0;
    }

    :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-container {
      padding: 0;
      margin: var(--space-lg);
      border-radius: var(--radius-2xl);
      box-shadow: var(--shadow-2xl);
      border: 1px solid var(--color-gray-200);
      max-width: 90vw;
      width: 100%;
      max-width: 600px;
    }

    :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-content {
      padding: var(--space-xl);
      margin: 0;
      max-height: 70vh;
      overflow-y: auto;
    }

    :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-content::-webkit-scrollbar {
      width: 6px;
    }

    :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-content::-webkit-scrollbar-track {
      background: var(--color-gray-100);
      border-radius: var(--radius-sm);
    }

    :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-content::-webkit-scrollbar-thumb {
      background: var(--color-gray-400);
      border-radius: var(--radius-sm);
    }

    :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-title {
      padding: var(--space-xl) var(--space-xl) 0 var(--space-xl);
      margin: 0;
      font-size: 1.5rem;
      font-weight: 700;
      color: var(--text-primary);
      background: linear-gradient(135deg, var(--color-gray-50), transparent);
      border-bottom: 1px solid var(--color-gray-200);
      padding-bottom: var(--space-lg);
    }

    :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-actions {
      padding: var(--space-lg) var(--space-xl) var(--space-xl) var(--space-xl);
      background: linear-gradient(180deg, transparent, var(--color-gray-50));
      border-top: 1px solid var(--color-gray-200);
      gap: var(--space-md);
    }

    :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-actions button {
      border-radius: var(--radius-lg);
      font-weight: 600;
      height: 44px;
      transition: all var(--animation-duration-normal) var(--animation-easing);
    }

    :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-actions button:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-md);
    }

    /* Enhanced Responsive Design */
    @media (max-width: 1400px) {
      .repositories-container {
        padding: var(--space-xl);
      }
    }

    @media (max-width: 768px) {
      .repositories-container {
        padding: var(--space-lg);
      }

      .header {
        flex-direction: column;
        gap: var(--space-lg);
        align-items: stretch;
      }

      .header h1 {
        font-size: 1.875rem;
        text-align: center;
      }

      .table-container {
        overflow-x: auto;
      }

      .repositories-table {
        min-width: 600px;
      }

      .repositories-table th,
      .repositories-table td {
        padding: var(--space-md);
      }

      .repo-info {
        min-width: 200px;
      }

      .action-buttons {
        flex-direction: column;
        gap: var(--space-xs);
      }

      .action-buttons button {
        width: 100%;
        height: 36px;
      }

      :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-container {
        margin: var(--space-md);
        max-width: calc(100vw - 32px);
      }

      :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-content {
        padding: var(--space-lg);
        max-height: 60vh;
      }

      :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-title {
        padding: var(--space-lg) var(--space-lg) 0 var(--space-lg);
        font-size: 1.25rem;
      }

      :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-actions {
        padding: var(--space-md) var(--space-lg) var(--space-lg) var(--space-lg);
        flex-direction: column;
      }

      :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-actions button {
        width: 100%;
      }
    }

    @media (max-width: 480px) {
      .repositories-container {
        padding: var(--space-md);
      }

      .header h1 {
        font-size: 1.5rem;
      }

      .repositories-table {
        font-size: 0.875rem;
      }

      .repo-name {
        font-size: 0.9rem;
      }

      .repo-url {
        font-size: 0.75rem;
      }

      :host ::ng-deep .connect-repository-dialog .mat-mdc-dialog-container {
        margin: var(--space-sm);
        max-width: calc(100vw - 16px);
      }
    }

    /* Animations */
    @keyframes slideInFromLeft {
      from {
        opacity: 0;
        transform: translateX(-30px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }

    .repositories-table tr {
      animation: slideInFromLeft var(--animation-duration-slow) var(--animation-easing) forwards;
    }

    .repositories-table tr:nth-child(1) { animation-delay: 0ms; }
    .repositories-table tr:nth-child(2) { animation-delay: 100ms; }
    .repositories-table tr:nth-child(3) { animation-delay: 200ms; }
    .repositories-table tr:nth-child(4) { animation-delay: 300ms; }
    .repositories-table tr:nth-child(5) { animation-delay: 400ms; }

    /* Loading states */
    @keyframes shimmer {
      0% { background-position: -200px 0; }
      100% { background-position: calc(200px + 100%) 0; }
    }

    .loading-shimmer {
      background: linear-gradient(90deg, var(--color-gray-200) 25%, var(--color-gray-300) 50%, var(--color-gray-200) 75%);
      background-size: 200px 100%;
      animation: shimmer 1.5s infinite;
    }

    /* Enhanced focus states */
    .repositories-table tr:focus-within {
      outline: 2px solid var(--color-primary);
      outline-offset: 2px;
    }

    /* Print styles */
    @media print {
      .repositories-table {
        box-shadow: none;
        border: 1px solid var(--color-gray-300);
      }

      .header button,
      .action-buttons {
        display: none;
      }
    }
  `]
})
export class RepositoriesComponent implements OnInit {
  repositories: Repository[] = [];
  loading = true;
  displayedColumns: string[] = ['name', 'type', 'organization', 'status', 'autoReview', 'lastSync', 'actions'];

  constructor(
    private repositoryService: RepositoryService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadRepositories();
  }

  loadRepositories(): void {
    this.loading = true;
    this.repositoryService.getRepositories().subscribe({
      next: (response) => {
        this.repositories = response.content || [];
        this.loading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar repositórios:', error);
        this.loading = false;
      }
    });
  }

  openConnectDialog(): void {
    const dialogRef = this.dialog.open(ConnectRepositoryDialogComponent, {
      width: '90vw',
      maxWidth: '500px',
      minWidth: '300px',
      maxHeight: '90vh',
      disableClose: true,
      autoFocus: true,
      restoreFocus: true,
      panelClass: 'connect-repository-dialog'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadRepositories();
      }
    });
  }

  testConnection(repository: Repository): void {
    this.repositoryService.testConnection(repository.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.snackBar.open('Conexão válida!', 'Fechar', { duration: 3000 });
        } else {
          this.snackBar.open('Falha na conexão', 'Fechar', { duration: 5000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Erro ao testar conexão', 'Fechar', { duration: 5000 });
      }
    });
  }

  syncRepository(repository: Repository): void {
    this.repositoryService.syncRepository(repository.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.snackBar.open('Sincronização concluída!', 'Fechar', { duration: 3000 });
          this.loadRepositories();
        } else {
          this.snackBar.open('Erro na sincronização', 'Fechar', { duration: 5000 });
        }
      },
      error: (error) => {
        this.snackBar.open('Erro ao sincronizar', 'Fechar', { duration: 5000 });
      }
    });
  }

  editRepository(repository: Repository): void {
    // TODO: Implementar edição
    console.log('Editando repositório:', repository);
  }

  disconnectRepository(repository: Repository): void {
    if (confirm(`Tem certeza que deseja desconectar o repositório "${repository.name}"?`)) {
      this.repositoryService.disconnectRepository(repository.id).subscribe({
        next: (response) => {
          this.snackBar.open('Repositório desconectado!', 'Fechar', { duration: 3000 });
          this.loadRepositories();
        },
        error: (error) => {
          this.snackBar.open('Erro ao desconectar repositório', 'Fechar', { duration: 5000 });
        }
      });
    }
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'GITHUB': return 'GitHub';
      case 'TFS': return 'TFS';
      case 'AZURE_DEVOPS': return 'Azure DevOps';
      default: return type;
    }
  }
}
