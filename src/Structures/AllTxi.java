package Structures;

import java.util.ArrayList;

public class AllTxi {
    private int reportReward = 0;
    private int totalJoules = 0;
    private String pubKey;
    private String privKey;
    //make map??
    private ArrayList<Txi> allTxi;

    public AllTxi(){
        allTxi = new ArrayList<>();
    }

    public ArrayList<Txi> getAllTxi() {
        return allTxi;
    }

    public void setAllTxi(ArrayList<Txi> allTxi) {
        this.allTxi = allTxi;
    }

    public void addTxi(Txi txi){
        allTxi.add(txi);
    }

    public Txi getTxi(int txiIndex){
        return allTxi.get(txiIndex);
    }

    public void removeTxi(int txiIndex){
        allTxi.remove(txiIndex);
    }

    public void removeTxi(Txi txi){
        allTxi.remove(txi);
    }

    public int size(){
        return allTxi.size();
    }

    public boolean isEmpty(){
        return allTxi.isEmpty();
    }

    public ArrayList<String> getWireList() {
        ArrayList<String> wireList = new ArrayList<>();
        wireList.add(String.valueOf(allTxi.size()));
        for (Txi txi : allTxi){
            wireList.addAll(txi.getWireList());
        }
        return wireList;
    }

    public int getReportReward() {
        return reportReward;
    }

    public void setReportReward(int reportReward) {
        this.reportReward = reportReward;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public int getChange(){
        return totalJoules - reportReward;
    }

    public int getTotalJoules() {
        return totalJoules;
    }

    public void setTotalJoules(int totalJoules) {
        this.totalJoules = totalJoules;
    }

    public String getPrivKey() {
        return privKey;
    }

    public void setPrivKey(String privKey) {
        this.privKey = privKey;
    }

    public void setTxHash(String txHash) {
        for (Txi txi : allTxi){
            txi.setTxHash(txHash);
        }
    }

    public String getVerifySql(){
        StringBuilder sql = new StringBuilder();
        for (Txi txi : allTxi){
            sql.append("(TXHASH = '");
            sql.append(txi.getTxiHash());
            sql.append("' AND TXOINDEX = ");
            sql.append(txi.getTxiTxoIndex());
            sql.append(")");
            if (allTxi.indexOf(txi) != allTxi.size() - 1){
                sql.append(" OR ");
            }
        }
        return sql.toString();
    }

    public boolean doTxListMatch(ArrayList<String> txList, ArrayList<String> pubKeyHashList) {
        //make sure all pub key are the same
        if (pubKeyHashList.stream().distinct().limit(2).count() > 1){
            System.out.println("more than one pubKey");
            return false;
        }
        //test all tx in allTxi in txHashList
        for (Txi txi : allTxi){
            if (!txList.contains(txi.getTxiHash())){
                System.out.println("contains a txi not in db");
                return false;
            }
        }
        return true;
    }

    public boolean containsTxHash(String txHash){
        for (Txi txi : allTxi){
            if (txi.getTxHash().equals(txHash)){
                return true;
            }
        }
        return false;
    }

    public void printAllTxi(){
        if (allTxi.isEmpty()){
            System.out.println("allTxi empty");
        }
        for (Txi txi : allTxi){
            txi.printTxi();
        }
    }

}
