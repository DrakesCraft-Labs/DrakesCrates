package me.jackstar.drakescrates;

import me.jackstar.drakescrates.application.repositories.VirtualKeyRepository;
import me.jackstar.drakescrates.domain.models.Crate;
import me.jackstar.drakescrates.domain.models.CrateModality;
import me.jackstar.drakescrates.domain.models.CrateType;
import me.jackstar.drakescrates.domain.models.Reward;
import me.jackstar.drakescrates.infrastructure.persistence.sqlite.SqliteVirtualKeyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ModalityAndVirtualCrateTest {

    private File tempDb;
    private VirtualKeyRepository repo;

    @BeforeEach
    public void setup() throws IOException {
        tempDb = File.createTempFile("test_virtual_keys_", ".db");
        repo = new SqliteVirtualKeyRepository(tempDb);
    }

    @AfterEach
    public void teardown() {
        if (repo != null) repo.close();
        if (tempDb != null && tempDb.exists()) tempDb.delete();
    }

    @Test
    public void testVirtualKeyOperations() {
        UUID player = UUID.randomUUID();
        assertEquals(0, repo.getBalance(player, "basic_key"));

        repo.addKeys(player, "basic_key", 5);
        assertEquals(5, repo.getBalance(player, "basic_key"));

        assertTrue(repo.takeKeys(player, "basic_key", 2));
        assertEquals(3, repo.getBalance(player, "basic_key"));

        assertFalse(repo.takeKeys(player, "basic_key", 10), "Cannot take more than balance");
        assertEquals(3, repo.getBalance(player, "basic_key"));

        repo.setKeys(player, "basic_key", 20);
        assertEquals(20, repo.getBalance(player, "basic_key"));
    }

    @Test
    public void testCrateModalityDetection() {
        // Since Paper 1.21.x, new ItemStack(Material) needs the server item registry, which unit
        // tests don't have. Modality detection relies on slimefun-id and commands, not the item.
        Reward vanillaReward = new Reward("vanilla_iron", null, 50.0, List.of(), null);
        assertFalse(vanillaReward.isSlimefun(), "Vanilla reward should not be detected as Slimefun");

        Reward sfReward = new Reward("sf_panel", null, 50.0, List.of(), null, "SOLAR_GENERATOR", false, 1);
        assertTrue(sfReward.isSlimefun(), "Reward with slimefun-id must be detected as Slimefun");

        Crate clasicoCrate = new Crate("clasico_test", null, CrateType.PHYSICAL_KEY, List.of(vanillaReward), null, List.of(), CrateModality.CLASICO);
        assertEquals(CrateModality.CLASICO, clasicoCrate.getModality());
        assertFalse(clasicoCrate.hasSlimefunRewards());

        Crate sfCrate = new Crate("sf_test", null, CrateType.PHYSICAL_KEY, List.of(sfReward), null, List.of(), CrateModality.SLIMEFUN);
        assertEquals(CrateModality.SLIMEFUN, sfCrate.getModality());
        assertTrue(sfCrate.hasSlimefunRewards());
    }

    @Test
    public void testCrateModalityParsing() {
        assertEquals(CrateModality.CLASICO, CrateModality.fromString("clasico"));
        assertEquals(CrateModality.CLASICO, CrateModality.fromString("VANILLA"));
        assertEquals(CrateModality.CLASICO, CrateModality.fromString("Vainilla"));
        assertEquals(CrateModality.SLIMEFUN, CrateModality.fromString("slimefun"));
        assertEquals(CrateModality.SLIMEFUN, CrateModality.fromString("SF"));
        assertEquals(CrateModality.ALL, CrateModality.fromString("global"));
        assertEquals(CrateModality.ALL, CrateModality.fromString(null));
    }
}
