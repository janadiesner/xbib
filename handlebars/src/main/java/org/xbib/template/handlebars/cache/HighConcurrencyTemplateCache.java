
package org.xbib.template.handlebars.cache;

import org.xbib.template.handlebars.HandlebarsException;
import org.xbib.template.handlebars.util.Pair;
import org.xbib.template.handlebars.Parser;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.io.ForwardingTemplateSource;
import org.xbib.template.handlebars.io.TemplateSource;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static org.xbib.template.handlebars.util.Validate.notNull;


public class HighConcurrencyTemplateCache implements TemplateCache {

    /**
     * The map cache.
     */
    private final ConcurrentMap<TemplateSource, Future<Pair<TemplateSource, Template>>> cache;

    /**
     * Creates a new HighConcurrencyTemplateCache.
     *
     * @param cache The concurrent map cache. Required.
     */
    protected HighConcurrencyTemplateCache(
            final ConcurrentMap<TemplateSource, Future<Pair<TemplateSource, Template>>> cache) {
        this.cache = notNull(cache, "The cache is required.");
    }

    /**
     * Creates a new HighConcurrencyTemplateCache.
     */
    public HighConcurrencyTemplateCache() {
        this(new ConcurrentHashMap<TemplateSource, Future<Pair<TemplateSource, Template>>>());
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void evict(final TemplateSource source) {
        cache.remove(source);
    }

    @Override
    public Template get(final TemplateSource source, final Parser parser) throws IOException {
        notNull(source, "The source is required.");
        notNull(parser, "The parser is required.");

        /**
         * Don't keep duplicated entries, remove old templates if a change is detected.
         */
        return cacheGet(templateSource(source), parser);
    }

    /**
     * Don't keep duplicated entries, remove old templates if a change is detected.
     *
     * @param source template source.
     * @return A custom template source.
     */
    private static ForwardingTemplateSource templateSource(final TemplateSource source) {
        return new ForwardingTemplateSource(source) {
            @Override
            public boolean equals(final Object obj) {
                if (obj instanceof TemplateSource) {
                    return source.filename().equals(((TemplateSource) obj).filename());
                }
                return false;
            }

            @Override
            public int hashCode() {
                return source.filename().hashCode();
            }
        };
    }

    /**
     * Get/Parse a template source.
     *
     * @param source The template source.
     * @param parser The parser.
     * @return A Handlebars template.
     * @throws java.io.IOException If we can't read input.
     */
    private Template cacheGet(final TemplateSource source, final Parser parser) throws IOException {
        notNull(source, "The source is required.");
        notNull(parser, "The parser is required.");

        boolean interrupted = false;

        FutureTask<Pair<TemplateSource, Template>> futureTask = newTask(source, parser);
        try {
            while (true) {
                Future<Pair<TemplateSource, Template>> future = cache.get(source);
                try {
                    if (future == null) {
                        future = putIfAbsent(source, futureTask);
                    } else if (source.lastModified() != future.get().getLeft().lastModified()) {
                        evict(source);
                        future = putIfAbsent(source, futureTask);
                    } else {
                    }
                    Pair<TemplateSource, Template> entry = future.get();
                    return entry.getRight();
                } catch (CancellationException ex) {
                    cache.remove(source, future);
                } catch (InterruptedException ex) {
                    // fall through and retry
                    interrupted = true;
                } catch (ExecutionException ex) {
                    if (future != null) {
                        cache.remove(source, future);
                    }
                    throw launderThrowable(source, ex.getCause());
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Creates a new future task for compiling the given source.
     *
     * @param source The template source.
     * @param parser The handlebars parser.
     * @return A new future task.
     */
    private FutureTask<Pair<TemplateSource, Template>> newTask(final TemplateSource source,
                                                               final Parser parser) {
        return new FutureTask<Pair<TemplateSource, Template>>(
                new Callable<Pair<TemplateSource, Template>>() {
                    @Override
                    public Pair<TemplateSource, Template> call() throws IOException {
                        return Pair.of(source, parser.parse(source));
                    }
                });
    }

    /**
     * Compute and put the result of the future task.
     *
     * @param source     The template source.
     * @param futureTask The future task.
     * @return The resulting value.
     */
    private Future<Pair<TemplateSource, Template>> putIfAbsent(final TemplateSource source,
                                                               final FutureTask<Pair<TemplateSource, Template>> futureTask) {
        Future<Pair<TemplateSource, Template>> future = cache.putIfAbsent(source, futureTask);
        if (future == null) {
            future = futureTask;
            futureTask.run();
        }
        return future;
    }

    /**
     * Re-throw the cause of an execution exception.
     *
     * @param source The template source. Required.
     * @param cause  The cause of an execution exception.
     * @return Re-throw a cause of an execution exception.
     */
    private RuntimeException launderThrowable(final TemplateSource source, final Throwable cause) {
        if (cause instanceof RuntimeException) {
            return (RuntimeException) cause;
        } else if (cause instanceof Error) {
            throw (Error) cause;
        } else {
            return new HandlebarsException("Can't parse: " + source, cause);
        }
    }

}
