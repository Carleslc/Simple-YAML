package org.simpleyaml.configuration.implementation;

import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.implementation.api.QuoteValue;
import org.simpleyaml.configuration.serialization.ConfigurationSerializable;
import org.simpleyaml.configuration.serialization.ConfigurationSerialization;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Bukkit
 * @author Carleslc
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/file/YamlRepresenter.java">Bukkit Source</a>
 */
public class SnakeYamlRepresenter extends Representer {

    public SnakeYamlRepresenter() {
        this.multiRepresenters.put(ConfigurationSection.class, new RepresentConfigurationSection());
        this.multiRepresenters.put(ConfigurationSerializable.class, new RepresentConfigurationSerializable());
        this.multiRepresenters.put(QuoteValue.class, new RepresentQuoteValue());
    }

    private final class RepresentConfigurationSection extends RepresentMap {

        @Override
        public Node representData(final Object data) {
            return super.representData(((ConfigurationSection) data).getValues(false));
        }

    }

    private final class RepresentConfigurationSerializable extends RepresentMap {

        @Override
        public Node representData(final Object data) {
            final ConfigurationSerializable serializable = (ConfigurationSerializable) data;
            final Map<String, Object> values = new LinkedHashMap<>();
            values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
            values.putAll(serializable.serialize());

            return super.representData(values);
        }

    }

    private final class RepresentQuoteValue implements Represent {

        @Override
        public Node representData(final Object data) {
            final QuoteValue quoteValue = (QuoteValue) data;

            final DumperOptions.ScalarStyle quoteScalarStyle = SnakeYamlQuoteValue.getQuoteScalarStyle(quoteValue.getQuoteStyle());
            final Object value = quoteValue.getValue();

            if (value == null) {
                return representScalar(Tag.NULL, "", quoteScalarStyle);
            }

            DumperOptions.ScalarStyle defaultScalarStyle = getDefaultScalarStyle();
            setDefaultScalarStyle(quoteScalarStyle); // change default scalar style

            Node node;
            final Represent representer = findBaseRepresenter(value.getClass());
            if (representer instanceof SnakeYamlQuoteStyleRepresenter.RepresentTypeQuoteStyle) {
                node = ((SnakeYamlQuoteStyleRepresenter.RepresentTypeQuoteStyle) representer).representBaseData(value);
            } else {
                node = representer.representData(value);
            }

            setDefaultScalarStyle(defaultScalarStyle); // restore default scalar style

            return node;
        }

    }

    protected Represent findBaseRepresenter(Class<?> clazz) {
        if (this.representers.containsKey(clazz)) {
            return this.representers.get(clazz);
        }
        for (Class<?> baseClass : this.multiRepresenters.keySet()) {
            if (baseClass != null && baseClass.isAssignableFrom(clazz)) {
                return this.multiRepresenters.get(baseClass);
            }
        }
        if (this.multiRepresenters.containsKey(null)) {
            return this.multiRepresenters.get(null);
        }
        return this.representers.get(null);
    }

}
