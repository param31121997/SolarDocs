package com.solardocs.infrastructure.pdf.strategy;

import java.util.Map;
import java.util.function.Function;

/**
 * Resolves one template field with a consistent priority order used by
 * every *GenerationStrategy:
 * <ol>
 *   <li>An explicit value passed in extraFields for this one generation
 *       (lets a vendor override a single document without touching the
 *       saved Customer record)</li>
 *   <li>The value saved on the Customer's PlantInstallationDetails -
 *       filled in once on Customer Details and reused by every document,
 *       so the one-click "Generate Compliance Package" flow (which sends
 *       no extraFields at all) still gets real values</li>
 *   <li>A hardcoded fallback default</li>
 * </ol>
 */
public final class FieldResolver {

    private FieldResolver() {}

    public static <T> String resolve(Map<String, Object> extraFields, String key, T details,
                                      Function<T, String> accessor, String fallback) {
        Object override = extraFields.get(key);
        if (override != null && !String.valueOf(override).isBlank()) {
            return String.valueOf(override);
        }
        String stored = details == null ? null : accessor.apply(details);
        if (stored != null && !stored.isBlank()) {
            return stored;
        }
        return fallback == null ? "" : fallback;
    }
}
