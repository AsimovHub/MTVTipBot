package ac.asimov.mtvtipbot;

import ac.asimov.mtvtipbot.dtos.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

@Component
public class MultiVACBlockchainGateway {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("mtv.rpcUrl")
    private String rpcUrl;

    @Value("mtv.chainId")
    private String chainId;


    public ResponseWrapperDto<AccountBalanceDto> getAccountBalance(WalletAccountDto account) {
        try {
            logger.info("Fetching account balance for " + account.getReceiverAddress());
            Web3j web3 = Web3j.build(new HttpService(rpcUrl));
            EthGetBalance result = web3.ethGetBalance(account.getReceiverAddress(), DefaultBlockParameter.valueOf("latest")).send();
            BigDecimal balanceInEther = Convert.fromWei(result.getBalance().toString(), Convert.Unit.ETHER);
            return new ResponseWrapperDto(new AccountBalanceDto(balanceInEther));
        } catch (Exception e) {
            e.printStackTrace();
            if (StringUtils.isBlank(e.getMessage())) {
                return new ResponseWrapperDto("RPC error");
            } else {
                return new ResponseWrapperDto(e.getMessage());
            }
        }
    }

    public ResponseWrapperDto<TransactionResponseDto> sendFunds(TransferRequestDto request) {
        try {
            Web3j web3 = Web3j.build(new HttpService(rpcUrl));

            BigDecimal weiAmount = Convert.toWei(request.getAmount(), Convert.Unit.ETHER);
            Credentials credentials = Credentials.create(request.getSender().getPrivateKey());


            // A transfer cost 21,000 units of gas
            BigInteger gasLimit = BigInteger.valueOf(21000);

            // I am willing to pay 1Gwei (1,000,000,000 wei or 0.000000001 ether) for each unit of gas consumed by the transaction.
            BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();
            // String from, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value
            // Get nonce
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce =  ethGetTransactionCount.getTransactionCount();

            // Prepare the rawTransaction
            RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    request.getReceiver().getReceiverAddress(),
                    weiAmount.toBigInteger());

            // Sign the transaction
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, Integer.parseInt(chainId), credentials);

            // Convert it to Hexadecimal String to be sent to the node
            String hexValue = Numeric.toHexString(signedMessage);
            // Send transaction
            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();

            // Get the transaction hash
            String transactionHash = ethSendTransaction.getTransactionHash();
            return new ResponseWrapperDto(new TransactionResponseDto(transactionHash));
        } catch (Exception e) {
            e.printStackTrace();
            if (StringUtils.isBlank(e.getMessage())) {
                return new ResponseWrapperDto("RPC error");
            } else {
                return new ResponseWrapperDto(e.getMessage());
            }
        }
    }

    public ResponseWrapperDto<TransactionResponseDto> sendCompleteFunds(TransferRequestDto request) {
        try {
            Web3j web3 = Web3j.build(new HttpService(rpcUrl));
            EthSendTransaction result = new EthSendTransaction();

            BigDecimal weiAmount = Convert.toWei("1", Convert.Unit.ETHER);
            Credentials credentials = Credentials.create(request.getSender().getPrivateKey());


            // A transfer cost 21,000 units of gas
            BigInteger gasLimit = BigInteger.valueOf(21000);

            // I am willing to pay 1Gwei (1,000,000,000 wei or 0.000000001 ether) for each unit of gas consumed by the transaction.
            BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();
            // String from, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value
            // Get nonce
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce =  ethGetTransactionCount.getTransactionCount();
            Transaction estTransaction = Transaction.createEtherTransaction(
                    request.getSender().getReceiverAddress(),
                    nonce,
                    gasPrice,
                    gasLimit,
                    request.getReceiver().getReceiverAddress(),
                    weiAmount.toBigInteger());

            BigInteger gasFee = web3.ethEstimateGas(estTransaction).send().getAmountUsed();

            // Prepare the rawTransaction
            RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    request.getReceiver().getReceiverAddress(),
                    weiAmount.toBigInteger().subtract(gasFee));

            // Sign the transaction
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, Integer.parseInt(chainId), credentials);

            // Convert it to Hexadecimal String to be sent to the node
            String hexValue = Numeric.toHexString(signedMessage);
            // Send transaction
            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();

            // Get the transaction hash
            String transactionHash = ethSendTransaction.getTransactionHash();
            return new ResponseWrapperDto(new TransactionResponseDto(transactionHash));
        } catch (Exception e) {
            e.printStackTrace();
            if (StringUtils.isBlank(e.getMessage())) {
                return new ResponseWrapperDto("RPC error");
            } else {
                return new ResponseWrapperDto(e.getMessage());
            }
        }
    }

    public WalletAccountDto generateNewWallet() throws Exception {
        ECKeyPair walletKeys = Keys.createEcKeyPair();
        String address = "0x" + Keys.getAddress(walletKeys.getPublicKey());
        return new WalletAccountDto("0x" + walletKeys.getPrivateKey().toString(16), address);
    }

    public boolean isWalletValid(WalletAccountDto walletAccount) {
        return WalletUtils.isValidAddress(walletAccount.getReceiverAddress());
    }
}
