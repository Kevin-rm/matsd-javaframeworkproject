package mg.itu.prom16.validation;

import mg.matsd.javaframework.core.annotations.Nullable;

import java.time.LocalDateTime;

public class GlobalError {
    private final Throwable throwable;
    private final LocalDateTime createdAt;

    GlobalError(Throwable throwable) {
        this.throwable = throwable;
        createdAt = LocalDateTime.now();
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return throwable.getMessage();
    }

    @Nullable
    public Throwable getThrowableCause() {
        return throwable.getCause();
    }

    @Override
    public String toString() {
        return "GlobalError{" +
            "throwable=" + throwable +
            ", createdAt=" + createdAt +
            '}';
    }
}
