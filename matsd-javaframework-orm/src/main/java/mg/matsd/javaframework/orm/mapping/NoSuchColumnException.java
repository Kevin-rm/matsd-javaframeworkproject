package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.exceptions.BaseException;

class NoSuchColumnException extends BaseException {
    NoSuchColumnException(Relation relation, String notFoundColumn) {
        super(String.format("Aucune colonne identifiée par le nom de champ \"%s\" dans la relation \"%s\"",
            notFoundColumn, relation.getName()
        ));
    }
}
