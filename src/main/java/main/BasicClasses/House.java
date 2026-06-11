package main.BasicClasses;

import java.io.Serializable;

public class House implements Comparable<House>, Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private long year;
    private long numberOfFlatsOnFloor;
    private Integer numberOfLifts;

    public House(HouseBuilder houseBuilder) {
        if (houseBuilder == null) {
            throw new IllegalArgumentException("Не создан HouseBuilder!");
        }
        if (houseBuilder.name == null || houseBuilder.name.isEmpty()) {
            throw new IllegalArgumentException("Имя не должно быть пустым!");
        }
        if (houseBuilder.year <= 0) {
            throw new IllegalArgumentException("Год должен быть больше 0!");
        }
        if (houseBuilder.numberOfLifts == null || houseBuilder.numberOfLifts <= 0) {
            throw new IllegalArgumentException("Количество лифтов должно быть больше 0!");
        }
        if (houseBuilder.numberOfFlatsOnFloor <= 0) {
            throw new IllegalArgumentException("Количество квартир на этаж должно быть больше 0!");
        }
        this.name = houseBuilder.name;
        this.year = houseBuilder.year;
        this.numberOfLifts = houseBuilder.numberOfLifts;
        this.numberOfFlatsOnFloor = houseBuilder.numberOfFlatsOnFloor;
    }

    public String getName() {
        return name;
    }

    public long getYear() {
        return year;
    }

    public long getNumberOfFlatsOnFloor() {
        return numberOfFlatsOnFloor;
    }

    public Integer getNumberOfLifts() {
        return numberOfLifts;
    }

    public String toString() {
        return "house name: " + name + "\n" +
                "year: " + Long.toString(year) + "\n" +
                "number of flats on floor: " + Long.toString(numberOfFlatsOnFloor) + "\n" +
                "number of lifts: " + numberOfLifts.toString();
    }

    @Override
    public int compareTo(House o) {
        return (int) (this.getYear() - o.getYear());
    }
}
