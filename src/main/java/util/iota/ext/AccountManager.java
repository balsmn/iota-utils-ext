package util.iota.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.iota.jota.IotaAPI;
import org.iota.jota.builder.AddressRequest;
import org.iota.jota.dto.response.GetNewAddressResponse;
import org.iota.jota.dto.response.SendTransferResponse;
import org.iota.jota.error.ArgumentException;
import org.iota.jota.model.Bundle;
import org.iota.jota.model.Transaction;
import org.iota.jota.model.Transfer;
import org.iota.jota.utils.Checksum;
import org.iota.jota.utils.SeedRandomGenerator;
import org.iota.jota.utils.TrytesConverter;
import org.springframework.stereotype.Component;

import util.iota.ext.config.BlockchainClientConfig;
import util.iota.ext.model.WalletType;
import util.iota.ext.model.AccountResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AccountManager {

    private static final Integer DEPTH = 3;
    private static final Integer MINIMUM_WEIGHT_MAGNITUDE = 9;

    private final IotaAPI iotaAPI;
    private final BlockchainClientConfig config;

    public AccountManager(IotaAPI iotaAPI, BlockchainClientConfig config) {
        this.iotaAPI = iotaAPI;
        this.config = config;
    }

    public AccountResponse createNewAccount() {
        String newSeed = SeedRandomGenerator.generateNewSeed();
        try {
            GetNewAddressResponse response = iotaAPI.generateNewAddresses(
                    new AddressRequest.Builder(newSeed, config.getSeedSecurityLevel()).index(
                            IotaAPIHelper.DEFAULT_INDEX).checksum(true).build());
            log.debug("New address {} created in duration: {}ms", response.getAddresses(), response.getDuration());
            return AccountResponse.builder()
                    .accountAddress(Checksum.removeChecksum(response.first()))
                    .accountBalance(0L)
                    .accountSeeds(Collections.singletonList(newSeed))
                    .walletType(WalletType.NORMAL)
                    .build();
        } catch (ArgumentException e) {
            log.error("Could not create new address", e);
            throw new AccountCreationException("Could not create new address");
        }
    }

    public AccountResponse createNewMultisigAccount() {
        String userSeed = SeedRandomGenerator.generateNewSeed();
        String backendSeed = SeedRandomGenerator.generateNewSeed();
        try {
            String addressWithChecksum = IotaAPIHelper.createNewMultisigAddress(userSeed, backendSeed,
                    config.getSeedSecurityLevel());
            return AccountResponse.builder()
                    .accountAddress(addressWithChecksum)
                    .accountBalance(0L)
                    .accountSeeds(Arrays.asList(userSeed, backendSeed))
                    .walletType(WalletType.MULTISIG)
                    .build();
        } catch (ArgumentException e) {
            log.error("Could not create new multisig address", e);
            return null;
        }
    }

    public Long getAccountBalance(String address) {
        return iotaAPI.getBalance(address);
    }

    public List<Transaction> sendTransaction(String fromAddress,
                                             String walletSeed,
                                             String toAddress,
                                             Long amount,
                                             String customMessage,
                                             String tag) {
        Transfer transfer = new Transfer(Checksum.addChecksum(toAddress), amount,
                TrytesConverter.asciiToTrytes(customMessage), TrytesConverter.asciiToTrytes(tag));
        SendTransferResponse transferResponse = iotaAPI.sendTransfer(walletSeed, IotaAPIHelper.DEFAULT_SECURITY_LEVEL,
                DEPTH, MINIMUM_WEIGHT_MAGNITUDE, Collections.singletonList(transfer), null, null, true, true, null);
        if (transferResponse.getSuccessfully().length != 0 && Arrays.stream(transferResponse.getSuccessfully())
                .findFirst()
                .orElse(false)) {
            log.debug("Successfully completed transaction from [{}] to [{}]. Transaction hash: [{}]", fromAddress,
                    toAddress, transferResponse.getTransactions().get(0).getHash());
            return transferResponse.getTransactions();
        } else {
            log.error("Transaction failed. Transfer response: {}", transferResponse);
            return Collections.EMPTY_LIST;
        }
    }

    public List<Transaction> sendMultisigTransaction(String multiSigFromAddress,
                                                     String remainderAddress,
                                                     String toAddress,
                                                     String multisigSeed1,
                                                     String multisigSeed2,
                                                     Integer securityLevel,
                                                     Long amount,
                                                     String customMessage,
                                                     String tag) {
        Transfer transfer = new Transfer(Checksum.addChecksum(toAddress), amount,
                TrytesConverter.asciiToTrytes(customMessage), TrytesConverter.asciiToTrytes(tag));
        List<Transaction> transactions = iotaAPI.initiateTransfer((securityLevel * 2),
                Checksum.addChecksum(multiSigFromAddress), Checksum.addChecksum(remainderAddress),
                Collections.singletonList(transfer), config.getTestMode());
        Bundle bundle = IotaAPIHelper.createBundleAndAddMultiSignatures(multiSigFromAddress, transactions,
                securityLevel, multisigSeed1, multisigSeed2);
        List<String> trytes = new ArrayList<>();
        for (Transaction tx : bundle.getTransactions()) {
            trytes.add(tx.toTrytes());
        }
        transactions = iotaAPI.sendTrytes(trytes.toArray(new String[trytes.size()]), DEPTH, MINIMUM_WEIGHT_MAGNITUDE,
                null);
        log.debug("Successfully executed Multisig transaction {}", transactions);
        return transactions;
    }

    public List<Transaction> findTransactionsByTag(String tag) {
        List<Transaction> transactions = iotaAPI.findTransactionObjectsByTag(TrytesConverter.asciiToTrytes(tag));
        log.debug("{} Transactions found for the given tag {}", transactions.size(), tag);
        return transactions;

    }

    public List<Transaction> findTransactionByAddress(String address) {
        List<Transaction> transactions = iotaAPI.findTransactionObjectsByAddresses(
                new String[] {Checksum.addChecksum(address)});
        log.debug("{} Transactions found for the given address {}", transactions.size(), address);
        return transactions;
    }
}
