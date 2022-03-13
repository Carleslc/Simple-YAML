package org.simpleyaml.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * IO Factory. Can be implemented as a lambda function.
 * @param <T> a closeable java.io object like Reader or InputStream.
 */
@FunctionalInterface
public interface SupplierIO<T extends Closeable> {
    T get() throws IOException;

    /**
     * {@code Reader get()}
     * <br>
     * Can be implemented as a lambda function. e.g. {@code () -> new BufferedReader(...)}
     */
    interface Reader extends SupplierIO<java.io.Reader> {}

    /**
     * {@code InputStream get()}
     * <br>
     * Can be implemented as a lambda function. e.g. {@code () -> new FileInputStream(file)}
     */
    interface InputStream extends SupplierIO<java.io.InputStream> {}
}
