package services;

import java.time.LocalDateTime;

public class PasswordResetTokenData {

    private final Long userId;
    private final String email;
    private final String fullName;
    private final String token;
    private final LocalDateTime expiresAt;

    public PasswordResetTokenData(Long userId, String email, String fullName, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
