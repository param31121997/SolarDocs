import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export type SupportedLang = 'en' | 'hi' | 'mr';

const STORAGE_KEY = 'solardocs.language';
const SUPPORTED: SupportedLang[] = ['en', 'hi', 'mr'];

/**
 * Lightweight runtime translation service (no external i18n library needed).
 * Loads /assets/i18n/{lang}.json and exposes a signal-based `t()` lookup plus
 * an `instant()` helper for use outside templates (e.g. Snackbar messages).
 *
 * Switching language is instant — no app rebuild, no page reload — which is
 * exactly what a single-vendor desktop app needs, since the vendor picks
 * their working language once during setup and can change it from Settings.
 */
@Injectable({ providedIn: 'root' })
export class TranslateService {
  private http = inject(HttpClient);

  private dictionaries = new Map<SupportedLang, Record<string, any>>();
  private currentLang = signal<SupportedLang>(this.detectInitialLang());

  readonly lang = computed(() => this.currentLang());
  readonly supportedLanguages: SupportedLang[] = SUPPORTED;

  private ready = signal(false);
  readonly isReady = computed(() => this.ready());

  async init(): Promise<void> {
    await this.ensureLoaded(this.currentLang());
    // Pre-warm the other two so switching language later is instant with no flash.
    for (const l of SUPPORTED) {
      if (l !== this.currentLang()) this.ensureLoaded(l).catch(() => {});
    }
    this.ready.set(true);
  }

  private detectInitialLang(): SupportedLang {
    const saved = (typeof window !== 'undefined' ? localStorage.getItem(STORAGE_KEY) : null) as SupportedLang | null;
    if (saved && SUPPORTED.includes(saved)) return saved;
    const browser = (typeof navigator !== 'undefined' ? navigator.language.slice(0, 2) : 'en') as SupportedLang;
    return SUPPORTED.includes(browser) ? browser : 'en';
  }

  private async ensureLoaded(lang: SupportedLang): Promise<void> {
    if (this.dictionaries.has(lang)) return;
    const dict = await firstValueFrom(this.http.get<Record<string, any>>(`/assets/i18n/${lang}.json`));
    this.dictionaries.set(lang, dict);
  }

  async use(lang: SupportedLang): Promise<void> {
    await this.ensureLoaded(lang);
    this.currentLang.set(lang);
    localStorage.setItem(STORAGE_KEY, lang);
    document.documentElement.setAttribute('lang', lang);
  }

  /** Instant (non-reactive) lookup — use in .ts code, e.g. snackbar text. */
  instant(key: string, params?: Record<string, string | number>): string {
    const dict = this.dictionaries.get(this.currentLang()) ?? this.dictionaries.get('en');
    let value = key.split('.').reduce<any>((obj, part) => (obj ? obj[part] : undefined), dict);
    if (value === undefined) return key; // fall back to the key itself — never a blank UI
    if (params) {
      for (const [k, v] of Object.entries(params)) {
        value = value.replace(new RegExp(`{{\\s*${k}\\s*}}`, 'g'), String(v));
      }
    }
    return value;
  }
}
