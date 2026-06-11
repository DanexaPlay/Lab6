package main.Common;

import main.BasicClasses.Coordinates;
import main.BasicClasses.Furnish;
import main.BasicClasses.House;
import main.BasicClasses.Transport;

import java.io.Serializable;

public class FlatData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Coordinates coordinates;
    private double area;
    private int numberOfRooms;
    private Boolean isNew;
    private Furnish furnish;
    private Transport transport;
    private House house;

    public FlatData(String name, Coordinates coordinates, double area, int numberOfRooms,
                    Boolean isNew, Furnish furnish, Transport transport, House house) {
        this.name = name;
        this.coordinates = coordinates;
        this.area = area;
        this.numberOfRooms = numberOfRooms;
        this.isNew = isNew;
        this.furnish = furnish;
        this.transport = transport;
        this.house = house;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
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

    public House getHouse() {
        return house;
    }
}
