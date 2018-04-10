package gg.amy.pgorm;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author amy
 * @since 4/10/18.
 */
public class AsyncPgMapper<T> {
    private final PgMapper<T> mapper;
    
    // TODO: Provide a way to customize this
    private static final ExecutorService POOL = Executors.newCachedThreadPool();
    
    public AsyncPgMapper(final PgMapper<T> mapper) {
        this.mapper = mapper;
    }
    
    public Future<Void> save(T entity) {
        return POOL.submit(() -> {
            mapper.save(entity);
            return null;
        });
    }
    
    public Future<Optional<T>> load(Object pk) {
        return POOL.submit(() -> mapper.load(pk));
    }
}
