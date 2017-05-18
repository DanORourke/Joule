package Node;

import DB.SQLiteJDBC;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class NodeServer implements Runnable  {
    private SQLiteJDBC db;
    private NodeBase nb;
    private ServerSocket listener;
    private boolean stop;
    private String networkType;
    private String myIp;
    private int myPort;
    private String myNetName;

    public NodeServer(NodeBase nb, SQLiteJDBC db, String networkType) throws IOException  {
        this.nb = nb;
        this.db = db;
        this.networkType = networkType;
        setInfo();
        this.stop = false;
    }

    public void run(){
        setListener(myPort);
        hearFriends();
    }

    private void setInfo(){
        if (networkType.equals("outside")){
            this.myIp = nb.getOutsideIp();
            this.myPort = nb.getOutsidePort();
            this.myNetName = nb.getOutsideNetName();
        }else{
            this.myIp = nb.getInsideIp();
            this.myPort = nb.getInsidePort();
            this.myNetName = nb.getInsideNetName();
        }
    }

    public void setStop(){
        stop = true;
    }

    public ArrayList<String> getServerInfo(){
        ArrayList<String> info = new ArrayList<>();
        info.add(myIp);
        info.add(String.valueOf(myPort));
        info.add(myNetName);
        return info;
    }

    private void setListener(int listenPort){
        try {
            listener = new ServerSocket(listenPort);
            // trying to fix freezing issues, turn this off, not sure why I want it anyway
            //listener.setSoTimeout(1000);

        } catch (BindException e) {
            System.out.println("SERVER JVM_Bind Exception");
            nb.serverFailed(networkType);

        } catch (IOException e) {
            e.printStackTrace();
            nb.serverFailed(networkType);
        }
        try {
            System.out.println("NodeServer listening on port: " + listener.getLocalPort() + " myPort: " + myPort +
                    " on Ip: " + listener.getInetAddress().getLocalHost() + " myIp: " + myIp);
        } catch(NullPointerException e){
            System.out.println("SERVER Null Pointer Exception");

        }catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private void hearFriends(){
        while (!stop && !Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = listener.accept();
                NodeTalker talker = new NodeTalker(socket, db, nb, "listen", networkType);
                nb.addTalker(talker, networkType);

            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                e.printStackTrace();
                // trying to fix freezing issues
                try {
                    System.out.println("NodeServer listener.close() Ip: " + myIp + " myPort: " + myPort +
                            " myNetName: " + myNetName);
                    listener.close();
                    setListener(myPort);
                } catch (IOException e2) {
                    e2.printStackTrace();
                    setListener(myPort);
                }
            }
        }
        try {
            listener.close();
            System.out.println("NodeServer listener.close() Ip: " + myIp + " myPort: " + myPort +
                    " myNetName: " + myNetName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
