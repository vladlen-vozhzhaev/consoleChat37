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
        // 0.0.0.0 - 255.255.255.255 NAT
        try {
            ServerSocket serverSocket = new ServerSocket(9123);
            System.out.println("Сервер запущен");
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                sockets.add(socket);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataInputStream in = new DataInputStream(socket.getInputStream());
                            String clientMessage;
                            while (true){
                                clientMessage = in.readUTF();
                                System.out.println(clientMessage);
                                for (Socket socket : sockets) {
                                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                                    out.writeUTF(clientMessage.toUpperCase());
                                }
                            }
                        }catch (IOException e){
                            System.out.println("Клиент отключился");
                            sockets.remove(socket);
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
