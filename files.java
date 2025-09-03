import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

class files {
    static Socket clientSocket = null;
    static ServerSocket ss = null;

    public class MyRunnable implements Runnable {
        @Override
        public void run() {

        }
    }

    public static void receiveFiles() throws IOException {
        try {
            // FileInputStream fis = new FileInputStream("file.txt");
            FileOutputStream fos = new FileOutputStream("file2.txt");

            ss = new ServerSocket(8080);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");
            ExecutorService pool = Executors.newFixedThreadPool(3);
            while (true) {
                // wait for multiple clients
                clientSocket = ss.accept();
                pool.execute(() -> {
                    try {
                        System.out.println("connected: " + clientSocket.getLocalAddress());
                        InputStream in = clientSocket.getInputStream(); // raw input
                        // Buffer to hold incoming bytes
                        byte[] buffer = new byte[1024 * 64];
                        int bytesRead;

                        // write the data that are loaded into the buffer
                        while ((bytesRead = in.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException ex) {
        }
    }

    public static class MyThread extends Thread {
        public static int num = 9;

        @Override
        public void run() {

            System.out.println("From another thread");
        }

    }

    public static void main(String[] args) throws IOException {

        receiveFiles();
    }

}