package util.iota.ext.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferResponse {
    private String transactionHash;
    private Boolean successful;
    private Long timestamp;
    /**
     * Address that holds the remaining funds, if any
     * after a successful transfer. If the transfer has
     * failed, then the remainderAddress will be empty.
     */
    private String remainderAddress;
}
