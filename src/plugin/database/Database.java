package plugin.database;

import arc.util.Log;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import static plugin.PVars.*;

public class Database {
    public static final HikariDataSource dataSource = createDataSource();

    private static HikariDataSource createDataSource() {
        try {
            Class.forName("org.postgresql.Driver");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + db);
            config.setUsername(dbUser);
            if(!dbPassword.equals("empty") && !dbPassword.isEmpty())
                config.setPassword(dbPassword);

            config.setMaximumPoolSize(2);
            config.setMinimumIdle(1);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(5000);

            return new HikariDataSource(config);
        } catch (ClassNotFoundException err) {
            Log.err(err);
            return null;
        }
    }

    /**
     * @author <a href="https://github.com/ols45234">ols45234</a>
     * */
    public static <T> Optional<T> executeQueryAsync(String sql, ThrowingConsumer<PreparedStatement> parameterSetter, SQLFunction<ResultSet, T> mapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.apply(rs));
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
     * @author <a href="https://github.com/ols45234">ols45234</a>
     * */
    public static boolean executeUpdate(String sql, ThrowingConsumer<PreparedStatement> parameterSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);
            int updated = pstmt.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            // System.out.println(e);
            Log.err("SQL query failed @ @", sql, e);
            return false;
        }
    }
    /**
     * @author <a href="https://github.com/ols45234">ols45234</a>
     * */
    public static <T> List<T> executeQueryList(String sql, ThrowingConsumer<PreparedStatement> parameterSetter, SQLFunction<ResultSet, T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.apply(rs));
                }
            }
        } catch (SQLException e) {
            // System.out.println(e);
            Log.err("SQL query failed @ @", sql, e);
        }
        return results;
    }
    /**
     * @author <a href="https://github.com/ols45234">ols45234</a>
     * */
    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
    }
    /**
     * @author <a href="https://github.com/ols45234">ols45234</a>
     * */
    @FunctionalInterface
    public interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}
