package mg.itu.prom16.view.layout;

import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.io.Writer;

enum PutType {
    APPEND {
        @Override
        void write(Writer writer, String bodyResult, String putContents) throws IOException {
            writer.write(bodyResult);
            writer.write(putContents);
        }
    },
    PREPEND {
        @Override
        void write(Writer writer, String bodyResult, String putContents) throws IOException {
            writer.write(putContents);
            writer.write(bodyResult);
        }
    },
    REPLACE {
        @Override
        void write(Writer writer, String bodyResult, String putContents) throws IOException {
            writer.write(putContents);
        }
    };

    static PutType fromString(final String string) {
        Assert.notBlank(string, false, "L'argument string ne peut pas être vide ou \"null\"");

        try {
            return valueOf(string.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Type de \"put\" " +
                "non reconnu à partir de la chaîne passée en argument : \"%s\"", string));
        }
    }

    abstract void write(Writer writer, String bodyResult, String putContents) throws IOException;
}
