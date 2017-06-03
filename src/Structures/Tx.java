package Structures;

public class Tx extends TxSuper{
    private AllTxi allTxi;

    public Tx(){
        super();
        allTxi = new AllTxi();
    }

    public AllTxi getAllTxi() {
        return allTxi;
    }

    public int getAllTxiSize() {
        return allTxi.size();
    }

    public void setAllTxi(AllTxi AllTxi) {
        this.allTxi = allTxi;
    }

    public void addTxi(Txi txi){
        allTxi.addTxi(txi);
    }

    public Txi getTxi(int index){
        return allTxi.getTxi(index);
    }

    public void removeTxi(int index){
        allTxi.removeTxi(index);
    }

    public void removeTxi(Txi txi){
        allTxi.removeTxi(txi);
    }

}
