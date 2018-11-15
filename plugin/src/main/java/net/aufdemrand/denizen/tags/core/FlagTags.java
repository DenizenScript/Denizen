package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.flags.FlagManager.Value;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;

import java.text.DecimalFormat;

@Deprecated
public class FlagTags {

    Denizen denizen;

    public FlagTags(Denizen denizen) {
        this.denizen = denizen;
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                flagTag(event);
            }
        }, "flag");
    }

    private enum ReplaceType {LENGTH, SIZE, ASSTRING, ABS, ASINT, ASDOUBLE, ASLIST, ASMONEY, ASPLAYERLIST, ASNPCLIST, ASCSLIST, ISEXPIRED, EXPIRATION}

    /**
     * Replaces FLAG TAGs. Called automatically by the dScript ScriptBuilder and Executer.
     *
     * @param event ReplaceableTagEvent
     */

    public void flagTag(ReplaceableTagEvent event) {
        if (!event.matches("flag")) {
            return;
        }

        if (!event.hasValue()) {
            return;
        }

        dB.echoError(event.getAttributes().getScriptEntry().getResidingQueue(), "flag.x: tags are deprecated! Use <x.flag[]>, EG, <global.flag[name]>!");

        // Replace <FLAG...> TAGs.
        String flagName = event.getValue().split(":").length > 1
                ? event.getValue().split(":")[0].toUpperCase() : event.getValue().toUpperCase();
        String flagFallback = event.hasAlternative() ? event.getAlternative().toString() : "EMPTY";
        int index = -1;
        ReplaceType replaceType = ReplaceType.ASSTRING;

        // Get format, if specified
        if (flagName.contains(".")) {
            try {
                int replaceTypeIndex = flagName.split("\\.").length - 1;
                replaceType = ReplaceType.valueOf(flagName.split("\\.")[replaceTypeIndex].replace("_", "").toUpperCase());
                flagName = flagName.replace("." + flagName.split("\\.")[replaceTypeIndex], "");
            }
            catch (Exception e) {
                replaceType = ReplaceType.ASSTRING;
            }
        }

        // Get index, if specified
        // TODO: Remove this code at 1.0 release. This call been deprecated and is now removed!
        // if (event.hasValueContext()) {
        //    try {
        //        index = Integer.valueOf(event.getValueContext());
        //    } catch (NumberFormatException e) { index = -1; }
        // }

        // Check flag replacement type
        if (event.getType().toUpperCase().startsWith("G")) {
            if (denizen.flagManager().getGlobalFlag(flagName).get(index).isEmpty()) {
                // dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' flag not found, using fallback!", flagName);
            }
            else {
                FlagManager.Flag flag = denizen.flagManager().getGlobalFlag(flagName);
                event.setReplaced(getReplaceable(flag, flag.get(index), replaceType));
                // dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value '" + event.getReplaced() + "'.", flagName);
            }

        }
        else if (event.getType().toUpperCase().startsWith("D") || event.getType().toUpperCase().startsWith("N")) {
            if (denizen.flagManager().getNPCFlag(((BukkitTagContext) event.getContext()).npc.getId(), flagName).get(index).isEmpty()) {
                // dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' flag not found, using fallback!", flagName);
            }
            else {
                FlagManager.Flag flag = denizen.flagManager().getNPCFlag(((BukkitTagContext) event.getContext()).npc.getId(), flagName);
                event.setReplaced(getReplaceable(flag, flag.get(index), replaceType));
                // dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value '" + event.getReplaced() + "'.", flagName);
            }

        }
        else if (event.getType().toUpperCase().startsWith("P")) {
            // Separate name since subType context may specify a different (or offline) player
            dPlayer player = ((BukkitTagContext) event.getContext()).player;

            // No name? No flag replacement!
            if (player == null) {
                return;
            }

            if (denizen.flagManager().getPlayerFlag(player, flagName).get(index).isEmpty()) {
                if (replaceType.toString().equals("ISEXPIRED")) {
                    event.setReplaced("true");
                }
            }
            else {
                FlagManager.Flag flag = denizen.flagManager().getPlayerFlag(player, flagName);
                event.setReplaced(getReplaceable(flag, flag.get(index), replaceType));
                // dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value '" + event.getReplaced() + "'.", flagName);

            }
        }
    }

    private String getReplaceable(FlagManager.Flag flag, Value value, ReplaceType replaceType) {
        switch (replaceType) {
            case ASINT:
                return String.valueOf(value.asInteger());
            case ASDOUBLE:
                return String.valueOf(value.asDouble());
            case ABS:
                return String.valueOf(Math.abs(value.asDouble()));
            case ASSTRING:
                return value.asString();
            case ASLIST:
                return String.valueOf(value.asList());
            case ASPLAYERLIST:
                return String.valueOf(value.asList("p@"));
            case ASNPCLIST:
                return String.valueOf(value.asList("n@"));
            case ASCSLIST:
                return String.valueOf(value.asCommaSeparatedList());
            case ASMONEY:
                DecimalFormat d = new DecimalFormat("0.00");
                return String.valueOf(d.format(value.asDouble()));
            case LENGTH:
                return String.valueOf(value.asString().length());
            case SIZE:
                return String.valueOf(value.asSize());
            case ISEXPIRED:
                return String.valueOf(flag.checkExpired());
            case EXPIRATION:
                return String.valueOf(flag.expirationTime());
        }
        return null;
    }
}
