package com.example.syncapp.util;

import android.util.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

/**
 * The CryptoSystem class provides methods for file encryption and decryption
 * using AES and RSA algorithms with options for data compression.
 */
@SuppressWarnings({"UnusedReturnValue"})
public class CryptoSystem {

    public static final String SECRETE_KEY = "HiRaghavendraKJ1";

    /**
     * Helper method to generate an AES SecretKey from a string key.
     *
     * @return An AES SecretKey generated from the string key.
     * @throws NoSuchAlgorithmException If the AES algorithm is not available.
     */
    public static SecretKey getAESKey() throws NoSuchAlgorithmException {
        byte[] keyBytes = SECRETE_KEY.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Encrypts a file using AES algorithm without compression.
     *
     * @param inputFile  The input file to be encrypted.
     * @param outputFile The encrypted output file.
     * @param secretKey  The secret key used for encryption.
     * @return True if encryption is successful, false otherwise.
     */
    public static boolean encryptAES(File inputFile, File outputFile, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            try (FileInputStream inputStream = new FileInputStream(inputFile);
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                byte[] inputBytes = new byte[(int) inputFile.length()];
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(inputBytes);

                byte[] outputBytes = cipher.doFinal(inputBytes);
                outputStream.write(outputBytes);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Encrypts data from a file using AES algorithm without compression.
     *
     * @param inputFile  The input file to be encrypted.
     * @param secretKey  The secret key used for encryption.
     * @return The encrypted bytes if encryption is successful, null otherwise.
     */
    public static byte[] encryptAES(File inputFile, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV(); // Get the initialization vector

            try (FileInputStream inputStream = new FileInputStream(inputFile)) {
                byte[] inputBytes = new byte[(int) inputFile.length()];
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(inputBytes);
                return cipher.doFinal(inputBytes);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException | IOException e) {
            Log.e(LogHelper.tag(CryptoSystem.class), "encryptAES: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Decrypts a file using AES algorithm without compression.
     *
     * @param inputFile  The encrypted input file to be decrypted.
     * @param outputFile The decrypted output file.
     * @param secretKey  The secret key used for decryption.
     * @return True if decryption is successful, false otherwise.
     */
    public static boolean decryptAES(File inputFile, File outputFile, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            try (FileInputStream inputStream = new FileInputStream(inputFile);
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                byte[] inputBytes = new byte[(int) inputFile.length()];
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(inputBytes);

                byte[] outputBytes = cipher.doFinal(inputBytes);
                outputStream.write(outputBytes);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Encrypts a file using AES algorithm with data compression.
     *
     * @param inputFile  The input file to be encrypted.
     * @param outputFile The encrypted and compressed output file.
     * @param secretKey  The secret key used for encryption.
     * @return True if encryption is successful, false otherwise.
     */
    public static boolean encryptAndCompressAES(File inputFile, File outputFile, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            try (FileInputStream inputStream = new FileInputStream(inputFile);
                 FileOutputStream compressedStream = new FileOutputStream(outputFile);
                 GZIPOutputStream gzipOutputStream = new GZIPOutputStream(compressedStream)) {

                byte[] inputBytes = new byte[(int) inputFile.length()];
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(inputBytes);

                byte[] outputBytes = cipher.doFinal(inputBytes);
                gzipOutputStream.write(outputBytes);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decrypts a file using AES algorithm with data decompression.
     *
     * @param inputFile  The encrypted and compressed input file to be decrypted.
     * @param outputFile The decrypted output file.
     * @param secretKey  The secret key used for decryption.
     * @return True if decryption is successful, false otherwise.
     */
    public static boolean decompressAndDecryptAES(File inputFile, File outputFile, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            //noinspection IOStreamConstructor
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(inputFile));
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {


                byte[] inputBytes = readAllBytesFromStream(gzipInputStream);
                byte[] outputBytes = cipher.doFinal(inputBytes);
                outputStream.write(outputBytes);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Encrypts a file using RSA algorithm without compression.
     *
     * @param inputFile  The input file to be encrypted.
     * @param outputFile The encrypted output file.
     * @param publicKey  The public key used for encryption.
     * @return True if encryption is successful, false otherwise.
     */
    public static boolean encryptRSA(File inputFile, File outputFile, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // Using RSA with ECB mode and PKCS1Padding
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            try (FileInputStream inputStream = new FileInputStream(inputFile);
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                byte[] inputBytes = new byte[(int) inputFile.length()];
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(inputBytes);

                byte[] outputBytes = cipher.doFinal(inputBytes);
                outputStream.write(outputBytes);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decrypts a file using RSA algorithm without compression.
     *
     * @param inputFile  The encrypted input file to be decrypted.
     * @param outputFile The decrypted output file.
     * @param privateKey The private key used for decryption.
     * @return True if decryption is successful, false otherwise.
     */
    public static boolean decryptRSA(File inputFile, File outputFile, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // Using RSA with ECB mode and PKCS1Padding
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            try (FileInputStream inputStream = new FileInputStream(inputFile);
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                byte[] inputBytes = new byte[(int) inputFile.length()];
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(inputBytes);

                byte[] outputBytes = cipher.doFinal(inputBytes);
                outputStream.write(outputBytes);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Encrypts a file using RSA algorithm with data compression.
     *
     * @param inputFile  The input file to be encrypted.
     * @param outputFile The encrypted and compressed output file.
     * @param publicKey  The public key used for encryption.
     * @return True if encryption is successful, false otherwise.
     */
    public static boolean encryptAndCompressRSA(File inputFile, File outputFile, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // Using RSA with ECB mode and PKCS1Padding
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            try (FileInputStream inputStream = new FileInputStream(inputFile);
                 FileOutputStream compressedStream = new FileOutputStream(outputFile);
                 GZIPOutputStream gzipOutputStream = new GZIPOutputStream(compressedStream)) {

                byte[] inputBytes = new byte[(int) inputFile.length()];
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(inputBytes);

                byte[] outputBytes = cipher.doFinal(inputBytes);
                gzipOutputStream.write(outputBytes);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decrypts a file using RSA algorithm with data decompression.
     *
     * @param inputFile  The encrypted and compressed input file to be decrypted.
     * @param outputFile The decrypted output file.
     * @param privateKey The private key used for decryption.
     * @return True if decryption is successful, false otherwise.
     */
    public static boolean decompressAndDecryptRSA(File inputFile, File outputFile, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // Using RSA with ECB mode and PKCS1Padding
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            //noinspection IOStreamConstructor
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(inputFile));
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                byte[] inputBytes = readAllBytesFromStream(gzipInputStream);
                byte[] outputBytes = cipher.doFinal(inputBytes);
                outputStream.write(outputBytes);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private static byte[] readAllBytesFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteStream.write(buffer, 0, bytesRead);
        }
        return byteStream.toByteArray();
    }


    @SuppressWarnings("unused")
    public enum KeySize {
        AES_128(128),
        AES_192(192),
        AES_256(256),
        RSA_2048(2048),
        RSA_3072(3072),
        RSA_4096(4096);

        private final int value;

        KeySize(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Helper method to generate AES keys with the default key size (128 bits).
     *
     * @return A randomly generated AES SecretKey.
     * @throws NoSuchAlgorithmException If the AES algorithm is not available.
     */
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        return generateAESKey(KeySize.AES_128);
    }

    /**
     * Helper method to generate AES keys with the specified key size.
     *
     * @param keySize The desired AES key size.
     * @return A randomly generated AES SecretKey.
     * @throws NoSuchAlgorithmException If the AES algorithm is not available.
     */
    public static SecretKey generateAESKey(KeySize keySize) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize.getValue());
        return keyGenerator.generateKey();
    }

    /**
     * Helper method to generate RSA key pairs with the default key size (2048 bits).
     *
     * @return A randomly generated RSA KeyPair.
     * @throws NoSuchAlgorithmException If the RSA algorithm is not available.
     */
    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        return generateRSAKeyPair(KeySize.RSA_2048);
    }

    /**
     * Helper method to generate RSA key pairs with the specified key size.
     *
     * @param keySize The desired RSA key size.
     * @return A randomly generated RSA KeyPair.
     * @throws NoSuchAlgorithmException If the RSA algorithm is not available.
     */
    public static KeyPair generateRSAKeyPair(KeySize keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize.getValue());
        return keyPairGenerator.generateKeyPair();
    }



    /**
     * Sample usage of the CryptoSystem methods.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        try {
            // Generate keys for AES and RSA
            SecretKey aesKey = generateAESKey();
            KeyPair rsaKeyPair = generateRSAKeyPair();
            PublicKey publicKey = rsaKeyPair.getPublic();
            PrivateKey privateKey = rsaKeyPair.getPrivate();

            // Sample files
            File inputFile = new File("input.txt");
            File encryptedFileAES = new File("encryptedAES.txt");
            File decryptedFileAES = new File("decryptedAES.txt");
            File encryptedFileAESCompressed = new File("encryptedAESCompressed.txt");
            File decryptedFileAESCompressed = new File("decryptedAESCompressed.txt");
            File encryptedFileRSA = new File("encryptedRSA.txt");
            File decryptedFileRSA = new File("decryptedRSA.txt");
            File encryptedFileRSACompressed = new File("encryptedRSACompressed.txt");
            File decryptedFileRSACompressed = new File("decryptedRSACompressed.txt");

            // AES encryption without compression
            encryptAES(inputFile, encryptedFileAES, aesKey);
            System.out.println("File encrypted with AES successfully!");

            // AES decryption without compression
            decryptAES(encryptedFileAES, decryptedFileAES, aesKey);
            System.out.println("File decrypted with AES successfully!");

            // AES encryption with compression
            encryptAndCompressAES(inputFile, encryptedFileAESCompressed, aesKey);
            System.out.println("File encrypted with AES and compressed successfully!");

            // AES decryption with decompression
            decompressAndDecryptAES(encryptedFileAESCompressed, decryptedFileAESCompressed, aesKey);
            System.out.println("File decrypted with AES and decompressed successfully!");

            // RSA encryption without compression
            encryptRSA(inputFile, encryptedFileRSA, publicKey);
            System.out.println("File encrypted with RSA successfully!");

            // RSA decryption without compression
            decryptRSA(encryptedFileRSA, decryptedFileRSA, privateKey);
            System.out.println("File decrypted with RSA successfully!");

            // RSA encryption with compression
            encryptAndCompressRSA(inputFile, encryptedFileRSACompressed, publicKey);
            System.out.println("File encrypted with RSA and compressed successfully!");

            // RSA decryption with decompression
            decompressAndDecryptRSA(encryptedFileRSACompressed, decryptedFileRSACompressed, privateKey);
            System.out.println("File decrypted with RSA and decompressed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}