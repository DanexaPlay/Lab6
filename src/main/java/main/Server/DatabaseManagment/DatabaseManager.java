package main.Server.DatabaseManagment;
import main.Common.Encryption;
import main.Common.LoginData;
import main.Common.RegisterData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Random;

public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    private final String url = "jdbc:postgresql://localhost:15432/studs";
    private Connection connection;

    public DatabaseManager() {
        try {
            Properties info = new Properties();
            info.load(new FileInputStream("src/main/java/main/Server/db.cfg"));
            connection = DriverManager.getConnection(url, info);
            logger.info("База данных подключена");
            System.out.println("Успешное подключение к базе данных");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Не удалось подключиться к базе данных!");
        } catch (IOException e2) {
            System.out.println("Файл конфигурации базы данных не найден!");
        }
    }

    public boolean checkUsernameAvailability(String username) {
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

    public boolean checkPassword(String username, String password) throws SQLException {
        String query = "SELECT username, hash_password FROM users WHERE username = ?";
        PreparedStatement st = connection.prepareStatement(query);
        st.setString(1, username);
        ResultSet resultSet = st.executeQuery();
        resultSet.next();
        return resultSet.getString(1).equals(username) && resultSet.getString(2).equals(password);
    }

    public void register(RegisterData registerData) throws SQLException, UserAlreadyExistsException {
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
    }

    public void login(LoginData loginData) throws SQLException {
        String username = loginData.getUsername();
        String password = loginData.getPassword();
        if (checkUsernameAvailability(username)) {
            throw new UserNotExistsException(username);
        }
        String query = "SELECT salt FROM users WHERE username = ?";
        PreparedStatement st = connection.prepareStatement(query);
        st.setString(1, username);
        ResultSet resultSet = st.executeQuery();
        resultSet.next();
        String salt = resultSet.getString(1);
        password += salt;
        password = Encryption.encryptString(password);
        if (!checkPassword(username, password)) {
            throw new InvalidCredentialsException();
        }
    }
}
