package Structures;

import java.util.ArrayList;

public class Block {
    private Header header;
    private JouleBase jouleBase;
    private AllTx allTx;
    private boolean proper = true;
    private ArrayList<String> missingTx;

    public Block(){
        header = new Header();
        jouleBase = new JouleBase();
        allTx = new AllTx();
    }

    public Block(boolean proper, ArrayList<String> missingTx){
        this.proper = proper;
        this.missingTx = missingTx;
    }

    public Block(Header header, JouleBase base, AllTx allTx){
        this.header = header;
        this.jouleBase = base;
        this.allTx = allTx;

    }

    public AllTx getAllTx() {
        return allTx;
    }

    public void setAllTx(AllTx allTx){
        this.allTx = allTx;
    }

    public void setAllReports(ArrayList<Tx> allTx) {
        this.allTx.setAllTx(allTx);
    }

    public void addTx(Tx tx){
        allTx.addTx(tx);
    }

    public void removeTx(Tx tx){
        allTx.removeTx(tx);
    }

    public void removeTx(int index){
        allTx.removeTx(index);
    }

    public Tx getTx(int index){
        return allTx.getTx(index);
    }

    public JouleBase getJouleBase(){
        return jouleBase;
    }

    public void setJouleBase(JouleBase jouleBase){
        this.jouleBase = jouleBase;
    }

    public long getTimeOfBlock(){
        return jouleBase.getTime();
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public boolean correctMerkle() {
        return header.getMerkleRoot().equals(allTx.calcMerkleRoot(jouleBase.getHash()));
    }

    public ArrayList<String> convertBlockForWire(){
        //convert the obect into a list of strings, will add together when sending
        ArrayList<String> wireBlock = new ArrayList<>();
        wireBlock.addAll(header.getWireList());
        wireBlock.addAll(jouleBase.getWireList());
        //only give the hash for tx, not whole thing
        wireBlock.addAll(allTx.getBlockWireList());
        return wireBlock;
    }

    public void printBlock(){
        header.printHeader();
        jouleBase.printJouleBase();
        allTx.printAllTx();
    }

    public boolean isProper() {
        return proper;
    }

    public void setProper(boolean proper) {
        this.proper = proper;
    }

    public ArrayList<String> getMissingTx() {
        return missingTx;
    }

    public void setMissingTx(ArrayList<String> missingTx) {
        this.missingTx = missingTx;
    }
}
