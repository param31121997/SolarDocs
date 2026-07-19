import { Component, inject, input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DocumentService, UploadedDocument } from '../../core/services/document.service';

interface DocumentTypeOption {
  code: string;
  label: string;
  requiredFor: string;
}

@Component({
  selector: 'app-customer-documents',
  standalone: true,
  imports: [
    CommonModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule,
    MatInputModule, MatProgressSpinnerModule, MatSelectModule, MatSnackBarModule
  ],
  templateUrl: './customer-documents.component.html',
  styleUrl: './customer-documents.component.scss'
})
export class CustomerDocumentsComponent implements OnInit {
  customerId = input.required<string>();
  private readonly documentService = inject(DocumentService);
  private readonly snackBar = inject(MatSnackBar);

  readonly documentTypes: DocumentTypeOption[] = [
    { code: 'AADHAAR', label: 'Aadhaar card', requiredFor: 'Consumer identity' },
    { code: 'PAN', label: 'PAN card', requiredFor: 'Consumer identity' },
    { code: 'ELECTRICITY_BILL', label: 'Latest electricity bill', requiredFor: 'Application and consumer number' },
    { code: 'PROPERTY_DOCUMENT', label: 'Property / roof ownership document', requiredFor: 'Site eligibility' },
    { code: 'CUSTOMER_SIGNATURE', label: 'Consumer signature', requiredFor: 'Agreement and declarations' },
    { code: 'SITE_PHOTO_BEFORE', label: 'Site photo — before installation', requiredFor: 'Survey evidence' },
    { code: 'SITE_PHOTO_AFTER', label: 'Installed system photo', requiredFor: 'Commissioning evidence' },
    { code: 'METER_PHOTO', label: 'Meter photo', requiredFor: 'Net-metering documentation' },
    { code: 'MODULE_DATASHEET', label: 'PV module datasheet', requiredFor: 'DCR / commissioning documentation' },
    { code: 'INVERTER_DATASHEET', label: 'Inverter datasheet', requiredFor: 'DCR / commissioning documentation' },
    { code: 'WARRANTY_CERTIFICATE', label: 'Warranty certificate', requiredFor: 'Customer handover' }
  ];

  selectedType = signal(this.documentTypes[0].code);
  selectedFile = signal<File | null>(null);
  uploadedDocuments = signal<UploadedDocument[]>([]);
  isLoading = signal(true);
  isUploading = signal(false);

  ngOnInit(): void {
    this.loadDocuments();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.item(0) ?? null);
  }

  upload(): void {
    const file = this.selectedFile();
    if (!file) {
      this.snackBar.open('Choose a file before uploading.', 'Close', { duration: 3000 });
      return;
    }

    this.isUploading.set(true);
    this.documentService.upload(this.customerId(), this.selectedType(), file).subscribe({
      next: document => {
        this.uploadedDocuments.update(current => [document, ...current]);
        this.selectedFile.set(null);
        this.isUploading.set(false);
        this.snackBar.open('Document uploaded successfully.', 'Close', { duration: 3000 });
      },
      error: error => {
        this.isUploading.set(false);
        this.snackBar.open(error?.error?.error?.message ?? 'Document upload failed.', 'Close', { duration: 5000 });
      }
    });
  }

  labelFor(type: string): string {
    return this.documentTypes.find(option => option.code === type)?.label ?? type;
  }

  hasDocument(type: string): boolean {
    return this.uploadedDocuments().some(document => document.type === type);
  }

  private loadDocuments(): void {
    this.documentService.listUploaded(this.customerId()).subscribe({
      next: documents => {
        this.uploadedDocuments.set(documents);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('Unable to load customer documents.', 'Close', { duration: 5000 });
      }
    });
  }
}
