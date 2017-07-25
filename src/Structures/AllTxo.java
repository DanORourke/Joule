package Structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

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

    public boolean isEmpty(){
        return allTxo.isEmpty();
    }

    public ArrayList<String> getWireList() {
        ArrayList<String> wireList = new ArrayList<>();
        wireList.add(String.valueOf(allTxo.size()));
        for (Txo txo : allTxo){
            wireList.addAll(txo.getWireList());
        }
        return wireList;
    }

    public void setTxHash(String txHash) {
        int i = 0;
        for (Txo txo : allTxo){
            txo.setTxHash(txHash);
            txo.setTxoIndex(i);
            i ++;
        }
    }

    public boolean isVerified() {
        if (allTxo.isEmpty()){
            return true;
        }
        //make sure indexes don't repeat or skip
        HashSet<Integer> indexes = new HashSet<>();
        int min = 1;
        int max = -1;
        for (Txo txo : allTxo){
            int index = txo.getTxoIndex();
            indexes.add(index);
            if (index < min){
                min = index;
            }
            if (index > max){
                max = index;
            }
        }
        if (indexes.size() != allTxo.size() || min != 0 || (max != allTxo.size() - 1)){
            return false;
        }
        return true;
    }

    public void printAllTxo(){
        if (allTxo.isEmpty()){
            System.out.println("allTxo empty");
        }
        for (Txo txo : allTxo){
            txo.printTxo();
        }
    }
}
