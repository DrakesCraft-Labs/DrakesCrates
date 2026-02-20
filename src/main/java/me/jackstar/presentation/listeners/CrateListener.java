package me.jackstar.drakescrates.presentation.listeners;

import me.jackstar.drakescraft.utils.MessageUtils;
import me.jackstar.drakescrates.application.repositories.CrateRepository;
import me.jackstar.drakescrates.application.usecases.OpenCrateUseCase;
import me.jackstar.drakescrates.domain.models.Crate;
import me.jackstar.drakescrates.domain.models.CrateType;
import me.jackstar.drakescrates.domain.models.Key;
import me.jackstar.drakescrates.domain.models.OpenResult;
import me.jackstar.drakescrates.presentation.animation.CrateAnimation;
import me.jackstar.drakescrates.presentation.editor.CratePreviewManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CrateListener implements Listener {

    private final CrateRepository crateRepository;
    private final OpenCrateUseCase openCrateUseCase;
    private final CrateAnimation crateAnimation;
    private final CratePreviewManager cratePreviewManager;

    public CrateListener(CrateRepository crateRepository, OpenCrateUseCase openCrateUseCase,
            CrateAnimation crateAnimation, CratePreviewManager cratePreviewManager) {
        this.crateRepository = crateRepository;
        this.openCrateUseCase = openCrateUseCase;
        this.crateAnimation = crateAnimation;
        this.cratePreviewManager = cratePreviewManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if ((event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
                || event.getClickedBlock() == null) {
            return;
        }

        final Player player = event.getPlayer();
        final Location clickedLocation = event.getClickedBlock().getLocation();
        final Optional<Crate> crateOptional = crateRepository.findCrateByLocation(clickedLocation);
        if (crateOptional.isEmpty()) {
            return;
        }

        event.setCancelled(true);

        Crate crate = crateOptional.get();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            cratePreviewManager.openPreview(player, crate);
            return;
        }

        ItemStack keyForValidation = null;

        if (crate.getType() == CrateType.PHYSICAL_KEY) {
            Optional<Key> requiredKey = crateRepository.findRequiredKeyForCrate(crate.getId());
            if (requiredKey.isEmpty()) {
                MessageUtils.send(player, "<red>This crate has an invalid key setup.</red>");
                return;
            }

            keyForValidation = findMatchingKeyInInventory(player, requiredKey.get());
            if (keyForValidation == null) {
                MessageUtils.send(player, "<red>You need the correct key to open this crate.</red>");
                return;
            }
        }

        OpenResult result = openCrateUseCase.execute(player, crate, keyForValidation);
        if (!result.isSuccess()) {
            MessageUtils.send(player, "<red>" + result.getMessage() + "</red>");
            return;
        }

        if (crate.getType() == CrateType.PHYSICAL_KEY && keyForValidation != null) {
            consumeOneKey(keyForValidation);
        }

        crateAnimation.start(player, crate, result.getWinningReward());
    }

    private ItemStack findMatchingKeyInInventory(Player player, Key key) {
        ItemStack target = key.getItem();
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getAmount() < 1) {
                continue;
            }
            if (stack.isSimilar(target)) {
                return stack;
            }
        }
        return null;
    }

    private void consumeOneKey(ItemStack stack) {
        int amount = stack.getAmount();
        if (amount <= 1) {
            stack.setAmount(0);
            return;
        }
        stack.setAmount(amount - 1);
    }
}
