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
      background: var(--bg-secondary);
    }

    .sidenav {
      width: 280px;
      background: var(--bg-surface);
      border-right: 1px solid var(--color-gray-200);
      box-shadow: var(--shadow-lg);
    }

    .sidenav-header {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
      color: var(--text-inverse);
      display: flex;
      align-items: center;
      gap: 12px;
      padding: var(--space-lg) var(--space-lg);
      position: relative;
      overflow: hidden;
    }

    .sidenav-header::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="white" opacity="0.1"/><circle cx="90" cy="40" r="0.5" fill="white" opacity="0.1"/><circle cx="10" cy="60" r="0.5" fill="white" opacity="0.1"/><circle cx="30" cy="80" r="0.5" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
      opacity: 0.4;
    }

    .logo {
      width: 40px;
      height: 40px;
      border-radius: var(--radius-lg);
      background: var(--bg-surface);
      padding: var(--space-sm);
      z-index: 1;
    }

    .app-name {
      font-size: 1.5rem;
      font-weight: 700;
      letter-spacing: -0.025em;
      z-index: 1;
    }

    .sidenav mat-nav-list {
      padding: var(--space-md) 0;
    }

    .sidenav mat-list-item {
      color: var(--text-secondary);
      margin: 0 var(--space-md);
      border-radius: var(--radius-lg);
      transition: all var(--animation-duration-normal) var(--animation-easing);
      font-weight: 500;
      height: 48px;
      position: relative;
      overflow: hidden;
    }

    .sidenav mat-list-item::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background: var(--color-primary);
      transform: scaleY(0);
      transition: transform var(--animation-duration-normal) var(--animation-easing);
    }

    .sidenav mat-list-item:hover {
      background: var(--color-gray-100);
      color: var(--text-primary);
      transform: translateX(4px);
    }

    .sidenav mat-list-item.active {
      background: var(--color-primary);
      color: var(--text-inverse);
      box-shadow: var(--shadow-md);
    }

    .sidenav mat-list-item.active::before {
      transform: scaleY(1);
    }

    .sidenav mat-icon {
      color: inherit;
      margin-right: var(--space-md);
    }

    .sidenav mat-divider {
      margin: var(--space-lg) var(--space-md);
      background: var(--color-gray-200);
    }

    .toolbar {
      position: sticky;
      top: 0;
      z-index: 1000;
      background: var(--bg-surface);
      backdrop-filter: blur(10px);
      -webkit-backdrop-filter: blur(10px);
      border-bottom: 1px solid var(--color-gray-200);
      box-shadow: var(--shadow-sm);
      height: 72px;
      padding: 0 var(--space-xl);
    }

    .toolbar button[mat-icon-button] {
      border-radius: var(--radius-lg);
      transition: all var(--animation-duration-normal) var(--animation-easing);
    }

    .toolbar button[mat-icon-button]:hover {
      background: var(--color-gray-100);
      transform: scale(1.05);
    }

    .toolbar-title {
      font-size: 1.375rem;
      font-weight: 600;
      color: var(--text-primary);
      margin-left: var(--space-lg);
      background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .toolbar-spacer {
      flex: 1 1 auto;
    }

    .content {
      min-height: calc(100vh - 72px);
      background: var(--bg-secondary);
      position: relative;
    }

    .content::before {
      content: '';
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: 
        radial-gradient(circle at 25% 25%, var(--color-primary-light) 0%, transparent 50%),
        radial-gradient(circle at 75% 75%, var(--color-secondary) 0%, transparent 50%);
      opacity: 0.02;
      pointer-events: none;
      z-index: -1;
    }

    /* User Menu Enhancements */
    .mat-mdc-menu-panel {
      border-radius: var(--radius-xl) !important;
      box-shadow: var(--shadow-xl) !important;
      border: 1px solid var(--color-gray-200) !important;
      backdrop-filter: blur(10px);
      -webkit-backdrop-filter: blur(10px);
    }

    /* Responsive Design */
    @media (max-width: 1024px) {
      .sidenav {
        width: 260px;
      }
    }

    @media (max-width: 768px) {
      .sidenav {
        width: 240px;
      }
      
      .toolbar {
        padding: 0 var(--space-lg);
      }
      
      .toolbar-title {
        font-size: 1.25rem;
      }
    }

    @media (max-width: 640px) {
      .sidenav {
        width: 100vw;
      }
      
      .toolbar {
        padding: 0 var(--space-md);
      }
      
      .toolbar-title {
        display: none;
      }
    }

    /* Animation Enhancements */
    @keyframes slideIn {
      from {
        opacity: 0;
        transform: translateX(-20px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }

    .sidenav mat-list-item {
      animation: slideIn var(--animation-duration-slow) var(--animation-easing) forwards;
    }

    .sidenav mat-list-item:nth-child(1) { animation-delay: 50ms; }
    .sidenav mat-list-item:nth-child(2) { animation-delay: 100ms; }
    .sidenav mat-list-item:nth-child(3) { animation-delay: 150ms; }
    .sidenav mat-list-item:nth-child(4) { animation-delay: 200ms; }
    .sidenav mat-list-item:nth-child(5) { animation-delay: 250ms; }
    .sidenav mat-list-item:nth-child(6) { animation-delay: 300ms; }

    /* Focus states */
    .sidenav mat-list-item:focus-visible {
      outline: 2px solid var(--color-primary);
      outline-offset: 2px;
    }

    /* High contrast mode support */
    @media (prefers-contrast: high) {
      .sidenav-header {
        background: var(--text-primary);
      }
      
      .sidenav mat-list-item.active {
        background: var(--text-primary);
        outline: 2px solid var(--bg-surface);
      }
    }

    /* Reduced motion support */
    @media (prefers-reduced-motion: reduce) {
      * {
        transition: none !important;
        animation: none !important;
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
