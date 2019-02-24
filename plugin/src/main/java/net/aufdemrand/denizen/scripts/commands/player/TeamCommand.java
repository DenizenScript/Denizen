package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.ScoreboardHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        String name = null;
        String prefix = null;
        String suffix = null;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesPrefix("id")
                    && !scriptEntry.hasObject("id")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else if (arg.matchesPrefix("name")
                    && !scriptEntry.hasObject("name")) {
                Element nameElement = arg.asElement();
                name = nameElement.asString();
                scriptEntry.addObject("name", nameElement);
            }
            else if (arg.matchesPrefix("add")
                    && !scriptEntry.hasObject("add")) {
                scriptEntry.addObject("add", arg.asType(dList.class));
            }
            else if (arg.matchesPrefix("remove")
                    && !scriptEntry.hasObject("remove")) {
                scriptEntry.addObject("remove", arg.asType(dList.class));
            }
            else if (arg.matchesPrefix("prefix")
                    && !scriptEntry.hasObject("prefix")) {
                Element prefixElement = arg.asElement();
                prefix = prefixElement.asString();
                scriptEntry.addObject("prefix", prefixElement);
            }
            else if (arg.matchesPrefix("suffix")
                    && !scriptEntry.hasObject("suffix")) {
                Element suffixElement = arg.asElement();
                suffix = suffixElement.asString();
                scriptEntry.addObject("suffix", suffixElement);
            }

        }

        if (name == null || name.length() == 0 || name.length() > 16) {
            throw new InvalidArgumentsException("Must specify a team name between 1 and 16 characters!");
        }

        if (!scriptEntry.hasObject("add") && !scriptEntry.hasObject("remove")
                && !scriptEntry.hasObject("prefix") && !scriptEntry.hasObject("suffix")) {
            throw new InvalidArgumentsException("Must specify something to do with the team!");
        }

        if ((prefix != null && prefix.length() > 16) || (suffix != null && suffix.length() > 16)) {
            throw new InvalidArgumentsException("Prefixes and suffixes must be 16 characters or less!");
        }

        scriptEntry.defaultObject("id", new Element("main"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element id = scriptEntry.getElement("id");
        Element name = scriptEntry.getElement("name");
        dList add = scriptEntry.getdObject("add");
        dList remove = scriptEntry.getdObject("remove");
        Element prefix = scriptEntry.getElement("prefix");
        Element suffix = scriptEntry.getElement("suffix");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    id.debug() +
                            name.debug() +
                            (add != null ? add.debug() : "") +
                            (remove != null ? remove.debug() : "") +
                            (prefix != null ? prefix.debug() : "") +
                            (suffix != null ? suffix.debug() : ""));

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
            team = board.registerNewTeam(name.asString());
        }

        if (add != null) {
            for (String string : add) {
                if (string.startsWith("p@")) {
                    string = dPlayer.valueOf(string).getName();
                }
                if (!team.hasEntry(string)) {
                    team.addEntry(string);
                }
            }
        }

        if (remove != null) {
            for (String string : remove) {
                if (string.startsWith("p@")) {
                    string = dPlayer.valueOf(string).getName();
                }
                if (team.hasEntry(string)) {
                    team.removeEntry(string);
                }
            }
        }

        if (prefix != null) {
            team.setPrefix(prefix.asString());
        }

        if (suffix != null) {
            team.setSuffix(suffix.asString());
        }

        if (team.getEntries().isEmpty()) {
            team.unregister();
        }
    }
}
