package com.musicapp.music_app.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class EncryptionManagement {
    private static final String ENCRYPTION_ALGORITHM = "AES";

    /**
     * Generates a new AES-128 encryption key.
     *
     * This method uses the specified encryption algorithm (defined by the constant ENCRYPTION_ALGORITHM)
     * to create a symmetric key suitable for AES encryption. The key length is set to 128 bits, which
     * provides a balance between security and performance.
     *
     * @return a SecretKey object representing the newly generated AES-128 encryption key.
     * @throws Exception if an error occurs during the key generation process, such as when the
     *                   specified encryption algorithm is unavailable.
     */

    public static SecretKey generateEncryptionKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGen.init(128);
        return keyGen.generateKey();
    }

    /**
     * Encrypts and saves a file to a specified folder with a unique name.
     *
     * This method takes an input file stream, encrypts its content using the provided
     * symmetric encryption key, and saves the encrypted file in the specified folder.
     * The resulting file is named uniquely based on the current system time
     * (e.g., "<timestamp>.enc") to avoid collisions.
     *
     * @param fileInputStream the InputStream of the file to be encrypted.
     * @param folder the directory path where the encrypted file will be saved.
     * @param key the SecretKey used for encrypting the file.
     * @return the full path of the saved encrypted file as a String.
     * @throws Exception if an error occurs during encryption or file operations.
     */

    public static String saveEncryptedFile(InputStream fileInputStream, String folder, SecretKey key) throws Exception {
        String uniqueFileName = System.currentTimeMillis() + ".enc";
        Path filePath = Paths.get(folder, uniqueFileName);
        encryptAndSaveFile(fileInputStream, filePath.toFile(), key);
        return filePath.toString();
    }

    /**
     * Encrypts data from an InputStream and saves it to a specified file.
     *
     * This method reads data from the provided InputStream, encrypts it using the specified
     * symmetric encryption key, and writes the encrypted data to the specified output file.
     * The encryption is performed using a `Cipher` initialized with the specified encryption algorithm
     * (defined by the constant ENCRYPTION_ALGORITHM) in encryption mode (ENCRYPT_MODE).
     *
     * The method processes the data in chunks of 1024 bytes for efficiency. The final block of data
     * is handled separately to ensure proper encryption padding, if required.
     *
     * @param inputStream the InputStream containing the data to encrypt.
     * @param outputFile the File where the encrypted data will be written.
     * @param key the SecretKey used for encryption.
     * @throws Exception if an error occurs during encryption or file operations, such as
     *                   issues with the encryption algorithm, key, or file handling.
     */

    private static void encryptAndSaveFile(InputStream inputStream, File outputFile, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] encrypted = cipher.update(buffer, 0, bytesRead);
                if (encrypted != null) {
                    fos.write(encrypted);
                }
            }
            byte[] finalBytes = cipher.doFinal();
            if(finalBytes != null) {
                fos.write(finalBytes);
            }
        }
    }

    public static String saveEncryptedFileWithCustomName(InputStream fileInputStream, String folder, String fileName, SecretKey key) throws Exception {
        String fileNameWithExtension = fileName + ".enc";
        Path filePath = Paths.get(folder, fileNameWithExtension);
        encryptAndSaveFile(fileInputStream, filePath.toFile(), key);
        return filePath.toString();
    }

    public static SecretKey getSecretKeyFromBase64(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new javax.crypto.spec.SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    public static byte[] decryptFile(String encryptedFilePath, SecretKey key) throws Exception {
        Path path = Path.of(encryptedFilePath);
        try (InputStream fileInputStream = Files.newInputStream(path)) {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return decryptAndGetBytes(fileInputStream, cipher);
        }
    }

    private static byte[] decryptAndGetBytes(InputStream inputStream, Cipher cipher) throws Exception {
        byte[] buffer = new byte[1024];
        int bytesRead;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] decrypted = cipher.update(buffer, 0, bytesRead);
                if (decrypted != null) {
                    outputStream.write(decrypted);
                }
            }
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) {
                outputStream.write(finalBytes);
            }
            return outputStream.toByteArray();
        }
    }
}
