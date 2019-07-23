package com.denizenscript.denizen;

import com.denizenscript.denizen.events.bukkit.ScriptReloadEvent;
import com.denizenscript.denizen.flags.FlagManager;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.scripts.containers.core.*;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizencore.DenizenImplementation;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptHelper;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debuggable;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DenizenCoreImplementation implements DenizenImplementation {

    @Override
    public File getScriptFolder() {
        File file = null;
        // Get the script directory
        if (Settings.useDefaultScriptPath()) {
            file = new File(DenizenAPI.getCurrentInstance()
                    .getDataFolder() + File.separator + "scripts");
        }
        else {
            file = new File(Settings.getAlternateScriptPath().replace("/", File.separator));
        }
        return file;
    }

    @Override
    public String getImplementationVersion() {
        return Denizen.versionTag;
    }

    @Override
    public void debugMessage(String message) {
        Debug.log(message);
    }

    @Override
    public void debugException(Throwable ex) {
        Debug.echoError(ex);
    }

    @Override
    public void debugError(String error) {
        Debug.echoError(error);
    }

    @Override
    public void debugError(ScriptQueue scriptQueue, String s) {
        Debug.echoError(scriptQueue, s);
    }

    @Override
    public void debugError(ScriptQueue scriptQueue, Throwable throwable) {
        Debug.echoError(scriptQueue, throwable);
    }

    @Override
    public void debugReport(Debuggable debuggable, String s, String s1) {
        Debug.report(debuggable, s, s1);
    }

    @Override
    public void debugApproval(String message) {
        Debug.echoApproval(message);
    }

    @Override
    public void debugEntry(Debuggable debuggable, String s) {
        Debug.echoDebug(debuggable, s);
    }

    @Override
    public void debugEntry(Debuggable debuggable, com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement debugElement, String s) {
        Debug.echoDebug(debuggable, debugElement, s);
    }

    @Override
    public void debugEntry(Debuggable debuggable, com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement debugElement) {
        Debug.echoDebug(debuggable, debugElement);
    }

    @Override
    public String getImplementationName() {
        return "Spigot";
    }

    @Override
    public void preScriptReload() {
        // Remove all recipes added by Denizen item scripts
        ItemScriptHelper.removeDenizenRecipes();
        // Remove all registered commands added by Denizen command scripts
        CommandScriptHelper.removeDenizenCommands();
        // Remove all registered economy scripts if needed
        if (Depends.vault != null) {
            EconomyScriptContainer.cleanup();
        }
    }

    @Override
    public void onScriptReload() {
        Depends.setupEconomy();
        Bukkit.getServer().getPluginManager().callEvent(new ScriptReloadEvent());
    }

    @Override
    public void buildCoreContainers(YamlConfiguration config) {
        ScriptRegistry._buildCoreYamlScriptContainers(config);
    }

    @Override
    public List<YamlConfiguration> getOutsideScripts() {
        List<YamlConfiguration> files = new ArrayList<>();
        try {
            files.add(ScriptHelper.loadConfig("Denizen.jar/util.dsc", DenizenAPI.getCurrentInstance().getResource("util.dsc")));
        }
        catch (IOException e) {
            Debug.echoError(e);
        }
        return files;
    }

    @Override
    public boolean shouldDebug(Debuggable debug) {
        return Debug.shouldDebug(debug);
    }

    @Override
    public void debugQueueExecute(ScriptEntry entry, String queue, String execute) {
        Consumer<String> altDebug = entry.getResidingQueue().debugOutput;
        entry.getResidingQueue().debugOutput = null;
        Debug.echoDebug(entry, com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement.Header,
                ChatColor.LIGHT_PURPLE + "Queue '" + queue + ChatColor.LIGHT_PURPLE + "' Executing: " + ChatColor.WHITE + execute);
        entry.getResidingQueue().debugOutput = altDebug;
    }

    @Override
    public void debugTagFill(Debuggable entry, String tag, String result) {
        Debug.echoDebug(entry, ChatColor.DARK_GRAY + "Filled tag <" + ChatColor.WHITE + tag
                + ChatColor.DARK_GRAY + "> with '" + ChatColor.WHITE + result + ChatColor.DARK_GRAY + "'.");
    }

    @Override
    public String queueHeaderInfo(ScriptEntry scriptEntry) {
        BukkitScriptEntryData data = ((BukkitScriptEntryData) scriptEntry.entryData);
        if (data.hasPlayer() && data.hasNPC()) {
            return " with player '" + data.getPlayer().getName() + "' and NPC '" + data.getNPC().getId() + "/" + data.getNPC().getName() + "'";
        }
        else if (data.hasPlayer()) {
            return " with player '" + data.getPlayer().getName() + "'";
        }
        else if (data.hasNPC()) {
            return " with NPC '" + data.getNPC().getId() + "/" + data.getNPC().getName() + "'";
        }
        return "";
    }

    @Override
    public TagContext getTagContextFor(ScriptEntry scriptEntry, boolean b) {
        PlayerTag player = scriptEntry != null ? Utilities.getEntryPlayer(scriptEntry) : null;
        NPCTag npc = scriptEntry != null ? Utilities.getEntryNPC(scriptEntry) : null;
        return new BukkitTagContext(player, npc, b, scriptEntry,
                scriptEntry != null ? scriptEntry.shouldDebug() : true,
                scriptEntry != null ? scriptEntry.getScript() : null);
    }

    @Override
    public boolean needsHandleArgPrefix(String prefix) {
        return prefix.equals("player") || prefix.equals("npc") || prefix.equals("npcid");
    }

    @Override
    public boolean handleCustomArgs(ScriptEntry scriptEntry, Argument arg, boolean if_ignore) {
        // Fill player/off-line player
        if (arg.matchesPrefix("player") && !if_ignore) {
            Debug.echoDebug(scriptEntry, "...replacing the linked player with " + arg.getValue());
            String value = TagManager.tag(arg.getValue(), new BukkitTagContext(scriptEntry, false));
            PlayerTag player = PlayerTag.valueOf(value);
            if (player == null || !player.isValid()) {
                Debug.echoError(scriptEntry.getResidingQueue(), value + " is an invalid player!");
            }
            ((BukkitScriptEntryData) scriptEntry.entryData).setPlayer(player);
            return true;
        }

        // Fill NPCID/NPC argument
        else if (arg.matchesPrefix("npc, npcid") && !if_ignore) {
            Debug.echoDebug(scriptEntry, "...replacing the linked NPC with " + arg.getValue());
            String value = TagManager.tag(arg.getValue(), new BukkitTagContext(scriptEntry, false));
            NPCTag npc = NPCTag.valueOf(value);
            if (npc == null || !npc.isValid()) {
                Debug.echoError(scriptEntry.getResidingQueue(), value + " is an invalid NPC!");
                return false;
            }
            ((BukkitScriptEntryData) scriptEntry.entryData).setNPC(npc);
            return true;
        }
        return false;
    }

    @Override
    public void refreshScriptContainers() {
        VersionScriptContainer.scripts.clear();
        ItemScriptHelper.item_scripts.clear();
        ItemScriptHelper.item_scripts_by_hash_id.clear();
        InventoryScriptHelper.inventory_scripts.clear();
    }

    @Override
    public String scriptQueueSpeed() {
        return Settings.scriptQueueSpeed();
    }

    @Override
    public ListTag valueOfFlagListTag(String string) {
        FlagManager.Flag flag = DenizenAPI.getCurrentInstance().getFlag(string);
        if (flag == null) {
            return null;
        }
        return new ListTag(flag.toString(), true, flag.values());
    }

    @Override
    public boolean matchesFlagListTag(String arg) {
        boolean flag = false;
        if (arg.startsWith("fl")) {
            if (arg.indexOf('[') == 2) {
                int cb = arg.indexOf(']');
                if (cb > 4 && arg.indexOf('@') == (cb + 1)) {
                    String owner = arg.substring(3, cb);
                    flag = arg.substring(cb + 2).length() > 0 && (PlayerTag.matches(owner)
                            || (Depends.citizens != null && NPCTag.matches(owner)));
                }
            }
            else if (arg.indexOf('@') == 2) {
                flag = arg.substring(3).length() > 0;
            }
        }
        return flag;
    }

    @Override
    public String getLastEntryFromFlag(String flag) {
        FlagManager.Flag theflag = DenizenAPI.getCurrentInstance().getFlag(flag);
        if (theflag == null || theflag.getLast() == null) {
            return null;
        }
        return theflag.getLast().asString();
    }

    @Override
    public TagContext getTagContext(ScriptEntry scriptEntry) {
        return new BukkitTagContext(scriptEntry, false);
    }

    @Override
    public ScriptEntryData getEmptyScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
    }

    @Override
    public int getTagTimeout() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1)) {
            return 0;
        }
        return Settings.tagTimeout();
    }

    @Override
    public boolean allowConsoleRedirection() {
        return Settings.allowConsoleRedirection();
    }

    @Override
    public String cleanseLogString(String input) {
        return cleanseLog(input);
    }

    public static String cleanseLog(String input) {
        String esc = String.valueOf((char) 0x1b);
        String repc = String.valueOf(ChatColor.COLOR_CHAR);
        if (input.contains(esc)) {
            input = StringUtils.replace(input, esc + "[0;30;22m", repc + "0");
            input = StringUtils.replace(input, esc + "[0;34;22m", repc + "1");
            input = StringUtils.replace(input, esc + "[0;32;22m", repc + "2");
            input = StringUtils.replace(input, esc + "[0;36;22m", repc + "3");
            input = StringUtils.replace(input, esc + "[0;31;22m", repc + "4");
            input = StringUtils.replace(input, esc + "[0;35;22m", repc + "5");
            input = StringUtils.replace(input, esc + "[0;33;22m", repc + "6");
            input = StringUtils.replace(input, esc + "[0;37;22m", repc + "7");
            input = StringUtils.replace(input, esc + "[0;30;1m", repc + "8");
            input = StringUtils.replace(input, esc + "[0;34;1m", repc + "9");
            input = StringUtils.replace(input, esc + "[0;32;1m", repc + "a");
            input = StringUtils.replace(input, esc + "[0;36;1m", repc + "b");
            input = StringUtils.replace(input, esc + "[0;31;1m", repc + "c");
            input = StringUtils.replace(input, esc + "[0;35;1m", repc + "d");
            input = StringUtils.replace(input, esc + "[0;33;1m", repc + "e");
            input = StringUtils.replace(input, esc + "[0;37;1m", repc + "f");
            input = StringUtils.replace(input, esc + "[5m", repc + "k");
            input = StringUtils.replace(input, esc + "[21m", repc + "l");
            input = StringUtils.replace(input, esc + "[9m", repc + "m");
            input = StringUtils.replace(input, esc + "[4m", repc + "n");
            input = StringUtils.replace(input, esc + "[3m", repc + "o");
            input = StringUtils.replace(input, esc + "[m", repc + "r");
        }
        return input;
    }

    @Override
    public boolean matchesType(String comparable, String comparedto) {

        boolean outcome = false;

        if (comparedto.equalsIgnoreCase("location")) {
            outcome = LocationTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("material")) {
            outcome = MaterialTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("materiallist")) {
            outcome = ListTag.valueOf(comparable).containsObjectsFrom(MaterialTag.class);
        }
        else if (comparedto.equalsIgnoreCase("entity")) {
            outcome = EntityTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("spawnedentity")) {
            outcome = (EntityTag.matches(comparable) && EntityTag.valueOf(comparable).isSpawned());
        }
        else if (comparedto.equalsIgnoreCase("npc")) {
            outcome = NPCTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("player")) {
            outcome = PlayerTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("offlineplayer")) {
            outcome = (PlayerTag.valueOf(comparable) != null && !PlayerTag.valueOf(comparable).isOnline());
        }
        else if (comparedto.equalsIgnoreCase("onlineplayer")) {
            outcome = (PlayerTag.valueOf(comparable) != null && PlayerTag.valueOf(comparable).isOnline());
        }
        else if (comparedto.equalsIgnoreCase("item")) {
            outcome = ItemTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("cuboid")) {
            outcome = CuboidTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("trade")) {
            outcome = TradeTag.matches(comparable);
        }
        else {
            Debug.echoError("Invalid 'matches' type '" + comparedto + "'!");
        }

        return outcome;
    }

    @Override
    public Thread getMainThread() {
        return NMSHandler.getInstance().getMainThread();
    }

    @Override
    public boolean allowedToWebget() {
        return Settings.allowWebget();
    }

    @Override
    public void preTagExecute() {
        try {
            NMSHandler.getInstance().disableAsyncCatcher();
        }
        catch (Throwable e) {
            Debug.echoError("Running not-Spigot?!");
        }
    }

    @Override
    public void postTagExecute() {
        try {
            NMSHandler.getInstance().undisableAsyncCatcher();
        }
        catch (Throwable e) {
            Debug.echoError("Running not-Spigot?!");
        }
    }

    Boolean tTimeoutSil = null;

    @Override
    public boolean tagTimeoutWhenSilent() {
        if (tTimeoutSil == null) {
            tTimeoutSil = Settings.tagTimeoutSilent();
        }
        return tTimeoutSil;
    }

    @Override
    public boolean getDefaultDebugMode() {
        return Settings.defaultDebugMode();
    }

    @Override
    public boolean canWriteToFile(File f) {
        return Utilities.canWriteToFile(f);
    }

    public static ChatColor[] DEBUG_FRIENDLY_COLORS = new ChatColor[] {
            ChatColor.AQUA, ChatColor.BLUE, ChatColor.DARK_AQUA, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN,
            ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY, ChatColor.GREEN,
            ChatColor.LIGHT_PURPLE, ChatColor.WHITE, ChatColor.YELLOW
    };

    @Override
    public String getRandomColor() {
        return DEBUG_FRIENDLY_COLORS[CoreUtilities.getRandom().nextInt(DEBUG_FRIENDLY_COLORS.length)].toString();
    }

    @Override
    public int whileMaxLoops() {
        return Settings.whileMaxLoops();
    }

    @Override
    public boolean allowLogging() {
        return Settings.allowLogging();
    }

    @Override
    public boolean canReadFile(File f) {
        return Utilities.canReadFile(f);
    }

    @Override
    public boolean allowFileCopy() {
        return Settings.allowFilecopy();
    }

    @Override
    public File getDataFolder() {
        return DenizenAPI.getCurrentInstance().getDataFolder();
    }

    @Override
    public boolean allowStrangeYAMLSaves() {
        return Settings.allowStrangeYAMLSaves();
    }
}
