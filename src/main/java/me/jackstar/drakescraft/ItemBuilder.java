package me.jackstar.drakescraft.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, Math.max(1, amount));
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item != null ? item.clone() : new ItemStack(Material.STONE);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder type(Material material) {
        if (material != null) {
            ItemStack newItem = new ItemStack(material, item != null ? item.getAmount() : 1);
            if (meta != null) {
                newItem.setItemMeta(meta);
            }
            this.item = newItem;
            this.meta = this.item.getItemMeta();
        }
        return this;
    }

    public ItemBuilder name(String name) {
        if (meta != null) {
            meta.displayName(MessageUtils.parse(name));
        }
        return this;
    }

    public ItemBuilder name(Component component) {
        if (meta != null && component != null) {
            meta.displayName(component);
        }
        return this;
    }

    public ItemBuilder lore(String... lore) {
        if (meta != null) {
            List<Component> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(MessageUtils.parse(line));
            }
            meta.lore(loreList);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        return lore(lore.toArray(new String[0]));
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder glowing() {
        return enchant(Enchantment.UNBREAKING, 1).flags(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder modelData(int modelData) {
        if (meta != null) {
            meta.setCustomModelData(modelData);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
