package io.github.luismtueme.acceptance.support;

import io.github.luismtueme.framework.config.Config;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;

/**
 * Retries an assertion until it passes or the wait timeout (WAIT_TIMEOUT) runs out, then fails with the last
 * assertion error. Use it for anything that changes after an action. Never sleep instead.
 *
 * <pre>
 * Eventually.assertThat(() -> assertThat(loginScreen.errorText()).contains("locked out"));
 * </pre>
 */
public final class Eventually {

    private static final Duration POLL_INTERVAL = Duration.ofMillis(100);

    private Eventually() {}

    public static void assertThat(ThrowingRunnable assertion) {
        Awaitility.await()
                .atMost(Config.get().waitTimeout())
                .pollInterval(POLL_INTERVAL)
                .pollInSameThread()
                .untilAsserted(assertion);
    }
}
