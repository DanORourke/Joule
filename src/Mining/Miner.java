package Mining;

import DB.SQLiteJDBC;
import Node.NodeBase;
import ReadWrite.Construct;
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

    private void loop(){
        while (active){
            if (wait){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                //create possible block
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
                        //skip if need to reset or hash not under target
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

    public void setWait(boolean wait) {
        this.wait = wait;
        this.reset = true;
    }

    private void sendBlock(Block block){
        block.printBlock();
        if (new Verify(db).isBlockVerified(block, nb)){
            System.out.println("I'm rich!!!!!!");
            nb.addMinedBlock(block);
        }
        else{
            System.out.println("block failed verification");
        }
    }

}
