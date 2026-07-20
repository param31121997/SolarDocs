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
import { ItemService } from '../../../core/services/item.service';
import { Item } from '../../../core/models/item.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { TranslateService } from '../../../core/i18n/translate.service';
import { ItemFormDialogComponent, ItemFormDialogResult } from './item-form-dialog.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-item-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatTableModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatCardModule, MatProgressBarModule,
    MatSlideToggleModule, MatTooltipModule, MatDialogModule, MatSnackBarModule,
    TranslatePipe
  ],
  templateUrl: './item-list.component.html',
  styleUrl: './item-list.component.scss'
})
export class ItemListComponent {
  private itemService = inject(ItemService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private translate = inject(TranslateService);

  items = signal<Item[]>([]);
  columns = ['itemName', 'description', 'active', 'createdAt', 'actions'];
  isLoading = signal(false);
  searchQuery = signal('');
  includeInactive = signal(false);

  constructor() { this.reload(); }

  reload() {
    this.isLoading.set(true);
    this.itemService.list(this.searchQuery(), this.includeInactive()).subscribe({
      next: (list) => { this.items.set(list); this.isLoading.set(false); },
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
    const ref = this.dialog.open<ItemFormDialogComponent, unknown, ItemFormDialogResult>(
      ItemFormDialogComponent,
      { width: '480px', data: { item: null } }
    );
    ref.afterClosed().subscribe(result => {
      if (!result) return;
      this.itemService.create(result).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('item.created'), undefined, { duration: 2500 });
          this.reload();
        },
        error: (err) => this.showError(err)
      });
    });
  }

  openEditDialog(item: Item) {
    const ref = this.dialog.open<ItemFormDialogComponent, unknown, ItemFormDialogResult>(
      ItemFormDialogComponent,
      { width: '480px', data: { item } }
    );
    ref.afterClosed().subscribe(result => {
      if (!result) return;
      this.itemService.update(item.id, result).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('item.updated'), undefined, { duration: 2500 });
          this.reload();
        },
        error: (err) => this.showError(err)
      });
    });
  }

  onToggleActive(item: Item, event: MatSlideToggleChange) {
    this.itemService.setActive(item.id, event.checked).subscribe({
      next: () => this.reload(),
      error: (err) => { event.source.checked = !event.checked; this.showError(err); }
    });
  }

  confirmDelete(item: Item) {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        titleKey: 'item.deleteTitle',
        messageKey: 'item.deleteConfirm',
        messageParams: { name: item.itemName }
      }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.itemService.delete(item.id).subscribe({
        next: () => {
          this.snackBar.open(this.translate.instant('item.deleted'), undefined, { duration: 2500 });
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
