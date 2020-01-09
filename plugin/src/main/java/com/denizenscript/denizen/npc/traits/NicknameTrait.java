package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizencore.tags.TagManager;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

/**
 * <p>Adds the ability to 'nickname' an NPC. This is meant to extend the NPCs real
 * name to perhaps add more description. Similar to a Player's 'Display Name', but better.
 * Inside Denizen, Nicknames can be utilized containing Replaceable TAGs. Outside Denizen,
 * the methods contained in this Trait can be used to get, set, and remove nicknames.</p>
 * <p/>
 * <p>Nicknames should not used as a static reference to an NPC because of the
 * dynamic nature of the Trait. Each time the trait is asked for a nickname, tags are
 * replaced. This allows for nicknames to use FLAGs and other dynamically changing
 * TAGs and have the linked information updated live.</p>
 * <p/>
 * <p>Though not in this Trait class, Denizen also provides some Replaceable TAGs
 * for getting a NPCs nickname. Use <</p>
 */
public class NicknameTrait extends Trait implements Listener {

    @Persist("")
    private String nickname = null;

    public NicknameTrait() {
        super("nickname");
    }

    /**
     * Sets the nickname of this NPC. When setting, dScript TAGS
     * can be used. This included dScript color codes.
     *
     * @param nickName the new nickname for this NPC
     */
    public void setNickname(String nickName) {
        this.nickname = nickName;
    }

    /**
     * Gets the current nickname of this NPC. This may include color codes.
     * Note: To strip color codes, use {@link #getUncoloredNickname()}. If
     * this NPC does not have a nickname, its NPC name is returned instead.
     *
     * @return the nickname for this NPC
     */
    public String getNickname() {
        if (nickname == null || nickname.equals("")) {
            return npc.getName();
        }
        else {
            return TagManager.tag(nickname, // TODO: debug option?
                    new BukkitTagContext(null, DenizenAPI.getDenizenNPC(npc), false, null, true, null));
        }
    }

    /**
     * Gets the current nickname of this NPC and strips out all text colors.
     * To get the colored nickname use {@link #getNickname()}.
     *
     * @return The uncolored nickname for this NPC
     */
    public String getUncoloredNickname() {
        return ChatColor.stripColor(getNickname());
    }

    /**
     * Removes the current nickname from the NPC.
     */
    public void removeNickname() {
        nickname = null;
    }

    /**
     * Checks if the NPC has a nickname set.
     *
     * @return true if NPC has a nickname
     */
    public boolean hasNickname() {
        return (nickname != null);
    }
}
