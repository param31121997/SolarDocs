import { Component, inject, input, OnInit, signal, effect } from '@angular/core';
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
import { ItemService } from '../../core/services/item.service';
import { Item } from '../../core/models/item.model';
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
  private itemService = inject(ItemService);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);

  templates = signal<any[]>([]);
  selectedTemplate = signal<string>('');
  lineItems = signal<any[]>([{ slNo: 1, item: '', type: '', qty: '', unit: '', rate: 0, gstPercent: '', amount: 0 }]);
  generatedFile = signal<string | null>(null);
  isLoading = signal(false);
  isLoadingTemplates = signal(true);
  error = signal<string | null>(null);
  displayedColumns = ['slNo', 'product', 'item', 'type', 'qty', 'unit', 'rate', 'gstPercent', 'amount', 'actions'];

  catalog = signal<Item[]>([]);
  isLoadingCatalog = signal(true);
  private catalogAutoPopulated = false;

  constructor() {
    // Whenever both the catalog and a QUOTATION/INVOICE template selection
    // are available, and the line-item table is still in its untouched
    // default state, prefill it from the vendor's product catalog. This
    // only fires once per visit (catalogAutoPopulated) so it never
    // clobbers edits the vendor has already started making - after that,
    // "Load from Catalog" (added to the template) is there to re-run it
    // on demand.
    effect(() => {
      const template = this.selectedTemplate();
      const products = this.catalog();
      const isQuotationLike = template === 'QUOTATION' || template === 'INVOICE';

      if (isQuotationLike && products.length > 0 && !this.catalogAutoPopulated && this.isDefaultLineItemsState()) {
        this.loadFromCatalog();
        this.catalogAutoPopulated = true;
      }
    });
  }

  ngOnInit(): void {
    console.log('GenerateDocumentComponent initialized for customer:', this.customerId());
    this.loadTemplates();
    this.loadCatalog();
  }

  loadCatalog() {
    this.isLoadingCatalog.set(true);
    this.itemService.list().subscribe({
      next: (items) => {
        console.log('Item Master catalog loaded:', items);
        this.catalog.set(items || []);
        this.isLoadingCatalog.set(false);
      },
      error: (err) => {
        // Catalog is a convenience prefill, not a hard requirement - a
        // failure here should not block manual document generation.
        console.warn('Failed to load Item Master, falling back to manual entry:', err);
        this.catalog.set([]);
        this.isLoadingCatalog.set(false);
      }
    });
  }

  /** True while the table still holds only the single, never-edited blank row. */
  private isDefaultLineItemsState(): boolean {
    const items = this.lineItems();
    return items.length === 1 && !items[0].item && !items[0].qty && !items[0].rate;
  }

  /** Replaces the line-item table with one row per Item Master entry (rate/type/unit prefilled, quantity left blank for the vendor to fill in). */
  loadFromCatalog() {
    const items = this.catalog();
    if (items.length === 0) {
      this.snackBar.open('Item Master is empty. Add items in Master Data > Items first.', 'Close', { duration: 4000 });
      return;
    }
    this.lineItems.set(items.map((it, idx) => ({
      slNo: idx + 1,
      productCode: it.id,
      item: it.itemName,
      type: it.type ?? '',
      qty: '',
      unit: it.unit ?? '',
      rate: it.defaultRate ?? 0,
      gstPercent: it.defaultGstPercent ?? '',
      amount: 0
    })));
  }

  /** Applies an Item Master item's defaults onto one existing row, keeping any quantity the vendor already typed. */
  applyProduct(index: number, id: string) {
    const item = this.catalog().find(it => it.id === id);
    if (!item) {
      return;
    }
    this.lineItems.update(items => {
      const updated = [...items];
      updated[index] = {
        ...updated[index],
        productCode: item.id,
        item: item.itemName,
        type: item.type ?? '',
        unit: item.unit ?? '',
        rate: item.defaultRate ?? 0,
        gstPercent: item.defaultGstPercent ?? ''
      };
      return updated;
    });
    this.updateAmount(index);
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
        productCode: '',
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
