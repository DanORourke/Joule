package Structures;

public class Txi {
    //Hash of the tx this txi gives joules to
    private String txHash;
    //The index of this txi in the tx's txi list
    private int txiIndex;
    //Hash of the tx the txo this txi gets joules from is in
    private String txiHash;
    //The index of the txo this txi gets joules from in the old tx's txo list
    private int txiTxoIndex;

    public Txi(){

    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public int getTxiIndex() {
        return txiIndex;
    }

    public void setTxiIndex(int txiIndex) {
        this.txiIndex = txiIndex;
    }

    public String getTxiHash() {
        return txiHash;
    }

    public void setTxiHash(String txiHash) {
        this.txiHash = txiHash;
    }

    public int getTxiTxoIndex() {
        return txiTxoIndex;
    }

    public void setTxiTxoIndex(int txiTxoIndex) {
        this.txiTxoIndex = txiTxoIndex;
    }
}
