import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { RouterModule, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatSidenavModule,
    MatListModule,
    MatMenuModule
  ],
  template: `
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav #drawer class="sidenav" fixedInViewport mode="side" opened>
        <mat-toolbar class="sidenav-header">
          <img src="assets/maya-logo.png" alt="MAYA" class="logo" onerror="this.style.display='none'">
          <span class="app-name">MAYA</span>
        </mat-toolbar>
        
        <mat-nav-list>
          <a mat-list-item routerLink="/dashboard" routerLinkActive="active">
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>Dashboard</span>
          </a>
          
          <a mat-list-item routerLink="/repositories" routerLinkActive="active">
            <mat-icon matListItemIcon>folder</mat-icon>
            <span matListItemTitle>Repositórios</span>
          </a>
          
          <a mat-list-item routerLink="/code-reviews" routerLinkActive="active">
            <mat-icon matListItemIcon>code</mat-icon>
            <span matListItemTitle>Code Reviews</span>
          </a>
          
          <a mat-list-item routerLink="/review-prompts" routerLinkActive="active">
            <mat-icon matListItemIcon>edit</mat-icon>
            <span matListItemTitle>Prompts de Revisão</span>
          </a>
          
          <mat-divider></mat-divider>
          
          <a mat-list-item routerLink="/settings" routerLinkActive="active">
            <mat-icon matListItemIcon>settings</mat-icon>
            <span matListItemTitle>Configurações</span>
          </a>
          
          <a mat-list-item href="#" (click)="openDocumentation()">
            <mat-icon matListItemIcon>help</mat-icon>
            <span matListItemTitle>Documentação</span>
          </a>
        </mat-nav-list>
      </mat-sidenav>

      <mat-sidenav-content>
        <mat-toolbar class="toolbar">
          <button type="button" aria-label="Toggle sidenav" mat-icon-button (click)="drawer.toggle()">
            <mat-icon aria-label="Side nav toggle icon">menu</mat-icon>
          </button>
          
          <span class="toolbar-title">{{ getPageTitle() }}</span>
          
          <span class="toolbar-spacer"></span>
          
          <button mat-icon-button [matMenuTriggerFor]="userMenu">
            <mat-icon>account_circle</mat-icon>
          </button>
          
          <mat-menu #userMenu="matMenu">
            <button mat-menu-item>
              <mat-icon>person</mat-icon>
              <span>Perfil</span>
            </button>
            <button mat-menu-item>
              <mat-icon>settings</mat-icon>
              <span>Configurações</span>
            </button>
            <mat-divider></mat-divider>
            <button mat-menu-item>
              <mat-icon>exit_to_app</mat-icon>
              <span>Sair</span>
            </button>
          </mat-menu>
        </mat-toolbar>

        <div class="content">
          <router-outlet></router-outlet>
        </div>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .sidenav-container {
      height: 100vh;
    }

    .sidenav {
      width: 250px;
      background: #1e3a8a;
    }

    .sidenav-header {
      background: #1e40af;
      color: white;
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 0 16px;
    }

    .logo {
      width: 32px;
      height: 32px;
    }

    .app-name {
      font-size: 1.25rem;
      font-weight: bold;
    }

    .sidenav mat-nav-list {
      padding-top: 0;
    }

    .sidenav mat-list-item {
      color: #e5e7eb;
      border-radius: 0;
    }

    .sidenav mat-list-item:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    .sidenav mat-list-item.active {
      background: #3b82f6;
      color: white;
    }

    .sidenav mat-icon {
      color: inherit;
    }

    .toolbar {
      position: sticky;
      top: 0;
      z-index: 1000;
      background: white;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .toolbar-title {
      font-size: 1.25rem;
      font-weight: 500;
    }

    .toolbar-spacer {
      flex: 1 1 auto;
    }

    .content {
      min-height: calc(100vh - 64px);
      background: #f8fafc;
    }

    @media (max-width: 768px) {
      .sidenav {
        width: 200px;
      }
    }
  `]
})
export class AppComponent {
  title = 'MAYA Code Review System';
  currentPageTitle = 'MAYA - Code View System';

  constructor(private router: Router) {
    // Listen to route changes to update page title
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.updatePageTitle(event.url);
      });
  }

  getPageTitle(): string {
    return this.currentPageTitle;
  }

  private updatePageTitle(url: string): void {
    const titles: { [key: string]: string } = {
      '/dashboard': 'Dashboard - MAYA',
      '/repositories': 'Repositórios - MAYA',
      '/code-reviews': 'Code Reviews - MAYA',
      '/review-prompts': 'Prompts de Revisão - MAYA',
      '/settings': 'Configurações - MAYA'
    };

    this.currentPageTitle = titles[url] || 'MAYA - Code View System';
  }

  openDocumentation(): void {
    window.open('https://docs.maya.sinqia.com.br', '_blank');
  }
}
