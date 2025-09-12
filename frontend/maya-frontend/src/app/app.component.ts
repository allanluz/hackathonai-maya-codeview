import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { ClickOutsideDirective } from './directives/click-outside.directive';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ClickOutsideDirective],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'MAYA Code Review System';

  // Navigation state
  showImportDropdown = false;
  showUserMenu = false;
  isDarkTheme = false;

  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit() {
    // Load theme preference from localStorage (only in browser)
    if (isPlatformBrowser(this.platformId)) {
      const savedTheme = localStorage.getItem('maya-theme');
      this.isDarkTheme = savedTheme === 'dark';
      this.applyTheme();
    }
  }

  /**
   * Toggle import dropdown menu
   */
  toggleImportDropdown(): void {
    this.showImportDropdown = !this.showImportDropdown;
    // Close user menu if open
    if (this.showImportDropdown) {
      this.showUserMenu = false;
    }
  }

  /**
   * Toggle user menu dropdown
   */
  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
    // Close import dropdown if open
    if (this.showUserMenu) {
      this.showImportDropdown = false;
    }
  }

  /**
   * Check if import routes are active
   */
  isImportActive(): boolean {
    const url = this.router.url;
    return url.includes('/import') || url.includes('/import-tfs');
  }

  /**
   * Toggle application theme
   */
  toggleTheme(): void {
    this.isDarkTheme = !this.isDarkTheme;
    this.applyTheme();
    
    // Save preference to localStorage (only in browser)
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('maya-theme', this.isDarkTheme ? 'dark' : 'light');
    }
  }

  /**
   * Apply the current theme to the document
   */
  private applyTheme(): void {
    if (isPlatformBrowser(this.platformId)) {
      const body = document.body;
      if (this.isDarkTheme) {
        body.classList.add('dark-theme');
        body.classList.remove('light-theme');
      } else {
        body.classList.add('light-theme');
        body.classList.remove('dark-theme');
      }
    }
  }
}
