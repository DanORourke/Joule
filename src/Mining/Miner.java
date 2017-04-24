package Mining;

import DB.SQLiteJDBC;
import Node.NodeBase;
import ReadWrite.Construct;
import ReadWrite.MathStuff;
import ReadWrite.Verify;

import java.io.IOException;
import java.util.ArrayList;

public class Miner implements Runnable {
    private NodeBase nb;
    private SQLiteJDBC db;
    private volatile boolean wait;
    private boolean reset;
    private volatile String username;
    private String pubKey;
    private ArrayList<String> incompleteHeader;
    private ArrayList<ArrayList> tweetBaseTx;
    private ArrayList<String> txToAdd;
    private String headerToHash;
    private String target;


    public Miner(NodeBase nb, SQLiteJDBC db, String username) throws IOException{
        this.nb = nb;
        this.db = db;
        this.username = username;
        this.wait = false;
        this.reset = false;
        setKey();
    }

    public void run(){
        mineLoop();
    }

    public void setKey(){
        ArrayList<String> keys = db.getUserKeys(username);
        this.pubKey = keys.get(0);
    }

    private void makeHeaderToHash(){
        ArrayList<ArrayList> total = new Construct(db).constructBlockToMine(pubKey);
        if (total.size() == 3){
            this.incompleteHeader = total.get(0);
            this.tweetBaseTx = total.get(1);
            this.txToAdd = total.get(2);
            this.target = (String)total.get(0).get(3);
            this.headerToHash = incompleteHeader.get(0) + incompleteHeader.get(1) + incompleteHeader.get(2) + target;
            //mineBlock(incompleteHeader.get(0));
        }else{
            System.out.println("miner constructBlockToMine failed, total: " + total);
            headerToHash = "";
        }
    }

    private void mineLoop(){
        while (true){
            makeHeaderToHash();
            if (wait){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                ArrayList<String> results = createMiningResults();
                if (!results.isEmpty() && !reset){
                    createAndSendBlock(results.get(0), Integer.valueOf(results.get(1)));
                }
            }
            reset = false;
        }
    }

    private ArrayList<String> createMiningResults(){
        ArrayList<String> results = new ArrayList<>();
        int nonce = 0;
        String height = incompleteHeader.get(0);
        while(nonce < 10 && !reset){
            String hash = new MathStuff().createHash(headerToHash + String.valueOf(nonce));
            System.out.println("Miner createMiningResults hash: " + hash + " wait: " + wait + " height: " + height +
                    " nonce: " + nonce);
            if (hash.compareTo(target) <= 0){
                System.out.println("Miner found new block");
                results.add(hash);
                results.add(String.valueOf(nonce));
                return results;
            }else {
                //Wait so as to not bog down my machine
                try {
                    Thread.sleep(1000);
                    nonce ++;
                } catch (InterruptedException e) {
                    System.out.println("Miner sleep interrupted while mining");
                    e.printStackTrace();
                }
            }
        }
        return results;
    }

    public void setWait(boolean wait) {
        this.wait = wait;
        this.reset = true;
    }

    private void createAndSendBlock(String hash, int nonce){
        incompleteHeader.add(0, hash);
        incompleteHeader.add(String.valueOf(nonce));
        ArrayList<ArrayList> minedBlock = new ArrayList<>();
        minedBlock.add(incompleteHeader);
        minedBlock.add(tweetBaseTx);
        minedBlock.add(txToAdd);
        if (new Verify(db).isBlockVerified(minedBlock, nb)){
            nb.addMinedBlock(minedBlock);
        }
    }

}
