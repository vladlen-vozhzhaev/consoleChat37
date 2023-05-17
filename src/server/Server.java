package server;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

public class Server {
    static String db_url = "jdbc:mysql://127.0.0.1:3306/chat37";
    static String db_login = "root";
    static String db_pass = "";
    static Connection connection;
    public static void main(String[] args) {
        ArrayList<User> users = new ArrayList<>();
        // 0.0.0.0 - 255.255.255.255 NAT
        try {
            ServerSocket serverSocket = new ServerSocket(9123);
            System.out.println("Сервер запущен");
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                User user = new User(socket);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            JSONParser jsonParser = new JSONParser();
                            while (true){
                                jsonObject.put("msg", "Для регистрации /reg, \n" +
                                                        "для авторизации /login");
                                jsonObject.put("type", 1);
                                user.getOut().writeUTF(jsonObject.toJSONString());
                                jsonObject = (JSONObject) jsonParser.parse(user.getIn().readUTF());
                                String command = jsonObject.get("msg").toString();
                                if(command.equals("/reg")){
                                    if(user.reg(db_url, db_login, db_pass)) break;
                                }else if(command.equals("/login")){
                                    if(user.login(db_url, db_login, db_pass)) break;
                                }
                            }
                            users.add(user);
                            sendUserList(users);
                            jsonObject.put("msg", user.getName()+" добро пожаловать на сервер!");
                            jsonObject.put("self_id", user.getId());
                            jsonObject.put("type", 1);
                            user.getOut().writeUTF(jsonObject.toJSONString());
                            ArrayList<Message> messages = Message.readPublicMessages(db_url, db_login, db_pass);


                            for (Message message : messages) {
                                jsonObject.put("msg", message.getMsg());
                                jsonObject.put("type", message.getType());
                                if(message.getType() == 1)
                                    user.getOut().writeUTF(jsonObject.toJSONString());
                                else if(message.getType() == 2){
                                    File file = new File("files/"+message.getMsg());
                                    long fileSize = file.length();
                                    jsonObject.put("fileSize", fileSize);
                                    user.getOut().writeUTF(jsonObject.toJSONString());
                                    FileInputStream fis = new FileInputStream(file);
                                    BufferedInputStream bis = new BufferedInputStream(fis);
                                    byte[] buffer = new byte[1024];
                                    int i;
                                    while ((i = bis.read(buffer)) != -1){
                                        user.getOut().write(buffer);
                                    }
                                    user.getOut().flush();
                                }
                            }

                            String clientMessage;
                            while (true){
                                jsonObject = (JSONObject) jsonParser.parse(user.getIn().readUTF());
                                String action = "";
                                if(jsonObject.get("action") != null)  action = (String) jsonObject.get("action");
                                if(action.equals("getMessages")){
                                    int fromUser = user.getId();
                                    int toUser = Integer.parseInt(jsonObject.get("toUser").toString());
                                    messages = Message.readPrivateMessages(db_url, db_login, db_pass, fromUser, toUser);
                                    for (Message message : messages) {
                                        jsonObject.put("msg", message.getMsg());
                                        user.getOut().writeUTF(jsonObject.toJSONString());
                                    }
                                } else if (action.equals("sendFile")) {
                                    // Принимаем файл с клиента!
                                    String fileName = jsonObject.get("fileName").toString();
                                    long fileSize = Long.parseLong(jsonObject.get("fileSize").toString());
                                    FileOutputStream fos = new FileOutputStream("files/"+fileName, true);
                                    System.out.println("Принимаем файл "+fileName);
                                    byte[] buffer = new byte[1024];
                                    while (true){
                                        user.getIn().read(buffer);
                                        for (byte b : buffer){
                                            fos.write(b);
                                        }
                                        fileSize -= 1024;
                                        if(fileSize <= 0) break;
                                    }
                                    Message message = new Message(fileName, user.getId(), 0, (byte) 2);
                                    message.saveMessage(db_url, db_login, db_pass);
                                } else{
                                    clientMessage = jsonObject.get("msg").toString();
                                    System.out.println(clientMessage);
                                    if((boolean) jsonObject.get("public")) {
                                        Message message = new Message(clientMessage, user.getId(), 0, (byte) 1);
                                        message.saveMessage(db_url, db_login, db_pass);
                                        for (User user1 : users) {
                                            if (user.getName().equals(user1.getName())) continue;
                                            jsonObject.remove("msg");
                                            jsonObject.put("msg", user.getName() + ": " + clientMessage);
                                            user1.getOut().writeUTF(jsonObject.toJSONString());
                                        }
                                    }else{
                                        // Получаем имя получателя
                                        int toUser = (Integer.parseInt(jsonObject.get("id").toString()));
                                        Message message = new Message(clientMessage, user.getId(), toUser, (byte) 1);
                                        message.saveMessage(db_url, db_login, db_pass);
                                        for (User user1 : users){ // Перебираем всех, чтобы найти нужного
                                            if(user1.getId() == toUser){
                                                jsonObject.put("msg", user.getName() + ": " + clientMessage);
                                                user1.getOut().writeUTF(jsonObject.toJSONString());
                                                break;
                                            }
                                        }
                                    }
                                }

                            }
                        }catch (IOException e){
                            System.out.println("Клиент отключился");
                            users.remove(user);
                            sendUserList(users);
                        }catch (SQLException | ParseException e){
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void sendUserList(ArrayList<User> users){
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        users.forEach(user -> {
            JSONObject jsonUserObject = new JSONObject();
            jsonUserObject.put("name", user.getName());
            jsonUserObject.put("id", user.getId());
            jsonArray.add(jsonUserObject);
        });
        jsonObject.put("onlineUsers", jsonArray);
        users.forEach(user -> {
            try {
                user.getOut().writeUTF(jsonObject.toJSONString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
