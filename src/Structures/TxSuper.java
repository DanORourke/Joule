package Structures;

import ReadWrite.MathStuff;

public class TxSuper {
    private String hash;
    private int type;
    private String signature;
    private int reportLength;
    private String report;
    private int numberToMiner;
    private AllTxo allTxo;

    public TxSuper(){
        allTxo = new AllTxo();
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
    public void createHash(){
        //do I need to convert to string??
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

    public void addTxo(int txoIndex, int numberToTxo, String txoPubKey){
        allTxo.addTxo(txoIndex, new Txo(hash, txoIndex, numberToTxo, txoPubKey));
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
}
