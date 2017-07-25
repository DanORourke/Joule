package Structures;

import ReadWrite.MathStuff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TxSuper {
    private String hash;
    private int type;
    private String signature;
    private int reportLength;
    private String report;
    private int numberToMiner;
    private int txIndex;
    private AllTxo allTxo;

    public TxSuper(){
        allTxo = new AllTxo();
    }

    public TxSuper(String hash, int type, String signature, int reportLength, String report, int numberToMiner){
        this.hash = hash;
        this.type = type;
        this.signature = signature;
        this.reportLength = reportLength;
        this.report = report;
        this.numberToMiner = numberToMiner;
        allTxo = new AllTxo();
    }

    public TxSuper(String hash, int type, String signature, int reportLength, String report,
                   int numberToMiner, AllTxo allTxo){
        this.hash = hash;
        this.type = type;
        this.signature = signature;
        this.reportLength = reportLength;
        this.report = report;
        this.numberToMiner = numberToMiner;
        this.allTxo = allTxo;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
    public void createHash(){
        hash = new MathStuff().createHash(type + signature +
                reportLength + report + numberToMiner);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getReportLength() {
        return reportLength;
    }

    public void setReportLength(int reportLength) {
        this.reportLength = reportLength;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public int getNumberToMiner() {
        return numberToMiner;
    }

    public void setNumberToMiner(int numberToMiner) {
        this.numberToMiner = numberToMiner;
    }

    public AllTxo getAllTxo() {
        return allTxo;
    }

    public int getAllTxoSize() {
        return allTxo.size();
    }

    public void setAllTxo(AllTxo allTxo) {
        this.allTxo = allTxo;
    }

    public void addTxo(Txo txo){
        allTxo.addTxo(txo);
    }

    public void addTxo(int txoIndex, int joulesToTxo, String txoPubKey){
        allTxo.addTxo(txoIndex, new Txo(hash, txoIndex, joulesToTxo, txoPubKey));
    }

    public Txo getTxo(int index){
        return allTxo.getTxo(index);
    }

    public void removeTxo(int index){
        allTxo.removeTxo(index);
    }

    public void removeTxo(Txo txo){
        allTxo.removeTxo(txo);
    }

    public boolean testHash(){
        return hash.equals(new MathStuff().createHash(type + signature +
                reportLength + report + numberToMiner));
    }

    public int getTxIndex() {
        return txIndex;
    }

    public void setTxIndex(int txIndex) {
        this.txIndex = txIndex;
    }

    public ArrayList<String> getWireList() {
        ArrayList<String> wireList = new ArrayList<>();
        wireList.addAll(Arrays.asList(hash, String.valueOf(type), signature,
                String.valueOf(reportLength), report, String.valueOf(numberToMiner)));
        return wireList;
    }

}
