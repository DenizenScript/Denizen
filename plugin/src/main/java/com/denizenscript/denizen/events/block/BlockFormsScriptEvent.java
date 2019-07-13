package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

public class BlockFormsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block forms
    // <block> forms
    //
    // @Regex ^on [^\s]+ forms$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a block is formed based on world conditions, EG, when snow forms in a snow storm or ice forms in a cold biome.
    //
    // @Context
    // <context.location> returns the LocationTag the block that is forming.
    // <context.material> returns the MaterialTag of the block that is forming.
    //
    // -->

    public BlockFormsScriptEvent() {
        instance = this;
    }

    public static BlockFormsScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public BlockFormEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("forms");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }

        String mat = path.eventArgLowerAt(0);
        return tryMaterial(material, mat);
    }

    @Override
    public String getName() {
        return "BlockForms";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockForms(BlockFormEvent event) {

        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getNewState());
        this.event = event;
        fire(event);
    }
}
