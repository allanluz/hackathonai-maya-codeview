import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { RepositoriesComponent } from './components/repositories/repositories.component';
import { CodeReviewsComponent } from './components/code-reviews/code-reviews.component';
import { ReviewPromptsComponent } from './components/review-prompts/review-prompts.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'repositories', component: RepositoriesComponent },
  { path: 'code-reviews', component: CodeReviewsComponent },
  { path: 'review-prompts', component: ReviewPromptsComponent },
  { path: 'settings', redirectTo: '/dashboard' }, // TODO: Criar SettingsComponent
  { path: '**', redirectTo: '/dashboard' }
];
