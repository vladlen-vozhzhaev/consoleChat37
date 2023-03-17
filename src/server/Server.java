package server;

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
                            user.getOut().writeUTF("Введите имя: ");
                            String name = user.getIn().readUTF();
                            user.setName(name);
                            user.getOut().writeUTF(user.getName()+" добро пожаловать на сервер!");
                            String clientMessage;
                            while (true){
                                clientMessage = user.getIn().readUTF();
                                System.out.println(clientMessage);
                                for (User user1 : users) {
                                    user1.getOut().writeUTF(user.getName()+": "+clientMessage);
                                }
                            }
                        }catch (IOException e){
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
}
