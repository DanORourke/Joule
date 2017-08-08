package DB;

import ReadWrite.MathStuff;
import Structures.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class SQLiteJDBC {
    private Connection c;
    private boolean firstTime;
    private String pubKey1;
    private String pubKey1Hash;
    private long seedTime;
    private String seedTarget;

    public SQLiteJDBC() {
        //connect to db
        this.c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:vault.db");
            DatabaseMetaData dm = c.getMetaData();

            System.out.println("Driver name: " + dm.getDriverName());
            System.out.println("Driver version: " + dm.getDriverVersion());
            System.out.println("Product name: " + dm.getDatabaseProductName());
            System.out.println("Product version: " + dm.getDatabaseProductVersion());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Opened database successfully");

        //set up db if first time opened
        initializeDb();

    }

    private synchronized void initializeDb(){
        //set up db if first time opened, boolean firstTime affects network procedure
        Statement stmt = null;
        //set up first block info
        pubKey1  = "MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAE8JX01CgitQi8cAR15nsgU/" +
                "gFEZoX3jDMcWe9OhbqTOPM2dwPjM90oK3o6rCZu3Ks";
        pubKey1Hash = new MathStuff().createHash(pubKey1);
        //seedTime = 1501421294121L;
        seedTime = 1501553629121L;
        seedTarget = "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT name FROM sqlite_master " +
                    "WHERE type='table' ORDER BY name;" );
            if (rs.isBeforeFirst() ) {
                rs.close();
                stmt.close();
                System.out.println("Has data");
                firstTime = false;
            }
            else {
                rs.close();
                stmt.close();
                System.out.println("No data");
                firstTime = true;
                // set up db
                createTables();
                //turn on to give server access to first block
                //addSeedUsers();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized boolean getFirstTime(){
        return firstTime;
    }

    private synchronized void createTables(){
        // create tables in the db
        createUserTable();
        createFriendsTable();
        createBlockChain();
        createTXTable();
        createTXITable();
        createTXOTable();
        createMapTable();
        createOpenTxoTable();
        createProfileTable();
        createFollowTable();
        createNetworkTable();
        //add first block and network contact
        addSeeds();

    }

    private synchronized void createNetworkTable(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE NETWORKTABLE " +
                    " (ID INTEGER PRIMARY KEY, " +
                    " USERNAME TEXT NOT NULL, " +
                    " NETWORKNAME TEXT NOT NULL, " +
                    " IP TEXT, " +
                    " PORT TEXT, " +
                    " NETNAME TEXT, " +
                    "UNIQUE (USERNAME, NETWORKNAME))";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("NETWORKTABLE Table created successfully");
    }

    private synchronized void createFollowTable(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE FOLLOWTABLE " +
                    " (ID INTEGER PRIMARY KEY, " +
                    " USERNAME TEXT NOT NULL, " +
                    " FOLLOWPUBKEYHASH TEXT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("FOLLOWTABLE Table created successfully");
    }

    private synchronized void addSeeds(){
        //add first block
        addSeedBlock();
        //add network contact
        addSeedNodes();

    }

    private synchronized void createOpenTxoTable(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE OPENTXOTABLE " +
                    " (ID INTEGER PRIMARY KEY, " +
                    " TXHASH TEXT NOT NULL, " +
                    " TXOINDEX INT NOT NULL, " +
                    " HEADERHASH TEXT NOT NULL, " +
                    " COINNUMBER INT NOT NULL, " +
                    " PUBKEYHASH TEXT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("OPENTXOTABLE Table created successfully");
    }

    private synchronized void createProfileTable(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE PROFILETABLE " +
                    " (ID INTEGER PRIMARY KEY, " +
                    " PUBKEYHASH TEXT UNIQUE NOT NULL, " +
                    " PUBKEY TEXT UNIQUE NOT NULL, " +
                    " NAME TEXT NOT NULL, " +
                    " ABOUT TEXT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("PROFILETABLE Table created successfully");
    }

    private synchronized void createMapTable(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE MAPTABLE " +
                    " (ID INTEGER PRIMARY KEY, " +
                    " HEADERHASH TEXT NOT NULL, " +
                    " TXHASH TEXT NOT NULL, " +
                    " TXINDEX INT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("MAPTABLE Table created successfully");
    }

    private synchronized void createTXTable(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE TXTABLE " +
                    " (ID INTEGER PRIMARY KEY," +
                    " TXHASH TEXT NOT NULL UNIQUE, " +
                    " TYPE INT NOT NULL, " +
                    " UNLOCK TEXT NOT NULL, " +
                    " REPORTLENGTH INT NOT NULL, " +
                    " REPORT TEXT NOT NULL, " +
                    " NUMBERTOMINER INT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("TXTABLE Table created successfully");
    }

    private synchronized void createTXOTable(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE TXOTABLE " +
                    " (ID INTEGER PRIMARY KEY," +
                    " TXHASH TEXT NOT NULL, " +
                    " TXOINDEX INT NOT NULL, " +
                    " NUMBERTOTXO INT NOT NULL, " +
                    " TXOPUBKEYHASH TEXT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("TXTABLE Table created successfully");
    }

    private synchronized void createTXITable(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE TXITABLE " +
                    " (ID INTEGER PRIMARY KEY, " +
                    " TXHASH TEXT NOT NULL, " +
                    " TXIINDEX INT NOT NULL, " +
                    " TXIHASH TEXT NOT NULL, " +
                    " TXITXOINDEX INT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("TXITABLE Table created successfully");
    }

    private synchronized void createBlockChain(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE BLOCKCHAIN " +
                    " (ID INTEGER PRIMARY KEY, " +
                    " HEADERHASH TEXT NOT NULL UNIQUE, " +
                    " HEIGHT INT NOT NULL, " +
                    " MERKLEROOT TEXT NOT NULL, " +
                    " PREVIOUSHASH TEXT NOT NULL, " +
                    " TARGET TEXT NOT NULL, " +
                    " NONCE INT NOT NULL, " +
                    " CHAIN INT NOT NULL)";
            // Chain and Id are not part of the header
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("BLOCKCHAIN Table created successfully");
    }


    private synchronized void createFriendsTable(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE FRIENDS " +
                    " (IP TEXT NOT NULL, " +
                    " PORT INT NOT NULL, " +
                    " NETNAME TEXT NOT NULL, " +
                    " LASTCONTACT INT, " +
                    " NAMEOFNETWORK TEXT NOT NULL, " +
                    "UNIQUE (IP, PORT, NAMEOFNETWORK))";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("FRIENDS Table created successfully");
    }

    private synchronized void addSeedBlock(){
        //create Joulebse
        JouleBase base = new JouleBase(seedTime);
        //add the txo to the tx
        base.addTxo(0, 25, pubKey1);
        //create header
        Header header = new Header(0, "0", seedTarget, 0);
        AllTx allTx = new AllTx();
        //create the merkle root
        header.setMerkleRoot(allTx.calcMerkleRoot(base.getHash()));
        //calculate header hash
        header.calculateHash();
        //create block
        Block block = new Block(header, base, allTx);
        //add block to the database
        addBlock(block);
    }

    private synchronized void addSeedNodes(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "INSERT INTO FRIENDS (IP,PORT,NETNAME, NAMEOFNETWORK) VALUES " +
                    "('73.246.234.225', 54332, 'Joule', 'outside');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("Seed node added");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void addSeedUsers(){

    }

    private synchronized void createUserTable(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE USER " +
                    " (USERNAME TEXT NOT NULL, " +
                    " SALT TEXT NOT NULL, " +
                    " HASH TEXT NOT NULL, " +
                    " PUBKEY TEXT NOT NULL, " +
                    " PRIVKEY TEXT NOT NULL, " +
                    " PUBKEYHASH TEXT NOT NULL, " +
                    " REPORTREWARD INT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("USER Table created successfully");
    }

    public synchronized Boolean login(String username, String password){
        ArrayList<String> user = getUserInfo(username);

        if (user == null || user.isEmpty()) {
            return false;
        }else{
            String recordName = user.get(0);
            String recordSalt = user.get(1);
            String recordHash = user.get(2);
            String hashedPass = new MathStuff().createPassHash(recordSalt, password);
            if (username.equals(recordName) && recordHash.equals(hashedPass)){
                return true;
            }else{
                return false;
            }
        }
    }

    public synchronized Boolean newUser(String username, String password){
        ArrayList<String> user = getUserInfo(username);

        if (user == null || !user.isEmpty() || username.equals("") || password.equals("")) {
            return false;
        }else{
            ArrayList<String> newUser = new MathStuff().createNewUser(password);
            addUser(username, newUser.get(0), newUser.get(1), newUser.get(2),
                    newUser.get(3), newUser.get(4), newUser.get(5));
            return true;
        }

    }

    private synchronized void addUser(String username, String salt, String hashPass, String pubKey, String privKey,
                         String pubKeyHash, String coinPerTweet){
        Statement stmt = null;
        try {
            stmt = c.createStatement();

            String sql = "INSERT INTO USER (USERNAME,SALT,HASH, PUBKEY, PRIVKEY, PUBKEYHASH, REPORTREWARD) " +
                    "VALUES ('" + username + "', '" + salt + "', '" + hashPass + "', '" +
                    pubKey + "', '" + privKey + "', '" + pubKeyHash + "', '" + coinPerTweet + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("User added");

            Statement stmt2 = c.createStatement();

        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        updateProfile(pubKey);
        alternateFollow(pubKeyHash, "YES", username);
        if (!pubKeyHash.equals(pubKey1Hash)){
            alternateFollow(pubKey1Hash, "YES", username);
        }
    }

    public synchronized ArrayList<String> getUserInfo(String username){
        ArrayList<String> user = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM USER WHERE USERNAME = '" + username + "';" );
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    user.addAll(Arrays.asList(rs.getString("USERNAME"),
                            rs.getString("SALT"), rs.getString("HASH"),
                            rs.getString("PUBKEY"), rs.getString("PRIVKEY"),
                            rs.getString("PUBKEYHASH"),
                            String.valueOf(rs.getInt("REPORTREWARD"))));
                }
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("db.getUserInfo username: " + username + "user: " + user);
        return user;
    }

    public synchronized ArrayList<ArrayList> getFriends(String nameOfNetwork){
        ArrayList<ArrayList> friends = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM FRIENDS WHERE NAMEOFNETWORK = '" + nameOfNetwork +
                    "' ORDER BY LASTCONTACT DESC LIMIT 50;" );
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    ArrayList<String> friend = new ArrayList<>();
                    friend.add(rs.getString("IP"));
                    friend.add(String.valueOf(rs.getInt("PORT")));
                    friend.add(rs.getString("NETNAME"));
                    friends.add(friend);
                }
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return friends;
        }
        return friends;
    }

    public synchronized void updateLastContact(String ip, int port, String netName, String nameOfNetWork, long date){

        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE FRIENDS SET LASTCONTACT = " + date + ", NETNAME = '" + netName + "' " +
                    "WHERE IP = '" + ip + "' AND PORT = " + port + " AND NAMEOFNETWORK = '" + nameOfNetWork + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addFriends(ArrayList<String> friends, String nameOfNetwork){
        int i = 1;
        while (i < friends.size()){
            String ip = friends.get(i);
            int port = Integer.valueOf(friends.get(i + 1));
            String netName = friends.get(i + 2);
            newFriend(ip, port, netName, nameOfNetwork, 0);
            i = i + 3;
        }
    }

    public synchronized boolean newFriend(String ip , int port, String netName, String nameOfNetwork, int code){
        long time = 0;
        if (code != 0){
            time = new Date().getTime();
        }
        System.out.println("db.newFriend ip: " + ip + " port: " + port + " netName: " + netName +
                " nameOfNetWork: " + nameOfNetwork + " time: " + time);

        PreparedStatement preparedStatement = null;
        int entered = 0;
        try {
            c.setAutoCommit(false);
            String sql = "INSERT OR IGNORE INTO FRIENDS (IP, PORT, NETNAME, NAMEOFNETWORK, LASTCONTACT) VALUES " +
                    "('" + ip + "', " + port + ", '" + netName + "', '" + nameOfNetwork + "', " + time + ");";

            preparedStatement = c.prepareStatement(sql);
            entered = preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return entered == 1;
    }

    public synchronized ArrayList<String> getBlockHeight(){
        ArrayList<String> info = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            c.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT * FROM BLOCKCHAIN WHERE HEIGHT = (SELECT MAX(HEIGHT) FROM BLOCKCHAIN);");
            if (rs.isBeforeFirst()){
                info.add(rs.getString("HEIGHT"));
                info.add(rs.getString("HEADERHASH"));
            }
            stmt.close();
            rs.close();
            return info;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }

    public synchronized ArrayList<Block> getAllBlocks(int height){
        //get the header hash of all headers at this height
        ArrayList<String> allHeaders = getHeadersAtHeight(height);
        //add all of those blocks into a list
        ArrayList<Block> allBlocks = new ArrayList<>();
        for (String headerHash : allHeaders){
            allBlocks.add(getBlock(headerHash));
        }
        return allBlocks;
    }

    private synchronized Block getBlock(String headerHash){
        //get header
        Header header = getHeader(headerHash);
        // get JouleBase
        JouleBase jouleBase = getJouleBase(headerHash);
        // get allTx
        AllTx allTx = getAllTx(headerHash);
        //form block
        Block block = new Block(header, jouleBase, allTx);
        block.printBlock();
        return block;
    }

    private synchronized AllTx getAllTx(String headerHash){
        AllTx allTx = new AllTx();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM TXTABLE WHERE TXHASH IN " +
                    "(SELECT TXHASH FROM MAPTABLE WHERE " +
                    "HEADERHASH = '" + headerHash + "' AND " +
                    "TXINDEX != 0);");
            if (rs.isBeforeFirst()){
                while(rs.next()){
                    allTx.addTx(new Tx(
                            rs.getString("TXHASH"),
                            rs.getInt("TYPE"),
                            rs.getString("UNLOCK"),
                            rs.getInt("REPORTLENGTH"),
                            rs.getString("REPORT"),
                            rs.getInt("NUMBERTOMINER")
                    ));
                }
            }else {
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allTx;
    }

    private synchronized JouleBase getJouleBase(String headerHash){
        //get Tx
        Statement stmt = null;
        JouleBase jouleBase = new JouleBase();
        boolean good = false;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM TXTABLE WHERE TXHASH = " +
                    "(SELECT TXHASH FROM MAPTABLE WHERE HEADERHASH = '" + headerHash + "' " +
                    "AND TXINDEX = 0);");
            if (rs.isBeforeFirst()){
                //construct Tx
                jouleBase = new JouleBase(rs.getString("TXHASH"), rs.getInt("TYPE"),
                        rs.getString("UNLOCK"), rs.getInt("REPORTLENGTH"),
                        rs.getString("REPORT"), rs.getInt("NUMBERTOMINER"));
                rs.close();
                stmt.close();
                good = true;
            }else {
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (good){
            //get allTxo
            AllTxo allTxo = getAllTxo(jouleBase.getHash());
            jouleBase.setAllTxo(allTxo);
            return jouleBase;
        }
        //TODO come up with better catch if something bad happens here
        return new JouleBase();
    }

    public synchronized Header getHeader(String headerHash){
        Statement stmt = null;
        Header header = new Header(false);
        try {
            stmt = c.createStatement();
            c.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT * FROM BLOCKCHAIN WHERE HEADERHASH =" +
                    " '" + headerHash + "';");
            if (rs.isBeforeFirst()){
                header = new Header(headerHash, rs.getInt("HEIGHT"), rs.getString("MERKLEROOT"),
                        rs.getString("PREVIOUSHASH"), rs.getString("TARGET"),
                        rs.getInt("NONCE"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return header;
    }

    private synchronized ArrayList<String> convertReportToProfile(String tweet){
        return new ArrayList(Arrays.asList(tweet.split("/////")));
    }

    private synchronized void updateProfile(String pubKey){
        String pubKeyHash = new MathStuff().createHash(pubKey);
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "INSERT OR IGNORE INTO PROFILETABLE (PUBKEYHASH, PUBKEY, NAME, ABOUT) VALUES " +
                    "('" + pubKeyHash + "', '" + pubKey + "', 'NA', 'NA');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("ProfileReport added or ignored");
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    public synchronized ArrayList<String> getUserKeys(String username){
        ArrayList<String> keys = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT PUBKEY, PRIVKEY, PUBKEYHASH FROM USER WHERE USERNAME= '"
                    + username + "';");
            if (rs.isBeforeFirst()){
                while(rs.next()){
                    keys.add(rs.getString("PUBKEY"));
                    keys.add(rs.getString("PRIVKEY"));
                    keys.add(rs.getString("PUBKEYHASH"));
                }
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keys;
    }

    public synchronized AllTxi getNewGiveAllTxi(String username, int numberToGive){
        AllTxi allTxi = new AllTxi();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT OPENTXOTABLE.*, USER.* FROM OPENTXOTABLE " +
                    "INNER JOIN USER ON OPENTXOTABLE.PUBKEYHASH = USER.PUBKEYHASH " +
                    "WHERE " +
                    "USER.USERNAME = '" + username + "' " +
                    "AND " +
                    "OPENTXOTABLE.HEADERHASH IN (SELECT HEADERHASH FROM BLOCKCHAIN WHERE CHAIN = 1) " +
                    "ORDER BY OPENTXOTABLE.ID ASC;");
            if (rs.isBeforeFirst()){
                int i = 0;
                int loop = 0;
                int totalNeeded = 1;
                while(i < totalNeeded && rs.next()){
                    if (loop == 0){
                        int reportReward = rs.getInt("REPORTREWARD");
                        totalNeeded = reportReward + numberToGive;
                        allTxi.setReportReward(reportReward);
                        allTxi.setPubKey(rs.getString("PUBKEY"));
                        allTxi.setPrivKey(rs.getString("PRIVKEY"));
                    }
                    allTxi.addTxi(new Txi(rs.getString("TXHASH"),
                            rs.getInt("TXOINDEX")));
                    i += rs.getInt("COINNUMBER");
                    loop++;
                }
                // make sure have enough joules to spend
                if (i >= totalNeeded){
                    allTxi.setTotalJoules(i);
                }else{
                    allTxi = new AllTxi();
                }
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allTxi;
    }

    public synchronized AllTxi getNewReportAllTxi(String username){
        AllTxi allTxi = new AllTxi();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT OPENTXOTABLE.*, USER.* FROM OPENTXOTABLE " +
                    "INNER JOIN USER ON OPENTXOTABLE.PUBKEYHASH = USER.PUBKEYHASH " +
                    "WHERE " +
                    "USER.USERNAME = '" + username + "' " +
                    "AND " +
                    "OPENTXOTABLE.HEADERHASH IN (SELECT HEADERHASH FROM BLOCKCHAIN WHERE CHAIN = 1) " +
                    "ORDER BY OPENTXOTABLE.ID ASC;");
            if (rs.isBeforeFirst()){
                int i = 0;
                int loop = 0;
                int totalNeeded = 1;
                while(i < totalNeeded && rs.next()){
                    if (loop == 0){
                        totalNeeded =  rs.getInt("REPORTREWARD");
                        allTxi.setReportReward(totalNeeded);
                        allTxi.setPubKey(rs.getString("PUBKEY"));
                        allTxi.setPrivKey(rs.getString("PRIVKEY"));
                    }
                    allTxi.addTxi(new Txi(rs.getString("TXHASH"),
                            rs.getInt("TXOINDEX")));
                    i += rs.getInt("COINNUMBER");
                    loop++;
                }
                // make sure have enough joules to spend
                if (i >= totalNeeded){
                    allTxi.setTotalJoules(i);
                }else{
                    allTxi = new AllTxi();
                }
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allTxi;
    }

    public synchronized boolean verifyAllTxi(AllTxi allTxi){
        ArrayList<String> txList = new ArrayList<>();
        ArrayList<String> pubKeyList = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM OPENTXOTABLE " +
                    "WHERE " +
                    allTxi.getVerifySql() + " " +
                    "AND TXHASH IN (SELECT TXHASH FROM MAPTABLE WHERE HEADERHASH IN " +
                    "(SELECT HEADERHASH FROM BLOCKCHAIN WHERE CHAIN = 1));");
            if (rs.isBeforeFirst()){
                while(rs.next()) {
                    txList.add(rs.getString("TXHASH"));
                    pubKeyList.add(rs.getString("PUBKEYHASH"));
                }
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (allTxi.doTxListMatch(txList, pubKeyList)){
            allTxi.setPubKey(getPubKeyFromHash(pubKeyList.get(0)));
            return true;
        }
        return false;
    }

    public synchronized boolean isTxAcceptableForBlock(Tx tx, String previousBlockHash){
        Header previousHeader = getHeader(previousBlockHash);
        if (!previousHeader.isProper()){
            System.out.println("db.isTxAcceptableForBlock previousHeader empty");
            return false;
        }

        for (Txi txi : tx.getAllTxi().getAllTxi()){
            System.out.println("db.isTxAcceptableForBlock txiHash: " + txi.getTxiHash());
            //check if txi is in a parent block of the header and not spent somewhere else in the same chain
            if (!isTxiAcceptable(txi, previousHeader)){
                return false;
            }
        }
        return true;
    }

    private synchronized boolean isTxiAcceptable(Txi txi, Header previousHeader){
        //check if txi is in a parent block
        ArrayList<Header> txiHeaders = getHeaderOfTx(txi.getTxiHash());
        for (Header header : txiHeaders){
            if (!inSameChain(header.getHeaderHash(), header.getHeight(),
                    previousHeader.getHeaderHash(),previousHeader.getHeight())){
                return false;
            }
        }
        //check if txi has been spent somewhere else in a parent block
        ArrayList<Header> otherTxiHeaders = getHeaderOfCompetingTxi(txi);
        for (Header header : otherTxiHeaders){
            if (inSameChain(header.getHeaderHash(), header.getHeight(),
                    previousHeader.getHeaderHash(),previousHeader.getHeight())){
                return false;
            }
        }
        return true;
    }

    private synchronized ArrayList<Header> getHeaderOfCompetingTxi(Txi txi){
        ArrayList<Header> headers = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            c.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT * FROM BLOCKCHAIN WHERE HEADERHASH IN " +
                    "(SELECT HEADERHASH FROM MAPTABLE WHERE TXHASH IN " +
                    "(SELECT TXHASH FROM TXITABLE WHERE " +
                    "TXIHASH = '" + txi.getTxiHash() + "' AND " +
                    "TXITXOINDEX = " + txi.getTxiTxoIndex() + " AND " +
                    "TXHASH != '" + txi.getTxHash() + "'));");
            if (rs.isBeforeFirst()){
                while(rs.next()){
                    headers.add(new Header(rs.getString("HEADERHASH"), rs.getInt("HEIGHT"),
                            rs.getString("MERKLEROOT"), rs.getString("PREVIOUSHASH"),
                            rs.getString("TARGET"), rs.getInt("NONCE")));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return headers;
    }

    private synchronized ArrayList<Header> getHeaderOfTx(String txHash){
        ArrayList<Header> headers = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            c.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT * FROM BLOCKCHAIN WHERE " +
                    "HEADERHASH IN " +
                    "(SELECT HEADERHASH FROM MAPTABLE WHERE " +
                    "TXHASH = '" + txHash + "');");
            if (rs.isBeforeFirst()){
                while(rs.next()){
                    headers.add(new Header(rs.getString("HEADERHASH"), rs.getInt("HEIGHT"),
                            rs.getString("MERKLEROOT"), rs.getString("PREVIOUSHASH"),
                            rs.getString("TARGET"), rs.getInt("NONCE")));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return headers;
    }

    private synchronized boolean inSameChain(String firstHeader, int firstHeight,
                                String secondHeader, int secondHeight){
        System.out.println("db.inSameChain firstHeader: " + firstHeader + " firstHeight: " + firstHeight +
                " secondHeader: " + secondHeader + " SecondHeight:" + secondHeight);
        int difference = secondHeight - firstHeight;
        if (difference < 0){
            return false;
        }
        if (difference == 0){
            return firstHeader.equals(secondHeader);
        }
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(constructInSameChainSql3(secondHeader, firstHeader, firstHeight));
            boolean exists = rs.isBeforeFirst();
            rs.close();
            stmt.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private synchronized String constructInSameChainSql3(String higherHeaderHash, String lowerHeaderHash,
                                                         int lowerHeight){
        String sql = "WITH RECURSIVE " +
                "same_chain(HEADERHASH) AS (" +
                "VALUES('" + higherHeaderHash + "') " +
                "UNION " +
                "SELECT BLOCKCHAIN.PREVIOUSHASH FROM BLOCKCHAIN, same_chain " +
                "WHERE BLOCKCHAIN.HEADERHASH = same_chain.HEADERHASH " +
                "AND BLOCKCHAIN.HEIGHT >= " + lowerHeight + ") " +
                "SELECT HEADERHASH FROM same_chain " +
                "WHERE HEADERHASH = '" + lowerHeaderHash + "'; ";
        return sql;
    }

    public synchronized String getName(String pubKeyHash){
        String name = "NA";
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT NAME FROM PROFILETABLE WHERE PUBKEYHASH = '" +
                    pubKeyHash + "';");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    name = rs.getString("NAME");
                }
                if (name.equals("NA")){
                    stmt.close();
                    rs.close();
                    return pubKeyHash;
                }
                stmt.close();
                rs.close();
                return name;
            }
            stmt.close();
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Other Guy";
    }

    public synchronized String getPubKeyHashFromTxiHash(String txiHash, int txiTxoHash){
        String pubKey = "";
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT TXOPUBKEYHASH FROM TXOTABLE WHERE TXHASH = '" +
                    txiHash + "' AND TXOINDEX = " + txiTxoHash + ";");
            if (rs.isBeforeFirst()){
                pubKey = rs.getString("TXOPUBKEYHASH");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pubKey;
    }

    public synchronized String getPubKeyFromHash(String pubKeyHash) {
        String pubKey = "";
        try {
            c.setAutoCommit(false);
            PreparedStatement stmt = c.prepareStatement( "SELECT PUBKEY FROM PROFILETABLE WHERE PUBKEYHASH = ?;");
            stmt.setString(1, pubKeyHash);
            ResultSet rs = stmt.executeQuery();
            if (rs.isBeforeFirst()){
                pubKey = rs.getString("PUBKEY");
            }else {
                pubKey = "NA";
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pubKey;
    }

    public synchronized boolean addBlock(Block block){
        //adding block, only need to add header and joulebase, rest is already in db
        boolean added = addHeader(block.getHeader());
        if (added){
            //add JouleBase
            added = addJouleBase(block.getJouleBase());
        }
        if (added){
            // update the chain numbers in blockchain
            updateChainNumbers();
            //update convenience tables
            mapNewBlock(block);
            updateOpenTxoTable(block);
        }
        System.out.println("Block added");
        return added;
    }

    private synchronized void updateOpenTxoTable(Block block){
        Header header = block.getHeader();
        addJouleBaseOpenTxo(block.getJouleBase(), header);
        for (Tx tx : block.getAllTx().getAllTx()){
            updateOpenTxo(tx, header);
            removeOpenTxo(tx);
        }
    }

    private synchronized void removeOpenTxo(Tx tx) {
        //remove the txo from opentxo this tx is spending in its txi
        for (Txi txi : tx.getAllTxi().getAllTxi()){
            Statement stmt = null;
            try {
                stmt = c.createStatement();

                String sql = "DELETE FROM OPENTXOTABLE WHERE TXHASH = '" + txi.getTxiHash() + "' AND TXOINDEX = " +
                        txi.getTxiTxoIndex() + ";";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                System.out.println("OpenTxo removed");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private synchronized void addOpenTxo(TxSuper tx) {
        String txHash = tx.getHash();
        for (Txo txo : tx.getAllTxo().getAllTxo()){
            Statement stmt = null;
            try {
                stmt = c.createStatement();

                String sql = "INSERT INTO OPENTXOTABLE (TXHASH, TXOINDEX, HEADERHASH, COINNUMBER, PUBKEYHASH) " +
                        "VALUES (" +
                        "'" + txHash + "', " + txo.getTxoIndex() + ", 'NA', " +
                        txo.getJoulesToTxo() + ", '" + txo.getTxoPubKeyHash() + "');";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                System.out.println("OpenTxo added");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private synchronized void addJouleBaseOpenTxo(TxSuper tx, Header header) {
        String txHash = tx.getHash();
        String headerHash = header.getHeaderHash();
        for (Txo txo : tx.getAllTxo().getAllTxo()){
            Statement stmt = null;
            try {
                stmt = c.createStatement();

                String sql = "INSERT INTO OPENTXOTABLE (TXHASH, TXOINDEX, HEADERHASH, COINNUMBER, PUBKEYHASH) " +
                        "VALUES (" +
                        "'" + txHash + "', " + txo.getTxoIndex() + ", '" + headerHash + "', " +
                        txo.getJoulesToTxo() + ", '" + txo.getTxoPubKeyHash() + "');";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                System.out.println("JouleBase OpenTxo updated");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private synchronized void updateOpenTxo(TxSuper tx, Header header) {
        String headerHash = header.getHeaderHash();
        for (Txo txo : tx.getAllTxo().getAllTxo()){
            Statement stmt = null;
            //System.out.println(headerHash + " " + txo.getTxHash() + " " + txo.getTxoIndex());
            try {
                stmt = c.createStatement();

                String sql = "UPDATE OPENTXOTABLE SET HEADERHASH  = '" + headerHash + "' WHERE TXHASH = '"
                        + txo.getTxHash() + "' AND TXOINDEX = " + txo.getTxoIndex() + ";";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                System.out.println("OpenTxo tx updated");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private synchronized void mapNewBlock(Block block){
        Header header = block.getHeader();
        mapTx(block.getJouleBase(), header);
        for (Tx tx : block.getAllTx().getAllTx()){
            mapTx(tx, header);
        }
    }

    private synchronized void mapTx(TxSuper tx, Header header){
        Statement stmt = null;
        try {
            stmt = c.createStatement();

            String sql = "INSERT INTO MAPTABLE (HEADERHASH, TXHASH, TXINDEX) VALUES (" +
                    "'" + header.getHeaderHash() + "', " +
                    "'" + tx.getHash() + "', " + tx.getTxIndex() + ");";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("map added");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private synchronized boolean addHeader(Header header){
        //add the header of the block
        int entered = 0;
        try {
            PreparedStatement preparedStatement = null;
            String sql = "INSERT OR IGNORE INTO BLOCKCHAIN (HEADERHASH, HEIGHT, " +
                    "MERKLEROOT, PREVIOUSHASH, TARGET, NONCE, CHAIN) VALUES " +
                    "('" + header.getHeaderHash() + "', " + header.getHeight() + ", " +
                    "'" + header.getMerkleRoot() + "', '" + header.getPreviousHash() + "', '" + header.getTarget() +
                    "', " + header.getNonce() + ", '1');";
            preparedStatement = c.prepareStatement(sql);
            entered = preparedStatement.executeUpdate();
            preparedStatement.close();
            c.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
        return (entered == 1);
    }


    public synchronized boolean addTx(Tx tx){
        //add tx
        int entered = 0;
        try {
            PreparedStatement preparedStatement = null;
            String sql = "INSERT OR IGNORE INTO TXTABLE (TXHASH, TYPE, UNLOCK, REPORTLENGTH, REPORT, NUMBERTOMINER) " +
                    "VALUES " +
                    "('" + tx.getHash() + "', " + tx.getType() + ", '" + tx.getSignature() +
                    "', " + tx.getReportLength() + ", ?, " + tx.getNumberToMiner() + ");";
            preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, tx.getReport());
            entered = preparedStatement.executeUpdate();
            preparedStatement.close();
            c.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
        if (entered == 1){
            //add all txo of tx to db
            addAllTxo(tx.getAllTxo());
            //add all txi of tx to db
            addAllTxi(tx.getAllTxi());
            //remove txo referenced in txi from open txo
            removeOpenTxo(tx);
            //add txo to open txo table with a placeholder header hash
            addOpenTxo(tx);
            //change profiles if profile report
            if (tx.getType() == 3){
                updateProfileTable(tx);
            }
        }
        return (entered == 1);
    }


    private synchronized void updateProfileTable(Tx tx) {
        ArrayList<String> nameAbout = convertReportToProfile(tx.getReport());
        //get the first txi, they all have the same pubkey but this one will always exist
        Txi txi = tx.getAllTxi().getTxi(0);
        PreparedStatement stmt = null;
        try {
            stmt = c.prepareStatement("UPDATE PROFILETABLE SET NAME = ?, ABOUT = ? WHERE PUBKEYHASH = " +
                    "(SELECT TXOPUBKEYHASH FROM TXOTABLE WHERE TXHASH = '" + txi.getTxiHash() +
                    "' AND TXOINDEX = '" + txi.getTxiTxoIndex() + "');");
            stmt.setString(1, nameAbout.get(0));
            stmt.setString(2, nameAbout.get(1));
            stmt.executeUpdate();
            stmt.close();
            c.commit();
            System.out.println("ProfileTable updated");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private synchronized boolean addJouleBase(JouleBase base){
        //add JouleBase
        int entered = 0;
        try {
            PreparedStatement preparedStatement = null;
            String sql = "INSERT OR IGNORE INTO TXTABLE (TXHASH, TYPE, UNLOCK, REPORTLENGTH, REPORT, NUMBERTOMINER) " +
                    "VALUES " +
                    "('" + base.getHash() + "', " + base.getType() + ", '" + base.getSignature() +
                    "', " + base.getReportLength() + ", ?, " + base.getNumberToMiner() + ");";
            preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, base.getReport());
            entered = preparedStatement.executeUpdate();
            preparedStatement.close();
            c.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
        if (entered == 1){
            //add all txo of JouleBase to db
            addAllTxo(base.getAllTxo());
        }
        return (entered == 1);
    }

    private synchronized void addAllTxi(AllTxi allTxi){
        //add all new txi
        for (Txi txi: allTxi.getAllTxi()){
            Statement stmt = null;
            try {
                stmt = c.createStatement();

                String sql = "INSERT INTO TXITABLE (TXHASH, TXIINDEX, TXIHASH, TXITXOINDEX) " +
                        "VALUES ('" + txi.getTxHash() + "', " + txi.getTxiIndex() + ", '" +
                        txi.getTxiHash() + "', " + txi.getTxiTxoIndex() + ");";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private synchronized void addAllTxo(AllTxo allTxo){
        //add all new txo
        for (Txo txo: allTxo.getAllTxo()){
            Statement stmt = null;
            try {
                stmt = c.createStatement();

                String sql = "INSERT INTO TXOTABLE (TXHASH, NUMBERTOTXO, TXOINDEX, TXOPUBKEYHASH) " +
                        "VALUES ('" + txo.getTxHash() + "', " + txo.getJoulesToTxo() + ", " + txo.getTxoIndex() +
                        ", '" + txo.getTxoPubKeyHash() + "');";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                //add pubkey mapping to profile table if not already there
                updateProfile(txo.getTxoPubKey());
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private synchronized void updateChainNumbers(){
        resetChain3();
        resetChainLoop(getHighestHeaders());
    }

    private synchronized void resetChain3(){
        Statement stmt = null;
        try {
            stmt = c.createStatement();

            String sql = "UPDATE BLOCKCHAIN SET CHAIN  = '3';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("Chain updated");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private synchronized ArrayList<String> getHighestHeaders(){
        ArrayList<String> info = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            c.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT HEADERHASH, HEIGHT FROM BLOCKCHAIN WHERE HEIGHT = " +
                    "(SELECT MAX(HEIGHT) FROM BLOCKCHAIN);");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    info.add(rs.getString("HEADERHASH"));
                }
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }

    private synchronized void resetChainLoop(ArrayList<String> highestHeaders){

        int i = highestHeaders.size() - 1;
        while (i >= 0){
            if (i != 0){
                resetChain(2, highestHeaders.get(i));
            }else{
                resetChain(1, highestHeaders.get(i));
            }
            i--;
        }
    }

    private synchronized void resetChain(int number, String headerHash) {
        String sql = constructResetChainSql(number, headerHash);
        Statement stmt = null;
        try {
            c.close();
            c = DriverManager.getConnection("jdbc:sqlite:vault.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private synchronized String constructResetChainSql(int number, String headerHash){
        String sql = "WITH RECURSIVE " +
                "same_chain(HEADERHASH) AS (" +
                "VALUES('" + headerHash + "') " +
                "UNION " +
                "SELECT BLOCKCHAIN.PREVIOUSHASH FROM BLOCKCHAIN, same_chain " +
                "WHERE BLOCKCHAIN.HEADERHASH = same_chain.HEADERHASH) " +
                "UPDATE BLOCKCHAIN SET CHAIN = " + number + " WHERE HEADERHASH IN " +
                "(SELECT HEADERHASH FROM same_chain); ";
        return sql;
    }

    public synchronized Tx getTx(String txHash){
        //get allTxi
        AllTxi allTxi = getAllTxi(txHash);
        //get allTxo
        AllTxo allTxo = getAllTxo(txHash);

        //get Tx
        Statement stmt = null;
        Tx tx = new Tx(false);
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM TXTABLE WHERE TXHASH = '" + txHash + "';");
            if (rs.isBeforeFirst()){
                //construct Tx
                tx = new Tx(rs.getString("TXHASH"), rs.getInt("TYPE"),
                        rs.getString("UNLOCK"), rs.getInt("REPORTLENGTH"),
                        rs.getString("REPORT"), rs.getInt("NUMBERTOMINER"), allTxi, allTxo);
                rs.close();
                stmt.close();
            }else {
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tx;
    }

    private synchronized AllTxi getAllTxi(String txHash){
        AllTxi allTxi = new AllTxi();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM TXITABLE WHERE TXHASH = '" + txHash + "';");
            if (rs.isBeforeFirst()){
                while (rs.next()){

                    allTxi.addTxi(new Txi(txHash, rs.getInt("TXIINDEX"),
                            rs.getString("TXIHASH"), rs.getInt("TXITXOINDEX")));
                }
                rs.close();
                stmt.close();
            }else {
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allTxi;
    }

    private synchronized AllTxo getAllTxo(String txHash){
        AllTxo allTxo = new AllTxo();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM TXOTABLE LEFT OUTER JOIN PROFILETABLE ON " +
                    "TXOTABLE.TXOPUBKEYHASH = PROFILETABLE.PUBKEYHASH WHERE TXHASH = '" + txHash + "';");
            if (rs.isBeforeFirst()){
                while (rs.next()){

                    allTxo.addTxo(new Txo(txHash, rs.getInt("TXOINDEX"),
                            rs.getInt("NUMBERTOTXO"), rs.getString("PUBKEY")));
                }
                rs.close();
                stmt.close();
            }else {
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allTxo;
    }

    public synchronized ArrayList<String> cullBlockTxList(ArrayList<String> txList){
        if (txList.isEmpty()){
            return new ArrayList<>();
        }

        ArrayList<String> newTxList = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(constructCullBlockTxListSQL(txList));
            if (rs.isBeforeFirst()){
                newTxList.addAll(txList);
                while (rs.next()){
                    String hash = rs.getString("TXHASH");
                    if (newTxList.contains(hash)){
                        newTxList.remove(hash);
                    }
                }
                rs.close();
                stmt.close();
                return newTxList;
            }else {
                rs.close();
                stmt.close();
                return txList;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        newTxList.add("no");
        return newTxList;
    }

    private synchronized String constructCullBlockTxListSQL(ArrayList<String> txList){
        String sql = "SELECT TXHASH FROM TXTABLE WHERE ";
        for (int i = 0; i < txList.size(); i ++){
            if (i != txList.size() - 1){
                sql = sql + "TXHASH = '" + txList.get(i) + "' OR ";
            }else {
                sql = sql + "TXHASH = '" + txList.get(i) + "';";
            }
        }
        return sql;
    }

    public synchronized ArrayList<String> getPastHeaderHashes(int height){
        ArrayList<String> pastHeaderHashes = new ArrayList<>();
        Statement stmt = null;
        int lowerBound = height - 10;
        if (lowerBound < 0){
            lowerBound = 0;
        }
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT HEADERHASH, HEIGHT FROM BLOCKCHAIN WHERE HEIGHT BETWEEN "
                    + lowerBound + " AND " + height + ";");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    pastHeaderHashes.add(String.valueOf(rs.getInt("HEIGHT")));
                    pastHeaderHashes.add(rs.getString("HEADERHASH"));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("db.getPastHeaderHashes pastHeaderHashes: " + pastHeaderHashes);
        return pastHeaderHashes;
    }

    public synchronized ArrayList<ArrayList> searchProfileHash(String hash){
        ArrayList<ArrayList> profiles = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM PROFILETABLE WHERE PUBKEYHASH = '" + hash + "';");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    ArrayList<String> profile = new ArrayList<>();
                    profile.addAll(Arrays.asList(rs.getString("PUBKEYHASH"),
                            rs.getString("NAME"), rs.getString("ABOUT")));
                    profiles.add(profile);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return profiles;
    }

    public synchronized ArrayList<ArrayList> searchProfileAbout(String about, int startingPoint){
        ArrayList<ArrayList> profiles = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM PROFILETABLE WHERE ABOUT LIKE '%" + about +
                    "%' LIMIT 5 OFFSET " + startingPoint + ";");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    ArrayList<String> profile = new ArrayList<>();
                    profile.addAll(Arrays.asList(rs.getString("PUBKEYHASH"),
                            rs.getString("NAME"), rs.getString("ABOUT")));
                    profiles.add(profile);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return profiles;
    }

    public synchronized ArrayList<ArrayList> getReporters(String username){
        ArrayList<ArrayList> profiles = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM PROFILETABLE WHERE PUBKEYHASH IN " +
                    "(SELECT FOLLOWPUBKEYHASH FROM FOLLOWTABLE WHERE USERNAME = '" + username + "');");
            //
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    ArrayList<String> profile = new ArrayList<>();
                    profile.addAll(Arrays.asList(rs.getString("PUBKEYHASH"),
                            rs.getString("NAME"), rs.getString("ABOUT")));
                    profiles.add(profile);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return profiles;
    }

    public synchronized ArrayList<ArrayList> searchProfileName(String name, int startingPoint){
        ArrayList<ArrayList> profiles = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM PROFILETABLE WHERE NAME LIKE '%" + name +
                    "%' LIMIT 5 OFFSET " + startingPoint + ";");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    ArrayList<String> profile = new ArrayList<>();
                    profile.addAll(Arrays.asList(rs.getString("PUBKEYHASH"),
                            rs.getString("NAME"), rs.getString("ABOUT")));
                    profiles.add(profile);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return profiles;
    }

    public synchronized ArrayList<ArrayList> searchTweets(String search, int startNumber) {
        ArrayList<ArrayList> tweets = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT TXTABLE.TXHASH, TXTABLE.REPORT, " +
                    "PROFILETABLE.NAME, PROFILETABLE.PUBKEYHASH " +
                    "FROM TXTABLE LEFT OUTER JOIN TXITABLE ON TXTABLE.TXHASH = TXITABLE.TXHASH " +
                    "LEFT OUTER JOIN TXOTABLE ON TXITABLE.TXIHASH = TXOTABLE.TXHASH " +
                    "LEFT OUTER JOIN PROFILETABLE ON TXOTABLE.TXOPUBKEYHASH = PROFILETABLE.PUBKEYHASH " +
                    "WHERE TXTABLE.TYPE = '2' AND TXITABLE.TXITXOINDEX = TXOTABLE.TXOINDEX AND " +
                    "REPORT LIKE '%" + search + "%' ORDER BY TXTABLE.ID DESC LIMIT 100 OFFSET " +
                    startNumber + ";");

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    ArrayList<String> tweet = new ArrayList<>();
                    tweet.add(rs.getString("NAME"));
                    tweet.add(rs.getString("REPORT"));
                    tweet.add(rs.getString("PUBKEYHASH"));
                    tweet.add(rs.getString("TXHASH"));
                    tweets.add(tweet);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tweets;
    }
    //TODO make these only show reports in the main chain
    public synchronized ArrayList<String> getPastTweetsResults(String pubKeyHash, int startingPoint){
        ArrayList<String> tweets = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT DISTINCT TXTABLE.TXHASH, TXTABLE.REPORT, " +
                    "PROFILETABLE.NAME, PROFILETABLE.PUBKEYHASH " +
                    "FROM TXTABLE LEFT OUTER JOIN TXITABLE ON TXTABLE.TXHASH = TXITABLE.TXHASH " +
                    "LEFT OUTER JOIN TXOTABLE ON TXITABLE.TXIHASH = TXOTABLE.TXHASH " +
                    "LEFT OUTER JOIN PROFILETABLE ON TXOTABLE.TXOPUBKEYHASH = PROFILETABLE.PUBKEYHASH " +
                    "WHERE TXTABLE.TYPE = 2 AND TXITABLE.TXITXOINDEX = TXOTABLE.TXOINDEX AND " +
                    "PROFILETABLE.PUBKEYHASH = '" + pubKeyHash + "' ORDER BY TXTABLE.ID DESC LIMIT 100 OFFSET " +
                    startingPoint + ";");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    tweets.add(rs.getString("REPORT"));
                    tweets.add(rs.getString("TXHASH"));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tweets;
    }

    public synchronized ArrayList<String> getPastTweetsFeed(int startingPoint, String username){
        ArrayList<String> tweets = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT DISTINCT TXTABLE.TXHASH, TXTABLE.REPORT, " +
                    "PROFILETABLE.NAME, PROFILETABLE.PUBKEYHASH " +
                    "FROM TXTABLE LEFT OUTER JOIN TXITABLE ON TXTABLE.TXHASH = TXITABLE.TXHASH " +
                    "LEFT OUTER JOIN TXOTABLE ON TXITABLE.TXIHASH = TXOTABLE.TXHASH " +
                    "LEFT OUTER JOIN PROFILETABLE ON TXOTABLE.TXOPUBKEYHASH = PROFILETABLE.PUBKEYHASH " +
                    "WHERE TXTABLE.TYPE = 2 AND TXITABLE.TXITXOINDEX = TXOTABLE.TXOINDEX AND " +
                    "PROFILETABLE.PUBKEYHASH IN (SELECT FOLLOWPUBKEYHASH FROM FOLLOWTABLE WHERE USERNAME = '" +
                    username + "') ORDER BY TXTABLE.ID DESC LIMIT 100 OFFSET " + startingPoint + ";");

            if (rs.isBeforeFirst()){
                while (rs.next()){
                    tweets.add(rs.getString("NAME"));
                    tweets.add(rs.getString("PUBKEYHASH"));
                    tweets.add(rs.getString("REPORT"));
                    tweets.add(rs.getString("TXHASH"));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tweets;
    }

    public synchronized String getTimeOfTweet(String txHash){
        String time = "?";
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT REPORT FROM TXTABLE WHERE TXHASH = " +
                    "(SELECT TXHASH FROM MAPTABLE WHERE TXINDEX = '0' AND HEADERHASH = " +
                    "(SELECT HEADERHASH FROM MAPTABLE WHERE TXHASH = '" + txHash + "'));");

            if (rs.isBeforeFirst()){
                while (rs.next()){
                    String stringRecord = rs.getString("REPORT");
                    long longRecord = Long.valueOf(stringRecord);
                    System.out.println("db getTimeOfTweet longRecord: " + longRecord);
                    Date date = new Date(longRecord);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
                    time = sdf.format(date);
                }
            }else {
                time = "New";
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return time;
    }

    public synchronized boolean doIFollow(String pubKeyHash, String username){
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM FOLLOWTABLE WHERE " +
                    "FOLLOWPUBKEYHASH = '" + pubKeyHash + "' AND USERNAME = '" + username + "';");

            if (rs.isBeforeFirst()){
                rs.close();
                stmt.close();
                return true;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized void alternateFollow(String pubKeyHash, String follow, String username){
        if (follow.equals("YES")){
            Statement stmt = null;
            try {
                stmt = c.createStatement();

                String sql = "INSERT INTO FOLLOWTABLE (USERNAME, FOLLOWPUBKEYHASH) VALUES " +
                        "('" + username + "', '" + pubKeyHash + "');";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                System.out.println("Follow alternated");
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            Statement stmt = null;
            try {
                stmt = c.createStatement();

                String sql = "DELETE FROM FOLLOWTABLE WHERE USERNAME = '" + username + "' AND FOLLOWPUBKEYHASH = '" +
                        pubKeyHash + "';";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                System.out.println("Follow alternated");
            }catch (Exception e){
                e.printStackTrace();
            }
        }


    }

    public synchronized ArrayList<String> createMyProfile(String username){
        ArrayList<String> profile = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM PROFILETABLE WHERE PUBKEYHASH = " +
                    "(SELECT PUBKEYHASH FROM USER WHERE USERNAME = '" + username + "');");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    profile.addAll(Arrays.asList(rs.getString("PUBKEYHASH"),
                            rs.getString("NAME"), rs.getString("ABOUT")));
                    rs.close();
                    stmt.close();
                    return profile;
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return profile;
    }

    public synchronized HashMap<String, String> getIpPort(String username, String networkName){
        HashMap<String, String> ipPort = new HashMap<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT IP, PORT, NETNAME FROM NETWORKTABLE WHERE USERNAME = '" +
                    username + "' AND NETWORKNAME = '" + networkName + "';");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                   ipPort.put( "ip", rs.getString("IP"));
                   ipPort.put("port", String.valueOf(rs.getInt("PORT")));
                   ipPort.put("netName", rs.getString("NETNAME"));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ipPort;
    }

    public synchronized boolean changeUsername(String oldUsername, String newUsername){
        ArrayList<String> conflict = getUserInfo(newUsername);
        if (newUsername.length() == 0 || !conflict.isEmpty()){
            return false;
        }else {
            Statement stmt = null;
            try {
                stmt = c.createStatement();
                String sql = "UPDATE USER SET USERNAME = " +
                        "('" + newUsername + "') WHERE USERNAME = '" + oldUsername + "';";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                stmt = c.createStatement();
                sql = "UPDATE FOLLOWTABLE SET USERNAME = " +
                        "('" + newUsername + "') WHERE USERNAME = '" + oldUsername + "';";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                stmt = c.createStatement();
                sql = "UPDATE NETWORKTABLE SET USERNAME = " +
                        "('" + newUsername + "') WHERE USERNAME = '" + oldUsername + "';";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                System.out.println("username updated");
                return true;
            }catch (Exception e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                return false;
            }
        }
    }

    public synchronized boolean changePassword(String username, String newPassword, String newPassword2){
        if (!newPassword.equals(newPassword2) || newPassword.length() == 0){
            return false;
        }
        ArrayList<String> newSaltPass = new MathStuff().createNewSaltPass(newPassword);
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "UPDATE USER SET SALT = '" + newSaltPass.get(0) + "', HASH = '" + newSaltPass.get(1) + "' " +
                    "WHERE USERNAME = '" + username + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("password updated");
            return true;
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        }
    }
    public synchronized boolean updateMyIp(String username, String myIp, String networkName){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "WITH new (USERNAME, IP, NETWORKNAME) AS ( VALUES('" + username + "', '" +
                    myIp + "', '" + networkName + "') ) " +
                    "INSERT OR REPLACE INTO NETWORKTABLE (ID, USERNAME, NETWORKNAME, IP, PORT, NETNAME) " +
                    "SELECT old.ID, new.USERNAME, new.NETWORKNAME, new.IP, old.PORT, old.NETNAME " +
                    "FROM new LEFT JOIN NETWORKTABLE AS old ON new.USERNAME = old.USERNAME AND " +
                    "new.NETWORKNAME = old.NETWORKNAME;";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("myIp updated");
            return true;
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        }

    }

    public synchronized boolean updateMyPort(String username, int myPort, String networkName){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "WITH new (USERNAME, PORT, NETWORKNAME) AS ( VALUES('" + username + "', '" +
                    myPort + "', '" + networkName + "') ) " +
                    "INSERT OR REPLACE INTO NETWORKTABLE (ID, USERNAME, NETWORKNAME, IP, PORT, NETNAME) " +
                    "SELECT old.ID, new.USERNAME, new.NETWORKNAME, old.IP, new.PORT, old.NETNAME " +
                    "FROM new LEFT JOIN NETWORKTABLE AS old ON new.USERNAME = old.USERNAME AND " +
                    "new.NETWORKNAME = old.NETWORKNAME;";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("myIp updated");
            return true;
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        }
    }

    public synchronized boolean updateNetName(String username, String netName, String networkName){
        PreparedStatement preparedStatement = null;
        try {
            c.setAutoCommit(false);
            String sql = "WITH new (USERNAME, NETNAME, NETWORKNAME) AS ( VALUES('" + username + "', ?, '"
                    + networkName + "') ) " +
                    "INSERT OR REPLACE INTO NETWORKTABLE (ID, USERNAME, NETWORKNAME, IP, PORT, NETNAME) " +
                    "SELECT old.ID, new.USERNAME, new.NETWORKNAME, old.IP, old.PORT, new.NETNAME " +
                    "FROM new LEFT JOIN NETWORKTABLE AS old ON new.USERNAME = old.USERNAME AND " +
                    "new.NETWORKNAME = old.NETWORKNAME;";
            preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, netName);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            c.commit();
            System.out.println("myNetName updated");
            return true;
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        }
    }

    public synchronized long getTimeOfPastBlock(int height){
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT REPORT FROM TXTABLE WHERE TXHASH = " +
                    "(SELECT TXHASH FROM MAPTABLE WHERE TXINDEX = 0 AND " +
                    "HEADERHASH = (SELECT HEADERHASH FROM BLOCKCHAIN WHERE CHAIN = 1 " +
                    "AND HEIGHT = " + height + " ));");
            if (rs.isBeforeFirst()){
                long time = Long.parseLong(rs.getString("REPORT"));
                rs.close();
                stmt.close();
                return time;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private synchronized ArrayList<String> getHeadersAtHeight(int height){
        ArrayList<String> blocks = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT HEADERHASH FROM BLOCKCHAIN WHERE HEIGHT = " + height + ";");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    blocks.add(rs.getString("HEADERHASH"));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return blocks;
    }

    public synchronized String getTxToSpend(String username){
        int coinsTotal = 0;
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT COINNUMBER FROM OPENTXOTABLE WHERE PUBKEYHASH = " +
                    "(SELECT PUBKEYHASH FROM USER WHERE USERNAME = '" + username + "') " +
                    "AND HEADERHASH IN (SELECT HEADERHASH FROM BLOCKCHAIN WHERE CHAIN = '1');");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    coinsTotal = coinsTotal + Integer.valueOf(rs.getString("COINNUMBER"));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.valueOf(coinsTotal);
    }

    public synchronized int getTxPerReport(String username){
        int reportReward = 1;
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT REPORTREWARD FROM USER WHERE USERNAME = '" + username + "';");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    reportReward = rs.getInt("REPORTREWARD");
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reportReward;
    }

    public synchronized void updateTxPerTweet(String username, String newReward){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "UPDATE USER SET REPORTREWARD = '" + newReward + "' WHERE USERNAME = '" + username + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("reportReward updated");
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    public synchronized ArrayList<ArrayList> getMyOpenTx(String username, int offset){
        ArrayList<ArrayList> openList = new ArrayList<>();
        Statement stmt = null;
        try {
            ArrayList<ArrayList> allOpen = new ArrayList<>();
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT OPENTXOTABLE.TXHASH, OPENTXOTABLE.COINNUMBER, " +
                    "OPENTXOTABLE.HEADERHASH, TXTABLE.TYPE, BLOCKCHAIN.HEIGHT FROM OPENTXOTABLE " +
                    "JOIN TXTABLE ON " +
                    "OPENTXOTABLE.TXHASH = TXTABLE.TXHASH " +
                    "JOIN BLOCKCHAIN ON " +
                    "OPENTXOTABLE.HEADERHASH = BLOCKCHAIN.HEADERHASH " +
                    "WHERE OPENTXOTABLE.PUBKEYHASH = " +
                    "(SELECT PUBKEYHASH FROM USER WHERE USERNAME = '" + username + "') AND " +
                    "BLOCKCHAIN.CHAIN = '1' " +
                    "ORDER BY TXTABLE.ID DESC LIMIT 100 OFFSET " + offset + ";");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    ArrayList<String> openTx = new ArrayList<>();
                    openTx.addAll(Arrays.asList(rs.getString("TXHASH"), rs.getString("TYPE"),
                            rs.getString("COINNUMBER"), rs.getString("HEADERHASH"),
                            rs.getString("HEIGHT")));
                    allOpen.add(openTx);
                }
            }

            rs.close();
            stmt.close();
            return allOpen;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return openList;
    }

    public synchronized boolean haveBlockHash(String headerHash){
        boolean have = false;
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT HEADERHASH FROM BLOCKCHAIN WHERE HEADERHASH = '" +
                    headerHash +"';");
            if (rs.isBeforeFirst()){
                have = true;

            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return have;
    }

    public synchronized AllTx getNewMinerBlockAllTx(int maxInBlock){
        AllTx allTx = new AllTx();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM TXTABLE WHERE TXHASH NOT IN " +
                    "(SELECT TXHASH FROM MAPTABLE) AND TYPE != '1';");
            if (rs.isBeforeFirst()){
                while(rs.next()){
                    allTx.addTx(new Tx(
                            rs.getString("TXHASH"),
                            rs.getInt("TYPE"),
                            rs.getString("UNLOCK"),
                            rs.getInt("REPORTLENGTH"),
                            rs.getString("REPORT"),
                            rs.getInt("NUMBERTOMINER")
                    ));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //tell each tx its index in the list
        allTx.setTxIndex();
        return allTx;
    }

    public Header getHighestHeader() {
        Header header = new Header();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM BLOCKCHAIN WHERE " +
                    "HEIGHT = (SELECT MAX(HEIGHT) FROM BLOCKCHAIN) " +
                    "AND CHAIN = 1;");
            if (rs.isBeforeFirst()){
                header = new Header(
                        rs.getString("HEADERHASH"),
                        rs.getInt("HEIGHT"),
                        rs.getString("MERKLEROOT"),
                        rs.getString("PREVIOUSHASH"),
                        rs.getString("TARGET"),
                        rs.getInt("NONCE")
                        );
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return header;
    }

    public boolean checkBlockHeader(String previousHash, int height) {
        boolean exists = false;
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT ID FROM BLOCKCHAIN WHERE " +
                    "HEIGHT = " + height + " " +
                    "AND HEADERHASH = '" + previousHash + "';");
            if (rs.isBeforeFirst()){
                exists = true;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }

    public void finishAllTx(AllTx allTx) {
        for (Tx tx : allTx.getAllTx()){
            tx.setAllTxo(getAllTxo(tx.getHash()));
            tx.setAllTxi(getAllTxi(tx.getHash()));
        }
    }
}
