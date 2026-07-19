import { ChangeDetectorRef, OnDestroy, Pipe, PipeTransform, inject } from '@angular/core';
import { TranslateService } from './translate.service';

/**
 * Impure pipe so the view updates immediately when the language signal changes
 * (Angular's default pipe purity would otherwise cache the first translation).
 * Usage in templates: {{ 'customer.title' | translate }}
 */
@Pipe({ name: 'translate', standalone: true, pure: false })
export class TranslatePipe implements PipeTransform, OnDestroy {
  private translate = inject(TranslateService);
  private cdr = inject(ChangeDetectorRef);
  private lastLang: string | null = null;
  private lastKey: string | null = null;
  private lastValue = '';

  transform(key: string, params?: Record<string, string | number>): string {
    const lang = this.translate.lang();
    if (key !== this.lastKey || lang !== this.lastLang) {
      this.lastKey = key;
      this.lastLang = lang;
      this.lastValue = this.translate.instant(key, params);
    }
    return this.lastValue;
  }

  ngOnDestroy(): void {}
}
