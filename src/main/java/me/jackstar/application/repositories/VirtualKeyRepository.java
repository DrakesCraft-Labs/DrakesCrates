package me.jackstar.drakescrates.application.repositories;

import java.util.Map;
import java.util.UUID;

public interface VirtualKeyRepository {
    int getBalance(UUID playerId, String keyId);
    void addKeys(UUID playerId, String keyId, int amount);
    boolean takeKeys(UUID playerId, String keyId, int amount);
    void setKeys(UUID playerId, String keyId, int amount);
    Map<String, Integer> getAllBalances(UUID playerId);
    void close();
}
