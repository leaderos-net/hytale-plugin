package net.leaderos.hytale.plugin.helpers;

import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import net.leaderos.hytale.plugin.LeaderosPlugin;

import javax.annotation.Nullable;
import java.util.logging.Level;

public class PlayerUtil {
    @Nullable
    public static PlayerRef findPlayerByName(String username) {
        try {
            Universe universe = Universe.get();
            if (universe == null) {
                return null;
            }
            var player = universe.getPlayerByUsername(username, NameMatching.EXACT);
            if (player == null) {
                LeaderosPlugin.getInstance().getLogger().at(Level.WARNING).log("Player not found: " + username, "Please check the username and try again.");
            }
            return player;
        } catch (Exception e) {
            LeaderosPlugin.getInstance().getLogger().at(Level.WARNING).log("Error finding player by name: " + username + " - " + e.getMessage());
        }
        return null;
    }
}
