package Mining;

import DB.SQLiteJDBC;
import Node.NodeBase;
import ReadWrite.Construct;
import ReadWrite.MathStuff;
import ReadWrite.Verify;
import Structures.Block;

import java.io.IOException;
import java.util.ArrayList;

public class Miner implements Runnable {
    private NodeBase nb;
    private SQLiteJDBC db;
    private volatile boolean wait;
    private boolean reset;
    private boolean active;
    private volatile String username;
    private String pubKey;
    private ArrayList<String> incompleteHeader;
    private ArrayList<ArrayList> tweetBaseTx;
    private ArrayList<String> txToAdd;
    private String headerToHash;
    private String target;


    public Miner(NodeBase nb, SQLiteJDBC db, String username) throws IOException{
        //set variables
        this.nb = nb;
        this.db = db;
        this.username = username;
        this.wait = false;
        this.reset = false;
        this.active = true;
        setKey();
    }

    public void run(){
        //mineLoop();
        loop();
    }

    public void setKey(){
        //get pubkey of user from db to claim miner rewards
        ArrayList<String> keys = db.getUserKeys(username);
        this.pubKey = keys.get(0);
    }

//    private void makeHeaderToHash(){
//        //reset variables from new potential block
//        ArrayList<ArrayList> total = new Construct(db).constructBlockToMine(pubKey);
//        if (total.size() == 3){
//            this.incompleteHeader = total.get(0);
//            this.tweetBaseTx = total.get(1);
//            this.txToAdd = total.get(2);
//            this.target = (String)total.get(0).get(3);
//            this.headerToHash = incompleteHeader.get(0) + incompleteHeader.get(1) + incompleteHeader.get(2) + target;
//        }else{
//            System.out.println("miner constructBlockToMine failed, total: " + total);
//            headerToHash = "";
//        }
//    }

//    private void mineLoop(){
//        while (active){
//            if (wait){
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }else {
//                makeHeaderToHash();
//                //create candidate hashes
//                ArrayList<String> results = createMiningResults();
//                //skip if need to reset or no hashes under target
//                if (!results.isEmpty() && !reset){
//                    createAndSendBlock(results.get(0), Integer.valueOf(results.get(1)));
//                }
//            }
//            reset = false;
//        }
//    }

    private void loop(){
        while (active){
            if (wait){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                //create possible block without hash
                Block partialBlock = new Construct(db).constructNewMinerBlock(pubKey);
                //check if block is valid
                if (partialBlock.getHeader().underTarget()){
                    //verify, add to db, and broadcast new block
                    sendBlock(partialBlock);
                }
                //if not, cycle nonce and see if valid
                else{
                    int i  = 0;
                    while (i < 9 && !reset){
                        i++;
                        //calc new hash and see if it is acceptable
                        boolean found = partialBlock.getHeader().cycleNonce();
                        //skip if need to reset or no hashes under target
                        if (found && !reset){
                            //verify, add to db, and broadcast new block
                            sendBlock(partialBlock);
                        }else {
                            //Wait so as to not bog down my machine
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                System.out.println("Miner sleep interrupted while mining");
                                active = false;
                                reset = true;
                            }
                        }
                    }
                }
            }
            reset = false;
        }
    }

    private ArrayList<String> createMiningResults(){
        //return empty if no qualified hashes, hash and nonce if find one
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
                //don't make any more hashes
                return results;
            }else {
                //Wait so as to not bog down my machine
                try {
                    Thread.sleep(1000);
                    nonce ++;
                } catch (InterruptedException e) {
                    System.out.println("Miner sleep interrupted while mining");
                    active = false;
                    reset = true;
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
        //finish creating block
        incompleteHeader.add(0, hash);
        incompleteHeader.add(String.valueOf(nonce));
        ArrayList<ArrayList> minedBlock = new ArrayList<>();
        minedBlock.add(incompleteHeader);
        minedBlock.add(tweetBaseTx);
        minedBlock.add(txToAdd);
        //add and propagate block if passes verification check
        if (new Verify(db).isBlockVerified(minedBlock, nb)){
            nb.addMinedBlock(minedBlock);
        }
    }

    private void sendBlock(Block block){
        block.printBlock();
        if (new Verify(db).isBlockVerified(block, nb)){
            block.printBlock();
            System.out.println("I'm rich!!!!!!");
            nb.addMinedBlock(block);
        }
        else{
            System.out.println("block failed verification");
        }
    }

}
