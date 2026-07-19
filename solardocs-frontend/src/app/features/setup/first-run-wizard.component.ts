import { Component, inject, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SetupService, SetupStatus } from '../../core/services/setup.service';

@Component({
  selector: 'app-first-run-wizard',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule
  ],
  templateUrl: './first-run-wizard.component.html',
  styleUrl: './first-run-wizard.component.scss'
})
export class FirstRunWizardComponent {
  private setupService = inject(SetupService);

  /** Emitted once the folder is saved, so the parent can show the "restart needed" state. */
  saved = output<void>();

  currentDefault = signal<string>('');
  chosenPath = signal<string>('');
  isSaving = signal(false);
  isSaved = signal(false);
  errorMessage = signal<string | null>(null);

  constructor() {
    this.setupService.status().subscribe((status: SetupStatus) => {
      this.currentDefault.set(status.currentDataDir);
      this.chosenPath.set(status.currentDataDir);
    });
  }

  useDefault() {
    this.chosenPath.set(this.currentDefault());
  }

  save() {
    const path = this.chosenPath().trim();
    if (!path) {
      this.errorMessage.set('Please enter a folder path.');
      return;
    }
    this.isSaving.set(true);
    this.errorMessage.set(null);

    this.setupService.chooseDataDirectory(path).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.isSaved.set(true);
        this.saved.emit();
      },
      error: (err) => {
        this.isSaving.set(false);
        this.errorMessage.set(
          err?.error?.error?.message || err?.message || 'Could not use that folder. Check the path and permissions.'
        );
      }
    });
  }
}
