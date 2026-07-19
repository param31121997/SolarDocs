import { Routes } from '@angular/router';
import { CustomerListComponent } from './features/customers/list/customer-list.component';
import { CustomerFormComponent } from './features/customers/form/customer-form.component';
import { CustomerDetailComponent } from './features/customers/detail/customer-detail.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { SettingsComponent } from './features/settings/settings.component';
import { BackupComponent } from './features/backup/backup.component';
import { LicenseActivationComponent } from './features/license/license-activation.component';
import { ProductCategoryListComponent } from './features/master-data/product-categories/product-category-list.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'customers', component: CustomerListComponent },
  { path: 'customers/new', component: CustomerFormComponent },
  { path: 'customers/:id/edit', component: CustomerFormComponent },
  { path: 'customers/:id', component: CustomerDetailComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'master-data/product-categories', component: ProductCategoryListComponent },
  { path: 'backup', component: BackupComponent },
  { path: 'license', component: LicenseActivationComponent },
];
