import { Component, inject, input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { DocumentService } from '../../core/services/document.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

@Component({
  selector: 'app-generate-document',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule, MatFormFieldModule, MatSelectModule,
    MatInputModule, MatButtonModule, MatTableModule, MatProgressSpinnerModule,
    MatSnackBarModule, MatCardModule, MatIconModule, TranslatePipe
  ],
  templateUrl: './generate-document.component.html',
  styleUrl: './generate-document.component.scss'
})
export class GenerateDocumentComponent implements OnInit {
  customerId = input.required<string>();
  private docService = inject(DocumentService);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);

  templates = signal<any[]>([]);
  selectedTemplate = signal<string>('');
  lineItems = signal<any[]>([{ slNo: 1, item: '', type: '', qty: '', unit: '', rate: 0, gstPercent: '', amount: 0 }]);
  generatedFile = signal<string | null>(null);
  isLoading = signal(false);
  isLoadingTemplates = signal(true);
  error = signal<string | null>(null);
  displayedColumns = ['slNo', 'item', 'type', 'qty', 'unit', 'rate', 'gstPercent', 'amount', 'actions'];

  ngOnInit(): void {
    console.log('GenerateDocumentComponent initialized for customer:', this.customerId());
    this.loadTemplates();
  }

  loadTemplates() {
    console.log('Loading templates from API...');
    this.isLoadingTemplates.set(true);
    this.error.set(null);

    this.docService.listTemplates().subscribe({
      next: (templates) => {
        console.log('Templates loaded successfully:', templates);
        
        if (templates && Array.isArray(templates) && templates.length > 0) {
          this.templates.set(templates);
          
          // Set first template as default
          const defaultTemplate = templates.find(t => t.code === 'QUOTATION') || templates[0];
          this.selectedTemplate.set(defaultTemplate.code);
          console.log('Selected default template:', defaultTemplate.code);
          
          this.isLoadingTemplates.set(false);
        } else {
          const msg = 'No templates available';
          console.warn(msg);
          this.error.set(msg);
          this.snackBar.open(msg, 'Close', { duration: 5000, panelClass: ['error-snackbar'] });
          this.isLoadingTemplates.set(false);
        }
      },
      error: (err) => {
        console.error('Failed to load templates:', err);
        const msg = err?.error?.message || err?.message || 'Failed to load templates. Please ensure the backend is running on http://localhost:8080';
        this.error.set(msg);
        this.isLoadingTemplates.set(false);
        this.snackBar.open(msg, 'Close', { duration: 7000, panelClass: ['error-snackbar'] });
      }
    });
  }

  addLine() {
    this.lineItems.update(items => {
      const newLine = {
        slNo: items.length + 1,
        item: '',
        type: '',
        qty: '',
        unit: '',
        rate: 0,
        gstPercent: '',
        amount: 0
      };
      console.log('Adding line item:', newLine);
      return [...items, newLine];
    });
  }

  removeLine(index: number) {
    console.log('Removing line item at index:', index);
    this.lineItems.update(items => {
      const updated = items.filter((_, i) => i !== index);
      // Re-index serial numbers
      return updated.map((item, idx) => ({ ...item, slNo: idx + 1 }));
    });
  }

  updateAmount(index: number) {
    this.lineItems.update(items => {
      const item = items[index];
      if (item.rate && item.qty) {
        const qty = parseFloat(item.qty) || 0;
        const rate = parseFloat(item.rate) || 0;
        item.amount = qty * rate;
        console.log(`Updated amount for line ${index}: ${item.amount}`);
      }
      return [...items];
    });
  }

  generate() {
    console.log('Generate button clicked');
    this.error.set(null);

    if (!this.selectedTemplate() || this.selectedTemplate().trim() === '') {
      const msg = 'Please select a template';
      console.warn(msg);
      this.snackBar.open(msg, 'Close', { duration: 3000 });
      return;
    }

    const templateCode = this.selectedTemplate();
    console.log('Generating document with template:', templateCode);

    // Validate line items for templates that require them
    if ((templateCode === 'QUOTATION' || templateCode === 'INVOICE') &&
        (!this.lineItems() || this.lineItems().length === 0)) {
      const msg = 'Please add at least one line item';
      console.warn(msg);
      this.snackBar.open(msg, 'Close', { duration: 3000 });
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);

    const payload = {
      lineItems: this.lineItems()
    };

    console.log('Sending generation request:', {
      customerId: this.customerId(),
      template: templateCode,
      payload
    });

    this.docService.generate(this.customerId(), templateCode, payload).subscribe({
      next: (doc) => {
        console.log('Document generated successfully:', doc);
        this.isLoading.set(false);
        this.generatedFile.set(doc.filePath || doc.toString());
        
        const successMsg = 'Document generated successfully!';
        this.snackBar.open(successMsg, 'Close', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
        
        // Reset form after successful generation
        setTimeout(() => {
          this.generatedFile.set(null);
        }, 5000);
      },
      error: (err) => {
        console.error('Document generation failed:', err);
        this.isLoading.set(false);
        
        let errMsg = 'Failed to generate document';
        if (err?.error?.message) {
          errMsg = err.error.message;
        } else if (err?.error?.data) {
          errMsg = err.error.data;
        } else if (err?.message) {
          errMsg = err.message;
        } else if (err?.status) {
          errMsg = `HTTP ${err.status}: ${err.statusText}`;
        }
        
        this.error.set(errMsg);
        this.snackBar.open(errMsg, 'Close', { duration: 7000, panelClass: ['error-snackbar'] });
      }
    });
  }

  /**
   * Trigger a refresh of templates if needed
   */
  retryLoadTemplates() {
    console.log('Retrying template load...');
    this.loadTemplates();
  }
}
