package ReadWrite;

import DB.SQLiteJDBC;
import Structures.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

public class Construct {
    private SQLiteJDBC db;
    private int txMaxInBlock = 100;
    public Construct(SQLiteJDBC db){
        this.db = db;
        //TODO create tweet and block objects, stop using a bunch of lists inside of lists
    }

//    public ArrayList<ArrayList> constructBlockToMine(String pubKey){
//        //construct partial block needed for mining
//
//        //get needed info
//        ArrayList<String> blockInfo = db.getMiningInfo();
//        ArrayList<String> txToAddArray = createTxToAddArray(blockInfo);
//        //construct the tweet base tx
//        String numberToAddTotal = txToAddArray.get(txToAddArray.size()-1);
//        txToAddArray.remove(txToAddArray.size()-1);
//        ArrayList<ArrayList> tweetBaseTx = constructTweetBaseTx(pubKey, numberToAddTotal);
//        //construct partial header
//        String merkleRoot = new MathStuff().createBlockMerkleRoot(txToAddArray, (String)tweetBaseTx.get(0).get(0));
//        String height = String.valueOf(Integer.valueOf(blockInfo.get(0)) + 1);
//        String previousHash = blockInfo.get(1);
//        ArrayList<String> header = new ArrayList<>();
//        String target =  calculateTarget(blockInfo, Integer.valueOf(height));
//        header.addAll(Arrays.asList(height, merkleRoot, previousHash, target));
//        //add all together
//        ArrayList<ArrayList> total = new ArrayList<>();
//        total.addAll(Arrays.asList(header, tweetBaseTx, txToAddArray));
//        System.out.println("Construct block to mine total: " + total);
//        return total;
//    }

    public Block constructNewMinerBlock(String pubKey){
        //get txs not yet in a block
        AllTx allTx = db.getNewMinerBlockAllTx();
        //count up the reward for this block + standard reward
        int reward = allTx.calcBlockReward() + getMinerReward();
        //create JouleBase tx for this block
        JouleBase base = constructJouleBase(pubKey, reward);
        //get header of highest block;
        Header pastHeader = db.getHighestHeader();
        //use that to create outline of new header
        Header header = new Header(pastHeader);
        //add merkleRoot to Header
        header.setMerkleRoot(allTx.calcMerkleRoot(base.getHash()));
        //change target at intervals
        if (header.getHeight()%100 == 0){
            header.resetTarget(db);
        }
        //calculate header hash
        header.calculateHash();
        //add together to form new Block
        Block block = new Block(header, base, allTx);
        block.printBlock();
        return block;
    }

    private String calculateTarget(ArrayList<String> blockInfo, int height){
        //TODO maybe put in mathstuff??
        if ((height%100) != 0){
            System.out.println("Construct calc target mod not 100");
            return blockInfo.get(2);
        }else {
            //use difference in timestamps over past 100 blocks to calculate new mining target
            BigInteger time1 = BigInteger.valueOf(db.getTimeOfPastBlock(blockInfo, 0));
            BigInteger time100 = BigInteger.valueOf(db.getTimeOfPastBlock(blockInfo, 99));
            BigInteger timeDifference = time1.subtract(time100);
            //10 minute goal time per block
            BigInteger goalTime = BigInteger.valueOf(99*10*60*1000);
            BigInteger oldTarget = new BigInteger(blockInfo.get(2),16);
            BigInteger holdingPattern = timeDifference.multiply(oldTarget);
            BigInteger newTarget = holdingPattern.divide(goalTime);
            String output = newTarget.toString(16);
            System.out.println("construct calcTarget output: " + output + " time1:" + time1 + " time100: " + time100 +
                    " difference: " + timeDifference + " goaltime: " + goalTime);
            //make sure target is 64 digits long with at least 2 leading zeros
//            if (output.length() > 64 || output.length() == 0 || (output.length() == 64 && (output.charAt(0) != '0' ||
//                    output.charAt(1) != '0')) || (output.length() == 63 && output.charAt(0) != '0')){
//                System.out.println("construct calcTarget output too big");
//                return "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
//            }
            if (output.length() > 62){
                System.out.println("construct calcTarget output too big");
                return "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
            }
            while (output.length() < 64){
                output = "0"+output;
            }
            return output;
        }
    }

    private ArrayList<String> createTxToAddArray(ArrayList<String> blockInfo){
        //collect txs to add into the block you are making, add mining reward information at end
        System.out.println("Construct createTxToAddArray started");
        ArrayList<String> txToAddFromDb = db.getTxToAddToBlock();
        System.out.println("Construct createTxToAddArray txToAddFromDb: " + txToAddFromDb);
        ArrayList<String> txToAddArray = new ArrayList<>();
        int i = 0;
        int numberToMinerTotal = 0;
        int numberAdded = 0;

        while(numberAdded <= txMaxInBlock && i < txToAddFromDb.size()){
            String txHash = txToAddFromDb.get(i);
            if (db.isTxAcceptableForBlock(txHash, blockInfo.get(1))){
                txToAddArray.add(txHash);
                int numberToMiner = Integer.valueOf(txToAddFromDb.get(i + 1));
                numberToMinerTotal = numberToMinerTotal + numberToMiner;
                numberAdded ++;
            }
            i += 2;
        }

        txToAddArray.add(String.valueOf(numberToMinerTotal));
        return txToAddArray;
    }

//    public ArrayList<ArrayList> constructTweetBaseTx(String pubKey, String numberToMinerTotal){
//        //construct a tweet base tx with all of its component parts
//        ArrayList<ArrayList> fullTweet = new ArrayList<>();
//        ArrayList<String> tx = new ArrayList<>();
//        ArrayList<ArrayList> txo = new ArrayList<>();
//        ArrayList<String> txoInstance = new ArrayList<>();
//
//        //add time and default values to tx
//        long time = new java.util.Date().getTime();
//        String hash = new MathStuff().createHash("1" + "0" + "0" + String.valueOf(time) + "0");
//        tx.addAll(Arrays.asList(hash, "1", "0", "0", String.valueOf(time), "0"));
//        fullTweet.add(tx);
//
//        //add reward, tx position number, and lock to txo
//        String minerTotal = String.valueOf(Integer.valueOf(numberToMinerTotal) + getMinerReward());
//        txoInstance.addAll(Arrays.asList(minerTotal, "0", pubKey));
//        txo.add(txoInstance);
//        fullTweet.add(txo);
//
//        return fullTweet;
//    }

    private JouleBase constructJouleBase(String pubKey, int reward){
        //create the jouleBase tx
        long time = new java.util.Date().getTime();
        JouleBase base = new JouleBase(time);
        //add the txo to the tx
        base.addTxo(0, reward, pubKey);
        return base;
    }

    private int getMinerReward(){
        //TODO make this respond to inflation in some way, grow with demand, usage. same as in verify
        return 25;
    }

    public Tx constructProfileTx(String profile, String user){
        //start with finding tx whose txo can spent as a txi
        AllTxi allTxi = db.getNewReportAllTxi(user);
        //return marked tx if there are no txo to spend
        if (allTxi.getAllTxi().isEmpty()){
            System.out.println("Not enough joules to spend");
            return new Tx(false);
        }
        //construct allTxo
        AllTxo allTxo = new AllTxo();
        int change = allTxi.getChange();
        if (change > 0){
            Txo txo = new Txo(0, change, allTxi.getPubKey());
            allTxo.addTxo(txo);
        }
        //construct tx
        Tx tx = new Tx(allTxi, allTxo, profile, 3);
        if (!new Verify(db).verifyTx(tx)){
            return new Tx(false);
        }
        return tx;
    }

    public Tx constructReportTx(String report, String user){
        //start with finding tx whose txo can spent as a txi
        AllTxi allTxi = db.getNewReportAllTxi(user);
        //return marked tx if there are no txo to spend
        if (allTxi.getAllTxi().isEmpty()){
            System.out.println("Not enough joules to spend");
            return new Tx(false);
        }
        //construct allTxo
        AllTxo allTxo = new AllTxo();
        int change = allTxi.getChange();
        if (change > 0){
            Txo txo = new Txo(0, change, allTxi.getPubKey());
            allTxo.addTxo(txo);
        }
        //construct tx
        Tx tx = new Tx(allTxi, allTxo, report);
        if (!new Verify(db).verifyTx(tx)){
            return new Tx(false);
        }
        return tx;
    }

    public Tx constructGiveTx(String username, String pubKey, int number) {
        //start with finding tx whose txo can spend as a txi
        AllTxi allTxi = db.getNewGiveAllTxi(username, number);
        //return marked tx if there are not enough joules to spend
        if (allTxi.getAllTxi().isEmpty()){
            System.out.println("Not enough joules to spend");
            return new Tx(false);
        }
        //construct allTxo
        AllTxo allTxo = new AllTxo();
        //add give txo
        if (pubKey.length() == 64){
            pubKey = db.getPubKeyFromHash(pubKey);
        }
        if (pubKey.equals("NA")){
            return new Tx(false);
        }
        Txo txo = new Txo(0, number, pubKey);
        allTxo.addTxo(txo);
        //add change txo
        int change = allTxi.getChange() - number;
        if (change > 0){
            Txo txoChange = new Txo(1, change, allTxi.getPubKey());
            allTxo.addTxo(txoChange);
        }else if (change < 0){
            return new Tx(false);
        }
        //construct tx
        Tx tx = new Tx(allTxi, allTxo, 4);
        if (!new Verify(db).verifyTx(tx)){
            return new Tx(false);
        }
        return tx;
    }

    public ArrayList<ArrayList> constructSimpleTx(String username, String tweet, String type){
        //constructs the tx used for a standard tweet and a profile tweet
        ArrayList<ArrayList> fullTweet = new ArrayList<>();
        ArrayList<String> tx = new ArrayList<>();
        ArrayList<ArrayList> txoList = new ArrayList<>();
        ArrayList<ArrayList> txiList;

        //find tx whose txo I can now spend as a txi
        ArrayList<ArrayList> spendableTx = getSpendableTx(username);
        if (spendableTx.isEmpty()){
            System.out.println("constructSimpleTx no spendable tx");
            return fullTweet;
        }

        //convert those txo to txi
        txiList = createTxi(spendableTx);

        ArrayList<String> userInfo = getUser(username);
        ArrayList<String> change = calculateChange(spendableTx, userInfo);
        ArrayList<String> txoInstance = new ArrayList<>();
        txoInstance.addAll(Arrays.asList(change.get(1), "0", change.get(2)));
        txoList.add(txoInstance);

        String txiMerkle = new MathStuff().createTxMerkleRoot(txiList);
        String unlock = unlockTxi(txiMerkle, userInfo);
        String tweetLength = String.valueOf(tweet.length());
        String txHash = new MathStuff().createHash(type + unlock + tweetLength + tweet + change.get(0));
        tx.addAll(Arrays.asList(txHash, type, unlock, tweetLength, tweet, change.get(0)));

        fullTweet.add(tx);
        fullTweet.add(txoList);
        fullTweet.add(txiList);
        if (new Verify(db).isTweetVerified(fullTweet)){
            return fullTweet;
        }else {
            fullTweet.clear();
            return fullTweet;
        }
    }

    private ArrayList<String> calculateChange(ArrayList<ArrayList> spendableTx, ArrayList<String> userInfo){
        ArrayList<String> change = new ArrayList<>();
        String numberToMiner = userInfo.get(6);
        int intToMiner = Integer.valueOf(numberToMiner);
        change.add(numberToMiner);
        System.out.println("Construct calc change spendableTx: " + spendableTx);
        int changeNumber = 0;

        for (ArrayList<String> txo : spendableTx){
            changeNumber = changeNumber + Integer.valueOf(txo.get(1));
        }
        changeNumber = changeNumber - intToMiner;
        change.add(String.valueOf(changeNumber));

        if (change.get(1).equals("0")){
            change.add("0");
        }else {
            change.add(String.valueOf(userInfo.get(3)));
        }
        return change;
    }

    private ArrayList<ArrayList> createTxi(ArrayList<ArrayList> spendableTx){
        int number = 0;
        ArrayList<ArrayList> txi = new ArrayList<>();

        if (spendableTx.isEmpty()){
            return txi;
        }

        for (ArrayList<String> txo : spendableTx){
            String txiHash = txo.get(0);
            String txiIndex = String.valueOf(number);
            String txiTxoIndex = txo.get(2);
            ArrayList<String> txiInstance = new ArrayList<>();
            txiInstance.add(txiIndex);
            txiInstance.add(txiHash);
            txiInstance.add(txiTxoIndex);
            txi.add(txiInstance);
            number++;
        }

        return txi;
    }

    private String unlockTxi(String merkle, ArrayList<String> userInfo){
        String privKey = userInfo.get(4);
        return new MathStuff().signTx(privKey, merkle);
    }

    private ArrayList<ArrayList> getSpendableTx(String username){
        //
        ArrayList<String> userInfo = db.getUserInfo(username);
        String pubKeyHash = userInfo.get(5);
        String txPerTweet = userInfo.get(6);

        ArrayList<String> miningInfo = db.getMiningInfo();
        ArrayList<ArrayList> spendableTx = db.getSpendableTxo(pubKeyHash, txPerTweet, miningInfo);

        return spendableTx;
    }

    private ArrayList<ArrayList> getGiveSpendableTx(String username, String number){
        ArrayList<String> userInfo = db.getUserInfo(username);
        String pubKeyHash = userInfo.get(5);
        String txPerTweet = userInfo.get(6);

        ArrayList<String> miningInfo = db.getMiningInfo();
        ArrayList<ArrayList> spendableTx = db.getSpendableGiveTxo(pubKeyHash, txPerTweet, miningInfo, number);

        return spendableTx;
    }

    private ArrayList<String> getUser(String username){
        return db.getUserInfo(username);
    }

    public ArrayList<ArrayList> constructGiveTxold(String username, String pubKey, String number) {
        ArrayList<ArrayList> fullTweet = new ArrayList<>();
        if (pubKey.length() == 64){
            pubKey = db.getPubKeyFromHash(pubKey);
        }
        if (!new MathStuff().isNumber(number)){
            return fullTweet;
        }
        ArrayList<String> tx = new ArrayList<>();
        ArrayList<ArrayList> txoList = new ArrayList<>();
        ArrayList<ArrayList> txiList;

        ArrayList<ArrayList> spendableTx = getGiveSpendableTx(username, number);
        if (spendableTx.isEmpty()){
            System.out.println("constructSimpleTx no spendable tx");
            return fullTweet;
        }

        txiList = createTxi(spendableTx);

        ArrayList<String> userInfo = getUser(username);
        ArrayList<String> change = calculateGiveChange(spendableTx, userInfo, pubKey, number);
        ArrayList<String> txoChangeInstance = new ArrayList<>();
        txoChangeInstance.addAll(Arrays.asList(change.get(1), "0", change.get(2)));
        txoList.add(txoChangeInstance);
        ArrayList<String> txoGiveInstance = new ArrayList<>();
        txoGiveInstance.addAll(Arrays.asList(change.get(3), "1", change.get(4)));
        txoList.add(txoGiveInstance);

        String type = "4";
        String zero = "0";
        String txiMerkle = new MathStuff().createTxMerkleRoot(txiList);
        String unlock = unlockTxi(txiMerkle, userInfo);
        String txHash = new MathStuff().createHash(type + unlock + zero + zero + change.get(0));
        tx.addAll(Arrays.asList(txHash, type, unlock, zero, zero, change.get(0)));

        fullTweet.add(tx);
        fullTweet.add(txoList);
        fullTweet.add(txiList);
        if (new Verify(db).isTweetVerified(fullTweet)){
            return fullTweet;
        }else {
            fullTweet.clear();
            return fullTweet;
        }
    }

    private ArrayList<String> calculateGiveChange(ArrayList<ArrayList> spendableTx, ArrayList<String> userInfo,
                                                  String givePubKey, String number) {
        ArrayList<String> change = new ArrayList<>();
        String numberToMiner = userInfo.get(6);
        int intToMiner = Integer.valueOf(numberToMiner);
        change.add(numberToMiner);
        System.out.println("Construct calc change spendableTx: " + spendableTx);
        int totalNumber = 0;

        for (ArrayList<String> txo : spendableTx){
            totalNumber = totalNumber + Integer.valueOf(txo.get(1));
        }
        int changeNumber = totalNumber - intToMiner - Integer.valueOf(number);
        change.add(String.valueOf(changeNumber));

        if (change.get(1).equals("0")){
            change.add("0");
        }else {
            change.add(String.valueOf(userInfo.get(3)));
        }
        change.add(number);
        change.add(givePubKey);

        return change;
    }
}
