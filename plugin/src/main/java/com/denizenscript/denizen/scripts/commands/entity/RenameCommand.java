package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityFormObject;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class RenameCommand extends AbstractCommand {

    public RenameCommand() {
        setName("rename");
        setSyntax("rename [<name>/cancel] (t:<entity>|...) (per_player) (for:<player>|...) (list_name_only)");
        setRequiredArguments(1, 5);
        setParseArgs(false);
        isProcedural = false;
    }

    // <--[command]
    // @Name Rename
    // @Syntax rename [<name>/cancel] (t:<entity>|...) (per_player) (for:<player>|...) (list_name_only)
    // @Required 1
    // @Maximum 5
    // @Short Renames the linked NPC or list of entities.
    // @Group entity
    //
    // @Description
    // Renames the linked NPC or list of entities.
    // Functions like the '/npc rename' command.
    //
    // Can rename a spawned or unspawned NPC to any name up to 256 characters.
    //
    // Can rename a vanilla entity to any name up to 256 characters, and will automatically make the nameplate visible.
    //
    // Can rename a player to any name up to 16 characters. This will affect only the player's nameplate.
    //
    // Optionally specify 'per_player' to reprocess the input tags for each player when renaming a vanilla entity
    // (meaning, if you use "- rename <player.name> t:<[someent]> per_player", every player will see their own name on that entity).
    // A per_player rename will remain active until the entity is renamed again or the server is restarted.
    // Rename to "cancel" per_player to intentionally end a per_player rename.
    // Optionally specify "for:" a list of players when using per_player.
    //
    // Optionally specify 'list_name_only' to only change the tab list name for a player. Works with 'per_player'.
    //
    // @Tags
    // <EntityTag.name>
    // <NPCTag.nickname>
    //
    // @Usage
    // Use to rename the linked NPC to 'Bob'.
    // - rename Bob
    //
    // @Usage
    // Use to rename a different NPC to 'Bob'.
    // - rename Bob t:<[some_npc]>
    //
    // @Usage
    // Use to make an entity show players their own name for 10 seconds.
    // - rename <green><player.name> t:<[some_entity]> per_player
    // - wait 10s
    // - rename cancel t:<[some_entity]> per_player
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("t", "target", "targets")) {
                scriptEntry.addObject("targets", ListTag.getListFor(TagManager.tagObject(arg.getValue(), scriptEntry.getContext()), scriptEntry.getContext()));
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("for")) {
                scriptEntry.addObject("players", ListTag.getListFor(TagManager.tagObject(arg.getValue(), scriptEntry.getContext()), scriptEntry.getContext()).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("per_player")
                    && arg.matches("per_player")) {
                scriptEntry.addObject("per_player", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("list_name_only")
                    && arg.matches("list_name_only")) {
                scriptEntry.addObject("list_name_only", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("name")) {
                scriptEntry.addObject("name", arg.getRawElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("name")) {
            throw new InvalidArgumentsException("Must specify a name!");
        }
        if (!scriptEntry.hasObject("targets")) {
            if (Utilities.getEntryNPC(scriptEntry) == null || !Utilities.getEntryNPC(scriptEntry).isValid()) {
                throw new InvalidArgumentsException("Must have an NPC attached, or specify a list of targets to rename!");
            }
            scriptEntry.addObject("targets", new ListTag(Utilities.getEntryNPC(scriptEntry)));
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        final ElementTag name = scriptEntry.getElement("name");
        ElementTag perPlayer = scriptEntry.getElement("per_player");
        ElementTag listNameOnly = scriptEntry.getElement("list_name_only");
        ListTag targets = scriptEntry.getObjectTag("targets");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        if (perPlayer != null && perPlayer.asBoolean()) {
            NetworkInterceptHelper.enable();
            if (scriptEntry.dbCallShouldDebug()) {
                Debug.report(scriptEntry, getName(), name, targets, perPlayer, listNameOnly, db("for", players));
            }
            for (ObjectTag target : targets.objectForms) {
                EntityTag entity = target.asType(EntityTag.class, CoreUtilities.noDebugContext);
                if (entity != null) {
                    Entity bukkitEntity = entity.getBukkitEntity();
                    if (bukkitEntity == null) {
                        Debug.echoError("Invalid entity in rename command.");
                        continue;
                    }
                    if (name.asString().equals("cancel")) {
                        customNames.remove(bukkitEntity.getUniqueId());
                        if (bukkitEntity.isCustomNameVisible()) {
                            if (players == null) {
                                for (Player player : NMSHandler.entityHelper.getPlayersThatSee(bukkitEntity)) {
                                    NMSHandler.packetHelper.sendRename(player, bukkitEntity, bukkitEntity.getCustomName(), false);
                                }
                            }
                            else {
                                for (PlayerTag player : players) {
                                    NMSHandler.packetHelper.sendRename(player.getPlayerEntity(), bukkitEntity, bukkitEntity.getCustomName(), false);
                                }
                            }
                        }
                        else {
                            bukkitEntity.setCustomNameVisible(true);
                            bukkitEntity.setCustomNameVisible(false); // Force a metadata update
                        }
                    }
                    else {
                        final BukkitTagContext originalContext = (BukkitTagContext) scriptEntry.context.clone();
                        HashMap<UUID, RenameData> playerToFuncMap = customNames.computeIfAbsent(bukkitEntity.getUniqueId(), k -> new HashMap<>());
                        Function<Player, String> nameGetter = p -> {
                            originalContext.player = new PlayerTag(p);
                            return TagManager.tag(name.asString(), originalContext);
                        };
                        RenameData renamer = new RenameData();
                        renamer.nameFunction = nameGetter;
                        renamer.listOnly = listNameOnly != null && listNameOnly.asBoolean();
                        if (players == null) {
                            playerToFuncMap.put(null, renamer);
                        }
                        else {
                            for (PlayerTag player : players) {
                                playerToFuncMap.put(player.getUUID(), renamer);
                            }
                        }
                        if (players == null) {
                            for (Player player : NMSHandler.entityHelper.getPlayersThatSee(bukkitEntity)) {
                                NMSHandler.packetHelper.sendRename(player, bukkitEntity, "", renamer.listOnly);
                            }
                        }
                        else {
                            for (PlayerTag player : players) {
                                NMSHandler.packetHelper.sendRename(player.getPlayerEntity(), bukkitEntity, "", renamer.listOnly);
                            }
                        }
                    }
                }
            }
            return;
        }
        String nameString = TagManager.tag(name.asString(), scriptEntry.context);
        if (nameString.length() > 256) {
            nameString = nameString.substring(0, 256);
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("name", nameString), listNameOnly, targets);
        }
        for (ObjectTag target : targets.objectForms) {
            EntityFormObject entity = target.asType(EntityTag.class, CoreUtilities.noDebugContext);
            if (entity == null) {
                entity = target.asType(NPCTag.class, scriptEntry.context);
            }
            else {
                entity = ((EntityTag) entity).getDenizenObject();
            }
            if (entity == null) {
                Debug.echoError("Invalid entity in rename command.");
                continue;
            }
            if (entity instanceof NPCTag) {
                NPC npc = ((NPCTag) entity).getCitizen();
                if (npc.isSpawned()) {
                    Location prev = npc.getStoredLocation().clone();
                    npc.despawn(DespawnReason.PENDING_RESPAWN);
                    npc.setName(nameString);
                    npc.spawn(prev);
                }
                else {
                    npc.setName(nameString);
                }
            }
            else if (entity instanceof PlayerTag) {
                if (listNameOnly != null && listNameOnly.asBoolean()) {
                    PaperAPITools.instance.setPlayerListName(((PlayerTag) entity).getPlayerEntity(), nameString);
                }
                else {
                    String limitedName = nameString.length() > 16 ? nameString.substring(0, 16) : nameString;
                    NMSHandler.instance.getProfileEditor().setPlayerName(((PlayerTag) entity).getPlayerEntity(), limitedName);
                }
            }
            else {
                Entity bukkitEntity = entity.getDenizenEntity().getBukkitEntity();
                customNames.remove(bukkitEntity.getUniqueId());
                bukkitEntity.setCustomName(nameString);
                bukkitEntity.setCustomNameVisible(true);
            }
        }
    }

    public static HashMap<UUID, HashMap<UUID, RenameData>> customNames = new HashMap<>();

    public static class RenameData {

        public Function<Player, String> nameFunction;

        public boolean listOnly;
    }

    public static boolean hasAnyDynamicRenames() {
        return !customNames.isEmpty();
    }

    public static void addDynamicRename(Entity bukkitEntity, Player forPlayer, RenameData rename) {
        NetworkInterceptHelper.enable();
        HashMap<UUID, RenameData> playerToFuncMap = customNames.computeIfAbsent(bukkitEntity.getUniqueId(), k -> new HashMap<>());
        playerToFuncMap.put(forPlayer == null ? null : forPlayer.getUniqueId(), rename);
        if (forPlayer == null) {
            for (Player player : NMSHandler.entityHelper.getPlayersThatSee(bukkitEntity)) {
                NMSHandler.packetHelper.sendRename(player, bukkitEntity, "", rename.listOnly);
            }
        }
        else {
            NMSHandler.packetHelper.sendRename(forPlayer, bukkitEntity, "", rename.listOnly);
        }
    }

    public static String getCustomNameFor(UUID entityId, Player player, boolean isForList) {
        HashMap<UUID, RenameData> map = customNames.get(entityId);
        if (map == null) {
            return null;
        }
        RenameData rename = map.get(player.getUniqueId());
        if (rename == null || (rename.listOnly && !isForList)) {
            rename = map.get(null);
            if (rename == null || (rename.listOnly && !isForList)) {
                return null;
            }
        }
        return rename.nameFunction.apply(player);
    }
}
