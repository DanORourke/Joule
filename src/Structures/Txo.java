package Structures;

import ReadWrite.MathStuff;

import java.util.ArrayList;
import java.util.Arrays;

public class Txo {
    //The Hash of the tx this txo is in
    private String txHash;
    //Index of txo in tx's txo list
    private int txoIndex;
    //Number of Joules this txo contains
    private int joulesToTxo;
    //The pubKey that locks the Joules to this txo
    private String txoPubKey;
    //Hash of pubkey
    private String txoPubKeyHash;

    public Txo(){

    }

    public Txo(String hash, int index, int joulesToTxo, String txoPubKey){
        this.txHash = hash;
        this.txoIndex = index;
        this.joulesToTxo = joulesToTxo;
        this.txoPubKey = txoPubKey;
        this.txoPubKeyHash = new MathStuff().createHash(txoPubKey);
    }

    public Txo(int index, int joulesToTxo, String txoPubKey){
        this.txoIndex = index;
        this.joulesToTxo = joulesToTxo;
        this.txoPubKey = txoPubKey;
        this.txoPubKeyHash = new MathStuff().createHash(txoPubKey);
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

    public int getJoulesToTxo() {
        return joulesToTxo;
    }

    public void setJoulesToTxo(int numberToTxo) {
        this.joulesToTxo = numberToTxo;
    }

    public String getTxoPubKey() {
        return txoPubKey;
    }

    public void setTxoPubKey(String txoPubKey) {
        this.txoPubKey = txoPubKey;
    }

    public String getTxoPubKeyHash() {
        return txoPubKeyHash;
    }

    public void setTxoPubKeyHash(String txoPubKeyHash) {
        this.txoPubKeyHash = txoPubKeyHash;
    }

    public ArrayList<String> getWireList() {
        ArrayList<String> wireList = new ArrayList<>();
        wireList.addAll(Arrays.asList(String.valueOf(txoIndex), String.valueOf(joulesToTxo), txoPubKey));
        return wireList;
    }

    public void printTxo(){
        System.out.println("TXO   txHash " + txHash + " txoIndex " + txoIndex + " joulesToTxo " + joulesToTxo +
                " txoPubKey " + txoPubKey + " txoPubKeyHash " + txoPubKeyHash);
    }
}
