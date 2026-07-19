package com.solardocs.api.customer.mapper;

import com.solardocs.api.customer.dto.CustomerSummaryResponseDto;
import com.solardocs.application.ports.CustomerIndexRepository;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerIndexMapper {

    CustomerSummaryResponseDto toResponse(CustomerIndexRepository.IndexEntry entry);
}
