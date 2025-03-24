package phrase.towerClans.clan;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import phrase.towerClans.clan.impls.ClanImpl;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ModifiedPlayer {

    private UUID playerUUID;
    private Clan clan;

    public ModifiedPlayer(UUID playerUUID, Clan clan) {
        this.playerUUID = playerUUID;
        this.clan = clan;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    public static ModifiedPlayer get(Player player) {


            for(Map.Entry<String, ClanImpl> clan : ClanImpl.getClans().entrySet()) {

                for(Map.Entry<ModifiedPlayer, String> entry : clan.getValue().getMembers().entrySet()) {

                    if(!entry.getKey().getPlayer().equals(player)) continue;

                    return new ModifiedPlayer(player.getUniqueId(), clan.getValue());

                }

            }


        return new ModifiedPlayer(player.getUniqueId(), null);

    }

    public Clan getClan() {
        return clan;
    }

    public void setClan(Clan clan) {
        this.clan = clan;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModifiedPlayer that = (ModifiedPlayer) o;
        return Objects.equals(playerUUID, that.playerUUID) && Objects.equals(clan, that.clan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUUID, clan);
    }
}
