import DB.SQLiteJDBC;
import Node.NodeBase;
import UI.FXGUI;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0  || (args.length == 1 && args[0].equals("-outside"))){
            //runs as normal, only talking to outside networks
            SQLiteJDBC db = new SQLiteJDBC();
            NodeBase nb = new NodeBase(db);
            FXGUI.begin(db, nb);
        }else if (args.length == 2 && args[1].equals("-outside")){
            //this runs in inside mode on the network passed as parameter
            System.out.println("Cannot name network 'outside'");
            System.out.println("Invalid Arguments\n" +
                    "-outside or without arguments will only talk to the outside world\n" +
                    "-inside, -argument will only talk on local network 'argument'\n" +
                    "-both, -argument will talk on the local network 'argument' and also to the outside world");
        }else if (args.length == 2 && args[0].equals("-inside")){
            //this runs in inside mode on the network passed as parameter
            SQLiteJDBC db = new SQLiteJDBC();
            String one = args[0].substring(1);
            String two = args[1].substring(1);
            NodeBase nb = new NodeBase(db, one, two);
            FXGUI.begin(db, nb);
        }else if (args.length == 2 && args[0].equals("-both")){
            //this runs in inside/outside mode on the network passed as parameter
            SQLiteJDBC db = new SQLiteJDBC();
            String one = args[0].substring(1);
            String two = args[1].substring(1);
            NodeBase nb = new NodeBase(db, one, two);
            FXGUI.begin(db, nb);
        }else {
            //tells user acceptable parameters
            System.out.println("Invalid Arguments\n" +
                    "-outside or without arguments will only talk to the outside world\n" +
                    "-inside, -argument will only talk on local network 'argument'\n" +
                    "-both, -argument will talk on the local network 'argument' and also to the outside world");
        }

    }
}
