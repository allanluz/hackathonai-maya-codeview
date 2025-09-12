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

    .prompt-types {
      display: flex;
      gap: var(--space-sm);
      margin-bottom: var(--space-2xl);
      padding: var(--space-md);
      background: var(--bg-surface);
      border-radius: var(--radius-2xl);
      overflow-x: auto;
      box-shadow: var(--shadow-lg);
      border: 1px solid var(--color-gray-200);
    }

    .prompt-types::-webkit-scrollbar {
      height: 6px;
    }

    .prompt-types::-webkit-scrollbar-track {
      background: var(--color-gray-100);
      border-radius: var(--radius-sm);
    }

    .prompt-types::-webkit-scrollbar-thumb {
      background: var(--color-gray-400);
      border-radius: var(--radius-sm);
    }

    .prompt-types button {
      display: flex;
      align-items: center;
      gap: var(--space-sm);
      padding: var(--space-md) var(--space-lg);
      border-radius: var(--radius-lg);
      white-space: nowrap;
      font-weight: 600;
      transition: all var(--animation-duration-normal) var(--animation-easing);
      border: 2px solid transparent;
    }

    .prompt-types button:hover {
      background: var(--color-gray-100);
      transform: translateY(-2px);
      box-shadow: var(--shadow-sm);
    }

    .prompt-types button.active {
      background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
      color: var(--text-inverse);
      border-color: var(--color-primary-dark);
      box-shadow: var(--shadow-md);
    }

    .prompts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
      gap: var(--space-xl);
    }

    .prompt-card {
      background: var(--bg-surface);
      border: 1px solid var(--color-gray-200);
      border-radius: var(--radius-2xl);
      box-shadow: var(--shadow-lg);
      transition: all var(--animation-duration-normal) var(--animation-easing);
      position: relative;
      overflow: hidden;
      min-height: 360px;
      display: flex;
      flex-direction: column;
    }

    .prompt-card::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: linear-gradient(90deg, var(--color-primary), var(--color-secondary));
      transition: height var(--animation-duration-normal) var(--animation-easing);
    }

    .prompt-card:hover {
      transform: translateY(-8px);
      box-shadow: var(--shadow-2xl);
      border-color: var(--color-primary);
    }

    .prompt-card:hover::before {
      height: 6px;
    }

    mat-card-header {
      padding: var(--space-xl) var(--space-xl) var(--space-lg);
      background: linear-gradient(135deg, var(--color-gray-50), transparent);
    }

    .card-actions {
      display: flex;
      gap: var(--space-xs);
      margin-left: auto;
    }

    .card-actions button {
      border-radius: var(--radius-lg);
      transition: all var(--animation-duration-normal) var(--animation-easing);
      min-width: auto;
      width: 40px;
      height: 40px;
    }

    .card-actions button:hover {
      background: var(--color-gray-100);
      transform: scale(1.1);
      box-shadow: var(--shadow-sm);
    }

    mat-card-title {
      font-size: 1.25rem !important;
      font-weight: 700 !important;
      color: var(--text-primary) !important;
      margin-bottom: var(--space-sm) !important;
      line-height: 1.3 !important;
    }

    mat-card-subtitle {
      color: var(--text-secondary) !important;
      font-weight: 500 !important;
      font-size: 0.875rem !important;
    }

    mat-card-content {
      padding: 0 var(--space-xl) var(--space-lg);
      flex-grow: 1;
      display: flex;
      flex-direction: column;
    }

    .prompt-description {
      color: var(--text-secondary);
      margin-bottom: var(--space-lg);
      font-size: 0.9rem;
      line-height: 1.6;
    }

    .prompt-preview {
      margin-bottom: var(--space-lg);
      flex-grow: 1;
    }

    .template-preview {
      background: var(--color-gray-50);
      padding: var(--space-md);
      border-radius: var(--radius-lg);
      font-family: var(--font-mono);
      font-size: 0.8rem;
      color: var(--text-primary);
      max-height: 120px;
      overflow: hidden;
      position: relative;
      border: 1px solid var(--color-gray-200);
      line-height: 1.5;
    }

    .template-preview::after {
      content: '...';
      position: absolute;
      bottom: var(--space-sm);
      right: var(--space-md);
      background: var(--color-gray-50);
      padding-left: var(--space-sm);
      font-weight: 600;
      color: var(--text-secondary);
    }

    .prompt-meta {
      display: flex;
      flex-direction: column;
      gap: var(--space-sm);
      margin-bottom: var(--space-lg);
    }

    .meta-item {
      display: flex;
      align-items: center;
      gap: var(--space-sm);
      font-size: 0.85rem;
      color: var(--text-secondary);
      font-weight: 500;
    }

    .meta-item mat-icon {
      font-size: 1rem;
      width: 1rem;
      height: 1rem;
      color: var(--color-primary);
      background: var(--color-gray-100);
      padding: var(--space-xs);
      border-radius: var(--radius-sm);
    }

    .auxiliary-files {
      margin-top: var(--space-lg);
      padding-top: var(--space-lg);
      border-top: 1px solid var(--color-gray-200);
    }

    .auxiliary-files strong {
      color: var(--text-primary);
      font-weight: 600;
      font-size: 0.875rem;
    }

    .files-list {
      display: flex;
      flex-wrap: wrap;
      gap: var(--space-xs);
      margin-top: var(--space-sm);
    }

    .files-list mat-chip {
      font-size: 0.75rem !important;
      height: 28px !important;
      border-radius: var(--radius-lg) !important;
      font-weight: 600 !important;
      background: linear-gradient(135deg, var(--color-gray-100), var(--color-gray-200)) !important;
      color: var(--text-primary) !important;
    }

    mat-card-actions {
      padding: var(--space-lg) var(--space-xl) var(--space-xl);
      display: flex;
      gap: var(--space-sm);
      background: linear-gradient(180deg, transparent, var(--color-gray-50));
      border-top: 1px solid var(--color-gray-200);
      margin-top: auto;
    }

    mat-card-actions button {
      border-radius: var(--radius-lg);
      font-weight: 600;
      transition: all var(--animation-duration-normal) var(--animation-easing);
      font-size: 0.875rem;
      height: 40px;
    }

    mat-card-actions button:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-sm);
    }

    .empty-state {
      text-align: center;
      padding: var(--space-3xl);
      color: var(--text-secondary);
      background: var(--bg-surface);
      border-radius: var(--radius-2xl);
      border: 2px dashed var(--color-gray-300);
      grid-column: 1 / -1;
    }

    .empty-state mat-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      margin-bottom: var(--space-lg);
      color: var(--color-gray-400);
    }

    .empty-state h3 {
      margin: 0 0 var(--space-sm) 0;
      color: var(--text-primary);
      font-size: 1.5rem;
      font-weight: 600;
    }

    .empty-state p {
      margin: 0 0 var(--space-xl) 0;
      max-width: 400px;
      margin-left: auto;
      margin-right: auto;
      line-height: 1.6;
    }

    .empty-state button {
      height: 48px;
      border-radius: var(--radius-xl);
      font-weight: 600;
    }

    /* Enhanced Responsive Design */
    @media (max-width: 1400px) {
      .prompts-container {
        padding: var(--space-xl);
      }
      
      .prompts-grid {
        grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
      }
    }

    @media (max-width: 768px) {
      .prompts-container {
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

      .prompt-types {
        padding: var(--space-sm);
        gap: var(--space-xs);
      }

      .prompt-types button {
        padding: var(--space-sm) var(--space-md);
        font-size: 0.875rem;
      }

      .prompts-grid {
        grid-template-columns: 1fr;
        gap: var(--space-lg);
      }

      .prompt-card {
        min-height: 320px;
      }
    }

    @media (max-width: 480px) {
      .prompts-container {
        padding: var(--space-md);
      }

      .header h1 {
        font-size: 1.5rem;
      }

      .prompt-types {
        flex-direction: column;
        gap: var(--space-xs);
      }

      .prompt-types button {
        justify-content: center;
      }

      mat-card-header,
      mat-card-content,
      mat-card-actions {
        padding-left: var(--space-lg);
        padding-right: var(--space-lg);
      }

      mat-card-actions {
        flex-direction: column;
        gap: var(--space-xs);
      }

      mat-card-actions button {
        width: 100%;
      }
    }

    /* Animations */
    @keyframes slideInFromBottom {
      from {
        opacity: 0;
        transform: translateY(30px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .prompt-card {
      animation: slideInFromBottom var(--animation-duration-slow) var(--animation-easing) forwards;
    }

    .prompt-card:nth-child(1) { animation-delay: 0ms; }
    .prompt-card:nth-child(2) { animation-delay: 100ms; }
    .prompt-card:nth-child(3) { animation-delay: 200ms; }
    .prompt-card:nth-child(4) { animation-delay: 300ms; }
    .prompt-card:nth-child(5) { animation-delay: 400ms; }
    .prompt-card:nth-child(6) { animation-delay: 500ms; }

    .prompt-types button {
      animation: slideInFromBottom var(--animation-duration-normal) var(--animation-easing) forwards;
    }

    .prompt-types button:nth-child(1) { animation-delay: 0ms; }
    .prompt-types button:nth-child(2) { animation-delay: 50ms; }
    .prompt-types button:nth-child(3) { animation-delay: 100ms; }
    .prompt-types button:nth-child(4) { animation-delay: 150ms; }
    .prompt-types button:nth-child(5) { animation-delay: 200ms; }

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
    .prompt-card:focus-within {
      outline: 2px solid var(--color-primary);
      outline-offset: 2px;
    }

    .prompt-types button:focus {
      outline: 2px solid var(--color-primary);
      outline-offset: 2px;
    }

    /* Print styles */
    @media print {
      .prompt-card {
        break-inside: avoid;
        box-shadow: none;
        border: 1px solid var(--color-gray-300);
      }

      .header button,
      mat-card-actions,
      .card-actions {
        display: none;
      }

      .prompt-types {
        display: none;
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
