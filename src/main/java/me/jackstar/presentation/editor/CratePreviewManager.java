package me.jackstar.drakescrates.presentation.editor;

import me.jackstar.drakescraft.utils.MessageUtils;
import me.jackstar.drakescrates.domain.models.Crate;
import me.jackstar.drakescrates.domain.models.Reward;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CratePreviewManager implements Listener {

    private final PreviewHolder previewHolder = new PreviewHolder();

    public void openPreview(Player player, Crate crate) {
        if (player == null || crate == null) {
            return;
        }

        List<Reward> rewards = crate.getRewards();
        int size = Math.max(9, ((rewards.size() - 1) / 9 + 1) * 9);
        Inventory inventory = Bukkit.createInventory(
                previewHolder,
                size,
                MessageUtils.parse("<gold>Preview: </gold><yellow>" + crate.getId()));

        for (int i = 0; i < rewards.size() && i < size; i++) {
            Reward reward = rewards.get(i);
            ItemStack display = reward.getDisplayItem().clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<net.kyori.adventure.text.Component> lore = meta.lore();
                if (lore == null) {
                    lore = new ArrayList<>();
                } else {
                    lore = new ArrayList<>(lore);
                }
                lore.add(MessageUtils.parse("<dark_gray> "));
                lore.add(MessageUtils.parse("<gray>Chance: <gold>" + reward.getChance() + "%</gold>"));
                meta.lore(lore);

                String plainName = PlainTextComponentSerializer.plainText().serialize(reward.getDisplayName());
                if (plainName != null && !plainName.isBlank()) {
                    meta.displayName(reward.getDisplayName());
                }
                display.setItemMeta(meta);
            }
            inventory.setItem(i, display);
        }
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof PreviewHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof PreviewHolder) {
            event.setCancelled(true);
        }
    }

    private static final class PreviewHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
