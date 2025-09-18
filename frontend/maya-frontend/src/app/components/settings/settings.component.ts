import { Component, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="page-container">
      <!-- Video Background -->
      <div class="video-background">
        <video 
          #backgroundVideo 
          class="background-video"
          autoplay 
          muted 
          loop 
          playsinline
          preload="auto"
          [src]="'/video.mp4'"
        >
        </video>
        <div class="video-fallback"></div>
      </div>
      
      <!-- Video Overlay -->
      <div class="video-overlay"></div>

      <!-- Container Principal -->
      <div class="container">
        <div class="settings-container">
      <div class="container">
        <!-- Header -->
        <div class="page-header">
          <h1 class="page-title">
            <i class="pi pi-cog"></i>
            Settings
          </h1>
          <p class="page-subtitle">
            Configure system preferences and integrations
          </p>
        </div>

        <!-- Settings Tabs -->
        <div class="settings-tabs">
          <div class="tab-nav">
            <button 
              class="tab-button" 
              [class.active]="activeTab === 'general'"
              (click)="setActiveTab('general')">
              <i class="pi pi-sliders-h"></i>
              General
            </button>
            <button 
              class="tab-button" 
              [class.active]="activeTab === 'tfs'"
              (click)="setActiveTab('tfs')">
              <i class="pi pi-server"></i>
              TFS Integration
            </button>
            <button 
              class="tab-button" 
              [class.active]="activeTab === 'analysis'"
              (click)="setActiveTab('analysis')">
              <i class="pi pi-search"></i>
              Analysis
            </button>
            <button 
              class="tab-button" 
              [class.active]="activeTab === 'notifications'"
              (click)="setActiveTab('notifications')">
              <i class="pi pi-bell"></i>
              Notifications
            </button>
            <button 
              class="tab-button" 
              [class.active]="activeTab === 'users'"
              (click)="setActiveTab('users')">
              <i class="pi pi-users"></i>
              Users
            </button>
          </div>

          <!-- General Settings -->
          <div class="tab-content" *ngIf="activeTab === 'general'">
            <div class="settings-card">
              <h3>General Preferences</h3>
              
              <div class="setting-group">
                <label>Theme</label>
                <select class="setting-select">
                  <option>Light</option>
                  <option>Dark</option>
                  <option>Auto</option>
                </select>
                <small>Choose your preferred color theme</small>
              </div>

              <div class="setting-group">
                <label>Language</label>
                <select class="setting-select">
                  <option>English</option>
                  <option>Português (Brasil)</option>
                  <option>Español</option>
                </select>
                <small>Select the interface language</small>
              </div>

              <div class="setting-group">
                <label>Date Format</label>
                <select class="setting-select">
                  <option>DD/MM/YYYY</option>
                  <option>MM/DD/YYYY</option>
                  <option>YYYY-MM-DD</option>
                </select>
                <small>Choose your preferred date format</small>
              </div>

              <div class="setting-group">
                <label>Time Zone</label>
                <select class="setting-select">
                  <option>America/Sao_Paulo (UTC-3)</option>
                  <option>America/New_York (UTC-5)</option>
                  <option>Europe/London (UTC+0)</option>
                  <option>UTC</option>
                </select>
                <small>Set your local time zone</small>
              </div>

              <div class="checkbox-setting">
                <input type="checkbox" id="autoRefresh" checked>
                <label for="autoRefresh">Auto-refresh dashboard</label>
                <small>Automatically refresh data every 5 minutes</small>
              </div>

              <div class="checkbox-setting">
                <input type="checkbox" id="compactView">
                <label for="compactView">Compact view mode</label>
                <small>Show more information in less space</small>
              </div>
            </div>
          </div>

          <!-- TFS Integration -->
          <div class="tab-content" *ngIf="activeTab === 'tfs'">
            <div class="settings-card">
              <h3>TFS Server Configuration</h3>
              
              <div class="setting-group">
                <label>Default TFS Server URL *</label>
                <input type="text" class="setting-input" value="https://tfs.sinqia.com.br">
                <small>The primary TFS server for code analysis</small>
              </div>

              <div class="setting-row">
                <div class="setting-group">
                  <label>Default Project *</label>
                  <input type="text" class="setting-input" value="DriveAMnet">
                  <small>Default project for new analyses</small>
                </div>
                
                <div class="setting-group">
                  <label>Default Repository *</label>
                  <input type="text" class="setting-input" value="DriveAMnet">
                  <small>Default repository name</small>
                </div>
              </div>

              <div class="setting-group">
                <label>Connection Timeout (seconds)</label>
                <input type="number" class="setting-input" value="30" min="10" max="300">
                <small>Timeout for TFS server connections</small>
              </div>

              <div class="checkbox-setting">
                <input type="checkbox" id="saveCredentials" checked>
                <label for="saveCredentials">Remember TFS credentials</label>
                <small>Store credentials securely for automatic connection</small>
              </div>

              <div class="checkbox-setting">
                <input type="checkbox" id="autoConnect">
                <label for="autoConnect">Auto-connect on startup</label>
                <small>Automatically connect to TFS when the application starts</small>
              </div>

              <div class="setting-actions">
                <button class="btn-secondary">Test Connection</button>
                <button class="btn-primary">Save TFS Settings</button>
              </div>
            </div>
          </div>

          <!-- Analysis Settings -->
          <div class="tab-content" *ngIf="activeTab === 'analysis'">
            <div class="settings-card">
              <h3>Code Analysis Configuration</h3>
              
              <div class="setting-group">
                <label>Analysis Depth</label>
                <select class="setting-select">
                  <option>Basic (Fast)</option>
                  <option>Standard (Recommended)</option>
                  <option>Deep (Comprehensive)</option>
                </select>
                <small>Choose the level of analysis detail</small>
              </div>

              <div class="setting-group">
                <label>Minimum Score Threshold</label>
                <input type="number" class="setting-input" value="70" min="0" max="100">
                <small>Minimum acceptable code quality score (0-100)</small>
              </div>

              <div class="checkbox-group">
                <h4>Analysis Features</h4>
                
                <div class="checkbox-setting">
                  <input type="checkbox" id="analyzeCode" checked>
                  <label for="analyzeCode">Code Quality Analysis</label>
                  <small>Analyze code for quality issues and patterns</small>
                </div>

                <div class="checkbox-setting">
                  <input type="checkbox" id="analyzeConnections" checked>
                  <label for="analyzeConnections">Database Connection Analysis</label>
                  <small>Check for connection leaks and proper disposal</small>
                </div>

                <div class="checkbox-setting">
                  <input type="checkbox" id="analyzeTypes" checked>
                  <label for="analyzeTypes">Type Safety Analysis</label>
                  <small>Analyze type changes and safety issues</small>
                </div>

                <div class="checkbox-setting">
                  <input type="checkbox" id="analyzeValidations" checked>
                  <label for="analyzeValidations">Validation Analysis</label>
                  <small>Check input validation and security patterns</small>
                </div>

                <div class="checkbox-setting">
                  <input type="checkbox" id="analyzePerformance">
                  <label for="analyzePerformance">Performance Analysis</label>
                  <small>Analyze code for performance bottlenecks</small>
                </div>

                <div class="checkbox-setting">
                  <input type="checkbox" id="analyzeSecurity">
                  <label for="analyzeSecurity">Security Analysis</label>
                  <small>Check for security vulnerabilities</small>
                </div>
              </div>

              <div class="setting-group">
                <label>Excluded File Patterns</label>
                <textarea class="setting-textarea" rows="4" placeholder="Enter file patterns to exclude from analysis (one per line)&#10;*.min.js&#10;*/node_modules/*&#10;*/bin/*&#10;*/obj/*"></textarea>
                <small>Files matching these patterns will be excluded from analysis</small>
              </div>

              <div class="setting-actions">
                <button class="btn-secondary">Reset to Defaults</button>
                <button class="btn-primary">Save Analysis Settings</button>
              </div>
            </div>
          </div>

          <!-- Notifications -->
          <div class="tab-content" *ngIf="activeTab === 'notifications'">
            <div class="settings-card">
              <h3>Notification Preferences</h3>
              
              <div class="checkbox-group">
                <h4>Email Notifications</h4>
                
                <div class="checkbox-setting">
                  <input type="checkbox" id="emailEnabled" checked>
                  <label for="emailEnabled">Enable email notifications</label>
                  <small>Receive notifications via email</small>
                </div>

                <div class="checkbox-setting">
                  <input type="checkbox" id="emailCritical" checked>
                  <label for="emailCritical">Critical issues found</label>
                  <small>Get notified when critical issues are detected</small>
                </div>

                <div class="checkbox-setting">
                  <input type="checkbox" id="emailCompleted" checked>
                  <label for="emailCompleted">Analysis completed</label>
                  <small>Get notified when code analysis is completed</small>
                </div>

                <div class="checkbox-setting">
                  <input type="checkbox" id="emailFailed">
                  <label for="emailFailed">Analysis failed</label>
                  <small>Get notified when analysis fails</small>
                </div>

                <div class="checkbox-setting">
                  <input type="checkbox" id="emailWeekly">
                  <label for="emailWeekly">Weekly summary report</label>
                  <small>Receive a weekly summary of code quality metrics</small>
                </div>
              </div>

              <div class="setting-group">
                <label>Email Address</label>
                <input type="email" class="setting-input" value="allan.luz@sinqia.com.br">
                <small>Email address for notifications</small>
              </div>

              <div class="checkbox-group">
                <h4>Browser Notifications</h4>
                
                <div class="checkbox-setting">
                  <input type="checkbox" id="browserNotifications">
                  <label for="browserNotifications">Enable browser notifications</label>
                  <small>Show notifications in your browser</small>
                </div>

                <div class="checkbox-setting">
                  <input type="checkbox" id="soundNotifications">
                  <label for="soundNotifications">Sound notifications</label>
                  <small>Play sound for important notifications</small>
                </div>
              </div>

              <div class="setting-actions">
                <button class="btn-secondary">Test Email</button>
                <button class="btn-primary">Save Notification Settings</button>
              </div>
            </div>
          </div>

          <!-- Users Management -->
          <div class="tab-content" *ngIf="activeTab === 'users'">
            <div class="settings-card">
              <h3>User Management</h3>
              
              <div class="user-actions">
                <button class="btn-primary">
                  <i class="pi pi-plus"></i>
                  Add New User
                </button>
                <button class="btn-secondary">
                  <i class="pi pi-download"></i>
                  Export Users
                </button>
              </div>

              <div class="users-table">
                <table>
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Role</th>
                      <th>Last Login</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>Allan Luz</td>
                      <td>allan.luz&#64;sinqia.com.br</td>
                      <td>
                        <span class="role admin">Administrator</span>
                      </td>
                      <td>2 hours ago</td>
                      <td>
                        <span class="status active">Active</span>
                      </td>
                      <td>
                        <div class="action-buttons">
                          <button class="btn-small btn-secondary" title="Edit">
                            <i class="pi pi-pencil"></i>
                          </button>
                          <button class="btn-small btn-secondary" title="Permissions">
                            <i class="pi pi-key"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>Maria Silva</td>
                      <td>maria.silva&#64;sinqia.com.br</td>
                      <td>
                        <span class="role developer">Developer</span>
                      </td>
                      <td>1 day ago</td>
                      <td>
                        <span class="status active">Active</span>
                      </td>
                      <td>
                        <div class="action-buttons">
                          <button class="btn-small btn-secondary" title="Edit">
                            <i class="pi pi-pencil"></i>
                          </button>
                          <button class="btn-small btn-secondary" title="Permissions">
                            <i class="pi pi-key"></i>
                          </button>
                          <button class="btn-small btn-danger" title="Deactivate">
                            <i class="pi pi-times"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>João Santos</td>
                      <td>joao.santos&#64;sinqia.com.br</td>
                      <td>
                        <span class="role viewer">Viewer</span>
                      </td>
                      <td>3 days ago</td>
                      <td>
                        <span class="status inactive">Inactive</span>
                      </td>
                      <td>
                        <div class="action-buttons">
                          <button class="btn-small btn-secondary" title="Edit">
                            <i class="pi pi-pencil"></i>
                          </button>
                          <button class="btn-small btn-success" title="Activate">
                            <i class="pi pi-check"></i>
                          </button>
                          <button class="btn-small btn-danger" title="Delete">
                            <i class="pi pi-trash"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements AfterViewInit {
  @ViewChild('backgroundVideo', { static: false }) backgroundVideo!: ElementRef<HTMLVideoElement>;
  
  activeTab = 'general';

  constructor(private fb: FormBuilder) {}

  ngAfterViewInit() {
    this.initializeBackgroundVideo();
  }

  private initializeBackgroundVideo() {
    if (this.backgroundVideo?.nativeElement) {
      const video = this.backgroundVideo.nativeElement;
      
      // Ensure video plays
      video.play().catch(() => {
        // If autoplay fails, try to play on user interaction
        document.addEventListener('click', () => {
          video.play();
        }, { once: true });
      });
    }
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
  }
}