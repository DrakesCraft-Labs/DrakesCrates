package me.jackstar.drakescrates.oracle;

import org.bukkit.Material;
import java.util.List;

public record Relicary(String id, String name, String keyName, Material material, List<OracleReward> rewards) { }
