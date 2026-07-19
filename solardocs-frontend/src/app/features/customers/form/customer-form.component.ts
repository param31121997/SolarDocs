import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CustomerService } from '../../../core/services/customer.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { Customer } from '../../../core/models/customer.model';

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, TranslatePipe],
  templateUrl: './customer-form.component.html',
  styleUrl: './customer-form.component.scss'
})
export class CustomerFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private customerService = inject(CustomerService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  editingCustomerId = signal<string | null>(null);

  form = this.fb.group({
    name: ['', Validators.required],
    mobile: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    alternateMobile: ['', Validators.pattern(/^$|\d{10}$/)],
    addressLine: [''],
    village: [''],
    district: [''],
    state: [''],
    pincode: ['', Validators.pattern(/^$|\d{6}$/)],
    consumerNumber: [''],
    applicationNumber: [''],
    sanctionedLoadKw: [null as number | null],
    plantCapacityKw: [null as number | null],
    discom: [''],
    category: ['RESIDENTIAL']
  });

  ngOnInit(): void {
    const customerId = this.route.snapshot.paramMap.get('id');
    if (!customerId) {
      return;
    }
    this.editingCustomerId.set(customerId);
    this.customerService.get(customerId).subscribe(customer => this.form.patchValue(customer));
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    const editingId = this.editingCustomerId();
    const request = editingId
      ? this.customerService.update(editingId, value as Partial<Customer>)
      : this.customerService.create({
          name: value.name ?? '', mobile: value.mobile ?? '', addressLine: value.addressLine ?? '',
          village: value.village ?? '', district: value.district ?? '', state: value.state ?? '',
          pincode: value.pincode ?? ''
        });
    request.subscribe(customer => this.router.navigate(['/customers', customer.customerId]));
  }
}
