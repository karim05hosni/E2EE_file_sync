    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Key sk = generateKey();
        String message = "Hello Encryption";
        byte[] encryptedMsg = EncryptCBC(message, sk);
        System.out.println();
        Decrypt(encryptedMsg, sk);
    }