package util.iota.ext;

import org.iota.jota.IotaAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import util.iota.ext.config.BlockchainClientConfig;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BlockchainClientInstance {

    final BlockchainClientConfig config;

    public BlockchainClientInstance(BlockchainClientConfig config) {
        this.config = config;
    }

    @Bean
    public IotaAPI initClient() {
        log.debug("Using IOTA network: {}", config.getHost());
        // Create a new instance of the API object
        // and specify which node to connect to
        IotaAPI api = new IotaAPI.Builder()
                .protocol("https")
                .host(config.getHost())
                .port(443)
                .build();
        log.debug("Blockchain Node info : {}", api.getNodeInfo());
        return api;
    }
}
