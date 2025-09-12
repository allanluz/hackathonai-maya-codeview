import { Routes } from '@angular/router';

export const routes: Routes = [
  { 
    path: '', 
    redirectTo: '/dashboard', 
    pathMatch: 'full' 
  },
  { 
    path: 'dashboard', 
    loadComponent: () => import('./components/dashboard/dashboard.component.simple').then(m => m.DashboardComponent) 
  },
  { 
    path: 'reviews', 
    loadComponent: () => import('./components/dashboard/dashboard.component.simple').then(m => m.DashboardComponent) 
  },
  { 
    path: 'reports', 
    loadComponent: () => import('./components/reports/reports.component').then(m => m.ReportsComponent) 
  },
  { 
    path: 'import', 
    loadComponent: () => import('./components/dashboard/dashboard.component.simple').then(m => m.DashboardComponent) 
  },
  { 
    path: 'import-tfs', 
    loadComponent: () => import('./components/dashboard/dashboard.component.simple').then(m => m.DashboardComponent) 
  },
  { 
    path: 'analytics', 
    loadComponent: () => import('./components/reports/reports.component').then(m => m.ReportsComponent) 
  },
  { 
    path: 'configuration', 
    loadComponent: () => import('./components/settings/settings.component').then(m => m.SettingsComponent) 
  },
  { 
    path: 'settings', 
    loadComponent: () => import('./components/settings/settings.component').then(m => m.SettingsComponent) 
  },
  { 
    path: '**', 
    redirectTo: '/dashboard' 
  }
];
