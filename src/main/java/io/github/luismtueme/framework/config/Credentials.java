package io.github.luismtueme.framework.config;

/** A login for the application under test. {@link #toString()} never prints the password. */
public record Credentials(String username, String password) {

    @Override
    public String toString() {
        return "Credentials[username=%s, password=***]".formatted(username);
    }
}
