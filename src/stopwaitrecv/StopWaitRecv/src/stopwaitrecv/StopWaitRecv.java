/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stopwaitrecv;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tom
 */
public class StopWaitRecv {

    /**
     * @param args the command line arguments
     * @throws java.net.SocketException
     */
    public static void main(String[] args) throws SocketException, IOException {
        byte[] bytes = new byte[15];

        DatagramPacket pack = new DatagramPacket(bytes, bytes.length);
        DatagramSocket sock = new DatagramSocket(8888);

        System.out.println("Waiting for data....");

        String temp = "";
        String outPut = "";
        
        int lastSeq = 0;

        while (!sock.isClosed()) {

            try {
                sock.receive(pack);
                bytes = pack.getData();
                lastSeq = bytes[0];

                if (sock.getSoTimeout() <= 0) {
                    sock.setSoTimeout(250);
                }
            } catch (IOException ex) {
                
                sendReply((byte) lastSeq);
                sock.receive(pack);
                bytes = pack.getData();
                lastSeq = bytes[0];
            }

            
            if (stopPacket(bytes)) {
                sock.close();
            } else {
                bytes = cleanBytes(bytes);
                temp = new String(bytes);
                outPut = outPut + temp;

                sendReply(bytes[0]);

            }

        }
//        TODO: replace print method with one that will write to a destination file
        System.out.println(outPut);

    }

    private static void sendReply(byte recvSeqNum) throws SocketException, UnknownHostException, IOException {
        byte[] bytes = new byte[15];
        DatagramPacket pack = new DatagramPacket(bytes, bytes.length, InetAddress.getByName("localhost"), 7777);
        DatagramSocket sock = new DatagramSocket();

        byte sendAck;

        if (recvSeqNum == 0) {
            sendAck = 0x00;
        } else {
            sendAck = 0x01;
        }
        Frame frame = new Frame();
        frame.setSeq(recvSeqNum);
        frame.setAck(sendAck);

        int count = 0;
        for (byte b : frame.getBytes()) {
            bytes[count] = b;
            count++;
        }
        pack.setData(bytes);
        sock.send(pack);
    }

    private static boolean stopPacket(byte[] bytes) {
        boolean stop = true;
        for (int i = 2; i < 10; i++) {
            if (bytes[i] != 127) {
                stop = false;
            }
        }
        return stop;
    }

    private static byte[] cleanBytes(byte[] bytes) {
        byte[] newBytes = new byte[8];

        int j = 0;
        for (int i = 2; i < 10; i++) {
            if (bytes[i] > 31 || bytes[i] < 128) {
                newBytes[j] = bytes[i];
                j++;
            }
        }
        return newBytes;
    }

}
