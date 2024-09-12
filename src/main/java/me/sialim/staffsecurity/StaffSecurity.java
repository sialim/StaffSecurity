package me.sialim.staffsecurity;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class StaffSecurity extends JavaPlugin implements Listener {
    private List<String> commandsToLog;
    private String discordChannelId;

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

    private void logToDiscord(String message)
    {
        if (DiscordSRV.isReady && discordChannelId != null && !discordChannelId.isEmpty())
        {
            DiscordSRV.getPlugin().getJda().getTextChannelById(discordChannelId).sendMessage(message).queue();
        } else {
            getLogger().warning("DiscordSRV or the Discord channel ID aren't properly prepared.");
        }
    }

    private void logToDiscord(String pName, String fullCommand)
    {
        if (DiscordSRV.isReady && discordChannelId != null && !discordChannelId.isEmpty())
        {
            String message = ":warning: **" + pName + "** executed command: `" + fullCommand + "`";
            DiscordSRV.getPlugin().getJda().getTextChannelById(discordChannelId).sendMessage(message).queue();
        } else {
            getLogger().warning("DiscordSRV or the Discord channel ID aren't properly prepared.");
        }
    }
}
