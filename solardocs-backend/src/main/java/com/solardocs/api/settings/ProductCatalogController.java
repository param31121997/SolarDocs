package com.solardocs.api.settings;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.application.ports.ProductCatalogRepository;
import com.solardocs.domain.vendor.Product;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings/product-catalog")
public class ProductCatalogController {

    private final ProductCatalogRepository repository;

    public ProductCatalogController(ProductCatalogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ApiResponse<List<Product>> list() {
        return ApiResponse.ok(repository.findAll());
    }

    /** Replaces the whole catalog - the vendor edits the full list in Settings and saves it back. */
    @PutMapping
    public ApiResponse<List<Product>> save(@RequestBody List<Product> products) {
        repository.saveAll(products);
        return ApiResponse.ok(products);
    }
}
