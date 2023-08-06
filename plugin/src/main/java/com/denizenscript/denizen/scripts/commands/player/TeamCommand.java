package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.ScoreboardHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
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

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        String name = null;
        String prefix = null;
        String suffix = null;
        for (Argument arg : scriptEntry) {
            if (arg.matchesPrefix("id")
                    && !scriptEntry.hasObject("id")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else if (arg.matchesPrefix("name")
                    && !scriptEntry.hasObject("name")) {
                ElementTag nameElement = arg.asElement();
                name = nameElement.asString();
                scriptEntry.addObject("name", nameElement);
            }
            else if (arg.matchesPrefix("add")
                    && !scriptEntry.hasObject("add")) {
                scriptEntry.addObject("add", arg.asType(ListTag.class));
            }
            else if (arg.matchesPrefix("remove")
                    && !scriptEntry.hasObject("remove")) {
                scriptEntry.addObject("remove", arg.asType(ListTag.class));
            }
            else if (arg.matchesPrefix("prefix")
                    && !scriptEntry.hasObject("prefix")) {
                ElementTag prefixElement = arg.asElement();
                prefix = prefixElement.asString();
                scriptEntry.addObject("prefix", prefixElement);
            }
            else if (arg.matchesPrefix("suffix")
                    && !scriptEntry.hasObject("suffix")) {
                ElementTag suffixElement = arg.asElement();
                suffix = suffixElement.asString();
                scriptEntry.addObject("suffix", suffixElement);
            }
            else if (arg.matchesPrefix("color")
                    && arg.matchesEnum(ChatColor.class)
                    && !scriptEntry.hasObject("color")) {
                scriptEntry.addObject("color", arg.asElement());
            }
            else if (arg.matchesPrefix("option")
                    && !scriptEntry.hasObject("option")
                    && (arg.matchesEnum(Team.Option.class)
                    || arg.matches("friendly_fire", "see_invisible"))) {
                scriptEntry.addObject("option", arg.asElement());
            }
            else if (arg.matchesPrefix("status")
                    && !scriptEntry.hasObject("status")
                    && arg.matchesEnum(Team.OptionStatus.class)) {
                scriptEntry.addObject("status", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (name == null || name.length() == 0 || name.length() > 16) {
            throw new InvalidArgumentsException("Must specify a team name between 1 and 16 characters!");
        }
        if (!scriptEntry.hasObject("add") && !scriptEntry.hasObject("remove")
                && !scriptEntry.hasObject("option") && !scriptEntry.hasObject("color")
                && !scriptEntry.hasObject("prefix") && !scriptEntry.hasObject("suffix")) {
            throw new InvalidArgumentsException("Must specify something to do with the team!");
        }
        if ((prefix != null && prefix.length() > 64) || (suffix != null && suffix.length() > 64)) {
            throw new InvalidArgumentsException("Prefixes and suffixes must be 64 characters or less!");
        }
        if (scriptEntry.hasObject("option") != scriptEntry.hasObject("status")) {
            throw new InvalidArgumentsException("Option and Status arguments must go together!");
        }
        scriptEntry.defaultObject("id", new ElementTag("main"));
    }

    public static String translateEntry(String entry, TagContext context) {
        if (entry.startsWith("p@")) {
            PlayerTag player = PlayerTag.valueOf(entry, context);
            if (player != null) {
                return player.getName();
            }
        }
        else if (entry.startsWith("e@")) {
            EntityTag entity = EntityTag.valueOf(entry, context);
            if (entity != null) {
                return entity.getUUID().toString();
            }
        }
        return entry;
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.getElement("id");
        ElementTag name = scriptEntry.getElement("name");
        ListTag add = scriptEntry.getObjectTag("add");
        ListTag remove = scriptEntry.getObjectTag("remove");
        ElementTag prefix = scriptEntry.getElement("prefix");
        ElementTag suffix = scriptEntry.getElement("suffix");
        ElementTag option = scriptEntry.getElement("option");
        ElementTag status = scriptEntry.getElement("status");
        ElementTag color = scriptEntry.getElement("color");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id, name, add, remove, prefix, suffix, color, option, status);
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
        if (add != null) {
            for (String string : add) {
                string = translateEntry(string, scriptEntry.context);
                if (!team.hasEntry(string)) {
                    team.addEntry(string);
                }
            }
        }
        if (remove != null) {
            for (String string : remove) {
                string = translateEntry(string, scriptEntry.context);
                if (team.hasEntry(string)) {
                    team.removeEntry(string);
                }
            }
        }
        if (option != null) {
            String optName = option.asLowerString();
            String statusName = status.asLowerString();
            if (optName.equals("friendly_fire")) {
                team.setAllowFriendlyFire(statusName.equals("always"));
            }
            else if (optName.equals("see_invisible")) {
                team.setCanSeeFriendlyInvisibles(statusName.equals("always"));
            }
            else {
                team.setOption(Team.Option.valueOf(optName.toUpperCase()), Team.OptionStatus.valueOf(statusName.toUpperCase()));
            }
        }
        if (prefix != null) {
            PaperAPITools.instance.setTeamPrefix(team, prefix.asString());
        }
        if (suffix != null) {
            PaperAPITools.instance.setTeamSuffix(team, suffix.asString());
        }
        if (color != null) {
            team.setColor(ChatColor.valueOf(color.asString().toUpperCase()));
        }
        if (team.getEntries().isEmpty()) {
            team.unregister();
        }
    }
}
