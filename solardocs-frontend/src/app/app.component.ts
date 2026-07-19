import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { TranslatePipe } from './core/i18n/translate.pipe';
import { LanguageSwitcherComponent } from './shared/components/language-switcher/language-switcher.component';
import { SetupService } from './core/services/setup.service';
import { FirstRunWizardComponent } from './features/setup/first-run-wizard.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, RouterOutlet, RouterLink, RouterLinkActive,
    MatToolbarModule, MatSidenavModule, MatListModule, MatIconModule,
    TranslatePipe, LanguageSwitcherComponent, FirstRunWizardComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'SolarDocs';

  private setupService = inject(SetupService);

  /** null while we haven't checked yet, so we don't flash the wizard before the check completes. */
  needsSetup = signal<boolean | null>(null);

  ngOnInit(): void {
    this.setupService.status().subscribe({
      next: (status) => this.needsSetup.set(!status.configured),
      // If the setup check itself fails (backend not reachable yet), don't
      // block the whole app behind a wizard - just let the rest of the UI
      // show its own "can't connect" errors as normal.
      error: () => this.needsSetup.set(false)
    });
  }

  onDataDirectorySaved(): void {
    // Wizard shows its own "please restart" message; nothing else to do here.
  }
}
