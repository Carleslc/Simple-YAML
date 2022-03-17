package org.simpleyaml.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Every line written to this writer will be enqueued on flush with a fixed capacity.
 * <p>Those lines can be awaited and consumed.</p>
 * <p>This is an asynchronous blocking implementation. Different threads can write and read on this writer.</p>
 */
public class DumperBus extends Writer {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final BlockingQueue<Optional<String>> lineQueue;

    private final Dumper source;

    private StringBuffer lineBuffer;

    public DumperBus(final Dumper source, int capacity) {
        Validate.notNull(source, "Source not provided");
        this.source = source;
        this.lineQueue = new ArrayBlockingQueue<>(capacity, true);
    }

    public DumperBus(final Dumper source) {
        this(source, 100);
    }

    /**
     * Dump source values into this writer in a background thread.
     * @throws IOException if I/O error occurs
     */
    public void dump() throws IOException {
        this.lineBuffer = new StringBuffer();
        this.runThread(() -> {
            try {
                this.source.dump(this);
            } finally {
                this.close();
            }
        });
    }

    /**
     * Append to line a portion of an array of characters.
     * <p>New lines are flushed.</p>
     *
     * @param  str      Array of characters
     * @param  offset   Offset from which to start writing characters
     * @param  len      Number of characters to write
     *
     * @throws IndexOutOfBoundsException
     *         if {@code offset < 0} or {@code len < 0}
     *         or {@code offset+len > str.length}
     * @throws IOException if I/O error occurs
     */
    @Override
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void write(char[] str, int offset, int len) throws IOException {
        synchronized (lock) {
            // This only handles new lines at the end of the string to ensure good performance
            int last = offset + len - 1;
            if (last >= offset && last < str.length && str[last] == '\n') {
                last--;
                len--;
                if (last >= offset && str[last] == '\r') {
                    len--;
                }
                if (len > 0) {
                    this.lineBuffer.append(str, offset, len);
                }
                this.flush();
            } else {
                this.lineBuffer.append(str, offset, len);
            }
        }
    }

    /**
     * Append the current characters to the queue as a new line and reset the current line buffer.
     * @throws IOException if I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        if (this.lineBuffer.length() > 0) {
            this.append(this.lineBuffer.toString());
        }
        this.lineBuffer.setLength(0);
    }

    /**
     * Add a line to the queue.
     * @param line a new line to add
     * @throws IOException if I/O error occurs
     */
    private void append(final String line) throws IOException {
        try {
            this.lineQueue.put(Optional.ofNullable(line));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Consume the queue, blocking and awaiting a new line if the queue is empty.
     * @return the next line in the queue, may be null for the end of the file
     * @throws IOException if I/O error occurs
     */
    public String await() throws IOException {
        try {
            if (this.lineQueue.isEmpty() && this.isClosed()) {
                return null;
            }
            return this.lineQueue.take().orElse(null);
        } catch (InterruptedException e) {
            return null;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Called after dumping all the values.
     * @throws IOException if I/O error occurs
     */
    @Override
    public void close() throws IOException {
        if (!this.isClosed()) {
            this.flush();
            this.lineBuffer = null;
            this.append(null);
        }
        this.executor.shutdown();
    }

    protected boolean isClosed() {
        return this.lineBuffer == null;
    }

    /**
     * Get the dumper source where values are read.
     * @return the dumper source
     */
    public Dumper source() {
        return this.source;
    }

    /**
     * Start a new thread executing a task.
     * @param task the task to run
     * @throws IOException if I/O error occurs
     */
    protected void runThread(final Task task) throws IOException {
        try {
            this.executor.submit(() -> {
                task.run();
                return null;
            });
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Source for dumping values to this writer.
     */
    @FunctionalInterface
    public interface Dumper {
        void dump(final Writer writer) throws IOException;
    }

    /**
     * A runnable task that may throw an exception.
     */
    @FunctionalInterface
    private interface Task {
        void run() throws Exception;
    }
}
