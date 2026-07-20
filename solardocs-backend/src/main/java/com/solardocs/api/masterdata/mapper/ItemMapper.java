package com.solardocs.api.masterdata.mapper;

import com.solardocs.api.masterdata.dto.ItemResponseDto;
import com.solardocs.domain.masterdata.Item;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ItemMapper {

    ItemResponseDto toResponse(Item item);
}
