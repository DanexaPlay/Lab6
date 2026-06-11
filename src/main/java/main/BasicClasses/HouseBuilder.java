package main.BasicClasses;

import java.io.Serializable;

public class HouseBuilder implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String name;
    protected long year;
    protected long numberOfFlatsOnFloor;
    protected Integer numberOfLifts;

    public HouseBuilder name(String name) {
        this.name = name;
        return this;
    }

    public HouseBuilder year(long year) {
        this.year = year;
        return this;
    }

    public HouseBuilder numberOfFlatsOnFloor(long numberOfFlatsOnFloor) {
        this.numberOfFlatsOnFloor = numberOfFlatsOnFloor;
        return this;
    }

    public HouseBuilder numberOfLifts(int numberOfLifts) {
        this.numberOfLifts = numberOfLifts;
        return this;
    }

    public HouseBuilder() {
        super();
    }

    public House build() {
        House h1 = null;
        if (validateHouse()) {
            h1 = new House(this);
        } else {
            System.out.println("Недостаточно параметров для объекта House!");
        }
        return h1;
    }

    private boolean validateHouse() {
        return name != null && !name.isEmpty() && year > 0 && numberOfFlatsOnFloor > 0 && numberOfLifts != null && numberOfLifts > 0;
    }
}
