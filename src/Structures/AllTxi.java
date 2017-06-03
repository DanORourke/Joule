package Structures;

import java.util.ArrayList;

public class AllTxi {
    //make map??
    private ArrayList<Txi> allTxi;

    public AllTxi(){
        allTxi = new ArrayList<>();
    }

    public ArrayList<Txi> getAllTxi() {
        return allTxi;
    }

    public void setAllTxi(ArrayList<Txi> allTxi) {
        this.allTxi = allTxi;
    }

    public void addTxi(Txi txi){
        allTxi.add(txi);
    }

    public Txi getTxi(int txiIndex){
        return allTxi.get(txiIndex);
    }

    public void removeTxi(int txiIndex){
        allTxi.remove(txiIndex);
    }

    public void removeTxi(Txi txi){
        allTxi.remove(txi);
    }

    public int size(){
        return allTxi.size();
    }
}
