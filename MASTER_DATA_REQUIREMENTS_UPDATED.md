# Updated Master Data Requirements

Replace Product Categories with Items.

Master Data:
- Items
- Specifications
- Units
- GST

Items:
- itemId
- itemCode
- itemName
- description
- active

Specifications:
- specificationId
- itemId
- specificationName
- unit
- gst
- defaultRate
- active

Quotation flow:
Item -> Specification -> auto fill Unit, GST, Default Rate.
No Category module.
