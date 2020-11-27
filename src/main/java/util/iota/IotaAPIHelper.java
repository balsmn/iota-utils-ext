package util.iota;

import java.util.List;
import java.util.Optional;

import org.iota.jota.model.Bundle;
import org.iota.jota.model.Transaction;
import org.iota.jota.pow.SpongeFactory;
import org.iota.jota.utils.Converter;
import org.iota.jota.utils.Multisig;
import org.iota.jota.utils.Signing;
import org.iota.jota.utils.TrytesConverter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class IotaAPIHelper {
    public static Integer DEFAULT_INDEX = 0;
    public static Integer DEFAULT_SECURITY_LEVEL = 1;

    private IotaAPIHelper() {
    }

    public static String getCustomMessageFromTransaction(Transaction transaction) {
        // Custom messages are max of length 2187 chars and any additional chars are represented as "9" in trytes
        // replaced with empty space character on conversion back to ascii.
        return TrytesConverter.trytesToAscii(transaction.getSignatureFragments().substring(0, 2186)).trim();
    }

    public static String getTagFromTransaction(Transaction transaction) {
        // tags are of max length 27 chars and any additional chars are represented as "9" in trytes
        // replaced with empty space character on conversion back to ascii.
        return TrytesConverter.trytesToAscii(transaction.getTag().substring(0, 26)).trim();
    }

    public static String createNewMultisigAddress(String seed1, String seed2, Integer userSecurityLevel) {
        int securityLevel = Optional.of(userSecurityLevel).orElse(DEFAULT_SECURITY_LEVEL);
        Multisig ms = new Multisig();
        String digestOne = ms.getDigest(seed1, securityLevel, DEFAULT_INDEX);
        String digestTwo = ms.getDigest(seed2, securityLevel, DEFAULT_INDEX);
        String initiatedMultisigDigests = ms.addAddressDigest(digestOne, "");
        // Add the multisig by absorbing the second cosigners key digest
        String finalMultisigDigests = ms.addAddressDigest(digestTwo, initiatedMultisigDigests);
        // finally we generate the multisig address itself
        String multiSigAddress = ms.finalizeAddress(finalMultisigDigests);
        log.debug("New multisig address {} created. Valid: {}", multiSigAddress, ms.validateAddress(multiSigAddress,
                new int[][] {Converter.trits(digestOne), Converter.trits(digestTwo)}));
        return multiSigAddress;
    }

    public static Bundle createBundleAndAddMultiSignatures(String fromAddress,
                                                           List<Transaction> transactions,
                                                           Integer securityLevel,
                                                           String... seeds) {
        Signing sgn = new Signing(SpongeFactory.create(SpongeFactory.Mode.KERL));
        Multisig ms = new Multisig();
        Bundle bundle = new Bundle(transactions, transactions.size());
        int index = 0;
        for (String seed : seeds) {
            bundle = ms.addSignature(bundle, fromAddress, ms.getKey(seed, 0, securityLevel));
            index++;
        }
         Boolean validSignatures = sgn.validateSignatures(bundle, fromAddress);
        if (!validSignatures) {
            throw new InvalidSignaturesException(
                    "Signatures on the created bundle doesn't match the given address. May be wrong securityLevel "
                            + "was provided than what was given when the address was created");
        }
        return bundle;
    }
}
