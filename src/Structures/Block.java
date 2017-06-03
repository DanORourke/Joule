package Structures;

import java.util.ArrayList;

public class Block {
    private String headerHash;
    private int height;
    private String merkleRoot;
    private String previousHash;
    private String target;
    private int nonce;
    private int chain;
    private Tx jouleBase;
    private AllTx allTx;

    public Block(){
        allTx = new AllTx();
    }

    public String getHeaderHash() {
        return headerHash;
    }

    public void setHeaderHash(String headerHash) {
        this.headerHash = headerHash;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public int getChain() {
        return chain;
    }

    public void setChain(int chain) {
        this.chain = chain;
    }

    public AllTx getAllTx() {
        return allTx;
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

    public Tx getJouleBaseTx(){
        return jouleBase;
    }

    public void setJouleBase(Tx jouleBase){
        this.jouleBase = jouleBase;
    }

    public long getTimeOfBlock(){
        return Long.valueOf(jouleBase.getReport());
    }
}
