package client;

import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 9123);
            Scanner scanner = new Scanner(System.in);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String message;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true)
                            System.out.println(in.readUTF());
                    }catch (IOException e){
                        System.out.println("Потеряно соединение с сервером");
                    }
                }
            });
            thread.start();
            // {msg: "hello"}
            JSONObject jsonObject = new JSONObject();
            while (true){
                message = scanner.nextLine();
                jsonObject.put("msg", message);
                out.writeUTF(jsonObject.toJSONString());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
