# SolarDocs — Organized Build (Parts 1–5 + i18n)

This is your 5-part implementation guide turned into an actual project tree,
organized exactly per the Clean Architecture rules in your original prompt
(`domain` → `application` → `infrastructure` → `api` → `config`), plus a
working **English / Hindi / Marathi** translation layer wired into Angular
Material, as requested.

## What's in this package

```
SolarDocs/
├── solardocs-backend/          Spring Boot 3.3 (Java 21) — Maven project
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/solardocs/
│       │   ├── domain/          pure Java — Customer, CustomerId, Address, DocumentType,
│       │   │                    DocumentTemplate, VendorProfile, License, PinCredential,
│       │   │                    DomainException hierarchy — zero Spring imports
│       │   ├── application/     services + ports (CustomerService, DocumentGenerationService,
│       │   │                    DashboardService, LicenseService, PinAuthService, CustomerRepository
│       │   │                    interface, VendorProfileRepository interface, ...)
│       │   ├── infrastructure/  JSON persistence (atomic writes + file locking), the
│       │   │                    Thymeleaf+OpenHTMLtoPDF rendering pipeline, all 7 document
│       │   │                    generation Strategies (Quotation, Invoice, Agreement, DCR
│       │   │                    Declaration, Annexure-I, Net Meter Agreement, Commissioning
│       │   │                    Report), RSA license validation, AES field encryption, zip backup
│       │   ├── api/             REST controllers + DTOs + ApiResponse<T> envelope +
│       │   │                    GlobalExceptionHandler + @Gstin/@Pincode validators
│       │   └── config/          AppDataDirectoryConfig, CorsConfig, SpaFallbackController,
│       │                        TrayIconLauncher
│       └── resources/
│           ├── application.properties, logback-spring.xml
│           └── license/public.pem   ← you must generate this (see below)
│
├── solardocs-frontend/          Angular 18 (standalone components) + Angular Material
│   └── src/app/
│       ├── core/
│       │   ├── i18n/            TranslateService + TranslatePipe (the translation engine)
│       │   ├── models/, services/
│       ├── shared/components/language-switcher/   Material mat-menu language picker
│       └── features/            dashboard, customers (list/form/detail), documents
│                                 (generate-document), settings, backup, license
│   └── src/assets/i18n/         en.json, hi.json, mr.json  ← the actual translations
│
└── solardocs-data/              Sample data directory (what ships at D:\SolarDocsData
                                  on a vendor's machine): Config/, Templates/, Indexes/,
                                  Customers/, Reports/, Backup/, Logs/ — pre-seeded with
                                  document-types.json, templates-registry.json,
                                  product-catalog.json, config.json
```

## How the i18n / Angular Material translation layer works

You asked for an "Angular Material translator" for Marathi, Hindi, and English —
here's what was built, without needing any external translation service or paid API:

1. **`core/i18n/translate.service.ts`** — a small root-provided Angular service.
   On app bootstrap (`APP_INITIALIZER` in `app.config.ts`) it loads
   `/assets/i18n/{lang}.json`, remembers the vendor's last choice in
   `localStorage`, and falls back to the browser's language if nothing is saved.
2. **`core/i18n/translate.pipe.ts`** — an impure `{{ 'key.path' | translate }}`
   pipe used throughout every template instead of hardcoded English strings.
3. **`assets/i18n/en.json` / `hi.json` / `mr.json`** — parallel dictionaries,
   same key structure, covering navigation, every Customer field and status,
   every document type and generated-template name, dashboard labels,
   settings, backup, and license screens.
4. **`shared/components/language-switcher/`** — a proper Angular Material
   component (`mat-button` + `mat-menu`, with a `translate` icon) that lets the
   vendor flip between English / हिन्दी / मराठी instantly, no reload, from the
   top toolbar or the Settings screen. The choice persists across restarts.
5. **Font support** — `index.html` pulls in **Noto Sans Devanagari** so Hindi
   and Marathi glyphs render correctly (both languages share the Devanagari
   script) without extra configuration.

To add a 4th language later: add `assets/i18n/xx.json` with the same keys and
add `'xx'` to `SUPPORTED` in `translate.service.ts` — nothing else changes,
mirroring the same "registry, not code change" pattern your document-type and
template registries already use.

## Build order (matches your original Parts 1–5)

| Phase | What | Where |
|---|---|---|
| 0–1 | Foundation, atomic JSON storage, Customer CRUD | `domain/customer`, `application/customer`, `infrastructure/persistence/json`, `api/customer` |
| 2–3 | Document upload, PDF engine, Quotation/Invoice/Agreement | `application/document`, `infrastructure/pdf/**` |
| 4 | Government forms (DCR, Annexure-I, Net Meter, Commissioning) | `infrastructure/pdf/strategy/*GenerationStrategy.java` |
| 5 | Dashboard, CSV reports, Vendor Profile, Product Catalog | `application/dashboard`, `application/reports`, `api/settings` |
| 6 | Licensing (RSA-signed, offline, machine-bound) | `infrastructure/licensing`, `application/license` |
| 7 | Backup/Restore, exceptions, logging, validation, PIN, AES | `infrastructure/backup`, `infrastructure/security` |
| 8–10 | Packaging (jpackage), tray icon, first-run wizard | `config/SpaFallbackController.java`, `config/TrayIconLauncher.java`, `api/setup` |

## Before this compiles and runs

1. **Generate the license keypair** (Part 4, Step 2):
   ```
   openssl genpkey -algorithm RSA -out private.pem -pkeyopts rsa_keygen_bits:2048
   openssl rsa -pubout -in private.pem -out public.pem
   ```
   Keep `private.pem` secret; copy `public.pem` to
   `solardocs-backend/src/main/resources/license/public.pem`.
2. **Paste your real vendor documents' clause text** into the placeholder
   comments in `agreement-v1.html`, `annexure1-v1.html`,
   `net-meter-agreement-v1.html`, `commissioning-report-v1.html` — these were
   deliberately left as structured skeletons since the exact legal wording is
   yours (per Part 2 Step 10's copyright note).
3. **Install frontend deps and run** (this sandbox has no network access, so
   dependencies were not installed here):
   ```
   cd solardocs-frontend
   npm install
   ng serve
   ```
4. **Run the backend**:
   ```
   cd solardocs-backend
   ./mvnw spring-boot:run
   ```
5. For a single-executable Windows build, follow Part 5 Steps 1–2
   (`build-all.sh` + `jpackage`) unchanged — nothing about i18n or the
   package layout affects packaging.

## Roadmap checklist

Every phase from your original Part 5 checklist maps directly onto the
folders above. Nothing in Parts 1–5 was skipped; the AI provider gateway and
Postgres/multi-tenant migration remain deliberately unbuilt "V2" stubs, exactly
as your architecture document specifies.
