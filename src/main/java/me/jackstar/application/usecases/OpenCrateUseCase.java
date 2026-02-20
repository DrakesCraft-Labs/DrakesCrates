package me.jackstar.drakescrates.application.usecases;

import me.jackstar.drakescrates.domain.models.Crate;
import me.jackstar.drakescrates.domain.models.CrateType;
import me.jackstar.drakescrates.domain.models.OpenResult;
import me.jackstar.drakescrates.domain.models.Reward;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class OpenCrateUseCase {

    private final Random random = new Random();

    public OpenResult execute(Player player, Crate crate, ItemStack keyItem) {
        // 1. Validate Key if Physical
        if (crate.getType() == CrateType.PHYSICAL_KEY) {
            if (keyItem == null || keyItem.getAmount() < 1) {
                return OpenResult.failure("You need a key to open this crate!");
            }
            // Basic check, in reality we'd check NBT/PersistentDataContainer
            // For now, let's assume the passed keyItem IS the valid key if the caller says
            // so
            // In a real implementation, we would compare usage against a KeyRepository
        }

        // 2. Select Reward (Weighted Random)
        Reward reward = selectReward(crate.getRewards());
        if (reward == null) {
            return OpenResult.failure("No reward selected (Configuration error?)");
        }

        // 3. Return Success (Caller handles giving item/commands)
        return OpenResult.success(reward);
    }

    private Reward selectReward(List<Reward> rewards) {
        if (rewards == null || rewards.isEmpty())
            return null;

        double totalWeight = 0.0;
        for (Reward r : rewards) {
            totalWeight += r.getChance();
        }

        double randomValue = random.nextDouble() * totalWeight;
        double currentWeight = 0.0;

        for (Reward r : rewards) {
            currentWeight += r.getChance();
            if (randomValue <= currentWeight) {
                return r;
            }
        }
        return rewards.get(0); // Fallback
    }
}
