package sec.G31;


public class UDPclient extends UDPchannel
{
    private DatagramSocket _socket;
    private byte[] buf;
    private InetAddress _address;
    private int _port;

    public UDPClient() {
        _socket = new DatagramSocket();
    }

    public String sendMsg(String msg, InetAddress addressToSend, int portToSend) {
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, addressToSend, portToSend);
        _socket.send(packet);

        packet = new DatagramPacket(buf, buf.length);
        _socket.receive(packet);

        String received = new String(packet.getData(), 0, packet.getLength());
        return received;
    }

    public void close() {
        _socket.close();
    }

}