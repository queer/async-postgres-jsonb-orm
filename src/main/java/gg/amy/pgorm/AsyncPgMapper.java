package gg.amy.pgorm;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author amy
 * @since 4/10/18.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class AsyncPgMapper<T> {
    private static final ExecutorService POOL = Executors.newCachedThreadPool();
    private final PgMapper<T> mapper;
    
    public AsyncPgMapper(final PgMapper<T> mapper) {
        this.mapper = mapper;
    }
    
    public CompletableFuture<Void> save(final T entity) {
        return CompletableFuture.supplyAsync(() -> {
            mapper.save(entity);
            return null;
        }, POOL);
    }
    
    public CompletableFuture<Optional<T>> load(final Object pk) {
        return CompletableFuture.supplyAsync(() -> mapper.load(pk), POOL);
    }
}
