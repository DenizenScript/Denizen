package net.aufdemrand.denizen.scripts.commands.core.IfCommand;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.dLocation;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.ChatColor;

import java.util.List;

public class Comparable {

    public static enum Operator {
        EQUALS,
        MATCHES,
        OR_MORE,
        OR_LESS,
        MORE,
        LESS,
        CONTAINS,
        IS_EMPTY
    }

    public static enum Bridge {
        OR,
        AND,
        FIRST,
        THEN,
        ELSE
    }

    public static enum Logic {
        REGULAR,
        NEGATIVE
    }


    Logic         logic = Logic.REGULAR;
    Bridge       bridge = Bridge.OR;
    Object   comparable = null;
    Operator   operator = Operator.EQUALS;
    Object   comparedto = (boolean) true;
    Boolean     outcome = null;




    private void compare_as_strings() {

        outcome = false;

        String comparable = (String) this.comparable;
        String comparedto = (String) this.comparedto;

        if (comparable == null || comparedto == null) return;

        switch(operator) {

            // For checking if a FLAG is empty.
            case IS_EMPTY:
                outcome = comparable.length() == 0;
                break;

            // For checking straight up if comparable is equal to (ignoring case) comparedto
            case EQUALS:
                outcome = comparable.equalsIgnoreCase(comparedto);
                break;

            // For checking if the comparable contains comparedto
            case CONTAINS:
                outcome = comparable.toLowerCase().contains(comparedto.toLowerCase());
                break;

            // OR_MORE/OR_LESS/etc. deal with the LENGTH of the the comparable/comparedto strings
            case OR_MORE:
                outcome = comparable.length() >= comparedto.length();
                break;

            case OR_LESS:
                outcome = comparable.length() <= comparedto.length();
                break;

            case MORE:
                outcome = comparable.length() > comparedto.length();
                break;

            case LESS:
                outcome = comparable.length() < comparedto.length();
                break;

            // Check if the string comparable MATCHES a specific argument type,
            // as specified by comparedto
            case MATCHES:

                comparedto = comparedto.replace("_", "");

                if (comparedto.equalsIgnoreCase("location"))
                    outcome = dLocation.matches(comparable);

                else if (comparedto.equalsIgnoreCase("pose"))
                    outcome = true; // TODO: outcome = aH.matchesPose(comparable);

                else if (comparedto.equalsIgnoreCase("double"))
                    outcome = aH.matchesDouble(comparable);

                else if (comparedto.equalsIgnoreCase("integer"))
                    outcome = aH.matchesInteger(comparable);

                else if (comparedto.equalsIgnoreCase("even integer"))
                    outcome = aH.matchesInteger(comparable) && (aH.getIntegerFrom(comparable) % 2) == 0;

                else if (comparedto.equalsIgnoreCase("odd integer"))
                    outcome = aH.matchesInteger(comparable) && (aH.getIntegerFrom(comparable) % 2) == 1;

                else if (comparedto.equalsIgnoreCase("duration"))
                    outcome = Duration.matches(comparable);

                else if (comparedto.equalsIgnoreCase("boolean"))
                    outcome = (comparable.equalsIgnoreCase("true") || comparable.equalsIgnoreCase("false"));

                else if (comparedto.equalsIgnoreCase("entity"))
                    outcome = dEntity.matches(comparable);

                else if (comparedto.equalsIgnoreCase("spawnedentity")) {
                    if (dEntity.matches(comparable))
                        outcome = dEntity.valueOf(comparable).isSpawned();
                }

                else if (comparedto.equalsIgnoreCase("entitytype"))
                    outcome = aH.matchesEntityType(comparable);

                else if (comparedto.equalsIgnoreCase("npc"))
                    outcome = dNPC.matches(comparable);

                else if (comparedto.equalsIgnoreCase("player"))
                    outcome = dPlayer.matches(comparable);

                else if (comparedto.equalsIgnoreCase("offlineplayer")) {
                    if (dPlayer.matches(comparable))
                        outcome = !dPlayer.valueOf(comparable).isOnline();
                }

                else if (comparedto.equalsIgnoreCase("onlineplayer")) {
                    if (dPlayer.matches(comparable))
                        outcome = dPlayer.valueOf(comparable).isOnline();
                }

                else if (comparedto.equalsIgnoreCase("item"))
                    outcome = dItem.matches(comparable);

                break;
        }
    }

    public boolean determineOutcome() {

        //
        // Comparable is a STRING
        //
        if (comparable instanceof String) {

            compare_as_strings();
            return outcome;

        }	else if (comparable instanceof List) {
            switch(operator) {
                case CONTAINS:
                    for (String string : ((List<String>) comparable)) {
                        if (comparedto instanceof Integer) {
                            if (aH.getIntegerFrom(string) == (Integer) comparedto) outcome = true;

                        }   else if (comparedto instanceof Double) {
                            if (aH.getDoubleFrom(string) == (Double) comparedto) outcome = true;

                        }	else if (comparedto instanceof Boolean) {
                            if (Boolean.valueOf(string).booleanValue() == ((Boolean) comparedto).booleanValue()) outcome = true;

                        }   else if (comparedto instanceof String) {
                            if (string.equalsIgnoreCase((String) comparedto)) outcome = true;
                        }
                    }
                    break;
                case ORMORE:
                    if (((List<String>) comparable).size() >= (Integer.parseInt(String.valueOf(comparedto)))) outcome = true;
                    break;
                case ORLESS:
                    if (((List<String>) comparable).size() <= (Integer.parseInt(String.valueOf(comparedto)))) outcome = true;
                    break;
                case MORE:
                    if (((List<String>) comparable).size() > (Integer.parseInt(String.valueOf(comparedto)))) outcome = true;
                    break;
                case LESS:
                    if (((List<String>) comparable).size() < (Integer.parseInt(String.valueOf(comparedto)))) outcome = true;
                    break;
            }


            //
            // COMPARABLE IS DOUBLE
            //
        }   else if (comparable instanceof Double) {

            // Check to make sure comparedto is Double
            if (!(comparedto instanceof Double)) {
                // Not comparing with a Double, outcome = false
            } else {

                switch(operator) {
                    case EQUALS:
                        if (((Double) comparable).compareTo((Double) comparedto) == 0) outcome = true;
                        break;
                    case ORMORE:
                        if (((Double) comparable).compareTo((Double) comparedto) >= 0) outcome = true;
                        break;
                    case ORLESS:
                        if (((Double) comparable).compareTo((Double) comparedto) <= 0) outcome = true;
                        break;
                    case MORE:
                        if (((Double) comparable).compareTo((Double) comparedto) > 0) outcome = true;
                        break;
                    case LESS:
                        if (((Double) comparable).compareTo((Double) comparedto) < 0) outcome = true;
                        break;
                }
            }


            //
            // COMPARABLE IS INTEGER
            //
        }	else if (comparable instanceof Integer) {

            // Check to make sure comparedto is Integer
            if (!(comparedto instanceof Integer)) {
                // Not comparing with an Integer, outcome = false;
            } else {
                // Comparing integers.. let's do the logic
                switch(operator) {
                    case EQUALS:
                        if (((Integer) comparable).compareTo((Integer) comparedto) == 0) outcome = true;
                        break;
                    case ORMORE:
                        if (((Integer) comparable).compareTo((Integer) comparedto) >= 0) outcome = true;
                        break;
                    case ORLESS:
                        if (((Integer) comparable).compareTo((Integer) comparedto) <= 0) outcome = true;
                        break;
                    case MORE:
                        if (((Integer) comparable).compareTo((Integer) comparedto) > 0) outcome = true;
                        break;
                    case LESS:
                        if (((Integer) comparable).compareTo((Integer) comparedto) < 0) outcome = true;
                        break;
                }
            }


            //
            // COMPARABLE IS BOOLEAN
            //
        }   else if (comparable instanceof Boolean) {

            // Check to make sure comparedto is Boolean
            if (!(comparedto instanceof Boolean)) {
                // Not comparing with a Boolean, outcome = false;
            } else {
                // Comparing booleans.. let's do the logic
                if ((Boolean) comparable.equals((Boolean) comparedto))
                    outcome = true;
                else
                    outcome = false;
            }
        }

        if (logic == Comparable.Logic.NEGATIVE) outcome = !outcome;

    }













    @Override
    public String toString() {
        return  (logic != Logic.REGULAR ? "Logic='" + logic.toString() + "', " : "")
                + "Comparable='" + (comparable == null ? "null'" : comparable.getClass().getSimpleName()
                + "(" + ChatColor.AQUA + comparable + ChatColor.WHITE + ")'")
                + ", Operator='" + operator.toString()
                + "', ComparedTo='" + (comparedto == null ? "null'" : comparedto.getClass().getSimpleName()
                + "(" + ChatColor.AQUA + comparedto + ChatColor.WHITE + ")' ")
                + ChatColor.YELLOW + "--> OUTCOME='" + outcome + "'";
    }

}
