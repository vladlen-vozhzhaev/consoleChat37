package server;

import java.sql.*;
import java.util.ArrayList;

public class Message {
    private String msg;
    private int from_user;
    private int to_user;

    public Message(String msg, int from_user, int to_user) {
        this.msg = msg;
        this.from_user = from_user;
        this.to_user = to_user;
    }

    public String getMsg() {
        return msg;
    }

    public int getFrom_user() {
        return from_user;
    }

    public int getTo_user() {
        return to_user;
    }

    public void saveMessage(String db_url, String db_login, String db_pass) throws SQLException {
        Connection connection = DriverManager.getConnection(db_url, db_login, db_pass);
        Statement statement = connection.createStatement();
        statement.executeUpdate(
                "INSERT INTO messages (msg, to_user, from_user) VALUES (?,?,?)",
                new String[]{this.msg, String.valueOf(this.to_user), String.valueOf(this.from_user)});
        statement.close();
    }
    public static ArrayList<Message> readPublicMessages(String db_url, String db_login, String db_pass) throws SQLException{
        Connection connection = DriverManager.getConnection(db_url, db_login, db_pass);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM `messages`, `users` WHERE users.id = messages.from_user AND to_user='0'"
        );
        ArrayList<Message> messages = new ArrayList<>();
        while (resultSet.next()){
            String msg = resultSet.getString("msg");
            String name = resultSet.getString("name");
            int fromUser = resultSet.getInt("from_user");
            int to_user = 0;
            Message message = new Message(name+": "+msg, fromUser, to_user);
            messages.add(message);
        }
        statement.close();
        return messages;
    }
}
