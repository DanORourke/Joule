package Structures;

import java.util.ArrayList;


public class JouleBase extends TxSuper{
    public JouleBase(){
        super();
    }

    public JouleBase(long time) {
        super();
        setTime(time);
        setType(1);
        setSignature("0");
        setReportLength(0);
        setNumberToMiner(0);
        setTxIndex(0);
        createHash();
    }

    public JouleBase(String hash, int type, String signature, int reportLength, String report,
                   int numberToMiner, AllTxo allTxo){
        super(hash, type, signature, reportLength, report, numberToMiner, allTxo);
    }

    public JouleBase(String hash, int type, String signature, int reportLength, String report,
                     int numberToMiner){
        super(hash, type, signature, reportLength, report, numberToMiner);
    }

    public Long getTime() {
        return Long.valueOf(getReport());
    }

    public void setTime(Long time) {
        setReport(String.valueOf(time));
    }

    @Override
    public ArrayList<String> getWireList() {
        ArrayList<String> wireList = super.getWireList();
        Txo txo = getAllTxo().getAllTxo().get(0);
        wireList.addAll(txo.getWireList());
        return wireList;
    }

    public void printJouleBase(){
        System.out.println("JouleBase    hash " + getHash() + " type " + getType() + " signature " + getSignature() +
                " reportLength " + getReportLength() + " report " + getReport() +
                " numberToMiner " + getNumberToMiner() + " txIndex " + getTxIndex());
        getAllTxo().printAllTxo();
    }
}
