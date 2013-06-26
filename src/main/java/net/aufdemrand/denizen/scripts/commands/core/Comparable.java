package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;

import java.util.List;

public class Comparable {

    public static enum Operator {
        EQUALS, MATCHES, OR_MORE, OR_LESS, MORE,
        LESS, CONTAINS, IS_EMPTY
    }

    public static enum Bridge {
        OR, AND, FIRST, THEN, ELSE
    }

    public static enum Logic {
        REGULAR, NEGATIVE
    }

    Logic         logic = Logic.REGULAR;
    Bridge       bridge = Bridge.FIRST;
    Object   comparable = null;
    Operator   operator = Operator.EQUALS;
    Object   comparedto = (boolean) true;
    Boolean     outcome = null;


    public void setNegativeLogic() {
        logic = Logic.NEGATIVE;
    }


    public void setComparable(String arg) {

        // If a Number
        if (aH.matchesDouble(arg) || aH.matchesInteger(arg))
            comparable = aH.getDoubleFrom(arg);

            // If a Boolean
        else if (arg.equalsIgnoreCase("true")) comparable = true;
        else if (arg.equalsIgnoreCase("false")) comparable = false;

            // If a List<Object>
        else if (dList.matches(arg)) {
            comparable = dList.valueOf(arg);
        }

        // If none of the above, must be a String! :D
        // 'arg' is already a String.
        else comparable = arg;
    }


    public void setComparedto(String arg) {

        // If MATCHES, change comparable to String
        if (operator == Comparable.Operator.MATCHES)
            comparable = String.valueOf(comparable);

        // Comparable is String, return String
        if (comparable instanceof String)
            comparedto = arg;

            // Comparable is a Number, return Double
        else if (comparable instanceof Double) {
            if (aH.matchesDouble(arg) || aH.matchesInteger(arg))
                comparedto = aH.getDoubleFrom(arg);
            else {
                dB.echoDebug(ChatColor.YELLOW + "WARNING! " + ChatColor.WHITE + "Cannot compare NUMBER("
                        + comparable + ") with '" + arg + "'. Outcome for this Comparable will be false.");
                comparedto = Double.NaN;
            }
        }

        else if (comparable instanceof Boolean) {
            comparedto = aH.getBooleanFrom(arg);
        }

        else if (comparable instanceof dList) {
            if (dList.matches(arg))
                comparedto = dList.valueOf(arg);
        }

        else comparedto = arg;
    }


    public boolean determineOutcome() {

        outcome = false;

        if (comparable instanceof String) {
            compare_as_strings();
            return outcome;
        }

        else if (comparable instanceof dList) {
            compare_as_list();
            return outcome;
        }

        else if (comparable instanceof Double) {
            compare_as_numbers();
            return outcome;
        }

        else if (comparable instanceof Boolean) {

            // Check to make sure comparedto is Boolean
            if (!(comparedto instanceof Boolean)) {
                // Not comparing with a Boolean, outcome = false;
            } else {
                // Comparing booleans.. let's do the logic
                if (comparable.equals(comparedto))
                    outcome = true;
                else
                    outcome = false;
            }
        }

        if (logic == Comparable.Logic.NEGATIVE) outcome = !outcome;

        return outcome;
    }


    private void compare_as_numbers() {

        outcome = false;

        Double comparable = (Double) this.comparable;
        Double comparedto = (Double) this.comparedto;

        switch(operator) {

            case EQUALS:
                if (comparable.compareTo(comparedto) == 0) outcome = true;
                break;

            case OR_MORE:
                if (comparable.compareTo(comparedto) >= 0) outcome = true;
                break;

            case OR_LESS:
                if (comparable.compareTo(comparedto) <= 0) outcome = true;
                break;

            case MORE:
                if (comparable.compareTo(comparedto) > 0) outcome = true;
                break;

            case LESS:
                if (comparable.compareTo(comparedto) < 0) outcome = true;
                break;
        }

    }


    private void compare_as_list() {
        outcome = false;

        dList comparable = (dList) this.comparable;

        switch(operator) {

            case CONTAINS:
                for (String string : comparable) {
                    if (comparedto instanceof Integer) {
                        if (aH.matchesInteger(string)
                                && aH.getIntegerFrom(string) == (Integer) comparedto)
                            outcome = true;
                    }   else if (comparedto instanceof Double) {
                        if (aH.matchesDouble(string) &&
                                aH.getDoubleFrom(string) == (Double) comparedto)
                            outcome = true;
                    }   else if (comparedto instanceof String) {
                        if (string.equalsIgnoreCase((String) comparedto))
                            outcome = true;
                    }
                }
                break;

            case OR_MORE:
                if (!(comparedto instanceof Double)) break;
                outcome = (comparable.size() >= ((Double) comparedto).intValue());
                break;

            case OR_LESS:
                if (!(comparedto instanceof Double)) break;
                outcome = (comparable.size() <= ((Double) comparedto).intValue());
                break;

            case MORE:
                if (!(comparedto instanceof Double)) break;
                outcome = (comparable.size() > ((Double) comparedto).intValue());
                break;

            case LESS:
                if (!(comparedto instanceof Double)) break;
                outcome = (comparable.size() < ((Double) comparedto).intValue());
                break;

            case EQUALS:
                if (comparedto instanceof dList)
                    outcome = ((dList) comparedto).containsAll(comparable);
                break;
        }

    }



    private void compare_as_strings() {

        outcome = false;

        String comparable = String.valueOf(this.comparable);
        String comparedto = String.valueOf(this.comparedto);

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

                else if (comparedto.equalsIgnoreCase("entity"))
                    outcome = dEntity.matches(comparable);

                else if (comparedto.equalsIgnoreCase("spawnedentity"))
                    outcome = (dEntity.matches(comparable) && dEntity.valueOf(comparable).isSpawned());

                else if (comparedto.equalsIgnoreCase("entitytype"))
                    outcome = aH.matchesEntityType(comparable);

                else if (comparedto.equalsIgnoreCase("npc"))
                    outcome = dNPC.matches(comparable);

                else if (comparedto.equalsIgnoreCase("player"))
                    outcome = dPlayer.matches(comparable);

                else if (comparedto.equalsIgnoreCase("offlineplayer"))
                    outcome = (dPlayer.matches(comparable) && !dPlayer.valueOf(comparable).isOnline());

                else if (comparedto.equalsIgnoreCase("onlineplayer"))
                    outcome = (dPlayer.matches(comparable) && dPlayer.valueOf(comparable).isOnline());

                else if (comparedto.equalsIgnoreCase("item"))
                    outcome = dItem.matches(comparable);

                else if (comparedto.equalsIgnoreCase("pose"))
                    outcome = true; // TODO: outcome = aH.matchesPose(comparable);

                else if (comparedto.equalsIgnoreCase("duration"))
                    outcome = Duration.matches(comparable);

                    // Use aH on primitives

                else if (comparedto.equalsIgnoreCase("double"))
                    outcome = aH.matchesDouble(comparable);

                else if (comparedto.equalsIgnoreCase("integer"))
                    outcome = aH.matchesInteger(comparable);

                else if (comparedto.equalsIgnoreCase("even integer"))
                    outcome = aH.matchesInteger(comparable) && (aH.getIntegerFrom(comparable) % 2) == 0;

                else if (comparedto.equalsIgnoreCase("odd integer"))
                    outcome = aH.matchesInteger(comparable) && (aH.getIntegerFrom(comparable) % 2) == 1;

                else if (comparedto.equalsIgnoreCase("boolean"))
                    outcome = (comparable.equalsIgnoreCase("true") || comparable.equalsIgnoreCase("false"));

                break;
        }
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
