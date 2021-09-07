package ac.asimov.mtvtipbot.dtos;

public class EncryptionPairDto {

    private String encryptedValue;
    private String secret;
    private String salt;

    public EncryptionPairDto(String encryptedValue, String secret) {
        this.encryptedValue = encryptedValue;
        this.secret = secret;
    }

    public EncryptionPairDto(String encryptedValue, String secret, String salt) {
        this.encryptedValue = encryptedValue;
        this.secret = secret;
        this.salt = salt;
    }

    public String getEncryptedValue() {
        return encryptedValue;
    }

    public void setEncryptedValue(String encryptedValue) {
        this.encryptedValue = encryptedValue;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
