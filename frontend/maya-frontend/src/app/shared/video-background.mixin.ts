import { AfterViewInit, ElementRef, ViewChild } from '@angular/core';

export abstract class VideoBackgroundMixin implements AfterViewInit {
  @ViewChild('backgroundVideo', { static: false }) backgroundVideo!: ElementRef<HTMLVideoElement>;

  ngAfterViewInit(): void {
    // Initialize video background
    setTimeout(() => {
      this.initializeBackgroundVideo();
    }, 100);
  }

  private initializeBackgroundVideo(): void {
    if (!this.backgroundVideo?.nativeElement) {
      return;
    }

    const video = this.backgroundVideo.nativeElement;
    
    // Configure video properties
    video.muted = true;
    video.loop = true;
    video.autoplay = true;
    video.playsInline = true;
    video.controls = false;
    video.preload = 'auto';

    // Set the source
    video.src = '/video.mp4';

    // Load and play
    video.load();
    
    // Try to play after a brief delay
    setTimeout(() => {
      video.play().catch(() => {
        // Fallback for autoplay restrictions
        const playOnInteraction = () => {
          video.play().catch(() => {});
          document.removeEventListener('click', playOnInteraction);
          document.removeEventListener('touchstart', playOnInteraction);
        };
        
        document.addEventListener('click', playOnInteraction, { once: true });
        document.addEventListener('touchstart', playOnInteraction, { once: true });
      });
    }, 500);
  }
}

// Template HTML comum para o vídeo de fundo
export const VIDEO_BACKGROUND_TEMPLATE = `
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
`;

// CSS comum para todas as páginas
export const VIDEO_BACKGROUND_STYLES = `
/* Video Background Styles */
.video-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: -10;
  overflow: hidden;
  pointer-events: none;
}

.background-video {
  position: absolute;
  top: 50%;
  left: 50%;
  min-width: 100%;
  min-height: 100%;
  width: auto;
  height: auto;
  z-index: -10;
  transform: translateX(-50%) translateY(-50%);
  background-size: cover;
  object-fit: cover;
  filter: blur(1px) brightness(0.7) contrast(1.1);
}

.video-fallback {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, 
    rgba(30, 41, 59, 0.9) 0%,
    rgba(51, 65, 85, 0.8) 50%,
    rgba(0, 102, 204, 0.2) 100%
  );
  z-index: -9;
}

.video-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    135deg, 
    rgba(0, 102, 204, 0.1) 0%, 
    rgba(39, 87, 142, 0.2) 25%,
    rgba(255, 143, 0, 0.1) 50%,
    rgba(29, 41, 57, 0.3) 75%,
    rgba(15, 23, 42, 0.4) 100%
  );
  z-index: -8;
  backdrop-filter: blur(2px);
  pointer-events: none;
}

/* Container styles */
.page-container {
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  z-index: 1;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  position: relative;
  z-index: 10;
}

/* Glass-morphism card styles */
.glass-card {
  background: rgba(255, 255, 255, 0.95) !important;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2) !important;
  backdrop-filter: blur(15px) !important;
  border: 1px solid rgba(228, 231, 236, 0.6) !important;
}

.glass-card:hover {
  transform: translateY(-4px) !important;
  box-shadow: 0 4px 16px rgba(0, 102, 204, 0.25) !important;
  background: rgba(255, 255, 255, 0.98) !important;
}

/* Dark theme adjustments */
:host-context(.dark) .glass-card {
  background: rgba(30, 41, 59, 0.95) !important;
  border: 1px solid rgba(71, 85, 105, 0.6) !important;
}

:host-context(.dark) .glass-card:hover {
  background: rgba(30, 41, 59, 0.98) !important;
}

:host-context(.dark) .background-video {
  filter: blur(1px) brightness(0.4) contrast(1.2);
}

:host-context(.dark) .video-overlay {
  background: linear-gradient(
    135deg, 
    rgba(59, 130, 246, 0.15) 0%, 
    rgba(30, 41, 59, 0.4) 25%,
    rgba(245, 158, 11, 0.1) 50%,
    rgba(51, 65, 85, 0.5) 75%,
    rgba(15, 23, 42, 0.7) 100%
  );
}
`;