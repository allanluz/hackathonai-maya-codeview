import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { ReviewPromptService, ReviewPrompt } from '../../services/review-prompt.service';

type PromptType = 'GENERAL' | 'JAVA_SPECIFIC' | 'JAVASCRIPT_SPECIFIC' | 'SECURITY_FOCUSED' | 'PERFORMANCE_FOCUSED' | 'SINQIA_STANDARDS' | 'CONNECTION_LEAKS' | 'CUSTOM';

@Component({
  selector: 'app-review-prompts',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule
  ],
  template: `
    <div class="prompts-container">
      <div class="header">
        <h1>Prompts de Revisão</h1>
        <button mat-raised-button color="primary" (click)="openPromptDialog()">
          <mat-icon>add</mat-icon>
          Novo Prompt
        </button>
      </div>

      <!-- Prompt Types Tabs -->
      <div class="prompt-types">
        <button 
          *ngFor="let type of promptTypes" 
          mat-button 
          [class.active]="selectedType === type.value"
          (click)="selectType(type.value)">
          <mat-icon>{{ type.icon }}</mat-icon>
          {{ type.label }}
        </button>
      </div>

      <!-- Prompts Grid -->
      <div class="prompts-grid">
        <mat-card *ngFor="let prompt of filteredPrompts" class="prompt-card">
          <mat-card-header>
            <div mat-card-avatar>
              <mat-icon>{{ getPromptIcon(prompt.type) }}</mat-icon>
            </div>
            <mat-card-title>{{ prompt.name }}</mat-card-title>
            <mat-card-subtitle>{{ getPromptTypeLabel(prompt.type) }}</mat-card-subtitle>
            <div class="card-actions">
              <button mat-icon-button (click)="editPrompt(prompt)">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button (click)="duplicatePrompt(prompt)">
                <mat-icon>content_copy</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="deletePrompt(prompt)" [disabled]="prompt.isDefault">
                <mat-icon>delete</mat-icon>
              </button>
            </div>
          </mat-card-header>
          
          <mat-card-content>
            <p class="prompt-description">{{ prompt.description }}</p>
            
            <div class="prompt-preview">
              <strong>Template:</strong>
              <div class="template-preview">{{ getTemplatePreview(prompt.promptTemplate) }}</div>
            </div>

            <div class="prompt-meta">
              <div class="meta-item">
                <mat-icon>folder</mat-icon>
                <span>{{ getFileExtensions(prompt.fileExtensions) }}</span>
              </div>
              
              <div class="meta-item">
                <mat-icon>analytics</mat-icon>
                <span>{{ prompt.usageCount || 0 }} usos</span>
              </div>
              
              <div class="meta-item" *ngIf="prompt.isDefault">
                <mat-icon>star</mat-icon>
                <span>Padrão</span>
              </div>
            </div>

            <div class="auxiliary-files" *ngIf="false">
              <strong>Arquivos Auxiliares:</strong>
              <div class="files-list">
                <mat-chip>Exemplo.txt</mat-chip>
              </div>
            </div>
          </mat-card-content>

          <mat-card-actions>
            <button mat-button (click)="testPrompt(prompt)">
              <mat-icon>play_arrow</mat-icon>
              Testar
            </button>
            <button mat-button (click)="viewUsage(prompt)">
              <mat-icon>history</mat-icon>
              Histórico
            </button>
            <button mat-button *ngIf="!prompt.isDefault" (click)="setAsDefault(prompt)">
              <mat-icon>star_border</mat-icon>
              Definir como Padrão
            </button>
          </mat-card-actions>
        </mat-card>
      </div>

      <!-- Empty State -->
      <div class="empty-state" *ngIf="filteredPrompts.length === 0">
        <mat-icon>edit_note</mat-icon>
        <h3>Nenhum prompt encontrado</h3>
        <p>Crie seu primeiro prompt personalizado para começar a usar análises customizadas.</p>
        <button mat-raised-button color="primary" (click)="openPromptDialog()">
          <mat-icon>add</mat-icon>
          Criar Primeiro Prompt
        </button>
      </div>
    </div>
  `,
  styles: [`
    .prompts-container {
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

    .prompt-types {
      display: flex;
      gap: 8px;
      margin-bottom: 24px;
      padding: 8px;
      background: #f5f5f5;
      border-radius: 8px;
      overflow-x: auto;
    }

    .prompt-types button {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      border-radius: 4px;
      white-space: nowrap;
    }

    .prompt-types button.active {
      background: #2196f3;
      color: white;
    }

    .prompts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
      gap: 24px;
    }

    .prompt-card {
      min-height: 300px;
      position: relative;
    }

    .card-actions {
      display: flex;
      gap: 4px;
      margin-left: auto;
    }

    .prompt-description {
      color: #666;
      margin-bottom: 16px;
      font-size: 0.9rem;
    }

    .prompt-preview {
      margin-bottom: 16px;
    }

    .template-preview {
      background: #f8f9fa;
      padding: 12px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 0.8rem;
      color: #333;
      max-height: 100px;
      overflow: hidden;
      position: relative;
    }

    .template-preview::after {
      content: '...';
      position: absolute;
      bottom: 0;
      right: 8px;
      background: #f8f9fa;
      padding-left: 8px;
    }

    .prompt-meta {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-bottom: 16px;
    }

    .meta-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.85rem;
      color: #666;
    }

    .meta-item mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .auxiliary-files {
      margin-top: 16px;
    }

    .files-list {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
      margin-top: 8px;
    }

    .files-list mat-chip {
      font-size: 0.75rem;
      height: 24px;
    }

    .empty-state {
      text-align: center;
      padding: 48px;
      color: #666;
    }

    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
      color: #ccc;
    }

    .empty-state h3 {
      margin: 0 0 8px 0;
      color: #333;
    }

    .empty-state p {
      margin: 0 0 24px 0;
      max-width: 400px;
      margin-left: auto;
      margin-right: auto;
    }

    @media (max-width: 768px) {
      .prompts-container {
        padding: 16px;
      }

      .header {
        flex-direction: column;
        gap: 16px;
        align-items: stretch;
      }

      .prompts-grid {
        grid-template-columns: 1fr;
      }

      .prompt-types {
        justify-content: center;
      }
    }
  `]
})
export class ReviewPromptsComponent implements OnInit {
  prompts: ReviewPrompt[] = [];
  filteredPrompts: ReviewPrompt[] = [];
  selectedType: PromptType | 'ALL' = 'ALL';

  promptTypes = [
    { value: 'ALL' as any, label: 'Todos', icon: 'view_module' },
    { value: 'SECURITY' as PromptType, label: 'Segurança', icon: 'security' },
    { value: 'PERFORMANCE' as PromptType, label: 'Performance', icon: 'speed' },
    { value: 'QUALITY' as PromptType, label: 'Qualidade', icon: 'star' },
    { value: 'BEST_PRACTICES' as PromptType, label: 'Boas Práticas', icon: 'rule' },
    { value: 'DOCUMENTATION' as PromptType, label: 'Documentação', icon: 'description' },
    { value: 'CUSTOM' as PromptType, label: 'Personalizado', icon: 'tune' }
  ];

  constructor(
    private reviewPromptService: ReviewPromptService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadPrompts();
  }

  loadPrompts(): void {
    this.reviewPromptService.getPrompts().subscribe({
      next: (prompts) => {
        this.prompts = prompts;
        this.filterPrompts();
      },
      error: (error) => console.error('Erro ao carregar prompts:', error)
    });
  }

  selectType(type: PromptType | 'ALL'): void {
    this.selectedType = type;
    this.filterPrompts();
  }

  filterPrompts(): void {
    if (this.selectedType === 'ALL') {
      this.filteredPrompts = this.prompts;
    } else {
      this.filteredPrompts = this.prompts.filter(p => p.type === this.selectedType);
    }
  }

  openPromptDialog(prompt?: ReviewPrompt): void {
    // TODO: Implementar dialog de criação/edição de prompt
    console.log('Abrindo dialog para prompt:', prompt);
  }

  editPrompt(prompt: ReviewPrompt): void {
    this.openPromptDialog(prompt);
  }

  duplicatePrompt(prompt: ReviewPrompt): void {
    const duplicated = {
      name: `${prompt.name} (Cópia)`,
      description: prompt.description,
      promptTemplate: prompt.promptTemplate,
      systemInstructions: prompt.systemInstructions,
      type: prompt.type,
      fileExtensions: prompt.fileExtensions,
      focusAreas: Array.isArray(prompt.focusAreas) ? prompt.focusAreas : 
                  (prompt.focusAreas ? [prompt.focusAreas] : []),
      isDefault: false
    };
    
    this.reviewPromptService.createPrompt(duplicated).subscribe({
      next: () => {
        console.log('Prompt duplicado com sucesso');
        this.loadPrompts();
      },
      error: (error: any) => console.error('Erro ao duplicar prompt:', error)
    });
  }

  deletePrompt(prompt: ReviewPrompt): void {
    if (prompt.isDefault) {
      return;
    }

    if (confirm(`Tem certeza que deseja excluir o prompt "${prompt.name}"?`)) {
      this.reviewPromptService.deletePrompt(prompt.id!).subscribe({
        next: () => {
          console.log('Prompt excluído com sucesso');
          this.loadPrompts();
        },
        error: (error) => console.error('Erro ao excluir prompt:', error)
      });
    }
  }

  testPrompt(prompt: ReviewPrompt): void {
    // TODO: Implementar teste de prompt
    console.log('Testando prompt:', prompt);
  }

  viewUsage(prompt: ReviewPrompt): void {
    // TODO: Implementar visualização de histórico de uso
    console.log('Visualizando histórico do prompt:', prompt);
  }

  setAsDefault(prompt: ReviewPrompt): void {
    this.reviewPromptService.setDefault(prompt.id!).subscribe({
      next: () => {
        console.log('Prompt definido como padrão');
        this.loadPrompts();
      },
      error: (error: any) => console.error('Erro ao definir prompt como padrão:', error)
    });
  }

  getPromptIcon(type: PromptType): string {
    const typeData = this.promptTypes.find(t => t.value === type);
    return typeData?.icon || 'edit';
  }

  getPromptTypeLabel(type: PromptType): string {
    const typeData = this.promptTypes.find(t => t.value === type);
    return typeData?.label || type;
  }

  getTemplatePreview(template: string): string {
    if (!template) return '';
    return template.length > 200 ? template.substring(0, 200) + '...' : template;
  }

  getFileExtensions(extensions: string): string {
    return extensions || 'Todas as extensões';
  }
}
