package Node;

import DB.SQLiteJDBC;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class NodeParty implements Runnable {
    private SQLiteJDBC db;
    private NodeBase nb;
    private int timeout;

    public NodeParty(NodeBase nb, SQLiteJDBC db) throws IOException {
        this.nb = nb;
        this.db = db;
        this.timeout = 1000;
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
        callInsideFriends(3);
        callOutsideFriends(3);
    }

    private void callFriends(ArrayList<ArrayList> friends, ArrayList<NodeTalker> talkers,
                             int maxSize, String networkType){
        if (!friends.isEmpty()){
            for(int i = 0; i < friends.size() && talkers.size() < maxSize; i++){
                ArrayList<String> friend = friends.get(i);
                String friendIp = friend.get(0);
                int friendPort = Integer.valueOf(friend.get(1));
                if (nb.newFriendParty(friendIp, friendPort, networkType)){
                    try {
                        System.out.println("NodeParty callFriends calling IP: " + friendIp +
                                ", Port : " + friendPort);
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(friendIp, friendPort), timeout);
                        NodeTalker talker = new NodeTalker(socket, db, nb, "talk", networkType);
                        nb.addTalker(talker, networkType);
                    } catch (BindException e) {
                        System.out.println("NodeParty callFriends cannot bind to port");
                    } catch (ConnectException e) {
                        System.out.println("NodeParty callFriends connection refused IP: " + friendIp +
                                " Port : " + friendPort);
                    } catch (IOException e) {
                        System.out.println("NodeParty callFriends connection failed IP: " + friendIp +
                                " Port : " + friendPort);
                    }
                }
            }
        }
    }
}
