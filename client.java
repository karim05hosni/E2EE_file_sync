import java.io.*;
import java.net.*;

class client {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 8080;

        try (Socket socket = new Socket(hostname, port))  {
            
            System.out.println("Connected to server.");

            OutputStream out = socket.getOutputStream(); // raw output

            FileInputStream fis = new FileInputStream("../../Assassin's Creed IV Black Flag - [DODI Repack]/file.TXT");
            byte[] buffer = new byte[64 * 1024]; // 64 KB
            int bytesRead;
            while ((bytesRead = fis.read(buffer) ) !=-1){
                out.write(buffer, 0, bytesRead);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
