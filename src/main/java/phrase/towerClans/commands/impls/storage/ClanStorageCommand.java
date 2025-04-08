package phrase.towerClans.commands.impls.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import phrase.towerClans.Plugin;
import phrase.towerClans.clan.ModifiedPlayer;
import phrase.towerClans.clan.impls.ClanImpl;
import phrase.towerClans.commands.CommandHandler;
import phrase.towerClans.utils.ChatUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClanStorageCommand implements CommandHandler {

    public static final Set<UUID> PLAYERS = new HashSet<>();

    private Plugin plugin;
    private ChatUtil chatUtil;

    public ClanStorageCommand(Plugin plugin) {
        this.plugin = plugin;
        chatUtil = new ChatUtil(plugin);
    }

    @Override
    public boolean handler(Player player, String[] args) {

        ConfigurationSection configurationSection = plugin.getConfig().getConfigurationSection("message.command.storage");
        ModifiedPlayer modifiedPlayer = ModifiedPlayer.get(player);

        ClanImpl clan = (ClanImpl) modifiedPlayer.getClan();

        if(clan == null) {
            chatUtil.sendMessage(player, configurationSection.getString("you're_not_in_the_clan"));
            return true;
        }

        clan.showMenu(modifiedPlayer, ClanImpl.MenuType.MENU_CLAN_STORAGE.getId());
        chatUtil.sendMessage(player, configurationSection.getString("you_have_opened_the_clan's_storage"));
        PLAYERS.add(player.getUniqueId());
        return true;
    }
}
