package Node;

import DB.SQLiteJDBC;
import Mining.Miner;
import ReadWrite.Construct;
import ReadWrite.MathStuff;
import Structures.Block;
import Structures.Tx;
import Structures.Txi;
import UI.BigWindow;
import UI.Friend;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class NodeBase {
    private final SQLiteJDBC db;
    private String networkType;
    private String outsideIp;
    private int outsidePort;
    private String outsideNetName;
    private ArrayList<NodeTalker> outsideTalkers;
    private String insideIp;
    private int insidePort;
    private String insideNetName;
    private ArrayList<NodeTalker> insideTalkers;
    private ArrayList<NodeTalker> allTalkers;
    private String nameOfInsideNetwork;
    private BigWindow window;
    private ExecutorService minerExecutor;
    private ExecutorService outsideServerExecutor;
    private ExecutorService insideServerExecutor;
    private String username;
    private int blockHeight;
    private int blockChainHeight;
    private boolean gettingBlocks;
    private Miner miner;
    private NodeServer outsideServer;
    private NodeServer insideServer;
    //private long oldTime;

    public NodeBase(SQLiteJDBC db){
        this(db, "outside", null);
    }

    public NodeBase(SQLiteJDBC db, String networkType, String insideName){
        //set variables
        this.db = db;
        this.outsideTalkers = new ArrayList<>();
        this.insideTalkers = new ArrayList<>();
        this.allTalkers = new ArrayList<>();
        updateBlockHeight();
        this.blockChainHeight = 0;
        this.gettingBlocks = false;
        this.networkType = networkType;
        this.nameOfInsideNetwork = insideName;

    }

    private void scheduleNetworkCheck(String networkType){
        //call others periodically, start quickly if first time and callEnoughFriends only has the seed network
        int initialDelay = ((db.getFirstTime()) ? 5 : 60);
        //reset server every so often
        ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(1);

        ScheduledFuture scheduledFuture =
                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                                                      public void run() {
                                                          callEnoughFriends();
                                                      }
                                                  },
                        initialDelay, 60,
                        TimeUnit.SECONDS);
    }

    private void callEnoughFriends(){
        //contact others in P2P network
        try {
            NodeParty party = new NodeParty(this, db);
            Thread partyThread = new Thread(party);
            partyThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startNewServer(String whichOne){
        //listen for others who may call you
        if (whichOne.equals("outside")){
            //listen for others calling from outside ip
            this.outsideServerExecutor = Executors.newSingleThreadExecutor();
            if (username != null){
                try {
                    outsideServer = new NodeServer(this, db, "outside");
                    outsideServerExecutor.execute(outsideServer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else if (whichOne.equals("inside")){
            //listen for others calling from a local ip
            this.insideServerExecutor = Executors.newSingleThreadExecutor();
            if (username != null){
                try {
                    insideServer = new NodeServer(this, db, "inside");
                    insideServerExecutor.execute(insideServer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            //listen for both
            startNewServer("outside");
            startNewServer("inside");
        }

    }

    private void stopServer(String networkTypeAsk){
        //stop listening for others
        if (networkTypeAsk.equals("outside")){
            if (outsideServer != null){
                outsideServer.setStop();
            }
            outsideServerExecutor.shutdownNow();
            outsideServer = null;
        }else if (networkTypeAsk.equals("inside")){
            if (insideServer != null){
                insideServer.setStop();
            }
            insideServerExecutor.shutdownNow();
            insideServer = null;
        }else {
            stopServer("outside");
            stopServer("inside");
        }

    }

    private void newServer(String networkTypeAsk){
        //reset servers  delay so server will be stopped before trying to listen on same port again
        stopServer(networkTypeAsk);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startNewServer(networkTypeAsk);
            }
        }, 2*1000);
    }

    private void setDefaultInsideIpPort(){
        insideNetName = "NA";
        insideIp = "NA";
        insidePort = 0;
    }

    private void setDefaultOutsideIpPort(){
        outsideIp = "NA";
        outsidePort = 0;
        outsideNetName = "NA";
    }

    private void setIpPort(String networkType, boolean firstTImeThrough){
        //set network variables
        if (username == null){
            setDefaultOutsideIpPort();
            setDefaultInsideIpPort();
        }else if (networkType.equals("outside")) {
            HashMap<String, String> ipPort = db.getIpPort(username, networkType);
            if (ipPort.size() == 0){
                setDefaultOutsideIpPort();
                return;
            }else {
                if (ipPort.get("ip") != null) {
                    outsideIp = ipPort.get("ip");
                } else {
                    outsideIp = "NA";
                }

                outsidePort = Integer.valueOf(ipPort.get("port"));

                if (ipPort.get("netName") != null) {
                    outsideNetName = ipPort.get("netName");
                } else {
                    outsideNetName = "NA";
                }
            }
            if (firstTImeThrough){
                setDefaultInsideIpPort();
            }
        }else if (networkType.equals("inside")){
            HashMap<String, String> ipPort = db.getIpPort(username, nameOfInsideNetwork);
            if (ipPort.size() == 0){
                setDefaultInsideIpPort();
                return;
            }else {
                if (ipPort.get("ip") != null){
                    insideIp = ipPort.get("ip");
                }else {
                    insideIp = "NA";
                }

                insidePort = Integer.valueOf(ipPort.get("port"));

                if (ipPort.get("netName") != null){
                    insideNetName = ipPort.get("netName");
                }else {
                    insideNetName = "NA";
                }
            }
            if (firstTImeThrough){
                setDefaultOutsideIpPort();
            }
        }else if (networkType.equals("both")){
            setIpPort("outside", false);
            setIpPort(nameOfInsideNetwork, false);
        }

        System.out.println("outsideIp: " + outsideIp + " outsidePort: " + outsidePort +
                " outsideNetNam: " + outsideNetName + " insideIp: " + insideIp + " insidePort: " + insidePort +
                " insideNetName: " + insideNetName);
    }

    private void setUsername(String username){
        this.username = username;
    }

    public void serverFailed(String nameOfNetwork){
        stopServer(nameOfNetwork);
    }

    public void startNewMiner(){
        this.minerExecutor = Executors.newSingleThreadExecutor();
        System.out.println("startminer username: " + username);
        try {
            miner = new Miner(this, db, username);
            minerExecutor.execute(miner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setWindow(BigWindow window){
        this.window = window;
    }


    public boolean canWeTalk(String networkTypeAsk){
        if (networkTypeAsk.equals("outside")){
            if (networkType.equals("outside")){
                return (outsideTalkers.size() <= 10);
            }else if(networkType.equals("inside")){
                return false;
            }else{
                return (outsideTalkers.size() <= 5);
            }

        } else {
            if (networkType.equals("outside")){
                return false;
            }else if(networkType.equals("inside")){
                return (insideTalkers.size() <= 10);
            }else{
                return (insideTalkers.size() <= 5);
            }
        }
    }

    public boolean addTalker(NodeTalker talker, String netWorkTypeAsk){
        String theirIp = talker.getTheirIp();
        int theirPort = talker.getTheirPort();
        System.out.println("nb addTalker theirIp: " + theirIp + " theirPort: " + theirPort);

        if (isNewFriend(theirIp, theirPort, null)){
            if (netWorkTypeAsk.equals("outside")){
                outsideTalkers.add(talker);
                allTalkers.add(talker);
                talker.begin();
                System.out.println("nb addTalker outsideTalkers.size(): " + outsideTalkers.size());
                return true;
            }else{
                insideTalkers.add(talker);
                allTalkers.add(talker);
                talker.begin();
                System.out.println("nb addTalker insideTalkers.size(): " + insideTalkers.size());
                return true;
            }
        }else{
            talker.closeConnection(1);
            return false;
        }
    }

    public boolean isNewFriend(String friendIpAddress, int friendPort, NodeTalker friendTalker){
        boolean fresh = true;
        for (NodeTalker alreadyTalker : outsideTalkers){
            if (friendTalker == null || alreadyTalker != friendTalker){
                if (alreadyTalker.getTheirIp().equals(friendIpAddress) && alreadyTalker.getTheirPort() == friendPort){
                    fresh = false;
                }
            }
        }
        for (NodeTalker alreadyTalker : insideTalkers){
            if (friendTalker == null || alreadyTalker != friendTalker){
                if (alreadyTalker.getTheirIp().equals(friendIpAddress) && alreadyTalker.getTheirPort() == friendPort){
                    fresh = false;
                }
            }
        }
        System.out.println("nb isNewFriend fresh: " + fresh);
        return fresh;
    }

    public boolean newFriendParty(String friendIp, int friendPort, String networkTypeAsk){
        String myIp;
        int myPort;
        ArrayList<NodeTalker> talkers;
        if (networkTypeAsk.equals("outside")){
            myIp = outsideIp;
            myPort = outsidePort;
            talkers = outsideTalkers;
        }else {
            myIp = insideIp;
            myPort = insidePort;
            talkers = insideTalkers;
        }
//        System.out.println("friendIp: " + friendIp + " friendPort: " + friendPort +
//                " myIp: " + myIp + " myPort: " + myPort);
        if (friendIp.equals(myIp) && friendPort == myPort){
            return false;
        }
        if (friendIp.equals("127.0.0.1") && friendPort == myPort){
            return false;
        }
        for (NodeTalker talker : talkers){
            if (friendIp.equals(talker.getTheirIp()) && friendPort == talker.getTheirPort()){
                return false;
            }
        }

        return true;
    }

    public void removeTalker(NodeTalker talker, String networkTypeAsk, int code){
        System.out.println("nb removeTalker called");
        if (networkTypeAsk.equals("outside")){
            outsideTalkers.remove(talker);
            allTalkers.remove(talker);
            talker.closeConnection(code);
        }else {
            insideTalkers.remove(talker);
            allTalkers.remove(talker);
            talker.closeConnection(code);
        }
    }

    public ArrayList<String> getServerInfo(String networkTypeAsk){
        ArrayList<String> info = new ArrayList<>();
        if (networkTypeAsk.equals("outside")){
            if (outsideServer == null){
                return info;
            }else{
                return outsideServer.getServerInfo();
            }
        }else{
            if (insideServer == null){
                return info;
            }else{
                return insideServer.getServerInfo();
            }
        }

    }

    public boolean callFriend(String friendIp, int friendPort){
        if (isNewFriend(friendIp, friendPort, null) && new MathStuff().isValidPort(friendPort)){
            String type;
            if (new MathStuff().isValidIp(friendIp, "outside") && !networkType.equals("inside")){
                type = "outside";
            }else if (new MathStuff().isValidIp(friendIp, "inside") && !networkType.equals("outside")){
                type = "inside";
            }else {
                return false;
            }
            try {
                new NodeCaller(this, db, friendIp, friendPort, type).start();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }else{
            return false;
        }
    }

    public boolean addReport(String report, String user){
        //check if report is not empty
        if (report.length() == 0){
            return false;
        }
        //construct report
        Tx tx = new Construct(db).constructReportTx(report, user);
        //return false if tx cannot be formed
        if (!tx.isProper()){
            System.out.println("Tx not properly formed");
            return false;
        }
        //return false if not properly entered into the db
        if (!db.addTx(tx)){
            System.out.println("Tx cannot be entered into db");
            return false;
        }
        //send it to others
        propagateTx(tx, null);
        return true;
    }

    public boolean updateMyProfile(String profileReport){
        //check if report is not empty
        if (profileReport.length() == 0){
            return false;
        }
        //construct report
        Tx tx = new Construct(db).constructProfileTx(profileReport, username);
        //return false if tx cannot be formed
        if (!tx.isProper()){
            System.out.println("Tx not properly formed");
            return false;
        }
        //return false if not properly entered into the db
        if (!db.addTx(tx)){
            System.out.println("Tx cannot be entered into db");
            return false;
        }
        //send it to others
        propagateTx(tx, null);
        return true;
    }

    public boolean giveTx(String username, String pubKey, int number) {
        //construct report
        Tx tx = new Construct(db).constructGiveTx(username, pubKey, number);
        //return false if tx cannot be formed
        if (!tx.isProper()){
            System.out.println("Tx not properly formed");
            return false;
        }
        //return false if not properly entered into the db
        if (!db.addTx(tx)){
            System.out.println("Tx cannot be entered into db");
            return false;
        }
        //send it to others
        propagateTx(tx, null);
        return true;
    }


    public void addTx(Tx tx, NodeTalker talker){
        //add tx to the database
        if (db.addTx(tx)){
            //send the tx to nodes other than the one that sent it
            propagateTx(tx, talker);
            //check if tx needs to be displayed
            if (window != null && tx.getType() == 2){
                //get pubkey of sender
                Txi txi = tx.getAllTxi().getAllTxi().get(0);
                String txiHash = txi.getTxiHash();
                int txiTxoIndex = txi.getTxiTxoIndex();
                String tweeterPubKeyHash = db.getPubKeyHashFromTxiHash(txiHash, txiTxoIndex);
                //check if user follows this pubkey
                boolean doIFollow = db.doIFollow(tweeterPubKeyHash, username);
                System.out.println("nb.addFullTweet tweeterHash: " + tweeterPubKeyHash + " doIfollow: " + doIFollow);
                if (doIFollow){
                    //get pubkey user name
                    String name = db.getName(tweeterPubKeyHash);
                    //display report
                    window.addTweet(name, tx.getReport());
                }
            }
        }
    }

    private void propagateTx(Tx tx, NodeTalker talker){
        ArrayList<String> wireReadyTweet = tx.convertForWire();
        for (NodeTalker newGuy : allTalkers){
            if (newGuy != talker){
                newGuy.sendTweet(wireReadyTweet);
            }
        }
    }

    public void addHeardBlock(Block block, NodeTalker talker){
        if (db.addBlock(block)){
            propagateBlock(block, talker);
        }
    }

    public void addMinedBlock(Block block) {
        if (db.addBlock(block)){
            propagateBlock(block, null);
        }
    }

    private void propagateBlock(Block block, NodeTalker talker){
        ArrayList<String> wireBlock = block.convertBlockForWire();
        for (NodeTalker newGuy : allTalkers){
            if (newGuy != talker){
                newGuy.sendNewBlock(wireBlock);
            }
        }
    }

    public void stopMiner(){
        if (miner != null){
            miner.setWait(true);
            System.out.println("nb stopMiner");
        }
    }

    public void startMiner(){
        if (miner != null){
            miner.setWait(false);
            System.out.println("nb startMiner");
        }


    }
    public void updateBaseHeights(int talkerBlockHeight,int otherBlockHeight){
        updateBlockHeight();
        if (blockHeight < talkerBlockHeight){
            blockHeight = talkerBlockHeight;
        }
        if (blockChainHeight < otherBlockHeight){
            blockChainHeight = otherBlockHeight;
        }
    }

    public void decideWhereToStart(ArrayList<ArrayList> pastHeaderHashes, int startingPoint){
        if (!gettingBlocks){
            gettingBlocks = true;
            ArrayList<ArrayList> myHeaderHashes = convertToPastHeaderHashes(db.getPastHeaderHashes(startingPoint));
            ArrayList<String> myHashes = myHeaderHashes.get(1);
            ArrayList<String> myHeights = myHeaderHashes.get(0);
            ArrayList<String> pastHashes = pastHeaderHashes.get(1);
            ArrayList<String> pastHeights = pastHeaderHashes.get(0);
            int i = startingPoint;
            for (String hash : pastHashes){
                if (!myHashes.contains(hash) && (Integer.valueOf(pastHeights.get(pastHashes.indexOf(hash))) < i)){
                    i = Integer.valueOf(pastHeights.get(pastHashes.indexOf(hash)));
                }
            }
            System.out.println("NodeBase decideWhereToStart i: " + i);
            if (i == startingPoint - 10){
                gettingBlocks = false;
                for (NodeTalker talker : allTalkers){
                    talker.getPastTenHeaderHashes(i);
                }
            }else{
                getBlocks(blockChainHeight, i);
            }
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

    public void getBlocks(int otherBlockHeight, int chainIntersection){
        if (blockChainHeight < otherBlockHeight ){
            blockChainHeight = otherBlockHeight;
        }
        System.out.println("nb gettingBlocks: " + gettingBlocks);
        if (!(blockChainHeight > otherBlockHeight)){
            askForBlocks(chainIntersection);
        }else {
            gettingBlocks = false;
        }
    }

    private void askForBlocks(int chainIntersection){
        int heightDifference = blockChainHeight - chainIntersection;
        System.out.println("nb askForBlocks heightDifference: " + heightDifference +
                " allTalkers.size(): " + allTalkers.size());
        for (int i = 0; i <= heightDifference && i <= 10 ; i ++ ){
            if (chainIntersection + i != 0){
                allTalkers.get(i%allTalkers.size()).getBlock(chainIntersection + i);
            }
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                blockHeight = Integer.valueOf(db.getBlockHeight().get(0));
                gettingBlocks = false;
                for (NodeTalker talker : allTalkers){
                    talker.askForBlockHeight();
                }
            }
        }, 1*1*1000);
    }

    public void getMissingTx(ArrayList<String> missingTxList){
        int missingTxTotal = missingTxList.size();
        for (int i = 0; i < missingTxTotal ; i ++ ){
            allTalkers.get(i%allTalkers.size()).getTx(missingTxList.get(i));
        }
        allTalkers.get(0).askForBlockHeight();
    }

    public void changeUsername(String newUsername){
        username = newUsername;
    }

    public void signOut(){
        minerExecutor.shutdownNow();
        stopServer(networkType);
        setWindow(null);
        username = null;
    }

    public void updateMyIp(String newIp, String networkTypeAsk){
        if (networkTypeAsk.equals("outside")){
            outsideIp = newIp;
            for (NodeTalker talker : outsideTalkers){
                talker.setMyIp(outsideIp);
            }
            newServer("outside");
        }else {
            insideIp = newIp;
            for (NodeTalker talker : insideTalkers){
                talker.setMyIp(insideIp);
            }
            newServer("inside");
        }
    }

    public void updateMyPort(int newPort, String networkTypeAsk){
        if (networkTypeAsk.equals("outside")){
            outsidePort = newPort;
            for (NodeTalker talker : outsideTalkers){
                talker.setMyPort(outsidePort);
            }
            newServer("outside");
        }else {
            insidePort = newPort;
            for (NodeTalker talker : insideTalkers){
                talker.setMyPort(insidePort);
            }
            newServer("inside");
        }
    }

    public void updateMyNetName(String newNetName, String networkName){
        if (networkName.equals("outside")){
            outsideNetName = newNetName;
            for (NodeTalker talker : outsideTalkers){
                talker.setMyNetName(outsideNetName);
            }
        }else {
            insideNetName = newNetName;
            for (NodeTalker talker : insideTalkers){
                talker.setMyNetName(insideNetName);
            }
        }
    }

    public void removeNetFriend(Friend friend){
        ArrayList<NodeTalker> remove = new ArrayList<>();
        for (NodeTalker talker : allTalkers){
            if (talker.getTheirIp().equals(friend.getIp()) &&
                    String.valueOf(talker.getTheirPort()).equals(friend.getPort()) &&
                    talker.getTheirNetName().equals(friend.getName())){

                remove.add(talker);
            }
        }
        for (NodeTalker gone: remove){
            removeTalker(gone, gone.getNetworkType(),  1);
        }
        callEnoughFriends();
    }

    public ArrayList<ArrayList<String>> getNetFriends(String networkTypeAsk){
        ArrayList<ArrayList<String>> friends = new ArrayList<>();
        ArrayList<NodeTalker> askTalkers = (networkTypeAsk.equals("outside") ? outsideTalkers : insideTalkers);
        System.out.println("nb getNetFriends talkers: " + askTalkers);
        for (NodeTalker talker : askTalkers){
            ArrayList<String> friend = new ArrayList<>();
            friend.add(talker.getTheirIp());
            friend.add(String.valueOf(talker.getTheirPort()));
            friend.add(talker.getTheirNetName());
            friends.add(friend);
        }
        return friends;
    }


    public void shutdown(){
        for (NodeTalker talker : allTalkers){
            talker.closeConnection(1);
        }
    }

    public int[] getHeights(){
        updateBlockHeight();
        return new int[]{blockHeight, blockChainHeight, allTalkers.size()};
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getNameOfInsideNetwork() {
        return nameOfInsideNetwork;
    }

    public String getOutsideIp() {
        return outsideIp;
    }

    public String getInsideIp() {
        return insideIp;
    }

    public int getOutsidePort() {
        return outsidePort;
    }

    public int getInsidePort() {
        return insidePort;
    }

    public String getOutsideNetName(){
        return outsideNetName;
    }

    public String getInsideNetName(){
        return insideNetName;
    }

    public ArrayList<NodeTalker> getOutsideTalkers() {
        return outsideTalkers;
    }

    public ArrayList<NodeTalker> getInsideTalkers() {
        return insideTalkers;
    }

    public void startUser(String username){
        setUsername(username);
        setIpPort(networkType, true);
        //start contacting others
        callEnoughFriends();
        //start listening for others
        startNewServer(networkType);
        //setup periodic calling of friends
        scheduleNetworkCheck(networkType);
    }

    public int getBlockChainHeight(){
        updateBlockHeight();
        if (allTalkers.size() == 0){
            return blockHeight;
        }else if (blockHeight > blockChainHeight){
            return blockHeight;
        }else {
            return blockChainHeight;
        }
    }

    private void updateBlockHeight(){
        blockHeight = Integer.valueOf(db.getBlockHeight().get(0));
    }
}
