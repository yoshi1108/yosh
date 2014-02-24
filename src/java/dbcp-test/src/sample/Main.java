package sample;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final int CONNECTION_COUNT = 1;

    public static void main(String[] args) throws SQLException {

        System.out.println("start");

        ConnectionFactory factory = ConnectionFactory.getInstance();

        List<Connection> connections = createConnections(factory);
        releaseConnections(connections);

        List<Connection> connections2 = createConnections(factory);
        releaseConnections(connections2);

        List<Connection> connections3 = createConnections(factory);
        releaseConnections(connections3);

        System.out.println("end");
    }

    private static List<Connection> createConnections(ConnectionFactory factory) throws SQLException {
        List<Connection> conList = new ArrayList<Connection>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < CONNECTION_COUNT; i++) {
            Connection con = factory.getConnection();

            /** なんらかの処理 **/
            PreparedStatement ps = con.prepareCall("select * from hoge where id = ?");
            ps.setInt(1, 1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }
            ps.close();

            conList.add(con);
        }
        long end = System.currentTimeMillis();
        System.out.println("createConnections 所要時間:" + (end - start));
        return conList;
    }

    private static void releaseConnections(List<Connection> conList) throws SQLException {
        long start = System.currentTimeMillis();

        for (int i = 0; i < CONNECTION_COUNT; i++) {
            Connection con = conList.get(i);
            con.close();
        }
        conList.clear();
        long end = System.currentTimeMillis();
        System.out.println("releaseConnections 所要時間:" + (end - start));
    }

}