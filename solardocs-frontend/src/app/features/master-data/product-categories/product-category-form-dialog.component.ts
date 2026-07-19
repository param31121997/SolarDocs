import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { ProductCategory } from '../../../core/models/product-category.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

export interface ProductCategoryFormDialogData {
  category: ProductCategory | null; // null = Add, present = Edit
}

export interface ProductCategoryFormDialogResult {
  categoryName: string;
  description: string;
}

/**
 * Single dialog reused for both Add and Edit - the only difference is
 * whether MAT_DIALOG_DATA.category is null (Add) or populated (Edit),
 * matching how the module brief treats them as one form with two entry
 * points rather than two separate screens.
 */
@Component({
  selector: 'app-product-category-form-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, TranslatePipe
  ],
  templateUrl: './product-category-form-dialog.component.html'
})
export class ProductCategoryFormDialogComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<ProductCategoryFormDialogComponent, ProductCategoryFormDialogResult>);
  data = inject<ProductCategoryFormDialogData>(MAT_DIALOG_DATA);

  isEditMode = this.data.category !== null;

  form = this.fb.group({
    categoryName: [this.data.category?.categoryName ?? '', [Validators.required, Validators.maxLength(100)]],
    description: [this.data.category?.description ?? '', [Validators.maxLength(500)]]
  });

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close({
      categoryName: this.form.value.categoryName!.trim(),
      description: this.form.value.description?.trim() ?? ''
    });
  }

  cancel() {
    this.dialogRef.close();
  }
}
