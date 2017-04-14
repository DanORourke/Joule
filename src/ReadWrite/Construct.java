package ReadWrite;

import DB.SQLiteJDBC;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

public class Construct {
    private SQLiteJDBC db;
    private int txMaxInBlock = 100;
    public Construct(SQLiteJDBC db){
        this.db = db;
        //TODO convert stored keys to something more manageable, different format.
        //TODO pressing enter in the tweet breaks everything, fix that
    }

    public ArrayList<ArrayList> constructBlockToMine(String pubKey){

        ArrayList<String> blockInfo = db.getMiningInfo();
        ArrayList<String> txToAddArray = createTxToAddArray(blockInfo);

        String numberToAddTotal = txToAddArray.get(txToAddArray.size()-1);
        txToAddArray.remove(txToAddArray.size()-1);
        ArrayList<ArrayList> tweetBaseTx = constructTweetBaseTx(pubKey, numberToAddTotal);

        String merkleRoot = new MathStuff().createBlockMerkleRoot(txToAddArray, (String)tweetBaseTx.get(0).get(0));
        String height = String.valueOf(Integer.valueOf(blockInfo.get(0)) + 1);
        String previousHash = blockInfo.get(1);
        ArrayList<String> header = new ArrayList<>();
        String target =  calculateTarget(blockInfo, Integer.valueOf(height));
        header.addAll(Arrays.asList(height, merkleRoot, previousHash, target));

        ArrayList<ArrayList> total = new ArrayList<>();
        total.addAll(Arrays.asList(header, tweetBaseTx, txToAddArray));
        System.out.println("Construct block to mine total: " + total);
        return total;
    }

    private String calculateTarget(ArrayList<String> blockInfo, int height){
        if ((height%100) != 0){
            System.out.println("Construct calc target mod not 100");
            return blockInfo.get(2);
        }else {
            BigInteger time1 = BigInteger.valueOf(db.getTimeOfPastBlock(blockInfo, 0));
            BigInteger time100 = BigInteger.valueOf(db.getTimeOfPastBlock(blockInfo, 99));
            BigInteger timeDifference = time1.subtract(time100);
            //10 minute goal time per block
            BigInteger goalTime = BigInteger.valueOf(99*10*60*1000);
            BigInteger oldTarget = new BigInteger(blockInfo.get(2),16);
            BigInteger holdingPattern = oldTarget.multiply(timeDifference);
            BigInteger newTarget = holdingPattern.divide(goalTime);
            String output = newTarget.toString(16);
            System.out.println("construct calcTarget output: " + output + " time1:" + time1 + " time100: " + time100 +
                    " difference: " + timeDifference + " goaltime: " + goalTime);

            if (output.length() > 64 || output.length() == 0 || (output.length() == 64 && (output.charAt(0) != '0' ||
                    output.charAt(1) != '0'))){
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

    public ArrayList<ArrayList> constructTweetBaseTx(String pubKey, String numberToMinerTotal){
        ArrayList<ArrayList> fullTweet = new ArrayList<>();
        ArrayList<String> tx = new ArrayList<>();
        ArrayList<ArrayList> txo = new ArrayList<>();
        ArrayList<String> txoInstance = new ArrayList<>();

        long time = new java.util.Date().getTime();
        String hash = new MathStuff().createHash("1" + "0" + "0" + String.valueOf(time) + "0");
        tx.addAll(Arrays.asList(hash, "1", "0", "0", String.valueOf(time), "0"));
        fullTweet.add(tx);

        String minerTotal = String.valueOf(Integer.valueOf(numberToMinerTotal) + getMinerReward());
        txoInstance.addAll(Arrays.asList(minerTotal, "0", pubKey));
        txo.add(txoInstance);
        fullTweet.add(txo);

        return fullTweet;
    }

    private int getMinerReward(){
        //TODO make official, also same as in verifyTweet
        return 25;
    }

    public ArrayList<ArrayList> constructSimpleTx(String username, String tweet, String type){
        ArrayList<ArrayList> fullTweet = new ArrayList<>();
        ArrayList<String> tx = new ArrayList<>();
        ArrayList<ArrayList> txoList = new ArrayList<>();
        ArrayList<ArrayList> txiList;

        ArrayList<ArrayList> spendableTx = getSpendableTx(username);
        if (spendableTx.isEmpty()){
            System.out.println("constructSimpleTx no spendable tx");
            return fullTweet;
        }

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
        if (spendableTx.isEmpty()){
            return spendableTx;
        }
        int number = 0;
        ArrayList<ArrayList> txi = new ArrayList<>();

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
        return new MathStuff().unlockTxo(privKey, merkle);
    }

    private ArrayList<ArrayList> getSpendableTx(String username){
        //TODO update this, make a single call
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

    public ArrayList<ArrayList> constructGiveTx(String username, String pubKey, String number) {
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
