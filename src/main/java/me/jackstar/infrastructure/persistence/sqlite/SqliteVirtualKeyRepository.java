package me.jackstar.drakescrates.infrastructure.persistence.sqlite;

import me.jackstar.drakescrates.application.repositories.VirtualKeyRepository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SqliteVirtualKeyRepository implements VirtualKeyRepository {

    private static final Logger LOGGER = Logger.getLogger("DrakesCrates-VirtualKeys");
    private final String url;

    public SqliteVirtualKeyRepository(File databaseFile) {
        this.url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS drakes_virtual_keys ("
                    + "uuid TEXT NOT NULL, "
                    + "key_id TEXT NOT NULL, "
                    + "balance INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY(uuid, key_id)"
                    + ");");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not initialize virtual keys table in SQLite database", e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public int getBalance(UUID playerId, String keyId) {
        if (playerId == null || keyId == null) return 0;
        String normalizedKey = keyId.trim().toLowerCase(Locale.ROOT);
        String sql = "SELECT balance FROM drakes_virtual_keys WHERE uuid = ? AND key_id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.setString(2, normalizedKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Math.max(0, rs.getInt("balance"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to get virtual key balance for " + playerId + " / " + keyId, e);
        }
        return 0;
    }

    @Override
    public void addKeys(UUID playerId, String keyId, int amount) {
        if (playerId == null || keyId == null || amount <= 0) return;
        String normalizedKey = keyId.trim().toLowerCase(Locale.ROOT);
        String sql = "INSERT INTO drakes_virtual_keys (uuid, key_id, balance) VALUES (?, ?, ?) "
                + "ON CONFLICT(uuid, key_id) DO UPDATE SET balance = balance + excluded.balance";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.setString(2, normalizedKey);
            ps.setInt(3, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to add virtual keys for " + playerId + " / " + keyId, e);
        }
    }

    @Override
    public boolean takeKeys(UUID playerId, String keyId, int amount) {
        if (playerId == null || keyId == null || amount <= 0) return false;
        String normalizedKey = keyId.trim().toLowerCase(Locale.ROOT);
        String sql = "UPDATE drakes_virtual_keys SET balance = balance - ? WHERE uuid = ? AND key_id = ? AND balance >= ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setString(2, playerId.toString());
            ps.setString(3, normalizedKey);
            ps.setInt(4, amount);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to take virtual keys for " + playerId + " / " + keyId, e);
        }
        return false;
    }

    @Override
    public void setKeys(UUID playerId, String keyId, int amount) {
        if (playerId == null || keyId == null) return;
        String normalizedKey = keyId.trim().toLowerCase(Locale.ROOT);
        int finalAmount = Math.max(0, amount);
        String sql = "INSERT INTO drakes_virtual_keys (uuid, key_id, balance) VALUES (?, ?, ?) "
                + "ON CONFLICT(uuid, key_id) DO UPDATE SET balance = excluded.balance";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.setString(2, normalizedKey);
            ps.setInt(3, finalAmount);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to set virtual keys for " + playerId + " / " + keyId, e);
        }
    }

    @Override
    public Map<String, Integer> getAllBalances(UUID playerId) {
        Map<String, Integer> map = new HashMap<>();
        if (playerId == null) return map;
        String sql = "SELECT key_id, balance FROM drakes_virtual_keys WHERE uuid = ? AND balance > 0";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("key_id"), rs.getInt("balance"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to list all virtual keys for " + playerId, e);
        }
        return map;
    }

    @Override
    public void close() {
        // SQLite file connection handles pooling automatically
    }
}
