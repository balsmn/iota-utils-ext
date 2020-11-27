package util.iota.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "iota.config")
@Data
public class BlockchainClientConfig {
    private String host;
    /**
     * depending upon which the length of the privateKey is generated.
     * 1 - 2187 characters
     * 2 - 4374 characters
     * 3 - 6561 characters
     */
    private Integer seedSecurityLevel = 1;
    /**
     * If true, wallet balance will not be checked before transfer.
     * True is used only for testing. If true is used in production,
     * the transaction will be accepted by IoTA network but never be confirmed.
     */
    private Boolean testMode = false;
}
