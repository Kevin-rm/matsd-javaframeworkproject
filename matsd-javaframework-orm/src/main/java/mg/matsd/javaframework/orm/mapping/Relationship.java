package mg.matsd.javaframework.orm.mapping;

import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.orm.base.internal.UtilFunctions;

class Relationship {
    protected Class<?>  owner;
    protected Class<?>  targetEntity;
    protected FetchType fetchType;

    protected Relationship(Class<?> owner, Class<?> targetEntity, FetchType fetchType) {
        this.owner     = owner;
        this.setTargetEntity(targetEntity);
        this.fetchType = fetchType;
    }

    Class<?> getOwner() {
        return owner;
    }

    Class<?> getTargetEntity() {
        return targetEntity;
    }

    protected void setTargetEntity(Class<?> targetEntity) {
        UtilFunctions.assertIsEntity(targetEntity);
        Assert.state(targetEntity != owner,
            () -> new IllegalArgumentException(
                String.format("L'entité cible doit être différente de l'entité propriétaire")
            )
        );

        this.targetEntity = targetEntity;
    }

    FetchType getFetchType() {
        return fetchType;
    }

    /* static class ManyToMany extends Relationship {

        protected ManyToMany(Class<?> targetEntity, FetchType fetchType) {
            super(targetEntity);
        }
    }

    static class ManyToOne extends Relationship {

        protected ManyToOne(Class<?> targetEntity, FetchType fetchType) {
            super(targetEntity, fetchType);
        }
    }

    static class OneToMany extends Relationship {

        protected OneToMany(Class<?> targetEntity, FetchType fetchType) {
            super(targetEntity, fetchType);
        }
    }

    static class OneToOne extends Relationship {

        protected OneToOne(Class<?> targetEntity, FetchType fetchType) {
            super(targetEntity, fetchType);
        }
    } */
}
