import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FileTransportHandler extends Thread {
    DatagramSocket ds = null;
    private InetAddress ip;
    private int port;
    private int packetSize;
    byte[] fileContents;
    int numOfPackets;

    public FileTransportHandler(InetAddress address, int port, byte[] fileContents , int packetSize) {
        this.ip = address;
        this.packetSize = packetSize;
        this.port = port;
        numOfPackets = 1 + (fileContents.length/(packetSize - 4));
        System.out.printf("num of packets %d\n", numOfPackets);
        this.fileContents = fileContents;
        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.start();
    }

    @Override
    public void run() {
        System.out.println("here :/");
        for (int i = 0; i < numOfPackets; i++) {
            int startPoint = i * (packetSize - 4);
            int finishPoint = startPoint + (packetSize - 4) - 1;
            byte[] pkt = new byte[packetSize];
            pkt = setOffset(pkt , i);
            byte[] pktdata = fragment(fileContents, startPoint, finishPoint);
            for (int j = 4; j < packetSize; j++) {
                pkt[j] = pktdata[j-4];
            }
            DatagramPacket dpkt = new DatagramPacket(pkt, pkt.length, ip, port);
            try {
                System.out.println("sending in handler");
                ds.send(dpkt);
                System.out.println("sent the packet");
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] setOffset(byte[] pkt , int offset){
        byte[] offsetbyte = intToByteArray(offset);
        for (int i = 0; i < offsetbyte.length; i++) {
            pkt[i] = offsetbyte[i];
        }
        return pkt;
    }

    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public static byte[] fragment(byte[] source, int startPoint, int finishPoint) {
        int threshold = source.length;
        int resLength = finishPoint - startPoint + 1;
        byte[] res = new byte[resLength];


        for (int i = startPoint; i <= finishPoint; i++) {
            if (i < threshold) {
                res[i - startPoint] = source[i];
            }
        }
        return res;
    }
}
