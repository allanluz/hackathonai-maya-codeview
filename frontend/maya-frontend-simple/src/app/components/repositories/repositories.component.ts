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
    <mat-dialog-content>
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
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-raised-button color="primary" [disabled]="connectForm.invalid || connecting" (click)="connect()">
        <mat-icon *ngIf="connecting">hourglass_empty</mat-icon>
        {{ connecting ? 'Conectando...' : 'Conectar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .connect-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 400px;
    }

    .toggle-field {
      margin: 16px 0;
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
      padding: 24px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .table-container {
      overflow-x: auto;
    }

    .repositories-table {
      width: 100%;
    }

    .repo-info {
      display: flex;
      flex-direction: column;
    }

    .repo-name {
      font-weight: 500;
    }

    .repo-url {
      font-size: 0.9rem;
      color: #666;
    }

    .action-buttons {
      display: flex;
      gap: 4px;
    }

    .type-github { background: #333; color: white; }
    .type-tfs { background: #0078d4; color: white; }
    .type-azure_devops { background: #0078d4; color: white; }

    .status-active { background: #4caf50; color: white; }
    .status-inactive { background: #9e9e9e; color: white; }

    .enabled { color: #4caf50; }
    .disabled { color: #9e9e9e; }

    .empty-state {
      text-align: center;
      padding: 48px;
      color: #666;
    }

    .empty-state mat-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      margin-bottom: 16px;
      color: #ccc;
    }

    .loading-container {
      padding: 24px;
      text-align: center;
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
      width: '500px',
      disableClose: true
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
