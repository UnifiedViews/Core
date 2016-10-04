package eu.unifiedviews.commons.util;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.jasypt.util.binary.BasicBinaryEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;

public class Cryptography {

    private final BasicBinaryEncryptor bytesEncryptor;

    private final BasicTextEncryptor stringEncryptor;

    /**
     * @param cryptographyKeyFile
     *            Absolute file path with cryptography key in it.
     * @throws IOException
     *             If something is wrong with cryptography key file.
     */
    public Cryptography(String cryptographyKeyFile) throws IOException {
        String cryptographyKey = DatatypeConverter.printBase64Binary(FileUtils.readFileToByteArray(new File(cryptographyKeyFile)));

        bytesEncryptor = new BasicBinaryEncryptor();
        bytesEncryptor.setPassword(cryptographyKey);

        stringEncryptor = new BasicTextEncryptor();
        stringEncryptor.setPassword(cryptographyKey);
    }

    /**
     * @param plainText
     *            byte array for encryption.
     * @return Encrypted byte array.
     */
    public byte[] encrypt(byte[] plainText) {
        return bytesEncryptor.encrypt(plainText);
    }

    /**
     * @param cipherText
     *            byte array for decryption.
     * @return Decrypted byte array.
     */
    public byte[] decrypt(byte[] cipherText) {
        return bytesEncryptor.decrypt(cipherText);
    }

    /**
     * @param plainText
     *            string for encryption.
     * @return Encrypted string.
     */
    public String encrypt(String plainText) {
        return stringEncryptor.encrypt(plainText);
    }

    /**
     * @param cipherText
     *            string for decryption.
     * @return Decrypted string.
     */
    public String decrypt(String cipherText) {
        return stringEncryptor.decrypt(cipherText);
    }

}
