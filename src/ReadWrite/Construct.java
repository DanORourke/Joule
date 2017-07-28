package ReadWrite;

import DB.SQLiteJDBC;
import Structures.*;

public class Construct {
    private SQLiteJDBC db;
    private int txMaxInBlock = 100;
    public Construct(SQLiteJDBC db){
        this.db = db;
        //TODO use max in block
    }

    public Block constructNewMinerBlock(String pubKey){
        //get txs not yet in a block
        AllTx allTx = db.getNewMinerBlockAllTx(txMaxInBlock);
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
        if (pubKey.length() != 100){
            return new Tx(false);
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
}
