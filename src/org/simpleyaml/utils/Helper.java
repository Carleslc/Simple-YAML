/*
 * MIT License
 *
 * Copyright (c) 2020 Hasan Demirtaş
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.simpleyaml.utils;

import com.amihaiemil.eoyaml.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.serialization.ConfigurationSerializable;
import org.simpleyaml.configuration.serialization.ConfigurationSerialization;

public final class Helper {

    private static final List<Class<?>> SCALAR_TYPES = Arrays.asList(
        Integer.class, Long.class, Float.class, Double.class, Short.class, String.class, Boolean.class, Character.class,
        Byte.class);

    private Helper() {
    }

    public static void loadFromString(final ConfigurationSection section, final String contents) throws IOException {
        Optional.ofNullable(Yaml.createYamlInput(contents).readYamlMapping()).ifPresent(t ->
            Helper.convertMapToSection(t, section));
    }

    public static YamlMapping mapAsYamlMapping(final Map<String, Object> map) {
        final AtomicReference<YamlMappingBuilder> builder = new AtomicReference<>(Yaml.createYamlMappingBuilder());
        Helper.buildMap(builder, Helper.withoutMemorySection(map));
        return builder.get().build();
    }

    private static void convertMapToSection(final YamlMapping mapping, final ConfigurationSection section) {
        Helper.convertMapToSection(Helper.yamlMappingAsMap(mapping), section);
    }

    private static Map<String, Object> yamlMappingAsMap(final YamlMapping mapping) {
        final Map<String, Object> map = new HashMap<>();
        mapping.keys().stream()
            .filter(node -> node instanceof Scalar)
            .map(YamlNode::asScalar)
            .forEach(keyNode ->
                Helper.yamlNodeAsObject(mapping.value(keyNode)).ifPresent(o ->
                    map.put(keyNode.value().replace("\"", ""), o)));
        return map;
    }

    private static Map<String, Object> withoutMemorySection(final Map<String, Object> values) {
        final Map<String, Object> map = new HashMap<>();
        values.forEach((s, o) -> {
            if (o instanceof ConfigurationSection) {
                map.put(s, Helper.withoutMemorySection(((ConfigurationSection) o).getValues(false)));
            } else if (o instanceof ConfigurationSerializable) {
                final ConfigurationSerializable serializable = (ConfigurationSerializable) o;
                final Map<String, Object> inner = new HashMap<>();
                inner.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
                    ConfigurationSerialization.getAlias(serializable.getClass()));
                inner.putAll(serializable.serialize());
                map.put(s, inner);
            } else {
                map.put(s, o);
            }
        });
        return map;
    }

    private static void buildMap(final AtomicReference<YamlMappingBuilder> builder,
                                 final Map<String, Object> map) {
        map.forEach((s, o) ->
            Helper.objectAsYamlNode(o).ifPresent(node ->
                builder.set(builder.get().add(s, node))));
    }

    private static List<Object> sequenceAsList(final YamlSequence sequence) {
        return sequence.values().stream()
            .map(Helper::yamlNodeAsObject)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    private static List<Object> streamAsList(final YamlStream stream) {
        return stream
            .map(Helper::yamlNodeAsObject)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    private static void buildSequence(final AtomicReference<YamlSequenceBuilder> builder,
                                      final Collection<?> objects) {
        objects.stream()
            .map(Helper::objectAsYamlNode)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(node -> builder.set(builder.get().add(node)));
    }

    private static Optional<YamlNode> objectAsYamlNode(final Object o) {
        if (o instanceof Collection<?>) {
            final AtomicReference<YamlSequenceBuilder> sequenceBuilder = new AtomicReference<>(Yaml.createYamlSequenceBuilder());
            Helper.buildSequence(sequenceBuilder, (Collection<?>) o);
            return Optional.ofNullable(sequenceBuilder.get().build());
        }
        if (o instanceof Map<?, ?>) {
            final Map<String, Object> objectmap = (Map<String, Object>) o;
            final AtomicReference<YamlMappingBuilder> mappingBuilder = new AtomicReference<>(Yaml.createYamlMappingBuilder());
            Helper.buildMap(mappingBuilder, objectmap);
            return Optional.ofNullable(mappingBuilder.get().build());
        }
        if (Helper.SCALAR_TYPES.contains(o.getClass())) {
            final String value = String.valueOf(o);
            final AtomicReference<YamlScalarBuilder> atomic = new AtomicReference<>(Yaml.createYamlScalarBuilder());
            if (value.contains("\n")) {
                Arrays.stream(value.split("\n")).forEach(s ->
                    atomic.set(atomic.get().addLine(s)));
                return Optional.of(atomic.get().buildFoldedBlockScalar());
            }
            return Optional.ofNullable(atomic.get().addLine(value).buildPlainScalar());
        }
        return Optional.empty();
    }

    private static Optional<Object> yamlNodeAsObject(final YamlNode value) {
        final Object object;
        if (value instanceof Scalar) {
            object = Helper.getAsAll((Scalar) value);
        } else if (value instanceof YamlSequence) {
            object = Helper.sequenceAsList(value.asSequence());
        } else if (value instanceof YamlStream) {
            object = Helper.streamAsList(value.asStream());
        } else if (value instanceof YamlMapping) {
            object = Helper.yamlMappingAsMap(value.asMapping());
        } else {
            object = null;
        }
        return Optional.ofNullable(object);
    }

    private static Object getAsAll(final Scalar scalar) {
        final Optional<Integer> optional1 = Helper.getAsInteger(scalar);
        if (optional1.isPresent()) {
            return optional1.get();
        }
        final Optional<Long> optional2 = Helper.getAsLong(scalar);
        if (optional2.isPresent()) {
            return optional2.get();
        }
        final Optional<Double> optional4 = Helper.getAsDouble(scalar);
        if (optional4.isPresent()) {
            return optional4.get();
        }
        final Optional<Float> optional3 = Helper.getAsFloat(scalar);
        if (optional3.isPresent()) {
            return optional3.get();
        }
        final Optional<Short> optional5 = Helper.getAsShort(scalar);
        if (optional5.isPresent()) {
            return optional5.get();
        }
        final Optional<Boolean> optional6 = Helper.getAsBoolean(scalar);
        if (optional6.isPresent()) {
            return optional6.get();
        }
        final Optional<Character> optional7 = Helper.getAsCharacter(scalar);
        if (optional7.isPresent()) {
            return optional7.get();
        }
        final Optional<Byte> optional8 = Helper.getAsByte(scalar);
        if (optional8.isPresent()) {
            return optional8.get();
        }
        return Helper.getAsString(scalar).orElse(null);
    }

    private static Optional<Integer> getAsInteger(final Scalar scalar) {
        return Optional.ofNullable(scalar.value())
            .flatMap(Helper::toInt);
    }

    private static Optional<Long> getAsLong(final Scalar scalar) {
        return Optional.ofNullable(scalar.value())
            .flatMap(Helper::toLong);
    }

    private static Optional<Float> getAsFloat(final Scalar scalar) {
        return Optional.ofNullable(scalar.value())
            .flatMap(Helper::toFloat);
    }

    private static Optional<Double> getAsDouble(final Scalar scalar) {
        return Optional.ofNullable(scalar.value())
            .flatMap(Helper::toDouble);
    }

    private static Optional<Short> getAsShort(final Scalar scalar) {
        return Optional.ofNullable(scalar.value())
            .flatMap(Helper::toShort);
    }

    private static Optional<String> getAsString(final Scalar scalar) {
        return Optional.ofNullable(scalar.value());
    }

    private static Optional<Boolean> getAsBoolean(final Scalar scalar) {
        return Optional.ofNullable(scalar.value())
            .flatMap(Helper::toBoolean);
    }

    private static Optional<Character> getAsCharacter(final Scalar scalar) {
        return Optional.ofNullable(scalar.value())
            .flatMap(Helper::toCharacter);
    }

    private static Optional<Byte> getAsByte(final Scalar scalar) {
        return Optional.ofNullable(scalar.value())
            .flatMap(Helper::toByte);
    }

    private static void convertMapToSection(final Map<?, ?> input, final ConfigurationSection section) {
        final Map<String, Object> result = Helper.deserialize(input);
        notfound:
        if (result.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
            final Map<String, Object> typed = result.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            final ConfigurationSection parent = section.getParent();
            if (parent == null) {
                break notfound;
            }
            try {
                parent.set(section.getName(), ConfigurationSerialization.deserializeObject(typed));
            } catch (final IllegalArgumentException ex) {
                throw new RuntimeException("Could not deserialize object", ex);
            }
            return;
        }
        for (final Map.Entry<?, ?> entry : result.entrySet()) {
            final String key = entry.getKey().toString();
            final Object value = entry.getValue();
            if (value instanceof Map<?, ?>) {
                Helper.convertMapToSection((Map<?, ?>) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

    private static Map<String, Object> deserialize(final Map<?, ?> input) {
        return input.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> Objects.toString(entry.getKey()),
                entry -> {
                    final Object value = entry.getValue();
                    if (value instanceof Map<?, ?>) {
                        return Helper.deserialize((Map<?, ?>) value);
                    }
                    if (value instanceof Iterable<?>) {
                        return Helper.deserialize((Iterable<?>) value);
                    }
                    if (value instanceof Stream<?>) {
                        return Helper.deserialize(((Stream<?>) value).collect(Collectors.toList()));
                    }
                    return value;
                }));
    }

    private static Collection<Object> deserialize(final Iterable<?> input) {
        final Collection<Object> objects = new ArrayList<>();
        input.forEach(o -> {
            if (o instanceof Map) {
                objects.add(Helper.deserialize((Map<?, ?>) o));
            } else if (o instanceof List<?>) {
                objects.add(Helper.deserialize((Iterable<?>) o));
            } else {
                objects.add(o);
            }
        });
        return objects;
    }

    private static Optional<Integer> toInt(final Object object) {
        if (object == null) {
            return Optional.empty();
        }
        if (object instanceof Number) {
            return Optional.of(((Number) object).intValue());
        }
        try {
            return Optional.of(Integer.parseInt(object.toString()));
        } catch (final NumberFormatException ignored) {
        }
        return Optional.empty();
    }

    private static Optional<Double> toDouble(final Object object) {
        if (object == null) {
            return Optional.empty();
        }
        if (object instanceof Number) {
            return Optional.of(((Number) object).doubleValue());
        }
        try {
            return Optional.of(Double.parseDouble(object.toString()));
        } catch (final NumberFormatException ignored) {
        }
        return Optional.empty();
    }

    private static Optional<Float> toFloat(final Object object) {
        if (object == null) {
            return Optional.empty();
        }
        if (object instanceof Number) {
            return Optional.of(((Number) object).floatValue());
        }
        try {
            return Optional.of(Float.parseFloat(object.toString()));
        } catch (final NumberFormatException ignored) {
        }
        return Optional.empty();
    }

    private static Optional<Long> toLong(final Object object) {
        if (object == null) {
            return Optional.empty();
        }
        if (object instanceof Number) {
            return Optional.of(((Number) object).longValue());
        }
        try {
            return Optional.of(Long.parseLong(object.toString()));
        } catch (final NumberFormatException ignored) {
        }
        return Optional.empty();
    }

    private static Optional<Byte> toByte(final Object object) {
        if (object == null) {
            return Optional.empty();
        }
        if (object instanceof Number) {
            return Optional.of(((Number) object).byteValue());
        }
        try {
            return Optional.of(Byte.parseByte(object.toString()));
        } catch (final NumberFormatException ignored) {
        }
        return Optional.empty();
    }

    private static Optional<Short> toShort(final Object object) {
        if (object == null) {
            return Optional.empty();
        }
        if (object instanceof Number) {
            return Optional.of(((Number) object).shortValue());
        }
        try {
            return Optional.of(Short.parseShort(object.toString()));
        } catch (final NumberFormatException ignored) {
        }
        return Optional.empty();
    }

    private static Optional<Boolean> toBoolean(final Object object) {
        if (object == null) {
            return Optional.empty();
        }
        if (object instanceof Boolean) {
            return Optional.of((Boolean) object);
        }
        if ("true".equalsIgnoreCase(object.toString())) {
            return Optional.of(true);
        }
        if ("false".equalsIgnoreCase(object.toString())) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    private static Optional<Character> toCharacter(final Object object) {
        if (object instanceof Character) {
            return Optional.of((Character) object);
        }
        return Optional.empty();
    }

}
