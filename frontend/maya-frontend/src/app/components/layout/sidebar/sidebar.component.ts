import { Component, OnInit, ViewChild } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { MayaApiService } from '../../../services/maya-api.service';
import { MaterialModule } from '../../../material.module';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [MaterialModule, CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent implements OnInit {
  @ViewChild('sidenav') sidenav!: MatSidenav;

  version = environment.version;
  activeReviews = 0;
  
  // Status da conexão TFS
  tfsConnectionIcon = 'cloud_off';
  tfsConnectionClass = 'connection-offline';
  tfsConnectionTooltip = 'TFS desconectado';

  constructor(private mayaApiService: MayaApiService) {}

  ngOnInit(): void {
    this.loadActiveReviews();
    this.checkTfsConnection();
  }

  toggle(): void {
    this.sidenav.toggle();
  }

  private loadActiveReviews(): void {
    this.mayaApiService.dashboard$.subscribe(dashboard => {
      if (dashboard) {
        this.activeReviews = dashboard.activeReviews;
      }
    });
  }

  private checkTfsConnection(): void {
    this.mayaApiService.getTfsProjects().subscribe({
      next: () => {
        this.tfsConnectionIcon = 'cloud_done';
        this.tfsConnectionClass = 'connection-online';
        this.tfsConnectionTooltip = 'TFS conectado';
      },
      error: () => {
        this.tfsConnectionIcon = 'cloud_off';
        this.tfsConnectionClass = 'connection-offline';
        this.tfsConnectionTooltip = 'Erro na conexão TFS';
      }
    });
  }
}
