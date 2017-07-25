package Structures;

import DB.SQLiteJDBC;
import ReadWrite.MathStuff;
import java.util.ArrayList;

public class AllTx {
    private ArrayList<Tx> allTx;

    public AllTx(){
        allTx = new ArrayList<>();
    }

    public ArrayList<Tx> getAllTx() {
        return allTx;
    }

    public void setAllTx(ArrayList<Tx> allTx) {
        this.allTx = allTx;
    }

    public void addTx(Tx tx){
        allTx.add(tx);
    }

    public void addWireTx(String txHash){
        allTx.add(new Tx(txHash));
    }

    public Tx getTx(int txIndex){
        return allTx.get(txIndex);
    }

    public void removeTx(int txIndex){
        allTx.remove(txIndex);
    }

    public void removeTx(Tx tx){
        allTx.remove(tx);
    }

    public int size(){
        return allTx.size();
    }

    public int calcBlockReward() {
        int reward = 0;
        for (Tx tx: allTx){
            reward += tx.getNumberToMiner();
        }
        return reward;
    }

    public String calcMerkleRoot(String joulebaseHash) {
        ArrayList<String> hashList = getHashList();
        hashList.add(0, joulebaseHash);
        return new MathStuff().createMerkleRoot(hashList);
    }

    public boolean isEmpty(){
        return allTx.isEmpty();
    }

    public ArrayList<String> getHashList() {
        ArrayList<String> hashList = new ArrayList<>();
        for (Tx tx: allTx){
            hashList.add(tx.getHash());
        }
        return hashList;
    }

    public void setTxIndex(){
        int i = 1;
        for (Tx tx: allTx){
            tx.setTxIndex(i);
            i++;
        }
    }

    public ArrayList<String> getBlockWireList() {
        ArrayList<String> wireBlock = new ArrayList<>();
        for(Tx tx : allTx){
            wireBlock.add(tx.getHash());
        }
        return wireBlock;
    }

    public void printAllTx(){
        if (allTx.isEmpty()){
            System.out.println("alltx empty");
        }else{
            for (Tx tx : allTx){
                tx.printTx();
            }
        }
    }
}
