package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.ScoreboardHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamCommand extends AbstractCommand {

    public TeamCommand() {
        setName("team");
        setSyntax("team (id:<scoreboard>/{main}) [name:<team>] (add:<entry>|...) (remove:<entry>|...) (prefix:<prefix>) (suffix:<suffix>) (option:<type> status:<status>) (color:<color>)");
        setRequiredArguments(2, 9);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Team
    // @Syntax team (id:<scoreboard>/{main}) [name:<team>] (add:<entry>|...) (remove:<entry>|...) (prefix:<prefix>) (suffix:<suffix>) (option:<type> status:<status>) (color:<color>)
    // @Required 2
    // @Maximum 9
    // @Short Controls scoreboard teams.
    // @Group player
    //
    // @Description
    // The Team command allows you to control a scoreboard team.
    //
    // Use the "prefix" or "suffix" arguments to modify a team's playername prefix and suffix.
    //
    // The "entry" value can be a player's name to affect that player, or an entity's UUID to affect that entity.
    // You can alternately input a raw PlayerTag or EntityTag, and they will be automatically translated to the name/UUID internally.
    //
    // Use the "color" argument to set the team color (for glowing, names, etc). Must be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/ChatColor.html>.
    //
    // Use the "add" and "remove" arguments to add or remove players by name to/from the team.
    //
    // Use the "option" and "status" arguments together to set a team option's status.
    // Option can be "COLLISION_RULE", "DEATH_MESSAGE_VISIBILITY", or "NAME_TAG_VISIBILITY", with status "ALWAYS", "FOR_OTHER_TEAMS", "FOR_OWN_TEAM", or "NEVER".
    // Option can instead be "FRIENDLY_FIRE" or "SEE_INVISIBLE", only allowing status "ALWAYS" or "NEVER".
    //
    // @Tags
    // <server.scoreboard[(<board>)].team[<team>].members>
    //
    // @Usage
    // Use to add a player to a team.
    // - team name:red add:<player>
    //
    // @Usage
    // Use to add some mob to a team.
    // - team name:blue add:<player.location.find_entities[monster].within[10]>
    //
    // @Usage
    // Use to change the prefix for a team.
    // - team name:red "prefix:[<red>Red Team<reset>]"
    //
    // @Usage
    // Use to hide nameplates for members of a team.
    // - team name:red option:name_tag_visibility status:never
    // -->

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("id") @ArgPrefixed @ArgDefaultText("main") ElementTag id,
                                   @ArgName("name") @ArgPrefixed @ArgDefaultNull ElementTag name,
                                   @ArgName("add") @ArgPrefixed @ArgDefaultNull ListTag addEntities,
                                   @ArgName("remove") @ArgPrefixed @ArgDefaultNull ListTag removeEntities,
                                   @ArgName("prefix") @ArgPrefixed @ArgDefaultNull ElementTag prefix,
                                   @ArgName("suffix") @ArgPrefixed @ArgDefaultNull ElementTag suffix,
                                   @ArgName("option") @ArgPrefixed @ArgDefaultNull ElementTag option,
                                   @ArgName("status") @ArgPrefixed @ArgDefaultNull Team.OptionStatus status,
                                   @ArgName("color") @ArgPrefixed @ArgDefaultNull ChatColor color) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && name.asString().length() > 16) {
            throw new InvalidArgumentsRuntimeException("Must specify a team name between 1 and 16 characters!");
        }
        if (addEntities == null && removeEntities == null && option == null && color == null && prefix == null && suffix == null) {
            throw new InvalidArgumentsRuntimeException("Must specify something to do with the team!");
        }
        if ((option == null) != (status == null)) {
            throw new InvalidArgumentsRuntimeException("Option and Status arguments must go together!");
        }
        Scoreboard board;
        if (id.asString().equalsIgnoreCase("main")) {
            board = ScoreboardHelper.getMain();
        }
        else {
            if (ScoreboardHelper.hasScoreboard(id.asString())) {
                board = ScoreboardHelper.getScoreboard(id.asString());
            }
            else {
                board = ScoreboardHelper.createScoreboard(id.asString());
            }
        }
        Team team = board.getTeam(name.asString());
        if (team == null) {
            String low = name.asLowerString();
            team = board.getTeams().stream().filter(t -> CoreUtilities.toLowerCase(t.getName()).equals(low)).findFirst().orElse(null);
            if (team == null) {
                team = board.registerNewTeam(name.asString());
            }
        }
        if (removeEntities != null) {
            for (ObjectTag obj : removeEntities.objectForms) {
                String remove = translateEntry(obj, scriptEntry.context);
                if (remove != null) {
                    team.removeEntry(remove);
                }
            }
        }
        if (addEntities != null) {
            for (ObjectTag obj : addEntities.objectForms) {
                String add = translateEntry(obj, scriptEntry.context);
                if (add != null) {
                    team.addEntry(add);
                }
            }
        }
        if (option != null) {
            switch (option.asLowerString()) {
                case "friendly_fire" -> {
                    team.setAllowFriendlyFire(status == Team.OptionStatus.ALWAYS);
                }
                case "see_invisible" -> {
                    team.setCanSeeFriendlyInvisibles(status == Team.OptionStatus.ALWAYS);
                }
                default -> {
                    if (option.matchesEnum(Team.Option.class)) {
                        team.setOption(option.asEnum(Team.Option.class), status);
                    }
                    else {
                        throw new InvalidArgumentsRuntimeException("Option doesn't exist!");
                    }
                }
            }
        }
        if (prefix != null) {
            PaperAPITools.instance.setTeamPrefix(team, prefix.asString());
        }
        if (suffix != null) {
            PaperAPITools.instance.setTeamSuffix(team, suffix.asString());
        }
        if (color != null) {
            team.setColor(color);
        }
        if (team.getEntries().isEmpty()) {
            team.unregister();
        }
    }

    public static String translateEntry(ObjectTag obj, TagContext context) {
        if (obj.shouldBeType(PlayerTag.class)) {
            return obj.asType(PlayerTag.class, context).getName();
        }
        else if (obj.shouldBeType(EntityTag.class)) {
            return obj.asType(EntityTag.class, context).getUUID().toString();
        }
        else {
            return obj.toString();
        }
    }
}
