package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.UUID;

public class LegacySavesUpdater {

    public static void updateLegacySaves() {
        Debug.log("==== UPDATING LEGACY SAVES TO NEW FLAG ENGINE ====");
        File savesFile = new File(Denizen.getInstance().getDataFolder(), "saves.yml");
        FileConfiguration saveSection = YamlConfiguration.loadConfiguration(savesFile);
        savesFile.renameTo(new File(Denizen.getInstance().getDataFolder(), "saves.yml.bak"));
        if (saveSection.contains("Global")) {
            Debug.log("==== Update global data ====");
            ConfigurationSection globalSection = saveSection.getConfigurationSection("Global");
            if (globalSection.contains("Flags")) {
                applyFlags(Denizen.getInstance().serverFlagMap, globalSection.getConfigurationSection("Flags"));
            }
            if (globalSection.contains("Scripts")) {
                ConfigurationSection scriptsSection = globalSection.getConfigurationSection("Scripts");
                for (String script : scriptsSection.getKeys(false)) {
                    ConfigurationSection scriptSection = scriptsSection.getConfigurationSection(script);
                    if (scriptSection.contains("Cooldown Time")) {
                        long time = scriptSection.getLong("Cooldown Time");
                        TimeTag cooldown = new TimeTag(time);
                        Denizen.getInstance().serverFlagMap.setFlag("__interact_cooldown." + script, cooldown, cooldown);
                    }
                }
            }
        }
        if (saveSection.contains("Players")) {
            Debug.log("==== Update player data ====");
            ConfigurationSection playerSection = saveSection.getConfigurationSection("Players");
            for (String plPrefix : playerSection.getKeys(false)) {
                ConfigurationSection subSection = playerSection.getConfigurationSection(plPrefix);
                for (String uuidString : subSection.getKeys(false)) {
                    UUID id = UUID.fromString(uuidString.substring(0, 8) + "-" + uuidString.substring(8, 12) + "-" + uuidString.substring(12, 16) + "-" + uuidString.substring(16, 20) + "-" + uuidString.substring(20, 32));
                    PlayerTag player = PlayerTag.valueOf(id.toString(), CoreUtilities.errorButNoDebugContext);
                    if (player == null) {
                        Debug.echoError("Cannot update data for player with id: " + uuidString);
                        continue;
                    }
                    ConfigurationSection actual = subSection.getConfigurationSection(uuidString);
                    AbstractFlagTracker tracker = player.getFlagTracker();
                    if (actual.contains("Flags")) {
                        applyFlags(tracker, actual.getConfigurationSection("Flags"));
                    }
                    if (actual.contains("Scripts")) {
                        ConfigurationSection scriptsSection = actual.getConfigurationSection("Scripts");
                        for (String script : scriptsSection.getKeys(false)) {
                            ConfigurationSection scriptSection = scriptsSection.getConfigurationSection(script);
                            if (scriptSection.contains("Current Step")) {
                                tracker.setFlag("__interact_step." + script, new ElementTag(scriptSection.getString("Current Step")), null);
                            }
                            if (scriptSection.contains("Cooldown Time")) {
                                long time = scriptSection.getLong("Cooldown Time");
                                TimeTag cooldown = new TimeTag(time);
                                tracker.setFlag("__interact_cooldown." + script, cooldown, cooldown);
                            }
                        }
                    }
                    player.reapplyTracker(tracker);
                }
            }
        }
        if (saveSection.contains("NPCs")) {
            final ConfigurationSection npcsSection = saveSection.getConfigurationSection("NPCs");
            new BukkitRunnable() {
                @Override
                public void run() {
                    Debug.log("==== Late update NPC data ====");
                    for (String npcId : npcsSection.getKeys(false)) {
                        ConfigurationSection actual = npcsSection.getConfigurationSection(npcId);
                        NPCTag npc = NPCTag.valueOf(npcId, CoreUtilities.errorButNoDebugContext);
                        if (npc == null) {
                            Debug.echoError("Cannot update data for NPC with id: " + npcId);
                            continue;
                        }
                        AbstractFlagTracker tracker = npc.getFlagTracker();
                        if (actual.contains("Flags")) {
                            applyFlags(tracker, actual.getConfigurationSection("Flags"));
                        }
                        npc.reapplyTracker(tracker);
                        Debug.log("==== Done late-updating NPC data ====");
                    }
                }
            }.runTaskLater(Denizen.getInstance(), 3);
        }
        Denizen.getInstance().saveSaves();
        Debug.log("==== Done updating legacy saves (except NPCs) ====");
    }

    public static void applyFlags(AbstractFlagTracker tracker, ConfigurationSection section) {
        try {
            for (String flagName : section.getKeys(false)) {
                if (flagName.endsWith("-expiration")) {
                    continue;
                }
                TimeTag expireAt = null;
                if (section.contains(flagName + "-expiration")) {
                    long expireTime = section.getLong(flagName + "-expiration");
                    expireAt = new TimeTag(expireTime);
                }
                Object value = section.get(flagName);
                ObjectTag setAs = CoreUtilities.objectToTagForm(value, CoreUtilities.errorButNoDebugContext);
                tracker.setFlag(flagName, setAs, expireAt);
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }
}
