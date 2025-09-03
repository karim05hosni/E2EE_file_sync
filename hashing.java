
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


class hashing {
    private static void hashText (String s) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] input = s.getBytes();
        byte[] digest = messageDigest.digest(input);
        for (byte elem : digest) {
            System.out.print(elem);
        }
    }
    public static void main(String[] args) throws NoSuchAlgorithmException {
        hashText("HoSni@67??");
    }
}