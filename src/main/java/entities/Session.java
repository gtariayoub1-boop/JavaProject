package entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(
        name = "sessions",
        indexes = {
                @Index(name = "sessions_sess_lifetime_idx", columnList = "sess_lifetime")
        }
)
public class Session {

    @Id
    @Column(name = "sess_id", length = 128, nullable = false)
    private String sessId;

    @Lob
    @Column(name = "sess_data", nullable = false, columnDefinition = "BLOB")
    private byte[] sessData;

    @Column(name = "sess_lifetime", nullable = false)
    private Integer sessLifetime;

    @Column(name = "sess_time", nullable = false)
    private Integer sessTime;

    public Session() {
    }

    public Session(String sessId, byte[] sessData, Integer sessLifetime, Integer sessTime) {
        this.sessId = sessId;
        this.sessData = sessData;
        this.sessLifetime = sessLifetime;
        this.sessTime = sessTime;
    }

    public String getSessId() {
        return sessId;
    }

    public void setSessId(String sessId) {
        this.sessId = sessId;
    }

    public byte[] getSessData() {
        return sessData;
    }

    public String getSessDataAsString() {
        if (sessData == null) {
            return "";
        }
        return new String(sessData);
    }

    public void setSessData(byte[] sessData) {
        this.sessData = sessData;
    }

    public void setSessData(String sessData) {
        if (sessData != null) {
            this.sessData = sessData.getBytes();
        }
    }

    public Integer getSessLifetime() {
        return sessLifetime;
    }

    public void setSessLifetime(Integer sessLifetime) {
        this.sessLifetime = sessLifetime;
    }

    public Integer getSessTime() {
        return sessTime;
    }

    public void setSessTime(Integer sessTime) {
        this.sessTime = sessTime;
    }

    public boolean isExpired() {
        if (sessTime == null || sessLifetime == null) {
            return true;
        }
        long currentTime = Instant.now().getEpochSecond();
        return (sessTime + sessLifetime) < currentTime;
    }

    public int getRemainingLifetime() {
        if (sessTime == null || sessLifetime == null) {
            return 0;
        }
        long currentTime = Instant.now().getEpochSecond();
        long expiryTime = (long) sessTime + sessLifetime;
        long remaining = expiryTime - currentTime;
        return (int) Math.max(0, remaining);
    }

    public LocalDateTime getCreatedAt() {
        if (sessTime == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(sessTime), ZoneId.systemDefault());
    }

    public LocalDateTime getExpiresAt() {
        if (sessTime == null || sessLifetime == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond((long) sessTime + sessLifetime), ZoneId.systemDefault());
    }

    public void updateTimestamp() {
        this.sessTime = (int) Instant.now().getEpochSecond();
    }

    public void refreshLifetime(int lifetime) {
        this.sessLifetime = lifetime;
        updateTimestamp();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Session)) {
            return false;
        }
        Session session = (Session) o;
        return sessId != null && sessId.equals(session.sessId);
    }

    @Override
    public int hashCode() {
        return sessId != null ? sessId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Session{"
                + "sessId='" + sessId + '\''
                + ", sessDataSize=" + (sessData != null ? sessData.length : 0)
                + ", sessLifetime=" + sessLifetime
                + ", sessTime=" + sessTime
                + ", expired=" + isExpired()
                + ", remainingLifetime=" + getRemainingLifetime()
                + '}';
    }
}
