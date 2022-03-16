package org.simpleyaml.utils;

import org.simpleyaml.configuration.ConfigurationSection;

import java.util.Map;

public final class SectionUtils {

    public static void convertMapsToSections(final Map<?, ?> values, final ConfigurationSection section) {
        if (values == null) {
            return;
        }
        for (final Map.Entry<?, ?> entry : values.entrySet()) {
            Object keyObject = entry.getKey();
            String key;
            if (keyObject == null) {
                key = "";
            } else {
                key = keyObject.toString();
            }
            final Object value = entry.getValue();

            if (value instanceof Map) {
                convertMapsToSections((Map<?, ?>) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

}
