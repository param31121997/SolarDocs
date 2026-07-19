import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { VendorProfileService } from '../../core/services/vendor-profile.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';
import { LanguageSwitcherComponent } from '../../shared/components/language-switcher/language-switcher.component';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, TranslatePipe, LanguageSwitcherComponent
  ],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss'
})
export class SettingsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private vendorProfileService = inject(VendorProfileService);

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

  ngOnInit() {
    this.vendorProfileService.get().subscribe(p => { if (p) this.form.patchValue(p); });
  }

  save() {
    this.vendorProfileService.update(this.form.value as any).subscribe();
  }
}
