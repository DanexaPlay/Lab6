package main.Server.DatabaseManagment;
import main.BasicClasses.*;
import main.Common.Encryption;
import main.Common.FlatData;
import main.Common.LoginData;
import main.Common.RegisterData;
import main.Server.Commands.CommandProcessor;
import main.Server.FlatFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    private final String url = "jdbc:postgresql://localhost:15432/studs";
    private Connection connection;
    private FlatFactory flatFactory = new FlatFactory();

    public DatabaseManager() {
        try {
            Properties info = new Properties();
            info.load(new FileInputStream("src/main/java/main/Server/db.cfg"));
            connection = DriverManager.getConnection(url, info);
            logger.info("База данных подключена");
            System.out.println("Успешное подключение к базе данных");
            initializeTables();
        } catch (SQLException e) {
            this.connection = null;
            System.out.println("Не удалось подключиться к базе данных!");
        } catch (IOException e2) {
            this.connection = null;
            System.out.println("Файл конфигурации базы данных не найден!");
        }
    }

    private void initializeTables() {
        if (!isConnected()) {
            logger.warn("Невозможно инициализировать таблицы: нет подключения к базе данных");
            return;
        }

        String createFurnishType = """
            DO $$
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'furnish') THEN
                    CREATE TYPE furnish AS ENUM ('DESIGNER', 'BAD', 'LITTLE');
                END IF;
            END
            $$;
            """;

        String createTransportType = """
            DO $$
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transport') THEN
                    CREATE TYPE transport AS ENUM ('NONE', 'NORMAL', 'ENOUGH');
                END IF;
            END
            $$;
            """;

        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                hash_password TEXT NOT NULL,
                salt TEXT NOT NULL
            );
            """;

        String createFlatsTable = """
            CREATE TABLE IF NOT EXISTS flats (
                id BIGSERIAL PRIMARY KEY,
                author_username TEXT NOT NULL REFERENCES users(username) ON DELETE CASCADE,

                name TEXT NOT NULL,
                x_coordinate REAL NOT NULL,
                y_coordinate BIGINT NOT NULL,
                creation_date DATE NOT NULL,

                area DOUBLE PRECISION NOT NULL,
                number_of_rooms INT NOT NULL,
                is_new BOOLEAN NOT NULL,

                furnish furnish,
                transport transport,

                house_name TEXT NOT NULL,
                house_year BIGINT NOT NULL,
                house_number_of_flats_on_floor BIGINT NOT NULL,
                house_number_of_lifts INT NOT NULL
            );
            """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(createFurnishType);
            statement.execute(createTransportType);
            statement.execute(createUsersTable);
            statement.execute(createFlatsTable);

            logger.info("Таблицы базы данных успешно проверены/созданы");
        } catch (SQLException e) {
            logger.warn("Не удалось инициализировать таблицы базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isMissingSchemaException(SQLException e) {
        String state = e.getSQLState();
        return "42P01".equals(state) || "42704".equals(state);
    }

    private boolean recoverSchemaIfNeeded(SQLException e) {
        if (!isMissingSchemaException(e)) {
            return false;
        }
        logger.warn("Схема БД отсутствует или повреждена. Пересоздаю таблицы...");
        initializeTables();
        logger.warn("Схема БД пересоздана. Команду нужно повторить.");
        return true;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public synchronized boolean checkUsernameAvailability(String username) {
        String query = "SELECT 1 FROM USERS WHERE username = ?";
        PreparedStatement st = null;
        try {
            st = connection.prepareStatement(query);
            st.setString(1, username);
            ResultSet resultSet = st.executeQuery();
            if (resultSet.next()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка с базой данных пользователей");
        }
        return true;
    }

    public synchronized boolean checkPassword(String username, String password) {
        if (!isConnected()) {
            return false;
        }

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return false;
        }

        String query = """
            SELECT hash_password, salt
            FROM users
            WHERE username = ?
            """;

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, username);

            try (ResultSet resultSet = st.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }

                String storedHash = resultSet.getString("hash_password");
                String salt = resultSet.getString("salt");

                String calculatedHash = Encryption.encryptString(password + salt);

                return storedHash.equals(calculatedHash);
            }
        } catch (SQLException e) {
            if (recoverSchemaIfNeeded(e)) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean isFlatOwner(long id, String username) {
        if (!isConnected()) {
            return false;
        }

        if (username == null || username.isBlank()) {
            return false;
        }

        String query = """
            SELECT 1
            FROM flats
            WHERE id = ? AND author_username = ?
            """;

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setLong(1, id);
            st.setString(2, username);

            try (ResultSet resultSet = st.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void register(RegisterData registerData) throws SQLException, UserAlreadyExistsException {
        String username = registerData.getUsername();
        String password = registerData.getPassword();
        if (!checkUsernameAvailability(username)) {
            throw new UserAlreadyExistsException(username);
        }
        String query = "INSERT INTO USERS VALUES (?, ?, ?)";
        PreparedStatement st = connection.prepareStatement(query);
        Random random = new Random();
        String salt = String.valueOf(random.nextInt(1000, 10000));
        password += salt;
        password = Encryption.encryptString(password);
        st.setString(1, username);
        st.setString(2, password);
        st.setString(3, salt);
        st.execute();
        logger.info("Пользователь " + username + " зарегистрировался");
    }

    public synchronized void login(LoginData loginData) throws SQLException {
        String username = loginData.getUsername();
        String password = loginData.getPassword();
        if (checkUsernameAvailability(username)) {
            throw new UserNotExistsException(username);
        }
        if (!checkPassword(username, password)) {
            throw new InvalidCredentialsException();
        }
        logger.info("Пользователь " + username + " авторизовался");
    }

    public synchronized long addFlat(FlatData data, String username) {
        try {
            Flat flat = flatFactory.create(data);
            String query = """
                    INSERT INTO FLATS (author_username, name, x_coordinate, y_coordinate, creation_date, area, number_of_rooms, is_new, furnish, transport,
                    house_name, house_year, house_number_of_flats_on_floor, house_number_of_lifts) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::furnish, ?::transport, ?, ?, ?, ?)
                    RETURNING id""";
            PreparedStatement st = connection.prepareStatement(query);
            st.setString(1, username);
            st.setString(2, flat.getName());
            Coordinates coordinates = flat.getCoordinates();
            st.setFloat(3, coordinates.getX());
            st.setLong(4, coordinates.getY());
            st.setDate(5, Date.valueOf(flat.getCreationDate()));
            st.setDouble(6, flat.getArea());
            st.setInt(7, flat.getNumberOfRooms());
            st.setBoolean(8, flat.getIsNew());
            if (flat.getFurnish() == null) {
                st.setNull(9, java.sql.Types.VARCHAR);
            } else {
                st.setString(9, flat.getFurnish().name());
            }

            if (flat.getTransport() == null) {
                st.setNull(10, java.sql.Types.VARCHAR);
            } else {
                st.setString(10, flat.getTransport().name());
            }
            House house = flat.getHouse();
            st.setString(11, house.getName());
            st.setLong(12, house.getYear());
            st.setLong(13, house.getNumberOfFlatsOnFloor());
            st.setInt(14, house.getNumberOfLifts());
            ResultSet resultSet = st.executeQuery();
            if (resultSet.next()) {
                long id = resultSet.getLong("id");
                logger.info("Добавлен новый объект Flat с id " + id);
                return id;
            }

            throw new SQLException("База данных не вернула id после добавления Flat");

        } catch (SQLException e) {
            if (recoverSchemaIfNeeded(e)) {
                return -1;
            }
            e.printStackTrace();
            return -1;
        }
    }

    public synchronized boolean removeFlatById(long id, String username) {
        if (!isConnected()) {
            logger.warn("Удаление невозможно: нет подключения к базе данных");
            return false;
        }

        if (username == null || username.isBlank()) {
            return false;
        }

        String query = """
            DELETE FROM flats
            WHERE id = ? AND author_username = ?
            """;

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setLong(1, id);
            st.setString(2, username);

            int affectedRows = st.executeUpdate();

            return affectedRows > 0;
        } catch (SQLException e) {
            if (recoverSchemaIfNeeded(e)) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    public synchronized Vector<Flat> read_from_database() {
        if (!isConnected()) {
            logger.warn("База данных недоступна, возвращается пустая коллекция");
            return new Vector<>();
        }
        Vector<Flat> flats = new Vector<>();

        String query = """
            SELECT
                id,
                author_username,
                name,
                x_coordinate,
                y_coordinate,
                creation_date,
                area,
                number_of_rooms,
                is_new,
                furnish,
                transport,
                house_name,
                house_year,
                house_number_of_flats_on_floor,
                house_number_of_lifts
            FROM FLATS
            """;

        try (PreparedStatement st = connection.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                Coordinates coordinates = new Coordinates(
                        rs.getFloat("x_coordinate"),
                        rs.getLong("y_coordinate")
                );

                Furnish furnish = null;
                String furnishValue = rs.getString("furnish");
                if (furnishValue != null) {
                    furnish = Furnish.valueOf(furnishValue);
                }

                Transport transport = null;
                String transportValue = rs.getString("transport");
                if (transportValue != null) {
                    transport = Transport.valueOf(transportValue);
                }

                House house = new HouseBuilder()
                        .name(rs.getString("house_name"))
                        .year(rs.getLong("house_year"))
                        .numberOfFlatsOnFloor(rs.getLong("house_number_of_flats_on_floor"))
                        .numberOfLifts(rs.getInt("house_number_of_lifts"))
                        .build();

                Flat flat = new FlatBuilder()
                        .name(rs.getString("name"))
                        .coordinates(coordinates)
                        .area(rs.getDouble("area"))
                        .numberOfRooms(rs.getInt("number_of_rooms"))
                        .isNew(rs.getBoolean("is_new"))
                        .furnish(furnish)
                        .transport(transport)
                        .house(house)
                        .build();
                flat.setId(rs.getLong("id"));
                flat.setCreationDate(rs.getDate("creation_date").toLocalDate());
                flat.setAuthorUsername(rs.getString("author_username"));

                flats.add(flat);
            }

        } catch (SQLException e) {
            if (recoverSchemaIfNeeded(e)) {
                return new Vector<>();
            }
            e.printStackTrace();
        }

        return flats;
    }

    public synchronized Flat updateFlat(long id, FlatData data, String username) {
        if (!isConnected()) {
            return null;
        }

        Flat flat = flatFactory.create(data);

        String query = """
                UPDATE flats
                SET
                    name = ?,
                    x_coordinate = ?,
                    y_coordinate = ?,
                    area = ?,
                    number_of_rooms = ?,
                    is_new = ?,
                    furnish = ?::furnish,
                    transport = ?::transport,
                    house_name = ?,
                    house_year = ?,
                    house_number_of_flats_on_floor = ?,
                    house_number_of_lifts = ?
                WHERE id = ? AND author_username = ?
                RETURNING
                    id,
                    author_username,
                    name,
                    x_coordinate,
                    y_coordinate,
                    creation_date,
                    area,
                    number_of_rooms,
                    is_new,
                    furnish,
                    transport,
                    house_name,
                    house_year,
                    house_number_of_flats_on_floor,
                    house_number_of_lifts
                """;

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, flat.getName());

            Coordinates coordinates = flat.getCoordinates();
            st.setFloat(2, coordinates.getX());
            st.setLong(3, coordinates.getY());

            st.setDouble(4, flat.getArea());
            st.setInt(5, flat.getNumberOfRooms());
            st.setBoolean(6, flat.getIsNew());

            if (flat.getFurnish() == null) {
                st.setNull(7, java.sql.Types.VARCHAR);
            } else {
                st.setString(7, flat.getFurnish().name());
            }

            if (flat.getTransport() == null) {
                st.setNull(8, java.sql.Types.VARCHAR);
            } else {
                st.setString(8, flat.getTransport().name());
            }

            House house = flat.getHouse();
            st.setString(9, house.getName());
            st.setLong(10, house.getYear());
            st.setLong(11, house.getNumberOfFlatsOnFloor());
            st.setInt(12, house.getNumberOfLifts());

            st.setLong(13, id);
            st.setString(14, username);

            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return buildFlatFromResultSet(rs);
            }
        } catch (SQLException e) {
            if (recoverSchemaIfNeeded(e)) {
                return null;
            }
            e.printStackTrace();
            return null;
        }
    }

    private Flat buildFlatFromResultSet(ResultSet rs) throws SQLException {
        Coordinates coordinates = new Coordinates(
                rs.getFloat("x_coordinate"),
                rs.getLong("y_coordinate")
        );

        Furnish furnish = null;
        String furnishValue = rs.getString("furnish");
        if (furnishValue != null) {
            furnish = Furnish.valueOf(furnishValue);
        }

        Transport transport = null;
        String transportValue = rs.getString("transport");
        if (transportValue != null) {
            transport = Transport.valueOf(transportValue);
        }

        House house = new HouseBuilder()
                .name(rs.getString("house_name"))
                .year(rs.getLong("house_year"))
                .numberOfFlatsOnFloor(rs.getLong("house_number_of_flats_on_floor"))
                .numberOfLifts(rs.getInt("house_number_of_lifts"))
                .build();

        Flat flat = new FlatBuilder()
                .name(rs.getString("name"))
                .coordinates(coordinates)
                .area(rs.getDouble("area"))
                .numberOfRooms(rs.getInt("number_of_rooms"))
                .isNew(rs.getBoolean("is_new"))
                .furnish(furnish)
                .transport(transport)
                .house(house)
                .build();
        flat.setAuthorUsername(rs.getString("author_username"));
        flat.setId(rs.getLong("id"));

        return flat;
    }

    public synchronized boolean saveCollection(Collection<Flat> collection) {
        if (!isConnected()) {
            logger.warn("Коллекция не сохранена: нет подключения к базе данных");
            return false;
        }

        String query = """
            INSERT INTO flats (
                id,
                author_username,
                name,
                x_coordinate,
                y_coordinate,
                creation_date,
                area,
                number_of_rooms,
                is_new,
                furnish,
                transport,
                house_name,
                house_year,
                house_number_of_flats_on_floor,
                house_number_of_lifts
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::furnish, ?::transport, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                author_username = EXCLUDED.author_username,
                name = EXCLUDED.name,
                x_coordinate = EXCLUDED.x_coordinate,
                y_coordinate = EXCLUDED.y_coordinate,
                creation_date = EXCLUDED.creation_date,
                area = EXCLUDED.area,
                number_of_rooms = EXCLUDED.number_of_rooms,
                is_new = EXCLUDED.is_new,
                furnish = EXCLUDED.furnish,
                transport = EXCLUDED.transport,
                house_name = EXCLUDED.house_name,
                house_year = EXCLUDED.house_year,
                house_number_of_flats_on_floor = EXCLUDED.house_number_of_flats_on_floor,
                house_number_of_lifts = EXCLUDED.house_number_of_lifts
            """;

        boolean previousAutoCommit = true;

        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement st = connection.prepareStatement(query)) {
                for (Flat flat : collection) {
                    st.setLong(1, flat.getId());
                    st.setString(2, flat.getAuthorUsername());
                    st.setString(3, flat.getName());

                    st.setFloat(4, flat.getCoordinates().getX());
                    st.setLong(5, flat.getCoordinates().getY());

                    st.setDate(6, java.sql.Date.valueOf(flat.getCreationDate()));
                    st.setDouble(7, flat.getArea());
                    st.setInt(8, flat.getNumberOfRooms());
                    st.setBoolean(9, flat.getIsNew());

                    if (flat.getFurnish() == null) {
                        st.setNull(10, Types.VARCHAR);
                    } else {
                        st.setString(10, flat.getFurnish().name());
                    }

                    if (flat.getTransport() == null) {
                        st.setNull(11, Types.VARCHAR);
                    } else {
                        st.setString(11, flat.getTransport().name());
                    }

                    st.setString(12, flat.getHouse().getName());
                    st.setLong(13, flat.getHouse().getYear());
                    st.setLong(14, flat.getHouse().getNumberOfFlatsOnFloor());
                    st.setInt(15, flat.getHouse().getNumberOfLifts());

                    st.addBatch();
                }

                st.executeBatch();
            }

            try (Statement sequenceStatement = connection.createStatement()) {
                sequenceStatement.execute("""
                    SELECT setval(
                        pg_get_serial_sequence('flats', 'id'),
                        COALESCE((SELECT MAX(id) FROM flats), 1),
                        (SELECT COUNT(*) FROM flats) > 0
                    )
                    """);
            }

            connection.commit();
            logger.info("Коллекция успешно синхронизирована с базой данных");
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }

            logger.warn("Не удалось сохранить коллекцию в базу данных: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(previousAutoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized List<Long> clearUserFlats(String username) {
        List<Long> removedIds = new ArrayList<>();

        if (!isConnected()) {
            logger.warn("Clear невозможен: нет подключения к базе данных");
            return removedIds;
        }

        if (username == null || username.isBlank()) {
            return removedIds;
        }

        String query = """
            DELETE FROM flats
            WHERE author_username = ?
            RETURNING id
            """;

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, username);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    removedIds.add(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return removedIds;
    }

    public synchronized List<Long> removeUserFlatsByIds(Collection<Long> ids, String username) {
        List<Long> removedIds = new ArrayList<>();

        if (!isConnected()) {
            logger.warn("Удаление невозможно: нет подключения к базе данных");
            return removedIds;
        }

        if (ids == null || ids.isEmpty()) {
            return removedIds;
        }

        if (username == null || username.isBlank()) {
            return removedIds;
        }

        String query = """
            DELETE FROM flats
            WHERE id = ? AND author_username = ?
            RETURNING id
            """;

        try {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement st = connection.prepareStatement(query)) {
                for (Long id : ids) {
                    st.setLong(1, id);
                    st.setString(2, username);

                    try (ResultSet rs = st.executeQuery()) {
                        if (rs.next()) {
                            removedIds.add(rs.getLong("id"));
                        }
                    }
                }
            }

            connection.commit();
            connection.setAutoCommit(previousAutoCommit);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
            recoverSchemaIfNeeded(e);
            e.printStackTrace();
        }

        return removedIds;
    }
}

