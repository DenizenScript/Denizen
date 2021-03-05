package com.denizenscript.denizen.utilities.implementation;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.events.bukkit.ScriptReloadEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.scripts.containers.core.*;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.debugging.DebugSubmit;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.maps.DenizenMapManager;
import com.denizenscript.denizencore.DenizenImplementation;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debuggable;
import com.denizenscript.denizencore.utilities.debugging.StrongWarning;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;

public class DenizenCoreImplementation implements DenizenImplementation {

    @Override
    public File getScriptFolder() {
        File file;
        // Get the script directory
        if (Settings.useDefaultScriptPath()) {
            file = new File(Denizen.getInstance().getDataFolder() + File.separator + "scripts");
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
        // Give map image downloads a new chance
        DenizenMapManager.failedUrls.clear();
    }

    @Override
    public void onScriptReload() {
        Depends.setupEconomy();
        Bukkit.getServer().getPluginManager().callEvent(new ScriptReloadEvent());
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
    public boolean needsHandleArgPrefix(String prefix) {
        return prefix.equals("player") || prefix.equals("npc");
    }

    // <--[language]
    // @name The Player and NPC Arguments
    // @group Script Command System
    // @description
    // The "player:<player>" and "npc:<npc>" arguments are special meta-arguments that are available for all commands, but are only useful for some.
    // They are written like:
    // - give stick player:<server.flag[some_player]>
    // or:
    // - assign set script:MyScript npc:<entry[save].created_npc>
    //
    // Denizen tracks a "linked player" and a "linked NPC" in queues and the commands within.
    // Many commands automatically operate on the linked player/NPC or by default or exclusively
    // (for example, "give" defaults to giving items to the linked player but that can be changed with the "to" argument,
    // "assignment" exclusively changes the assignment of the linked NPC, and that cannot be changed except by the global NPC argument).
    //
    // When the player argument is used, it sets the linked player for the specific command it's on.
    // This is only useful for commands that default to operating on the linked player.
    // This can also be useful with the "run" command to link a specific player to the new queue.
    //
    // The NPC argument is essentially equivalent to the player argument, but for the linked NPC instead of the linked player.
    //
    // These arguments will also affect tags (mainly "<player>" and "<npc>") in the same command line (regardless of argument order).
    // If you need to use the original player/NPC in a tag on the same line, use the define command to track it.
    // -->

    public static StrongWarning invalidPlayerArg = new StrongWarning("The 'player:' arg should not be used in commands like define/flag/yaml/... just input the player directly instead.");
    public static StrongWarning invalidNpcArg = new StrongWarning("The 'npc:' arg should not be used in commands like define/flag/yaml/... just input the npc directly instead.");
    public static HashSet<String> invalidPlayerArgCommands = new HashSet<>(Arrays.asList("DEFINE", "FLAG", "YAML"));

    @Override
    public boolean handleCustomArgs(ScriptEntry scriptEntry, Argument arg, boolean if_ignore) {
        // Fill player/off-line player
        if (arg.matchesPrefix("player") && !if_ignore) {
            if (invalidPlayerArgCommands.contains(scriptEntry.getCommandName())) {
                invalidPlayerArg.warn(scriptEntry);
            }
            Debug.echoDebug(scriptEntry, "...replacing the linked player with " + arg.getValue());
            String value = TagManager.tag(arg.getValue(), scriptEntry.getContext());
            PlayerTag player = PlayerTag.valueOf(value, scriptEntry.context);
            if (player == null || !player.isValid()) {
                Debug.echoError(scriptEntry.getResidingQueue(), value + " is an invalid player!");
            }
            ((BukkitScriptEntryData) scriptEntry.entryData).setPlayer(player);
            ((BukkitTagContext) scriptEntry.context).player = player;
            return true;
        }

        // Fill NPC argument
        else if (arg.matchesPrefix("npc") && !if_ignore) {
            if (invalidPlayerArgCommands.contains(scriptEntry.getCommandName())) {
                invalidNpcArg.warn(scriptEntry);
            }
            Debug.echoDebug(scriptEntry, "...replacing the linked NPC with " + arg.getValue());
            String value = TagManager.tag(arg.getValue(), scriptEntry.getContext());
            NPCTag npc = NPCTag.valueOf(value, scriptEntry.context);
            if (npc == null || !npc.isValid()) {
                Debug.echoError(scriptEntry.getResidingQueue(), value + " is an invalid NPC!");
                return false;
            }
            ((BukkitScriptEntryData) scriptEntry.entryData).setNPC(npc);
            ((BukkitTagContext) scriptEntry.context).npc = npc;
            return true;
        }
        return false;
    }

    @Override
    public void refreshScriptContainers() {
        InventoryScriptHelper.inventoryScripts.clear();
        EntityScriptHelper.scripts.clear();
        ItemScriptHelper.item_scripts.clear();
        ItemScriptHelper.item_scripts_by_hash_id.clear();
    }

    @Override
    public String scriptQueueSpeed() {
        return Settings.scriptQueueSpeed();
    }

    @Override
    public AbstractFlagTracker getServerFlags() {
        return Denizen.getInstance().serverFlagMap;
    }

    @Override
    public TagContext getTagContext(ScriptContainer container) {
        return new BukkitTagContext(container);
    }

    @Override
    public TagContext getTagContext(ScriptEntry scriptEntry) {
        return new BukkitTagContext(scriptEntry);
    }

    @Override
    public ScriptEntryData getEmptyScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
    }

    @Override
    public int getTagTimeout() {
        if (!Settings.tagTimeoutUnsafe()) {
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

        // Part of Deprecations.oldMatchesOperator

        boolean outcome = false;

        if (comparedto.equalsIgnoreCase("location")) {
            outcome = LocationTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("material")) {
            outcome = MaterialTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("materiallist")) {
            outcome = ListTag.valueOf(comparable, CoreUtilities.basicContext).containsObjectsFrom(MaterialTag.class);
        }
        else if (comparedto.equalsIgnoreCase("entity")) {
            outcome = EntityTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("spawnedentity")) {
            outcome = (EntityTag.matches(comparable) && EntityTag.valueOf(comparable, CoreUtilities.basicContext).isSpawned());
        }
        else if (comparedto.equalsIgnoreCase("npc")) {
            outcome = NPCTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("player")) {
            outcome = PlayerTag.matches(comparable);
        }
        else if (comparedto.equalsIgnoreCase("offlineplayer")) {
            outcome = (PlayerTag.valueOf(comparable, CoreUtilities.basicContext) != null && !PlayerTag.valueOf(comparable, CoreUtilities.basicContext).isOnline());
        }
        else if (comparedto.equalsIgnoreCase("onlineplayer")) {
            outcome = (PlayerTag.valueOf(comparable, CoreUtilities.basicContext) != null && PlayerTag.valueOf(comparable, CoreUtilities.basicContext).isOnline());
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
    public boolean allowedToWebget() {
        return Settings.allowWebget();
    }

    public static Thread tagThread = null;

    public static boolean isSafeThread() {
        return Bukkit.isPrimaryThread() || Thread.currentThread().equals(tagThread);
    }

    @Override
    public void preTagExecute() {
        try {
            NMSHandler.getInstance().disableAsyncCatcher();
            tagThread = Thread.currentThread();
        }
        catch (Throwable e) {
            Debug.echoError("Running not-Spigot?!");
        }
    }

    @Override
    public void postTagExecute() {
        try {
            NMSHandler.getInstance().undisableAsyncCatcher();
            tagThread = null;
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
        return Denizen.getInstance().getDataFolder();
    }

    @Override
    public boolean allowStrangeYAMLSaves() {
        return Settings.allowStrangeYAMLSaves();
    }

    @Override
    public void startRecording() {
        Debug.record = true;
        Debug.recording = new StringBuilder();
    }

    @Override
    public void stopRecording() {
        Debug.record = false;
        Debug.recording = new StringBuilder();
    }

    @Override
    public void submitRecording(Consumer<String> processResult) {
        if (!Debug.record) {
            processResult.accept("disabled");
            return;
        }
        Debug.record = false;
        final DebugSubmit submit = new DebugSubmit();
        submit.recording = Debug.recording.toString();
        Debug.recording = new StringBuilder();
        submit.start();
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                if (!submit.isAlive()) {
                    this.cancel();
                    if (submit.Result == null) {
                        processResult.accept(null);
                    }
                    else {
                        processResult.accept(submit.Result);
                    }
                }
            }
        };
        task.runTaskTimer(Denizen.getInstance(), 0, 5);
    }

    @Override
    public FlaggableObject simpleWordToFlaggable(String word, ScriptEntry entry) {
        if (CoreUtilities.equalsIgnoreCase(word, "player")) {
            return Utilities.getEntryPlayer(entry);
        }
        if (CoreUtilities.equalsIgnoreCase(word, "npc")) {
            return Utilities.getEntryNPC(entry);
        }
        if (!word.contains("@")) {
            Notable noted = NotableManager.getSavedObject(word);
            if (noted instanceof LocationTag) {
                return (LocationTag) noted;
            }
        }
        ObjectTag obj = ObjectFetcher.pickObjectFor(word, entry.context);
        if (obj instanceof FlaggableObject) {
            return (FlaggableObject) obj;
        }
        return null;
    }
}
