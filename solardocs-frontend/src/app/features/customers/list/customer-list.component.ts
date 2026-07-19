import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomerSummary } from '../../../core/models/customer.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink, MatTableModule, MatButtonModule,
    MatInputModule, MatFormFieldModule, MatIconModule, MatCardModule,
    MatProgressBarModule, MatTooltipModule, TranslatePipe
  ],
  templateUrl: './customer-list.component.html',
  styleUrl: './customer-list.component.scss'
})
export class CustomerListComponent {
  private customerService = inject(CustomerService);

  customers = signal<CustomerSummary[]>([]);
  columns = ['customerId', 'name', 'mobile', 'village', 'status', 'actions'];
  isLoading = signal(false);
  searchQuery = signal('');

  constructor() { this.reload(); }

  reload(q?: string) {
    this.isLoading.set(true);
    this.customerService.list(q).subscribe({
      next: (list) => {
        this.customers.set(list);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  onSearch(value: string) {
    this.searchQuery.set(value);
    this.reload(value);
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }
}
