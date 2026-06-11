package main.BasicClasses;

import java.io.Serializable;
import java.util.Objects;

public class FlatBuilder implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String name;
    protected Coordinates coordinates;
    protected double area;
    protected int numberOfRooms;
    protected Boolean isNew = null;
    protected Furnish furnish;
    protected Transport transport;
    protected House house;

    public FlatBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FlatBuilder coordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public FlatBuilder area(double area) {
        this.area = area;
        return this;
    }

    public FlatBuilder numberOfRooms(int numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
        return this;
    }

    public FlatBuilder isNew(Boolean isNew) {
        this.isNew = isNew;
        return this;
    }

    public FlatBuilder furnish(Furnish furnish) {
        this.furnish = furnish;
        return this;
    }

    public FlatBuilder transport(Transport transport) {
        this.transport = transport;
        return this;
    }

    public FlatBuilder house(House house) {
        this.house = house;
        return this;
    }

    public FlatBuilder() {
        super();
    }

    public Flat build() {
        Flat f1 = null;
        if (ValidateFlat()) {
            f1 = new Flat(this);
        } else {
            System.out.println("Недостаточно параметров для объекта Flat!");
        }
        return f1;
    }

    private boolean ValidateFlat() {
        return name != null && !name.isEmpty() && !Objects.isNull(coordinates) && area > 0 && numberOfRooms > 0;
    }
}
