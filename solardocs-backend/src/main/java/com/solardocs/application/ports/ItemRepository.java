package com.solardocs.application.ports;

import com.solardocs.domain.masterdata.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    List<Item> findAll();

    Optional<Item> findById(String id);

    Optional<Item> findByItemNameIgnoreCase(String itemName);

    /** Upserts by id - insert on create, replace-in-place on update/activate/deactivate. */
    void save(Item item);
}
