import { Component, inject, input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CustomerService } from '../../../core/services/customer.service';
import { Customer } from '../../../core/models/customer.model';

interface FieldDef {
  key: string;   // form control name, matches PlantInstallationDetails field name
  label: string;
  type?: 'text' | 'date';
}

/**
 * These are filled in ONCE per customer here, then every compliance
 * document (Work Completion Report, Guarantee Certificate, Annexure-I,
 * Proforma-A, DCR Declaration, Net Meter Agreement) pulls from these
 * same values automatically - see FieldResolver.java / PlantInstallationDetails.java.
 * Grouped to roughly match the order these facts get collected on-site.
 */
const FIELD_GROUPS: { title: string; fields: FieldDef[] }[] = [
  {
    title: 'Consumer & Installation',
    fields: [
      { key: 'email', label: 'Consumer Email' },
      { key: 'aadhaarNumber', label: 'Aadhaar Number' },
      { key: 'installationDate', label: 'Installation Date', type: 'date' },
    ]
  },
  {
    title: 'Module',
    fields: [
      { key: 'moduleWattage', label: 'Wattage per Module (W)' },
      { key: 'moduleCount', label: 'No. of Modules' },
      { key: 'moduleCapacityKw', label: 'Module Capacity (kW)' },
      { key: 'moduleSerialNumbers', label: 'Module Serial Numbers' },
      { key: 'cellManufacturerName', label: 'Cell Manufacturer Name' },
      { key: 'cellGstInvoiceNo', label: 'Cell GST Invoice No.' },
    ]
  },
  {
    title: 'Inverter & Earthing',
    fields: [
      { key: 'inverterMake', label: 'Inverter Make' },
      { key: 'inverterRating', label: 'Inverter Rating' },
      { key: 'inverterCapacityKw', label: 'Inverter Capacity (kW)' },
      { key: 'chargeControllerType', label: 'Charge Controller Type' },
      { key: 'hpd', label: 'HPD' },
      { key: 'earthing1Ohms', label: 'Earthing Pit 1 (Ω)' },
      { key: 'earthing2Ohms', label: 'Earthing Pit 2 (Ω)' },
      { key: 'earthing3Ohms', label: 'Earthing Pit 3 (Ω)' },
    ]
  },
  {
    title: 'Inspection & Net Metering',
    fields: [
      { key: 'inspectionDate', label: 'Inspection Date', type: 'date' },
      { key: 'inspectionLetterNo', label: 'Inspection Letter No.' },
      { key: 'inspectionLetterDate', label: 'Inspection Letter Date', type: 'date' },
      { key: 'agreementPlace', label: 'Net Metering Agreement - Place' },
      { key: 'netMeterSerialNo', label: 'Net Meter Serial No.' },
    ]
  }
];

@Component({
  selector: 'app-plant-details-form',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule, MatSnackBarModule, MatProgressSpinnerModule
  ],
  templateUrl: './plant-details-form.component.html',
  styleUrl: './plant-details-form.component.scss'
})
export class PlantDetailsFormComponent implements OnInit {
  customerId = input.required<string>();

  private customerService = inject(CustomerService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);

  groups = FIELD_GROUPS;
  form: FormGroup = this.fb.group({});
  isLoading = signal(true);
  isSaving = signal(false);
  loadError = signal<string | null>(null);

  ngOnInit() {
    const controls: Record<string, any> = {};
    for (const group of this.groups) {
      for (const field of group.fields) controls[field.key] = [''];
    }
    this.form = this.fb.group(controls);
    this.load();
  }

  private load() {
    this.isLoading.set(true);
    this.loadError.set(null);
    this.customerService.get(this.customerId()).subscribe({
      next: (c: Customer) => {
        this.form.patchValue({
          email: c.plantEmail ?? '',
          aadhaarNumber: c.plantAadhaarNumber ?? '',
          installationDate: c.plantInstallationDate ?? '',
          moduleWattage: c.plantModuleWattage ?? '',
          moduleCount: c.plantModuleCount ?? '',
          moduleCapacityKw: c.plantModuleCapacityKw ?? '',
          moduleSerialNumbers: c.plantModuleSerialNumbers ?? '',
          cellManufacturerName: c.plantCellManufacturerName ?? '',
          cellGstInvoiceNo: c.plantCellGstInvoiceNo ?? '',
          inverterMake: c.plantInverterMake ?? '',
          inverterRating: c.plantInverterRating ?? '',
          inverterCapacityKw: c.plantInverterCapacityKw ?? '',
          chargeControllerType: c.plantChargeControllerType ?? '',
          hpd: c.plantHpd ?? '',
          earthing1Ohms: c.plantEarthing1Ohms ?? '',
          earthing2Ohms: c.plantEarthing2Ohms ?? '',
          earthing3Ohms: c.plantEarthing3Ohms ?? '',
          inspectionDate: c.plantInspectionDate ?? '',
          inspectionLetterNo: c.plantInspectionLetterNo ?? '',
          inspectionLetterDate: c.plantInspectionLetterDate ?? '',
          agreementPlace: c.plantAgreementPlace ?? '',
          netMeterSerialNo: c.plantNetMeterSerialNo ?? '',
        });
        this.isLoading.set(false);
      },
      error: (err) => {
        this.loadError.set(err?.error?.message || 'Failed to load plant details');
        this.isLoading.set(false);
      }
    });
  }

  save() {
    this.isSaving.set(true);
    this.customerService.updatePlantDetails(this.customerId(), this.form.value).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.snackBar.open('Plant details saved. Every compliance document will use these values.', 'Close', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
      },
      error: (err) => {
        this.isSaving.set(false);
        this.snackBar.open(err?.error?.message || 'Failed to save plant details', 'Close', {
          duration: 6000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
