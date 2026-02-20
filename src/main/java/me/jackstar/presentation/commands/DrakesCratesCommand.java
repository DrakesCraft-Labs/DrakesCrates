package me.jackstar.drakescrates.presentation.commands;

import me.jackstar.drakescraft.utils.MessageUtils;
import me.jackstar.drakescrates.application.repositories.CrateRepository;
import me.jackstar.drakescrates.domain.models.Key;
import me.jackstar.drakescrates.presentation.editor.CrateEditorManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DrakesCratesCommand implements CommandExecutor {

    private final CrateRepository crateRepository;
    private final CrateEditorManager crateEditorManager;

    public DrakesCratesCommand(CrateRepository crateRepository, CrateEditorManager crateEditorManager) {
        this.crateRepository = crateRepository;
        this.crateEditorManager = crateEditorManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission("drakescrates.admin")) {
            MessageUtils.send(sender, "<red>You do not have permission to use this command.</red>");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            crateRepository.reload();
            MessageUtils.send(sender, "<green>DrakesCrates reloaded successfully.</green>");
            return true;
        }

        if ("givekey".equalsIgnoreCase(args[0])) {
            return handleGiveKey(sender, label, args);
        }
        if ("editor".equalsIgnoreCase(args[0])) {
            if (!(sender instanceof Player player)) {
                MessageUtils.send(sender, "<red>Only players can open the editor.</red>");
                return true;
            }
            crateEditorManager.openEditor(player, args.length > 1 ? args[1] : null);
            return true;
        }

        sendUsage(sender, label);
        return true;
    }

    private boolean handleGiveKey(CommandSender sender, String label, String[] args) {
        if (args.length < 4) {
            MessageUtils.send(sender, "<red>Usage: /" + label + " givekey <player> <key_id> <amount></red>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            MessageUtils.send(sender, "<red>Player not found or offline.</red>");
            return true;
        }

        String keyId = args[2];
        Key key = crateRepository.findKeyById(keyId).orElse(null);
        if (key == null) {
            MessageUtils.send(sender, "<red>Unknown key id: <gray>" + keyId + "</gray></red>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            MessageUtils.send(sender, "<red>Amount must be a valid integer.</red>");
            return true;
        }
        if (amount < 1) {
            MessageUtils.send(sender, "<red>Amount must be at least 1.</red>");
            return true;
        }

        ItemStack keyItem = key.getItem();
        keyItem.setAmount(Math.min(amount, keyItem.getMaxStackSize()));
        int left = giveStacked(target, keyItem, amount);

        int delivered = amount - left;
        MessageUtils.send(sender, "<green>Given <yellow>" + delivered + "</yellow> key(s) <gray>(" + keyId
                + ")</gray> to <aqua>" + target.getName() + "</aqua>.</green>");

        if (left > 0) {
            MessageUtils.send(sender, "<red>" + left + " key(s) could not fit in inventory.</red>");
            MessageUtils.send(target, "<red>Your inventory was full. " + left + " key(s) were not delivered.</red>");
        } else {
            MessageUtils.send(target,
                    "<green>You received <yellow>" + delivered + "</yellow> key(s) <gray>(" + keyId + ")</gray>.</green>");
        }
        return true;
    }

    private int giveStacked(Player target, ItemStack baseItem, int totalAmount) {
        int left = totalAmount;
        int max = Math.max(1, baseItem.getMaxStackSize());

        while (left > 0) {
            int batch = Math.min(left, max);
            ItemStack stack = baseItem.clone();
            stack.setAmount(batch);
            var notFit = target.getInventory().addItem(stack);
            if (!notFit.isEmpty()) {
                int remainderAmount = 0;
                for (ItemStack remainder : notFit.values()) {
                    remainderAmount += remainder.getAmount();
                }
                int delivered = batch - remainderAmount;
                left -= delivered;
                return left;
            }
            left -= batch;
        }
        return 0;
    }

    private void sendUsage(CommandSender sender, String label) {
        MessageUtils.send(sender, "<yellow>DrakesCrates commands:</yellow>");
        MessageUtils.send(sender, "<gray>/" + label + " givekey <player> <key_id> <amount></gray>");
        MessageUtils.send(sender, "<gray>/" + label + " editor [crate_id]</gray>");
        MessageUtils.send(sender, "<gray>/" + label + " reload</gray>");
    }
}
