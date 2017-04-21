package Node;

import DB.SQLiteJDBC;
import Mining.Miner;
import ReadWrite.Construct;
import ReadWrite.MathStuff;
import UI.BigWindow;
import UI.Friend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
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

    public NodeBase(SQLiteJDBC db){
        this.db = db;
        this.outsideTalkers = new ArrayList<>();
        this.insideTalkers = new ArrayList<>();
        this.allTalkers = new ArrayList<>();
        this.blockHeight = Integer.valueOf(db.getBlockHeight().get(0));
        this.blockChainHeight = 0;
        this.gettingBlocks = false;
        setIpPort("outside", null);
        callEnoughFriends();
        startNewServer("outside");
        scheduleNetworkCheck();
    }

    public NodeBase(SQLiteJDBC db, String networkType, String insideName){
        this.db = db;
        this.outsideTalkers = new ArrayList<>();
        this.insideTalkers = new ArrayList<>();
        this.allTalkers = new ArrayList<>();
        this.blockHeight = Integer.valueOf(db.getBlockHeight().get(0));
        this.blockChainHeight = 0;
        this.gettingBlocks = false;
        setIpPort(networkType, insideName);
        callEnoughFriends();
        startNewServer(networkType);
        scheduleNetworkCheck();
    }

    private void scheduleNetworkCheck(){
        int initialDelay = ((db.getFirstTime()) ? 5 : 60);

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
        try {
            new NodeParty(this, db).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startNewServer(String whichOne){
        if (whichOne.equals("outside")){
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
            startNewServer("outside");
            startNewServer("inside");
        }

    }

    private void stopServer(String networkTypeAsk){
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

    public void newServer(String networkTypeAsk){
        stopServer(networkTypeAsk);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startNewServer(networkTypeAsk);
            }
        }, 2*1000);
    }


    public void serverFailed(String nameOfNetwork){
        stopServer(nameOfNetwork);
    }

    public void setIpPort(String networkType, String insideName){
        this.networkType = networkType;
        this.nameOfInsideNetwork = insideName;

        if (username == null){
            outsideIp = "NA";
            outsidePort = 0;
            insideNetName = "NA";
            insideIp = "NA";
            insidePort = 0;
            outsideNetName = "NA";
        }else if (networkType.equals("outside")){
            ArrayList<String> ipPort = db.getIpPort(username, "outside");
            if (ipPort.size() == 3){
                outsideIp = ipPort.get(0);
                outsidePort = Integer.valueOf(ipPort.get(1));
                outsideNetName = ipPort.get(2);
            }
        }else if (networkType.equals("inside")){
            ArrayList<String> ipPort = db.getIpPort(username, nameOfInsideNetwork);
            if (ipPort.size() == 3){
                insideIp = ipPort.get(0);
                insidePort = Integer.valueOf(ipPort.get(1));
                insideNetName = ipPort.get(2);
            }
        } else {
            ArrayList<String> ipPort = db.getIpPort(username, "outside");
            if (ipPort.size() == 3){
                outsideIp = ipPort.get(0);
                outsidePort = Integer.valueOf(ipPort.get(1));
                outsideNetName = ipPort.get(2);
            }
            ArrayList<String> ipPort2 = db.getIpPort(username, nameOfInsideNetwork);
            if (ipPort2.size() == 3){
                insideIp = ipPort2.get(0);
                insidePort = Integer.valueOf(ipPort2.get(1));
                insideNetName = ipPort2.get(2);
            }
        }
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void updateTalkersUser(String networkTypeAsk){
        if (networkTypeAsk.equals("outside")){
            System.out.println("nb updateTalkersUser outsideTalkers.size(): " + outsideTalkers.size());
            for (NodeTalker talker : outsideTalkers){
                talker.updateUser(outsideIp, outsidePort, outsideNetName);
            }
        }else if (networkTypeAsk.equals("inside")){
            System.out.println("nb updateTalkersUser insideTalkers.size(): " + insideTalkers.size());
            for (NodeTalker talker : insideTalkers){
                talker.updateUser(insideIp, insidePort, insideNetName);
            }
        }else{
            updateTalkersUser("outside");
            updateTalkersUser("inside");
        }
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

    public boolean addTweet(String tweet, String user, NodeTalker talker){
        //TODO construct tweet, verify, display
        if (tweet.length() == 0){
            return false;
        }
        ArrayList<ArrayList> fullTweet = new Construct(db).constructSimpleTx(user, tweet, "2");
        System.out.println("NodeBase addTweet() fullTweet: " + fullTweet);
        if (fullTweet.isEmpty()){
            return false;
        }
        boolean success = db.addFullTweet(fullTweet);
        System.out.println("addTweet success = " + success);
        if (!success){
            return false;
        }else{
            propagateTweet(fullTweet, talker);
            System.out.println("nb addTweet proptweet");
            return true;
        }
    }

    public void addFullTweet(ArrayList<ArrayList> fullTweet, NodeTalker talker){
        System.out.println("nb.addFullTweet fullTweet: " + fullTweet);
        if (db.addFullTweet(fullTweet)){
            propagateTweet(fullTweet, talker);
            if (window != null && fullTweet.get(0).get(1).equals("2")){
                String txiHash = ((ArrayList<String>)fullTweet.get(2).get(0)).get(1);
                String txiTxoIndex = ((ArrayList<String>)fullTweet.get(2).get(0)).get(2);
                String tweeterPubKeyHash = db.getPubKeyHashFromTxiHash(txiHash, txiTxoIndex);
                boolean doIFollow = db.doIFollow(tweeterPubKeyHash, username);
                System.out.println("nb.addFullTweet tweeterHash: " + tweeterPubKeyHash + " doIfollow: " + doIFollow);
                if (doIFollow){
                    String name = db.getName(tweeterPubKeyHash);
                    window.addTweet(name, (String)fullTweet.get(0).get(4));
                }
            }
        }
    }

    public void addFullGiveTweet(ArrayList<ArrayList> fullTweet){
        db.addFullTweet(fullTweet);
    }

    private void propagateTweet(ArrayList<ArrayList> fullTweet, NodeTalker talker){
        ArrayList<String> wireReadyTweet = convertFullTweetForWire(fullTweet);
        for (NodeTalker newGuy : allTalkers){
            if (newGuy != talker){
                newGuy.sendTweet(wireReadyTweet);
            }
        }
    }

    public ArrayList<String> convertFullTweetForWire(ArrayList<ArrayList> oldFullTweet){
        System.out.println("NodeBase convertFullTweetForWire oldFullTweet: " + oldFullTweet);
        ArrayList<String> wireFullTweet = new ArrayList<>();
        ArrayList<String> tx = oldFullTweet.get(0);
        ArrayList<ArrayList> txo = oldFullTweet.get(1);

        wireFullTweet.addAll(Arrays.asList(tx.get(0), tx.get(1), tx.get(2), tx.get(3), tx.get(4), tx.get(5)));
        wireFullTweet.add(String.valueOf(txo.size()));

        for (ArrayList<String> txoInstance : txo){
            wireFullTweet.add(txoInstance.get(0));
            wireFullTweet.add(txoInstance.get(1));
            wireFullTweet.add(txoInstance.get(2));
        }

        if (!tx.get(1).equals("1")){
            ArrayList<ArrayList> txi = oldFullTweet.get(2);
            wireFullTweet.add(String.valueOf(txi.size()));
            for (ArrayList<String> txiInstance : txi){
                wireFullTweet.add(txiInstance.get(0));
                wireFullTweet.add(txiInstance.get(1));
                wireFullTweet.add(txiInstance.get(2));
            }

        }
        return wireFullTweet;
    }

    public void addHeardBlock(ArrayList<ArrayList> fullBlock, NodeTalker talker){
        System.out.println("NodeBase addHeardBlock fullBlock: " + fullBlock);
        if (db.addFullBlock(fullBlock)){
            propagateBlock(fullBlock, talker);
        }
    }

    public void addHeardGiveBlock(ArrayList<ArrayList> fullBlock){
        System.out.println("NodeBase addHeardBlock fullBlock: " + fullBlock);
        db.addFullBlock(fullBlock);
    }


    public void addMinedBlock(ArrayList<ArrayList> fullBlock){
        System.out.println("NodeBase addMinedBlock fullBlock: " + fullBlock);
        if (db.addFullBlock(fullBlock)){
            propagateBlock(fullBlock, null);
        }
    }

    private void propagateBlock(ArrayList<ArrayList> fullBlock, NodeTalker talker){
        //TODO make this
        ArrayList<String> wireBlock = convertFullBlockForWire(fullBlock);

        for (NodeTalker newGuy : allTalkers){
            if (newGuy != talker){
                newGuy.sendNewBlock(wireBlock);
            }
        }
    }

    public ArrayList<String> convertFullBlockForWire(ArrayList<ArrayList> fullBlock){
        ArrayList<String> header = fullBlock.get(0);
        ArrayList<ArrayList> tweetBase = fullBlock.get(1);
        ArrayList<String> tx = tweetBase.get(0);
        ArrayList<String> txo = (ArrayList<String>)tweetBase.get(1).get(0);
        ArrayList<String> wireBlock = new ArrayList<>();
        wireBlock.addAll(header);
        wireBlock.addAll(tx);
        wireBlock.addAll(txo);
        if (fullBlock.size() == 3 && !fullBlock.get(2).isEmpty()){
            ArrayList<String> txList = fullBlock.get(2);
            wireBlock.addAll(txList);
        }
        return wireBlock;
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

    public boolean updateMyProfile(String profileTweet){
        ArrayList<ArrayList> fullTweet = new Construct(db).constructSimpleTx(username, profileTweet, "3");
        System.out.println("NodeBase addProfileTweet() fullTweet: " + fullTweet);
        if (fullTweet.isEmpty()){
            return false;
        }
        boolean success = db.addFullTweet(fullTweet);
        System.out.println("updateMyProfile success = " + success);
        if (!success){
            return false;
        }else{
            propagateTweet(fullTweet, null);
            System.out.println("nb updateMyProfile proptweet");
            return true;
        }
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

    public ArrayList<ArrayList> getNetFriends(String networkTypeAsk){
        ArrayList<ArrayList> friends = new ArrayList<>();
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

    public boolean giveTx(String username, String pubKey, String number) {
        ArrayList<ArrayList> fullTweet = new Construct(db).constructGiveTx(username, pubKey, number);
        System.out.println("NodeBase giveTx fullTweet: " + fullTweet);
        if (fullTweet.isEmpty()){
            return false;
        }
        boolean success = db.addFullTweet(fullTweet);
        System.out.println("giveTx success = " + success);
        if (!success){
            return false;
        }else{
            propagateTweet(fullTweet, null);
            System.out.println("nb giveTx proptweet");
            return true;
        }
    }

    public void shutdown(){
        for (NodeTalker talker : allTalkers){
            talker.closeConnection(1);
        }
    }

    public int[] getHeights(){
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
        setIpPort(networkType, nameOfInsideNetwork);
        startNewServer(networkType);
        startNewMiner();
        updateTalkersUser(networkType);
    }

    public int getBlockChainHeight(){
        if (allTalkers.size() == 0){
            return blockHeight;
        }else if (blockHeight > blockChainHeight){
            return blockHeight;
        }else {
            return blockChainHeight;
        }
    }
}
