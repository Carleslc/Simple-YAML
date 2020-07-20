package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.serialization.ConfigurationSerializable;
import org.simpleyaml.configuration.serialization.ConfigurationSerialization;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Bukkit
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/file/YamlRepresenter.java">Bukkit Source</a>
 */
public class YamlRepresenter extends Representer {

    public YamlRepresenter() {
        this.multiRepresenters.put(ConfigurationSection.class, new RepresentConfigurationSection());
        this.multiRepresenters.put(ConfigurationSerializable.class, new RepresentConfigurationSerializable());
    }

    private class RepresentConfigurationSection extends RepresentMap {

        @Override
        public Node representData(final Object data) {
            return super.representData(((ConfigurationSection) data).getValues(false));
        }

    }

    private class RepresentConfigurationSerializable extends RepresentMap {

        @Override
        public Node representData(final Object data) {
            final ConfigurationSerializable serializable = (ConfigurationSerializable) data;
            final Map<String, Object> values = new LinkedHashMap<String, Object>();
            values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
            values.putAll(serializable.serialize());

            return super.representData(values);
        }

    }

}
