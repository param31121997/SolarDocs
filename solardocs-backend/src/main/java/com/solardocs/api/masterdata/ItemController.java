package com.solardocs.api.masterdata;

import com.solardocs.api.common.ApiResponse;
import com.solardocs.api.masterdata.dto.*;
import com.solardocs.api.masterdata.mapper.ItemMapper;
import com.solardocs.application.masterdata.ItemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Master Data - Items. Stores the catalog of reusable quotation line
 * items (Solar PV Module, Inverter, Mounting Structure, Cables, DCDB,
 * Earthing, Installation & Commissioning, Transportation, Maintenance...)
 * picked from when building a quotation. Kept under /api/master/items
 * per the module brief, alongside the existing /api/master-data/* space
 * used by Product Categories.
 */
@RestController
@RequestMapping("/api/master/items")
public class ItemController {

    private final ItemService service;
    private final ItemMapper mapper;

    public ItemController(ItemService service, ItemMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ApiResponse<ItemResponseDto> create(@Valid @RequestBody CreateItemRequestDto req) {
        return ApiResponse.ok(mapper.toResponse(service.create(req.itemName(), req.description())));
    }

    @GetMapping
    public ApiResponse<List<ItemResponseDto>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<ItemResponseDto> items = service.search(q, includeInactive).stream()
                .map(mapper::toResponse)
                .toList();
        return ApiResponse.ok(items);
    }

    @GetMapping("/{id}")
    public ApiResponse<ItemResponseDto> get(@PathVariable String id) {
        return ApiResponse.ok(mapper.toResponse(service.get(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<ItemResponseDto> update(@PathVariable String id,
                                                @Valid @RequestBody UpdateItemRequestDto req) {
        return ApiResponse.ok(mapper.toResponse(service.update(id, req.itemName(), req.description())));
    }

    @PatchMapping("/{id}/active")
    public ApiResponse<ItemResponseDto> setActive(@PathVariable String id,
                                                    @RequestBody UpdateItemActiveStatusRequestDto req) {
        return ApiResponse.ok(mapper.toResponse(service.setActive(id, req.active())));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.ok(null);
    }
}
