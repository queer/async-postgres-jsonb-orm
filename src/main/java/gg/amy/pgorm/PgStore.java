package gg.amy.pgorm;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The actual datastore. Does the "heavy work" of interfacing with the
 * database. <p />
 * May also be configured from environment variables. The following are
 * supported:<br />
 * <ul>
 * <li><code>POSTGRES_URL</code> - The JDBC URL for connecting to the Postgres
 * server.</li>
 * <li><code>POSTGRES_USERNAME</code> - The username for logging in to the
 * Postgres server.</li>
 * <li><code>POSTGRES_PASSWORD</code> - The password for logging in to the
 * Postgres server.</li>
 * </ul>
 * <p/>
 * Default values:
 * <ul>
 * <li><code>cachePrepStmts</code> - <code>true</code></li>
 * <li><code>prepStmtCacheSize</code> - <code>250</code></li>
 * <li><code>prepStmtCacheSqlLimit</code> - <code>2048</code></li>
 * </ul>
 * If these values don't work for you for some reason, pass your own
 * {@link HikariConfig} to the constructor.
 *
 * @author amy
 * @since 4/10/18.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class PgStore {
    private final Map<Class<?>, PgMapper<?>> syncMappers = new ConcurrentHashMap<>();
    private final Map<Class<?>, AsyncPgMapper<?>> asyncMappers = new ConcurrentHashMap<>();
    private final HikariConfig config;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Getter(AccessLevel.PACKAGE)
    private HikariDataSource hikari;
    @Getter
    private boolean connected;
    
    public PgStore(final String url, final String user, final String pass) {
        this(buildConfig(url, user, pass));
    }
    
    public PgStore(final HikariConfig config) {
        this.config = config;
    }
    
    private static HikariConfig buildConfig(final String url, final String user, final String pass) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(pass);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return config;
    }
    
    public static PgStore fromEnv() {
        return new PgStore(System.getenv("POSTGRES_URL"), System.getenv("POSTGRES_USERNAME"),
                System.getenv("POSTGRES_PASSWORD"));
    }
    
    public static PgStore fromDsn(final String dsn) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dsn);
        return new PgStore(config);
    }
    
    public void connect() {
        if(connected) {
            return;
        }
        hikari = new HikariDataSource(config);
        connected = true;
    }
    
    public void disconnect() {
        if(!connected) {
            return;
        }
        hikari.close();
        connected = false;
    }
    
    public <T> boolean isMappedSync(final Class<T> c) {
        return syncMappers.containsKey(c);
    }
    
    public <T> boolean isMappedAsync(final Class<T> c) {
        return asyncMappers.containsKey(c);
    }
    
    public <T> PgMapper<T> mapSync(final Class<T> c) {
        if(syncMappers.containsKey(c)) {
            // This should be safe
            // If it's not, we REALLY screwed up...
            //noinspection unchecked
            return (PgMapper<T>) syncMappers.get(c);
        }
        // Create a new mapper
        final PgMapper<T> mapper = new PgMapper<>(this, c);
        syncMappers.put(c, mapper);
        return mapper;
    }
    
    public <T> AsyncPgMapper<T> mapAsync(final Class<T> c) {
        if(asyncMappers.containsKey(c)) {
            // This should be safe
            // If it's not, we REALLY screwed up...
            //noinspection unchecked
            return (AsyncPgMapper<T>) asyncMappers.get(c);
        }
        // Create a new mapper
        final PgMapper<T> sync = mapSync(c);
        final AsyncPgMapper<T> async = new AsyncPgMapper<>(sync);
        asyncMappers.put(c, async);
        return async;
    }
    
    public void sql(@SuppressWarnings("TypeMayBeWeakened") final SqlConsumer<Connection> consumer) {
        try(final Connection connection = hikari.getConnection()) {
            consumer.accept(connection);
        } catch(final SQLException e) {
            logger.error("Exception while executing SQL:", e);
            throw new IllegalStateException(e);
        }
    }
    
    public void sql(final String sql, @SuppressWarnings("TypeMayBeWeakened") final SqlConsumer<PreparedStatement> consumer) {
        sql(connection -> {
            try(final PreparedStatement statement = connection.prepareStatement(sql)) {
                logger.debug("Accepting consumer to prepare statement: {}", sql);
                consumer.accept(statement);
            } catch(final SQLException e) {
                logger.error("Exception while executing SQL statement '{}':", sql, e);
                throw new IllegalStateException(e);
            }
        });
    }
    
    public void sql(final String sql) {
        logger.debug("Running statement: {}", sql);
        sql(sql, PreparedStatement::execute);
    }
    
    @FunctionalInterface
    public interface SqlConsumer<T> extends Consumer<T> {
        @Override
        default void accept(final T t) {
            try {
                sql(t);
            } catch(final SQLException e) {
                throw new RuntimeException(e);
            }
        }
        
        void sql(T t) throws SQLException;
    }
}
