package com.solardocs.application.masterdata;

import com.solardocs.application.ports.ProductCategoryRepository;
import com.solardocs.domain.common.exception.DuplicateCategoryNameException;
import com.solardocs.domain.masterdata.ProductCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ProductCategoryService {

    private static final Logger log = LoggerFactory.getLogger(ProductCategoryService.class);

    private final ProductCategoryRepository repository;

    public ProductCategoryService(ProductCategoryRepository repository) {
        this.repository = repository;
    }

    public ProductCategory create(String categoryName, String description) {
        String normalizedName = requireName(categoryName);
        assertNameIsUnique(normalizedName, null);

        String id = UUID.randomUUID().toString();
        String categoryCode = repository.nextCategoryCode();
        ProductCategory category = ProductCategory.create(id, categoryCode, normalizedName, description);
        repository.save(category);

        log.info("Product category created: {} ({})", category.categoryName(), category.categoryCode());
        return category;
    }

    public ProductCategory get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product category " + id + " not found"));
    }

    /** Categories a vendor picks from day-to-day - active only, alphabetical. */
    public List<ProductCategory> list(boolean includeInactive) {
        return repository.findAll().stream()
                .filter(c -> includeInactive || c.active())
                .sorted(Comparator.comparing(ProductCategory::categoryName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<ProductCategory> search(String query, boolean includeInactive) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        if (normalizedQuery.isEmpty()) {
            return list(includeInactive);
        }
        return list(includeInactive).stream()
                .filter(c -> c.categoryName().toLowerCase().contains(normalizedQuery)
                        || c.categoryCode().toLowerCase().contains(normalizedQuery)
                        || (c.description() != null && c.description().toLowerCase().contains(normalizedQuery)))
                .toList();
    }

    public ProductCategory update(String id, String categoryName, String description) {
        ProductCategory existing = get(id);
        String normalizedName = requireName(categoryName);
        assertNameIsUnique(normalizedName, id);

        ProductCategory updated = existing.withDetails(normalizedName, description);
        repository.save(updated);

        log.info("Product category updated: {}", updated.categoryCode());
        return updated;
    }

    public ProductCategory setActive(String id, boolean active) {
        ProductCategory existing = get(id);
        ProductCategory updated = existing.withActive(active);
        repository.save(updated);

        log.info("Product category {} set active={}", updated.categoryCode(), active);
        return updated;
    }

    /**
     * Soft delete - the record is kept with active=false rather than removed,
     * so a category already referenced by a product (once the Products
     * module exists) never becomes a dangling reference.
     */
    public void delete(String id) {
        ProductCategory deleted = setActive(id, false);
        log.info("Product category soft-deleted: {} ({})", deleted.categoryName(), deleted.categoryCode());
    }

    private String requireName(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("Category name must not be empty");
        }
        return categoryName.trim();
    }

    private void assertNameIsUnique(String categoryName, String excludingId) {
        repository.findByCategoryNameIgnoreCase(categoryName)
                .filter(existing -> !existing.id().equals(excludingId))
                .ifPresent(existing -> { throw new DuplicateCategoryNameException(categoryName); });
    }
}
