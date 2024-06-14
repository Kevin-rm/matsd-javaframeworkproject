package mg.matsd.javaframework.core.exceptions;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.StringUtils;

public abstract class BaseException extends RuntimeException {
    protected BaseException(@Nullable String message) {
        super(message);
    }

    protected BaseException(@Nullable String message, @Nullable String prefix) {
        super(formatMessage(message, prefix));
    }

    protected BaseException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    protected BaseException(
        @Nullable String    message,
        @Nullable String    prefix,
        @Nullable Throwable cause
    ) {
        super(formatMessage(message, prefix), cause);
    }

    protected BaseException(@Nullable Throwable cause) {
        super(cause);
    }

    private static String formatMessage(@Nullable String message, @Nullable String prefix) {
        if (prefix == null)              return message;
        if (StringUtils.isBlank(prefix)) return message;

        String formattedMessage = prefix;
        if (message != null && StringUtils.hasText(message))
            formattedMessage += String.format(" : %s", message);

        return formattedMessage;
    }
}
