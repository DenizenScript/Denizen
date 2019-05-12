package net.aufdemrand.denizen.scripts.commands.server;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.BanList;
import org.bukkit.Bukkit;

import java.util.Date;
import java.util.List;

public class BanCommand extends AbstractCommand {

    public enum Actions {
        ADD, REMOVE
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("action") && (arg.matchesPrefix("action")
                    || arg.matchesEnum(Actions.values()))) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("addresses") && arg.matchesPrefix("addresses", "address")) {
                scriptEntry.addObject("addresses", arg.asType(dList.class));
            }
            else if (!scriptEntry.hasObject("targets") && (arg.matchesPrefix("targets", "target")
                    || arg.matchesArgumentList(dPlayer.class))) {
                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("reason") && arg.matchesPrefix("reason")) {
                scriptEntry.addObject("reason", arg.asElement());
            }
            else if (!scriptEntry.hasObject("duration") && (arg.matchesPrefix("duration", "time", "d", "expiration")
                    || arg.matchesArgumentType(Duration.class))) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            else if (!scriptEntry.hasObject("source") && arg.matchesPrefix("source")) {
                scriptEntry.addObject("source", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }

        scriptEntry.defaultObject("action", new Element("add"))
                .defaultObject("reason", new Element("Banned."))
                .defaultObject("source", new Element("(Unknown)"));

        if (Actions.valueOf(scriptEntry.getObject("action").toString().toUpperCase()) == null) {
            throw new IllegalArgumentException("Invalid action specified.");
        }

        if ((!scriptEntry.hasObject("targets") || ((List<dPlayer>) scriptEntry.getObject("targets")).isEmpty())
                && (!scriptEntry.hasObject("addresses") || ((List<Element>) scriptEntry.getObject("addresses")).isEmpty())) {
            throw new IllegalArgumentException("Must specify a valid target or address!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        Element action = scriptEntry.getElement("action");
        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");
        dList addresses = (dList) scriptEntry.getObject("addresses");
        Element reason = scriptEntry.getElement("reason");
        Duration duration = scriptEntry.getdObject("duration");
        Element source = scriptEntry.getElement("source");

        Date expiration = null;
        if (duration != null && duration.getTicks() != 0) {
            expiration = new Date(new Duration(System.currentTimeMillis() / 50 + duration.getTicks()).getTicks() * 50);
        }

        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    action.debug() +
                            (targets != null ? aH.debugObj("targets", targets) : "") +
                            (addresses != null ? addresses.debug() : "") +
                            reason.debug() +
                            (duration != null ? duration.debug() : "") +
                            source.debug());
        }

        Actions banAction = Actions.valueOf(action.toString().toUpperCase());

        switch (banAction) {
            case ADD:
                if (targets != null) {
                    for (dPlayer player : targets) {
                        if (player.isValid()) {
                            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason.toString(), expiration, source.toString());
                            if (player.isOnline()) {
                                player.getPlayerEntity().kickPlayer(reason.toString());
                            }
                        }
                    }
                }
                if (addresses != null) {
                    for (String address : addresses) {
                        Bukkit.getBanList(BanList.Type.IP).addBan(address, reason.toString(), expiration, source.toString());
                    }
                }
                break;

            case REMOVE:
                if (targets != null) {
                    for (dPlayer player : targets) {
                        if (player.isValid()) {
                            if (player.getOfflinePlayer().isBanned()) {
                                Bukkit.getBanList(BanList.Type.NAME).pardon(player.getName());
                            }
                        }
                    }
                }
                if (addresses != null) {
                    for (String address : addresses) {
                        Bukkit.getBanList(BanList.Type.IP).pardon(address);
                    }
                }
                break;

        }

    }

}
