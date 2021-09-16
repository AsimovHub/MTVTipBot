package ac.asimov.mtvtipbot.helper;

import ac.asimov.mtvtipbot.dtos.EncryptionPairDto;
import com.google.common.primitives.UnsignedBytes;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Component;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;

@Component
public class CryptoHelper {

    private final static String ALGORITHM = "aes-256-ctr";


    /*
    function encryptWithUserId(data, userId) {
    return encrypt(data, userId);
}

function decryptWithUserId(encrypted, userId, salt) {
    return decrypt(encrypted, userId, salt);
}

function encrypt(text, secret) {
    const iv = crypto.randomBytes(16);
    let key = crypto.createHash('sha256').update(String(secret)).digest('base64').substr(0, 32);


    const cipher = crypto.createCipheriv(algorithm, key, iv);
    const encrypted = Buffer.concat([cipher.update(text), cipher.final()]);
    return {
        iv: iv.toString('hex'),
        content: encrypted.toString('hex')
    };
}

function decrypt(hash, secret, salt) {
    let key = crypto.createHash('sha256').update(String(secret)).digest('base64').substr(0, 32);

    const decipher = crypto.createDecipheriv(algorithm, key, Buffer.from(salt, 'hex'));
    const decrypted = Buffer.concat([decipher.update(Buffer.from(hash, 'hex')), decipher.final()]);
    return decrypted.toString();
};

     */

    public static EncryptionPairDto encrypt(String text, String secret) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        return encrypt(text, secret, Hex.encodeHexString(salt));
    }

    public static EncryptionPairDto encrypt(String text, String secret, String salt) throws Exception {


        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(secret.getBytes(StandardCharsets.UTF_8));
        String stringKey = Base64.toBase64String(md.digest()).substring(0, 32);

        IvParameterSpec ivSpec = new IvParameterSpec(Hex.decodeHex(salt));
        SecretKeySpec secretKeySpecs = new SecretKeySpec(stringKey.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpecs, ivSpec);
        byte[] output = cipher.update(text.getBytes());

        return new EncryptionPairDto(Hex.encodeHexString(output), secret, salt);
    }



    public static String decrypt(String encodedString, String secret, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(secret.getBytes(StandardCharsets.UTF_8));
        String stringKey = Base64.toBase64String(md.digest()).substring(0, 32);
        // let key = crypto.createHash('sha256').update(String(secret)).digest('base64').substr(0, 32);


        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

        IvParameterSpec ivSpec = new IvParameterSpec(Hex.decodeHex(salt));
        SecretKeySpec secretKeySpecs = new SecretKeySpec(stringKey.getBytes(), "AES");

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpecs, ivSpec);
        byte[] output = cipher.update(Hex.decodeHex(encodedString));

        // String decrypted = Hex.encodeHexString(output);
        String decrypted = bytesToHexString(output);
        return decrypted;
    }

    public static String userIdToKey(Long userId) throws Exception {
        if (userId == null || userId == 0) {
            throw new Exception("Empty input");
        }
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(userId.toString().getBytes(StandardCharsets.UTF_8));
        byte[] result = md.digest();
        return Hex.encodeHexString(result);
    }

    private static byte[] unsignedByte(byte[] input) {
        byte[] output = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
            byte v = input[i];
            if (v < 0) {
                int in = (v & 0xFF);
            }
            output[i] = v;
        }
        return output;
    }

    private static String bytesToHexString(byte[] input) {
        StringBuilder sb = new StringBuilder();
        for (byte b : input) {
            sb.append(Character.toString(b));

        }
        return sb.toString();
    }
}
