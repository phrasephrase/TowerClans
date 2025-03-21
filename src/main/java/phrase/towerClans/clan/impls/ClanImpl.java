package phrase.towerClans.clan.impls;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import phrase.towerClans.Plugin;
import phrase.towerClans.clan.AbstractClan;
import phrase.towerClans.clan.ClanResponse;
import phrase.towerClans.clan.ModifiedPlayer;
import phrase.towerClans.utils.ChatUtil;
import phrase.towerClans.utils.HexUtil;
import phrase.towerClans.utils.ItemBuilder;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ClanImpl extends AbstractClan {

    private static final Map<String, ClanImpl> CLANS = new HashMap<>();
    private ChatUtil chatUtil = new ChatUtil();

    public ClanImpl() {
    }


    public ClanImpl(String name) {
        super(name);
    }

    @Override
    public ClanResponse invite(ModifiedPlayer modifiedPlayer) {
        ConfigurationSection configurationSection = Plugin.getInstance().getConfig().getConfigurationSection("message.command.accept");
        ClanImpl clan = (ClanImpl) modifiedPlayer.getClan();
        if (clan.getMembers().containsKey(modifiedPlayer)) return new ClanResponse(configurationSection.getString("accept.you_are_in_a_clan"), ClanResponse.ResponseType.FAILURE);
        configurationSection = Plugin.getInstance().getConfig().getConfigurationSection("message.command.invite");
        clan.getMembers().put(modifiedPlayer, RankType.MEMBER.getName());

        for (Map.Entry<ModifiedPlayer, String> entry : clan.getMembers().entrySet()) {
            String string = configurationSection.getString("notification_of_the_invitation").replace("%player%", modifiedPlayer.getPlayer().getName());
            chatUtil.sendMessage(entry.getKey().getPlayer(), string);
        }

        return new ClanResponse(null, ClanResponse.ResponseType.SUCCESS);
    }

    @Override
    public ClanResponse kick(ModifiedPlayer modifiedPlayer) {
        ConfigurationSection configurationSection = Plugin.getInstance().getConfig().getConfigurationSection("message.command.kick");
        ClanImpl clan = (ClanImpl) modifiedPlayer.getClan();
        if (!clan.getMembers().containsKey(modifiedPlayer)) return new ClanResponse(configurationSection.getString("the_player_is_not_in_the_clan"), ClanResponse.ResponseType.FAILURE);
        clan.getMembers().remove(modifiedPlayer);

        for (Map.Entry<ModifiedPlayer, String> entry : clan.getMembers().entrySet()) {
            String string = configurationSection.getString("notification_of_exclusion").replace("%player%", modifiedPlayer.getPlayer().getName());
            chatUtil.sendMessage(entry.getKey().getPlayer(), string);
        }

        return new ClanResponse(null, ClanResponse.ResponseType.SUCCESS);
    }

    @Override
    public ClanResponse invest(ModifiedPlayer modifiedPlayer, int amount) {
        ConfigurationSection configurationSection = Plugin.getInstance().getConfig().getConfigurationSection("message.command.invest");
        if (Plugin.getInstance().economy.getBalance(modifiedPlayer.getPlayer()) < amount) new ClanResponse(configurationSection.getString("you_don't_have_enough"), ClanResponse.ResponseType.FAILURE);
        int maximumBalance = LevelType.getLevelMaximumBalance(getLevel());
        ClanImpl clan = (ClanImpl) modifiedPlayer.getClan();
        if ((clan.getBalance() + amount) > maximumBalance) new ClanResponse(configurationSection.getString("there_is_no_place_in_the_clan"), ClanResponse.ResponseType.FAILURE);
        Plugin.getInstance().economy.withdrawPlayer(modifiedPlayer.getPlayer(), amount);
        clan.setBalance(clan.getBalance() + amount);

        for (Map.Entry<ModifiedPlayer, String> entry : clan.getMembers().entrySet()) {
            String string = configurationSection.getString("notification_of_investment").replace("%player%", modifiedPlayer.getPlayer().getName()).replace("%amount%", String.valueOf(amount));
            chatUtil.sendMessage(entry.getKey().getPlayer(), string);
        }

        return new ClanResponse(null, ClanResponse.ResponseType.SUCCESS);
    }

    @Override
    public ClanResponse withdraw(ModifiedPlayer modifiedPlayer, int amount) {
        ConfigurationSection configurationSection = Plugin.getInstance().getConfig().getConfigurationSection("message.command.withdraw");
        if (getBalance() < amount) return new ClanResponse(configurationSection.getString("not_in_the_clan"), ClanResponse.ResponseType.FAILURE);

        ClanImpl clan = (ClanImpl) modifiedPlayer.getClan();
        Plugin.getInstance().economy.depositPlayer(modifiedPlayer.getPlayer(), amount);
        clan.setBalance(clan.getBalance() - amount);

        for (Map.Entry<ModifiedPlayer, String> entry : getMembers().entrySet()) {
            String string = configurationSection.getString("notification_of_withdrawal").replace("%player%", modifiedPlayer.getPlayer().getName()).replace("%amount%", String.valueOf(amount));
            chatUtil.sendMessage(entry.getKey().getPlayer(), string);
        }

        return new ClanResponse(null, ClanResponse.ResponseType.SUCCESS);
    }

    @Override
    public ClanResponse leave(ModifiedPlayer modifiedPlayer) {
        ConfigurationSection configurationSection = Plugin.getInstance().getConfig().getConfigurationSection("message.command.leave");
        if (!getMembers().containsKey(modifiedPlayer)) return new ClanResponse(configurationSection.getString("you're_not_in_the_clan)"), ClanResponse.ResponseType.FAILURE);
        ClanImpl clan = (ClanImpl) modifiedPlayer.getClan();
        clan.getMembers().remove(modifiedPlayer);

        for (Map.Entry<ModifiedPlayer, String> entry : clan.getMembers().entrySet()) {
            String string = configurationSection.getString("notification_of_exclusion").replace("%player%", modifiedPlayer.getPlayer().getName());
            chatUtil.sendMessage(entry.getKey().getPlayer(), string);
        }

        return new ClanResponse(null, ClanResponse.ResponseType.SUCCESS);
    }

    @Override
    public ClanResponse rank(ModifiedPlayer modifiedPlayer, int id) {
        ConfigurationSection configurationSection = Plugin.getInstance().getConfig().getConfigurationSection("message.command.rank");
        if (id == 1) return new ClanResponse(configurationSection.getString("you_can't_give_out_a_leader_rank"), ClanResponse.ResponseType.FAILURE);
        ClanImpl clan = (ClanImpl) modifiedPlayer.getClan();
        if (id == 2) clan.getMembers().replace(modifiedPlayer, getMembers().get(modifiedPlayer), RankType.DEPUTY.getName());
        else if (id == 3)
            clan.getMembers().replace(modifiedPlayer, getMembers().get(modifiedPlayer), RankType.MEMBER.getName());
        else return new ClanResponse(configurationSection.getString("this_rank_does_not_exist"), ClanResponse.ResponseType.FAILURE);
        ConfigurationSection configSection = Plugin.getInstance().getConfig().getConfigurationSection("message.command.rank");

        for (Map.Entry<ModifiedPlayer, String> entry : clan.getMembers().entrySet()) {
            String string = configSection.getString("notification_of_rank").replace("%player%", modifiedPlayer.getPlayer().getName()).replace("%rank%", (id == 2) ? RankType.DEPUTY.getName() : RankType.MEMBER.getName());
            chatUtil.sendMessage(entry.getKey().getPlayer(), string);
        }

        return new ClanResponse(null, ClanResponse.ResponseType.SUCCESS);
    }

    @Override
    public ClanResponse disband(ModifiedPlayer modifiedPlayer) {
        ConfigurationSection configurationSection = Plugin.getInstance().getConfig().getConfigurationSection("message.command.disband");
        if (!getMembers().get(modifiedPlayer).equals("Лидер")) return new ClanResponse(configurationSection.getString("you_are_not_a_leader"), ClanResponse.ResponseType.FAILURE);

        ClanImpl clan = (ClanImpl) modifiedPlayer.getClan();
        for (Map.Entry<ModifiedPlayer, String> entry : clan.getMembers().entrySet()) {
            entry.getKey().setClan(null);

            chatUtil.sendMessage(entry.getKey().getPlayer(), configurationSection.getString("notification_of_disband"));
        }

        CLANS.remove((clan).getName());
        return new ClanResponse(null, ClanResponse.ResponseType.SUCCESS);
    }

    @Override
    public void showMenu(ModifiedPlayer modifiedPlayer, int id) {
        Inventory menu = null;

        switch (id) {
            case 1:
                menu = MenuType.MENU_CLAN.getMenu((ClanImpl) modifiedPlayer.getClan(), 1);
                modifiedPlayer.getPlayer().openInventory(menu);
                break;
            case 2:
                menu = MenuType.MENU_CLAN_MEMBERS.getMenu((ClanImpl) modifiedPlayer.getClan(), 2);
                modifiedPlayer.getPlayer().openInventory(menu);
                break;
            case 3:
                menu = MenuType.MENU_LEVEL_CLAN.getMenu((ClanImpl) modifiedPlayer.getClan(), 3);
        }

        modifiedPlayer.getPlayer().openInventory(menu);
    }

    public enum MenuType {

        MENU_CLAN(1),
        MENU_CLAN_MEMBERS(2),
        MENU_LEVEL_CLAN(3);

        private final int id;

        MenuType(int id) {
            this.id = id;
        }

        public static Inventory getMenu(ClanImpl clan, int id) {
            Inventory menu = null;
            ItemStack redStainedGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            int[] indices = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
            ItemStack back;
            int maximumBalance;
            List<String> list;
            List<String> replacedList;
            int slot;
            ConfigurationSection configSection;

            switch (id) {
                case 1:

                    menu = Bukkit.createInventory(null, 45, "Клан " + clan.getName());

                    for (int index : indices) {
                        menu.setItem(index, redStainedGlassPane);
                    }

                    configSection = Plugin.getInstance().getConfig().getConfigurationSection("settings.menu.menu_clan.items");

                    maximumBalance = LevelType.getLevelMaximumBalance(clan.getLevel());

                    list = configSection.getStringList("information.lore");
                    final int finalMaximumBalance = maximumBalance;
                    replacedList = list.stream().map(string -> {
                        String replacedString = string
                                .replace("%name%", clan.getName())
                                .replace("%members%", String.valueOf(clan.getMembers().size()))
                                .replace("%level%", String.valueOf(clan.getLevel()))
                                .replace("%xp%", String.valueOf(clan.getXp()))
                                .replace("%balance%", String.valueOf(clan.getBalance()))
                                .replace("%pvp%", (clan.isPvp()) ? "Да" : "Нет")
                                .replace("%maximum_balance%", String.valueOf(finalMaximumBalance))
                                .replace("%kills%", String.valueOf(clan.getKills()))
                                .replace("%deaths%", String.valueOf(clan.getDeaths()));

                        return HexUtil.color(replacedString);
                    }).collect(Collectors.toList());

                    ItemStack information = new ItemBuilder(Material.KNOWLEDGE_BOOK)
                            .setName(HexUtil.color(configSection.getString("information.title")))
                            .setLore(replacedList)
                            .build();

                    list = configSection.getStringList("members_clan.lore");
                    final int finalMaximumBalance1 = maximumBalance;
                    replacedList = list.stream().map(string -> {
                        String replacedString = string
                                .replace("%name%", clan.getName())
                                .replace("%members%", String.valueOf(clan.getMembers().size()))
                                .replace("%level%", String.valueOf(clan.getLevel()))
                                .replace("%xp%", String.valueOf(clan.getXp()))
                                .replace("%balance%", String.valueOf(clan.getBalance()))
                                .replace("%pvp%", (clan.isPvp()) ? "Да" : "Нет")
                                .replace("%maximum_balance%", String.valueOf(finalMaximumBalance1))
                                .replace("%kills%", String.valueOf(clan.getKills()))
                                .replace("%deaths%", String.valueOf(clan.getDeaths()));

                        return HexUtil.color(replacedString);
                    }).collect(Collectors.toList());

                    ItemStack members = new ItemBuilder(Material.TOTEM_OF_UNDYING)
                            .setName(HexUtil.color(configSection.getString("members_clan.title")))
                            .setLore(replacedList)
                            .build();

                    list = configSection.getStringList("level_clan.lore");
                    final int finalMaximumBalance4 = maximumBalance;
                    replacedList = list.stream().map(string -> {
                        String replacedString = string
                                .replace("%name%", clan.getName())
                                .replace("%members%", String.valueOf(clan.getMembers().size()))
                                .replace("%level%", String.valueOf(clan.getLevel()))
                                .replace("%xp%", String.valueOf(clan.getXp()))
                                .replace("%balance%", String.valueOf(clan.getBalance()))
                                .replace("%pvp%", (clan.isPvp()) ? "Да" : "Нет")
                                .replace("%maximum_balance%", String.valueOf(finalMaximumBalance4))
                                .replace("%kills%", String.valueOf(clan.getKills()))
                                .replace("%deaths%", String.valueOf(clan.getDeaths()));

                        return HexUtil.color(replacedString);
                    }).collect(Collectors.toList());

                    ItemStack level = new ItemBuilder(Material.DIAMOND)
                            .setName(HexUtil.color(configSection.getString("level_clan.title")))
                            .setLore(replacedList)
                            .build();

                    back = new ItemBuilder(Material.SPECTRAL_ARROW)
                            .setName(HexUtil.color(configSection.getString("exit.title")))
                            .build();

                    menu.setItem(34, back);
                    menu.setItem(10, information);
                    menu.setItem(11, members);
                    menu.setItem(12, level);
                    return menu;
                case 2:
                    menu = Bukkit.createInventory(null, 45, "Участники клана");


                    for (int index : indices) {
                        menu.setItem(index, redStainedGlassPane);
                    }

                    configSection = Plugin.getInstance().getConfig().getConfigurationSection("settings.menu.menu_clan_members");

                    back = new ItemBuilder(Material.SPECTRAL_ARROW)
                            .setName(HexUtil.color(configSection.getString("in_menu.title")))
                            .build();

                    menu.setItem(34, back);

                    slot = 10;
                    for (Map.Entry<ModifiedPlayer, String> entry : clan.getMembers().entrySet()) {

                        if (menu.getItem(slot) == null) {
                            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                            if (Bukkit.getPlayer(entry.getKey().getPlayer().getName()) == null)
                                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(entry.getKey().getPlayer().getName()));
                            else skullMeta.setOwningPlayer(Bukkit.getPlayer(entry.getKey().getPlayer().getName()));
                            list = configSection.getStringList("player.lore");
                            replacedList = list.stream().map(string -> {
                                String replacedString = string
                                        .replace("%rank%", clan.getMembers().get(entry.getKey()));
                                return HexUtil.color(replacedString);
                            }).collect(Collectors.toList());
                            skullMeta.setLore(replacedList);
                            skullMeta.setDisplayName(HexUtil.color("&fИгрок " + entry.getKey().getPlayer().getName()));
                            skull.setItemMeta(skullMeta);
                            menu.setItem(slot, skull);
                            slot++;
                        } else {
                            slot++;
                        }

                    }
                    return menu;
                case 3:
                    menu = Bukkit.createInventory(null, 45, "Уровень клана");

                    for (int index : indices) {
                        menu.setItem(index, redStainedGlassPane);
                    }

                    configSection = Plugin.getInstance().getConfig().getConfigurationSection("settings.menu.menu_level_clan.level");

                    back = new ItemBuilder(Material.SPECTRAL_ARROW)
                            .setName(HexUtil.color(configSection.getString("in_menu.title")))
                            .build();

                    menu.setItem(34, back);

                    slot = 10;
                    for (int i = 1; i <= LevelType.countLevel; i++) {
                        maximumBalance = LevelType.getLevelMaximumBalance(i);
                        if (clan.getLevel() < i) {
                            ItemStack furnaceMinecart = new ItemStack(Material.FURNACE_MINECART);
                            ItemMeta furnaceMinecartMeta = furnaceMinecart.getItemMeta();
                            furnaceMinecartMeta.setDisplayName(HexUtil.color(configSection.getString("not_received.title").replace("%level%", String.valueOf(i))));
                            list = configSection.getStringList("not_received.lore");
                            final int finalMaximumBalance2 = maximumBalance;
                            replacedList = list.stream().map(string -> {
                                String replacedString = string
                                        .replace("%name%", clan.getName())
                                        .replace("%members%", String.valueOf(clan.getMembers().size()))
                                        .replace("%level%", String.valueOf(clan.getLevel()))
                                        .replace("%xp%", String.valueOf(clan.getXp()))
                                        .replace("%pvp%", (clan.isPvp()) ? "Да" : "Нет").replace("%maximum_balance%", String.valueOf(finalMaximumBalance2))
                                        .replace("%kills%", String.valueOf(clan.getKills()))
                                        .replace("%deaths%", String.valueOf(clan.getDeaths()));
                                return HexUtil.color(replacedString);
                            }).collect(Collectors.toList());
                            furnaceMinecartMeta.setLore(replacedList);
                            furnaceMinecart.setItemMeta(furnaceMinecartMeta);
                            menu.setItem(slot, furnaceMinecart);
                            slot++;
                            continue;
                        }

                        ItemStack chestMinecart = new ItemStack(Material.CHEST_MINECART);
                        ItemMeta chestMinecartMeta = chestMinecart.getItemMeta();
                        chestMinecartMeta.setDisplayName(HexUtil.color(configSection.getString("received.title").replace("%level%", String.valueOf(i))));
                        list = configSection.getStringList("received.lore");
                        final int finalMaximumBalance3 = maximumBalance;
                        replacedList = list.stream().map(string -> {
                            String replacedString = string
                                    .replace("%name%", clan.getName())
                                    .replace("%members%", String.valueOf(clan.getMembers().size()))
                                    .replace("%level%", String.valueOf(clan.getLevel()))
                                    .replace("%xp%", String.valueOf(clan.getXp()))
                                    .replace("%pvp%", (clan.isPvp()) ? "Да" : "Нет").replace("%maximum_balance%", String.valueOf(finalMaximumBalance3))
                                    .replace("%kills%", String.valueOf(clan.getKills()))
                                    .replace("%deaths%", String.valueOf(clan.getDeaths()));
                            return HexUtil.color(replacedString);
                        }).collect(Collectors.toList());
                        chestMinecartMeta.setLore(replacedList);
                        chestMinecart.setItemMeta(chestMinecartMeta);
                        menu.setItem(slot, chestMinecart);
                        slot++;

                    }
            }
            return menu;
        }

        public int getId() {
            return id;
        }


        public static boolean identical(Inventory o1, Inventory o2) {

            ItemStack[] items1 = o1.getContents();
            ItemStack[] items2 = o2.getContents();

            if(items1.length != items2.length) return false;

            for(int i = 0; i < items1.length; i++) {
                ItemStack item1 = items1[i];
                ItemStack item2 = items2[i];

                if(item1 == null && item2 == null) continue;

                if(item1 == null || item2 == null) return false;

                if(!item1.isSimilar(item2)) return false;

            }

            return true;
        }

    }




    public static Map<String, ClanImpl> getClans() {
        return CLANS;
    }
}
