package me.jackstar.drakescrates.application.repositories;

import me.jackstar.drakescrates.domain.models.Crate;
import me.jackstar.drakescrates.domain.models.Key;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Optional;

public interface CrateRepository {

    void reload();

    Collection<Crate> getAllCrates();

    Collection<Key> getAllKeys();

    Optional<Crate> findCrateById(String crateId);

    Optional<Key> findKeyById(String keyId);

    Optional<Key> findRequiredKeyForCrate(String crateId);

    Optional<Crate> findCrateByLocation(Location location);

    boolean updateRewardChance(String crateId, String rewardId, double newChance);
}
