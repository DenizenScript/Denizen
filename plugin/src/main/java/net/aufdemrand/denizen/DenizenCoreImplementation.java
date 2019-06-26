package net.aufdemrand.denizen;

import net.aufdemrand.denizen.events.bukkit.ScriptReloadEvent;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.containers.core.*;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.DenizenImplementation;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.ScriptHelper;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;
import net.aufdemrand.denizencore.utilities.debugging.Debuggable;
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
        dB.log(message);
    }

    @Override
    public void debugException(Throwable ex) {
        dB.echoError(ex);
    }

    @Override
    public void debugError(String error) {
        dB.echoError(error);
    }

    @Override
    public void debugError(ScriptQueue scriptQueue, String s) {
        dB.echoError(scriptQueue, s);
    }

    @Override
    public void debugError(ScriptQueue scriptQueue, Throwable throwable) {
        dB.echoError(scriptQueue, throwable);
    }

    @Override
    public void debugReport(Debuggable debuggable, String s, String s1) {
        dB.report(debuggable, s, s1);
    }

    @Override
    public void debugApproval(String message) {
        dB.echoApproval(message);
    }

    @Override
    public void debugEntry(Debuggable debuggable, String s) {
        dB.echoDebug(debuggable, s);
    }

    @Override
    public void debugEntry(Debuggable debuggable, net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement debugElement, String s) {
        dB.echoDebug(debuggable, debugElement, s);
    }

    @Override
    public void debugEntry(Debuggable debuggable, net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement debugElement) {
        dB.echoDebug(debuggable, debugElement);
    }

    @Override
    public String getImplementationName() {
        return "Bukkit";
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
    public void buildCoreContainers(net.aufdemrand.denizencore.utilities.YamlConfiguration config) {
        ScriptRegistry._buildCoreYamlScriptContainers(config);
    }

    @Override
    public List<YamlConfiguration> getOutsideScripts() {
        List<net.aufdemrand.denizencore.utilities.YamlConfiguration> files = new ArrayList<>();
        try {
            files.add(ScriptHelper.loadConfig("Denizen.jar/util.dsc", DenizenAPI.getCurrentInstance().getResource("util.dsc")));
        }
        catch (IOException e) {
            dB.echoError(e);
        }
        return files;
    }

    @Override
    public boolean shouldDebug(Debuggable debug) {
        return dB.shouldDebug(debug);
    }

    @Override
    public void debugQueueExecute(ScriptEntry entry, String queue, String execute) {
        Consumer<String> altDebug = entry.getResidingQueue().debugOutput;
        entry.getResidingQueue().debugOutput = null;
        dB.echoDebug(entry, ChatColor.DARK_GRAY + "Queue '" + queue + ChatColor.DARK_GRAY + "' Executing: " + execute);
        entry.getResidingQueue().debugOutput = altDebug;
    }

    @Override
    public void debugTagFill(Debuggable entry, String tag, String result) {
        dB.echoDebug(entry, ChatColor.DARK_GRAY + "Filled tag <" + ChatColor.WHITE + tag
                + ChatColor.DARK_GRAY + "> with '" + ChatColor.WHITE + result + ChatColor.DARK_GRAY + "'.");
    }

    @Override
    public void debugCommandHeader(ScriptEntry scriptEntry) {
        if (!dB.shouldDebug(scriptEntry)) {
            return;
        }
        if (scriptEntry.getOriginalArguments() == null ||
                scriptEntry.getOriginalArguments().size() == 0 ||
                !scriptEntry.getOriginalArguments().get(0).equals("\0CALLBACK")) {
            if (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                dB.echoDebug(scriptEntry, net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement.Header,
                        "Executing dCommand: " + scriptEntry.getCommandName() + "/p@" +
                                ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getName());
            }
            else {
                dB.echoDebug(scriptEntry, net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement.Header, "Executing dCommand: " +
                        scriptEntry.getCommandName() + (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ?
                        "/n@" + ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getName() : ""));
            }
        }
    }

    @Override
    public TagContext getTagContextFor(ScriptEntry scriptEntry, boolean b) {
        dPlayer player = scriptEntry != null ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer() : null;
        dNPC npc = scriptEntry != null ? ((BukkitScriptEntryData) scriptEntry.entryData).getNPC() : null;
        return new BukkitTagContext(player, npc, b, scriptEntry,
                scriptEntry != null ? scriptEntry.shouldDebug() : true,
                scriptEntry != null ? scriptEntry.getScript() : null);
    }

    @Override
    public boolean needsHandleArgPrefix(String prefix) {
        return prefix.equals("player") || prefix.equals("npc") || prefix.equals("npcid");
    }

    @Override
    public boolean handleCustomArgs(ScriptEntry scriptEntry, aH.Argument arg, boolean if_ignore) {
        // Fill player/off-line player
        if (arg.matchesPrefix("player") && !if_ignore) {
            dB.echoDebug(scriptEntry, "...replacing the linked player with " + arg.getValue());
            String value = TagManager.tag(arg.getValue(), new BukkitTagContext(scriptEntry, false));
            dPlayer player = dPlayer.valueOf(value);
            if (player == null || !player.isValid()) {
                dB.echoError(scriptEntry.getResidingQueue(), value + " is an invalid player!");
            }
            ((BukkitScriptEntryData) scriptEntry.entryData).setPlayer(player);
            return true;
        }

        // Fill NPCID/NPC argument
        else if (arg.matchesPrefix("npc, npcid") && !if_ignore) {
            dB.echoDebug(scriptEntry, "...replacing the linked NPC with " + arg.getValue());
            String value = TagManager.tag(arg.getValue(), new BukkitTagContext(scriptEntry, false));
            dNPC npc = dNPC.valueOf(value);
            if (npc == null || !npc.isValid()) {
                dB.echoError(scriptEntry.getResidingQueue(), value + " is an invalid NPC!");
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
    public dList valueOfFlagdList(String string) {
        FlagManager.Flag flag = DenizenAPI.getCurrentInstance().getFlag(string);
        if (flag == null) {
            return null;
        }
        return new dList(flag.toString(), true, flag.values());
    }

    @Override
    public boolean matchesFlagdList(String arg) {
        boolean flag = false;
        if (arg.startsWith("fl")) {
            if (arg.indexOf('[') == 2) {
                int cb = arg.indexOf(']');
                if (cb > 4 && arg.indexOf('@') == (cb + 1)) {
                    String owner = arg.substring(3, cb);
                    flag = arg.substring(cb + 2).length() > 0 && (dPlayer.matches(owner)
                            || (Depends.citizens != null && dNPC.matches(owner)));
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
            outcome = dLocation.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("material")) {
            outcome = dMaterial.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("materiallist")) {
            outcome = dList.valueOf(comparable).containsObjectsFrom(dMaterial.class);
        }
        else if (comparedto.equalsIgnoreCase("entity")) {
            outcome = dEntity.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("spawnedentity")) {
            outcome = (dEntity.matches(comparable) && dEntity.valueOf(comparable).isSpawned());
        }
        else if (comparedto.equalsIgnoreCase("npc")) {
            outcome = dNPC.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("player")) {
            outcome = dPlayer.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("offlineplayer")) {
            outcome = (dPlayer.valueOf(comparable) != null && !dPlayer.valueOf(comparable).isOnline());
        }
        else if (comparedto.equalsIgnoreCase("onlineplayer")) {
            outcome = (dPlayer.valueOf(comparable) != null && dPlayer.valueOf(comparable).isOnline());
        }
        else if (comparedto.equalsIgnoreCase("item")) {
            outcome = dItem.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("cuboid")) {
            outcome = dCuboid.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("trade")) {
            outcome = dTrade.matches(comparable);
        }
        else {
            dB.echoError("Invalid 'matches' type '" + comparedto + "'!");
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
            dB.echoError("Running not-Spigot?!");
        }
    }

    @Override
    public void postTagExecute() {
        try {
            NMSHandler.getInstance().undisableAsyncCatcher();
        }
        catch (Throwable e) {
            dB.echoError("Running not-Spigot?!");
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
}
