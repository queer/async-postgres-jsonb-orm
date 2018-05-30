package gg.amy.pgorm.query;

import gg.amy.pgorm.PgMapper;

/**
 * WARNING: THIS CLASS DOES NO ERROR CHECKING FOR QUERY SYNTAX. USE AT YOUR OWN
 * RISK. YOU HAVE BEEN WARNED.
 *
 * @author amy
 * @since 5/30/18.
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class QueryBuilder<T> {
    private static final String BASE = "SELECT DATA FROM ";
    private final PgMapper<T> mapper;
    private String query;
    
    public QueryBuilder(final PgMapper<T> mapper, final String table) {
        this.mapper = mapper;
        query = BASE + table + ' ';
    }
    
    public QueryBuilder where() {
        query += " WHERE ";
        return this;
    }
    
    public QueryBuilder has(final String field) {
        query += " data->" + field + " IS NOT NULL ";
        return this;
    }
    
    public QueryBuilder and() {
        query += " AND ";
        return this;
    }
    
    public QueryBuilder compareValue(final String field, final String type, final String value, final Comparison comparison) {
        query += " (data->" + field + ")::text::" + type + ' ' + comparison.symbol + ' ' + value + ' ';
        return this;
    }
    
    public QueryBuilder compareField(final String field1, final String type1, final String field2, final String type2, final Comparison comparison) {
        query += " (data->" + field1 + ")::text::" + type1 + ' ' + comparison.symbol + ' ' + "(data->" + field2 + ")::text::" + type2 + ' ';
        return this;
    }
    
    public QueryBuilder order(final String field, final String type, final Order order) {
        query += " ORDER BY (data->" + field + ")::text::" + type + ' ' + order.order + ' ';
        return this;
    }
    
    public enum Order {
        ASCENDING("ASC"),
        DESCENDING("DESC"),;
        
        private final String order;
        
        Order(final String order) {
            this.order = order;
        }
    }
    
    public enum Comparison {
        GT(">"),
        LT("<"),
        EQ("="),;
        
        private final String symbol;
        
        Comparison(final String symbol) {
            this.symbol = symbol;
        }
    }
}
