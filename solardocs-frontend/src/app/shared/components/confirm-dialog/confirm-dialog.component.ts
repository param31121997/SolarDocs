import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

export interface ConfirmDialogData {
  titleKey: string;
  messageKey: string;
  messageParams?: Record<string, string | number>;
  confirmKey?: string;
  cancelKey?: string;
}

/**
 * Generic Yes/No confirmation dialog - not category-specific, so any
 * future destructive action (Products, Inventory adjustments, ...) can
 * reuse it by passing different translation keys instead of a new dialog.
 */
@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, TranslatePipe],
  templateUrl: './confirm-dialog.component.html'
})
export class ConfirmDialogComponent {
  private dialogRef = inject(MatDialogRef<ConfirmDialogComponent, boolean>);
  data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);

  confirm() { this.dialogRef.close(true); }
  cancel() { this.dialogRef.close(false); }
}
