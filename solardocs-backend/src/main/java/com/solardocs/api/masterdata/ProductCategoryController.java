package com.solardocs.api.masterdata;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.api.masterdata.dto.*;
import com.solardocs.api.masterdata.mapper.ProductCategoryMapper;
import com.solardocs.application.masterdata.ProductCategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Master Data - Product Categories. Kept under its own /api/master-data
 * prefix (rather than folded into /api/settings) so future master-data
 * modules - Products, Specifications - can sit alongside it without the
 * URL space implying they're vendor-preference settings.
 */
@RestController
@RequestMapping("/api/master-data/product-categories")
public class ProductCategoryController {

    private final ProductCategoryService service;
    private final ProductCategoryMapper mapper;

    public ProductCategoryController(ProductCategoryService service, ProductCategoryMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ApiResponse<ProductCategoryResponseDto> create(@Valid @RequestBody CreateProductCategoryRequestDto req) {
        return ApiResponse.ok(mapper.toResponse(service.create(req.categoryName(), req.description())));
    }

    @GetMapping
    public ApiResponse<List<ProductCategoryResponseDto>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<ProductCategoryResponseDto> categories = service.search(q, includeInactive).stream()
                .map(mapper::toResponse)
                .toList();
        return ApiResponse.ok(categories);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductCategoryResponseDto> get(@PathVariable String id) {
        return ApiResponse.ok(mapper.toResponse(service.get(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductCategoryResponseDto> update(@PathVariable String id,
                                                            @Valid @RequestBody UpdateProductCategoryRequestDto req) {
        return ApiResponse.ok(mapper.toResponse(service.update(id, req.categoryName(), req.description())));
    }

    @PatchMapping("/{id}/active")
    public ApiResponse<ProductCategoryResponseDto> setActive(@PathVariable String id,
                                                               @RequestBody UpdateCategoryActiveStatusRequestDto req) {
        return ApiResponse.ok(mapper.toResponse(service.setActive(id, req.active())));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.ok(null);
    }
}
