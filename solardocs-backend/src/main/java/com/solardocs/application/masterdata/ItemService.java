package com.solardocs.application.masterdata;

import com.solardocs.application.ports.ItemRepository;
import com.solardocs.domain.common.exception.DuplicateItemNameException;
import com.solardocs.domain.masterdata.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public Item create(String itemName, String description) {
        String normalizedName = requireName(itemName);
        assertNameIsUnique(normalizedName, null);

        String id = UUID.randomUUID().toString();
        Item item = Item.create(id, normalizedName, description);
        repository.save(item);

        log.info("Item created: {}", item.itemName());
        return item;
    }

    public Item get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item " + id + " not found"));
    }

    /** Items a vendor picks from while building a quotation - active only, alphabetical. */
    public List<Item> list(boolean includeInactive) {
        return repository.findAll().stream()
                .filter(i -> includeInactive || i.active())
                .sorted(Comparator.comparing(Item::itemName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Item> search(String query, boolean includeInactive) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        if (normalizedQuery.isEmpty()) {
            return list(includeInactive);
        }
        return list(includeInactive).stream()
                .filter(i -> i.itemName().toLowerCase().contains(normalizedQuery)
                        || (i.description() != null && i.description().toLowerCase().contains(normalizedQuery)))
                .toList();
    }

    public Item update(String id, String itemName, String description) {
        Item existing = get(id);
        String normalizedName = requireName(itemName);
        assertNameIsUnique(normalizedName, id);

        Item updated = existing.withDetails(normalizedName, description);
        repository.save(updated);

        log.info("Item updated: {}", updated.itemName());
        return updated;
    }

    public Item setActive(String id, boolean active) {
        Item existing = get(id);
        Item updated = existing.withActive(active);
        repository.save(updated);

        log.info("Item {} set active={}", updated.itemName(), active);
        return updated;
    }

    /**
     * Soft delete - the record is kept with active=false rather than removed,
     * so an item already referenced by an existing quotation never becomes
     * a dangling reference. Only active items are offered when building new
     * quotations (see {@link #list(boolean)}).
     */
    public void delete(String id) {
        Item deleted = setActive(id, false);
        log.info("Item soft-deleted: {}", deleted.itemName());
    }

    private String requireName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("Item name must not be empty");
        }
        return itemName.trim();
    }

    private void assertNameIsUnique(String itemName, String excludingId) {
        repository.findByItemNameIgnoreCase(itemName)
                .filter(existing -> !existing.id().equals(excludingId))
                .ifPresent(existing -> { throw new DuplicateItemNameException(itemName); });
    }
}
