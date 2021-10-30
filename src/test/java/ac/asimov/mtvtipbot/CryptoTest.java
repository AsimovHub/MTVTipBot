package ac.asimov.mtvtipbot;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.EncryptionPairDto;
import ac.asimov.mtvtipbot.helper.CryptoHelper;
import ac.asimov.mtvtipbot.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = { MTVTipBotApplication.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {
		TransactionService.class,
		MultiVACBlockchainGateway.class })
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestEntityManager
@ImportAutoConfiguration
class CryptoTest {

	@Test
	public void testUserIdToKey() throws Exception {
		Long userId = 123456890L;
		String hashedResult = CryptoHelper.userIdToKey(userId);
		String correctHashedResult = "3c37ccb27683f19f61debaf31fd3743f";
		assertEquals(hashedResult, correctHashedResult);

		userId = 112233449988L;
		hashedResult = CryptoHelper.userIdToKey(userId);
		correctHashedResult = "dd0dc8220bed0d3891cab8c955402650";
		assertEquals(hashedResult, correctHashedResult);

		userId = 1286367821L;
		hashedResult = CryptoHelper.userIdToKey(userId);
		correctHashedResult = "fb28f0c6bc0d61bafcb8e7a0acf32040";
		assertEquals(hashedResult, correctHashedResult);

		userId = 8712768218L;
		hashedResult = CryptoHelper.userIdToKey(userId);
		correctHashedResult = "fa1c25f1aaabc9a677bb84e1edfcba7d";
		assertEquals(hashedResult, correctHashedResult);
	}

	@Test
	public void testEncryptionAndDecryption() throws Exception {
		ECKeyPair walletKeys = Keys.createEcKeyPair();
		String address = "0x" + Keys.getAddress(walletKeys.getPublicKey());
		String privateKey = "0x" + walletKeys.getPrivateKey().toString(16);

		String secret = UUID.randomUUID().toString().split("-")[0] + UUID.randomUUID().toString().split("-")[0];

		EncryptionPairDto encrypted = CryptoHelper.encrypt(privateKey, secret);
		assertNotNull(encrypted);
		System.out.println("Private Key: " + privateKey);
		System.out.println("Encrypted Private Key: " + encrypted.getEncryptedValue());
		System.out.println("Secret: " + encrypted.getSecret());
		System.out.println("Salt: " + encrypted.getSalt());

		String outputString = CryptoHelper.decrypt(encrypted.getEncryptedValue(), encrypted.getSecret(), encrypted.getSalt());
		assertEquals(privateKey, outputString);
	}

	@Test
	public void testDecrypt() throws Exception {
		String outputString = CryptoHelper.decrypt("4a20b4e4243c395e4a6c9c6f0a72b09401e3921ba1057f034bdef6e80c5415ebf5bd0d43f18c3930e63c7a5ed64a4058c903fb1e422824addc150a26c6b54d7189b4", "890e4865ed88ff09", "a758391d6a96cbe43df2512dd81e4b11");
		assertEquals("0x30b8a0d6bbbf7ddac5545b27c9b25a2c67a56536aa4e11f52f30df2c36eafd51", outputString );
	}

	@Test
	public void testEncrypt() throws Exception {
		EncryptionPairDto output = CryptoHelper.encrypt("0x30b8a0d6bbbf7ddac5545b27c9b25a2c67a56536aa4e11f52f30df2c36eafd51", "890e4865ed88ff09", "a758391d6a96cbe43df2512dd81e4b11");
		assertEquals("a758391d6a96cbe43df2512dd81e4b11", output.getSalt());
		assertEquals("4a20b4e4243c395e4a6c9c6f0a72b09401e3921ba1057f034bdef6e80c5415ebf5bd0d43f18c3930e63c7a5ed64a4058c903fb1e422824addc150a26c6b54d7189b4", output.getEncryptedValue());
	}
}
