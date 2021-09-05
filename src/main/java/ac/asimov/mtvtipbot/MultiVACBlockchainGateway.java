package ac.asimov.mtvtipbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Keys;

import java.math.BigDecimal;

@Component
public class MultiVACBlockchainGateway {

    @Value("swap.gateway.mtv.rpcUrl")
    private String rpcUrl;

    @Value("swap.gateway.mtv.chainId")
    private Integer chainId;


    public ResponseWrapperDto<AccountBalance> getAccountBalance(WalletAccount account) {
        try {
            logger.info("Fetching account balance for " + getCurrency());
            Web3j web3 = Web3j.build(new HttpService(getRPCUrl()));
            EthGetBalance result = web3.ethGetBalance(account.getReceiverAddress(), DefaultBlockParameter.valueOf("latest")).send();
            BigDecimal balanceInEther = Convert.fromWei(result.getBalance().toString(), Convert.Unit.ETHER);
            return new ResponseWrapperDto(new AccountBalance(balanceInEther));
        } catch (Exception e) {
            e.printStackTrace();
            if (StringUtils.isBlank(e.getMessage())) {
                return new ResponseWrapperDto(getCurrency() + " RPC error");
            } else {
                return new ResponseWrapperDto(e.getMessage());
            }
        }
    }

    public ResponseWrapperDto<TransactionResponseDto> sendFunds(TransferRequestDto request) {
        try {
            Web3j web3 = Web3j.build(new HttpService(getRPCUrl()));

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
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, getChainId(), credentials);

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
                return new ResponseWrapperDto(getCurrency() + " RPC error");
            } else {
                return new ResponseWrapperDto(e.getMessage());
            }
        }
    }

    public ResponseWrapperDto<TransactionResponseDto> sendCompleteFunds(TransferRequestDto request) {
        try {
            Web3j web3 = Web3j.build(new HttpService(getRPCUrl()));
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
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, getChainId(), credentials);

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
                return new ResponseWrapperDto(getCurrency() + " RPC error");
            } else {
                return new ResponseWrapperDto(e.getMessage());
            }
        }
    }

    public WalletAccount generateNewWallet() throws Exception {
        ECKeyPair walletKeys = Keys.createEcKeyPair();
        String address = "0x" + Keys.getAddress(walletKeys.getPublicKey());
        return new WalletAccount(walletKeys.getPrivateKey().toString(16), address);
    }

    public boolean isWalletValid(WalletAccount walletAccount) {
        return StringUtils.startsWith(walletAccount.getReceiverAddress(),"0x");
    }

    public BigDecimal getTransactionFee() throws Exception {
        Web3j web3 = Web3j.build(new HttpService(getRPCUrl()));
        EthSendTransaction result = new EthSendTransaction();

        BigDecimal weiAmount = Convert.toWei("1", Convert.Unit.ETHER);
        Credentials credentials = Credentials.create(getPoolWallet().getPrivateKey());


        // A transfer cost 21,000 units of gas
        BigInteger gasLimit = BigInteger.valueOf(21000);

        // I am willing to pay 1Gwei (1,000,000,000 wei or 0.000000001 ether) for each unit of gas consumed by the transaction.
        BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();
        // String from, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value
        // Get nonce
        EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        Transaction estTransaction = Transaction.createEtherTransaction(
                getPoolWallet().getReceiverAddress(),
                nonce,
                gasPrice,
                gasLimit,
                getPoolWallet().getReceiverAddress(),
                weiAmount.toBigInteger());

        BigInteger gasFee = web3.ethEstimateGas(estTransaction).send().getAmountUsed();

        BigDecimal amount = Convert.fromWei(new BigDecimal(gasFee), Convert.Unit.ETHER);
        return amount;
    }
}
