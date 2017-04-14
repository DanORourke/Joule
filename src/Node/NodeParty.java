package Node;

import DB.SQLiteJDBC;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class NodeParty extends Thread {
    private SQLiteJDBC db;
    private NodeBase nb;
    private int timeout;

    public NodeParty(NodeBase nb, SQLiteJDBC db) throws IOException {
        this.nb = nb;
        this.db = db;
        this.timeout = 5000;
    }

    public void run(){
        System.out.println("NodeParty called");
        String networkType = nb.getNetworkType();
        if (networkType.equals("outside")){
            callOutsideFriends(6);
        }else if (networkType.equals("inside")){
            callInsideFriends(6);
        }else{
            callBothFriends();
        }
    }

    private void callOutsideFriends(int maxSize){
        //TODO use nodeCaller
        ArrayList<ArrayList> friends = db.getFriends("outside");
        ArrayList<NodeTalker> talkers = nb.getOutsideTalkers();
        callFriends(friends, talkers, maxSize, "outside");
    }

    private void callInsideFriends(int maxSize){
        String nameOfNetwork = nb.getNameOfInsideNetwork();
        ArrayList<ArrayList> friends = db.getFriends(nameOfNetwork);
        ArrayList<NodeTalker> talkers = nb.getInsideTalkers();
        callFriends(friends, talkers, maxSize, "inside");
    }

    private void callBothFriends(){
        callOutsideFriends(3);
        callInsideFriends(3);
    }

    private void callFriends(ArrayList<ArrayList> friends, ArrayList<NodeTalker> talkers,
                             int maxSize, String networkType){
        int j = talkers.size();
        if (!friends.isEmpty()){
            for(int i = 0; i < friends.size() || j >= maxSize; i++){
                ArrayList<String> friend = friends.get(i);
                String friendIp = friend.get(0);
                int friendPort = Integer.valueOf(friend.get(1));
                String friendNetName = friend.get(2);
                if (nb.newFriendParty(friendIp, friendPort, networkType)){
                    try {
                        System.out.println("NodeParty callFriends calling IP: " + friendIp +
                                ", Port : " + friendPort);
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(friendIp, friendPort), timeout);
                        NodeTalker talker = new NodeTalker(socket, db, nb, "talk", networkType);
                        nb.addTalker(talker, networkType);
                        j++;
                    } catch (IOException e) {
                        System.out.println("NodeParty callFriends connection refused IP: " + friendIp +
                                " Port : " + friendPort);
                    }
                }
            }
        }
    }

}
