package net.aufdemrand.denizen.utilities;

import org.bukkit.entity.Player;

/**
 * @author alkarin
 * version: 1.4
 *
 * This class is strictly used because Bukkit DOES NOT UPDATE experience after enchanting
 * so using player.getTotalExperience() returns an INCORRECT amount
 *
 * Levels based off of 1.3 exp formulas verified by myself.
 * Formulas used.
 * lvl <= 15 : 17*lvl;
 * 15 < lvl < 31 : 17*l + 3*(0.5*l2*(l2+1)), and 17 + 3*l2, where l2 = (l - 16)
 * lvl > 30 : 17*l + 3*(0.5*l2*(l2+1))+4*(0.5*l3*(l3+1)) and
 * 					17+inc, 17 + 3*l2 +4*l3, where l2 = (l-16) and l3=(l-31)
 *
 * The forms you see in the functions are simplifications of the above
 */

public class ExpUtil {

    /**
     * Get the total amount of experience that a player has
     * @param p
     * @return
     */
    public static int getTotalExperience(Player p){
        return getTotalExperience(p.getLevel(),p.getExp());
    }

    /**
     * Get the total amount of experience to a level with a fractional exp bar
     * @param level
     * @param bar
     * @return
     */
    public static int getTotalExperience(int level, double bar){
        return getTotalExpToLevel(level) + (int) (getExpToLevel(level+1)*bar);
    }

    /**
     * Get the total amount of experience needed to get to level
     * @param level
     * @return
     */
    public static int getTotalExpToLevel(int level){
        if (level < 16){
            return 17*level;
        } else if (level < 31){
            return (int) (1.5*level*level -29.5*level+360 );
        } else {
            return (int) (3.5*level*level-151.5*level+2220);
        }
    }
    /**
     * Get the amount of experience needed to go from level -1 to level
     * @param level
     * @return
     */
    public static int getExpToLevel(int level) {
        if (level < 16){
            return 17;
        } else if (level < 31){
            return 3*level - 31;
        } else {
            return 7*level - 155;
        }
    }

    /**
     * Set the total amount of experience for a player
     * @param player
     */
    public static void clearExperience(Player player){
        setTotalExperience(player,0);
    }

    /**
     * Set the total amount of experience for a player
     * @param player
     * @param exp
     */
    public static void setTotalExperience(Player player, int exp){
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
        if (exp > 0)
            player.giveExp(exp);
    }

    /**
     * Set the level of a player
     * @param player
     * @param level
     */
    public static void setLevel(Player player, int level){
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
        if (level > 0)
            player.giveExp(getExpToLevel(level));
    }

    /**
     * Give experience to a player
     * @param player
     * @param exp
     */
    public static void giveExperience(Player player, int exp){
        final int currentExp = getTotalExperience(player);
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
        final int newexp = currentExp + exp;
        if (newexp > 0)
            player.giveExp(newexp);
    }
}

