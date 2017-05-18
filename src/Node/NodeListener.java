package Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class NodeListener extends Thread{

    private Socket socket;
    private NodeTalker talker;
    private BufferedReader in;
    private boolean stop;

    public NodeListener(Socket socket, NodeTalker talker) {
        //TODO add timeout if no response, check connection, close thread, tell db if dead line

        this.socket = socket;
        this.talker = talker;
        this.stop = false;
    }

    public void run(){
        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        listen();
    }

    public void removeTalker(){
        this.talker = null;
    }

    public String getConnectedIp(){
        InetAddress addr = socket.getInetAddress();
        return addr.getHostAddress();
    }

    public int getConnectedPort(){
        return socket.getPort();
    }

    private void listen(){
        try {
            while (!stop) {
                String input = in.readLine();

                if (input != null) {
                    System.out.println("input from Ip: " + getConnectedIp() + " Port: " + getConnectedPort() +
                            " message: " + input);
                    ArrayList<String> words = parseInput(input);
                    if (talker != null){
                        talker.hearThis(words);
                    }
                    System.out.println("nl stop: " + stop);
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client Ip: " + getConnectedIp() + " Port: " +
                    getConnectedPort() + " e:" + e);
        } finally {
            try {
                in.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");
            }
            System.out.println("Connection with client Ip: " + getConnectedIp() + " Port: " +
                    getConnectedPort() + " closed");
        }
    }

    public void setStop(Boolean stop) {
        this.stop = stop;
        System.out.println("stop: " + stop);
    }

    private ArrayList<String> parseInput(String input){
        ArrayList<String> words = new ArrayList<>();
        if (input.equals("ping") || input.equals("pong")){
            words.add(input);
            return words;
        }

        ArrayList aList= new ArrayList(Arrays.asList(input.split(",")));

        System.out.println("nl parseInput aList: " + aList);
        if ((!aList.get(0).equals("newTweet") && !aList.get(0).equals("giveTweet")) ||
                ((aList.get(0).equals("newTweet") || aList.get(0).equals("giveTweet")) && (aList.get(2).equals("4")) )){
            return aList;
        }

        // deal with tweets with commas in them
        ArrayList<String> fullTweet = new ArrayList<>();
        int j = 0;
        String word = "";
        int length = 0;
        int whereInTweet = 0;
        for (int i = 0, n = input.length(); i < n; i++) {
            char c = input.charAt(i);
            if (j != 4 && j != 5){
                if (c == ','){
                    fullTweet.add(word);
                    word = "";
                    j++;
                }else if (i + 1 != n){
                    word = word + c;
                }else {
                    word = word + c;
                    fullTweet.add(word);
                }
            }else if (j == 4){
                if (c == ','){
                    fullTweet.add(word);
                    length = Integer.valueOf(word);
                    word = "";
                    j++;
                }else {
                    word = word + c;
                }
            }else if (j == 5){
                if (whereInTweet == length){
                    fullTweet.add(word);
                    word = "";
                    j++;
                }else {
                    word = word + c;
                    whereInTweet ++;
                }
            }
        }
        System.out.println("nl parseInput fullTweet: " + fullTweet);
        return fullTweet;
    }

}
