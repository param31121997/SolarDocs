import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatCardModule } from '@angular/material/card';
import { BackupService } from '../../core/services/backup.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';
import { TranslateService } from '../../core/i18n/translate.service';

@Component({
  selector: 'app-backup',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatListModule, MatCardModule, TranslatePipe],
  templateUrl: './backup.component.html',
  styleUrl: './backup.component.scss'
})
export class BackupComponent {
  private backupService = inject(BackupService);
  private translate = inject(TranslateService);

  backups = signal<string[]>([]);

  constructor() { this.reload(); }

  reload() { this.backupService.list().subscribe(list => this.backups.set(list)); }

  create() { this.backupService.create().subscribe(() => this.reload()); }

  restore(fileName: string) {
    if (confirm(this.translate.instant('backup.confirmRestore'))) {
      this.backupService.restore(fileName).subscribe(() => this.reload());
    }
  }
}
