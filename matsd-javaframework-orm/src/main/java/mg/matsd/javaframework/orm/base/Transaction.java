package mg.matsd.javaframework.orm.base;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.orm.exceptions.RollbackException;
import mg.matsd.javaframework.orm.exceptions.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;

public class Transaction {
    private final Session session;

    Transaction(Session session) {
        this.session = session;
    }

    public void begin() {
        try {
            checkIfSessionIsOpen();
            Assert.state(!isActive(), "La transaction actuelle est déjà active");

            Connection connection = session.connection();
            if (connection.getAutoCommit()) connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new TransactionException("Problème rencontré lors du début de la transaction", e);
        }
    }

    public void commit() {
        try {
            checkIfSessionIsOpen();
            Assert.state(isActive(),
                "Impossible d'effectuer un commit car la transaction actuelle n'est plus active");

            Connection connection = session.connection();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TransactionException("Échec du commit", e);
        }
    }

    public void rollback() {
        try {
            checkIfSessionIsOpen();
            Assert.state(isActive(),
                "Impossible d'effectuer un rollback car la transaction actuelle n'est plus active");

            Connection connection = session.connection();
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TransactionException("Échec du rollback", new RollbackException(e));
        }
    }

    public boolean isActive() {
        try {
            return !session.connection().getAutoCommit();
        } catch (SQLException e) {
            throw new TransactionException(e);
        }
    }

    private void checkIfSessionIsOpen() {
        Assert.state(session.isOpen(),
            "La session responsable de la transaction courante a été fermée");
    }
}
