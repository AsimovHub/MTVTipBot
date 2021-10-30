package ac.asimov.mtvtipbot;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.dtos.TransferRequestDto;
import ac.asimov.mtvtipbot.dtos.WalletAccountDto;
import ac.asimov.mtvtipbot.service.TransactionService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.web3j.crypto.WalletUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
class MultiVACBlockchainGatewayTest {

	@Autowired
	private MultiVACBlockchainGateway blockchainGateway;

	@Test
	void testWalletCreation() throws Exception {
		WalletAccountDto wallet = blockchainGateway.generateNewWallet();
		assertTrue(StringUtils.startsWith(wallet.getReceiverAddress(), "0x"));
		assertTrue(blockchainGateway.isWalletValid(wallet));
		assertTrue(WalletUtils.isValidPrivateKey(wallet.getPrivateKey()));
	}

	// Does not work as it requires a test account with funds
	// @Test
	void testGasEstimation() throws Exception {
		ResponseWrapperDto<BigDecimal> result = blockchainGateway.getEstimatedGas(new TransferRequestDto(blockchainGateway.generateNewWallet(), blockchainGateway.generateNewWallet(), BigDecimal.ZERO));
		assertFalse(result.hasErrors());
		assertTrue(result.getResponse().compareTo(BigDecimal.ZERO) > 0);
	}
}
