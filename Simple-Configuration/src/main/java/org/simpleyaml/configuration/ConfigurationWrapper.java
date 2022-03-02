package org.simpleyaml.configuration;

import org.simpleyaml.utils.Validate;

import java.util.Objects;

public class ConfigurationWrapper<T extends Configuration> {

    protected final T configuration;
    protected final String path;
    protected final ConfigurationWrapper<T> parent;

    protected ConfigurationWrapper(final T configuration, final String path, final ConfigurationWrapper<T> parent) {
        Validate.notNull(configuration, "configuration cannot be null!");
        this.configuration = configuration;
        this.path = parent == null ? path : parent.childPath(path);
        this.parent = parent;
    }

    public ConfigurationWrapper(final T configuration, final String path) {
        this(configuration, path, null);
    }

    /**
     * Get a wrapper builder for the given child path.
     * @param path child path of this section path, not null.
     * @throws IllegalArgumentException if path is null
     * @return the child path wrapper
     */
    public ConfigurationWrapper<T> path(final String path) {
        return new ConfigurationWrapper<>(configuration, path, this);
    }

    /**
     * Set the given value to this path.
     * <p>
     * If value is null, the entry will be removed. Any existing entry will be
     * replaced, regardless of what the new value is.
     *
     * @param value new value to set the path to.
     * @return this object, for chaining.
     */
    public ConfigurationWrapper<T> set(final Object value) {
        return set(configuration::set, value);
    }

    /**
     * Set the given value to the provided child path.
     * <p>
     * If value is null, the entry will be removed. Any existing entry will be
     * replaced, regardless of what the new value is.
     *
     * @param child the child path of this section path.
     * @param value new value to set the child path to.
     * @return this object, for chaining.
     */
    public ConfigurationWrapper<T> setChild(final String child, final Object value) {
        return setToChild(configuration::set, child, value);
    }

    /**
     * Sets the default value of this path.
     * <p>
     * If no source {@link Configuration} was provided as a default
     * collection, then a new {@link MemoryConfiguration} will be created to
     * hold the new default value.
     * <p>
     * If value is null, the value will be removed from the default
     * Configuration source.
     * <p>
     * If the value as returned by {@link ConfigurationSection#getDefaultSection()} is null, then
     * this will create a new section at the path, replacing anything that may
     * have existed there previously.
     *
     * @param value value to set the default to.
     * @return this object, for chaining.
     */
    public ConfigurationWrapper<T> addDefault(final Object value) {
        return set(configuration::addDefault, value);
    }

    /**
     * Sets the default value of the provided child path.
     * <p>
     * If no source {@link Configuration} was provided as a default
     * collection, then a new {@link MemoryConfiguration} will be created to
     * hold the new default value.
     * <p>
     * If value is null, the value will be removed from the default
     * Configuration source.
     * <p>
     * If the value as returned by {@link ConfigurationSection#getDefaultSection()} is null, then
     * this will create a new section at the path, replacing anything that may
     * have existed there previously.
     *
     * @param child the child path of this section path.
     * @param value value to set the default to.
     * @return this object, for chaining.
     */
    public ConfigurationWrapper<T> addDefault(final String child, final Object value) {
        return setToChild(configuration::addDefault, child, value);
    }

    /**
     * Creates an empty {@link ConfigurationSection} at this path.
     * <p>
     * Any value that was previously set at this path will be overwritten. If
     * the previous value was itself a {@link ConfigurationSection}, it will
     * be orphaned.
     *
     * @return this object, for chaining.
     */
    public ConfigurationWrapper<T> createSection() {
        return apply(configuration::createSection);
    }

    /**
     * Creates an empty {@link ConfigurationSection} at the provided child path.
     * <p>
     * Any value that was previously set at this  will be overwritten. If
     * the previous value was itself a {@link ConfigurationSection}, it will
     * be orphaned.
     *
     * @param child the child path of this section path.
     * @return this object, for chaining.
     */
    public ConfigurationWrapper<T> createSection(final String child) {
        return applyToChild(configuration::createSection, child);
    }

    /**
     * Gets the current path.
     * @return this path.
     */
    public String getCurrentPath() {
        return this.path;
    }

    /**
     * Returns to the parent.
     * @return the parent path wrapper, or null if this path does not have a parent.
     */
    public ConfigurationWrapper<T> parent() {
        if (this.parent == null && this.path != null) {
            int lastSectionIndex = this.path.lastIndexOf(this.configuration.options().pathSeparator());

            if (lastSectionIndex >= 0) {
                return new ConfigurationWrapper<>(this.configuration, this.path.substring(0, lastSectionIndex));
            }
        }
        return this.parent;
    }

    protected final String childPath(final String child) {
        if (this.path == null) {
            return child;
        }
        return this.path + this.configuration.options().pathSeparator() + child;
    }

    protected ConfigurationWrapper<T> apply(final ApplyToPath method) {
        method.apply(this.path);
        return this;
    }

    protected ConfigurationWrapper<T> applyToChild(final ApplyToPath method, final String path) {
        method.apply(this.childPath(path));
        return this;
    }

    protected ConfigurationWrapper<T> set(final SetToPath method, final Object value) {
        method.set(this.path, value);
        return this;
    }

    protected ConfigurationWrapper<T> setToChild(final SetToPath method, final String child, final Object value) {
        method.set(this.childPath(child), value);
        return this;
    }

    @FunctionalInterface
    protected interface ApplyToPath {
        void apply(final String path);
    }

    @FunctionalInterface
    protected interface SetToPath {
        <T> void set(final String path, final T value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationWrapper<?> that = (ConfigurationWrapper<?>) o;
        return configuration == that.configuration && Objects.equals(path, that.path) && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration, path, parent);
    }

}
