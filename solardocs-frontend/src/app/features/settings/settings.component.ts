import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormArray, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { VendorProfileService } from '../../core/services/vendor-profile.service';
import { ProductCatalogService, Product } from '../../core/services/product-catalog.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';
import { LanguageSwitcherComponent } from '../../shared/components/language-switcher/language-switcher.component';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatSnackBarModule, TranslatePipe, LanguageSwitcherComponent
  ],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss'
})
export class SettingsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private vendorProfileService = inject(VendorProfileService);
  private productCatalogService = inject(ProductCatalogService);
  private snackBar = inject(MatSnackBar);

  form = this.fb.group({
    companyName: [''],
    gstin: [''],
    registeredAddress: [''],
    logoPath: [''],
    bankAccountName: [''],
    bankAccountNumber: [''],
    bankIfsc: [''],
    signatoryName: ['']
  });

  productForm = this.fb.group({
    products: this.fb.array<FormGroup>([])
  });

  get products(): FormArray<FormGroup> {
    return this.productForm.get('products') as FormArray<FormGroup>;
  }

  ngOnInit() {
    this.vendorProfileService.get().subscribe(p => { if (p) this.form.patchValue(p); });
    this.loadCatalog();
  }

  save() {
    this.vendorProfileService.update(this.form.value as any).subscribe(() => {
      this.snackBar.open('Vendor profile saved', 'Close', { duration: 3000 });
    });
  }

  loadCatalog() {
    this.productCatalogService.list().subscribe(products => {
      this.products.clear();
      (products || []).forEach(p => this.products.push(this.buildProductGroup(p)));
    });
  }

  private buildProductGroup(product?: Product): FormGroup {
    return this.fb.group({
      code: [product?.code ?? '', Validators.required],
      name: [product?.name ?? '', Validators.required],
      type: [product?.type ?? ''],
      unit: [product?.unit ?? ''],
      defaultRate: [product?.defaultRate ?? 0],
      defaultGstPercent: [product?.defaultGstPercent ?? '']
    });
  }

  addProduct() {
    this.products.push(this.buildProductGroup());
  }

  removeProduct(index: number) {
    this.products.removeAt(index);
  }

  saveCatalog() {
    if (this.productForm.invalid) {
      this.snackBar.open('Every product needs at least a code and a name', 'Close', { duration: 4000 });
      return;
    }
    const products = this.products.value as Product[];
    this.productCatalogService.save(products).subscribe({
      next: () => this.snackBar.open('Product catalog saved', 'Close', { duration: 3000 }),
      error: () => this.snackBar.open('Failed to save product catalog', 'Close', { duration: 4000 })
    });
  }
}
