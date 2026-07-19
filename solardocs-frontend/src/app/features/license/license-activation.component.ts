import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { LicenseService, License } from '../../core/services/license.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

@Component({
  selector: 'app-license-activation',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatCardModule, TranslatePipe],
  templateUrl: './license-activation.component.html',
  styleUrl: './license-activation.component.scss'
})
export class LicenseActivationComponent {
  private licenseService = inject(LicenseService);

  licenseKey = '';
  status = signal<License | null>(null);

  constructor() { this.refresh(); }

  refresh() { this.licenseService.status().subscribe(s => this.status.set(s)); }

  activate() { this.licenseService.activate(this.licenseKey).subscribe(() => this.refresh()); }
}
