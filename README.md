# A/Sync Postgres JSONB ORM

A super-simple ORM for Postgres / JSONB, using HikariCP underneath. Supports both sync / async usage. 

Note: I make no guarantees about it being perfectly thread-safe or anything. Use at your own risk.

This library will automatically create tables and indexes for you based off of the values of the entity class annotations. 
This may have performance implications, so it's probably a good idea to do initial class mapping once at the start of your 
application to get that out of the way. 

## Building

Just run `mvn clean package`.

To run the tests, set the following environment variables:
```
POSTGRES_URL="jdbc:postgresql://localhost/test"
POSTGRES_USERNAME="test"
POSTGRES_PASSWORD="abc123"
```
and then run `mvn test`.

## Usage

Get it on JitPack: https://jitpack.io/#queer/async-postgres-jsonb-orm

Example code:
```Java
// A class you could save / load
@Table("data_table") // Save/load with a table named `data_table`
@Index({"name", "something"}) // Index on the JSONB fields `name` and `something`
public class Data {
    // This field will be the primary key, and will be stored in the DB as a column named "id"  
    @PrimaryKey("id")
    private String primaryKey;
    
    private String name;
    private String something;
    private long someNumber;
    
    public Data(String primaryKey, String name, String something, String someNumber) {
        this.primaryKey = primaryKey;
        this.name = name;
        this.something = something;
        this.someNumber = someNumber;
    }
}

public class Example {
    // Make a data store
    public void makeStore() {
        // Pass a JDBC URL / username / password
        PgStore store = new PgStore("jdbc:postgresql://localhost/test", "test", "abc123");
        // Or pass your own HikariConfig instance
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:test");
        config.setUsername("test");
        config.setPassword("abc123");
        PgStore store = new PgStore(config);
        // Or configure it from the environment, by setting the following env vars:
        // - POSTGRES_URL="jdbc:postgresql://localhost/test"
        // - POSTGRES_USERNAME="test"
        // - POSTGRES_PASSWORD="abc123"
        PgStore store = PgStore.fromEnv();
    }
    
    // Save and load some data
    public void saveLoad() {
        PgStore store = PgStore.fromEnv();
        Data data = new Data("spooky id", "some name", "potato", 1234L);
        // Save the data synchronously
        store.mapSync(Data.class).save(data);
        // Load the data synchronously
        Optional<Data> result = store.mapSync(Data.class).load("spooky id");
        if(result.isPresent()) {
            // Yay we have it
        } else {
            // We don't have it and that's lame
        }
    }
    
    // Save and load some data but async this time
    public void saveLoadAsync() {
        PgStore store = PgStore.fromEnv();
        Data data = new Data("spooky id", "some name", "potato", 1234L);
        // Save the data asynchronously
        Future<Void> saveFuture = store.mapAsync(Data.class).save(data);
        saveFuture.get(); // Wait for it to finish
        // Load the data asynchronously
        Future<Optional<Data>> loadFuture = store.mapAsync(Data.class).load("spooky id");
        Optional<Data> result = loadFuture.get(); // Wait for it to finish
        if(result.isPresent()) {
            // Yay we have it
        } else {
            // We don't have it and that's lame
        }
    }
}
```

## Things to remember

- You can choose index type with `@BtreeIndex` vs `@GIndex`. The latter is GIN indexing.

- The `AsyncPgMapper` uses `Executors.newCachedThreadPool()` by default. In the future, there might be a way to set an 
  alternative.

- Data is mapped to/from JSON using Jackson. Make sure your entity classes work correctly with Jackson.