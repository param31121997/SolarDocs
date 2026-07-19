import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSlideToggleModule, MatSlideToggleChange } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ProductCategoryService } from '../../../core/services/product-category.service';
import { ProductCategory } from '../../../core/models/product-category.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { TranslateService } from '../../../core/i18n/translate.service';
import {
  ProductCategoryFormDialogComponent,
  ProductCategoryFormDialogResult
} from './product-category-form-dialog.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-product-category-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatTableModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatCardModule, MatProgressBarModule,
    MatSlideToggleModule, MatTooltipModule, MatDialogModule, MatSnackBarModule,
    TranslatePipe
  ],
  templateUrl: './product-category-list.component.html',
  styleUrl: './product-category-list.component.scss'
})
export class ProductCategoryListComponent {
  private categoryService = inject(ProductCategoryService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private translate = inject(TranslateService);

  categories = signal<ProductCategory[]>([]);
  columns = ['categoryCode', 'categoryName', 'description', 'active', 'createdAt', 'actions'];
  isLoading = signal(false);
  searchQuery = signal('');
  includeInactive = signal(false);

  constructor() { this.reload(); }

  reload() {
    this.isLoading.set(true);
    this.categoryService.list(this.searchQuery(), this.includeInactive()).subscribe({
      next: (list) => { this.categories.set(list); this.isLoading.set(false); },
      error: () => this.isLoading.set(false)
    });
  }

  onSearch(value: string) {
    this.searchQuery.set(value);
    this.reload();
  }

  onToggleIncludeInactive(checked: boolean) {
    this.includeInactive.set(checked);
    this.reload();
  }

  openAddDialog() {
    const ref = this.dialog.open<ProductCategoryFormDialogComponent, unknown, ProductCategoryFormDialogResult>(
      ProductCategoryFormDialogComponent,
      { width: '480px', data: { category: null } }
    );
    ref.afterClosed().subscribe(result => {
      if (!result) return;
      this.categoryService.create(result).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('productCategory.created'), undefined, { duration: 2500 });
          this.reload();
        },
        error: (err) => this.showError(err)
      });
    });
  }

  openEditDialog(category: ProductCategory) {
    const ref = this.dialog.open<ProductCategoryFormDialogComponent, unknown, ProductCategoryFormDialogResult>(
      ProductCategoryFormDialogComponent,
      { width: '480px', data: { category } }
    );
    ref.afterClosed().subscribe(result => {
      if (!result) return;
      this.categoryService.update(category.id, result).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('productCategory.updated'), undefined, { duration: 2500 });
          this.reload();
        },
        error: (err) => this.showError(err)
      });
    });
  }

  onToggleActive(category: ProductCategory, event: MatSlideToggleChange) {
    this.categoryService.setActive(category.id, event.checked).subscribe({
      next: () => this.reload(),
      error: (err) => { event.source.checked = !event.checked; this.showError(err); }
    });
  }

  confirmDelete(category: ProductCategory) {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        titleKey: 'productCategory.deleteTitle',
        messageKey: 'productCategory.deleteConfirm',
        messageParams: { name: category.categoryName }
      }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.categoryService.delete(category.id).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('productCategory.deleted'), undefined, { duration: 2500 });
          this.reload();
        },
        error: (err) => this.showError(err)
      });
    });
  }

  private showError(err: any) {
    const message = err?.error?.error?.message ?? this.translate.instant('common.unexpectedError');
    this.snackBar.open(message, undefined, { duration: 3500 });
  }
}
