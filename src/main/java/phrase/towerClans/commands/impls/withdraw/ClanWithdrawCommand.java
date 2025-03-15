package phrase.towerClans.commands.impls.withdraw;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import phrase.towerClans.Plugin;
import phrase.towerClans.clan.AbstractClan;
import phrase.towerClans.clan.ModifiedPlayer;
import phrase.towerClans.clan.impls.ClanImpl;
import phrase.towerClans.commands.CommandHandler;
import phrase.towerClans.utils.ChatUtil;

public class ClanWithdrawCommand implements CommandHandler {

    @Override
    public boolean handler(Player player, String[] args) {
        ModifiedPlayer modifiedPlayer = ModifiedPlayer.get(player);

        ConfigurationSection configurationSection = Plugin.getInstance().getConfig().getConfigurationSection("message.command.withdraw");

        if (args.length < 2) {
            ChatUtil.getChatUtil().sendMessage(player, configurationSection.getString("usage_command"));
            return false;
        }

        if (modifiedPlayer.getClan() == null) {
            ChatUtil.getChatUtil().sendMessage(player, configurationSection.getString("you're_not_in_the_clan"));
            return true;
        }

        ClanImpl clan = (ClanImpl) modifiedPlayer.getClan();

        if (!clan.getMembers().get(modifiedPlayer).equals(AbstractClan.RankType.LEADER.getName()) && !clan.getMembers().get(modifiedPlayer).equals(AbstractClan.RankType.DEPUTY.getName())) {
            ChatUtil.getChatUtil().sendMessage(player, configurationSection.getString("you_don't_have_permission"));
            return true;
        }


        int amount = 0;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            ChatUtil.getChatUtil().sendMessage(player, configurationSection.getString("usage_command"));
            return false;
        }

        boolean b = clan.withdraw(modifiedPlayer, amount);
        if (b) {
            ChatUtil.getChatUtil().sendMessage(player, configurationSection.getString("you_have_withdrawn_from_the_clan"));
            return true;
        } else {
            ChatUtil.getChatUtil().sendMessage(player, configurationSection.getString("not_in_the_clan"));
        }

        return true;
    }
}
