package me.jackstar.drakescrates.oracle;

import org.bukkit.Bukkit; import org.bukkit.entity.Player; import java.util.*;

public final class OracleService {
    private final OracleRepository database; private final OracleYamlRepository relicaries; private final Random random = new Random();
    public OracleService(OracleRepository database, OracleYamlRepository relicaries) { this.database=database;this.relicaries=relicaries; }
    public int balance(UUID player,String relicary){return database.balance(player,relicary);} public int pity(UUID player,String relicary){return database.pity(player,relicary);}
    public void give(UUID id,String relicary,int count){validate(relicary);database.addKeys(id,relicary,count);} public boolean take(UUID id,String relicary,int count){validate(relicary);return database.takeKeys(id,relicary,count);} public void setPity(UUID id,String relicary,int count){validate(relicary);database.setPity(id,relicary,count);}
    public OracleRepository.Draw open(UUID id,String relicary){Relicary r=relicaries.get(relicary);if(r==null)throw new IllegalArgumentException("Relicario desconocido");return database.draw(id,r,random);}
    public void deliver(Player player,long claim){for(String command:database.claim(player.getUniqueId(),claim)){if(!command.isBlank()) Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command.replace("%player%",player.getName()));}}
    public int claimAll(Player player){List<List<String>> pending=database.claimAll(player.getUniqueId());for(List<String> commands:pending)for(String command:commands)if(!command.isBlank())Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command.replace("%player%",player.getName()));return pending.size();}
    private void validate(String id){if(relicaries.get(id)==null)throw new IllegalArgumentException("Relicario desconocido: "+id);}
}
