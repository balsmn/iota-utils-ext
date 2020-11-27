package util.iota.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountResponse {
    private String accountAddress;
    private List<String> accountSeeds;
    private Long accountBalance;
    private WalletType walletType;
}
