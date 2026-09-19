package me.jackstar.drakescrates.oracle;

import org.bukkit.Material;
import java.util.List;

public record OracleReward(String id, String name, String rarity, double weight, Material material, List<String> commands) {
    public boolean isTopTier() { return "LEGENDARIO".equalsIgnoreCase(rarity) || "DIVINO".equalsIgnoreCase(rarity); }
}
