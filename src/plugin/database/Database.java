package plugin.database;

import arc.util.Log;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static plugin.PVars.*;

/**
 * Database manager for the plugin.
 * Provides utility methods for executing SQL queries and updates using a Hikari connection pool.
 */
public class Database {
    /**
     * The shared data source for database connections.
     */
    public static final HikariDataSource dataSource = createDataSource();

    /**
     * Creates and configures the Hikari connection pool.
     *
     * @return A configured {@link HikariDataSource}, or null if the PostgreSQL driver is not found.
     */
    private static HikariDataSource createDataSource() {
        try {
            Class.forName("org.postgresql.Driver");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + db);
            config.setUsername(dbUser);
            if (!dbPassword.equals("empty") && !dbPassword.isEmpty())
                config.setPassword(dbPassword);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(3);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(5000);

            return new HikariDataSource(config);
        } catch (ClassNotFoundException err) {
            Log.err(err);
            return null;
        }
    }

    /**
     * Executes a SQL query asynchronously and returns an optional result.
     *
     * @param <T>              The type of the result object.
     * @param sql               The SQL query string.
     * @param statementSetter   A functional interface to set parameters on the {@link PreparedStatement}.
     * @param serealizer        A functional interface to map the {@link ResultSet} to the result object.
     * @return An {@link Optional} containing the result if found, otherwise empty.
     */
    public static <T> Optional<T> executeQueryAsync(String sql, StatementSetter<PreparedStatement> statementSetter, Serealizer<ResultSet, T> serealizer) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            statementSetter.accept(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(serealizer.apply(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            // System.out.println(e);
            Log.err("SQL query failed @ @", sql, e);
            return Optional.empty();
        }
    }

    /**
     * Executes a SQL update statement.
     *
     * @param sql               The SQL update string.
     * @param statementSetter   A functional interface to set parameters on the {@link PreparedStatement}.
     * @return true if one or more rows were updated, false otherwise.
     */
    public static boolean executeUpdate(String sql, StatementSetter<PreparedStatement> statementSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            statementSetter.accept(pstmt);
            int updated = pstmt.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            // System.out.println(e);
            Log.err("SQL query failed @ @", sql, e);
            return false;
        }
    }

    /**
     * Executes a SQL query and returns a list of all results.
     *
     * @param <T>              The type of the result object.
     * @param sql               The SQL query string.
     * @param statementSetter   A functional interface to set parameters on the {@link PreparedStatement}.
     * @param serealizer        A functional interface to map the {@link ResultSet} to the result object.
     * @return A list of result objects extracted from the query.
     */
    public static <T> List<T> executeQueryList(String sql, StatementSetter<PreparedStatement> statementSetter, Serealizer<ResultSet, T> serealizer) {
        List<T> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            statementSetter.accept(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(serealizer.apply(rs));
                }
            }
        } catch (SQLException e) {
            // System.out.println(e);
            Log.err("SQL query failed @ @", sql, e);
        }
        return results;
    }

    /**
     * Functional interface for setting parameters on a statement.
     *
     * @param <T> The type of the statement.
     */
    @FunctionalInterface
    public interface StatementSetter<T> {
        void accept(T t) throws SQLException;
    }

    /**
     * Functional interface for serializing a result set into a result object.
     *
     * @param <T> The type of the result set.
     * @param <R> The type of the result object.
     */
    @FunctionalInterface
    public interface Serealizer<T, R> {
        R apply(T t) throws SQLException;
    }
}
