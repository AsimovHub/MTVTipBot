package ac.asimov.mtvtipbot.blockchain;

import ac.asimov.mtvtipbot.blockchain.contracts.AsimovToken;
import ac.asimov.mtvtipbot.dtos.*;
import ac.asimov.mtvtipbot.helper.DefaultMessage;
import ac.asimov.mtvtipbot.service.TransactionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class MultiVACBlockchainGateway {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${mtv.rpcUrl}")
    private String rpcUrl;

    @Value("${mtv.chainId}")
    private String chainId;

    @Value("${isaac.contractAddress}")
    private String isaacTokenAddress;

    @Autowired
    private TransactionService transactionService;



    public ResponseWrapperDto<AccountBalanceDto> getISAACAccountBalance(WalletAccountDto account) {
        try {
            if (account == null) {
                return new ResponseWrapperDto<>("Account is null");
            }
            Web3j web3 = Web3j.build(new HttpService(rpcUrl));
            Credentials credentials = Credentials.create(Keys.createEcKeyPair());
            AsimovToken contract = AsimovToken.load(isaacTokenAddress, web3, credentials, new StaticGasProvider(DefaultGasProvider.GAS_PRICE, BigInteger.valueOf(3_000_000)));
            BigInteger balance = contract.balanceOf(account.getReceiverAddress()).send();

            if (balance == null) {
                throw new Exception("Error while checking balance");
            }

            BigDecimal balanceInEther = Convert.fromWei(balance.toString(), Convert.Unit.ETHER);

            return new ResponseWrapperDto<>(new AccountBalanceDto(balanceInEther));
        } catch (Exception e) {
            e.printStackTrace();
            if (StringUtils.isBlank(e.getMessage())) {
                return new ResponseWrapperDto<>("RPC error");
            } else {
                return new ResponseWrapperDto<>(e.getMessage());
            }
        }
    }

    public ResponseWrapperDto<TransactionResponseDto> sendISAACFunds(TransferRequestDto request) {
        try {
            Web3j web3 = Web3j.build(new HttpService(rpcUrl));
            EthGetTransactionCount ethGetTransactionCount = web3
                    .ethGetTransactionCount(request.getSender().getReceiverAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();


            Credentials credentials = Credentials.create(request.getSender().getPrivateKey());
            BigDecimal weiAmount = Convert.toWei(request.getAmount(), Convert.Unit.ETHER);
            // Encode the function
            final Function function = new Function(
                    AsimovToken.FUNC_TRANSFER,
                    Arrays.<Type>asList(
                            new org.web3j.abi.datatypes.Address(request.getReceiver().getReceiverAddress()),
                            new org.web3j.abi.datatypes.generated.Uint256(weiAmount.toBigInteger())),
                    Collections.<TypeReference<?>>emptyList());
            String encodedFunction = FunctionEncoder
                    .encode(function);

            // Create new Transaction
            EthBlock lastBlock = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, true).send();
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, BigInteger.valueOf(1_000_000_000L), lastBlock.getBlock().getGasLimit(), isaacTokenAddress, BigInteger.ZERO, encodedFunction);

            // Sign the Transaction
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, Long.parseLong(chainId), credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            // Send the Transaction
            org.web3j.protocol.core.methods.response.EthSendTransaction transactionResponse = web3.ethSendRawTransaction(hexValue).sendAsync().get(1000 * 15, TimeUnit.MILLISECONDS);
            if (transactionResponse.hasError()) {
                return new ResponseWrapperDto<>(transactionResponse.getError().getMessage());
            }
            if (StringUtils.isBlank(transactionResponse.getTransactionHash())) {
                return new ResponseWrapperDto<>("Transaction error");
            }
            return new ResponseWrapperDto<>(new TransactionResponseDto(transactionResponse.getTransactionHash()));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>("RPC error");
        }
    }

    public ResponseWrapperDto<AccountBalanceDto> getMTVAccountBalance(WalletAccountDto account) {
        try {
            logger.info("Fetching MTV account balance for " + account.getReceiverAddress());
            Web3j web3 = Web3j.build(new HttpService(rpcUrl));
            EthGetBalance result = web3.ethGetBalance(account.getReceiverAddress(), DefaultBlockParameter.valueOf("latest")).send();
            BigDecimal balanceInEther = Convert.fromWei(result.getBalance().toString(), Convert.Unit.ETHER);
            return new ResponseWrapperDto<>(new AccountBalanceDto(balanceInEther));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>(e.getMessage());
        } catch (Error e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>("RPC error");
        }
    }

    public ResponseWrapperDto<TransactionResponseDto> sendMTVFunds(TransferRequestDto request) {
        try {
            Web3j web3 = Web3j.build(new HttpService(rpcUrl));

            BigDecimal weiAmount = Convert.toWei(request.getAmount(), Convert.Unit.ETHER);
            Credentials credentials = Credentials.create(request.getSender().getPrivateKey());

            BigInteger gasLimit = BigInteger.valueOf(21000);

            BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce =  ethGetTransactionCount.getTransactionCount();

            RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    request.getReceiver().getReceiverAddress(),
                    weiAmount.toBigInteger());
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, Integer.parseInt(chainId), credentials);

            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();

            if (ethSendTransaction.hasError()) {
                String errorMessage = ethSendTransaction.getError() != null ? ethSendTransaction.getError().getMessage() : "Blockchain error";
                if (StringUtils.equals(errorMessage, "replacement transaction underpriced")) {
                    errorMessage = "Please wait some time to process the current transaction";
                } else if (StringUtils.equals(errorMessage, "already known")) {
                    errorMessage = DefaultMessage.UNKNOWN_ERROR_CHECK_ACCOUNT_BALANCE;
                }
                logger.error(errorMessage);
                return new ResponseWrapperDto<>(errorMessage);
            }

            String transactionHash = ethSendTransaction.getTransactionHash();
            transactionService.createTransaction(request, transactionHash);

            return new ResponseWrapperDto<>(new TransactionResponseDto(transactionHash));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>("RPC error");
        }
    }

    public ResponseWrapperDto<TransactionResponseDto> sendMTVCompleteFunds(TransferRequestDto request) {
        try {
            Web3j web3 = Web3j.build(new HttpService(rpcUrl));

            EthGetBalance balanceResult = web3.ethGetBalance(request.getSender().getReceiverAddress(), DefaultBlockParameter.valueOf("latest")).send();
            BigDecimal balanceInEther = Convert.fromWei(balanceResult.getBalance().toString(), Convert.Unit.ETHER);

            Credentials credentials = Credentials.create(request.getSender().getPrivateKey());
            BigInteger gasLimit = BigInteger.valueOf(21000);
            BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            ResponseWrapperDto<BigDecimal> gasFeeResponse = getEstimatedGas(request);

            BigDecimal sentAmount;
            if (gasFeeResponse.hasErrors()) {
                sentAmount = balanceInEther.subtract(new BigDecimal("0.000021"));
            } else {
                sentAmount = balanceInEther.subtract(gasFeeResponse.getResponse());
            }
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    request.getReceiver().getReceiverAddress(),
                    Convert.toWei(sentAmount, Convert.Unit.ETHER).toBigInteger());

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, Integer.parseInt(chainId), credentials);

            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();

            if (ethSendTransaction.hasError()) {
                String errorMessage = ethSendTransaction.getError() != null ? ethSendTransaction.getError().getMessage() : "Blockchain error";
                logger.error(errorMessage);
                return new ResponseWrapperDto<>(errorMessage);
            }

            String transactionHash = ethSendTransaction.getTransactionHash();

            request.setAmount(balanceInEther);
            transactionService.createTransaction(request, transactionHash);

            return new ResponseWrapperDto<>(new TransactionResponseDto(transactionHash));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>("RPC error");
        }
    }

    public ResponseWrapperDto<BigDecimal> getEstimatedGas(TransferRequestDto request) {
        try {
            Web3j web3 = Web3j.build(new HttpService(rpcUrl));
            Credentials credentials = Credentials.create(request.getSender().getPrivateKey());
            BigInteger gasLimit = BigInteger.valueOf(21000);
            BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce =  ethGetTransactionCount.getTransactionCount();
            Transaction estTransaction = Transaction.createEtherTransaction(
                    request.getSender().getReceiverAddress(),
                    nonce,
                    gasPrice,
                    gasLimit,
                    request.getReceiver().getReceiverAddress(),
                    Convert.toWei(request.getAmount() != null ? request.getAmount().toString() : "0.1", Convert.Unit.ETHER).toBigInteger());
            EthEstimateGas result = web3.ethEstimateGas(estTransaction).send();

            if (result.hasError()) {
                if (StringUtils.isBlank(result.getError().getMessage())) {
                    return new ResponseWrapperDto<>(DefaultMessage.SERVER_ERROR);
                } else {
                    return new ResponseWrapperDto<>(result.getError().getMessage());
                }
            }
            BigInteger gasFee = result.getAmountUsed();
            BigDecimal gasInEther = Convert.fromWei(Convert.toWei(gasFee.toString(), Convert.Unit.GWEI), Convert.Unit.ETHER);
            return new ResponseWrapperDto<>(gasInEther);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>("RPC error");
        }
    }

    public ResponseWrapperDto<BigDecimal> getISAACEstimatedGas(TransferRequestDto request) {
        try {
            if (request.getReceiver() == null) {
                return new ResponseWrapperDto<>("Receiver is null");
            }
            if (request.getSender() == null) {
                return new ResponseWrapperDto<>("Sender is null");
            }
            Web3j web3 = Web3j.build(new HttpService(rpcUrl));
            Credentials credentials = Credentials.create(request.getSender().getPrivateKey());
            BigInteger gasLimit = BigInteger.valueOf(3000000);
            BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce =  ethGetTransactionCount.getTransactionCount();

            Function sendFunction = new Function(
                    AsimovToken.FUNC_TRANSFER,
                    Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(request.getReceiver().getReceiverAddress()),
                            new org.web3j.abi.datatypes.generated.Uint256(Convert.toWei(request.getAmount() != null ? request.getAmount().toString() : "0.1", Convert.Unit.ETHER).toBigInteger())),
                    Collections.<TypeReference<?>>emptyList());
            Transaction tx = Transaction.createFunctionCallTransaction(request.getReceiver().getReceiverAddress(),
                    nonce,
                    gasPrice,
                    gasLimit,
                    request.getReceiver().getReceiverAddress(),
                    Convert.toWei(request.getAmount() != null ? request.getAmount().toString() : "0.1", Convert.Unit.ETHER).toBigInteger(),
                    FunctionEncoder.encode(sendFunction));
            EthEstimateGas gasEstimate = web3.ethEstimateGas(tx).send();
            if (gasEstimate.hasError()) {
                logger.info("Contract error: {}", gasEstimate.getError().getMessage());
            } else {
                logger.info("Gas estimate: {}", gasEstimate.getAmountUsed()); // will throw in case of error
            }

            BigInteger gasFee = gasEstimate.getAmountUsed();
            BigDecimal gasInEther = Convert.fromWei(Convert.toWei(gasFee.toString(), Convert.Unit.GWEI), Convert.Unit.ETHER);
            return new ResponseWrapperDto<>(gasInEther);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapperDto<>("RPC error");
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
