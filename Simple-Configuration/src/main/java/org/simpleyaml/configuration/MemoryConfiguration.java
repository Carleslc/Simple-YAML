package org.simpleyaml.configuration;

import org.simpleyaml.utils.Validate;

import java.util.Map;

/**
 * This is a {@link Configuration} implementation that does not save or load
 * from any source, and stores all values in memory only.
 * This is useful for temporary Configurations for providing defaults.
 *
 * @author Bukkit
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/MemoryConfiguration.java">Bukkit Source</a>
 */
public class MemoryConfiguration extends MemorySection implements Configuration {

    protected Configuration defaults;

    protected MemoryConfigurationOptions options;

    /**
     * Creates an empty {@link MemoryConfiguration} with no default values.
     */
    public MemoryConfiguration() {}

    /**
     * Creates an empty {@link MemoryConfiguration} using the specified
     * {@link Configuration} as a source for all default values.
     *
     * @param defaults Default value provider
     * @throws IllegalArgumentException Thrown if defaults is null
     */
    public MemoryConfiguration(final Configuration defaults) {
        this.defaults = defaults;
    }

    @Override
    public void addDefaults(final Map<String, Object> defaults) {
        Validate.notNull(defaults, "Defaults may not be null");

        for (final Map.Entry<String, Object> entry : defaults.entrySet()) {
            this.addDefault(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void addDefaults(final Configuration defaults) {
        Validate.notNull(defaults, "Defaults may not be null");

        for (String key : defaults.getKeys(true)) {
            if (!defaults.isConfigurationSection(key)) {
                addDefault(key, defaults.get(key));
            }
        }
    }

    @Override
    public Configuration getDefaults() {
        return this.defaults;
    }

    @Override
    public void setDefaults(final Configuration defaults) {
        Validate.notNull(defaults, "Defaults may not be null");

        this.defaults = defaults;
    }

    @Override
    public MemoryConfigurationOptions options() {
        if (this.options == null) {
            this.options = new MemoryConfigurationOptions(this);
        }
        return this.options;
    }

    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    @Override
    public void addDefault(final String path, final Object value) {
        Validate.notNull(path, "Path may not be null");

        if (this.defaults == null) {
            this.defaults = new MemoryConfiguration();
        }

        this.defaults.set(path, value);
    }

}
