package util.iota.ext;

import static org.assertj.core.api.Assertions.assertThat;

import org.iota.jota.utils.TrytesConverter;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TrytesTest {

    private final String payload = "{\"id\": \"some-it\", \"message\": \"hello iota\"}";

    @Test
    public void testTrytesConversion() {
        log.info("Testing conversion of {}", payload);
        String trytesText = TrytesConverter.asciiToTrytes(payload);
        String asciiText = TrytesConverter.trytesToAscii(trytesText);
        assertThat(asciiText).isEqualTo(payload);
    }
}
