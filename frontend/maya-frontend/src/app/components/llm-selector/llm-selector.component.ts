import { Component, OnInit, Output, EventEmitter, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MaterialModule } from '../../material.module';
import { MayaApiService } from '../../services/maya-api.service';

@Component({
  selector: 'app-llm-selector',
  standalone: true,
  imports: [CommonModule, FormsModule, MaterialModule],
  templateUrl: './llm-selector.component.html',
  styleUrl: './llm-selector.component.scss'
})
export class LlmSelectorComponent implements OnInit {
  @Input() selectedModel: string = '';
  @Output() modelSelected = new EventEmitter<string>();

  availableModels: string[] = [];
  isLoading = false;
  error: string | null = null;

  private mayaApiService = inject(MayaApiService);

  ngOnInit(): void {
    this.loadAvailableModels();
  }

  private loadAvailableModels(): void {
    this.isLoading = true;
    this.error = null;

    this.mayaApiService.getLlmModels().subscribe({
      next: (response) => {
        if (response.status === 'SUCCESS' && response.models) {
          this.availableModels = response.models;
          
          // Seleciona o primeiro modelo se nenhum estiver selecionado
          if (!this.selectedModel && this.availableModels.length > 0) {
            this.selectedModel = this.availableModels[0];
            this.onModelChange();
          }
        } else {
          this.error = response.message || 'Erro ao carregar modelos';
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar modelos LLM:', error);
        this.error = 'Falha na conexão com o serviço de IA';
        this.isLoading = false;
        
        // Modelos fallback para desenvolvimento
        this.availableModels = [
          'gemini-2.0-flash-001',
          'gemini-pro',
          'gpt-4-turbo',
          'gpt-3.5-turbo'
        ];
        
        if (!this.selectedModel) {
          this.selectedModel = this.availableModels[0];
          this.onModelChange();
        }
      }
    });
  }

  onModelChange(): void {
    this.modelSelected.emit(this.selectedModel);
  }

  getModelDisplayName(model: string): string {
    const modelNames: { [key: string]: string } = {
      'gemini-2.0-flash-001': 'Gemini 2.0 Flash',
      'gemini-pro': 'Gemini Pro',
      'gpt-4-turbo': 'GPT-4 Turbo',
      'gpt-3.5-turbo': 'GPT-3.5 Turbo'
    };
    return modelNames[model] || model;
  }

  getModelFamily(model: string): string {
    if (model.toLowerCase().includes('gemini')) {
      return 'Google';
    } else if (model.toLowerCase().includes('gpt')) {
      return 'OpenAI';
    }
    return 'Outros';
  }

  getUniqueModelFamilies(): string[] {
    const families = new Set<string>();
    this.availableModels.forEach(model => {
      families.add(this.getModelFamily(model));
    });
    return Array.from(families).sort();
  }

  getModelsByFamily(family: string): string[] {
    return this.availableModels.filter(model => this.getModelFamily(model) === family);
  }

  refreshModels(): void {
    this.loadAvailableModels();
  }
}