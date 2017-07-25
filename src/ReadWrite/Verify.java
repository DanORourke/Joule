package ReadWrite;

import DB.SQLiteJDBC;
import Node.NodeBase;
import Structures.*;

import java.util.ArrayList;

public class Verify {
    private SQLiteJDBC db;

    public Verify(SQLiteJDBC db){
        this.db = db;
    }

    public boolean isTweetVerified(ArrayList<ArrayList> fullTweet){
        System.out.println("Verify isTweetVerified fullTweet: " + fullTweet);
        if (fullTweet.isEmpty()){
            return false;
        }
        ArrayList<String> tx = fullTweet.get(0);
        ArrayList<ArrayList> txoList = fullTweet.get(1);
        String type = tx.get(1);
        if (type.equals("1")){
            //should not get here, only check tweetbase when checking a block
            return verifyTweetBaseTxFromFullTweet(tx, txoList);
        }else{
            ArrayList<ArrayList> txiList = fullTweet.get(2);
            return verifyFullTweet(tx, txoList, txiList);
        }
    }

    private boolean verifyMostTweetBase(ArrayList<String> tx){
        String hash = tx.get(0);
        String type = tx.get(1);
        String unlock = tx.get(2);
        String twtLength = tx.get(3);
        String twt = tx.get(4);
        String numberToMine = tx.get(5);

        if (!hash.equals(new MathStuff().createHash(type + unlock + twtLength + twt +
                numberToMine))){
            return false;
        }if (!type.equals("1")){
            return false;
        }
        if (!unlock.equals("0")){
            return false;
        }
        if (!twtLength.equals("0")){
            return false;
        }
        //TODO make sure twt is a time and it is accurate enough, no funny business going on
        if (!numberToMine.equals("0")){
            return false;
        }

        return true;
    }

    private boolean verifyTweetBaseTxFromFullTweet(ArrayList<String> tx, ArrayList<ArrayList> txoList) {
        ArrayList<String> txo = txoList.get(0);
        boolean verified = verifyMostTweetBase(tx);
        String numberToMine = txo.get(0);
        if (!verifyMinerRewardFromFullTweet(numberToMine)){
            verified = false;
        }
        return verified;
    }

    public boolean verifyTx(Tx tx){
        boolean good  = verifyAllTxi(tx) && verifyAllTxo(tx) && verifyTxHead(tx);
        System.out.println("verify Tx: " + good);
        return good;
    }

    private boolean verifyTxHead(Tx tx){
        boolean good = tx.isHeadVerified();
        System.out.println("verify TxHead: " + good);
        return good;
    }

    private boolean verifyAllTxo(Tx tx){
        AllTxo allTxo = tx.getAllTxo();
        boolean good = allTxo.isVerified();
        System.out.println("verify AllTxo: " + good);
        return good;
    }

    private boolean verifyAllTxi(Tx tx){
        AllTxi allTxi = tx.getAllTxi();
        boolean good = !allTxi.isEmpty();
        boolean gooder = db.verifyAllTxi(allTxi);
        System.out.println("verify AllTxi: " + (good && gooder) + " !isEmpty: " + good + " dbcheck: " + gooder);
        return good;
    }

    private boolean verifyFullTweet(ArrayList<String> tx, ArrayList<ArrayList> txoList, ArrayList<ArrayList> txiList) {
        ArrayList<ArrayList> allTxiTxo = db.getAllTxiTxo(txiList);
        if (allTxiTxo.isEmpty()){
            System.out.println("VerifyFullTweetTx allTxiTxo empty");
            return false;
        }

        if (!verifyFullTweetTx(tx, txoList, txiList, allTxiTxo)) {
            System.out.println("VerifyFullTweetTx verify: " + false);
            return false;
        }
        if (!verifyFullTweetTxoList(tx, txoList, txiList, allTxiTxo)) {
            System.out.println("VerifyFullTweetTxoList verify: " + false);
            return false;
        }
        if (!verifyFullTweetTxiList(tx, txoList, txiList, allTxiTxo)) {
            System.out.println("VerifyFullTweetTxiList verify: " + false);
            return false;
        }
        System.out.println("VerifyFullTweet verify: " + true);
        return true;
    }

    private boolean verifyFullTweetTx (ArrayList<String> tx, ArrayList<ArrayList> txoList,
                                       ArrayList<ArrayList> txiList, ArrayList<ArrayList> allTxiTxo){

        String hash = tx.get(0);
        String type = tx.get(1);
        String unlock = tx.get(2);
        String twtLength = tx.get(3);
        String twt = tx.get(4);
        String numberToMiner = tx.get(5);

        if (!hash.equals(new MathStuff().createHash(type + unlock + twtLength + twt + numberToMiner))){
            return false;
        }
        if (type.equals("2") || type.equals("3")){
            if (!new MathStuff().isNumber(twtLength) || Integer.valueOf(twtLength) != twt.length()){
                return false;
            }
        }
        if (type.equals("4") && (!twtLength.equals("0") || !twt.equals("0"))){
            return false;
        }

        if (!new MathStuff().isNumber(numberToMiner)){
            return false;
        }
        String txiMerkle = new MathStuff().createTxMerkleRoot(txiList);
        String pubKey = db.getPubKeyFromHash((String)allTxiTxo.get(0).get(3));

        if (!new MathStuff().testSig(pubKey, unlock, txiMerkle)){
            return false;
        }

        return true;
    }

    private boolean verifyFullTweetTxoList (ArrayList<String> tx, ArrayList<ArrayList> txoList,
                                       ArrayList<ArrayList> txiList, ArrayList<ArrayList> allTxiTxo){

        if (!txoIndexesDifferentNumbers(txoList)){
            return false;
        }

        int numberOfTxSpendable = getNumberOfSpendable(allTxiTxo);
        int numberSpent = getNumberSpent(tx, txoList);
        if (numberOfTxSpendable != numberSpent){
            return false;
        }

        return true;
    }

    private boolean verifyFullTweetTxiList (ArrayList<String> tx, ArrayList<ArrayList> txoList,
                                       ArrayList<ArrayList> txiList, ArrayList<ArrayList> allTxiTxo){
        //check all pubkeys are the same
        //check all txi actually exist and have not been used by another//done while getting alltxitxo

        if (!samePubKeys(allTxiTxo)){
            return false;
        }


        return true;
    }

    private boolean txoIndexesDifferentNumbers(ArrayList<ArrayList> txoList){
        ArrayList<String> indexes = new ArrayList<>();
        for (ArrayList<String> txo : txoList){
            String index = txo.get(1);
            if (!new MathStuff().isNumber(index)){
                return false;
            }
            if (indexes.contains(index)){
                return false;
            }
            indexes.add(index);
        }
        return true;
    }

    private int getNumberOfSpendable(ArrayList<ArrayList> allTxiTxo){
        int spendable = 0;
        for (ArrayList<String> txo : allTxiTxo){
            spendable = spendable + Integer.valueOf(txo.get(1));
        }
        return spendable;
    }

    private int getNumberSpent(ArrayList<String> tx, ArrayList<ArrayList> txoList){
        int spent = Integer.valueOf(tx.get(5));
        for (ArrayList<String> txo : txoList){
            spent = spent + Integer.valueOf(txo.get(0));
        }
        return spent;
    }


    private boolean samePubKeys(ArrayList<ArrayList> allTxiTxo){
        String pubKeyHash = (String) allTxiTxo.get(0).get(3);
        for (ArrayList<String> tx : allTxiTxo){
            if (!tx.get(3).equals(pubKeyHash)){
                return false;
            }
        }
        return true;
    }

    private boolean verifyMinerRewardFromFullTweet(String numerToMine) {
        //TODO make this
        return true;
    }

    private boolean verifyMinerRewardFromFullBlock(ArrayList<String> tx, ArrayList<ArrayList> txoList,
                                                   ArrayList<String> txList){
        int claimedReward = calculateReward(txoList);
        int rewards = 25;
        if (txList.isEmpty()){
            if (claimedReward == rewards){
                return true;
            }else{
                return false;
            }
        }
        ArrayList<Integer> allBlockTxMinerRewards = db.getAllBlockTxMinerRewards(txList);
        for (Integer reward: allBlockTxMinerRewards){
            rewards = rewards + reward;
        }
        if (claimedReward == rewards){
            return true;
        }else{
            return false;
        }
    }

    private int calculateReward(ArrayList<ArrayList> txoList){
        int reward = 0;
        for (ArrayList<String> txo : txoList){
            reward = reward + Integer.valueOf(txo.get(0));
        }
        return reward;
    }

    public boolean isBlockVerified(ArrayList<ArrayList> fullBlock, NodeBase nb){
        // make this ask for tx if don't have it already, does in verifyBlockTxList
        System.out.println("Verify isBlockVerified fullBlock: " + fullBlock);
        boolean verified = false;
        boolean headerVerified = verifyHeader(fullBlock);
        boolean tweetBaseVerified = verifyTweetBaseTxFromFullBlock(fullBlock);
        boolean txListVerified = verifyBlockTxList(fullBlock, nb);

        if (headerVerified && tweetBaseVerified && txListVerified){
            verified = true;
        }
        System.out.println("Verify isBlockVerified verified: " + verified);
        return verified;
    }

    public boolean isBlockVerified(Block block, NodeBase nb) {
        return (isAllTxVerified(block, nb) && isHeaderVerified(block) && isJouleBaseVerified(block));
    }

    private synchronized boolean isAllTxVerified(Block block, NodeBase nb){
        AllTx allTx = block.getAllTx();
        if (allTx.isEmpty()){
            return true;
        }
        //check if db contains all tx's
        ArrayList<String> allTxHashList = allTx.getHashList();
        ArrayList<String> txListNotInDb = db.cullBlockTxList(allTxHashList);
        if (!txListNotInDb.isEmpty()){
            //if not, ask network for the missing tx
            nb.getMissingTx(txListNotInDb);
            return false;
        }
        //check each tx is okay to be in this block, inputs/outputs align
        String previousBlock = block.getHeader().getPreviousHash();
        for (String txHash: allTxHashList){
            if (!db.isTxAcceptableForBlock(txHash, previousBlock)){
                return false;
            }
        }
        //fill out tx in all Tx
        db.finishAllTx(allTx);
        return true;
    }

    private boolean isJouleBaseVerified(Block block){
        JouleBase base = block.getJouleBase();
        //test hash is correct
        if (!base.testHash()){
            return false;
        }
        //test type is correct
        if (base.getType() != 1){
            return false;
        }
        //test signature is correct
        if (!base.getSignature().equals("0")){
            return false;
        }
        //test report length is correct
        if (base.getReportLength() != 0){
            return false;
        }
        //TODO make sure report is a time and it is accurate enough, no funny business going on
        //test number to miner is correct
        if (base.getNumberToMiner() != 0){
            return false;
        }
        //test txo is correct
        AllTxo allTxo = base.getAllTxo();
        int reward = 0;
        int i = 0;
        for (Txo txo: allTxo.getAllTxo()){
            //check index is correct
            if (i != txo.getTxoIndex()){
                return false;
            }
            //check references joulebase
            if (!txo.getTxHash().equals(base.getHash())){
                return false;
            }
            //total reward, cycle index
            reward = reward + txo.getJoulesToTxo();
            i++;
        }
        //check reward is correct
        if (reward != block.getAllTx().calcBlockReward() + getMinerReward()){
            return false;
        }

        return true;
    }

    private int getMinerReward(){
        //TODO make this respond to inflation in some way, grow with demand, usage. same as in construct
        return 25;
    }

    private boolean isHeaderVerified(Block block){
        Header header = block.getHeader();
        //test if previous hash and height check out
        //TODO check that target is correct target in here
        if (!db.checkBlockHeader(header.getPreviousHash(), header.getHeight() - 1)){
            return false;
        }
        //test hash is correct
        if (!header.correctHash()){
            return false;
        }
        //test merkle root
        if (!block.correctMerkle()){
            return false;
        }
        //test hash is below target
        if (header.getHeaderHash().compareTo(header.getTarget()) != -1){
            return false;
        }
        return true;
    }

    private boolean verifyTweetBaseTxFromFullBlock(ArrayList<ArrayList> fullBlock){
        ArrayList<ArrayList> fullTweet = fullBlock.get(1);
        ArrayList<String> tx = fullTweet.get(0);
        ArrayList<ArrayList> txo = fullTweet.get(1);
        ArrayList<String> txList = new ArrayList<>();
        if (fullBlock.size() == 3){
            txList.addAll(fullBlock.get(2));
        }
        boolean verified = verifyMostTweetBase(tx);

        if (!verifyMinerRewardFromFullBlock(tx, txo, txList)){
            verified = false;
        }
        System.out.println("Verify verifyTweetBaseTxFromFullBlock verified: " + verified);
        return verified;
    }

    private boolean verifyHeader(ArrayList<ArrayList> fullBlock) {
        //test previous hash is real, matches height, test merkle, test hash is real, test hash is below threshold.
        boolean verified = true;
        ArrayList<String> blockHeader = fullBlock.get(0);
        ArrayList<String> previousHeader = db.getBlockHeader(blockHeader.get(3));
        System.out.println("Verify verifyHeader previousHeader: " + previousHeader);
        if (previousHeader == null || previousHeader.isEmpty()) {
            verified = false;
            System.out.println("Verify verifyHeader verified: " + verified);
        }else {
            if (Integer.valueOf(blockHeader.get(1)) != Integer.valueOf(previousHeader.get(1)) + 1) {
                verified = false;
            }
            System.out.println("Verify verifyHeader verified: " + verified);
            ArrayList<String> txToAdd = (ArrayList<String>) fullBlock.get(2);
            String tweetBaseTxHash = ((ArrayList<String>)fullBlock.get(1).get(0)).get(0);
            String verifyMerkle = new MathStuff().createBlockMerkleRoot(txToAdd, tweetBaseTxHash);
            System.out.println("Verify verifyHeader stuff sent to math: " + txToAdd + tweetBaseTxHash);
            System.out.println("Verify verifyHeader verifyMerkle: " + verifyMerkle + " oldMerkle: " + blockHeader.get(2));
            if (!blockHeader.get(2).equals(verifyMerkle)) {
                verified = false;
            }
            System.out.println("Verify verifyHeader verified: " + verified);

            if (!belowThreshold(fullBlock)) {
                verified = false;
            }
            System.out.println("Verify verifyHeader verified: " + verified);

            if (!blockHeader.get(0).equals(new MathStuff().createHash(blockHeader.get(1) + blockHeader.get(2) +
                    blockHeader.get(3) + blockHeader.get(4) + blockHeader.get(5)))) {
                verified = false;
            }
        }
        System.out.println("Verify verifyHeader verified: " + verified);
        return verified;
    }

    private boolean belowThreshold(ArrayList<ArrayList> fullBlock){
        //TODO actually test this
        return true;
    }

    private boolean verifyBlockTxList(ArrayList<ArrayList> fullBlock, NodeBase nb){
        boolean verified = false;
        if (fullBlock.size() == 2 || fullBlock.get(2).isEmpty() || fullBlock.get(2).get(0).equals("no")){
            verified = true;
        }else {
            //separate txList into those I have in my db and those I do not
            ArrayList<String> txList = fullBlock.get(2);
            ArrayList<String> txListNotInDb = db.cullBlockTxList(txList);
            System.out.println("Verify verifyBlocktxList txListNotInDb: " + txListNotInDb);
            if (txListNotInDb.isEmpty() || txListNotInDb.get(0).equals("no")){
                String previousBlock = (String) fullBlock.get(0).get(3);
//                ArrayList<String> chainInfo =new ArrayList<>();
//                chainInfo.add((String)fullBlock.get(0).get(1));
//                chainInfo.add((String)fullBlock.get(0).get(0));
                verified = true;
                for (String txHash: txList){
                    if (!db.isTxAcceptableForBlock(txHash, previousBlock)){
                        verified = false;
                    }
                }
            }else{
                nb.getMissingTx(txListNotInDb);
            }
        }
        System.out.println("Verify verifyBlockTxList verified: " + verified);
        return verified;
    }
}
