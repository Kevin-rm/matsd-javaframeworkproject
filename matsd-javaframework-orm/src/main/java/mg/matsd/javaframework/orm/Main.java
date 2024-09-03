package mg.matsd.javaframework.orm;

import mg.matsd.javaframework.orm.base.EntityManagerFactory;
import mg.matsd.javaframework.orm.exceptions.DatabaseException;
import mg.matsd.javaframework.orm.setup.Configuration;

public class Main {
    public static void main(String[] args) throws DatabaseException {
        EntityManagerFactory entityManagerFactory = (EntityManagerFactory) new Configuration().buildSessionFactory();

        /*try (EntityManager entityManager = ((EntityManagerFactory) new Configuration().buildSessionFactory()).createEntityManager()) {
            //RawQuery<Cueillette> rawQuery = entityManager.createRawQuery("SELECT * FROM the_cueillette", Cueillette.class);

            //System.out.println(rawQuery.getResultsAsList());

            //Relation relation = new Relation(Cueillette.class);
            //System.out.println(relation.getColumns());
        }*/
    }
}