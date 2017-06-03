package Structures;

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

}
