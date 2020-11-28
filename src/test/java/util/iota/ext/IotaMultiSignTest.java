/*-
 * #%L
 * IoTA API Extension Library
 * %%
 * Copyright (C) 2020 github.com/balsmn
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package util.iota.ext;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.iota.jota.IotaAPI;
import org.iota.jota.error.ArgumentException;
import org.iota.jota.model.Bundle;
import org.iota.jota.model.Transaction;
import org.iota.jota.model.Transfer;
import org.iota.jota.pow.SpongeFactory;
import org.iota.jota.utils.Checksum;
import org.iota.jota.utils.Converter;
import org.iota.jota.utils.Multisig;
import org.iota.jota.utils.SeedRandomGenerator;
import org.iota.jota.utils.Signing;
import org.iota.jota.utils.TrytesConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class IotaMultiSignTest {

    private static final String TEST_SEED1 = SeedRandomGenerator.generateNewSeed();
    private static final String TEST_SEED2 = SeedRandomGenerator.generateNewSeed();
    private static final String REMAINDER_ADDRESS = IotaAPIHelper.createNewMultisigAddress(TEST_SEED1, TEST_SEED2, 2);
    private static final String RECEIVE_ADDRESS = "ZGHXPZYDKXPEOSQTAQOIXEEI9K9YKFKCWKYYTYAUWXK9QZAVMJXWAIZABOXHHNNBJIEBEUQRTBWGLYMTXPENVCJZBX";
    private static final String TEST_TAG = TrytesConverter.asciiToTrytes("TestTag");

    @Autowired
    private IotaAPI iotaClient;

    @Test
    public void testBasicMultiSigMaxSec() throws ArgumentException {
        Signing sgn = new Signing(SpongeFactory.create(SpongeFactory.Mode.KERL));
        String multiSigAddress = IotaAPIHelper.createNewMultisigAddress(TEST_SEED1, TEST_SEED2, 3);
        List<Transfer> transfers = new ArrayList<>();
        transfers.add(new Transfer(RECEIVE_ADDRESS, 999, "", TEST_TAG));

        List<Transaction> trxs = iotaClient.initiateTransfer(6, Checksum.addChecksum(multiSigAddress),
                Checksum.addChecksum(REMAINDER_ADDRESS), transfers, null, true);
        Bundle bundle = IotaAPIHelper.createBundleAndAddMultiSignatures(multiSigAddress, trxs, 3, TEST_SEED1,
                TEST_SEED2);

        log.debug("Bundle from transaction {}", bundle.getTransactions().get(0).getBundle());
        boolean isValidSignature = sgn.validateSignatures(bundle, multiSigAddress);
        log.debug("Result of multi-signature validation is {}", isValidSignature);
        assertThat(isValidSignature).isTrue();
    }

    @Test
    public void testBasicMultiSigMinSec() throws ArgumentException {
        Signing sgn = new Signing(SpongeFactory.create(SpongeFactory.Mode.KERL));
        String multiSigAddress = IotaAPIHelper.createNewMultisigAddress(TEST_SEED1, TEST_SEED2, 1);

        List<Transfer> transfers = new ArrayList<>();
        transfers.add(new Transfer(RECEIVE_ADDRESS, 999, "", TEST_TAG));

        List<Transaction> trxs = iotaClient.initiateTransfer(2, Checksum.addChecksum(multiSigAddress),
                Checksum.addChecksum(REMAINDER_ADDRESS), transfers, null, true);
        Bundle bundle = IotaAPIHelper.createBundleAndAddMultiSignatures(multiSigAddress, trxs, 1, TEST_SEED1,
                TEST_SEED2);

        log.debug("Bundle from transaction {}", bundle.getTransactions().get(0).getBundle());
        boolean isValidSignature = sgn.validateSignatures(bundle, multiSigAddress);
        log.debug("Result of multi-signature validation is {}", isValidSignature);
        assertThat(isValidSignature).isTrue();
    }

    @Test
    public void testDifferentSecurityLevel() {
        Signing sgn = new Signing(SpongeFactory.create(SpongeFactory.Mode.KERL));
        String seed = SeedRandomGenerator.generateNewSeed();
        for (int securityLevel = 1; securityLevel <= 3; securityLevel++) {
            int[] privateKeyTrits = sgn.key(Converter.trits(seed), 0, securityLevel);
            int[] digestsTrits = sgn.digests(privateKeyTrits);
            int[] addressTrits = sgn.address(digestsTrits);
            String digests = Converter.trytes(digestsTrits);
            String privateKey = Converter.trytes(privateKeyTrits);
            String address = Converter.trytes(addressTrits);
            log.debug("Security Level. {}, PrivateKeyLength: {}, Digest size: {}, address length: {}", securityLevel,
                    privateKey.length(), digests.length(), address.length());
            assertThat(privateKey.length()).isEqualTo(securityLevel * 2187);
            assertThat(digests.length()).isEqualTo(securityLevel * 81);
            assertThat(address.length()).isEqualTo(81);
        }
    }

    @Test
    public void testMultipleAddressGenerationFromSameSeeds() {
        Multisig ms = new Multisig();
        String seed1 = SeedRandomGenerator.generateNewSeed();
        String seed2 = SeedRandomGenerator.generateNewSeed();
        for (int securityLevel = 1; securityLevel <= 3; securityLevel++) {
            String digestOne = ms.getDigest(seed1, securityLevel, 0);
            String digestTwo = ms.getDigest(seed2, securityLevel, 0);
            String finalMultisigAddress1 = ms.finalizeAddress(
                    ms.addAddressDigest(digestTwo, ms.addAddressDigest(digestOne, "")));
            String finalMultisigAddress2 = ms.finalizeAddress(
                    ms.addAddressDigest(digestTwo, ms.addAddressDigest(digestOne, "")));
            assertThat(finalMultisigAddress1).isNotEqualTo(finalMultisigAddress2);
            log.debug("Security Level. {}, DigestOneLength: {}, DigestTwoLength: {} finalMultisigAddressLength: {}",
                    securityLevel, digestOne.length(), digestTwo.length(), finalMultisigAddress1.length());
        }
    }
}
