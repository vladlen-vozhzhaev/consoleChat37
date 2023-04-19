package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

public class User {
    private int id;
    private String name;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public User(Socket socket) throws IOException {
        this.socket = socket;
        in = new DataInputStream(this.socket.getInputStream());
        out = new DataOutputStream(this.socket.getOutputStream());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public DataInputStream getIn() {
        return in;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean reg(String db_url, String db_login, String db_pass) throws SQLException, IOException, ParseException {
        Connection connection = DriverManager.getConnection(db_url, db_login, db_pass);
        Statement statement = connection.createStatement();
        JSONObject jsonObject = new JSONObject();
        JSONParser jsonParser = new JSONParser();
        jsonObject.put("msg", "Введите имя: ");
        this.getOut().writeUTF(jsonObject.toJSONString());
        jsonObject = (JSONObject) jsonParser.parse(this.getIn().readUTF());
        String name = jsonObject.get("msg").toString();
        jsonObject.put("msg", "Введите email: ");
        this.getOut().writeUTF(jsonObject.toJSONString());
        jsonObject = (JSONObject) jsonParser.parse(this.getIn().readUTF());
        String login = jsonObject.get("msg").toString();
        jsonObject.put("msg", "Введите pass: ");
        this.getOut().writeUTF(jsonObject.toJSONString());
        jsonObject = (JSONObject) jsonParser.parse(this.getIn().readUTF());
        String pass = jsonObject.get("msg").toString();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '"+login+"'");
        if(resultSet.next()){
            jsonObject.put("msg", "Такой пользователь уже есть");
            this.getOut().writeUTF(jsonObject.toJSONString());
            return false;
        }else{
            statement.executeUpdate("INSERT INTO `users` (`name`, `login`, `pass`) " +
                    "VALUES ('"+name+"', '"+login+"', '"+pass+"')");
            statement.close();
            return true;
        }
    }
    public boolean login(String db_url, String db_login, String db_pass) throws SQLException, IOException, ParseException{
        Connection connection = DriverManager.getConnection(db_url, db_login, db_pass);
        Statement statement = connection.createStatement();
        JSONObject jsonObject = new JSONObject();
        JSONParser jsonParser = new JSONParser();
        jsonObject.put("msg", "Введите логин: ");
        this.getOut().writeUTF(jsonObject.toJSONString());
        jsonObject = (JSONObject) jsonParser.parse(this.getIn().readUTF());
        String login = jsonObject.get("msg").toString();
        jsonObject.put("msg", "Введите пароль: ");
        this.getOut().writeUTF(jsonObject.toJSONString());
        jsonObject = (JSONObject) jsonParser.parse(this.getIn().readUTF());
        String pass = jsonObject.get("msg").toString();
        ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM users WHERE login='"+login+"' AND pass='"+pass+"'"
        ); //     ivan@mail.ru'/*
        if(resultSet.next()){
            String name = resultSet.getString("name");
            int id = resultSet.getInt("id");
            this.setName(name);
            this.setId(id);
            return true;
        }else{
            jsonObject.put("msg", "Неверный логин или пароль");
            this.getOut().writeUTF(jsonObject.toJSONString());
            return false;
        }

    }

}
