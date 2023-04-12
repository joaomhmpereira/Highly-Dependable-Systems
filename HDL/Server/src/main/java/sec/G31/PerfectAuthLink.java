package sec.G31;

import sec.G31.messages.DecidedMessage;
import sec.G31.messages.Message;
import sec.G31.utils.TransactionBlock;

//import java.util.logging.Logger;
import javax.crypto.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;


public class PerfectAuthLink {
    //private final static Logger LOGGER = Logger.getLogger(PerfectAuthChannel.class.getName());
    private StubbornLink _stubChannel;
    private Server _server;
    private InetAddress _address;
    private int _port;
    private BroadcastManager _broadcastManager;
    private Hashtable<Integer, Integer> _broadcastNeighbors; // to send broadcast
    private final String _keyPath = "../keys/";
    private final String CIPHER_ALGO = "RSA/ECB/PKCS1PADDING";
    private final String DIGEST_ALGO = "SHA-256";

    public PerfectAuthLink(BroadcastManager broadcastManager, Server server, InetAddress serverAddress,
            int serverPort,
            Hashtable<Integer, Integer> broadcastNeighbours) {
        _server = server;
        _address = serverAddress;
        _port = serverPort;
        _stubChannel = new StubbornLink(this, _address, _port);
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

            //if (msg.isBlockSet() && msg.getBlock().getType().equals("SNAPSHOT")){
            //    System.out.println("PAC SENDING:: expectedValue: " + plainText);
            //}

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
        System.out.println("PAC:: received message from " + msg.getSenderId() + " " + msg);
        try {
            // verify that it has came from the correct port and with proper authentication
            if (!msg.getType().equals("START") && !msg.getType().equals("W_BALANCE") && !msg.getType().equals("S_BALANCE") 
                && !msg.getType().equals("TRANSACTION") && !msg.getType().equals("CREATE")) {
                if (_broadcastNeighbors.get(msg.getSenderId()) == port && verifyMessage(msg)){
                    System.out.println("PAC:: verified message from " + msg.getSenderId() + " " + msg);
                    _broadcastManager.receivedMessage(msg, port); // inform the upper layer
                } else {
                    System.out.println("PAC:: message not verified from " + msg.getSenderId() + " " + msg);
                }
            } else { // if message comes from client we don't have to verify the port
                if (verifyMessage(msg)) {
                    System.out.println("PAC:: verified message from " + msg.getSenderId() + " " + msg);
                    _broadcastManager.receivedMessage(msg, port); // inform the upper layer
                } else {
                    System.out.println("PAC:: message not verified from " + msg.getSenderId() + " " + msg);
                }
            }
        } catch (BadPaddingException e1) { // decryption failed, keys don't match
            if (msg.getType().equals("CREATE") || msg.getType().equals("S_BALANCE")
                || msg.getType().equals("W_BALANCE") || msg.getType().equals("TRANSACTION")) {
                DecidedMessage decidedMessage = new DecidedMessage(msg.getType(), "Error: You don't have permission to perform this operation.", _server.getId(), _server.getIBFT().getNonceCounter());
                _broadcastManager.sendDecide(decidedMessage, port);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
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

    public String signSnapshotBlock(TransactionBlock block) {
        try {
            String serverKeyPath = _keyPath + _server.getId() + "/private_key.der";
            PrivateKey key = readPrivateKey(serverKeyPath);

            String accountsStrings = block.getAccounts().toString();
            byte[] plainBytes = accountsStrings.getBytes();
            
            // digest data
            MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
            messageDigest.update(plainBytes);
            byte[] digestBytes = messageDigest.digest();

            // cipher data
            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherDigestBytes = cipher.doFinal(digestBytes); // ciphering the digest bytes

            String cipherB64dString = Base64.getEncoder().encodeToString(cipherDigestBytes);
            return cipherB64dString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Given a message, verify its signature
     * 
     * @param msg
     * @return
     * @throws Exception
     */
    public Boolean verifyMessage(Message msg) throws Exception {
        PublicKey key;
        if (msg.getType().equals("CREATE") || msg.getType().equals("S_BALANCE") || msg.getType().equals("W_BALANCE")) { // if it's a START or BALANCE message, the key is the public key of the client
            key = msg.getPublicKey();
        } else if (msg.getType().equals("TRANSACTION")) { // if it's a TRANSACTION message, the key is the source public key of the transaction
            key = msg.getValue().getSource();
        } else { // else, the key is the public key of the server
            String serverKeyPath = _keyPath + msg.getSenderId() + "/public_key.der";
            key = readPublicKey(serverKeyPath);
        }
        //String serverKeyPath = _keyPath + msg.getSenderId() + "/public_key.der";
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

        //if (msg.isBlockSet() && msg.getBlock().getType().equals("SNAPSHOT")){
        //    System.out.println("PAC:: expectedValue: " + plainText);
        //    System.out.println("PAC:: digestBytes: " + Arrays.toString(digestBytes));
        //    System.out.println("PAC:: uncipheredDigestBytes: " + Arrays.toString(uncipheredDigestBytes));
        //}
        
        return Arrays.equals(digestBytes, uncipheredDigestBytes);
    }

    public Boolean verifySignature(String signatureToVerify, String expectedValue, int serverId){
        try {
            String serverKeyPath = _keyPath + serverId + "/public_key.der";
            PublicKey key = readPublicKey(serverKeyPath);

            byte[] cipheredDigestBytes = Base64.getDecoder().decode(signatureToVerify);


            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] uncipheredDigestBytes = cipher.doFinal(cipheredDigestBytes);

            byte[] plainBytes = expectedValue.getBytes();

            // digest data
            MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
            messageDigest.update(plainBytes);
            byte[] digestBytes = messageDigest.digest();

            //System.out.println("PAC:: expectedValue: " + expectedValue);
            //System.out.println("PAC:: digestBytes: " + Arrays.toString(digestBytes));
            //System.out.println("PAC:: uncipheredDigestBytes: " + Arrays.toString(uncipheredDigestBytes));

            return Arrays.equals(digestBytes, uncipheredDigestBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}