package main.BasicClasses;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Flat implements Comparable<Flat>, Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Coordinates coordinates;
    private LocalDate creationDate;
    private double area;
    private int numberOfRooms;
    private Boolean isNew;
    private Furnish furnish;
    private Transport transport;
    private House house;

    static Long idCount = 0L;

    static {
        idCount += 1;
    }

    public House getHouse() {
        return house;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public double getArea() {
        return area;
    }

    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public Furnish getFurnish() {
        return furnish;
    }

    public Transport getTransport() {
        return transport;
    }

    public Flat(FlatBuilder flatBuilder) {
        if (flatBuilder == null) {
            throw new IllegalArgumentException("Не создан FlatBuilder!");
        }
        if (flatBuilder.name == null || flatBuilder.name.isEmpty()) {
            throw new IllegalArgumentException("Имя не должно быть пустым!");
        }
        if (Objects.isNull(flatBuilder.coordinates)) {
            throw new IllegalArgumentException("Нет координат!");
        }
        if (flatBuilder.area <= 0) {
            throw new IllegalArgumentException("Площадь должна быть больше 0!");
        }
        if (flatBuilder.numberOfRooms <= 0) {
            throw new IllegalArgumentException("Количество комнат должно быть больше 0!");
        }
        id = ++idCount;
        this.name = flatBuilder.name;
        this.coordinates = flatBuilder.coordinates;
        creationDate = LocalDate.now();
        this.area = flatBuilder.area;
        this.numberOfRooms = flatBuilder.numberOfRooms;
        this.isNew = flatBuilder.isNew;
        this.furnish = flatBuilder.furnish;
        this.transport = flatBuilder.transport;
        this.house = flatBuilder.house;
    }

    public String toString() {
        String s = "";
        s += "id: " + id.toString() + "\n";
        s += "name: " + name + "\n";
        s += "coordinates: " + coordinates.toString() + "\n";
        s += "creation date: " + creationDate.toString() + "\n";
        s += "area: " + Double.toString(area) + "\n";
        s += "number of rooms: " + Integer.toString(numberOfRooms) + "\n";
        s += "is new: " + Objects.toString(isNew, "") + "\n";
        s += "furnish: ";
        try {
            s += furnish.toString() + "\n";
        } catch (NullPointerException e1) {
            s += " " + "\n";
        }
        s += "transport: ";
        try {
            s += transport.toString() + "\n";
        } catch (NullPointerException e2) {
            s += " " + "\n";
        }
        try {
            s += house.toString();
        } catch (NullPointerException e3) {
            s += "house: ";
        }
        return s;
    }

    public void setId(long id) {
        this.id = id;
        idCount--;
    }

    @Override
    public int compareTo(Flat o) {
        return (int) (this.getId() - o.getId());
    }
}
