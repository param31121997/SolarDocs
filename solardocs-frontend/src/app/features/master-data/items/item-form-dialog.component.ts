import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Item } from '../../../core/models/item.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

export interface ItemFormDialogData {
  item: Item | null; // null = Add, present = Edit
}

export interface ItemFormDialogResult {
  itemName: string;
  description: string;
}

/**
 * Single dialog reused for both Add and Edit - the only difference is
 * whether MAT_DIALOG_DATA.item is null (Add) or populated (Edit), matching
 * how the Product Category form dialog treats them as one form with two
 * entry points rather than two separate screens.
 */
@Component({
  selector: 'app-item-form-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, TranslatePipe
  ],
  templateUrl: './item-form-dialog.component.html'
})
export class ItemFormDialogComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<ItemFormDialogComponent, ItemFormDialogResult>);
  data = inject<ItemFormDialogData>(MAT_DIALOG_DATA);

  isEditMode = this.data.item !== null;

  form = this.fb.group({
    itemName: [this.data.item?.itemName ?? '', [Validators.required, Validators.maxLength(150)]],
    description: [this.data.item?.description ?? '', [Validators.maxLength(500)]]
  });

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close({
      itemName: this.form.value.itemName!.trim(),
      description: this.form.value.description?.trim() ?? ''
    });
  }

  cancel() {
    this.dialogRef.close();
  }
}
