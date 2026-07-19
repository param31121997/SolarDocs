package com.solardocs.api.masterdata.mapper;

import com.solardocs.api.masterdata.dto.ProductCategoryResponseDto;
import com.solardocs.domain.masterdata.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductCategoryMapper {

    ProductCategoryResponseDto toResponse(ProductCategory category);
}
