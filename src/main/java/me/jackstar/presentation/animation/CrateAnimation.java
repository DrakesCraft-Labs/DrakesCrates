package me.jackstar.drakescrates.presentation.animation;

import me.jackstar.drakescrates.domain.models.Crate;
import me.jackstar.drakescrates.domain.models.Reward;
import org.bukkit.entity.Player;

public interface CrateAnimation {

    void start(Player player, Crate crate, Reward winReward);
}
