package com.solardocs.infrastructure.pdf;

import java.util.Map;

public interface PdfRenderer {
    byte[] render(String templateName, Map<String, Object> model);
}
