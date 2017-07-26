package ReadWrite;

import DB.SQLiteJDBC;
import Node.NodeBase;
import Structures.*;

import java.util.ArrayList;

public class Verify {
    private SQLiteJDBC db;
    private int maxInBlock = 100;

    public Verify(SQLiteJDBC db){
        this.db = db;
        //TODO use max in block, same as construct
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

    public boolean isBlockVerified(Block block, NodeBase nb) {
        boolean allTx = isAllTxVerified(block, nb);
        boolean header = isHeaderVerified(block);
        boolean jouleBase = isJouleBaseVerified(block);
        System.out.println("verify allTx: " + allTx + " header: " + header + " jouleBase: " + jouleBase);
        return (allTx && header && jouleBase);
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
        //TODO check that target is correct target
        Header header = block.getHeader();
        //test if previous hash and height check out
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
        if (!header.underTarget()){
            return false;
        }
        return true;
    }
}
