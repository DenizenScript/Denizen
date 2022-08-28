package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.scripts.ScriptHelper;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

public class LegacySavesUpdater {

    public static void updateLegacySaves() {
        Debug.log("==== UPDATING LEGACY SAVES TO NEW FLAG ENGINE ====");
        File savesFile = new File(Denizen.getInstance().getDataFolder(), "saves.yml");
        if (!savesFile.exists()) {
            Debug.echoError("Legacy update went weird: file doesn't exist?");
            return;
        }
        YamlConfiguration saveSection;
        try {
            FileInputStream fis = new FileInputStream(savesFile);
            String saveData = ScriptHelper.convertStreamToString(fis, false);
            fis.close();
            if (saveData.trim().length() == 0) {
                Debug.log("Nothing to update.");
                savesFile.delete();
                return;
            }
            saveSection = YamlConfiguration.load(saveData);
            if (saveSection == null) {
                Debug.echoError("Something went very wrong: legacy saves file failed to load!");
                return;
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return;
        }
        if (!savesFile.renameTo(new File(Denizen.getInstance().getDataFolder(), "saves.yml.bak"))) {
            Debug.echoError("Legacy saves file failed to rename!");
        }
        if (saveSection.contains("Global")) {
            Debug.log("==== Update global data ====");
            YamlConfiguration globalSection = saveSection.getConfigurationSection("Global");
            if (globalSection.contains("Flags")) {
                applyFlags("Server", DenizenCore.serverFlagMap, globalSection.getConfigurationSection("Flags"));
            }
            if (globalSection.contains("Scripts")) {
                YamlConfiguration scriptsSection = globalSection.getConfigurationSection("Scripts");
                for (StringHolder script : scriptsSection.getKeys(false)) {
                    YamlConfiguration scriptSection = scriptsSection.getConfigurationSection(script.str);
                    if (scriptSection.contains("Cooldown Time")) {
                        long time = Long.parseLong(scriptSection.getString("Cooldown Time"));
                        TimeTag cooldown = new TimeTag(time);
                        DenizenCore.serverFlagMap.setFlag("__interact_cooldown." + script.low, cooldown, cooldown);
                    }
                }
            }
        }
        if (saveSection.contains("Players")) {
            Debug.log("==== Update player data ====");
            YamlConfiguration playerSection = saveSection.getConfigurationSection("Players");
            for (StringHolder plPrefix : playerSection.getKeys(false)) {
                YamlConfiguration subSection = playerSection.getConfigurationSection(plPrefix.str);
                for (StringHolder uuidString : subSection.getKeys(false)) {
                    if (uuidString.str.length() != 32) {
                        Debug.echoError("Cannot update data for player with non-ID entry listed: " + uuidString);
                        continue;
                    }
                    try {
                        UUID id = UUID.fromString(uuidString.str.substring(0, 8) + "-" + uuidString.str.substring(8, 12) + "-" + uuidString.str.substring(12, 16) + "-" + uuidString.str.substring(16, 20) + "-" + uuidString.str.substring(20, 32));
                        PlayerTag player = PlayerTag.valueOf(id.toString(), CoreUtilities.errorButNoDebugContext);
                        if (player == null) {
                            Debug.echoError("Cannot update data for player with id: " + uuidString);
                            continue;
                        }
                        YamlConfiguration actual = subSection.getConfigurationSection(uuidString.str);
                        AbstractFlagTracker tracker = player.getFlagTracker();
                        if (actual.contains("Flags")) {
                            applyFlags(player.identify(), tracker, actual.getConfigurationSection("Flags"));
                        }
                        if (actual.contains("Scripts")) {
                            YamlConfiguration scriptsSection = actual.getConfigurationSection("Scripts");
                            for (StringHolder script : scriptsSection.getKeys(false)) {
                                YamlConfiguration scriptSection = scriptsSection.getConfigurationSection(script.str);
                                if (scriptSection.contains("Current Step")) {
                                    tracker.setFlag("__interact_step." + script, new ElementTag(scriptSection.getString("Current Step")), null);
                                }
                                if (scriptSection.contains("Cooldown Time")) {
                                    long time = Long.parseLong(scriptSection.getString("Cooldown Time"));
                                    TimeTag cooldown = new TimeTag(time);
                                    tracker.setFlag("__interact_cooldown." + script, cooldown, cooldown);
                                }
                            }
                        }
                        player.reapplyTracker(tracker);
                    }
                    catch (Throwable ex) {
                        Debug.echoError("Error updating flags for player with ID " + uuidString.str);
                        Debug.echoError(ex);
                    }
                }
            }
        }
        if (saveSection.contains("NPCs")) {
            final YamlConfiguration npcsSection = saveSection.getConfigurationSection("NPCs");
            new BukkitRunnable() {
                @Override
                public void run() {
                    Debug.log("==== Late update NPC data ====");
                    for (StringHolder npcId : npcsSection.getKeys(false)) {
                        YamlConfiguration actual = npcsSection.getConfigurationSection(npcId.str);
                        NPCTag npc = NPCTag.valueOf(npcId.str, CoreUtilities.errorButNoDebugContext);
                        if (npc == null) {
                            Debug.echoError("Cannot update data for NPC with id: " + npcId.str);
                            continue;
                        }
                        AbstractFlagTracker tracker = npc.getFlagTracker();
                        if (actual.contains("Flags")) {
                            applyFlags(npc.identify(), tracker, actual.getConfigurationSection("Flags"));
                        }
                        npc.reapplyTracker(tracker);
                        Debug.log("==== Done late-updating NPC data ====");
                    }
                }
            }.runTaskLater(Denizen.getInstance(), 3);
        }
        Denizen.getInstance().saveSaves(false);
        Debug.log("==== Done updating legacy saves (except NPCs) ====");
    }

    public static void applyFlags(String object, AbstractFlagTracker tracker, YamlConfiguration section) {
        try {
            if (section == null || section.getKeys(false).isEmpty()) {
                return;
            }
            for (StringHolder flagName : section.getKeys(false)) {
                if (flagName.low.endsWith("-expiration")) {
                    continue;
                }
                TimeTag expireAt = null;
                if (section.contains(flagName + "-expiration")) {
                    long expireTime = Long.parseLong(section.getString(flagName + "-expiration"));
                    expireAt = new TimeTag(expireTime);
                }
                Object value = section.get(flagName.str);
                ObjectTag setAs = CoreUtilities.objectToTagForm(value, CoreUtilities.errorButNoDebugContext);
                tracker.setFlag(flagName.low, setAs, expireAt);
            }
        }
        catch (Throwable ex) {
            Debug.echoError("Error while updating legacy flags for " + object);
            Debug.echoError(ex);
        }
    }
}
