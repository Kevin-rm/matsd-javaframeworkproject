package mg.matsd.javaframework.orm;

import mg.matsd.javaframework.orm.annotations.Entity;
import mg.matsd.javaframework.orm.annotations.Table;

@Entity
@Table(name = "the_parcelle")
public class Parcelle {
    private int id;
    private double surface;

    public int getId() {
        return id;
    }

    public Parcelle setId(int id) {
        this.id = id;
        return this;
    }

    public double getSurface() {
        return surface;
    }

    public Parcelle setSurface(double surface) {
        this.surface = surface;
        return this;
    }

    @Override
    public String toString() {
        return "Parcelle{" +
            "id=" + id +
            ", surface=" + surface +
            '}';
    }
}
