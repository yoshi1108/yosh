package sample;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;

public class DBCPConnectionFactory extends ConnectionFactory {

    private DataSource ds;

    protected DBCPConnectionFactory() {
        Properties properties = new Properties();
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream("dbcp.properties");
            properties.load(is);
            this.ds = BasicDataSourceFactory.createDataSource(properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
