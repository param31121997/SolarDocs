import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { CustomerService } from '../../../core/services/customer.service';
import { Customer } from '../../../core/models/customer.model';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { GenerateDocumentComponent } from '../../documents/generate-document.component';
import { CustomerDocumentsComponent } from '../../documents/customer-documents.component';
import { PlantDetailsFormComponent } from '../plant-details/plant-details-form.component';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [
    CommonModule, MatTabsModule, MatButtonModule, MatFormFieldModule, MatSelectModule,
    MatSnackBarModule, TranslatePipe, GenerateDocumentComponent, CustomerDocumentsComponent,
    PlantDetailsFormComponent
  ],
  templateUrl: './customer-detail.component.html',
  styleUrl: './customer-detail.component.scss'
})
export class CustomerDetailComponent {
  private route = inject(ActivatedRoute);
  private customerService = inject(CustomerService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  customer = signal<Customer | null>(null);
  readonly statuses = [
    'LEAD', 'QUOTATION_SENT', 'AGREEMENT_SIGNED', 'DOCUMENTS_COLLECTED',
    'INSTALLATION_IN_PROGRESS', 'COMMISSIONED', 'SUBSIDY_APPLIED',
    'SUBSIDY_RECEIVED', 'CLOSED'
  ];

  constructor() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.customerService.get(id).subscribe(c => this.customer.set(c));
  }

  editCustomer(): void {
    const customer = this.customer();
    if (customer) {
      this.router.navigate(['/customers', customer.customerId, 'edit']);
    }
  }

  updateStatus(status: string): void {
    const customer = this.customer();
    if (!customer || status === customer.status) {
      return;
    }
    this.customerService.updateStatus(customer.customerId, status).subscribe({
      next: updated => {
        this.customer.set(updated);
        this.snackBar.open('Customer status updated.', 'Close', { duration: 3000 });
      },
      error: () => this.snackBar.open('Unable to update customer status.', 'Close', { duration: 5000 })
    });
  }
}
