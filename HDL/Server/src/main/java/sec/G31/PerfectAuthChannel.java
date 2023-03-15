package sec.G31;

import sec.G31.messages.DecidedMessage;
import sec.G31.messages.Message;
//import java.util.logging.Logger;
import javax.crypto.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;

/**
 * TO-DO: implementar a autenticacao
 */
public class PerfectAuthChannel {
    //private final static Logger LOGGER = Logger.getLogger(PerfectAuthChannel.class.getName());
    private StubbornChannel _stubChannel;
    private Server _server;
    private InetAddress _address;
    private int _port;
    private BroadcastManager _broadcastManager;
    private Hashtable<Integer, Integer> _broadcastNeighbors; // to send broadcast
    private final String _keyPath = "../keys/";
    private final String CIPHER_ALGO = "RSA/ECB/PKCS1Padding";
    private final String DIGEST_ALGO = "SHA-256";

    public PerfectAuthChannel(BroadcastManager broadcastManager, Server server, InetAddress serverAddress,
            int serverPort,
            Hashtable<Integer, Integer> broadcastNeighbours) {
        _server = server;
        _address = serverAddress;
        _port = serverPort;
        _stubChannel = new StubbornChannel(this, _address, _port);
        _broadcastManager = broadcastManager;
        _broadcastNeighbors = broadcastNeighbours;
    }

    public void sendMessage(InetAddress destAddress, int destPort, Message msg) {
        try {
            String serverKeyPath = _keyPath + _server.getId() + "/private_key.der";
            PrivateKey key = readPrivateKey(serverKeyPath);

            // plain text
            String plainText = msg.stringForDigest();
            byte[] plainBytes = plainText.getBytes();

            // digest data
            MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
            messageDigest.update(plainBytes);
            byte[] digestBytes = messageDigest.digest();

            // cipher data
            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherDigestBytes = cipher.doFinal(digestBytes); // ciphering the digest bytes

            String cipherB64dString = Base64.getEncoder().encodeToString(cipherDigestBytes);

            msg.setCipheredDigest(cipherB64dString); // setting the field in the Message format

            // LOGGER.info("PAC:: " + destAddress + " " + destPort + " " + msg);
            _stubChannel.sendMessage(destAddress, destPort, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDecide(InetAddress destAddress, int destPort, DecidedMessage msg){
        try {
            String serverKeyPath = _keyPath + _server.getId() + "/private_key.der";
            PrivateKey key = readPrivateKey(serverKeyPath);

            // plain text
            String plainText = msg.stringForDigest();
            byte[] plainBytes = plainText.getBytes();

            // digest data
            MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
            messageDigest.update(plainBytes);
            byte[] digestBytes = messageDigest.digest();

            // cipher data
            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherDigestBytes = cipher.doFinal(digestBytes); // ciphering the digest bytes

            String cipherB64dString = Base64.getEncoder().encodeToString(cipherDigestBytes);

            msg.setCipheredDigest(cipherB64dString); // setting the field in the Message format

            // LOGGER.info("PAC:: " + destAddress + " " + destPort + " " + msg);
            _stubChannel.sendDecide(destAddress, destPort, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * verifies that it has come proper authenticated and from the correct port
     */
    public void receivedMessage(Message msg, int port, InetAddress address) {
        // LOGGER.info("PAC:: received message");
        try {
            // verify that it has came from the correct port and with proper authentication
            if (!msg.getType().equals("START")){
                if (_broadcastNeighbors.get(msg.getSenderId()) == port && verifyMessage(msg)){
                    //System.out.println("PAC:: verified message from " + msg.getSenderId() + " " + msg);
                    _broadcastManager.receivedMessage(msg); // inform the upper layer
                } else {
                    //System.out.println("PAC:: message from " + msg.getSenderId() + " " + msg + " was not verified");
                }
            } else { // if message comes from client we don't have to verify the port
                if (verifyMessage(msg)) {
                    //System.out.println("PAC:: verified message from " + msg.getSenderId() + " " + msg);
                    _broadcastManager.receivedMessage(msg); // inform the upper layer
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBroadcastManager(BroadcastManager manager) {
        _broadcastManager = manager;
    }

    private byte[] readFile(String path) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(path);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        return content;
    }

    public PrivateKey readPrivateKey(String privateKeyPath) throws Exception {
        byte[] privEncoded = readFile(privateKeyPath);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");
        PrivateKey priv = keyFacPriv.generatePrivate(privSpec);
        return priv;
    }

    public PublicKey readPublicKey(String publicKeyPath) throws Exception {
        byte[] pubEncoded = readFile(publicKeyPath);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
        PublicKey pub = keyFacPub.generatePublic(pubSpec);
        return pub;
    }

    /**
     * Given a message, verify its signature
     * 
     * @param msg
     * @return
     * @throws Exception
     */
    public Boolean verifyMessage(Message msg) throws Exception {
        String serverKeyPath;
        if (msg.getType().equals("START")){ // if it is a start message, the key is in the clients folder
            serverKeyPath = _keyPath + "clients/" + msg.getSenderId() + "/public_key.der";
        } else {
            serverKeyPath = _keyPath + msg.getSenderId() + "/public_key.der";
        }
        //String serverKeyPath = _keyPath + msg.getSenderId() + "/public_key.der";
        PublicKey key = readPublicKey(serverKeyPath);

        // decode from B64
        byte[] cipheredDigestBytes = Base64.getDecoder().decode(msg.getCipheredDigest());

        // uncipher
        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] uncipheredDigestBytes = cipher.doFinal(cipheredDigestBytes);

        // digest the other part of message and compare to unciphered digest
        // plain text
        String plainText = msg.stringForDigest();
        byte[] plainBytes = plainText.getBytes();

        // digest data
        MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
        messageDigest.update(plainBytes);
        byte[] digestBytes = messageDigest.digest();

        return Arrays.equals(digestBytes, uncipheredDigestBytes);
    }
}