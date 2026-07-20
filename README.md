# SolarDocs - Item Master + Generate Document fixes - full delivery

Every file changed across this session. Paths mirror your project exactly -
copy each one to the same path. "NEW" = new file, "MODIFIED" = overwrite
existing file, "OVERWRITE-DATA" = a data/template file, not source code.

## 1. Item Master module (backend) - all NEW
- domain/masterdata/Item.java
- domain/common/exception/DuplicateItemNameException.java
- application/ports/ItemRepository.java
- infrastructure/persistence/json/JsonItemRepository.java
- application/masterdata/ItemService.java
- api/masterdata/dto/CreateItemRequestDto.java
- api/masterdata/dto/UpdateItemRequestDto.java
- api/masterdata/dto/UpdateItemActiveStatusRequestDto.java
- api/masterdata/dto/ItemResponseDto.java
- api/masterdata/mapper/ItemMapper.java
- api/masterdata/ItemController.java

API: GET/POST /api/master/items, GET/PUT /api/master/items/{id},
PATCH /api/master/items/{id}/active, DELETE /api/master/items/{id}.
Storage: MasterData/items.json.

## 2. Item Master module (frontend) - all NEW except routes/nav/i18n
- core/models/item.model.ts
- core/services/item.service.ts
- features/master-data/items/item-form-dialog.component.ts / .html / .scss
- features/master-data/items/item-list.component.ts / .html / .scss
- app.routes.ts (MODIFIED - added /master-data/items route)
- app.component.html (MODIFIED - added sidebar nav link)
- assets/i18n/en.json, hi.json, mr.json (MODIFIED - added `item.*` + `nav.items` keys)

## 3. Generate Document screen - now driven by Item Master, MODIFIED
- features/documents/generate-document.component.ts
- features/documents/generate-document.component.html
- features/documents/generate-document.component.scss

Changes bundled in here:
- Swapped the old ProductCatalogService for ItemService
- Merged the separate "Product" dropdown column into the "Item" column
  itself via autocomplete (type to search Item Master, or type freehand
  for a custom line)
- Fixed the line-item table's horizontal-scroll-at-100%-zoom bug
  (was a hardcoded 1120px min-width + fixed-px inputs; now fully
  percentage-based and fluid)
- Fixed Amount calculation: was doing qty x rate only; now correctly
  does qty x rate x (1 + gstPercent/100), matching your reference
  quotation math exactly
- Fixed the GST% input having no (change) handler at all - editing GST%
  now actually recalculates Amount

## 4. Document service - MODIFIED (bug fix)
- core/services/document.service.ts

Backend returns HTTP 200 even for business failures
(success:false + error:{code,message}). This was being silently
unwrapped as `data: null` and crashing the component on
`doc.filePath`. Now throws with the real backend error message so it
surfaces properly instead of crashing.

## 5. Quotation PDF generation - MODIFIED (bug fix)
- infrastructure/pdf/strategy/QuotationGenerationStrategy.java

Customer with no saved address -> null Address -> Thymeleaf template's
`${address.addressLine}` threw. Now defaults to a blank Address instead
of crashing. Also hardened Rate/Amount parsing so a blank field doesn't
throw NumberFormatException.

## 6. PDF templates - OVERWRITE-DATA (bug fix, all 7)
- solardocs-data/Templates/*.html (quotation, invoice, agreement,
  annexure1, commissioning-report, dcr-declaration, net-meter-agreement)

Every template had `<meta charset="UTF-8">` unclosed. openhtmltopdf
requires strict XHTML, so this crashed EVERY document type with a
SAXParseException. Fixed to `<meta charset="UTF-8"/>` in all 7.

IMPORTANT: these are in solardocs-backend/solardocs-data/Templates/ -
that's your actual runtime data dir (confirmed via Config/config.json).
If you also keep templates at repo-root solardocs-data/Templates/ or
in src/main/resources/default-templates/, apply the same one-character
fix there too for consistency (not included in this zip - just add the
trailing `/` before `>` on the meta tag).

## 7. Seed data - OVERWRITE-DATA
- solardocs-data/MasterData/items.json

9 items pre-filled to match your sample quotation exactly (name, type,
unit, rate, GST%) - drop straight into
solardocs-backend/solardocs-data/MasterData/items.json.

---
After copying everything: restart the Spring Boot backend (template/data
file changes need a fresh read; Angular dev server hot-reloads on its own).
