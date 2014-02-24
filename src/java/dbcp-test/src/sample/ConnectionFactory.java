package sample;

import java.sql.Connection;

public abstract class ConnectionFactory {

    protected ConnectionFactory() {
    }

    public static ConnectionFactory getInstance() {
        return new DBCPConnectionFactory();
    }

    public abstract Connection getConnection();

}