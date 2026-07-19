package com.solardocs.api.document.mapper;

import com.solardocs.api.document.dto.UploadedDocumentResponseDto;
import com.solardocs.domain.document.UploadedDocument;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DocumentMapper {

    UploadedDocumentResponseDto toResponse(UploadedDocument document);
}
