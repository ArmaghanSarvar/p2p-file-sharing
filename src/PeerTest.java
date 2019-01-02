import java.net.*;
import java.io.*;
import java.util.Scanner;

public class PeerTest {

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        String command;
        String id = scanner.nextLine();
        try {
            //InetAddress group = InetAddress.getByName("255.255.255.255");
            //int port = 8888;

            InetAddress group = InetAddress.getByName("230.0.0.0");
            int port = 4446;
            command = scanner.nextLine();
            if (command.split(" ")[1].equals("-receive")) {
                //int port = 8888;
                new Receiver(group, port,id);
            }
            if (command.split(" ")[1].equals("-serve")) {
                //int port = scanner.nextInt();
                new Sender(port, command.split(" ")[5],id);
            }
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }
}