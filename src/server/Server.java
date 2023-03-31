package server;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        ArrayList<Socket> sockets = new ArrayList<>();
        ArrayList<User> users = new ArrayList<>();
        // 0.0.0.0 - 255.255.255.255 NAT
        try {
            ServerSocket serverSocket = new ServerSocket(9123);
            System.out.println("Сервер запущен");
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                User user = new User(socket);
                users.add(user);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("msg", "Введите имя: ");
                            user.getOut().writeUTF(jsonObject.toJSONString());
                            JSONParser jsonParser = new JSONParser();
                            jsonObject = (JSONObject) jsonParser.parse(user.getIn().readUTF());
                            String name = jsonObject.get("msg").toString();
                            boolean uniqueName = false;
                            while (!uniqueName){ // до тех пор пока имя не уникальное
                                uniqueName = true; // наверное имя уникально
                                for (User user1 : users) { // но мы проверим
                                    if(name.equals(user1.getName())){ // если нашли такое же имя, то
                                        user.getOut().writeUTF("Имя занято, выберите другое");
                                        jsonObject = (JSONObject) jsonParser.parse(user.getIn().readUTF());
                                        name = jsonObject.get("msg").toString();
                                        uniqueName = false; // имя было не уникально, нужно проверить ещё раз
                                        break;
                                    }
                                }
                            }

                            user.setName(name);
                            user.getOut().writeUTF(user.getName()+" добро пожаловать на сервер!");
                            String clientMessage;
                            while (true){
                                jsonObject = (JSONObject) jsonParser.parse(user.getIn().readUTF());
                                clientMessage = jsonObject.get("msg").toString();
                                System.out.println(clientMessage);
                                if((boolean) jsonObject.get("public"))
                                    for (User user1 : users) {
                                        if (name.equals(user1.getName())) continue;
                                        jsonObject.remove("msg");
                                        jsonObject.put("msg", user.getName()+": "+clientMessage);
                                        user1.getOut().writeUTF(jsonObject.toJSONString());
                                    }
                                else{
                                    // Получаем имя получателя
                                    String toName = jsonObject.get("name").toString();
                                    for (User user1 : users){ // Перебираем всех, чтобы найти нужного
                                        if(user1.getName().equals(toName)){
                                            user1.getOut().writeUTF(user.getName()+": "+clientMessage);
                                            break;
                                        }
                                    }
                                }
                            }
                        }catch (Exception e){
                            System.out.println("Клиент отключился");
                            users.remove(user);
                        }
                    }
                });
                thread.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void sendUserList(ArrayList<User> users){
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        users.forEach(user -> {
            String username = user.getName();
            jsonArray.add(username);
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
