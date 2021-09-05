package ac.asimov.mtvtipbot.helper;

public class CryptoHelper {


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
}
