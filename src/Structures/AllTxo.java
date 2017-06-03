package Structures;


import java.util.ArrayList;

public class AllTxo {
    //make map??
    private ArrayList<Txo> allTxo;

    public AllTxo(){
        allTxo = new ArrayList<>();
    }

    public ArrayList<Txo> getAllTxo() {
        return allTxo;
    }

    public void setAllTxo(ArrayList<Txo> allTxo) {
        this.allTxo = allTxo;
    }

    public void addTxo(Txo txo){
        allTxo.add(txo);
    }

    public void addTxo(int index, Txo txo){
        allTxo.add(index, txo);
    }

    public Txo getTxo(int txoIndex){
        return allTxo.get(txoIndex);
    }

    public void removeTxo(int txoIndex){
        allTxo.remove(txoIndex);
    }

    public void removeTxo(Txo txo){
        allTxo.remove(txo);
    }

    public int size(){
        return allTxo.size();
    }
}
