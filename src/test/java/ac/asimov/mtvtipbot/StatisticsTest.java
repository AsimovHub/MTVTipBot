package ac.asimov.mtvtipbot;

import ac.asimov.mtvtipbot.blockchain.MultiVACBlockchainGateway;
import ac.asimov.mtvtipbot.dtos.ResponseWrapperDto;
import ac.asimov.mtvtipbot.service.StatisticsService;
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

import static org.junit.jupiter.api.Assertions.assertFalse;

@ContextConfiguration(classes = { MTVTipBotApplication.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {TransactionService.class, MultiVACBlockchainGateway.class })
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestEntityManager
@ImportAutoConfiguration
class StatisticsTest {

	@Autowired
	private StatisticsService statisticsService;


	@Autowired
	private MultiVACBlockchainGateway multiVACBlockchainGateway;

	@Test
	public void testFetchTransaction() {
		String transactionHash = "0x6f2ed2b9f00d999c5e7c44c19a7678d9b8215b5a063512d8c0d33771321d58b5";
		ResponseWrapperDto<String> result = statisticsService.getTransaction(transactionHash);
		assertFalse(result.hasErrors());
		assertFalse(StringUtils.isBlank(result.getResponse()));
	}
}
