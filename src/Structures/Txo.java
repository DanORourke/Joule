package Structures;

public class Txo {
    //The Hash of the tx this txo is in
    private String txHash;
    //Index of txo in tx's txo list
    private int txoIndex;
    //Number of Joules this txo contains
    private int numberToTxo;
    //The pubKey that locks the Joules to this txo
    private String txoPubKey;

    public Txo(){

    }

    public Txo(String hash, int index, int numberToTxo, String txoPubKey){
        this.txHash = hash;
        this.txoIndex = index;
        this.numberToTxo = numberToTxo;
        this.txoPubKey = txoPubKey;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public int getTxoIndex() {
        return txoIndex;
    }

    public void setTxoIndex(int txoIndex) {
        this.txoIndex = txoIndex;
    }

    public int getNumberToTxo() {
        return numberToTxo;
    }

    public void setNumberToTxo(int numberToTxo) {
        this.numberToTxo = numberToTxo;
    }

    public String getTxoPubKey() {
        return txoPubKey;
    }

    public void setTxoPubKey(String txoPubKey) {
        this.txoPubKey = txoPubKey;
    }
}
