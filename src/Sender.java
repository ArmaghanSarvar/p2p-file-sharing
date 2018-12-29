import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Vector;

public class Sender extends Thread {
    private MulticastSocket socket = null;
    private DatagramSocket ds = null;
    private int port;
    private InetAddress ip;
    private Scanner scanner = new Scanner(System.in);
    private int sizeOfPacket = 65000;
    private Vector<String> fileList = new Vector<>();
    private boolean isSender = false;

    public Sender(int port, String path) {
        this.port = port;
        fileList.add(path);
        while (true) {
            System.out.println("Type end in order to finish assigning files ");
            String t = scanner.nextLine();
            if (t.equals("end")) {
                break;
            }
            fileList.add(t.split(" ")[5]);
        }
        this.start();
    }

    public void run() {
        System.out.println("ready to share files");

        while (true) {
            try {
                socket = new MulticastSocket(port);
                socket.joinGroup(InetAddress.getByName("230.0.0.0"));
                ds = new DatagramSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] mybytearray;
            try {
                //receive the multicast messages
                byte[] buffer = new byte[sizeOfPacket];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                System.out.println("before receive");
                socket.receive(messageIn);
                System.out.println("after receive");
                int secondPort = messageIn.getPort();
                System.out.println("clientPort: " + secondPort);
                ip = messageIn.getAddress();
                System.out.println("ClientAddress: " + ip);
                String fileReq = new String(messageIn.getData(), 0, messageIn.getLength());
                //System.out.println(fileReq);
                if (fileReq.split(" ")[1].equals("-receive")) {
                    isSender = true;
                    //System.out.println("it's true");
                }
                if (isSender) {
                    String filename2 = fileReq.split(" ")[2];
                    String path = contains(filename2);
                    System.out.println(path);
                    if (path == null) {
                        System.out.println("I don't have the file");
                        continue;
                    }
                    File peerFile = new File(path);
                    mybytearray = new byte[(int) peerFile.length()];
                    FileInputStream fis = new FileInputStream(peerFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    DataInputStream dis = new DataInputStream(bis);
                    dis.readFully(mybytearray, 0, mybytearray.length);
                    System.out.println("size of byteArray " + mybytearray.length);
                    int fileSize = mybytearray.length;
                    String fileInfo = String.valueOf(fileSize) + " " + String.valueOf(sizeOfPacket);
                    DatagramPacket pkt = new DatagramPacket(fileInfo.getBytes(), fileInfo.getBytes().length, ip, secondPort);
                    ds.send(pkt);
                    new FileTransportHandler(ip, secondPort, mybytearray, sizeOfPacket);
                }
            } catch (SocketException e) {
                System.out.println("Socket:" + e.getMessage());
            } catch (EOFException e) {
                System.out.println("EOF:" + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
            }
            socket.close();
            ds.close();
        }
    }

    private String contains(String file) {
        System.out.println(file);
        for (int i = 0; i < fileList.size(); i++) {
            String[] arr = fileList.get(i).split("\\\\");
            String s = arr[arr.length - 1];
            if (file.equals(s)) {
                return fileList.get(i);
            }
        }
        return null;
    }
}