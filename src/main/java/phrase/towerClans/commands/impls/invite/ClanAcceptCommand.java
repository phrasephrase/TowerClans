package phrase.towerClans.commands.impls.invite;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import phrase.towerClans.Plugin;
import phrase.towerClans.clan.ClanResponse;
import phrase.towerClans.clan.ModifiedPlayer;
import phrase.towerClans.clan.impls.ClanImpl;
import phrase.towerClans.commands.CommandHandler;
import phrase.towerClans.utils.ChatUtil;

import java.util.UUID;

public class ClanAcceptCommand implements CommandHandler {

    private final Plugin plugin;
    private final ChatUtil chatUtil;

    public ClanAcceptCommand(Plugin plugin) {
        this.plugin = plugin;
        chatUtil = new ChatUtil(plugin);
    }

    @Override
    public boolean handler(Player player, String[] args) {

        ModifiedPlayer modifiedPlayer = ModifiedPlayer.get(player);

        ConfigurationSection configurationSection = plugin.getConfig().getConfigurationSection("message.command.invite.accept");

        UUID senderPlayer = PlayerCalls.removePlayers(player.getUniqueId());

        if (senderPlayer == null) {
            chatUtil.sendMessage(player, configurationSection.getString("has_anyone_sent_you_a_request_to_join_clan"));
            return true;
        }

        if (modifiedPlayer.getClan() != null) {
            chatUtil.sendMessage(player, configurationSection.getString("you_are_in_a_clan"));
            return true;
        }

        ModifiedPlayer senderModifiedPlayer = ModifiedPlayer.get(Bukkit.getPlayer(senderPlayer));
        ClanImpl clan = (ClanImpl) senderModifiedPlayer.getClan();

        modifiedPlayer.setClan(clan);
        ClanResponse clanResponse = clan.invite(modifiedPlayer);

        if (clanResponse.isSuccess()) {
            chatUtil.sendMessage(player, configurationSection.getString("have_you_accepted_the_request_to_join_the_clan"));
            chatUtil.sendMessage(Bukkit.getPlayer(senderPlayer), configurationSection.getString("the_player_accepted_the_request_to_join_the_clan"));
            return true;
        } else {
            if (clanResponse.getMessage() != null) {
                chatUtil.sendMessage(Bukkit.getPlayer(senderPlayer), clanResponse.getMessage());
            }
        }

        return true;
    }
}
