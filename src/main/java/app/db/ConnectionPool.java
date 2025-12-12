package app.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionPool {

    private HikariDataSource ds;

    public ConnectionPool(String user, String password, String url, String db)
            throws SQLException
    {
        try {
            Logger.getLogger("db").log(Level.INFO,
                    String.format("Connection Pool created for: (%s, %s, %s, %s)", user, password, url, db));
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.postgresql.Driver");
            config.setJdbcUrl(String.format(url, db));
            config.setUsername(user);
            config.setPassword(password);
            config.setMaximumPoolSize(3);
            config.setPoolName("Postgresql Pool");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            ds = new HikariDataSource(config);
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }

    }

    public synchronized Connection getConnection()
            throws SQLException
    {
        return ds.getConnection();
    }
}
