package me.asofold.bpl.cncp.bedrock;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.network.session.GeyserSession;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import me.asofold.bpl.cncp.CompatNoCheatPlus;
import me.asofold.bpl.cncp.config.Settings;
import org.geysermc.floodgate.api.FloodgateApi;

public class BedrockPlayerListener implements Listener, PluginMessageListener {
    
    private Plugin floodgate = Bukkit.getPluginManager().getPlugin("floodgate-bukkit");
    private Plugin geyser    = Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
    private final Settings settings = CompatNoCheatPlus.getInstance().getSettings();
    
    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (floodgate != null && floodgate.isEnabled()) {
            FloodgateApi api = FloodgateApi.getInstance();
            if (api.isFloodgatePlayer(player.getUniqueId())) {
                processExemption(player);
            }
        } else
        if (geyser != null && geyser.isEnabled()) {
            GeyserSession session = GeyserConnector.getInstance().getPlayerByUuid(player.getUniqueId());
            if(session != null){
                processExemption(player);
            }
        }
    }

    private void processExemption(final Player player) {
        final IPlayerData pData = DataManager.getPlayerData(player);
        if (pData != null) {
            for (CheckType check : settings.extemptChecks) pData.exempt(check);
            MovingData data = pData.getGenericInstance(MovingData.class);
            data.bedrockPlayer = true;
        }
    }

    private void processExemption(final String playername) {
        final IPlayerData pData = DataManager.getPlayerData(playername);
        if (pData != null) {
            for (CheckType check : settings.extemptChecks) pData.exempt(check);
            MovingData data = pData.getGenericInstance(MovingData.class);
            data.bedrockPlayer = true;
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        if (CompatNoCheatPlus.getInstance().isBungeeEnabled() && channel.equals("cncp:geyser")) {
            geyser = null;
            floodgate = null;
            ByteArrayDataInput input = ByteStreams.newDataInput(data);
            String playerName = input.readUTF();
            processExemption(playerName);
        }
    }
}
