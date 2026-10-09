package io.github.luismtueme.framework.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MaskingTest {

    @Test
    void recognizesSecretHeaderNames() {
        assertThat(Masking.isSecret("Authorization")).isTrue();
        assertThat(Masking.isSecret("api_key")).isTrue();
        assertThat(Masking.isSecret("X-Api-Key")).isTrue();
        assertThat(Masking.isSecret("Content-Type")).isFalse();
    }

    @Test
    void masksSecretHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer secret");
        headers.put("Content-Type", "application/json");
        assertThat(Masking.headers(headers))
                .containsEntry("Authorization", Masking.MASK)
                .containsEntry("Content-Type", "application/json");
    }

    @Test
    void masksSecretJsonFieldsAtAnyDepth() {
        String masked = Masking.json(
                """
                {"user":"bob","password":"secret","nested":{"token":"abc","ok":true}}
                """);
        assertThat(masked)
                .contains("\"password\" : \"***\"")
                .contains("\"token\" : \"***\"")
                .contains("\"user\"");
        assertThat(masked).doesNotContain("secret").doesNotContain("abc");
    }

    @Test
    void leavesNonJsonBodiesAlone() {
        assertThat(Masking.json("not-json")).isEqualTo("not-json");
    }
}
