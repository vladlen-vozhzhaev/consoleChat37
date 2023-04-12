package server;

import java.sql.*;

public class Database {
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/chat37";
    static final String DB_LOGIN = "root";
    static final String DB_PASS = "";
    static Connection connection;
    static Statement statement;
    // Для получения состояния БД
    public static ResultSet executeQuery(String sql){
        try {
            connection = DriverManager.getConnection(DB_URL, DB_LOGIN, DB_PASS);
            statement = connection.createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    // Для изменения состояния БД
    public static int executeUpdate(String sql){
        try {
            connection = DriverManager.getConnection(DB_URL, DB_LOGIN, DB_PASS);
            statement = connection.createStatement();
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
