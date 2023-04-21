package sec.G31;

import java.util.logging.Logger;
import java.util.Hashtable;
import java.util.List;

import sec.G31.messages.DecidedMessage;
import sec.G31.messages.Message;
import sec.G31.messages.TransactionMessage;
import sec.G31.utils.TransactionBlock;

import java.security.PublicKey;
import java.util.ArrayList;

/**
 * IBFT will be responsible for the start and control over the IBFT
 * the server will only call the IBFT when sending a new message
 */
public class IBFT {
    // for logging
    private final static Logger LOGGER = Logger.getLogger(IBFT.class.getName());

    private Server _server;
    private int _leader; // algorithm 3 and 4 will change this
    private float _fee = 0.05f;
    private Account _leaderAccount;

    // algorithm variables
    private BroadcastManager _broadcast; // broadcast
    private int _instance;
    private int _currentRound;
    private int _preparedRound;
    private TransactionBlock _preparedValue;
    private TransactionBlock _inputValue;
    // private int _clientPort;
    // private boolean _sentPrepare;
    private boolean _sentCommit;
    private boolean _decided;
    // private int _numCommitsReceived; //
    private int _F; // faulty nodes
    private int _nonceCounter;
    private List<Message> _receivedMessages = new ArrayList<>(); // stores the received messages
    private Hashtable<TransactionBlock, ArrayList<Integer>> _prepareQuorum; // <value, list of ports that sent us
                                                                            // prepare>
    private Hashtable<TransactionBlock, ArrayList<Integer>> _commitQuorum; // <value, list of ports that sent us commit>
    private final String PREPARE_MSG = "PREPARE";
    private final String PRE_PREPARE_MSG = "PRE-PREPARE";
    private final String COMMIT_MSG = "COMMIT";

    // for transactions and blocks
    private TransactionBlock _currentTransactionBlock;
    //private Hashtable<PublicKey, Account> _accounts;
    private List<Account> _accounts;
    private List<TransactionMessage> _pendingTransactions;
    
    private String _ourSignature;
    private Hashtable<Integer, String> _consesnsusSignatures;
    // for the weak reads, every 3 blocks commited we start a consensus for status
    private int _blocksCommited; 

    /**
     * we only responde to messages of the same instance, we discard every message
     * that is from an instance from the future. Nao ha problema porque gracas ao
     * stubborn channel esssa mensagem eventualmente volta a vir para nos
     */

    /**
     * The constructor
     * for now the servers will know who is leader
     */
    public IBFT(Server server, int faultyNodes, PublicKey leaderPubKey) {
        _server = server;
        _instance = 0;
        _currentRound = 0;
        _preparedRound = 0;
        // _preparedValue = "";
        _prepareQuorum = new Hashtable<TransactionBlock, ArrayList<Integer>>();
        _commitQuorum = new Hashtable<TransactionBlock, ArrayList<Integer>>();
        _broadcast = new BroadcastManager(this, server, server.getBroadcastNeighbours());
        _F = faultyNodes;
        // _sentPrepare = false;
        _decided = false;
        _sentCommit = false;
        _leader = 1; // just for this implementation
        _currentTransactionBlock = new TransactionBlock("TRANSACTIONS");
        //_accounts = new Hashtable<PublicKey, Account>();
        _accounts = new ArrayList<Account>();
        _nonceCounter = 0;
        _pendingTransactions = new ArrayList<TransactionMessage>();
        createLeaderAccount(leaderPubKey);
        _consesnsusSignatures = new Hashtable<Integer, String>();
        _blocksCommited = 0;
    }

    private void createLeaderAccount(PublicKey leaderPubKey) {
        _leaderAccount = new Account(leaderPubKey, 150.0f);
        _accounts.add(_leaderAccount);
        System.out.println("[Server " + _server.getId() + "] created Leader Account");
    }

    /**
     * ===================================================================
     * SERVER OPERATIONS
     * 
     * these operations are the one that the client may call from the API
     * ===================================================================
     */

    /**
     * Create a new account (if it doesn't already exist) and add it to the list of
     * accounts
     * 
     * @param publicKey
     */
    public void createAccount(PublicKey publicKey, int clientPort) {
        if (!accountExists(publicKey)) {
            // initial balance fixed value > 0
            Account newAccount = new Account(publicKey, 150.0f);
            //_accounts.put(publicKey, newAccount);
            _accounts.add(newAccount);
            System.out.println("[SERVER " + _server.getId() + "]: Account created successfully. Client: " + clientPort);
            DecidedMessage decidedMessage = new DecidedMessage("CREATE", "Success", _server.getId(), _nonceCounter);
            _broadcast.sendDecide(decidedMessage, clientPort);
            _nonceCounter++;
        } else {
            System.out.println("Account already exists");
            DecidedMessage decidedMessage = new DecidedMessage("CREATE", "Error: Account already exists.",
                    _server.getId(), _nonceCounter);
            _broadcast.sendDecide(decidedMessage, clientPort);
            _nonceCounter++;
        }
    }

    public void weakCheckBalance(PublicKey publicKey, int clientPort){
        // there isn't one account with that public key no point on doing week read 
        if(!accountExists(publicKey)){
            System.out.println("You don't have an account yet");
            DecidedMessage decidedMessage = new DecidedMessage("W_BALANCE", "Error: Account not created.",
                    _server.getId(), _nonceCounter);
            _broadcast.sendDecide(decidedMessage, clientPort);
            _nonceCounter++;
        } else if (_server.getLastSnapshotBlock() != null) {
            //Account account = _accounts.get(publicKey);
            TransactionBlock snapshotBlock = _server.getLastSnapshotBlock();

            Account account = null;
            List<Account> accounts = snapshotBlock.getAccounts();
            for (Account a : accounts) {
                if (a.getPublicKey().equals(publicKey)) {
                    account = a;
                    break;
                }
            }

            System.out.println(
                    "[SERVER " + _server.getId() + "]: Balance checked successfully. Client: " + clientPort);
            DecidedMessage decidedMessage = new DecidedMessage("W_BALANCE", account.getBalance(), _server.getId(),
                    _nonceCounter, _blocksCommited);
            // add consensus signatures
            Hashtable<Integer, String> signatures = snapshotBlock.getSignatures();
            System.out.println("SIGNATURES SIZE: " + signatures.size());
            if (signatures.size() > 0) {
                System.out.println("SIGNATURES: " + signatures.toString());
            }
            decidedMessage.setSignatures(snapshotBlock.getSignatures());
            decidedMessage.setAccounts(snapshotBlock.getAccounts());
            _broadcast.sendDecide(decidedMessage, clientPort);
            _nonceCounter++;
        } else {
            System.out.println("No snapshot block yet");
            DecidedMessage decidedMessage = new DecidedMessage("W_BALANCE", "Error: No snapshot block yet.",
                    _server.getId(), _nonceCounter);
            _broadcast.sendDecide(decidedMessage, clientPort);
            _nonceCounter++;
        }
        
    }

    public void strongCheckBalance(PublicKey publicKey, int clientPort, int lastRead) {
        if (accountExists(publicKey) && lastRead <= _blocksCommited) {
            synchronized (this) { // necessario??
                //Account account = _accounts.get(publicKey);
                Account account = getAccount(publicKey);
                System.out.println(
                        "[SERVER " + _server.getId() + "]: Balance checked successfully. Client: " + clientPort);
                DecidedMessage decidedMessage = new DecidedMessage("S_BALANCE", account.getBalance(), _server.getId(),
                        _nonceCounter, _blocksCommited);
                _broadcast.sendDecide(decidedMessage, clientPort);
                _nonceCounter++;
            }
        } else {
            DecidedMessage decidedMessage = new DecidedMessage("S_BALANCE", -1, _server.getId(), _nonceCounter, _blocksCommited);
            _broadcast.sendDecide(decidedMessage, clientPort);
            _nonceCounter++;
            //System.out.println("Account doesn't exist");
        }
    }

    /**
     * doesn't add transactions if the block is already full
     * calls make transaction, so the transaction can now be added to the new block
     * 
     * @param msg
     */
    public synchronized void waitFunction(TransactionMessage msg) {
        try {
            _pendingTransactions.add(msg);

            while (_currentTransactionBlock.isCompleted())
                wait();

            TransactionMessage newMsg = _pendingTransactions.get(0);
            _pendingTransactions.remove(0);
            int clientPort = newMsg.getClientPort();
            makeTransaction(newMsg, clientPort);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * tries to append a transaction to a block
     * if block already full calls waitFunction for this transaction.
     * if block full call the consensus for this block of transactions
     * 
     * @param msg
     * @param clientPort
     */
    public void makeTransaction(TransactionMessage msg, int clientPort) {
        // if the accounts don't exist, send error message
        if (!accountExists(msg.getSource()) || !accountExists(msg.getDestination())) {
            DecidedMessage decidedMessage = new DecidedMessage("TRANSACTION",
                    "Error: One of the accounts doesn't exist.", _server.getId(), _nonceCounter);
            _broadcast.sendDecide(decidedMessage, clientPort);
            _nonceCounter++;
            return; // leave
        }

        boolean notEnoughMoney = false;
        msg.setClientPort(clientPort); // the transaction message has now the client port

        // validate the transaction
        synchronized (this) {
            // if it's already
            if (_currentTransactionBlock.isCompleted()) {
                waitFunction(msg); // the request will be done here
                return;
            }
            Account source = getAccount(msg.getSource());
            Account destination = getAccount(msg.getDestination());
            float amount = msg.getAmount();

            // not enough money, send error message
            if (source.canSubtractBalanceBlockchain(amount + amount * _fee)) {
                // update acounts
                source.subtractTempBalance(amount);
                destination.addTempBalance(amount);

                // add transaction to block
                _currentTransactionBlock.addTransaction(msg);

                // if block full, perform consensus
                if (_currentTransactionBlock.isCompleted()){
                    this.start(_currentTransactionBlock, _instance, clientPort);
                }
                    
            } else {
                notEnoughMoney = true;
            }
        }

        if (notEnoughMoney) {
            DecidedMessage decidedMessage = new DecidedMessage("TRANSACTION", "Error: Not enough balance.",
                    _server.getId(), _nonceCounter);
            _broadcast.sendDecide(decidedMessage, clientPort);
            _nonceCounter++;
        }
    }

    /**
     * ==============================================================================
     * IBFT ALGORITHM implementation
     * 
     * these operations refer to putting a block of transactions into the blockchain
     * ==============================================================================
     */

    public void cleanup() {
        _currentRound = 0;
        _preparedRound = 0;
        // _preparedValue = "";
        // _inputValue = "";
        _prepareQuorum.clear();
        _commitQuorum.clear();
        _receivedMessages.clear();
        _sentCommit = false;
        _decided = false;
    }

    /**
     * the server wants to send a new value
     * for now there is only 1 instance and only 1 round so there is no problem
     */
    public void start(TransactionBlock value, int instance, int clientPort) {
        System.out.println("[SERVER " + _server.getId() + "] IBFT:: started new instance " + instance);
        _inputValue = value;
        _instance = instance;

        if (_server.getId() == _leader) { // if it's the leader
            if (_server.isFaulty()){
                //if leader is faulty, dont start consensus
                return;
            }
            Message prePrepareMessage = new Message(PRE_PREPARE_MSG, _instance, _currentRound, _inputValue,
                    _server.getId(), _server.getPort(), _nonceCounter++);
            //System.out.println("[SERVER " + _server.getId() + "] PRE-PREPARE: " + prePrepareMessage.toString());
            _broadcast.sendBroadcast(prePrepareMessage);
        }
    }

    public void sendPrepares(int instance, int round, TransactionBlock value) {
        // set timer -> ainda nao precisamos porque ainda nao ha rondas
        Message prepareMessage;
        if (_server.isFaulty()) {
            //TransactionMessage byzantineMessage = new TransactionMessage(null, null, 20.0f);
            //TransactionBlock byzantineBlock = new TransactionBlock("TRANSACTIONS");
            //byzantineBlock.addTransaction(byzantineMessage);
            //prepareMessage = new Message(PREPARE_MSG, instance, round, byzantineBlock, 1, _server.getPort(), _nonceCounter++);
            //drop message
            return;
            //System.out.println("[SERVER " + _server.getId() + "] PREPARE: " + prepareMessage.toString());
        } else {
            prepareMessage = new Message(PREPARE_MSG, instance, round, value, _server.getId(), _server.getPort(), _nonceCounter++);   
            //System.out.println("[SERVER " + _server.getId() + "] PREPARE: " + prepareMessage.toString());
            if (value.getType().equals("SNAPSHOT")){
                prepareMessage.setSnapshotSignature(_ourSignature);
            }
            _broadcast.sendBroadcast(prepareMessage);
        }
    }

    public void receivedPrepareQuorum(int round, TransactionBlock value) {
        // for future reference of IBFT
        _preparedRound = round;
        _preparedValue = value;

        Message commitMessage = new Message(COMMIT_MSG, _instance, _preparedRound, _preparedValue, _server.getId(),
                _server.getPort(), _nonceCounter++);
        _broadcast.sendBroadcast(commitMessage); // broadcast
    }

    /**
     * when we receive a quorum of commit
     * 
     * perform the transactions in the block 
     * insert the block in the blockchain 
     * if 3 blocks commited then -> append status to blockchain
     * popping transactions from the waiting list 
     * 
     * @param msg
     */
    public void receivedCommitQuorum(Message msg) {
        // DECIDE -> dar append da string à blockchain
        LOGGER.info(" [SERVER " + _server.getId() + "] ===== DECIDED instance " + _instance + " =====");
        
        TransactionBlock block = msg.getBlock();
        synchronized (this) {
            if (block.getType().equals("TRANSACTIONS")) {
                List<TransactionMessage> transactions = msg.getBlock().getTransactions();
                // perform the transaction in order
                for (int i = 0; i < transactions.size(); i++) {
                    // get transaction
                    TransactionMessage transactionMessage = transactions.get(i);

                    // get source and dest accounts
                    PublicKey pubKeySource = transactionMessage.getSource();
                    PublicKey pubKeyDest = transactionMessage.getDestination();
                    //Account source = _accounts.get(pubKeySource);
                    //Account dest = _accounts.get(pubKeyDest);
                    Account source = getAccount(pubKeySource);
                    Account dest = getAccount(pubKeyDest);

                    // perform the transaction
                    float amount = transactionMessage.getAmount();
                    source.subtractBalance(amount);
                    dest.addBalance(amount);

                    // perform the transaction fee
                    source.subtractBalance(amount * _fee);
                    _leaderAccount.addBalance(amount * _fee);
                    //System.out.println("[Server " + _server.getId() + "] Leader Account - new bal: " + _leaderAccount.getBalance());
                }
            } else {
                System.out.println("[SERVER " + _server.getId() + "] Adding signatures to block, (size: " + _consesnsusSignatures.size() + ")");
                block.addSignatures(_consesnsusSignatures);
            }

            // insert the block in the blockchain
            _server.getBlockchain().addTransactionBlock(block);

            _consesnsusSignatures.clear();
            
            // update instance number of consensus
            _instance += 1;

            // APPEND STATUS BLOCK TO BLOCKCHAIN
            _blocksCommited += 1;
            if (_blocksCommited % 3 == 0) { // start the new consensus for the status
                this.cleanup();
                TransactionBlock snapshotBlock = new TransactionBlock("SNAPSHOT");
                snapshotBlock.setAccounts(_accounts);
                //System.out.println("[Server " + _server.getId() + "] ::: NEW SNAPSHOT ::: -> " + snapshotBlock.hashCode() + " - Instance " + _instance);
                // insert status block in blockchain 
                this.start(snapshotBlock, _instance, -1); //client port doesnt matter
            }    

            
            System.out.println("[Server " + _server.getId() + "] ::: NEW INSTANCE ::: -> " + _instance);

            // clean this up for the next consensus
            this.cleanup();

            // creating the new block 
            _currentTransactionBlock = new TransactionBlock("TRANSACTIONS");

            // popping the transactions that were waiting for this consensus to end
            //System.out.println("[Server " + _server.getId() + "] ::: NOTIFYING ALL :::");
            for(int i = 0; i < _pendingTransactions.size(); i++) {
                this.notify();
            }

        }

        // sending confirmations (only if it's a transaction block)
        if (block.getType().equals("TRANSACTIONS")){
            List<TransactionMessage> transactions = msg.getBlock().getTransactions();
            // sending the confirmations (not atomically)
            for (int i = 0; i < transactions.size(); i++) {
                DecidedMessage decidedMessage = new DecidedMessage("TRANSACTION", "Transaction done", _server.getId(),
                        _nonceCounter);
                _broadcast.sendDecide(decidedMessage, transactions.get(i).getClientPort());
                _nonceCounter++;
            }
        }
        
    }

    /**
     * when we receive a pre-prepare
     */
    public void receivePrePrepare(Message msg) {
        // if it's from the leader of this round and instance
        //System.out.println("[SERVER " + _server.getId() + "] PRE-PREPARE: " + msg.toString());
        if (msg.getInstance() == _instance && msg.getRound() == _currentRound &&
                msg.getSenderId() == _leader && !_receivedMessages.contains(msg)) {
            //System.out.println("[SERVER " + _server.getId() + "] PRE-PREPARE2: " + msg.toString());
            if (msg.getBlock().getType().equals("SNAPSHOT")) {
                String snapshotSignature;
                if (_server.isFaulty()){
                    snapshotSignature = _broadcast.getSnapshotSignature("FAKE");
                } else {
                    snapshotSignature = _broadcast.getSnapshotSignature(msg.getBlock().getAccounts().toString());
                }
                _consesnsusSignatures.put(_server.getId(), snapshotSignature);
                System.out.println("[SERVER " + _server.getId() + "] ADDED OUR SIGNATURE TO LIST (size: " + _consesnsusSignatures.size() + ")");
                _ourSignature = snapshotSignature;
            }

            _receivedMessages.add(msg);
            this.sendPrepares(_instance, _currentRound, msg.getBlock());
        }
    }

    /**
     * when we receive a prepare
     */
    public void receivePrepare(Message msg) {
        // verificar a autenticacao no perfect channel
        if (_decided || msg.getInstance() < _instance) {
            return;
        }

        if (msg.getBlock().getType().equals("SNAPSHOT")) {
            if (!_broadcast.verifySignature(msg.getSnapshotSignature(), _accounts.toString(), msg.getSenderId())) {
                System.out.println("[SERVER " + _server.getId() + "] INVALID SIGNATURE");
                return;
            } else {
                _consesnsusSignatures.put(msg.getSenderId(), msg.getSnapshotSignature());
                System.out.println("[SERVER " + _server.getId() + "] VALID SIGNATURE, adding to consensusSignatures (size: " + _consesnsusSignatures.size() + ")");
            }
        }   

        // if it's the same round and instance as ours
        // and has not received the message yet
        if (msg.getInstance() == _instance && msg.getRound() == _currentRound
                && !_receivedMessages.contains(msg) && !_decided) {
            _receivedMessages.add(msg);
            TransactionBlock value = msg.getBlock();
            // update the quorum or insert new entry if it isn't there
            // if still no one had sent prepare

            boolean found = false;

            // check if a server already has "prepared" this block 
            synchronized (this) {
                while (true) {
                    for (TransactionBlock block : _prepareQuorum.keySet()) {
                        if (value.equals(block)) { // if the value is already in the quorum
                            ArrayList<Integer> list = _prepareQuorum.get(block);
                            if (!list.contains(msg.getSenderId())) {
                                list.add(msg.getSenderId());
                                _prepareQuorum.put(block, list);
                            }
                            value = block;
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    } else { // if the value is not in the quorum -> add it
                        ArrayList<Integer> list = new ArrayList<Integer>();
                        list.add(msg.getSenderId());
                        _prepareQuorum.put(value, list);
                        break;
                    }
                }
            }

            // System.out.println("[SERVER " + _server.getId() + "] Prepare quorum size for
            // value: " + value.hashCode() + " -> " + _prepareQuorum.get(value).size());

            // only send one commit if we have already quorum
            if (_prepareQuorum.get(value).size() >= 2 * _F + 1 && !_sentCommit && !_decided) { // in case of quorum
                _sentCommit = true;
                this.receivedPrepareQuorum(_currentRound, value);
            }
        }
    }

    /**
     * when we receive a commit
     */
    public void receiveCommit(Message msg) {
        // verificar a autenticacao no perfect channel
        if (_decided || msg.getInstance() < _instance) {
            return;
        }
        // if it's the same round and instance as ours
        // and has not received the message yet
        if (msg.getInstance() == _instance && msg.getRound() == _currentRound
                && !_receivedMessages.contains(msg) && !_decided) {
            _receivedMessages.add(msg);
            TransactionBlock value = msg.getBlock();

            boolean found = false;

            synchronized (this) {
                while (true) {
                    for (TransactionBlock block : _commitQuorum.keySet()) {
                        if (value.equals(block)) { // if the value is already in the quorum
                            ArrayList<Integer> list = _commitQuorum.get(block);
                            if (!list.contains(msg.getSenderId())) {
                                list.add(msg.getSenderId());
                                _commitQuorum.put(block, list);
                            }
                            value = block;
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    } else { // if the value is not in the quorum -> add it
                        ArrayList<Integer> list = new ArrayList<Integer>();
                        list.add(msg.getSenderId());
                        _commitQuorum.put(value, list);
                        break;
                    }
                }
            }

            // System.out.println("[SERVER " + _server.getId() + "] Commit quorum size for
            // value: " + value + " -> " + _commitQuorum.get(value).size());

            // decide the value only one time
            if (_commitQuorum.get(value).size() >= 2 * _F + 1 && !_decided) {
                _decided = true;
                // System.out.println("[SERVER " + _server.getId() + "] <<< received " +
                // _commitQuorum.get(value).size()
                // + " commits for the value >>> " + value);
                this.receivedCommitQuorum(msg); // we received a quorum
            }
        }
    }

    public int getConsensusInstance() {
        return _instance;
    }

    public int getNonceCounter() {
        int nonce = _nonceCounter;
        _nonceCounter++;
        return nonce;
    }

    public boolean accountExists(PublicKey publicKey) {
        for (Account account : _accounts) {
            if (account.getPublicKey().equals(publicKey)) {
                return true;
            }
        }
        return false;
    }

    public Account getAccount(PublicKey publicKey) {
        for (Account account : _accounts) {
            if (account.getPublicKey().equals(publicKey)) {
                return account;
            }
        }
        return null;
    }
}
