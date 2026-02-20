package me.jackstar.drakescrates.domain.models;

public enum CrateType {
    PHYSICAL_KEY, // Requires a physical key item
    VIRTUAL_KEY, // Requires a virtual key currency
    FREE // No key required (e.g., daily reward)
}
