package org.simpleyaml.configuration;

/**
 * Various settings for controlling the input and output of a {@link MemoryConfiguration}
 * @author Bukkit
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/ConfigurationOptions.java">Bukkit Source</a>
 */
public class MemoryConfigurationOptions extends ConfigurationOptions {
    protected MemoryConfigurationOptions(MemoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public MemoryConfiguration configuration() {
        return (MemoryConfiguration) super.configuration();
    }

    @Override
    public MemoryConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @Override
    public MemoryConfigurationOptions pathSeparator(char value) {
        super.pathSeparator(value);
        return this;
    }
}
