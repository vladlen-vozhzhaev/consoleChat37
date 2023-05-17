package server;

import java.sql.*;
import java.util.ArrayList;

public class Message {
    private String msg;
    private int from_user;
    private int to_user;
    private byte type;

    public Message(String msg, int from_user, int to_user, byte type) {
        this.msg = msg;
        this.from_user = from_user;
        this.to_user = to_user;
        this.type = type;
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

    public byte getType() {
        return type;
    }

    public void saveMessage(String db_url, String db_login, String db_pass) throws SQLException {
        Connection connection = DriverManager.getConnection(db_url, db_login, db_pass);
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO messages (msg, to_user, from_user, type) VALUES (?,?,?,?)"
        );
        preparedStatement.setString(1, this.msg);
        preparedStatement.setInt(2, this.to_user);
        preparedStatement.setInt(3, this.from_user);
        preparedStatement.setByte(4, this.type);
        preparedStatement.executeUpdate();
        preparedStatement.close();
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
            byte type = resultSet.getByte("type");
            int to_user = 0;
            Message message;
            if(type == 1)
                message = new Message(name+": "+msg, fromUser, to_user, type);
            else
                message = new Message(msg, fromUser, to_user, type);
            messages.add(message);
        }
        statement.close();
        return messages;
    }

    public static ArrayList<Message> readPrivateMessages(String db_url, String db_login, String db_pass, int fromUser, int toUser) throws SQLException {
        Connection connection = DriverManager.getConnection(db_url, db_login, db_pass);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM `messages`, `users` WHERE users.id = messages.from_user AND (from_user='"+fromUser+"' AND to_user='"+toUser+"' OR from_user='"+toUser+"' AND to_user='"+fromUser+"')"
        );
        ArrayList<Message> messages = new ArrayList<>();
        while (resultSet.next()){
            String msg = resultSet.getString("msg");
            String name = resultSet.getString("name");
            fromUser = resultSet.getInt("from_user");
            int to_user = resultSet.getInt("to_user");
            byte type = resultSet.getByte("type");
            Message message = new Message(name+": "+msg, fromUser, to_user, type);
            messages.add(message);
        }
        statement.close();
        return messages;
    }}
