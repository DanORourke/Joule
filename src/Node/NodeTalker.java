package Node;

import DB.SQLiteJDBC;
import ReadWrite.MathStuff;
import ReadWrite.Verify;
import Structures.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class NodeTalker {
    private Socket socket;
    private SQLiteJDBC db;
    private NodeBase nb;
    private PrintWriter out;
    private NodeListener listener;
    private int blockHeight;
    private String blockHash;
    private int otherBlockHeight;
    private String currentState;
    private boolean canWeTalk;
    private String ping;
    private String myIp;
    private Integer myPort;
    private String myNetName;
    private boolean stop;
    private String theirIp;
    private int theirPort;
    private String theirNetName;
    private String networkType;
    private String nameOfNetwork;

    public NodeTalker(Socket socket, SQLiteJDBC db, NodeBase nb, String howStarted, String networkType){
        //TODO handle malformed data sent from other nodes without failing
        this.socket = socket;
        this.db = db;
        this.nb = nb;
        this.networkType = networkType;
        setInfo();
        this.currentState = howStarted;
        updateBlockHeight();
        this.canWeTalk = nb.canWeTalk(networkType);
        this.ping = "ping";
        this.stop = false;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setInfo(){
        if (networkType.equals("outside")){
            this.myIp = nb.getOutsideIp();
            this.myPort = nb.getOutsidePort();
            this.myNetName = nb.getOutsideNetName();
            this.nameOfNetwork = "outside";
        }else{
            this.myIp = nb.getInsideIp();
            this.myPort = nb.getInsidePort();
            this.myNetName = nb.getInsideNetName();
            this.nameOfNetwork = nb.getNameOfInsideNetwork();
        }

        this.theirIp = "NA";
        this.theirPort = 0;
        this.theirNetName = "NA";
    }

    public String getTheirIp(){
        return theirIp;
    }

    public int getTheirPort(){
        return theirPort;
    }

    public String getNetworkType(){
        return networkType;
    }
    public String getTheirNetName(){
        return theirNetName;
    }

    public String getConnectedIp(){
        InetAddress addr = socket.getInetAddress();
        return addr.getHostAddress();
    }

    public int getConnectedPort(){
        return socket.getPort();
    }

    public void setMyIp(String myIp) {
        this.myIp = myIp;
        sendString(getHello());
    }

    public void setMyPort(Integer myPort) {
        this.myPort = myPort;
        sendString(getHello());
    }

    public void setMyNetName(String newNetName){
        this.myNetName = newNetName;
        sendString(getHello());
    }

    public void updateUser(String newMyIp, int newMyPort, String newMyNetName){
        this.myIp = newMyIp;
        this.myPort = newMyPort;
        this.myNetName = newMyNetName;
        sendString(getHello());
    }

    public void begin(){
        createListen();
        introduce();
        sendPing();
    }

    private void sendString(String string){
        if (!stop){
            out.println(string);
            System.out.println("sent to Ip: " + getConnectedIp() + " Port: " + getConnectedPort() + " message: " + string);
        }
    }

    private void introduce(){
        if (currentState.equals("talk")){
            sendString(getHello());
            sendString(getFriends());
            sendString(getBlockHeight());
        }
        currentState = "listen";
    }

    private void createListen(){
        this.listener = new NodeListener(socket, this);
        Thread listenerThread = new Thread(listener);
        listenerThread.start();
    }

    private void setTheirIpPort(String friendIpAddress, int friendPort, String friendNetName){
        System.out.println("talker setTheirIpPort friendIp: " + friendIpAddress + " friendPort: " + friendPort +
                " friendNetName: " + friendNetName);

        if (friendIpAddress.equals("NA") || friendPort == 0 || !friendIpAddress.equals(getConnectedIp())){
            theirIp = getConnectedIp();
            theirPort = getConnectedPort();
            theirNetName = friendNetName;

        }else if (addressIsLegal(friendIpAddress, friendPort) &&
                nb.isNewFriend(friendIpAddress,friendPort, this)){
            theirIp = friendIpAddress;
            theirPort = friendPort;
            theirNetName = friendNetName;
            if (!db.newFriend(theirIp, theirPort, theirNetName, nameOfNetwork, 1)){
                long time = new Date().getTime();
                db.updateLastContact(theirIp, theirPort, theirNetName, nameOfNetwork, time);
            }
        }else{
            nb.removeTalker(this, networkType, 1);
        }
    }

    private boolean addressIsLegal(String friendIpAddress, int friendPort) {
        if (!new MathStuff().isValidIp(friendIpAddress, networkType)){
            return false;
        }else if (!new MathStuff().isValidPort(friendPort)){
            return false;
        }
        System.out.println("Talker addressIsLegal: " + true);
        return true;
    }

    private String getHello(){
        String hello = "getHello";
        return hello + "," + myIp + "," + String.valueOf(myPort) + "," + myNetName;
    }

    private String giveHello(){
        String hello = "giveHello";
        return hello + "," + myIp + "," + String.valueOf(myPort) + "," + myNetName;
    }

    public void askForBlockHeight(){
        updateBlockHeight();
        String hello = "getBlockHeight," + String.valueOf(blockHeight) + "," + blockHash;
        sendString(hello);
    }

    private String getBlockHeight(){
        String hello = "getBlockHeight";
        return hello + "," + String.valueOf(blockHeight) + "," + blockHash;
    }

    private String giveBlockHeight(){
        String hello = "giveBlockHeight";
        return hello + "," + String.valueOf(blockHeight) + "," + blockHash;
    }

    public void hearThis(ArrayList<String> words){

        String code = words.get(0);

        if (code.equals("getHello")){
            if (canWeTalk){
                setTheirIpPort(words.get(1), Integer.valueOf(words.get(2)), words.get(3));
                sendString(giveHello());
            }else{
                sendString(getFriends());
                nb.removeTalker(this, networkType, 1);
            }
        }else if (code.equals("giveHello")){
            setTheirIpPort(words.get(1), Integer.valueOf(words.get(2)), words.get(3));

        }else if (code.equals("Goodbye")){
            nb.removeTalker(this, networkType, 1);

        }else if (code.equals("getBlockHeight")){
            otherBlockHeight = Integer.valueOf(words.get(1));
            updateBlockHeight();
            System.out.println(blockHeight + ": " + otherBlockHeight);
            sendString(giveBlockHeight());
            checkIfMoreNeeded(words.get(2));

        }else if (code.equals("giveBlockHeight")){
            otherBlockHeight = Integer.valueOf(words.get(1));
            updateBlockHeight();
            System.out.println(blockHeight + ": " + otherBlockHeight);
            checkIfMoreNeeded(words.get(2));

        }else if (code.equals("getFriends")){
            db.addFriends(words, nameOfNetwork);
            sendString(giveFriends());

        }else if (code.equals("giveFriends")){
            db.addFriends(words, nameOfNetwork);
        }else if (code.equals("newBlock")){
            nb.stopMiner();
            words.remove(0);
            System.out.println("nodeTalker hearThis words minus 0: " + words);
            Block block = convertWireToBlock(words);
            boolean isVerified = new Verify(db).isBlockVerified(block, nb);
            if (isVerified){
                nb.addHeardBlock(block, this);
                updateBlockHeight();
                sendString(getBlockHeight());
                nb.startMiner();
            }else {
                askForBlockHeight();
            }

        }else if (code.equals("ping")){
            sendPong();
        }
        else if (code.equals("pong")){
            setPing("pong");
            System.out.println(socket.getInetAddress() + " pong received");

        }else if (code.equals("newTx")){
            words.remove(0);
            System.out.println("nodeTalker hearThis words minus 0: " + words);
            Tx report = convertWireToTx(words);
            boolean isVerified = new Verify(db).verifyTx(report);
            if (isVerified){
                nb.addTx(report, this);
            }
        }else if (code.equals("getBlock")){
            updateBlockHeight();
            if (Integer.valueOf(words.get(1)) <= blockHeight){
                giveBlock(Integer.valueOf(words.get(1)));
            }
        }else if (code.equals("giveBlock")){
            words.remove(0);
            System.out.println("nodeTalker hearThis words minus 0: " + words);
            Block block = convertWireToBlock(words);
            boolean isVerified = new Verify(db).isBlockVerified(block, nb);
            if (isVerified){
                db.addBlock(block);
                updateBlockHeight();
                if(blockHeight == otherBlockHeight){
                    nb.startMiner();
                }
            }else {
                askForBlockHeight();
            }
        }else if (code.equals("getTx")){
            String txHash = words.get(1);
            Tx tx = db.getTx(txHash);
            if (tx.isProper()){
                ArrayList<String> wireTx = tx.convertForWire();
                sendGiveTweet(wireTx);
            }
        }else if (code.equals("giveTweet")){
            words.remove(0);
            System.out.println("nodeTalker hearThis words minus 0: " + words);
            Tx tx = convertWireToTx(words);
            boolean isVerified = new Verify(db).verifyTx(tx);
            if (isVerified){
                db.addTx(tx);
            }
        }else if (code.equals("getPastHeaderHashes")){
            String blockHeight = words.get(1);
            sendPastHeaderHashes(blockHeight);
        }else if (code.equals("givePastHeaderHashes")){
            words.remove(0);
            ArrayList<ArrayList> pastHeaderHashes = convertToPastHeaderHashes(words);
            updateBlockHeight();
            nb.decideWhereToStart(pastHeaderHashes, Integer.valueOf(words.get(words.size() - 2)));
        }
    }

    private ArrayList<ArrayList> convertToPastHeaderHashes(ArrayList<String> words){
        ArrayList<ArrayList> pastHeaderHashes = new ArrayList<>();
        ArrayList<String> hashes = new ArrayList<>();
        ArrayList<String> heights = new ArrayList<>();
        int i = 0;
        while (i < words.size()){
            heights.add(words.get(i));
            hashes.add(words.get(i+1));
            i = i + 2;
        }
        pastHeaderHashes.add(heights);
        pastHeaderHashes.add(hashes);
        return pastHeaderHashes;
    }

    private void sendPastHeaderHashes(String blockHeight){
        ArrayList<String> pastHeaderHashes = db.getPastHeaderHashes(Integer.valueOf(blockHeight));
        String whole = "givePastHeaderHashes";
        for (String piece : pastHeaderHashes){
            //height, then hash
            whole = whole + "," + piece;
        }
        sendString(whole);
    }

    private Block convertWireToBlock(ArrayList<String> words){
        Header header = new Header(words.get(0), Integer.valueOf(words.get(1)), words.get(2), words.get(3),
                words.get(4), Integer.valueOf(words.get(5)));
        Txo txo = new Txo(words.get(6), Integer.valueOf(words.get(12)), Integer.valueOf(words.get(13)), words.get(14));
        AllTxo allTxo = new AllTxo();
        allTxo.addTxo(txo);
        JouleBase base = new JouleBase(words.get(6), Integer.valueOf(words.get(7)),
                words.get(8), Integer.valueOf(words.get(9)), words.get(10),
                Integer.valueOf(words.get(11)), allTxo);
        AllTx allTx = new AllTx();
        // add each tx in list to allTx
        if (words.size() > 15){
            int i = 15;
            while(i < words.size()){
                //allTx.addWireTx(words.get(i));
                //get tx from db to add
                allTx.addTx(db.getTx(words.get(i)));
                i++;
            }
        }
        Block block = new Block(header, base, allTx);
        block.printBlock();
        return block;
    }

    private ArrayList<ArrayList> convertToFullBlock(ArrayList<String> words){
        ArrayList<ArrayList> fullBlock = new ArrayList<>();
        ArrayList<String> header = new ArrayList<>();
        ArrayList<ArrayList> tweetBase = new ArrayList<>();
        ArrayList<String> tx = new ArrayList<>();
        ArrayList<ArrayList> txoList = new ArrayList<>();
        ArrayList<String> txo = new ArrayList<>();
        ArrayList<String> txToAdd = new ArrayList<>();

        header.addAll(Arrays.asList(words.get(0), words.get(1), words.get(2), words.get(3), words.get(4), words.get(5)));
        fullBlock.add(header);
        tx.addAll(Arrays.asList(words.get(6), words.get(7), words.get(8), words.get(9), words.get(10), words.get(11)));
        tweetBase.add(tx);
        txo.addAll(Arrays.asList(words.get(12), words.get(13), words.get(14)));
        txoList.add(txo);
        tweetBase.add(txoList);
        fullBlock.add(tweetBase);

        if (words.size() > 15){
            int i = 15;
            while(i < words.size()){
                txToAdd.add(words.get(i));
                i++;
            }
        }
        fullBlock.add(txToAdd);

        return fullBlock;
    }

    private Tx convertWireToTx(ArrayList<String> words){
        String hash  = words.get(0);
        Tx tx  = new Tx(hash, Integer.valueOf(words.get(1)), words.get(2),
                Integer.valueOf(words.get(3)), words.get(4), Integer.valueOf(words.get(5)));

        int numberOfTxo = Integer.valueOf(words.get(6));
        AllTxo allTxo = tx.getAllTxo();
        for (int i = 0; i < numberOfTxo; i++){
            Txo txo = new Txo(hash, Integer.valueOf(words.get(7 + (i * 3))),
                    Integer.valueOf(words.get(7 + (i * 3) + 1)), words.get(7 + (i * 3) + 2));
            allTxo.addTxo(txo);
        }

        int homeForTxi = 7 + (numberOfTxo * 3);
        int numberOfTxi = Integer.valueOf(words.get(homeForTxi));
        AllTxi allTxi = tx.getAllTxi();
        for (int i = 0; i < numberOfTxi; i++){
            Txi txi = new Txi(hash, Integer.valueOf(words.get(homeForTxi + 1 + (i * 3))),
                    words.get(homeForTxi + 1 + (i * 3) + 1),  Integer.valueOf(words.get(homeForTxi + 1 + (i * 3) + 2)));
            allTxi.addTxi(txi);
        }
        return tx;
    }

    private ArrayList<ArrayList> convertToFullTweet(ArrayList<String> words){
        ArrayList<ArrayList> fullTweet = new ArrayList<>();
        ArrayList<String> tx = new ArrayList<>();
        ArrayList<ArrayList> txoList = new ArrayList<>();
        ArrayList<ArrayList> txiList = new ArrayList<>();

        tx.addAll(Arrays.asList(words.get(0), words.get(1),words.get(2),words.get(3),words.get(4),words.get(5)));
        fullTweet.add(tx);

        int numberOfTxo = Integer.valueOf(words.get(6));
        for (int i = 0; i < numberOfTxo; i++){
            ArrayList<String> txoInstance = new ArrayList<>();
            txoInstance.addAll(Arrays.asList(words.get(7 + (i * 3) + 0), words.get(7 + (i * 3) + 1),
                    words.get(7 + (i * 3) + 2)));
            txoList.add(txoInstance);
        }
        fullTweet.add(txoList);

        int homeForTxi = 7 + (numberOfTxo * 3);
        int numberOfTxi = Integer.valueOf(words.get(homeForTxi));

        for (int i = 0; i < numberOfTxi; i++){
            ArrayList<String> txiInstance = new ArrayList<>();
            txiInstance.addAll(Arrays.asList(words.get(homeForTxi + 1 + (i * 3) + 0),
                    words.get(homeForTxi + 1 + (i * 3) + 1),  words.get(homeForTxi + 1 + (i * 3) + 2)));
            txiList.add(txiInstance);
        }
        fullTweet.add(txiList);

        System.out.println("nt convertTo fullTweet: " + fullTweet);
        return fullTweet;
    }

    private void updateBlockHeight(){
        ArrayList<String> highestBlock = db.getBlockHeight();
        blockHeight = Integer.valueOf(highestBlock.get(0));
        blockHash = highestBlock.get(1);
    }

    private void sendPing(){
        sendString("ping");
        setPing("ping");
        scheduleTestConnection();
    }

    private void sendPong(){
        sendString("pong");
    }

    private void scheduleTestConnection(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                testConnection();
            }
        }, 1*60*1000);
    }

    private void testConnection(){
        if (ping.equals("ping")){
            nb.removeTalker(this, networkType, 1);
        }else if (ping.equals("pong")){
            sendPing();
        }
    }

    private void setPing(String pong){
        ping = pong;
    }

    public void closeConnection(int code){
        if (code == 1){
            long time = new Date().getTime();
            db.updateLastContact(theirIp, theirPort, theirNetName, nameOfNetwork, time);
        }
        sendString("Goodbye");
        stop = true;
        listener.removeTalker();
        listener.setStop(true);
        out.close();
    }

    private void checkIfMoreNeeded(String otherHash){
        nb.updateBaseHeights(blockHeight, otherBlockHeight);
        if (blockHeight < otherBlockHeight){
            //need to get on the correct chain
            getPastTenHeaderHashes(blockHeight);
        }
        if (blockHeight == otherBlockHeight  && !blockHash.equals(otherHash) && !haveHeaderHashInChain(otherHash)){
            getPastTenHeaderHashes(blockHeight);
        }
    }

    private boolean haveHeaderHashInChain(String otherHash){
        return db.haveBlockHash(otherHash);
    }

    public void getPastTenHeaderHashes(int height){
        sendString("getPastHeaderHashes," + String.valueOf(height));
    }

    public void giveBlock(int height){
        ArrayList<Block> allBlocks = db.getAllBlocks(height);
        if (allBlocks.isEmpty()){
            return;
        }
        for (Block block : allBlocks){
            ArrayList<String> wireBlock = block.convertBlockForWire();
            sendGiveBlock(wireBlock);
        }
    }

//    public void giveBlockold(int height){
//        ArrayList<ArrayList> allFullBlocks = db.getAllFullBlocks(height);
//        if (allFullBlocks.isEmpty()){
//            return;
//        }
//        for (ArrayList<ArrayList> fullBlock : allFullBlocks){
//            ArrayList<String> wireBlock = nb.convertFullBlockForWire(fullBlock);
//            System.out.println("Talker giveBlock wireBlock: " + wireBlock);
//            if (fullBlock.size() == 3 && !fullBlock.get(2).isEmpty()){
//                ArrayList<ArrayList> fullTweetList = new ArrayList<>();
//                for (String tx: (ArrayList<String>)fullBlock.get(2)){
//                    fullTweetList.add(db.getFullTweet(tx));
//                }
//                for (ArrayList<ArrayList> fullTweet: fullTweetList){
//                    ArrayList<String> wireTweet = nb.convertFullTweetForWire(fullTweet);
//                    sendGiveTweet(wireTweet);
//                }
//            }
//            sendGiveBlock(wireBlock);
//        }
//    }


    private String getFriends(){
        String friends = "getFriends";
        ArrayList<ArrayList> allFriends = db.getFriends(nameOfNetwork);
        for (ArrayList<String> friend : allFriends){
            friends = friends + "," + friend.get(0) + "," + friend.get(1) + "," + friend.get(2);
        }
        return friends;
    }

    private String giveFriends(){
        String friends = "giveFriends";
        ArrayList<ArrayList> allFriends = db.getFriends(nameOfNetwork);
        for (ArrayList<String> friend : allFriends){
            friends = friends + "," + friend.get(0) + "," + friend.get(1) + "," + friend.get(2);
        }
        return friends;
    }

    public void sendTweet(ArrayList<String> fullTweet){
        String whole = "newTx";
        for (String piece : fullTweet){
            whole = whole + "," + piece;
        }
        sendString(whole);
    }

    public void sendGiveTweet(ArrayList<String> fullTweet){
        String whole = "giveTx";
        for (String piece : fullTweet){
            whole = whole + "," + piece;
        }
        sendString(whole);
    }

    public void sendNewBlock(ArrayList<String> fullBlock){
        System.out.println("sending block");
        String whole = "newBlock";
        for (String piece : fullBlock){
            whole = whole + "," + piece;
        }
        sendString(whole);
    }

    public void sendGiveBlock(ArrayList<String> fullBlock){
        String whole = "giveBlock";
        for (String piece : fullBlock){
            whole = whole + "," + piece;
        }
        sendString(whole);
    }

    public void getBlock(int height){
        String ask = "getBlock," + String.valueOf(height);
        sendString(ask);
    }

    public void getTx(String txHash){
        String ask = "getTx," + txHash;
        sendString(ask);
    }
}
