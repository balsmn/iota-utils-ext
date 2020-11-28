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
