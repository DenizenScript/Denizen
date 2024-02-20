package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.scripts.commands.generator.ArgSubType;
import net.citizensnpcs.trait.versioned.BossBarTrait;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;

import java.util.List;

public class NPCBossBarCommand extends AbstractCommand {

    public NPCBossBarCommand() {
        setName("npcbossbar");
        setSyntax("npcbossbar (remove) (color:<color>) (options:<option>|...) (range:<#>) (style:<style>) (title:<title>) (progress:<progress>) (view_permission:<permission>) (visible:<true/false>)");
        setRequiredArguments(1, 8);
        autoCompile();
    }

    // <--[command]
    // @Name npcbossbar
    // @Syntax npcbossbar (remove) (color:<color>) (options:<option>|...) (range:<#>) (style:<style>) (title:<title>) (progress:<progress>) (view_permission:<permission>) (visible:<true/false>)
    // @Required 1
    // @Maximum 8
    // @Short Controls or removes the linked NPC's bossbar.
    // @Group npc
    //
    // @Description
    // Controls or removes the linked NPC's bossbar.
    //
    // Progress can be a number between 1 and 100 or 'health' to make it track the NPC's health.
    // Placeholder API/Citizens placeholders are supported.
    //
    // Optionally specify a range around the NPC where the bossbar is visible, and/or a permission required to view it.
    // Input an empty view permission to remove it ('view_permission:').
    //
    // Valid colors: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/boss/BarColor.html>.
    // Valid styles: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/boss/BarStyle.html>.
    // Valid options: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/boss/BarFlag.html>.
    //
    // @Usage
    // Makes the linked NPC's bossbar green, and changes its title.
    // - npcbossbar color:green "title:This bossbar is green!"
    //
    // @Usage
    // Makes it so the linked NPC's bossbar can only be visible 5 blocks away from it.
    // - npcbossbar range:5
    //
    // @Usage
    // Removes a specific NPC's bossbar.
    // - npcbossbar remove npc:<[theNPC]>
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("color:", BarColor.values());
        tab.addWithPrefix("options:", BarFlag.values());
        tab.addWithPrefix("style:", BarStyle.values());
        tab.add("progress:health");
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("remove") boolean remove,
                                   @ArgName("color") @ArgPrefixed @ArgDefaultNull BarColor color,
                                   @ArgName("options") @ArgPrefixed @ArgDefaultNull @ArgSubType(BarFlag.class) List<BarFlag> options,
                                   @ArgName("range") @ArgPrefixed @ArgDefaultNull ElementTag range,
                                   @ArgName("style") @ArgPrefixed @ArgDefaultNull BarStyle style,
                                   @ArgName("title") @ArgPrefixed @ArgDefaultNull String title,
                                   @ArgName("progress") @ArgPrefixed @ArgDefaultNull String progress,
                                   @ArgName("view_permission") @ArgPrefixed @ArgDefaultNull String viewPermission,
                                   @ArgName("visible") @ArgPrefixed @ArgDefaultNull ElementTag visible) {
        NPCTag npc = Utilities.getEntryNPC(scriptEntry);
        if (npc == null) {
            throw new InvalidArgumentsRuntimeException("Must have a linked NPC.");
        }
        if (remove) {
            npc.getCitizen().removeTrait(BossBarTrait.class);
            return;
        }
        BossBarTrait trait = npc.getCitizen().getOrAddTrait(BossBarTrait.class);
        if (color != null) {
            trait.setColor(color);
        }
        if (range != null) {
            if (!range.isInt()) {
                throw new InvalidArgumentsRuntimeException("Invalid number '" + range + "' specified for 'range'.");
            }
            trait.setRange(range.asInt());
        }
        if (options != null) {
            trait.setFlags(options);
        }
        if (style != null) {
            trait.setStyle(style);
        }
        if (title != null) {
            trait.setTitle(title);
        }
        if (progress != null) {
            trait.setTrackVariable(progress);
        }
        if (viewPermission != null) {
            trait.setViewPermission(viewPermission.isEmpty() ? null : viewPermission);
        }
        if (visible != null) {
            if (!visible.isBoolean()) {
                throw new InvalidArgumentsRuntimeException("Invalid boolean '" + visible + "' specified for 'visible'.");
            }
            trait.setVisible(visible.asBoolean());
        }
    }
}
