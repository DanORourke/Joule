package Node;

import DB.SQLiteJDBC;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NodeCaller extends Thread{
    private int myPort;
    private SQLiteJDBC db;
    private NodeBase nb;
    private int timeout;
    private String myIp;
    private String friendIp;
    private int friendPort;
    private String networkType;

    public NodeCaller(NodeBase nb, SQLiteJDBC db, String friendIp, int friendPort, String networkType)
            throws IOException {
        this.nb = nb;
        this.db = db;
        this.networkType = networkType;
        setInfo();
        this.friendIp = friendIp;
        this.friendPort = friendPort;
        this.timeout = 5000;
    }

    private void setInfo(){
        if (networkType.equals("outside")){
            this.myIp = nb.getOutsideIp();
            this.myPort = nb.getOutsidePort();
        }else{
            this.myIp = nb.getInsideIp();
            this.myPort = nb.getInsidePort();
        }
    }

    public void run(){
        //System.out.println("NodeCaller called" + " " + myIp + " " + friendIp + " " + myPort + " " + friendPort);
        if (!myIp.equals(friendIp) || myPort != friendPort){
            try {
                Socket socket = new Socket();
                System.out.println("nodecaller calling IP: " + friendIp +
                        ", Port : " + friendPort);
                socket.connect(new InetSocketAddress(friendIp, friendPort), timeout);
                NodeTalker talker = new NodeTalker(socket, db, nb, "talk",networkType);
                nb.addTalker(talker, networkType);
            } catch (SocketTimeoutException e){
                System.out.println("nodecaller calling IP: " + friendIp +
                        ", Port : " + friendPort + " connect timed out");
            } catch(ConnectException e){
                System.out.println("nodecaller calling IP: " + friendIp +
                        ", Port : " + friendPort + " connection refused");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
