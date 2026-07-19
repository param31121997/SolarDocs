package com.solardocs.infrastructure.pdf;

import com.solardocs.infrastructure.pdf.strategy.DocumentGenerationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class DocumentGenerationStrategyFactory {

    private final Map<String, DocumentGenerationStrategy> strategies;

    // Spring auto-injects every DocumentGenerationStrategy bean —
    // adding a new document type later is just "add a new @Component", zero changes here.
    public DocumentGenerationStrategyFactory(List<DocumentGenerationStrategy> strategyBeans) {
        this.strategies = strategyBeans.stream()
                .collect(java.util.stream.Collectors.toMap(DocumentGenerationStrategy::templateCode, s -> s));
    }

    public DocumentGenerationStrategy resolve(String templateCode) {
        DocumentGenerationStrategy s = strategies.get(templateCode);
        if (s == null) throw new NoSuchElementException("No generation strategy registered for " + templateCode);
        return s;
    }
}
