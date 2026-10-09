package io.github.luismtueme.framework.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * {@link ExchangeRecorder} is a REST Assured filter exercised by {@code @api} scenarios. Unit coverage here focuses on
 * the masking helpers the recorder depends on when building the report document.
 */
class ExchangeRecorderTest {

    @Test
    void maskingUsedByTheRecorderHidesTokensInJsonBodies() {
        String body = Masking.json("{\"access_token\":\"raw-secret\",\"id\":1}");
        assertThat(body).contains(Masking.MASK).contains("\"id\"").doesNotContain("raw-secret");
    }

    @Test
    void maskingUsedByTheRecorderHidesAuthorizationHeaders() {
        assertThat(Masking.headers(Map.of("Authorization", "Bearer raw", "Accept", "application/json")))
                .containsEntry("Authorization", Masking.MASK)
                .containsEntry("Accept", "application/json");
    }
}
