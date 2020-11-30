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
package io.github.balsmn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.iota.jota.IotaAPI;
import org.iota.jota.model.Transaction;
import org.iota.jota.utils.Checksum;
import org.iota.jota.utils.Multisig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.balsmn.config.BlockchainClientConfig;
import io.github.balsmn.model.AccountResponse;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class AccountManagerTest {

    public static final String TEST_TAG = "MyOwnTag";
    private final String payload = "{\"id\": \"some-it\", \"message\": \"hello iota\"}";

    private final Multisig ms = new Multisig();
    private AccountResponse targetTestAccount;

    @Autowired
    AccountManager accountManager;

    @Autowired
    BlockchainClientConfig config;

    @Autowired
    IotaAPI iotaAPI;

    @BeforeEach
    public void init() {
        targetTestAccount = accountManager.createNewAccount();
    }

    @Test
    void createNewAccountSendTransactionAndFindTransaction() {
        AccountResponse accountResponse = accountManager.createNewAccount();
        assertThat(accountResponse).isNotNull();
        assertThat(accountResponse.getAccountSeeds()).hasSize(1);
        List<Transaction> transactionsBeforeSending = accountManager.findTransactionsByTag(TEST_TAG);
        List<Transaction> transactions = accountManager.sendTransaction(accountResponse.getAccountAddress(),
                accountResponse.getAccountSeeds().get(0), targetTestAccount.getAccountAddress(), 0L, payload, TEST_TAG);
        Transaction transaction = transactions.get(0);
        assertThat(IotaAPIHelper.getCustomMessageFromTransaction(transaction)).isEqualTo(payload);
        assertThat(IotaAPIHelper.getTagFromTransaction(transaction)).isEqualTo(TEST_TAG);

        List<Transaction> foundTransactions = accountManager.findTransactionsByTag(TEST_TAG);
        assertThat(foundTransactions.size()).isEqualTo(transactionsBeforeSending.size() + 1);
    }

    @Test
    void createMultisigAccountSendTransactionAndFindTransaction() {
        AccountResponse accountResponse = accountManager.createNewMultisigAccount();
        assertThat(accountResponse).isNotNull();
        assertThat(accountResponse.getAccountSeeds()).hasSize(2);
        String remainderAddress = IotaAPIHelper.createNewMultisigAddress(accountResponse.getAccountSeeds().get(0),
                accountResponse.getAccountSeeds().get(1), config.getSeedSecurityLevel());

        List<Transaction> transactionsBeforeSending = accountManager.findTransactionByAddress(
                targetTestAccount.getAccountAddress());
        List<Transaction> transactions = accountManager.sendMultisigTransaction(accountResponse.getAccountAddress(),
                remainderAddress, targetTestAccount.getAccountAddress(), accountResponse.getAccountSeeds().get(0),
                accountResponse.getAccountSeeds().get(1), config.getSeedSecurityLevel(), 100L, payload, TEST_TAG);
        Transaction transaction = transactions.get(0);
        assertThat(IotaAPIHelper.getCustomMessageFromTransaction(transaction)).isEqualTo(payload);
        assertThat(IotaAPIHelper.getTagFromTransaction(transaction)).isEqualTo(TEST_TAG);

        List<Transaction> foundTransactions = accountManager.findTransactionByAddress(
                targetTestAccount.getAccountAddress());

        assertThat(foundTransactions.size()).isEqualTo(transactionsBeforeSending.size() + 1);
        assertThat(iotaAPI.wereAddressesSpentFrom(Checksum.addChecksum(accountResponse.getAccountAddress()))
                .getStates()[0]).isTrue();
    }
}
