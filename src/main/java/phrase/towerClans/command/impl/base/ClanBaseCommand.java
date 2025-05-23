package phrase.towerClans.command.impl.base;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import phrase.towerClans.Plugin;
import phrase.towerClans.clan.entity.ModifiedPlayer;
import phrase.towerClans.clan.impl.ClanImpl;
import phrase.towerClans.command.CommandHandler;
import phrase.towerClans.util.ChatUtil;


public class ClanBaseCommand implements CommandHandler {

    private final Plugin plugin;
    private final ChatUtil chatUtil;

    public ClanBaseCommand(Plugin plugin) {
        this.plugin = plugin;
        chatUtil = new ChatUtil(plugin);
    }

    @Override
    public boolean handler(Player player, String[] args) {

        ModifiedPlayer modifiedPlayer = ModifiedPlayer.get(player);
        ClanImpl clan = (ClanImpl) modifiedPlayer.getClan();

        ConfigurationSection configurationSection = plugin.getConfig().getConfigurationSection("message.command.base");
        if(clan == null) {
            chatUtil.sendMessage(player, configurationSection.getString("you're_not_in_the_clan"));
            return true;
        }

        Location location = Base.getBase(clan);

        if(location == null) {
            chatUtil.sendMessage(player, configurationSection.getString("the_clan_doesn't_have_a_base"));
            return true;
        }

        player.teleport(location);
        chatUtil.sendMessage(player, configurationSection.getString("you_have_been_teleported_to_the_clan_base"));
        return true;
    }

}
