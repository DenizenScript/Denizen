package net.aufdemrand.denizen.events.block;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import com.denizenscript.denizencore.objects.dObject;
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
    // <context.location> returns the dLocation the block that is forming.
    // <context.material> returns the dMaterial of the block that is forming.
    //
    // -->

    public BlockFormsScriptEvent() {
        instance = this;
    }

    public static BlockFormsScriptEvent instance;
    public dLocation location;
    public dMaterial material;
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
    public dObject getContext(String name) {
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

        location = new dLocation(event.getBlock().getLocation());
        material = new dMaterial(event.getNewState());
        this.event = event;
        fire(event);
    }
}
