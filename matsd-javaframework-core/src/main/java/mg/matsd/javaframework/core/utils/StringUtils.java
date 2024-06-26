package mg.matsd.javaframework.core.utils;

import mg.matsd.javaframework.core.annotations.Nullable;

public final class StringUtils {
    private StringUtils() { }

    public static boolean hasText(String string) {
        Assert.notNull(string, "L'argument de la fonction hasText ne peut pas être \"null\"");

        if (string.strip().isEmpty()) return false;
        return true;
    }

    public static boolean isBlank(@Nullable String string) {
        return string != null && string.isBlank();
    }

    public static boolean hasLength(String string, int length) {
        Assert.notNull(string, "L'argument de type \"String\" de la fonction hasLength ne peut pas être \"null\"");

        return string.length() == length;
    }

    public static String firstLetterToUpper(@Nullable String string) {
        Assert.notBlank(string, false, "L'argument de la fonction firstLetterToUpper ne peut pas être vide ou \"null\"");

        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String toSnakeCase(String string) {
        Assert.notNull(string, "L'argument de la fonction toSnakeCase ne peut pas être \"null\"");
        if (!hasText(string)) return string;

        StringBuilder stringBuilder = new StringBuilder();

        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                if (i > 0 && chars[i - 1] != '_') stringBuilder.append('_');
                stringBuilder.append(Character.toLowerCase(c));
            } else stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }
}
