package ReadWrite;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Pattern;


public class MathStuff {
    public MathStuff(){

    }

    public ArrayList<String> createNewUser(String password){
        ArrayList<String> newUser = new ArrayList<>();
        String salt = createSalt();
        newUser.add(salt);
        newUser.add(createPassHash(salt, password));
        String[] keys = new String[0];
        try {
            keys = createNewKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        newUser.add(keys[0]);
        newUser.add(keys[1]);
        String pubKeyHash = createHash(keys[0]);
        newUser.add(pubKeyHash);
        newUser.add("1");
        return newUser;
    }

    private String[] createNewKeys() throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyPairGenerator kpg = null;
        KeyPair kp = null;
        try {
            kpg = KeyPairGenerator.getInstance("DSA", "SUN");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            kpg.initialize(1024, random);
            kp = kpg.genKeyPair();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        KeyFactory fact = KeyFactory.getInstance("DSA");
        X509EncodedKeySpec xSpec = fact.getKeySpec(kp.getPublic(),
                X509EncodedKeySpec.class);
        //TODO make base64
        String pubKey = Base64.getEncoder().encodeToString(xSpec.getEncoded());
        //String pubKey = convertByteToHex(xSpec.getEncoded());

        PKCS8EncodedKeySpec pSpec = fact.getKeySpec(kp.getPrivate(),
                PKCS8EncodedKeySpec.class);
        byte[] packed = pSpec.getEncoded();
        String privKey = Base64.getEncoder().encodeToString(packed);
        //String privKey = convertByteToHex(packed);

        return new String[]{pubKey, privKey};
    }

    public String createPassHash(String salt, String password){
        String saltedPassword = password + salt;
        return createHash(saltedPassword);
    }

    public String createHash(String text){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(text.getBytes());

        byte byteData[] = md.digest();
        return convertByteToHex(byteData);


    }

    private String convertByteToHex(byte[] byteData){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        //System.out.println("Hex format : " + sb.toString());
        return sb.toString();
    }

    private byte[] convertHexToByte(String s){
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private String createSalt(){
        Random rand = new Random();
        String salt = String.valueOf(rand.nextInt(10));
        int i;
        for (i = 0; i < 33; i++){
            salt = salt + String.valueOf(rand.nextInt(10));
        }
        return salt;
    }

    public String unlockTxo(String privKey, String merkle){
        try {
            byte[] clear = Base64.getDecoder().decode(privKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
            KeyFactory fact = KeyFactory.getInstance("DSA");
            PrivateKey priv = fact.generatePrivate(keySpec);
            Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
            dsa.initSign(priv);
            byte[] merkleBytes = Base64.getDecoder().decode(merkle);
            dsa.update(merkleBytes);
            byte[] signatureBytes = dsa.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return "no";

    }

    public boolean testUnLock(String pubKey, String unlock, String merkle){
        System.out.println("MathStuff testUnlock merkle: " + merkle + " unlock: " + unlock + " pubKey: " + pubKey);
        try {
            byte[] data = Base64.getDecoder().decode(pubKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory fact = KeyFactory.getInstance("DSA");
            PublicKey pub = fact.generatePublic(spec);
            Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
            sig.initVerify(pub);
            sig.update(Base64.getDecoder().decode(merkle));
            boolean verify = sig.verify(Base64.getDecoder().decode(unlock));
            System.out.println("Math testUnlock: " + verify);
            return verify;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String createBlockMerkleRoot(ArrayList<String> txList, String tweetBaseHash){
        ArrayList<String> tx = new ArrayList<>();
        tx.add(tweetBaseHash);
        if (txList != null){
            tx.addAll(txList);
        }
        return createMerkleRoot(tx);
    }

    public String createMerkleRoot(ArrayList<String> tx){
        int i = tx.size();
        while (i >= 1){
            if (i == 1){
                return createHash(tx.get(0) + tx.get(0));
            }
            ArrayList<String> newList = new ArrayList<>();
            if ((i%2)!=0){
                String again = tx.get(i - 1);
                tx.add(again);
                i = tx.size();
            }
            int j = 0;
            while (j <= i - 1){
                newList.add(createHash(tx.get(j) + tx.get(j+1)));
                j = j + 2;
            }
            tx.clear();
            tx.addAll(newList);
            i = tx.size();
        }
        return tx.get(0);
    }

    public String createTxMerkleRoot(ArrayList<ArrayList> txi) {
        ArrayList<String> allTxi = new ArrayList<>();
        for (ArrayList<String> txinstance : txi){
            allTxi.add(createHash(txinstance.get(0)));
            allTxi.add(txinstance.get(1));
        }
        return createMerkleRoot(allTxi);
    }

    public ArrayList<String> createNewSaltPass(String password) {
        ArrayList<String> saltPass = new ArrayList<>();
        String salt = createSalt();
        String hash = createPassHash(salt, password);
        saltPass.add(salt);
        saltPass.add(hash);
        return saltPass;
    }

    public boolean isValidPort(String portS){
        if (isNumber(portS)){
            return isValidPort(Integer.valueOf(portS));
        }
        return false;
    }

    public boolean isValidPort(int port){
        if (port >= 1024 && port <= 65535){
            return true;
        }
        return false;
    }

    public boolean isValidIp(String ip, String networkType){
        if (networkType.equals("outside")){
            return isValidIpOutside(ip);
        }else {
            return isValidIpInside(ip);
        }
    }

    public boolean isValidIpOutside(String ip){
        ArrayList<String> ipList= new ArrayList(Arrays.asList(ip.split(Pattern.quote("."))));
        if (ipList.size() != 4){
            return false;
        }
        return isValidIpOutside(ipList.get(0), ipList.get(1), ipList.get(2), ipList.get(3));
    }

    private boolean isValidIpOutside(String ip0s, String ip1s, String ip2s, String ip3s){
        if (!isNumber(ip0s) || !isNumber(ip1s) || !isNumber(ip2s) || !isNumber(ip3s)) {
            return false;
        }
        int ip0 = Integer.valueOf(ip0s);
        int ip1 = Integer.valueOf(ip1s);
        int ip2 = Integer.valueOf(ip2s);
        int ip3 = Integer.valueOf(ip3s);
        if (!inRange(ip0) || !inRange(ip1) || !inRange(ip2) || !inRange(ip3)){
            return false;
        }
        if (ip0 == 10){
            return false;
        }
        if (ip0 == 192 && ip1 == 168){
            return false;
        }
        if (ip0 == 172 && (ip1 >= 16 && ip1 <= 31)){
            return false;
        }
        if (ip0 == 127){
            return false;
        }
        return true;
    }

    public boolean isValidIpInside(String ip){
        ArrayList<String> ipList= new ArrayList(Arrays.asList(ip.split(Pattern.quote("."))));
        if (ipList.size() != 4){
            return false;
        }
        return isValidIpInside(ipList.get(0), ipList.get(1), ipList.get(2), ipList.get(3));
    }

    private boolean isValidIpInside(String ip0s, String ip1s, String ip2s, String ip3s){
        if (!isNumber(ip0s) || !isNumber(ip1s) || !isNumber(ip2s) || !isNumber(ip3s)) {
            return false;
        }
        int ip0 = Integer.valueOf(ip0s);
        int ip1 = Integer.valueOf(ip1s);
        int ip2 = Integer.valueOf(ip2s);
        int ip3 = Integer.valueOf(ip3s);
        if (!inRange(ip0) || !inRange(ip1) || !inRange(ip2) || !inRange(ip3)){
            return false;
        }
        if (ip0 != 10 && ip0 != 127){
            return false;
        }
        return true;
    }

    private boolean inRange(int ip){
        if (ip >= 0 && ip <= 255){
            return true;
        }
        return false;
    }

    public boolean isNumber(String s){
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),10) < 0) return false;
        }
        return true;
    }
}
