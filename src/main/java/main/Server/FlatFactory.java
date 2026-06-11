package main.Server;

import main.BasicClasses.Flat;
import main.BasicClasses.FlatBuilder;
import main.Common.FlatData;

public class FlatFactory {
    public Flat create(FlatData data) {
        validateFlatData(data);

        return new FlatBuilder()
                .name(data.getName())
                .coordinates(data.getCoordinates())
                .area(data.getArea())
                .numberOfRooms(data.getNumberOfRooms())
                .isNew(data.getIsNew())
                .furnish(data.getFurnish())
                .transport(data.getTransport())
                .house(data.getHouse())
                .build();
    }

    public void validateFlatData(FlatData data) {
        if (data == null) {
            throw new IllegalArgumentException("Объект не передан!");
        }
        if (data.getName() == null || data.getName().isBlank()) {
            throw new IllegalArgumentException("Имя не должно быть пустым!");
        }
        if (data.getCoordinates() == null) {
            throw new IllegalArgumentException("Нет координат!");
        }
        if (data.getArea() <= 0) {
            throw new IllegalArgumentException("Площадь должна быть больше 0!");
        }
        if (data.getNumberOfRooms() <= 0) {
            throw new IllegalArgumentException("Количество комнат должно быть больше 0!");
        }
    }
}
