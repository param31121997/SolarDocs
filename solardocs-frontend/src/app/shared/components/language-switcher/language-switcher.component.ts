import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { TranslateService, SupportedLang } from '../../../core/i18n/translate.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

/**
 * Angular Material language switcher — a globe icon button that opens a
 * mat-menu with the three supported languages. Drop this into the app
 * toolbar (see app.component.html) and it works anywhere in the app,
 * since TranslateService is a root-provided singleton.
 */
@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatMenuModule, TranslatePipe],
  template: `
    <button mat-button [matMenuTriggerFor]="menu" [attr.aria-label]="'language.label' | translate">
      <mat-icon>translate</mat-icon>
      <span class="lang-label">{{ 'language.' + translate.lang() | translate }}</span>
    </button>
    <mat-menu #menu="matMenu">
      <button mat-menu-item *ngFor="let l of translate.supportedLanguages"
              (click)="switch(l)"
              [class.active-lang]="l === translate.lang()">
        {{ 'language.' + l | translate }}
      </button>
    </mat-menu>
  `,
  styles: [`
    .lang-label { margin-left: 6px; }
    .active-lang { font-weight: 600; }
  `]
})
export class LanguageSwitcherComponent {
  translate = inject(TranslateService);

  switch(lang: SupportedLang) {
    this.translate.use(lang);
  }
}
