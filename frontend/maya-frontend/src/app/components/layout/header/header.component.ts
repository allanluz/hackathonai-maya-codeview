import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { MayaApiService } from '../../../services/maya-api.service';
import { MaterialModule } from '../../../material.module';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [MaterialModule, CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent implements OnInit {
  @Output() sidebarToggle = new EventEmitter<void>();

  // Status de saúde do sistema
  healthIcon = 'wifi_tethering';
  healthIconClass = 'health-connecting';
  healthTooltip = 'Verificando sistema...';
  
  // Notificações
  notificationCount = 0;

  constructor(private mayaApiService: MayaApiService) {}

  ngOnInit(): void {
    this.checkSystemHealth();
    this.loadNotifications();
  }

  toggleSidebar(): void {
    this.sidebarToggle.emit();
  }

  private checkSystemHealth(): void {
    this.mayaApiService.getHealth().subscribe({
      next: (health) => {
        if (health.status === 'UP') {
          this.healthIcon = 'wifi';
          this.healthIconClass = 'health-online';
          this.healthTooltip = 'Sistema online e funcionando';
        } else {
          this.healthIcon = 'wifi_off';
          this.healthIconClass = 'health-offline';
          this.healthTooltip = 'Sistema com problemas';
        }
      },
      error: () => {
        this.healthIcon = 'wifi_off';
        this.healthIconClass = 'health-offline';
        this.healthTooltip = 'Não foi possível conectar ao sistema';
      }
    });
  }

  private loadNotifications(): void {
    // Simular notificações - em produção seria um serviço específico
    this.notificationCount = 3;
  }
}
