package Structures;

import DB.SQLiteJDBC;
import ReadWrite.MathStuff;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Header {
    private String headerHash;
    private int height;
    private String merkleRoot;
    private String previousHash;
    private String target;
    private int nonce;
    private boolean proper = true;

    public Header(){
    }

    public Header(boolean proper){
        this.proper = proper;
    }

    public Header(String headerHash, int height, String merkleRoot,
                  String previousHash, String target, int nonce){
        this.headerHash = headerHash;
        this.height = height;
        this.merkleRoot = merkleRoot;
        this.previousHash = previousHash;
        this.target = target;
        this.nonce = nonce;
    }

    public Header(Header pastHeader){
        this.height = pastHeader.getHeight() + 1;
        this.previousHash = pastHeader.getHeaderHash();
        this.target = pastHeader.getTarget();
        nonce = 0;
    }

    public Header(int height, String previousHash, String target, int nonce){
        this.height = height;
        this.previousHash = previousHash;
        this.target = target;
        this.nonce = nonce;
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

    public void calculateHash(){
        headerHash = new MathStuff().createHash(height + merkleRoot + previousHash + target + nonce);
    }

    public boolean cycleNonce(){
        nonce++;
        calculateHash();
        return underTarget();
    }

    public boolean underTarget(){
        System.out.println("mined header hash: " + headerHash + "  target: " + target);
        return headerHash.compareTo(target) < 0;
    }

    public void resetTarget(SQLiteJDBC db){
        long time1 = db.getTimeOfPastBlock(height - 1);
        long time100 = db.getTimeOfPastBlock(height - 100);
        target = new MathStuff().calculateTarget(time1, time100, target);
    }

    public boolean correctHash() {
        return headerHash.equals(
                new MathStuff().createHash(
                        height + merkleRoot + previousHash + target + nonce));
    }

    public List<String> getWireList() {
        ArrayList<String> wireList = new ArrayList<>();
        wireList.addAll(Arrays.asList(headerHash, String.valueOf(height), merkleRoot,
                previousHash, target, String.valueOf(nonce)));
        return wireList;
    }

    public void printHeader(){
        System.out.println("HEADER   headerHash " + headerHash + " height " + height +
                " merkleRoot " + merkleRoot + " previousHash " + previousHash +
                " target " + target + " nonce " + nonce);
    }

    public boolean isProper() {
        return proper;
    }

    public void setProper(boolean proper) {
        this.proper = proper;
    }
}
