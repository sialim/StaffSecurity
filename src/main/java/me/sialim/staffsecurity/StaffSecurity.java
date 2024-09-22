package me.sialim.staffsecurity;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class StaffSecurity extends JavaPlugin implements Listener {
    private List<String> commandsToLog;
    private String discordChannelId;
    private String itemLogChannelId = "1287109476146610256";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();

        getServer().getPluginManager().registerEvents(this, this);
    }

    private void loadConfigValues()
    {
        FileConfiguration config = getConfig();
        commandsToLog = config.getStringList("commands_to_log");
        discordChannelId = config.getString("discord.channel_id");
    }
    
    @EventHandler public void onPlayerCommand(PlayerCommandPreprocessEvent e)
    {
        String command = e.getMessage().split(" ")[0].substring(1).toLowerCase();

        if (commandsToLog.contains(command))
        {
            String pName = e.getPlayer().getName();
            String fullCommand = e.getMessage();

            getLogger().info(pName + " executed command: " + fullCommand);
            logToDiscord(pName, fullCommand);
        }
    }

    @EventHandler public void onGameModeChange(PlayerGameModeChangeEvent e)
    {
        String pName = e.getPlayer().getName();
        GameMode newGameMode = e.getNewGameMode();

        getLogger().info(pName + " changed their game mode to: " + newGameMode.name());

        String message = ":warning: **" + pName + "** switched game mode to: `" + newGameMode.name() + "`";
        logToDiscord(message);
    }

    @EventHandler public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            String pName = e.getPlayer().getName();
            String itemName = e.getItemDrop().getItemStack().getType().name();

            String dropMessage = ":warning: **" + pName + "** dropped item: `" + itemName + "` from the creative inventory.";
            logToDiscord(itemLogChannelId, dropMessage);
        }
    }

    @EventHandler public void onCreativeInventory(InventoryCreativeEvent e) {
        if (e.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
            ItemStack itemName = e.getCurrentItem();
            String pName = e.getWhoClicked().getName();

            switch (e.getClick()) {
                case CREATIVE:
                    String takeMessage = ":warning: **" + pName + "** took item: `" + itemName + "` from the creative inventory.";
                    logToDiscord(itemLogChannelId, takeMessage);
                    break;

                case SHIFT_LEFT:
                case SHIFT_RIGHT:
                    if (e.getSlotType() == InventoryType.SlotType.QUICKBAR && e.getCurrentItem() == null) {
                        String deleteMessage = ":warning: **" + pName + "** deleted item: `" + itemName + "` from the creative inventory.";
                        logToDiscord(itemLogChannelId, deleteMessage);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private void logToDiscord(String message)
    {
        if (DiscordSRV.isReady && discordChannelId != null && !discordChannelId.isEmpty())
        {
            DiscordSRV.getPlugin().getJda().getTextChannelById(discordChannelId).sendMessage(message).queue();
        } else {
            getLogger().warning("DiscordSRV or the Discord channel ID aren't properly prepared.");
        }
    }

    private void logToDiscord(String channelId, String message) {
        if (DiscordSRV.isReady && channelId != null && !channelId.isEmpty()) {
            DiscordSRV.getPlugin().getJda().getTextChannelById(channelId).sendMessage(message).queue();
        } else {
            getLogger().warning("DiscordSRV or the specified Discord channel ID aren't properly prepared.");
        }
    }
}
