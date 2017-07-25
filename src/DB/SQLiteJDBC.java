package DB;

import ReadWrite.Construct;
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
        pubKey1  = "MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEeQUp" +
                "DfRodKm9cLA1ZlsjsuP3n/bXuxo+GpVoavLgcI4prhyRBzCRfcAqtjjdWO2r";
        pubKey1Hash = new MathStuff().createHash(pubKey1);
        seedTime = 1493558407121L;
        seedTarget = "00fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
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
                addSeedUsers();
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

//        ArrayList<ArrayList> fullBlock = new ArrayList<>();
//        ArrayList<String> header = new ArrayList<>();
//        ArrayList<ArrayList> fullTweet = new ArrayList<>();
//        ArrayList<String> tx = new ArrayList<>();
//        ArrayList<ArrayList> txoList = new ArrayList<>();
//        ArrayList<String> txo = new ArrayList<>();
//        ArrayList<String> txList = new ArrayList<>();
//
//        String txHash = new MathStuff().createHash( "1" + "0" + "0" + seedTime + "0");
//        tx.addAll(Arrays.asList(txHash, "1", "0", "0", seedTime, "0"));
//        txo.addAll(Arrays.asList("25", "0", pubKey1));
//        txoList.add(txo);
//        fullTweet.add(tx);
//        fullTweet.add(txoList);
//
//        String merkle = new MathStuff().createBlockMerkleRoot(null, tx.get(0));
//        String headerhash = new MathStuff().createHash("0" + merkle + "0" + seedTarget + "0");
//        header.addAll(Arrays.asList(headerhash, "0", merkle, "0", seedTarget, "0"));
//
//        fullBlock.add(header);
//        fullBlock.add(fullTweet);
//        fullBlock.add(txList);
//
//        addFullBlock(fullBlock);

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
                    "('73.246.234.225', '54321', 'Joule', 'outside');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("Seed node added");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void addSeedUsers(){
        //alternateFollow(pubKey1Hash, "YES", "asd");
        String asdName = "asd";
        String asdSalt = "9608756233693360688593294264682270";
        String asdHash = "7e9ae91dd29b15ee423d9ca3c8c3cb378fbabdf5370c16629cc6314685414a92";
        String asdPrivKey = "MDkCAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQEEHzAdAgEBBBjSbOVB7EEdAo+Whh7BdT2tGKsSR9UEIUc=";
        String asdCoinPerTweet = "1";
        addUser(asdName, asdSalt, asdHash, pubKey1, asdPrivKey, pubKey1Hash, asdCoinPerTweet);

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

    public synchronized ArrayList<String> getMiningInfo(){
        ArrayList<String> miningInfo = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            c.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT * FROM BLOCKCHAIN WHERE HEIGHT = " +
                    "(SELECT MAX(HEIGHT) FROM BLOCKCHAIN) ORDER BY HEADERHASH ASC;");
            miningInfo.add(String.valueOf(rs.getInt("HEIGHT")));
            miningInfo.add((rs.getString("HEADERHASH")));
            miningInfo.add(rs.getString("TARGET"));
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return miningInfo;
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

    private synchronized Header getHeader(String headerHash){
        Statement stmt = null;
        Header header = new Header();
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

    public synchronized ArrayList<ArrayList> getAllFullBlocks(int height){
        //TODO can I get all this in one call??
        ArrayList<ArrayList> allFullBlocks = new ArrayList<>();
        ArrayList<ArrayList> allBlockHeaders = getAllBlockHeaders(height);
        if (allBlockHeaders.isEmpty()){
            return allBlockHeaders;
        }
        for (ArrayList<String> blockHeader : allBlockHeaders){
            ArrayList<ArrayList> fullBlock = new ArrayList<>();
            ArrayList<ArrayList> tweetBaseTx = getTweetBaseTx(blockHeader.get(0));
            ArrayList<String> txList = getBlockTxList(blockHeader.get(0));
            fullBlock.add(blockHeader);
            fullBlock.add(tweetBaseTx);
            if (txList != null && !txList.isEmpty()){
                fullBlock.add(txList);
            }
            allFullBlocks.add(fullBlock);
        }
        return allFullBlocks;
    }

    private synchronized ArrayList<String> getBlockTxList(String blockHash){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            c.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT TXHASH FROM MAPTABLE WHERE HEADERHASH = '" + blockHash +
                    "' AND TXINDEX != '0' ORDER BY TXINDEX ASC;");
            if (rs.isBeforeFirst()){
                ArrayList<String> tweetBaseTx = new ArrayList<>();
                while (rs.next()){
                    String hash = rs.getString("TXHASH");
                    tweetBaseTx.add(hash);
                }
                rs.close();
                stmt.close();
                return tweetBaseTx;
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private synchronized ArrayList<ArrayList> getTweetBaseTx(String blockHash){
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            c.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT TXHASH FROM MAPTABLE WHERE HEADERHASH =" +
                    " '" + blockHash + "' AND TXINDEX = '" + 0 + "';");
            if (rs.isBeforeFirst()){
                String tweetBaseTxHash = rs.getString("TXHASH");
                rs.close();
                stmt.close();
                return getFullTweet(tweetBaseTxHash);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ArrayList<String> getBlockHeader(String headerHash){
        Statement stmt = null;
        ArrayList<String> header = new ArrayList<>();
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM BLOCKCHAIN WHERE HEADERHASH=" +
                    " '" + headerHash + "';");
            if (rs.isBeforeFirst()){
                header.addAll(Arrays.asList(rs.getString("HEADERHASH"), String.valueOf(rs.getInt("HEIGHT")),
                        rs.getString("MERKLEROOT"), rs.getString("PREVIOUSHASH"),
                        rs.getString("TARGET"), rs.getString("NONCE"))) ;
                rs.close();
                stmt.close();
                return header;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return header;
    }

    private synchronized ArrayList<ArrayList> getAllBlockHeaders(int height){
        Statement stmt = null;
        ArrayList<ArrayList> allBlockHeaders = new ArrayList<>();
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM BLOCKCHAIN WHERE HEIGHT=" +
                    " " + height + ";");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    ArrayList<String> header = new ArrayList<>();
                    header.addAll(Arrays.asList(rs.getString("HEADERHASH"), String.valueOf(height),
                            rs.getString("MERKLEROOT"), rs.getString("PREVIOUSHASH"),
                            rs.getString("TARGET"), rs.getString("NONCE"))) ;
                    allBlockHeaders.add(header);
                }
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allBlockHeaders;
    }


    public synchronized boolean addFullTweet(ArrayList<ArrayList> fullTweet){
        if (fullTweet.isEmpty()){
            System.out.println("db.addFullTweet empty fulltweet");
            return false;
        }

        ArrayList<String> tx = fullTweet.get(0);
        PreparedStatement preparedStatement = null;
        int entered = 0;
        try {
            c.setAutoCommit(false);
            String sql = "INSERT OR IGNORE INTO TXTABLE (TXHASH, TYPE, UNLOCK, TWEETLENGTH, TWEET, NUMBERTOMINER) VALUES " +
                    "('" + tx.get(0) + "', '" + tx.get(1) + "', '" + tx.get(2) + "', '" + tx.get(3) +
                    "', ?, '" + tx.get(5) + "');";

            preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, tx.get(4));
            entered = preparedStatement.executeUpdate();
            preparedStatement.close();
            System.out.println("Tweet: " + fullTweet);
            System.out.println("Tweet added");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        String txHash = tx.get(0);
        if (entered == 1){

            ArrayList<ArrayList> txoList = fullTweet.get(1);
            for (ArrayList<String> txoInstance : txoList) {
                //TODO this screwing up with fullTweet from newTweet
                updateProfile(txoInstance.get(2));
                String pubKeyHash = new MathStuff().createHash(txoInstance.get(2));
                try {
                    Statement stmt2 = c.createStatement();
                    String sql = "INSERT INTO TXOTABLE (TXHASH, NUMBERTOTXO, TXOINDEX, TXOPUBKEYHASH) " +
                            "VALUES ('" + txHash + "', '" + txoInstance.get(0) + "', '" + txoInstance.get(1) +
                            "', '" + pubKeyHash + "');";
                    stmt2.executeUpdate(sql);
                    stmt2.close();
                    c.commit();
                    System.out.println("TxoInstance: " + txoInstance);
                    System.out.println("TxoInstance added");

                    //Add the new txo to openTxoTable, give placeholder headerhash
                    Statement stmt = null;
                    stmt = c.createStatement();

                    sql = "INSERT INTO OPENTXOTABLE (TXHASH, TXOINDEX, HEADERHASH, COINNUMBER, PUBKEYHASH) VALUES (" +
                            "'" + txHash + "', '" + txoInstance.get(1) + "', 'NA', '" + txoInstance.get(0) +
                            "', '" + pubKeyHash + "');";
                    stmt.executeUpdate(sql);
                    stmt.close();
                    c.commit();
                    System.out.println("openTxo added");

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

            if (!tx.get(1).equals("1")){
                ArrayList<ArrayList> txiTotal = fullTweet.get(2);
                for (ArrayList<String> txiInstance : txiTotal) {

                    try {
                        Statement stmt2 = c.createStatement();
                        c.setAutoCommit(false);
                        String sql = "INSERT INTO TXITABLE (TXHASH, TXIINDEX, TXIHASH, TXITXOINDEX) VALUES ('" +
                                txHash + "', '" + txiInstance.get(0) + "', '" + txiInstance.get(1) + "', '" +
                                txiInstance.get(2) + "');";
                        stmt2.executeUpdate(sql);
                        stmt2.close();
                        c.commit();
                        System.out.println("TxiInstance: " + txiInstance);
                        System.out.println("TxiInstance added");

                        Statement stmt = null;
                        stmt = c.createStatement();

                        sql = "DELETE FROM OPENTXOTABLE WHERE TXHASH = '" + txiInstance.get(1) + "' AND TXOINDEX = '" +
                                txiInstance.get(2) + "';";
                        stmt.executeUpdate(sql);
                        stmt.close();
                        c.commit();
                        System.out.println("openTxo deleted");

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(tx.get(1).equals("3")){
                ArrayList<String> nameAbout = convertReportToProfile(tx.get(4));
                ArrayList<String> txi = (ArrayList<String>)fullTweet.get(2).get(0);
                PreparedStatement stmt = null;
                try {
                    stmt = c.prepareStatement("UPDATE PROFILETABLE SET NAME = ?, ABOUT = ? WHERE PUBKEYHASH = " +
                            "(SELECT TXOPUBKEYHASH FROM TXOTABLE WHERE TXHASH = '" + txi.get(1) +
                            "' AND TXOINDEX = '" + txi.get(2) + "');");
                    stmt.setString(1, nameAbout.get(0));
                    stmt.setString(2, nameAbout.get(1));
                    stmt.executeUpdate();
                    stmt.close();
                    c.commit();
                    System.out.println("ProfileTable updated");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return (!(entered == 0));
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

    public synchronized AllTxi getNewGiveAllTxi(String username, int numbnerToGive){
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
//                ResultSetMetaData md = rs.getMetaData();
//                int colCount = md.getColumnCount();
//
//                for (int i = 1; i <= colCount ; i++){
//                    String col_name = md.getColumnName(i);
//                    System.out.println(col_name);
//                }
                int i = 0;
                int loop = 0;
                int totalNeeded = 1;
                while(i < totalNeeded && rs.next()){
                    if (loop == 0){
                        int reportReward = rs.getInt("REPORTREWARD");
                        totalNeeded = reportReward + numbnerToGive;
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
//                ResultSetMetaData md = rs.getMetaData();
//                int colCount = md.getColumnCount();
//
//                for (int i = 1; i <= colCount ; i++){
//                    String col_name = md.getColumnName(i);
//                    System.out.println(col_name);
//                }
                int totalNeeded = rs.getInt("REPORTREWARD");
                allTxi.setReportReward(totalNeeded);
                allTxi.setPubKey(rs.getString("PUBKEY"));
                allTxi.setPrivKey(rs.getString("PRIVKEY"));
                allTxi.addTxi(new Txi(rs.getString("TXHASH"),
                        rs.getInt("TXOINDEX")));
                int i = rs.getInt("COINNUMBER");
                while(i < totalNeeded && rs.next()){
                    allTxi.addTxi(new Txi(rs.getString("TXHASH"),
                            rs.getInt("TXHASH")));
                    i += rs.getInt("COINNUMBER");
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

    public synchronized ArrayList<ArrayList> getSpendableTxo(String pubKeyHash, String txPerTweet,
                                                             ArrayList<String> miningInfo){
        int offset = 0;
        int totalSpendable = 0;
        int totalNeeded = Integer.valueOf(txPerTweet);
        ArrayList<ArrayList> spendableTx = new ArrayList<>();
        while (true){
            ArrayList<String> tx = getSpendableTxoCycle(pubKeyHash, offset);
            System.out.println("db.getSpendableTxo attempt: " + tx);
            if (tx.isEmpty()){
                spendableTx.clear();
                return spendableTx;
            }
            boolean acceptable = isTxAcceptable(tx.get(0), miningInfo);
            if (acceptable){
                spendableTx.add(tx);
                int newSpendable = Integer.valueOf(tx.get(1));
                totalSpendable = totalSpendable + newSpendable;
                System.out.println("db.getSpendable totalSpendable: : " + totalSpendable +
                        " totalNeeded: " + totalNeeded);
                if (totalSpendable >= totalNeeded){
                    return spendableTx;
                }else {
                    offset++;
                }
            }else {
                offset++;
            }
        }
    }

    private synchronized ArrayList<String> getSpendableTxoCycle(String pubKeyHash, int offset){
        ArrayList<String> txo = new ArrayList<>();
        Statement stmt = null;
        try {

            c.setAutoCommit(false);
            stmt = c.createStatement();
            //see if header hash if real or placeholder
            ResultSet rs = stmt.executeQuery("SELECT * FROM OPENTXOTABLE WHERE PUBKEYHASH = '" + pubKeyHash +
                    "' AND HEADERHASH != 'NA' ORDER BY ID ASC LIMIT 1 OFFSET '" + offset + "';");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    txo.addAll(Arrays.asList(rs.getString("TXHASH"),
                            rs.getString("COINNUMBER"), rs.getString("TXOINDEX"),
                            rs.getString("PUBKEYHASH")));
                    System.out.println("db.getSpendableTxoCycle attempt txo :" + txo);
                }
                stmt.close();
                rs.close();
            }else {
                stmt.close();
                return txo;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txo;
    }

    public synchronized boolean isTxAcceptableForBlock(String txHash, String previousBlock){
        String txInputHash = getTxiHash(txHash);
        if (txInputHash == null){
            return false;
        }
        ArrayList<String> previousHeader = getBlockHeader(previousBlock);
        ArrayList<String> chainInfo = new ArrayList<>();
        if (previousHeader.isEmpty()){
            System.out.println("db.isTxAcceptableForBlock previousHeader empty");
            return false;
        }
        chainInfo.add(previousHeader.get(1));
        chainInfo.add(previousHeader.get(0));
        System.out.println("db.isTxAcceptableForBlock txInputHash: " + txInputHash);
        return isTxAcceptable(txInputHash, chainInfo);
    }

    private synchronized String getTxiHash(String txHash){
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT TXIHASH FROM TXITABLE WHERE TXHASH = '" + txHash + "';");
            if (rs.isBeforeFirst()){
                return rs.getString("TXIHASH");
            }
            stmt.close();
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private synchronized boolean isTxAcceptable(String txHash, ArrayList<String> chainInfo){
        ArrayList<String> headerHashWhenTxi = getHeaderHashWhenTxi(txHash);
        ArrayList<String> headerHashOfTx = getHeaderHashOfTx(txHash);
        System.out.println("db.isTxAcceptable headerHashes WhenTxi: " + headerHashWhenTxi +
                "\n OfTx: " + headerHashOfTx);
        System.out.println("db.isTxAcceptable txHash and chainInfo: " + txHash + chainInfo);
        if (headerHashOfTx.get(0).equals("no")){
            return false;
        }
        if (!headerHashWhenTxi.get(0).equals("no")){
            for (int i = 0; i < headerHashWhenTxi.size(); i+=2){
                if (!headerHashWhenTxi.get(i).equals(headerHashOfTx.get(0))) {
                    if (inSameChain(headerHashWhenTxi.get(i), headerHashWhenTxi.get(i + 1), chainInfo.get(1), chainInfo.get(0))) {
                        return false;
                    }
                }
            }
        }
        for (int i = 0; i < headerHashOfTx.size(); i+=2){
            if (!inSameChain(headerHashOfTx.get(i), headerHashOfTx.get(i+1), chainInfo.get(1), chainInfo.get(0))){
                return false;
            }
        }
        return true;
    }

    private synchronized boolean inSameChain(String firstHeader, String firstHeightString,
                                String secondHeader, String secondHeightString){
        //TODO think about just checking chain numbers first, then not bothering with this thing if passes
        System.out.println("db.inSameChain firstHeader: " + firstHeader + " firstHeightString: " + firstHeightString +
                " secondHeader: " + secondHeader + " SecondHeightString:" + secondHeightString);
        int firstHeight = Integer.valueOf(firstHeightString);
        int secondHeight = Integer.valueOf(secondHeightString);
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
//            if (difference < 10){
//                ResultSet rs = stmt.executeQuery(constructInSameChainSql(secondHeader, difference));
//                if (rs.isBeforeFirst()){
//                    String chainHash = rs.getString("PREVIOUSHASH");
//                    System.out.println("inSameChain firstHeader: " + firstHeader + " chainHash: " + chainHash);
//                    rs.close();
//                    stmt.close();
//                    // this is causing chaintable to lock for some reason, reconnecting in hopes of fixing it?
//                    Connection c2 = DriverManager.getConnection("jdbc:sqlite:vault.db");
//                    c2.setAutoCommit(false);
//                    Connection c3 = c;
//                    c = c2;
//                    c3.close();
//                    return firstHeader.equals(chainHash);
//                }
//                rs.close();
//                stmt.close();
//                System.out.println("db.inSameChain rs empty");
//            }else {
//                ArrayList<String> sqlList = constructInSameChainSqlMore(secondHeader, difference);
//                stmt = c.createStatement();
//                stmt.executeUpdate(sqlList.get(0));
//                stmt.close();
//                c.commit();
//                Statement stmt2 = c.createStatement();
//                ResultSet rs = stmt2.executeQuery(sqlList.get(1));
//                String chainHash = rs.getString("PREVIOUSHASH");
//                rs.close();
//                stmt2.close();
//                //This should close the temp tables without risk of having no c when another thread calls
//                Connection c2 = DriverManager.getConnection("jdbc:sqlite:vault.db");
//                c2.setAutoCommit(false);
//                Connection c3 = c;
//                c = c2;
//                c3.close();
//                return firstHeader.equals(chainHash);
//            }

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

    private synchronized ArrayList<String> constructInSameChainSqlMore(String headerHash, int difference){
        System.out.println("db.constructInSameChainSqlMore started, difference: " + difference);
        ArrayList<String> sqlList = new ArrayList<>();
        String sqlCreate = "CREATE TEMPORARY TABLE A0 AS SELECT PREVIOUSHASH, HEADERHASH FROM BLOCKCHAIN WHERE " +
                "HEADERHASH = '" + headerHash + "'; ";
        int i = 1;
        while (i < difference){
            sqlCreate = sqlCreate + "CREATE TEMPORARY TABLE A" + String.valueOf(i) + " " +
                    "AS SELECT PREVIOUSHASH, HEADERHASH FROM BLOCKCHAIN WHERE HEADERHASH = " +
                    "(SELECT PREVIOUSHASH FROM A" + String.valueOf(i - 1) + "); ";
            i++;
        }
        String sqlSelect ="SELECT PREVIOUSHASH FROM A" + String.valueOf(i - 1) + "; ";
        i = 0;
        String sqlDrop = "";
        while (i < difference){
            sqlDrop = sqlDrop + "DROP TABLE A" + String.valueOf(i) + "; ";
            i++;
        }
        sqlList.add(sqlCreate);
        sqlList.add(sqlSelect);
        sqlList.add(sqlDrop);
        System.out.println("db.constructInSameChainSqlMore sqlist: " + sqlList);
        return sqlList;
    }

    private synchronized String constructInSameChainSql(String headerHash, int difference){
        System.out.println("db.constructInSameChainSql started, difference: " + difference);

        String sql = "SELECT PREVIOUSHASH FROM BLOCKCHAIN WHERE HEADERHASH = ";
        if (difference == 1){
            sql = sql + "'" + headerHash + "';";
            System.out.println("db.constructInSameChainSql sql: " + sql);
            return sql;
        }
        int i = 1;
        while(i < difference){
            sql = sql + "(SELECT PREVIOUSHASH FROM BLOCKCHAIN WHERE HEADERHASH = ";
            i++;
        }
        sql = sql + "'" + headerHash + "'";
        i = 1;
        while(i < difference){
            sql = sql + ")";
            i++;
        }
        sql = sql + "; ";
        System.out.println("db.constructInSameChainSql sql: " + sql);
        return sql;
    }

    private synchronized ArrayList<String> getHeaderHashOfTx(String txHash){
        ArrayList<String> headerHashOfTxi = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT HEADERHASH, HEIGHT FROM BLOCKCHAIN WHERE HEADERHASH IN " +
                    "(SELECT HEADERHASH FROM MAPTABLE WHERE TXHASH = '" + txHash + "');");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    headerHashOfTxi.addAll(Arrays.asList(rs.getString("HEADERHASH"),
                            String.valueOf(rs.getInt("HEIGHT"))));
                }
                stmt.close();
                rs.close();
                return headerHashOfTxi;
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        headerHashOfTxi.add("no");
        return headerHashOfTxi;

    }

    private synchronized ArrayList<String> getHeaderHashWhenTxi(String txHash){
        ArrayList<String> headerHashWhenTxi = new ArrayList<>();
        System.out.println("db.getHeaderHashWhenTxi txHash:  " + txHash);
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT HEADERHASH, HEIGHT FROM BLOCKCHAIN WHERE HEADERHASH IN " +
                    "(SELECT HEADERHASH FROM MAPTABLE WHERE TXHASH IN " +
                    //"(SELECT TXHASH FROM TXTABLE WHERE TXHASH IN " +
                    "(SELECT TXHASH FROM TXITABLE WHERE TXIHASH = '" + txHash +"'));");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    headerHashWhenTxi.addAll(Arrays.asList(rs.getString("HEADERHASH"),
                            String.valueOf(rs.getInt("HEIGHT"))));
                }
                rs.close();
                stmt.close();
                System.out.println("db.getHeaderHashWhenTxi headerHashWhenTxi:  " + headerHashWhenTxi);
                return headerHashWhenTxi;
            }
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        headerHashWhenTxi.add("no");
        return headerHashWhenTxi;
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

    public synchronized ArrayList<ArrayList> getAllTxiTxo(ArrayList<ArrayList> txiList){
        ArrayList<ArrayList> allTxiTxo = new ArrayList<>();
        try {
            for (ArrayList<String> txi : txiList){
                ArrayList<String> txo = new ArrayList<>();
                String txiHash = txi.get(1);
                String txiTxoIndex = txi.get(2);
                Statement stmt = null;
                c.setAutoCommit(false);
                stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery( "SELECT * FROM TXOTABLE WHERE TXHASH = '" + txiHash +
                        "' AND TXOINDEX = '" + txiTxoIndex + "';");
                if (rs.isBeforeFirst()){
                    txo.addAll(Arrays.asList(rs.getString("TXHASH"), rs.getString("NUMBERTOTXO"),
                            rs.getString("TXOINDEX"), rs.getString("TXOPUBKEYHASH")));
                }else{
                    rs.close();
                    stmt.close();
                    allTxiTxo.clear();
                    return allTxiTxo;
                }
                rs.close();
                stmt.close();
                allTxiTxo.add(txo);
            }
            return allTxiTxo;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        allTxiTxo.clear();
        return allTxiTxo;
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
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT PUBKEY FROM PROFILETABLE WHERE PUBKEYHASH = '" +
                    pubKeyHash + "';");
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

    public synchronized boolean havePubKey(String pubKey) {
        boolean answer = false;
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT PUBKEY FROM PROFILETABLE WHERE PUBKEY = '" +
                    pubKey + "';");
            if (rs.isBeforeFirst()){
                answer = true;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answer;
    }

    public synchronized ArrayList<String> getTxToAddToBlock(){
        ArrayList<String> txList = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT TXHASH, NUMBERTOMINER FROM TXTABLE WHERE TXHASH NOT IN " +
                    "(SELECT TXHASH FROM MAPTABLE) AND TYPE != '1';");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    txList.add(rs.getString("TXHASH"));
                    txList.add(String.valueOf(rs.getInt("NUMBERTOMINER")));
                }
            }
            rs.close();
            stmt.close();
            return txList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txList;
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
//            //add profile if pubKey unknown
//            if (tx.getType() == 4){
//                updateProfile(tx.getAllTxo().getAllTxo().get(0).getTxoPubKey());
//            }
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

    public synchronized boolean addFullBlock(ArrayList<ArrayList> fullBlock){
        //TODO add boolean so add header doesn't redo the chain every time when catching up, slows things down
        System.out.println("db.addFullBlock fullBlock: " + fullBlock);
        ArrayList<String> header = fullBlock.get(0);
        boolean added = addBlockHeader(header);
        System.out.println("db.addFullBlock header added: " + added);
        if (added){
            ArrayList<ArrayList> tweetBaseTx = fullBlock.get(1);
            addFullTweet(tweetBaseTx);
            if (!fullBlock.get(2).isEmpty()){
                System.out.println("db addFullBlock tweetbase plus tweets map add, fullblock.get(2): "
                        + fullBlock.get(2));
                ArrayList<String> componentTxs = fullBlock.get(2);
                componentTxs.add(0, (String)tweetBaseTx.get(0).get(0));
                mapNewBlock(componentTxs, header.get(0));
                updateOpenTxoTable(tweetBaseTx, componentTxs, header.get(0));
            }else {
                System.out.println("db addFullBlock tweetbase only map add");
                ArrayList<String> componentTxs = new ArrayList<>();
                componentTxs.add((String)tweetBaseTx.get(0).get(0));
                mapNewBlock(componentTxs, header.get(0));
                updateOpenTxoTable(tweetBaseTx, componentTxs, header.get(0));
            }
        }
        return  added;
    }

    private synchronized void updateOpenTxoTable(ArrayList<ArrayList> tweetBaseTx, ArrayList<String> componentTxs, String headerHash){
        //TODO too many calls, make this more efficient
        componentTxs.remove(0);

        ArrayList<String> tx = tweetBaseTx.get(0);
        ArrayList<String> txo = (ArrayList<String>)tweetBaseTx.get(1).get(0);
        Statement stmt = null;
        try {
            stmt = c.createStatement();

            String sql = "UPDATE OPENTXOTABLE SET HEADERHASH  = '" + headerHash + "' WHERE TXHASH = '"
                    + tx.get(0) + "' AND TXOINDEX = '" + txo.get(1) + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("OpenTxo TxBase updated");
        }catch (Exception e){
            e.printStackTrace();
        }

        for (String txHash: componentTxs){
            ArrayList<ArrayList> fullTweet = getFullTweet(txHash);

            Statement stmt2 = null;
            ArrayList<String> tx2 = fullTweet.get(0);
            ArrayList<ArrayList> txoList = fullTweet.get(1);

            for (ArrayList<String> txo2 : txoList){
                //add new txo to openTxoTable
                try {
                    stmt2 = c.createStatement();

                    String sql2 = "UPDATE OPENTXOTABLE SET HEADERHASH = '" + headerHash + "' WHERE TXHASH = '"
                            + tx2.get(0) + "' AND TXOINDEX = '" + txo2.get(1) + "';";
                    stmt2.executeUpdate(sql2);
                    stmt2.close();
                    c.commit();
                    System.out.println("OpenTxo TweetTx added");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized void mapNewBlock(ArrayList<String> componentTxs, String headerHash){
        for (String tx: componentTxs){
            Statement stmt = null;
            try {
                stmt = c.createStatement();

                String sql = "INSERT INTO MAPTABLE (HEADERHASH, TXHASH, TXINDEX) VALUES (" +
                        "'" + headerHash + "', " +
                        "'" + tx + "', '" + String.valueOf(componentTxs.indexOf(tx)) + "');";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                System.out.println("map added");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private synchronized boolean addBlockHeader(ArrayList<String> header){
        int entered = 0;
        try {
            PreparedStatement preparedStatement = null;
            String sql = "INSERT OR IGNORE INTO BLOCKCHAIN (HEADERHASH, HEIGHT, " +
                    "MERKLEROOT, PREVIOUSHASH, TARGET, NONCE, CHAIN) VALUES " +
                    "('" + header.get(0) + "', " + Integer.valueOf(header.get(1)) + ", " +
                    "'" + header.get(2) + "', '" + header.get(3) + "', '" + header.get(4) + "', " +
                    "'" + header.get(5) + "', '1');";
            preparedStatement = c.prepareStatement(sql);
            entered = preparedStatement.executeUpdate();
            preparedStatement.close();
            c.commit();
            //System.out.println("addBlockHeader called");
        }catch (Exception e){
            e.printStackTrace();
        }
        if (entered == 1){
            //done for now: speed this up, it is still slow
            updateChainNumbers();
        }
        return (entered == 1);
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
                info.add(String.valueOf(rs.getInt("HEIGHT")));
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
        int height = Integer.valueOf(highestHeaders.get(0));
        highestHeaders.remove(0);
        int i = highestHeaders.size() - 1;
        while (i >= 0){
            if (i != 0){
                resetChain2(highestHeaders.get(i), height);
            }else{
                resetChain1(highestHeaders.get(i), height);
            }
            i--;
        }
    }

    private synchronized void resetChain2(String headerHash, int height) {
        String sql = constructResetChainSql("2", headerHash, height);
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
//        boolean stop = false;
//        String previous = headerHash;
//        while (!stop) {
//            Statement stmt = null;
//            try {
//                stmt = c.createStatement();
//
//                String sql = "UPDATE BLOCKCHAIN SET CHAIN  = '2' " +
//                        "WHERE HEADERHASH = '" + previous + "';";
//                stmt.executeUpdate(sql);
//                stmt.close();
//                c.commit();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            try {
//                stmt = c.createStatement();
//                c.setAutoCommit(false);
//                ResultSet rs = stmt.executeQuery("SELECT PREVIOUSHASH FROM BLOCKCHAIN " +
//                        "WHERE HEADERHASH = '" + previous + "';");
//                if (rs.isBeforeFirst()) {
//                    previous = rs.getString("PREVIOUSHASH");
//                }
//                stmt.close();
//                rs.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//            if (previous.equals("0")){
//                stop = true;
//            }
//        }

    }

    private synchronized void resetChain1(String headerHash, int height){
        String sql = constructResetChainSql("1", headerHash, height);
        Statement stmt = null;
        try {
            //this stops table lock problems
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
//        boolean stop = false;
//        String previous = headerHash;
//        while (!stop) {
//            Statement stmt = null;
//            try {
//                stmt = c.createStatement();
//
//                String sql = "UPDATE BLOCKCHAIN SET CHAIN  = '1' " +
//                        "WHERE HEADERHASH = '" + previous + "';";
//                stmt.executeUpdate(sql);
//                stmt.close();
//                c.commit();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            try {
//                stmt = c.createStatement();
//                c.setAutoCommit(false);
//                ResultSet rs = stmt.executeQuery("SELECT PREVIOUSHASH FROM BLOCKCHAIN " +
//                        "WHERE HEADERHASH = '" + previous + "';");
//                if (rs.isBeforeFirst()) {
//                    previous = rs.getString("PREVIOUSHASH");
//                }
//                stmt.close();
//                rs.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//            if (previous.equals("0")){
//                stop = true;
//            }
//        }
    }

    private synchronized String constructResetChainSql(String number, String headerHash, int height){
//        String sql = "CREATE TEMPORARY TABLE IF NOT EXISTS CHAINTABLE (HEADERHASH TXT NOT NULL, HEIGHT INT NOT NULL); ";
//        //sql = sql + "DELETE FROM CHAINTABLE; ";
//        sql = sql + "INSERT INTO CHAINTABLE (HEADERHASH, HEIGHT) VALUES ('" + headerHash + "', " + height + "); ";
//        while (height > 0){
//            height--;
//            sql = sql + "INSERT INTO CHAINTABLE (HEADERHASH, HEIGHT) VALUES " +
//                    "((SELECT PREVIOUSHASH FROM BLOCKCHAIN WHERE HEADERHASH = " +
//                    "(SELECT HEADERHASH FROM CHAINTABLE WHERE HEIGHT = (SELECT MIN(HEIGHT) FROM CHAINTABLE))), "
//                    + height + "); ";
//        }
//        sql = sql + "UPDATE BLOCKCHAIN SET CHAIN  = '" + number + "' WHERE HEADERHASH IN " +
//                "(SELECT HEADERHASH FROM CHAINTABLE); ";
//        sql = sql + "DROP TABLE CHAINTABLE;";
//        System.out.println(sql);
//        return sql;

//        String sql = "WITH RECURSIVE " +
//                "parent_of(HEADERHASH, PREVIOUSHASH) AS " +
//                "(SELECT HEADERHASH, PREVIOUSHASH FROM BLOCKCHAIN)," +
//                "ancestor_of(HEADERHASH) AS " +
//                "(SELECT PREVIOUSHASH FROM parent_of WHERE HEADERHASH = '" + headerHash + "' " +
//                "UNION ALL " +
//                "SELECT PREVIOUSHASH FROM parent_of JOIN ancestor_of USING(HEADERHASH)) " +
//                "UPDATE BLOCKCHAIN SET CHAIN = '" + number + "' WHERE HEADERHASH IN " +
//                "(SELECT HEADERHASH FROM ancestor_of); ";

        String sql = "WITH RECURSIVE " +
                "same_chain(HEADERHASH) AS (" +
                "VALUES('" + headerHash + "') " +
                "UNION " +
                "SELECT BLOCKCHAIN.PREVIOUSHASH FROM BLOCKCHAIN, same_chain " +
                "WHERE BLOCKCHAIN.HEADERHASH = same_chain.HEADERHASH) " +
                "UPDATE BLOCKCHAIN SET CHAIN = '" + number + "' WHERE HEADERHASH IN " +
                "(SELECT HEADERHASH FROM same_chain); ";
        return sql;
    }

    private synchronized String getPreviousHash(String headerHash){
        String previous = "";
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            c.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT PREVIOUSHASH FROM BLOCKCHAIN " +
                    "WHERE HEADERHASH = '" + headerHash + "';");
            if (rs.isBeforeFirst()){
                previous = rs.getString("PREVIOUSHASH");
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return previous;
    }

    private synchronized void resetChain(String headerHash, int number){
        Statement stmt = null;
        try {
            stmt = c.createStatement();

            String sql = "UPDATE BLOCKCHAIN SET CHAIN  = '" + String.valueOf(number) + "' " +
                    "WHERE HEADERHASH = '" + headerHash + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<ArrayList> getFullTweet(String txHash){
        ArrayList<ArrayList> fullTweet = new ArrayList<>();
        ArrayList<String> tx = getTxOld(txHash);
        ArrayList<ArrayList> txoList = getTxoList(txHash);
        fullTweet.add(tx);
        fullTweet.add(txoList);
        ArrayList<ArrayList> txiList = new ArrayList<>();
        if (!tx.get(1).equals("1")){
            txiList = getTxiList(txHash);
            fullTweet.add(txiList);
        }
        if (tx.isEmpty() || txoList.isEmpty() || (!tx.get(1).equals("1") && txiList.isEmpty())){
            fullTweet.clear();
        }
        return fullTweet;
    }

    private synchronized ArrayList<ArrayList> getTxoList(String txHash){
        ArrayList<ArrayList> txo = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM TXOTABLE LEFT OUTER JOIN PROFILETABLE ON " +
                    "TXOTABLE.TXOPUBKEYHASH = PROFILETABLE.PUBKEYHASH WHERE TXHASH = '" + txHash + "';");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    ArrayList<String> txoInstance = new ArrayList<>();
                    txoInstance.addAll(Arrays.asList(rs.getString("NUMBERTOTXO"),
                            rs.getString("TXOINDEX"), rs.getString("PUBKEY")));
                    txo.add(txoInstance);
                }
                rs.close();
                stmt.close();
                return txo;

            }else {
                rs.close();
                stmt.close();
                return txo;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txo;
    }

    private synchronized ArrayList<ArrayList> getTxiList(String txHash){
        ArrayList<ArrayList> txi = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM TXITABLE WHERE TXHASH = '" + txHash + "';");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    ArrayList<String> txiInstance = new ArrayList<>();
                    txiInstance.addAll(Arrays.asList(rs.getString("TXIINDEX"),
                            rs.getString("TXIHASH"), rs.getString("TXITXOINDEX")));
                    txi.add(txiInstance);
                }
                rs.close();
                stmt.close();
                return txi;
            }else {
                rs.close();
                stmt.close();
                return txi;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txi;
    }

    public synchronized Tx getTx(String txHash){
        //get allTxi
        AllTxi allTxi = getAllTxi(txHash);
        //get allTxo
        AllTxo allTxo = getAllTxo(txHash);

        //get Tx
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM TXTABLE WHERE TXHASH = '" + txHash + "';");
            if (rs.isBeforeFirst()){
                //construct Tx
                Tx tx = new Tx(rs.getString("TXHASH"), rs.getInt("TYPE"),
                        rs.getString("UNLOCK"), rs.getInt("REPORTLENGTH"),
                        rs.getString("REPORT"), rs.getInt("NUMBERTOMINER"), allTxi, allTxo);
                rs.close();
                stmt.close();
                return tx;
            }else {
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //TODO come up with better catch if something bad happens here
        return new Tx();
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

    private synchronized ArrayList<String> getTxOld(String txHash){
        ArrayList<String> tx = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM TXTABLE WHERE TXHASH = '" + txHash + "';");
            if (rs.isBeforeFirst()){
                tx.addAll(Arrays.asList( rs.getString("TXHASH"), rs.getString("TYPE"),
                        rs.getString("UNLOCK"), rs.getString("TWEETLENGTH"),
                        rs.getString("TWEET"), rs.getString("NUMBERTOMINER")));
                rs.close();
                stmt.close();
                return tx;
            }else {
                tx.clear();
                rs.close();
                stmt.close();
                return tx;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tx.clear();
        return tx;
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

    public synchronized String getCurrentHeaderHash(){
        String currentHeaderHash = "";
        Statement stmt = null;

        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT HEADERHASH FROM BLOCKCHAIN ORDER BY HEIGHT DESC LIMIT 1");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    currentHeaderHash = rs.getString("HEADERHASH");
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("db.getCurrentHeaderHash currentHeaderHash: " + currentHeaderHash);
        return currentHeaderHash;
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

    public synchronized ArrayList<Integer> getAllBlockTxMinerRewards(ArrayList<String> txList){
        if (txList.isEmpty()){
            return new ArrayList<>();
        }

        ArrayList<Integer> newTxList = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(constructAllBlockMinerRewardsTxListSQL(txList));
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    Integer minerReward = Integer.valueOf(rs.getString("NUMBERTOMINER"));
                    newTxList.add(minerReward);
                }
                rs.close();
                stmt.close();
                return newTxList;
            }else {
                rs.close();
                stmt.close();
                return newTxList;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newTxList;

    }

    private synchronized String constructAllBlockMinerRewardsTxListSQL(ArrayList<String> txList){
        String sql = "SELECT NUMBERTOMINER FROM TXTABLE WHERE ";
        for (int i = 0; i < txList.size(); i ++){
            if (i != txList.size() - 1){
                sql = sql + "TXHASH = '" + txList.get(i) + "' OR ";
            }else {
                sql = sql + "TXHASH = '" + txList.get(i) + "';";
            }
        }
        return sql;
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
        //TODO, make less precise, return close things
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
        //TODO, make less precise, return close things
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
        //TODO, make less precise, return close things
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
            ResultSet rs = stmt.executeQuery("SELECT TXTABLE.TXHASH, TXTABLE.TWEET, " +
                    "PROFILETABLE.NAME, PROFILETABLE.PUBKEYHASH " +
                    "FROM TXTABLE LEFT OUTER JOIN TXITABLE ON TXTABLE.TXHASH = TXITABLE.TXHASH " +
                    "LEFT OUTER JOIN TXOTABLE ON TXITABLE.TXIHASH = TXOTABLE.TXHASH " +
                    "LEFT OUTER JOIN PROFILETABLE ON TXOTABLE.TXOPUBKEYHASH = PROFILETABLE.PUBKEYHASH " +
                    "WHERE TXTABLE.TYPE = '2' AND TXITABLE.TXITXOINDEX = TXOTABLE.TXOINDEX AND " +
                    "TWEET LIKE '%" + search + "%' ORDER BY TXTABLE.ID DESC LIMIT 100 OFFSET " +
                    startNumber + ";");

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    ArrayList<String> tweet = new ArrayList<>();
                    tweet.add(rs.getString("NAME"));
                    tweet.add(rs.getString("TWEET"));
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

    public synchronized ArrayList<String> getPastTweetsResults(String pubKeyHash, int startingPoint){
        ArrayList<String> tweets = new ArrayList<>();
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT TXTABLE.TXHASH, TXTABLE.REPORT, " +
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
            ResultSet rs = stmt.executeQuery( "SELECT TXTABLE.TXHASH, TXTABLE.REPORT, " +
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
                    //sdf.setTimeZone(TimeZone.getTimeZone("America/Chicago")); // give a timezone reference for formating
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

//        if (networkType.equals("outside")){
//            return getIpPortOutside(username);
//        }else {
//            return getIpPortInside(username);
//        }
    }


//    public synchronized ArrayList<String> getIpPortOutside(String username){
//        ArrayList<String> profile = new ArrayList<>();
//        Statement stmt = null;
//        try {
//            c.setAutoCommit(false);
//            stmt = c.createStatement();
//            ResultSet rs = stmt.executeQuery( "SELECT OUTSIDEIP, OUTSIDEPORT, NETNAME FROM USER WHERE USERNAME = '" +
//                    username + "';");
//            if (rs.isBeforeFirst()){
//                while (rs.next()){
//                    profile.add(rs.getString("OUTSIDEIP"));
//                    profile.add(String.valueOf(rs.getInt("OUTSIDEPORT")));
//                    profile.add(rs.getString("NETNAME"));
//                }
//            }
//            rs.close();
//            stmt.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return profile;
//    }
//
//    public synchronized ArrayList<String> getIpPortInside(String username){
//        ArrayList<String> profile = new ArrayList<>();
//        Statement stmt = null;
//        try {
//            c.setAutoCommit(false);
//            stmt = c.createStatement();
//            ResultSet rs = stmt.executeQuery( "SELECT INSIDEIP, INSIDEPORT, NETNAME FROM USER WHERE USERNAME = '" +
//                    username + "';");
//            if (rs.isBeforeFirst()){
//                while (rs.next()){
//                    profile.add(rs.getString("INSIDEIP"));
//                    profile.add(String.valueOf(rs.getInt("INSIDEPORT")));
//                    profile.add(rs.getString("NETNAME"));
//                }
//            }
//            rs.close();
//            stmt.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return profile;
//    }

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
//        if (networkType.equals("outside")){
//            return updateMyIpOutside(username, myIp);
//        }else if (networkType.equals("inside")){
//            return updateMyIpInside(username, myIp);
//        }else{
//            return false;
//        }
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
//        if (networkType.equals("outside")){
//            return updateMyPortOutside(username, myPort);
//        }else if (networkType.equals("inside")){
//            return updateMyPortInside(username, myPort);
//        }else{
//            return false;
//        }
    }

//    public synchronized boolean updateMyIpOutside(String username, String myIp){
//        Statement stmt = null;
//        try {
//            stmt = c.createStatement();
//            String sql = "UPDATE USER SET OUTSIDEIP = '" + myIp + "' WHERE USERNAME = '" + username + "';";
//            stmt.executeUpdate(sql);
//            stmt.close();
//            c.commit();
//            System.out.println("myIpOUTSIDE updated");
//            return true;
//        }catch (Exception e){
//            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
//            return false;
//        }
//    }
//
//    public synchronized boolean updateMyPortOutside(String username, int myPort){
//        Statement stmt = null;
//        try {
//            stmt = c.createStatement();
//            String sql = "UPDATE USER SET OUTSIDEPORT = " + myPort + " WHERE USERNAME = '" + username + "';";
//            stmt.executeUpdate(sql);
//            stmt.close();
//            c.commit();
//            System.out.println("myPortOUTSIDE updated");
//            return true;
//        }catch (Exception e){
//            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
//            return false;
//        }
//    }
//
//    public synchronized boolean updateMyIpInside(String username, String myIp){
//        Statement stmt = null;
//        try {
//            stmt = c.createStatement();
//            String sql = "UPDATE USER SET INSIDEIP = '" + myIp + "' WHERE USERNAME = '" + username + "';";
//            stmt.executeUpdate(sql);
//            stmt.close();
//            c.commit();
//            System.out.println("myIpINSIDE updated");
//            return true;
//        }catch (Exception e){
//            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
//            return false;
//        }
//    }
//
//    public synchronized boolean updateMyPortInside(String username, int myPort){
//        Statement stmt = null;
//        try {
//            stmt = c.createStatement();
//            String sql = "UPDATE USER SET INSIDEPORT = " + myPort + " WHERE USERNAME = '" + username + "';";
//            stmt.executeUpdate(sql);
//            stmt.close();
//            c.commit();
//            System.out.println("myPortINSIDE updated");
//            return true;
//        }catch (Exception e){
//            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
//            return false;
//        }
//    }

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
            System.out.println("myNetName updated");
            return true;
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        }
//        Statement stmt = null;
//        try {
//            stmt = c.createStatement();
//            String sql = "UPDATE USER SET NETNAME = '" + netName + "' WHERE USERNAME = '" + username + "';";
//            stmt.executeUpdate(sql);
//            stmt.close();
//            c.commit();
//            System.out.println("netName updated");
//            return true;
//        }catch (Exception e){
//            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
//            return false;
//        }
    }

    public synchronized long getTimeOfPastBlock(int height){
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT TWEET FROM TXTABLE WHERE TXHASH = " +
                    "(SELECT TXHASH FROM MAPTABLE WHERE TXINDEX = 0 AND " +
                    "HEADERHASH = (SELECT HEADERHASH FROM BLOCKCHAIN WHERE CHAIN = 1 " +
                    "AND HEIGHT = " + height + " ));");
            if (rs.isBeforeFirst()){
                long time = Long.parseLong(rs.getString("TWEET"));
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

    public synchronized long getTimeOfPastBlock(ArrayList<String> blockInfo, int heightDifference){

        if (heightDifference == 0){
            return timeOfBlock(blockInfo.get(1));
        }
        int rightHeight = Integer.valueOf(blockInfo.get(0)) - heightDifference;
        ArrayList<String> blocksAtRightHeight = getHeadersAtHeight(rightHeight);
        System.out.println("db.getTimeOfPastBlock blocksAtRightHeight: " + blocksAtRightHeight);

        for (String headerHash: blocksAtRightHeight){
            if (inSameChain(headerHash, String.valueOf(rightHeight), blockInfo.get(1), blockInfo.get(0))){
                return timeOfBlock(headerHash);
            }
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

    private synchronized long timeOfBlock(String headerHash){
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT TWEET FROM TXTABLE WHERE TXHASH = (SELECT TXHASH " +
                    "FROM MAPTABLE WHERE HEADERHASH = '" + headerHash + "' AND TXINDEX = '0');");
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    long time = Long.parseLong(rs.getString("TWEET"));
                    rs.close();
                    return time;
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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
            String sql = "UPDATE USER SET COINPERTWEET = '" + newReward + "' WHERE USERNAME = '" + username + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            System.out.println("coinPerTweet updated");
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

    public synchronized ArrayList<ArrayList> getSpendableGiveTxo(String pubKeyHash, String txPerTweet,
                                                    ArrayList<String> miningInfo, String giveNumber) {

        int totalNeeded = Integer.valueOf(txPerTweet) + Integer.valueOf(giveNumber);
        return getSpendableTxo(pubKeyHash, String.valueOf(totalNeeded), miningInfo);
    }

    public synchronized AllTx getNewMinerBlockAllTx(){
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
