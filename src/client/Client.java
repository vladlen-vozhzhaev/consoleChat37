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
                //    /m Ivan Hello Ivan! How are you!
                message = scanner.nextLine();
                if(message.indexOf("/m") == 0){
                    String[] words = message.split(" ");
                    int skipStr = 3+words[1].length(); // "/m " - 3 символа
                    String msg = message.substring(skipStr); // отрезаем сообщение
                    jsonObject.put("public", false);
                    jsonObject.put("id", words[1]);
                    jsonObject.put("msg", msg);
                }else {
                    jsonObject.put("public", true);
                    jsonObject.put("msg", message);
                }
                out.writeUTF(jsonObject.toJSONString());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
