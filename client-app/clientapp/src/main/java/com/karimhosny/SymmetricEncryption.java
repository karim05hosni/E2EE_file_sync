import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

class SymmetricEncryption {
    private static byte[] Encrypt(String message, Key sk) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sk);
        byte[] cipherBinary = cipher.doFinal(message.getBytes());
        for (byte elem : cipherBinary) {
            System.out.print(elem);
        }
        return cipherBinary;
    }
    /*
     * cipher block chaining mode
    */
    private static byte[] EncryptCBC(String message, Key sk) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16]; // 16 bytes for AES
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, sk, ivSpec);
        byte[] cipherBinary = cipher.doFinal(message.getBytes());
        for (byte elem : cipherBinary) {
            System.out.print(elem);
        }
        return cipherBinary;
    }
    private static Key generateKey() throws NoSuchAlgorithmException{
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        return generator.generateKey();
    }
    private static void Decrypt(byte[] encryptedMsg, Key sk) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sk);
        byte[] decryptedText = cipher.doFinal(encryptedMsg);
        String plainText = new String(decryptedText);
        System.out.println(plainText);
    }
    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Key sk = generateKey();
        String message = "Hello Encryption";
        byte[] encryptedMsg = EncryptCBC(message, sk);
        System.out.println();
        Decrypt(encryptedMsg, sk);
    }
}