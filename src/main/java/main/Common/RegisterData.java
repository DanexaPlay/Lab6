package main.Common;

import java.io.Serializable;

public class RegisterData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;

    public RegisterData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

}
