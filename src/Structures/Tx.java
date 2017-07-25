package Structures;

import ReadWrite.MathStuff;

import java.util.ArrayList;

public class Tx extends TxSuper{
    private AllTxi allTxi;
    private boolean proper;

    public Tx(){
        super();
        allTxi = new AllTxi();
        this.proper = true;
    }

    public Tx(String hash, int type, String signature, int reportLength, String report, int numberToMiner){
        super(hash, type, signature, reportLength, report, numberToMiner);
        allTxi = new AllTxi();
        this.proper = true;
    }

    public Tx(String hash, int type, String signature, int reportLength, String report, int numberToMiner,
              AllTxi allTxi, AllTxo allTxo){
        super(hash, type, signature, reportLength, report, numberToMiner, allTxo);
        this.allTxi = allTxi;
        this.proper = true;
    }

    public Tx(String txHash) {
        super();
        setHash(txHash);
        this.proper = true;
    }

    public Tx(boolean proper){
        this.proper = proper;
    }

    public Tx( AllTxi allTxi, AllTxo allTxo, String report){
        this.allTxi = allTxi;
        setAllTxo(allTxo);
        setReport(report);
        setType(2);
        setReportLength(report.length());
        setNumberToMiner(allTxi.getReportReward());
        setSignature(createSignature());
        createHash();
        allTxi.setTxHash(getHash());
        allTxo.setTxHash(getHash());
        this.proper = true;
    }

    public Tx( AllTxi allTxi, AllTxo allTxo, String report, int type){
        this.allTxi = allTxi;
        setAllTxo(allTxo);
        setReport(report);
        setType(type);
        setReportLength(report.length());
        setNumberToMiner(allTxi.getReportReward());
        setSignature(createSignature());
        createHash();
        allTxi.setTxHash(getHash());
        allTxo.setTxHash(getHash());
        this.proper = true;
    }

    public Tx( AllTxi allTxi, AllTxo allTxo, int type){
        this.allTxi = allTxi;
        setAllTxo(allTxo);
        setReport("0");
        setType(type);
        setReportLength(0);
        setNumberToMiner(allTxi.getReportReward());
        setSignature(createSignature());
        createHash();
        allTxi.setTxHash(getHash());
        allTxo.setTxHash(getHash());
        this.proper = true;
    }

    private String createSignature(){
        //create merkle from allTxi and allTxo, use that as thing to sign with privKey
        ArrayList<String> merkleList = new ArrayList<>();
        int i = 0;
        for (Txi txi : allTxi.getAllTxi()){
            //add txHash of tx
            merkleList.add(txi.getTxiHash());
            //add Hash of txi txo index
            merkleList.add(new MathStuff().createHash(String.valueOf(txi.getTxiTxoIndex())));
            //set txi index while we are here
            txi.setTxiIndex(i);
            i++;
        }
        for (Txo txo : getAllTxo().getAllTxo()){
            //add hash of txo pubKey
            merkleList.add(new MathStuff().createHash(String.valueOf(txo.getTxoPubKey())));
            //add hash of  value of txo index
            merkleList.add(new MathStuff().createHash(String.valueOf(txo.getTxoIndex())));
            //add hash of joules given
            merkleList.add(new MathStuff().createHash(String.valueOf(txo.getJoulesToTxo())));
        }
        System.out.println(merkleList);
        String sigMerkle = new MathStuff().createMerkleRoot(merkleList);
        System.out.println("sigMerkle: " + sigMerkle);
        return new MathStuff().signTx(allTxi.getPrivKey(), sigMerkle);
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

    @Override
    public ArrayList<String> getWireList() {
        ArrayList<String> wireList = super.getWireList();
        AllTxo allTxo = getAllTxo();
        wireList.addAll(allTxo.getWireList());
        wireList.addAll(allTxi.getWireList());
        return wireList;
    }

    public boolean isProper() {
        return proper;
    }

    public boolean isHeadVerified() {
        int type = getType();
        String sig = getSignature();
        int rLen = getReportLength();
        String report = getReport();
        int numberToMiner = getNumberToMiner();

        if (!testHash()){
            return false;
        }
        if (type == 2 || type == 3){
            if (rLen != report.length()){
                return false;
            }
        }
        if (type == 4 && (rLen != 0 || !report.equals("0"))){
            return false;
        }

        if (numberToMiner < 1){
            return false;
        }
        //test unlock
        ArrayList<String> merkleList = new ArrayList<>();
        AllTxi allTxi = getAllTxi();
        for (Txi txi : allTxi.getAllTxi()){
            merkleList.add(txi.getTxiHash());
            merkleList.add(new MathStuff().createHash(String.valueOf(txi.getTxiTxoIndex())));
        }
        for (Txo txo : getAllTxo().getAllTxo()){
            //add hash of txo pubKey
            merkleList.add(new MathStuff().createHash(String.valueOf(txo.getTxoPubKey())));
            //add hash of  value of txo index
            merkleList.add(new MathStuff().createHash(String.valueOf(txo.getTxoIndex())));
            //add hash of joules given
            merkleList.add(new MathStuff().createHash(String.valueOf(txo.getJoulesToTxo())));
        }
        System.out.println(merkleList);
        String sigMerkle = new MathStuff().createMerkleRoot(merkleList);
        System.out.println("sigMerkle: " + sigMerkle);
        String pubKey = getAllTxi().getPubKey();
        return new MathStuff().testSig(pubKey, sig, sigMerkle);
    }

    public ArrayList<String> convertForWire(){
        ArrayList<String> wireTx = new ArrayList<>();
        wireTx.add(getHash());
        wireTx.add(String.valueOf(getType()));
        wireTx.add(getSignature());
        wireTx.add(String.valueOf(getReportLength()));
        wireTx.add(getReport());
        wireTx.add(String.valueOf(getNumberToMiner()));

        wireTx.add(String.valueOf(getAllTxo().getAllTxo().size()));
        for (Txo txo : getAllTxo().getAllTxo()){
            wireTx.add(String.valueOf(txo.getTxoIndex()));
            wireTx.add(String.valueOf(txo.getJoulesToTxo()));
            wireTx.add(txo.getTxoPubKey());
        }

        wireTx.add(String.valueOf(allTxi.getAllTxi().size()));
        for (Txi txi : allTxi.getAllTxi()){
            wireTx.add(String.valueOf(txi.getTxiIndex()));
            wireTx.add(txi.getTxiHash());
            wireTx.add(String.valueOf(txi.getTxiTxoIndex()));
        }

        return wireTx;
    }

    public void printTx(){
        System.out.println("TX   hash " + getHash() + " type " + getType() + " signature " + getSignature() +
                " reportLength " + getReportLength() + " report " + getReport() +
                " numberToMiner " + getNumberToMiner() + " txIndex " + getTxIndex() + " proper " + proper);
        getAllTxo().printAllTxo();
        allTxi.printAllTxi();
    }
}

