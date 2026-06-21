package main.Server.Network;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveUserManager {
    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    public boolean login(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        return activeUsers.add(username);
    }

    public void logout(String username) {
        if (username != null) {
            activeUsers.remove(username);
        }
    }

    public boolean isActive(String username) {
        return username != null && activeUsers.contains(username);
    }
}
