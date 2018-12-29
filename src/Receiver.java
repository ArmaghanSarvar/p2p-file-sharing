import java.io.*;
import java.net.*;
import java.util.*;

public class Receiver extends Thread {
    private DatagramSocket ds = null;
    InetAddress group;
    int port;
    private Scanner scanner;
    Vector<Byte> file;
    private int receivingFileSize;
    private int packetSize;
    private int numOfPackets;
    Vector<byte[]> vec;

    public Receiver(InetAddress group, int port) {
        file = new Vector<>();
        this.group = group;
        this.port = port;
        this.start();

    }

    public void run() {
        String filename = "";
        String finalMessage;
        scanner = new Scanner(System.in);
        while (true) {
            try {
                ds = new DatagramSocket();
                System.out.println("File Request:");
                filename = scanner.nextLine();
                finalMessage = new String("p2p -receive " + filename);
                byte[] f = finalMessage.getBytes();
                DatagramPacket messageOut = new DatagramPacket(f, f.length, group, port);
                ds.send(messageOut);
                try {
                    ds.setSoTimeout(3000);
                } catch (SocketException e) {
                    continue;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("the request has been sent");
            try {
                byte[] buf2 = new byte[65535];
                DatagramPacket message = new DatagramPacket(buf2, buf2.length);
                System.out.println("here before receiving packet");
                ds.receive(message);
                System.out.println("here after receiving packet");
                String fileInfo = new String(message.getData(), 0, message.getLength());
                System.out.println(fileInfo.split(" ")[0]);
                receivingFileSize = Integer.parseInt(fileInfo.split(" ")[0]);
                packetSize = Integer.parseInt(fileInfo.split(" ")[1]);
                numOfPackets = 1 + (receivingFileSize / (packetSize - 4));

                System.out.println("info is " + fileInfo);
                System.out.println(numOfPackets);
            } catch (SocketException ex) {
                System.out.println("Socket:" + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("IO:" + ex.getMessage());
            }
            vec = new Vector<>(numOfPackets);
            int counter = 0;

            while (counter < numOfPackets) {
                try {
                    byte[] receiveBuf = new byte[packetSize];
                    DatagramPacket receiveMessage = new DatagramPacket(receiveBuf, receiveBuf.length);
                    ds.receive(receiveMessage);
                    concat(receiveMessage.getData());
                } catch (SocketException ex) {
                    System.out.println("Socket:" + ex.getMessage());
                } catch (IOException ex) {
                    System.out.println("IO:" + ex.getMessage());
                }
                counter++;
            }
            saveFile(filename, receivingFileSize);
            ds.close();
        }
    }

    private void saveFile(String fileName, int fileSize) {
        byte[] fileContents = new byte[fileSize];
        int i = 0;
        for (int j = 0; j < vec.size(); j++) {
            for (int k = 0; k < vec.get(j).length; k++) {
                fileContents[i] = vec.get(j)[k];
                i++;
                if (i == fileSize)
                    break;
            }
        }
        File file = new File(fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileContents);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void concat(byte[] fileSegment) {
//        for (int i = 0; i < fileSegment.length; i++) {
//            System.out.println(fileSegment[i]);
//        }

        byte[] res = new byte[fileSegment.length - 4];
        for (int i = 4; i < fileSegment.length; i++) {
            res[i - 4] = fileSegment[i];
        }
        byte[] offsetBytes = new byte[]{fileSegment[0], fileSegment[1], fileSegment[2], fileSegment[3]};
        vec.add(byteArrayToInt(offsetBytes), res);
    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
}