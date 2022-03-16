package org.simpleyaml.configuration;

import org.simpleyaml.configuration.serialization.ConfigurationSerializable;
import org.simpleyaml.utils.NumberConversions;
import org.simpleyaml.utils.StringUtils;
import org.simpleyaml.utils.Validate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * A type of {@link ConfigurationSection} that is stored in memory.
 *
 * @author Bukkit
 * @author Carlos Lazaro Costa (removed Bukkit-dependent accessors)
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/MemorySection.java">Bukkit Source</a>
 */
public class MemorySection implements ConfigurationSection {

    protected final Map<String, Object> map = new LinkedHashMap<>();

    private final Configuration root;

    private final ConfigurationSection parent;

    private final String path;

    private final String fullPath;

    /**
     * Creates an empty MemorySection for use as a root {@link Configuration}
     * section.
     * <p>
     * Note that calling this without being yourself a {@link Configuration}
     * will throw an exception!
     *
     * @throws IllegalStateException Thrown if this is not a {@link
     *                               Configuration} root.
     */
    protected MemorySection() {
        if (!(this instanceof Configuration)) {
            throw new IllegalStateException("Cannot construct a root MemorySection when not a Configuration");
        }

        this.path = "";
        this.fullPath = "";
        this.parent = null;
        this.root = (Configuration) this;
    }

    /**
     * Creates an empty MemorySection with the specified parent and path.
     *
     * @param parent Parent section that contains this own section.
     * @param path   Path that you may access this section from via the root
     *               {@link Configuration}.
     * @throws IllegalArgumentException Thrown is parent or path is null, or
     *                                  if parent contains no root Configuration.
     */
    protected MemorySection(final ConfigurationSection parent, final String path) {
        Validate.notNull(parent, "Parent cannot be null");
        Validate.notNull(path, "Path cannot be null");

        this.path = path;
        this.parent = parent;
        this.root = parent.getRoot();

        Validate.notNull(this.root, "Path cannot be orphaned");

        this.fullPath = MemorySection.createPath(parent, path);
    }

    /**
     * Creates a full path to the given {@link ConfigurationSection} from its
     * root {@link Configuration}.
     * <p>
     * You may use this method for any given {@link ConfigurationSection}, not
     * only {@link MemorySection}.
     *
     * @param section Section to create a path for.
     * @param key     Name of the specified section.
     * @return Full path of the section from its root.
     */
    public static String createPath(final ConfigurationSection section, final String key) {
        return MemorySection.createPath(section, key, section == null ? null : section.getRoot());
    }

    /**
     * Creates a relative path to the given {@link ConfigurationSection} from
     * the given relative section.
     * <p>
     * You may use this method for any given {@link ConfigurationSection}, not
     * only {@link MemorySection}.
     *
     * @param section    Section to create a path for.
     * @param key        Name of the specified section.
     * @param relativeTo Section to create the path relative to.
     * @return Full path of the section from its root.
     */
    public static String createPath(final ConfigurationSection section, final String key, final ConfigurationSection relativeTo) {
        Validate.notNull(section, "Cannot create path without a section");
        final Configuration root = section.getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot create path without a root");
        }
        final char separator = root.options().pathSeparator();

        final StringBuilder builder = new StringBuilder();
        for (ConfigurationSection parent = section; parent != null && parent != relativeTo; parent = parent.getParent()) {
            if (builder.length() > 0) {
                builder.insert(0, separator);
            }

            builder.insert(0, parent.getName());
        }

        if (key != null && key.length() > 0) {
            if (builder.length() > 0) {
                builder.append(separator);
            }

            builder.append(key);
        }

        return builder.toString();
    }

    @Override
    public Set<String> getKeys(final boolean deep) {
        final Set<String> result = new LinkedHashSet<>();

        final Configuration root = this.getRoot();
        if (root != null && root.options().copyDefaults()) {
            final ConfigurationSection defaults = this.getDefaultSection();

            if (defaults != null) {
                result.addAll(defaults.getKeys(deep));
            }
        }

        this.mapChildrenKeys(result, this, deep);

        return result;
    }

    @Override
    public Map<String, Object> getValues(final boolean deep) {
        final Map<String, Object> result = new LinkedHashMap<>();

        final Configuration root = this.getRoot();
        if (root != null && root.options().copyDefaults()) {
            final ConfigurationSection defaults = this.getDefaultSection();

            if (defaults != null) {
                result.putAll(defaults.getValues(deep));
            }
        }

        this.mapChildrenValues(result, this, deep);

        return result;
    }

    @Override
    public Map<String, Object> getMapValues(final boolean deep) {
        return this.getValues(deep).entrySet().stream()
            .map(entry -> {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                if (value instanceof ConfigurationSection) {
                    return new AbstractMap.SimpleEntry<>(key, ((ConfigurationSection) value).getMapValues(deep));
                }
                return new AbstractMap.SimpleEntry<>(key, value);
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public boolean contains(final String path) {
        return this.get(path) != null;
    }

    @Override
    public boolean isSet(final String path) {
        final Configuration root = this.getRoot();
        if (root == null) {
            return false;
        }
        if (root.options().copyDefaults()) {
            return this.contains(path);
        }
        return this.get(path, null) != null;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public String getCurrentPath() {
        return this.fullPath;
    }

    @Override
    public String getName() {
        return this.path;
    }

    @Override
    public Configuration getRoot() {
        return this.root;
    }

    @Override
    public ConfigurationSection getParent() {
        return this.parent;
    }

    @Override
    public Object get(final String path) {
        return this.get(path, this.getDefault(path));
    }

    @Override
    public Object get(final String path, final Object def) {
        Validate.notNull(path, "Path cannot be null");

        if (path.length() == 0) {
            return this;
        }

        final Configuration root = this.getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot access section without a root");
        }

        final char separator = root.options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        Object section = this;
        while ((i1 = StringUtils.firstSeparatorIndex(path, separator, i2 = i1 + 1)) != -1) {
            final String node = path.substring(i2, i1);
            section = this.getSection(section, node);
            if (section == null) {
                return def;
            }
        }

        final String key = path.substring(i2);

        return this.getObject(section, key, def);
    }

    @Override
    public void set(final String path, final Object value) {
        Validate.notNull(path, "Path cannot be null");

        final Configuration root = this.getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot use section without a root");
        }

        final char separator = root.options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        Object section = this;
        while ((i1 = StringUtils.firstSeparatorIndex(path, separator, i2 = i1 + 1)) != -1) {
            final String node = path.substring(i2, i1);
            final Object subSection = this.getSection(section, node);
            if (subSection == null) {
                if (section instanceof ConfigurationSection) {
                    section = ((ConfigurationSection) section).createSection(node);
                } else {
                    return;
                }
            } else {
                section = subSection;
            }
        }

        final String key = path.substring(i2);

        this.setObject(section, key, value);
    }

    @Override
    public ConfigurationSection createSection(final String path) {
        Validate.notEmpty(path, "Cannot create section at empty path");
        final Configuration root = this.getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot create section without a root");
        }

        final char separator = root.options().pathSeparator();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        Object section = this;
        while ((i1 = StringUtils.firstSeparatorIndex(path, separator, i2 = i1 + 1)) != -1) {
            final String node = path.substring(i2, i1);
            final Object subSection = this.getSection(section, node);
            if (subSection == null) {
                if (section instanceof ConfigurationSection) {
                    section = ((ConfigurationSection) section).createSection(node);
                } else {
                    return null;
                }
            } else {
                section = subSection;
            }
        }

        final String key = path.substring(i2);

        if (section == this) {
            final ConfigurationSection result = new MemorySection(this, key);
            this.map.put(key, result);
            return result;
        }
        if (section instanceof ConfigurationSection) {
            return ((ConfigurationSection) section).createSection(key);
        }
        return null;
    }

    private Object getObject(final Object section, final String node, final Object def) {
        Matcher listIndex = StringUtils.LIST_INDEX.matcher(node);

        if (!listIndex.matches()) {
            // Not indexed
            return this.getObjectRaw(section, node, def);
        }

        // Indexed
        Object object = findIndexed(section, listIndex.group(1), Integer.parseInt(listIndex.group(2)));

        return object != null ? object : def;
    }

    @SuppressWarnings("unchecked")
    private Object getObjectRaw(Object section, final String node, final Object def) {
        if (section == this) {
            section = this.map;
        }
        if (section instanceof ConfigurationSection) {
            return ((ConfigurationSection) section).get(node, def);
        }
        if (section instanceof ConfigurationSerializable) {
            section = ((ConfigurationSerializable) section).serialize();
        }
        if (section instanceof Map) {
            return ((Map<?, Object>) section).getOrDefault(node, def);
        }
        return def;
    }

    @SuppressWarnings("unchecked")
    private <K, V> void setObject(final Object section, final K node, final V value) {
        Matcher listIndex = StringUtils.LIST_INDEX.matcher((CharSequence) node);

        if (!listIndex.matches()) {
            // Not indexed
            this.setObjectRaw(section, node, value);
        } else {
            // Indexed
            Object it = null;
            String iterableNode = listIndex.group(1);

            if (iterableNode != null && !iterableNode.isEmpty()) {
                it = find(section, iterableNode);
            }

            if (it instanceof MemorySection) {
                it = ((MemorySection) section).map;
            } else if (it instanceof ConfigurationSection) {
                it = ((ConfigurationSection) section).getValues(false);
            }

            if (it != null) {
                int index = Integer.parseInt(listIndex.group(2));

                if (it instanceof Map) {
                    int len = ((Map<K, V>) it).size();
                    index = asListIndex(index, len);
                    if (index >= 0 && index < len) {
                        K key = null;
                        Iterator<K> iterator = ((Map<K, V>) it).keySet().iterator();
                        int j = -1;
                        while (iterator.hasNext() && ++j <= index) {
                            key = iterator.next();
                        }
                        this.setObjectRaw(section, key, value);
                    }
                } else if (it instanceof List) {
                    final List<V> list = (List<V>) it;
                    int len = list.size();

                    if (value == null && index == -1 && !list.isEmpty()) {
                        list.remove(len - 1);
                    } else if (value != null && (index == -1 || index == len)) {
                        list.add(value);
                    } else {
                        index = asListIndex(index, len);
                        if (index >= 0 && index < len) {
                            if (value == null) {
                                list.remove(index);
                            } else {
                                list.set(index, value);
                            }
                        }
                    }
                } else if (it instanceof Collection && value != null) {
                    final Collection<V> collection = (Collection<V>) it;
                    int len = collection.size();

                    if (index == -1 || index == 0 || index == len) {
                        ((Collection<V>) it).add(value);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <K, V> void setObjectRaw(Object section, final K key, final V value) {
        if (key != null) {
            if (section == this) {
                section = this.map;
            }
            if (section instanceof ConfigurationSection) {
                ((ConfigurationSection) section).set(String.valueOf(key), value);
            } else if (section instanceof Map) {
                if (value == null) {
                    ((Map<K, V>) section).remove(key);
                } else {
                    ((Map<K, V>) section).put(key, value);
                }
            }
        }
    }

    private Object getSection(final Object parent, final String node) {
        Matcher listIndex = StringUtils.LIST_INDEX.matcher(node);

        if (!listIndex.matches()) {
            // Not indexed
            return findSection(parent, node);
        }

        // Indexed
        final Object section = findIndexed(parent, listIndex.group(1), Integer.parseInt(listIndex.group(2)));

        return isSection(section) ? section : null;
    }

    private static boolean isSection(Object section) {
        return section instanceof ConfigurationSection || section instanceof ConfigurationSerializable || section instanceof Map;
    }

    private static Object findSection(Object section, final String node) {
        if (section instanceof ConfigurationSection) {
            final ConfigurationSection configurationSection = (ConfigurationSection) section;
            section = configurationSection.get(node, null);
            if (section == null && configurationSection instanceof MemorySection) {
                section = ((MemorySection) configurationSection).getDefault(node);
                if (section instanceof ConfigurationSection) {
                    section = configurationSection.createSection(node);
                }
            }
        } else if (section instanceof Map) {
            section = ((Map<?, ?>) section).get(node);
        }
        return isSection(section) ? section : null;
    }

    private static Object find(final Object section, final String node) {
        Object it = null;
        if (section instanceof ConfigurationSection) {
            final ConfigurationSection configurationSection = (ConfigurationSection) section;
            it = configurationSection.get(node, null);
            if (it == null && configurationSection instanceof MemorySection) {
                it = ((MemorySection) configurationSection).getDefault(node);
            }
        } else if (section instanceof Map) {
            it = ((Map<?, ?>) section).get(node);
        }
        return it;
    }

    private static Iterable<?> getIterable(Object section, final String node) {
        if (node != null && !node.isEmpty()) {
            section = find(section, node);
        }
        if (section instanceof ConfigurationSection) {
            section = ((ConfigurationSection) section).getValues(false).values();
        }
        return section instanceof Iterable ? (Iterable<?>) section : null;
    }

    private static Object findIndexed(final Object section, final String iterableNode, final int index) {
        return getIndexed(getIterable(section, iterableNode), index);
    }

    private static Object getIndexed(final Iterable<?> iterable, int index) {
        if (iterable == null) {
            return null;
        }

        if (iterable instanceof Collection) {
            int len = ((Collection<?>) iterable).size();

            index = asListIndex(index, len);
            if (index < 0 || index >= len) {
                return null;
            }
        }

        if (iterable instanceof List) {
            return ((List<?>) iterable).get(index); // O(1) for ArrayList, O(N) for LinkedList ...
        }

        Object value = null;

        final Iterator<?> it = iterable.iterator();

        if (index >= 0) {
            // Positive indexing of a iterable
            // time O(index + 1) <= O(N), N = iterable size

            int i = -1;

            while (it.hasNext()) {
                value = it.next();

                if (++i == index) {
                    break;
                }
            }

            if (i != index) {
                return null; // out of bounds
            }
        } else {
            // Negative indexing of a non-Collection iterable
            // time O(N), N = iterable size
            // memory O(W) <= O(N), W = -index

            LinkedList<Object> window = new LinkedList<>();
            int windowSize = -index;
            int filled = 0;

            while (it.hasNext()) {
                window.add(it.next());

                if (filled == windowSize) {
                    window.removeFirst();
                } else {
                    filled++;
                }
            }

            if (filled < windowSize) {
                return null; // out of bounds
            }

            value = window.getFirst();
        }

        return value;
    }

    private static int asListIndex(int i, int size) {
        if (i < 0) {
            return size + i; // convert negative to positive indexing
        }
        return i;
    }

    // Primitives

    @Override
    public String getString(final String path) {
        final Object def = this.getDefault(path);
        return this.getString(path, def != null ? def.toString() : null);
    }

    @Override
    public String getString(final String path, final String def) {
        final Object val = this.get(path, def);
        return val != null ? val.toString() : def;
    }

    @Override
    public boolean isString(final String path) {
        final Object val = this.get(path);
        return val instanceof String;
    }

    @Override
    public int getInt(final String path) {
        final Object def = this.getDefault(path);
        return this.getInt(path, def instanceof Number ? NumberConversions.toInt(def) : 0);
    }

    @Override
    public int getInt(final String path, final int def) {
        final Object val = this.get(path, def);
        return val instanceof Number ? NumberConversions.toInt(val) : def;
    }

    @Override
    public boolean isInt(final String path) {
        final Object val = this.get(path);
        return val instanceof Integer;
    }

    @Override
    public boolean getBoolean(final String path) {
        final Object def = this.getDefault(path);
        return this.getBoolean(path, def instanceof Boolean ? (Boolean) def : false);
    }

    @Override
    public boolean getBoolean(final String path, final boolean def) {
        final Object val = this.get(path, def);
        return val instanceof Boolean ? (Boolean) val : def;
    }

    @Override
    public boolean isBoolean(final String path) {
        final Object val = this.get(path);
        return val instanceof Boolean;
    }

    @Override
    public double getDouble(final String path) {
        final Object def = this.getDefault(path);
        return this.getDouble(path, def instanceof Number ? NumberConversions.toDouble(def) : 0);
    }

    @Override
    public double getDouble(final String path, final double def) {
        final Object val = this.get(path, def);
        return val instanceof Number ? NumberConversions.toDouble(val) : def;
    }

    @Override
    public boolean isDouble(final String path) {
        final Object val = this.get(path);
        return val instanceof Double;
    }

    @Override
    public long getLong(final String path) {
        final Object def = this.getDefault(path);
        return this.getLong(path, def instanceof Number ? NumberConversions.toLong(def) : 0);
    }

    @Override
    public long getLong(final String path, final long def) {
        final Object val = this.get(path, def);
        return val instanceof Number ? NumberConversions.toLong(val) : def;
    }

    @Override
    public boolean isLong(final String path) {
        final Object val = this.get(path);
        return val instanceof Long;
    }

    // Java
    @Override
    public List<?> getList(final String path) {
        final Object def = this.getDefault(path);
        return this.getList(path, def instanceof List ? (List<?>) def : null);
    }

    @Override
    public boolean isList(final String path) {
        final Object val = this.get(path);
        return val instanceof List;
    }

    @Override
    public List<String> getStringList(final String path) {
        final List<?> list = this.getList(path);

        if (list == null) {
            return new ArrayList<>(0);
        }

        final List<String> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof String || this.isPrimitiveWrapper(object)) {
                result.add(String.valueOf(object));
            }
        }

        return result;
    }

    @Override
    public List<Integer> getIntegerList(final String path) {
        final List<?> list = this.getList(path);

        if (list == null) {
            return new ArrayList<>(0);
        }

        final List<Integer> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Integer) {
                result.add((Integer) object);
            } else if (object instanceof String) {
                try {
                    result.add(Integer.valueOf((String) object));
                } catch (final Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((int) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).intValue());
            }
        }

        return result;
    }

    @Override
    public List<Boolean> getBooleanList(final String path) {
        final List<?> list = this.getList(path);

        if (list == null) {
            return new ArrayList<>(0);
        }

        final List<Boolean> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Boolean) {
                result.add((Boolean) object);
            } else if (object instanceof String) {
                if (Boolean.TRUE.toString().equals(object)) {
                    result.add(true);
                } else if (Boolean.FALSE.toString().equals(object)) {
                    result.add(false);
                }
            }
        }

        return result;
    }

    @Override
    public List<Double> getDoubleList(final String path) {
        final List<?> list = this.getList(path);

        if (list == null) {
            return new ArrayList<>(0);
        }

        final List<Double> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Double) {
                result.add((Double) object);
            } else if (object instanceof String) {
                try {
                    result.add(Double.valueOf((String) object));
                } catch (final Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((double) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).doubleValue());
            }
        }

        return result;
    }

    @Override
    public List<Float> getFloatList(final String path) {
        final List<?> list = this.getList(path);

        if (list == null) {
            return new ArrayList<>(0);
        }

        final List<Float> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Float) {
                result.add((Float) object);
            } else if (object instanceof String) {
                try {
                    result.add(Float.valueOf((String) object));
                } catch (final Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((float) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).floatValue());
            }
        }

        return result;
    }

    @Override
    public List<Long> getLongList(final String path) {
        final List<?> list = this.getList(path);

        if (list == null) {
            return new ArrayList<>(0);
        }

        final List<Long> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Long) {
                result.add((Long) object);
            } else if (object instanceof String) {
                try {
                    result.add(Long.valueOf((String) object));
                } catch (final Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((long) (Character) object);
            } else if (object instanceof Number) {
                result.add(((Number) object).longValue());
            }
        }

        return result;
    }

    @Override
    public List<Byte> getByteList(final String path) {
        final List<?> list = this.getList(path);

        if (list == null) {
            return new ArrayList<>(0);
        }

        final List<Byte> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Byte) {
                result.add((Byte) object);
            } else if (object instanceof String) {
                try {
                    result.add(Byte.valueOf((String) object));
                } catch (final Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((byte) ((Character) object).charValue());
            } else if (object instanceof Number) {
                result.add(((Number) object).byteValue());
            }
        }

        return result;
    }

    @Override
    public List<Character> getCharacterList(final String path) {
        final List<?> list = this.getList(path);

        if (list == null) {
            return new ArrayList<>(0);
        }

        final List<Character> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Character) {
                result.add((Character) object);
            } else if (object instanceof String) {
                final String str = (String) object;

                if (str.length() == 1) {
                    result.add(str.charAt(0));
                }
            } else if (object instanceof Number) {
                result.add((char) ((Number) object).intValue());
            }
        }

        return result;
    }

    @Override
    public List<Short> getShortList(final String path) {
        final List<?> list = this.getList(path);

        if (list == null) {
            return new ArrayList<>(0);
        }

        final List<Short> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Short) {
                result.add((Short) object);
            } else if (object instanceof String) {
                try {
                    result.add(Short.valueOf((String) object));
                } catch (final Exception ignored) {
                }
            } else if (object instanceof Character) {
                result.add((short) ((Character) object).charValue());
            } else if (object instanceof Number) {
                result.add(((Number) object).shortValue());
            }
        }

        return result;
    }

    @Override
    public List<Map<?, ?>> getMapList(final String path) {
        final List<?> list = this.getList(path);
        final List<Map<?, ?>> result = new ArrayList<>();

        if (list == null) {
            return result;
        }

        for (final Object object : list) {
            if (object instanceof Map) {
                result.add((Map<?, ?>) object);
            }
        }

        return result;
    }

    @Override
    public ConfigurationSection getConfigurationSection(final String path) {
        Object val = this.get(path, null);
        if (val != null) {
            return val instanceof ConfigurationSection ? (ConfigurationSection) val : null;
        }

        val = this.get(path, this.getDefault(path));
        return val instanceof ConfigurationSection ? this.createSection(path) : null;
    }

    @Override
    public boolean isConfigurationSection(final String path) {
        final Object val = this.get(path);
        return val instanceof ConfigurationSection;
    }

    @Override
    public ConfigurationSection getDefaultSection() {
        final Configuration root = this.getRoot();
        final Configuration defaults = root == null ? null : root.getDefaults();

        if (defaults != null) {
            if (defaults.isConfigurationSection(this.getCurrentPath())) {
                return defaults.getConfigurationSection(this.getCurrentPath());
            }
        }

        return null;
    }

    @Override
    public void addDefault(final String path, final Object value) {
        Validate.notNull(path, "Path cannot be null");

        final Configuration root = this.getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot add default without root");
        }
        if (root == this) {
            throw new UnsupportedOperationException("Unsupported addDefault(String, Object) implementation");
        }
        root.addDefault(MemorySection.createPath(this, path), value);
    }

    public ConfigurationSection createSection(final String path, final Map<?, ?> map) {
        final ConfigurationSection section = this.createSection(path);

        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                section.createSection(entry.getKey().toString(), (Map<?, ?>) entry.getValue());
            } else {
                section.set(entry.getKey().toString(), entry.getValue());
            }
        }

        return section;
    }

    public List<?> getList(final String path, final List<?> def) {
        final Object val = this.get(path, def);
        return (List<?>) (val instanceof List ? val : def);
    }

    @Override
    public String toString() {
        final Configuration root = this.getRoot();
        return this.getClass().getSimpleName() +
                "[path='" +
                this.getCurrentPath() +
                "', root='" +
                (root == null ? null : root.getClass().getSimpleName()) +
                "']";
    }

    protected boolean isPrimitiveWrapper(final Object input) {
        return input instanceof Integer || input instanceof Boolean ||
            input instanceof Character || input instanceof Byte ||
            input instanceof Short || input instanceof Double ||
            input instanceof Long || input instanceof Float;
    }

    protected Object getDefault(final String path) {
        Validate.notNull(path, "Path cannot be null");

        final Configuration root = this.getRoot();
        final Configuration defaults = root == null ? null : root.getDefaults();
        return defaults == null ? null : defaults.get(MemorySection.createPath(this, path));
    }

    protected void mapChildrenKeys(final Set<String> output, final ConfigurationSection section, final boolean deep) {
        if (section instanceof MemorySection) {
            final MemorySection sec = (MemorySection) section;

            for (final Map.Entry<String, Object> entry : sec.map.entrySet()) {
                output.add(MemorySection.createPath(section, entry.getKey(), this));

                if (deep && entry.getValue() instanceof ConfigurationSection) {
                    final ConfigurationSection subsection = (ConfigurationSection) entry.getValue();
                    this.mapChildrenKeys(output, subsection, deep);
                }
            }
        } else {
            final Set<String> keys = section.getKeys(deep);

            for (final String key : keys) {
                output.add(MemorySection.createPath(section, key, this));
            }
        }
    }

    protected void mapChildrenValues(final Map<String, Object> output, final ConfigurationSection section, final boolean deep) {
        if (section instanceof MemorySection) {
            final MemorySection sec = (MemorySection) section;

            for (final Map.Entry<String, Object> entry : sec.map.entrySet()) {
                output.put(MemorySection.createPath(section, entry.getKey(), this), entry.getValue());

                if (deep && (entry.getValue() instanceof ConfigurationSection)) {
                    this.mapChildrenValues(output, (ConfigurationSection) entry.getValue(), true);
                }
            }
        } else {
            final Map<String, Object> values = section.getValues(deep);

            for (final Map.Entry<String, Object> entry : values.entrySet()) {
                output.put(MemorySection.createPath(section, entry.getKey(), this), entry.getValue());
            }
        }
    }

}
