package me.jackstar.drakescrates.presentation.commands;

import me.jackstar.drakescraft.utils.MessageUtils;
import me.jackstar.drakescrates.application.repositories.CrateRepository;
import me.jackstar.drakescrates.application.repositories.VirtualKeyRepository;
import me.jackstar.drakescrates.domain.models.Key;
import me.jackstar.drakescrates.presentation.editor.CrateEditorManager;
import me.jackstar.drakescrates.presentation.gui.VirtualCrateMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DrakesCratesCommand implements CommandExecutor {

    private final CrateRepository crateRepository;
    private final VirtualKeyRepository virtualKeyRepository;
    private final CrateEditorManager crateEditorManager;
    private final VirtualCrateMenu virtualCrateMenu;
    private final Runnable reloadAction;

    public DrakesCratesCommand(CrateRepository crateRepository, VirtualKeyRepository virtualKeyRepository,
                               CrateEditorManager crateEditorManager, VirtualCrateMenu virtualCrateMenu,
                               Runnable reloadAction) {
        this.crateRepository = crateRepository;
        this.virtualKeyRepository = virtualKeyRepository;
        this.crateEditorManager = crateEditorManager;
        this.virtualCrateMenu = virtualCrateMenu;
        this.reloadAction = reloadAction;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (!sender.hasPermission("drakescrates.admin")) {
            // If normal player types /crates or /dc without args, open virtual menu!
            if (sender instanceof Player player) {
                if (virtualCrateMenu != null) {
                    virtualCrateMenu.openMenu(player);
                    return true;
                }
            }
            MessageUtils.send(sender, "<red>No tienes permisos para usar este comando.</red>");
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player player && virtualCrateMenu != null) {
                virtualCrateMenu.openMenu(player);
                return true;
            }
            sendUsage(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();

        if ("menu".equals(sub)) {
            if (!(sender instanceof Player player)) {
                MessageUtils.send(sender, "<red>Solo jugadores pueden abrir el menú.</red>");
                return true;
            }
            if (virtualCrateMenu != null) virtualCrateMenu.openMenu(player);
            return true;
        }

        if ("reload".equals(sub)) {
            if (reloadAction != null) {
                reloadAction.run();
            } else {
                crateRepository.reload();
            }
            MessageUtils.send(sender, "<green>DrakesCrates recargado correctamente.</green>");
            return true;
        }

        if ("givekey".equals(sub)) {
            return handleGiveKey(sender, label, args);
        }

        if ("givevirtualkey".equals(sub) || "givevk".equals(sub)) {
            return handleGiveVirtualKey(sender, label, args);
        }

        if ("takekey".equals(sub)) {
            return handleTakeKey(sender, label, args);
        }

        if ("balance".equals(sub) || "keys".equals(sub)) {
            return handleBalance(sender, label, args);
        }

        if ("editor".equals(sub)) {
            if (!(sender instanceof Player player)) {
                MessageUtils.send(sender, "<red>Solo jugadores pueden abrir el editor.</red>");
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
            MessageUtils.send(sender, "<red>Uso: /" + label + " givekey <jugador> <key_id> <cantidad> [fisica|virtual]</red>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            MessageUtils.send(sender, "<red>Jugador no encontrado o desconectado.</red>");
            return true;
        }

        String keyId = args[2].toLowerCase();
        Key key = crateRepository.findKeyById(keyId).orElse(null);
        if (key == null) {
            MessageUtils.send(sender, "<red>ID de llave desconocido: <gray>" + keyId + "</gray></red>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            MessageUtils.send(sender, "<red>La cantidad debe ser un número entero válido.</red>");
            return true;
        }
        if (amount < 1) {
            MessageUtils.send(sender, "<red>La cantidad debe ser al menos 1.</red>");
            return true;
        }

        boolean isVirtual = args.length > 4 && "virtual".equalsIgnoreCase(args[4]);

        if (isVirtual) {
            virtualKeyRepository.addKeys(target.getUniqueId(), keyId, amount);
            MessageUtils.send(sender, "<green>Entregadas <yellow>" + amount + "</yellow> llave(s) virtuales <gray>(" + keyId + ")</gray> a <aqua>" + target.getName() + "</aqua>.</green>");
            MessageUtils.send(target, "<green>Has recibido <yellow>" + amount + "</yellow> llave(s) virtuales <gray>(" + keyId + ")</gray>.</green>");
            return true;
        }

        ItemStack keyItem = key.getItem().clone();
        keyItem.setAmount(Math.min(amount, keyItem.getMaxStackSize()));
        int left = giveStacked(target, keyItem, amount);

        int delivered = amount - left;
        MessageUtils.send(sender, "<green>Entregadas <yellow>" + delivered + "</yellow> llave(s) físicas <gray>(" + keyId + ")</gray> a <aqua>" + target.getName() + "</aqua>.</green>");

        if (left > 0) {
            MessageUtils.send(sender, "<red>" + left + " llave(s) no cupieron en el inventario y se añadieron como virtuales.</red>");
            virtualKeyRepository.addKeys(target.getUniqueId(), keyId, left);
            MessageUtils.send(target, "<yellow>" + left + " llave(s) fueron convertidas a virtuales porque tu inventario estaba lleno.</yellow>");
        } else {
            MessageUtils.send(target, "<green>Has recibido <yellow>" + delivered + "</yellow> llave(s) físicas <gray>(" + keyId + ")</gray>.</green>");
        }
        return true;
    }

    private boolean handleGiveVirtualKey(CommandSender sender, String label, String[] args) {
        if (args.length < 4) {
            MessageUtils.send(sender, "<red>Uso: /" + label + " givevirtualkey <jugador> <key_id> <cantidad></red>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            MessageUtils.send(sender, "<red>Jugador no encontrado o desconectado.</red>");
            return true;
        }

        String keyId = args[2].toLowerCase();
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            MessageUtils.send(sender, "<red>Cantidad inválida.</red>");
            return true;
        }
        if (amount < 1) return true;

        virtualKeyRepository.addKeys(target.getUniqueId(), keyId, amount);
        MessageUtils.send(sender, "<green>Añadidas <yellow>" + amount + "</yellow> llaves virtuales <gray>(" + keyId + ")</gray> a <aqua>" + target.getName() + "</aqua>.</green>");
        MessageUtils.send(target, "<green>Has recibido <yellow>" + amount + "</yellow> llaves virtuales <gray>(" + keyId + ")</gray>.</green>");
        return true;
    }

    private boolean handleTakeKey(CommandSender sender, String label, String[] args) {
        if (args.length < 4) {
            MessageUtils.send(sender, "<red>Uso: /" + label + " takekey <jugador> <key_id> <cantidad></red>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            MessageUtils.send(sender, "<red>Jugador no encontrado.</red>");
            return true;
        }

        String keyId = args[2].toLowerCase();
        int amount = Integer.parseInt(args[3]);

        if (virtualKeyRepository.takeKeys(target.getUniqueId(), keyId, amount)) {
            MessageUtils.send(sender, "<green>Se retiraron <yellow>" + amount + "</yellow> llaves virtuales de " + target.getName() + ".</green>");
        } else {
            MessageUtils.send(sender, "<red>El jugador no tiene suficientes llaves virtuales para retirar.</red>");
        }
        return true;
    }

    private boolean handleBalance(CommandSender sender, String label, String[] args) {
        Player target = sender instanceof Player p ? p : null;
        if (args.length > 1) {
            target = Bukkit.getPlayerExact(args[1]);
        }
        if (target == null) {
            MessageUtils.send(sender, "<red>Especifica un jugador válido.</red>");
            return true;
        }

        Map<String, Integer> balances = virtualKeyRepository.getAllBalances(target.getUniqueId());
        MessageUtils.send(sender, "<gold>✦ <b>Llaves Virtuales de " + target.getName() + ":</b></gold>");
        if (balances.isEmpty()) {
            MessageUtils.send(sender, "<gray>No tiene llaves virtuales registradas.</gray>");
        } else {
            balances.forEach((id, bal) -> MessageUtils.send(sender, "<gray>• <yellow>" + id + "</yellow>: <aqua>" + bal + "</aqua></gray>"));
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
        MessageUtils.send(sender, "<yellow><b>DrakesCrates Comandos:</b></yellow>");
        MessageUtils.send(sender, "<gray>/" + label + " menu - Abre el menú de crates virtuales</gray>");
        MessageUtils.send(sender, "<gray>/" + label + " givekey <jugador> <key_id> <cantidad> [fisica|virtual]</gray>");
        MessageUtils.send(sender, "<gray>/" + label + " givevirtualkey <jugador> <key_id> <cantidad></gray>");
        MessageUtils.send(sender, "<gray>/" + label + " takekey <jugador> <key_id> <cantidad></gray>");
        MessageUtils.send(sender, "<gray>/" + label + " balance [jugador]</gray>");
        MessageUtils.send(sender, "<gray>/" + label + " editor [crate_id]</gray>");
        MessageUtils.send(sender, "<gray>/" + label + " reload</gray>");
    }
}
