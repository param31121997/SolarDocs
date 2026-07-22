import { Component, inject, input, OnInit, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { environment } from '../../../environments/environment';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { DocumentService } from '../../core/services/document.service';
import { ItemService } from '../../core/services/item.service';
import { Item } from '../../core/models/item.model';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

/** One field on the "Additional Details" panel for a non-quotation/invoice template. `default` mirrors the fallback the backend strategy applies via extraFields.getOrDefault(...) - present it means the field is a "default + dynamic" (yellow) value; absent means it's purely dynamic (red) and starts blank. */
export interface ExtraFieldDef {
  key: string;
  label: string;
  default?: string;
  type?: 'text' | 'date';
}

/** Per-template extra field definitions, matching the Map keys each *GenerationStrategy.java reads via extraFields. Fields already available on the Customer Details screen (name, consumer number, address, capacity, DISCOM, application number, mobile) are NOT listed here - the backend pulls those straight from the Customer record. */
export const EXTRA_FIELD_DEFS: Record<string, ExtraFieldDef[]> = {
  WORK_COMPLETION_REPORT: [
    { key: 'vendorCompanyName', label: 'Vendor Company Name (blank = use Settings > Vendor Profile)' },
    { key: 'category', label: 'Category (Govt/Private)', default: 'Private' },
    { key: 'sanctionNumber', label: 'Sanction Number (blank = use Application Number)' },
    { key: 'installationDate', label: 'Installation Date', type: 'date' },
    { key: 'moduleMake', label: 'Module Make', default: 'Mundra Solar PV Limited (Adani)' },
    { key: 'almmModelNumber', label: 'ALMM Model Number', default: 'ASB-M10-144' },
    { key: 'moduleWattage', label: 'Wattage per Module (W)' },
    { key: 'moduleCount', label: 'No. of Modules' },
    { key: 'moduleTotalCapacityKwp', label: 'Total Module Capacity (KWP) (blank = use Plant Capacity)' },
    { key: 'warrantyDetails', label: 'Warranty Details (Product + Performance)', default: '25 years' },
    { key: 'inverterMake', label: 'Inverter Make & Model' },
    { key: 'inverterRating', label: 'Inverter Rating' },
    { key: 'chargeControllerType', label: 'Type of Charge Controller / MPPT' },
    { key: 'inverterCapacityKw', label: 'Capacity of Inverter (KW)' },
    { key: 'hpd', label: 'HPD' },
    { key: 'yearOfManufacturing', label: 'Year of Manufacturing', default: String(new Date().getFullYear()) },
    { key: 'earthing1Ohms', label: 'Earthing 1 (Ohms)' },
    { key: 'earthing2Ohms', label: 'Earthing 2 (Ohms)' },
    { key: 'earthing3Ohms', label: 'Earthing 3 (Ohms)' },
    { key: 'earthResistanceCertified', label: 'Earth Resistance Certified (<5 Ohms)', default: 'Yes' },
    { key: 'lightningArrester', label: 'Lightning Arrester', default: 'Yes' }
  ],
  GUARANTEE_CERTIFICATE: [
    { key: 'vendorCompanyName', label: 'Vendor Company Name (blank = use Settings > Vendor Profile)' },
    { key: 'cmcYears', label: 'CMC Period (Years)', default: '5' },
    { key: 'aadhaarNumber', label: 'Consumer Aadhaar Number' }
  ],
  ANNEXURE_I: [
    { key: 'email', label: 'Consumer E-mail' },
    { key: 'installationDate', label: 'Installation Date', type: 'date' },
    { key: 'inverterCapacityKw', label: 'Inverter Capacity (KW)' },
    { key: 'inverterMake', label: 'Inverter Make' },
    { key: 'moduleCount', label: 'No. of PV Modules' },
    { key: 'moduleCapacityKw', label: 'Module Capacity (KW)' },
    { key: 'reArrangementType', label: 'RE Arrangement Type', default: 'Net Metering Arrangement' },
    { key: 'reSource', label: 'RE Source', default: 'Solar' },
    { key: 'capacityType', label: 'Capacity Type', default: 'Rooftop' },
    { key: 'projectModel', label: 'Project Model', default: 'capex' },
    { key: 'reInstalledCapacityRooftopGroundKw', label: 'RE Installed Capacity - Rooftop+Ground (KW)', default: 'NA' },
    { key: 'reInstalledCapacityGroundKw', label: 'RE Installed Capacity - Ground (KW)', default: 'NA' }
  ],
  COMMISSIONING_REPORT: [
    { key: 'vendorCompanyName', label: 'Vendor Company Name (blank = use Settings > Vendor Profile)' },
    { key: 'installationDate', label: 'Installation Date', type: 'date' },
    { key: 'inspectionDate', label: 'Pre-Commissioning Inspection Date', type: 'date' },
    { key: 'inspectionLetterNo', label: 'Inspection Guideline Letter No.' },
    { key: 'inspectionLetterDate', label: 'Inspection Guideline Letter Date', type: 'date' }
  ],
  DCR_DECLARATION: [
    { key: 'vendorCompanyName', label: 'Vendor Company Name (blank = use Settings > Vendor Profile)' },
    { key: 'vendorSignatoryName', label: 'Vendor Signatory Name (blank = use Settings > Vendor Profile)' },
    { key: 'vendorPhone', label: 'Vendor Phone (blank = use Settings > Vendor Profile)' },
    { key: 'vendorEmail', label: 'Vendor Email (blank = use Settings > Vendor Profile)' },
    { key: 'installationDate', label: 'Installation Date', type: 'date' },
    { key: 'moduleCapacityKwp', label: 'Module Capacity (KWp) (blank = use Plant Capacity)' },
    { key: 'moduleCount', label: 'No. of PV Modules' },
    { key: 'moduleSerialNumbers', label: 'PV Module Serial Numbers (comma-separated)' },
    { key: 'cellManufacturerName', label: "Cell Manufacturer's Name" },
    { key: 'cellGstInvoiceNo', label: 'Cell GST Invoice No.' },
    { key: 'moduleMake', label: 'PV Module Make', default: 'Adani' }
  ],
  NET_METER_AGREEMENT: [
    { key: 'vendorWitnessName', label: 'Witness (Vendor) Name (blank = use Settings > Vendor Profile)' },
    { key: 'agreementPlace', label: 'Place of Agreement' },
    { key: 'netMeterSerialNo', label: 'Net Meter Serial No.' }
  ]
};

@Component({
  selector: 'app-generate-document',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule, MatFormFieldModule, MatSelectModule,
    MatAutocompleteModule, MatInputModule, MatButtonModule, MatTableModule, MatProgressSpinnerModule,
    MatSnackBarModule, MatCardModule, MatIconModule, MatDividerModule, TranslatePipe
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
  private sanitizer = inject(DomSanitizer);

  /**
   * Whatever document was generated most recently (single template or the
   * compliance package) - shown as an inline PDF preview so the vendor can
   * verify it before sending it anywhere. Selecting a new one (from either
   * flow) replaces this - only ever one preview showing at a time, mirroring
   * the backend only ever keeping one current file per document.
   */
  previewDoc = signal<{ id: string; label: string } | null>(null);
  previewUrl = computed<SafeResourceUrl | null>(() => {
    const doc = this.previewDoc();
    if (!doc) return null;
    const url = `${environment.apiBaseUrl}/customers/${this.customerId()}/documents/generated/${doc.id}/view`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  });

  templates = signal<any[]>([]);
  selectedTemplate = signal<string>('');
  lineItems = signal<any[]>([{ slNo: 1, item: '', type: '', qty: '', unit: '', rate: 0, gstPercent: '', amount: 0 }]);
  generatedFile = signal<string | null>(null);
  isLoading = signal(false);
  isGeneratingPackage = signal(false);
  packageGenerated = signal<string | null>(null);
  packageError = signal<string | null>(null);
  isLoadingTemplates = signal(true);
  error = signal<string | null>(null);
  displayedColumns = ['slNo', 'item', 'type', 'qty', 'unit', 'rate', 'gstPercent', 'amount', 'actions'];

  catalog = signal<Item[]>([]);
  isLoadingCatalog = signal(true);
  private catalogAutoPopulated = false;

  /** Values for the current template's "Additional Details" fields (see EXTRA_FIELD_DEFS), keyed by field key. Rebuilt whenever the selected template changes. */
  extraFieldValues = signal<Record<string, string>>({});
  private lastExtraFieldsTemplate = '';

  /** The field definitions to render for the currently selected template - empty for QUOTATION/INVOICE, which use the line-item table instead. */
  currentExtraFieldDefs = computed<ExtraFieldDef[]>(() => EXTRA_FIELD_DEFS[this.selectedTemplate()] ?? []);

  /** Sum of every line's Amount (GST-inclusive), shown as the table's Total row. */
  totalAmount = computed(() => this.lineItems().reduce((sum, l) => sum + (parseFloat(l.amount) || 0), 0));

  /** True when a row is quantity-based (has a Unit, e.g. "W", "Pc", "KW") and should show the Qty input. Flat/lump-sum rows from Item Master (Transportation, Comprehensive Maintenance) have no unit, so Qty is meaningless for them - they're priced at Rate directly. */
  hasUnit(row: any): boolean {
    return !!(row.unit && String(row.unit).trim());
  }

  /** Two-way binding helper for a single extra field's value. */
  setExtraField(key: string, value: string) {
    this.extraFieldValues.update(v => ({ ...v, [key]: value }));
  }

  constructor() {
    // Whenever the selected template changes to one with its own
    // "Additional Details" fields, (re)build the value map from that
    // template's field defs - defaults pre-filled (mirroring the
    // backend's extraFields.getOrDefault fallback), dynamic fields blank.
    // Only resets when the template actually changes, so it never wipes
    // out values the vendor is mid-way through typing.
    effect(() => {
      const template = this.selectedTemplate();
      const defs = this.currentExtraFieldDefs();
      if (template !== this.lastExtraFieldsTemplate) {
        this.lastExtraFieldsTemplate = template;
        const values: Record<string, string> = {};
        for (const d of defs) {
          values[d.key] = d.default ?? '';
        }
        this.extraFieldValues.set(values);
      }
    });

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

  /** Replaces the line-item table with one row per Item Master entry (rate/type/unit prefilled, quantity left blank for the vendor to fill in - except flat-rate rows with no unit, which are priced immediately). */
  loadFromCatalog() {
    const items = this.catalog();
    if (items.length === 0) {
      this.snackBar.open('Item Master is empty. Add items in Master Data > Items first.', 'Close', { duration: 4000 });
      return;
    }
    this.lineItems.set(items.map((it, idx) => {
      const rate = it.defaultRate ?? 0;
      const gstPercent = parseFloat(it.defaultGstPercent ?? '') || 0;
      const flatRate = !(it.unit && it.unit.trim());
      const amount = flatRate ? rate + (rate * gstPercent / 100) : 0;
      return {
        slNo: idx + 1,
        productCode: it.id,
        item: it.itemName,
        type: it.type ?? '',
        qty: '',
        unit: it.unit ?? '',
        rate,
        gstPercent: it.defaultGstPercent ?? '',
        amount
      };
    }));
  }

  /** Applies an Item Master item's defaults onto one existing row (picked via the Item column's autocomplete), keeping any quantity the vendor already typed. */
  onItemSelected(index: number, event: MatAutocompleteSelectedEvent) {
    const itemName: string = event.option.value;
    const item = this.catalog().find(it => it.itemName === itemName);
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

  /** Options shown under the Item column's autocomplete as the vendor types - matches on item name, falls back to the full active list when empty. */
  filteredCatalog(query: string | null | undefined): Item[] {
    const items = this.catalog();
    const normalized = (query ?? '').trim().toLowerCase();
    if (!normalized) {
      return items;
    }
    return items.filter(it => it.itemName.toLowerCase().includes(normalized));
  }

  /** Typing in the Item column freehand (not picking a suggestion) clears any previously-applied catalog link so it's treated as a custom line again. */
  onItemTyped(index: number) {
    this.lineItems.update(items => {
      const updated = [...items];
      const current = updated[index];
      const stillMatches = this.catalog().some(it => it.id === current.productCode && it.itemName === current.item);
      if (!stillMatches) {
        updated[index] = { ...current, productCode: '' };
      }
      return updated;
    });
  }

  // The 6 compliance documents are only generated via the 1-click
  // "Generate Full Compliance Package" button now - they're intentionally
  // excluded here so the manual per-template form only ever offers the
  // documents that still need a one-off form (quotation/invoice line
  // items, agreement terms).
  private readonly PACKAGE_TEMPLATE_CODES = new Set([
    'WORK_COMPLETION_REPORT', 'GUARANTEE_CERTIFICATE', 'ANNEXURE_I',
    'COMMISSIONING_REPORT', 'DCR_DECLARATION', 'NET_METER_AGREEMENT'
  ]);

  loadTemplates() {
    console.log('Loading templates from API...');
    this.isLoadingTemplates.set(true);
    this.error.set(null);

    this.docService.listTemplates().subscribe({
      next: (allTemplates) => {
        console.log('Templates loaded successfully:', allTemplates);
        const templates = Array.isArray(allTemplates)
          ? allTemplates.filter(t => !this.PACKAGE_TEMPLATE_CODES.has(t.code))
          : allTemplates;

        if (templates && Array.isArray(templates) && templates.length > 0) {
          this.templates.set(templates);
          
          // Set first template as default
          const defaultTemplate = templates.find(t => t.code === 'QUOTATION') || templates[0];
          this.selectedTemplate.set(defaultTemplate.code);
          console.log('Selected default template:', defaultTemplate.code);
          
          this.isLoadingTemplates.set(false);
        } else {
          // Nothing left to show manually is fine - it just means every
          // template is part of the compliance package above. Not an error.
          this.templates.set([]);
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
      const rate = parseFloat(item.rate) || 0;
      const gstPercent = parseFloat(item.gstPercent) || 0;
      // Flat/lump-sum rows (no Unit - e.g. Transportation, Comprehensive
      // Maintenance) are priced at Rate directly, as if qty=1; the Qty
      // input is hidden for these in the template (see hasUnit()).
      const qty = this.hasUnit(item) ? (parseFloat(item.qty) || 0) : 1;
      const baseAmount = qty * rate;
      item.amount = baseAmount + (baseAmount * gstPercent / 100);
      console.log(`Updated amount for line ${index}: ${item.amount} (qty=${qty}, rate=${rate}, gst=${gstPercent}%)`);
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

    // QUOTATION/INVOICE send their line-item table; every other template
    // sends the "Additional Details" values collected for it above -
    // whatever the vendor left blank falls back to the backend's own
    // default (see each *GenerationStrategy.java's extraFields.getOrDefault).
    const payload = (templateCode === 'QUOTATION' || templateCode === 'INVOICE')
      ? { lineItems: this.lineItems() }
      : { ...this.extraFieldValues() };

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
        this.previewDoc.set({ id: doc.id, label: templateCode });

        const successMsg = 'Document generated successfully!';
        this.snackBar.open(successMsg, 'Close', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
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
   * One-click generation of the full compliance document set, merged into
   * a single PDF - no form, no re-typing anything already saved on this
   * customer (Customer Details + Plant Details) or in Settings > Vendor
   * Profile. Fields left blank on Plant Details simply render blank in
   * the PDF - fill in Plant Details first for a complete package.
   */
  generatePackage() {
    this.packageError.set(null);
    this.isGeneratingPackage.set(true);
    this.docService.generatePackage(this.customerId()).subscribe({
      next: (doc) => {
        this.isGeneratingPackage.set(false);
        this.packageGenerated.set(doc.filePath || doc.toString());
        this.previewDoc.set({ id: doc.id, label: 'Compliance Package' });
        this.snackBar.open('Compliance package generated successfully!', 'Close', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
      },
      error: (err) => {
        this.isGeneratingPackage.set(false);
        const msg = err?.error?.message || err?.message || 'Failed to generate compliance package';
        this.packageError.set(msg);
        this.snackBar.open(msg, 'Close', { duration: 7000, panelClass: ['error-snackbar'] });
      }
    });
  }

  closePreview() {
    this.previewDoc.set(null);
  }

  /**
   * Trigger a refresh of templates if needed
   */
  retryLoadTemplates() {
    console.log('Retrying template load...');
    this.loadTemplates();
  }
}
