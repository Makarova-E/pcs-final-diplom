import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final int PORT = 8989;
    private static final String HOST = "127.0.0.1";

    public static void main(String[] args) {
        try (
                Socket clientSocket = new Socket(HOST, PORT);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()))) {

            out.println("бизнес, адаптации");
            // out.println("какая");

            StringBuilder sb = new StringBuilder();
            String line = in.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = in.readLine();
            }
            String result = sb.toString();
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
