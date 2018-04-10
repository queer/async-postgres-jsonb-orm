package gg.amy.pgorm;

import gg.amy.pgorm.annotations.Index;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author amy
 * @since 4/10/18.
 */
@SuppressWarnings("WeakerAccess")
public class PgMapperTest {
    public static final String TEST_DATA_TABLE = "test_data_table";
    
    private PgStore store;
    
    private boolean envHas(final String key) {
        return System.getenv(key) != null;
    }
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean canTest() {
        return envHas("POSTGRES_URL") && envHas("POSTGRES_USERNAME") && envHas("POSTGRES_PASSWORD");
    }
    
    @Before
    public void createStore() {
        if(!canTest()) {
            return;
        }
        store = PgStore.fromEnv();
        store.connect();
        store.mapSync(CorrectTestDataClass.class);
    }
    
    @After
    public void deleteStore() {
        if(!canTest()) {
            return;
        }
        store.sql("DROP TABLE IF EXISTS " + TEST_DATA_TABLE + ';');
        store.disconnect();
    }
    
    @Test
    public void save() {
        if(!canTest()) {
            return;
        }
        final CorrectTestDataClass test = new CorrectTestDataClass("1234", "test", "something");
        System.out.println("save(): Saving data: " + test);
        store.mapSync(CorrectTestDataClass.class).save(test);
        System.out.println("save(): Successfully saved data: " + test);
    }
    
    @Test
    public void load() {
        if(!canTest()) {
            return;
        }
        final CorrectTestDataClass test = new CorrectTestDataClass("1234", "test", "something");
        System.out.println("load(): Saving data: " + test);
        store.mapSync(CorrectTestDataClass.class).save(test);
        final Optional<CorrectTestDataClass> loaded = store.mapSync(CorrectTestDataClass.class).load("1234");
        if(loaded.isPresent()) {
            assertEquals(test, loaded.get());
            System.out.println("load(): Successfully loaded data: " + test + " = " + loaded.get());
        } else {
            throw new IllegalStateException("load(): Loaded data not present!?");
        }
    }
    
    @Test(expected = IllegalStateException.class)
    public void noPrimaryKeyThrowsException() {
        store.mapSync(DataClassNoPk.class);
    }
    
    @Test(expected = IllegalStateException.class)
    public void noTableThrowsException() {
        store.mapSync(DataClassNoTable.class);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidSqlPk() {
        store.mapSync(DataClassBadPkType.class);
    }
    
    @Value
    @Table(TEST_DATA_TABLE)
    @Index({"name", "something"})
    @ToString
    @SuppressWarnings("WeakerAccess")
    @EqualsAndHashCode
    public static final class DataClassBadPkType {
        @PrimaryKey
        private Object id;
    
        private String name;
    
        private String something;
    }
    
    @Value
    @ToString
    @SuppressWarnings("WeakerAccess")
    @EqualsAndHashCode
    public static final class DataClassNoTable {
        @PrimaryKey
        private String id;
        
        private String name;
        
        private String something;
    }
    
    @Value
    @Table(TEST_DATA_TABLE)
    @Index({"name", "something"})
    @ToString
    @SuppressWarnings("WeakerAccess")
    @EqualsAndHashCode
    public static final class DataClassNoPk {
        private String id;
    
        private String name;
    
        private String something;
    }
    
    @Value
    @Table(TEST_DATA_TABLE)
    @Index({"name", "something"})
    @ToString
    @SuppressWarnings("WeakerAccess")
    @EqualsAndHashCode
    public static final class CorrectTestDataClass {
        @PrimaryKey
        private String id;
        
        private String name;
        
        private String something;
    }
}