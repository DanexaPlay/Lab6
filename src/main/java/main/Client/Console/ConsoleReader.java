package main.Client.Console;

import main.BasicClasses.Coordinates;
import main.BasicClasses.Furnish;
import main.BasicClasses.House;
import main.BasicClasses.HouseBuilder;
import main.BasicClasses.Transport;
import main.Common.FlatData;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.BooleanSupplier;

public class ConsoleReader {
    private final Scanner scanner;
    private final BooleanSupplier connectionChecker;

    public ConsoleReader(Scanner scanner) {
        this(scanner, () -> true);
    }

    public ConsoleReader(Scanner scanner, BooleanSupplier connectionChecker) {
        this.scanner = scanner;
        this.connectionChecker = connectionChecker;
    }

    public FlatData readFlat() {
        try {
            String n1 = enterName();
            Coordinates c1 = enterCoordinates();
            double a1 = enterArea();
            int nr1 = enterNumberOfRooms();
            boolean i1 = enterIsNew();
            Furnish f1 = enterFurnish();
            Transport t1 = enterTransport();
            House h1 = readHouse();
            return new FlatData(n1, c1, a1, nr1, i1, f1, t1, h1);
        } catch (IllegalArgumentException e1) {
            System.out.println("Неверный аргумент! Ввод начнется заново");
            return readFlat();
        } catch (NoSuchElementException e2) {
            System.out.println("Запрещённый символ!");
            throw e2;
        }
    }

    public House readHouse() {
        try {
            String n1 = enterHouseName();
            long y1 = enterYear();
            int nl1 = enterNumberOfLifts();
            long nf1 = enterNumbersOfFlatsOnFloor();
            return new HouseBuilder()
                    .name(n1)
                    .year(y1)
                    .numberOfLifts(nl1)
                    .numberOfFlatsOnFloor(nf1)
                    .build();
        } catch (IllegalArgumentException e1) {
            System.out.println("Неверный аргумент! Ввод начнется заново");
            return readHouse();
        } catch (NoSuchElementException e2) {
            System.out.println("Запрещённый символ!");
            throw e2;
        }
    }

    private void checkConnection() {
        if (!connectionChecker.getAsBoolean()) {
            throw new InputCancelledException("Сервер недоступен.");
        }
    }

    private String nextLine() {
        checkConnection();
        String line = scanner.nextLine().trim().replaceAll("[\\s]{2,}", " ");
        checkConnection();
        return line;
    }

    private String enterName() throws IllegalArgumentException {
        String name = "";
        System.out.println("Введите название!");
        String s1 = nextLine();
        if (!s1.isEmpty()) {
            name = s1;
            return name;
        } else {
            System.out.println("Строка не может быть пустой!");
            return name = enterName();
        }
    }

    private Coordinates enterCoordinates() throws IllegalArgumentException {
        Coordinates c1 = null;
        System.out.println("Введите координаты X и Y через пробел!");
        System.out.println("X - дробное число, вводите не больше 8 цифр после запятой, иначе будет погрешность!");
        System.out.println("Y - целое число от -2^64 до 2^64 - 1");
        try {
            String l1 = nextLine();
            if (l1.isEmpty()) {
                System.out.println("Строка не может быть пустой!");
                c1 = enterCoordinates();
                return c1;
            }
            String[] str = l1.split(" ");
            if (str.length > 2) {
                System.out.println("Слишком много аргументов!");
                c1 = enterCoordinates();
            }
            c1 = new Coordinates(Float.parseFloat(str[0].replace(",", ".")), Long.parseLong(str[1].replace(",", ".")));
            return c1;
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e2) {
            System.out.println("Неверно введены координаты!");
            c1 = enterCoordinates();
        }
        return c1;
    }

    private double enterArea() throws IllegalArgumentException {
        double area = 0;
        System.out.println("Введите площадь! Дробное число, не больше 16 знаков после запятой");
        try {
            area = Double.parseDouble(nextLine().replace(",", "."));
            return area;
        } catch (IllegalArgumentException e1) {
            System.out.println("Неправильный ввод!");
            area = enterArea();
        }
        return area;
    }

    private int enterNumberOfRooms() throws IllegalArgumentException {
        int number_of_rooms = 0;
        try {
            System.out.println("Введите количество комнат! Только целое число");
            number_of_rooms = Integer.parseInt(nextLine());
            return number_of_rooms;
        } catch (IllegalArgumentException e1) {
            System.out.println("Неправильный ввод!");
            number_of_rooms = enterNumberOfRooms();
        }
        return number_of_rooms;
    }

    private boolean enterIsNew() {
        System.out.println("Введите true если квартира новая, иначе false, или пропустите строку!");
        boolean isnew = false;
        try {
            String s1 = nextLine();
            s1 = s1.toLowerCase();
            isnew = Boolean.parseBoolean(s1);
            System.out.println("Будет записано значение: " + isnew);
            return isnew;
        } catch (IllegalArgumentException e1) {
            System.out.println("Неправильный ввод!");
            isnew = enterIsNew();
        }
        return isnew;
    }

    private Furnish enterFurnish() throws IllegalArgumentException {
        Furnish furnish;
        System.out.println("Введите дополнительные свойства квартиры: DESIGNER, BAD, LITTLE или пропустите строку!");
        String s = nextLine();
        if (s.isEmpty()) {
            furnish = null;
        } else {
            try {
                furnish = Furnish.valueOf(s.toUpperCase());
                return furnish;
            } catch (IllegalArgumentException e1) {
                System.out.println("Неверный ввод!");
                furnish = enterFurnish();
            }
        }
        return furnish;
    }

    private Transport enterTransport() throws IllegalArgumentException {
        Transport transport;
        System.out.println("Введите транспорт квартиры: NONE, NORMAL, ENOUGH или пропустите строку!");
        String s = nextLine();
        if (s.isEmpty()) {
            transport = null;
        } else {
            try {
                transport = Transport.valueOf(s.toUpperCase());
                return transport;
            } catch (IllegalArgumentException e1) {
                System.out.println("Неверный ввод!");
                transport = enterTransport();
            }
        }
        return transport;
    }

    private String enterHouseName() throws IllegalArgumentException {
        String name = "";
        System.out.println("Введите название дома!");
        String s1 = nextLine();
        if (!s1.isEmpty()) {
            name = s1;
            return name;
        } else {
            System.out.println("Строка не может быть пустой!");
            return name = enterHouseName();
        }
    }

    private long enterYear() throws IllegalArgumentException {
        long year = 0;
        System.out.println("Введите год! Только целое число");
        try {
            year = Long.parseLong(nextLine().replace(",", "."));
            return year;
        } catch (IllegalArgumentException e1) {
            System.out.println("Неправильный ввод!");
            year = enterYear();
        }
        return year;
    }

    private int enterNumberOfLifts() throws IllegalArgumentException {
        int numberOfLifts = 0;
        System.out.println("Введите количество лифтов! Только целое число");
        try {
            numberOfLifts = Integer.parseInt(nextLine().replace(",", "."));
            return numberOfLifts;
        } catch (IllegalArgumentException e1) {
            System.out.println("Неправильный ввод!");
            numberOfLifts = enterNumberOfLifts();
        }
        return numberOfLifts;
    }

    private long enterNumbersOfFlatsOnFloor() throws IllegalArgumentException {
        long numberOfFlats = 0;
        System.out.println("Введите количество квартир на этаж! Только целое число");
        try {
            numberOfFlats = Long.parseLong(nextLine().replace(",", "."));
            return numberOfFlats;
        } catch (IllegalArgumentException e1) {
            System.out.println("Неправильный ввод!");
            numberOfFlats = enterNumbersOfFlatsOnFloor();
        }
        return numberOfFlats;
    }
}
