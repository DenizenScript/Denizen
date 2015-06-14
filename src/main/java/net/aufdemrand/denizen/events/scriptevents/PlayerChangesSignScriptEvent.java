package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEllipsoid;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.tags.core.EscapeTags;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Arrays;
import java.util.HashMap;

public class PlayerChangesSignScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes sign
    // player changes sign in <notable cuboid>
    // player changes <material>
    // player changes <material> in <notable cuboid>
    //
    // @Cancellable true
    //
    // @Triggers when a player changes a sign.
    //
    // @Context
    // <context.location> returns the dLocation of the sign.
    // <context.new> returns the new sign text as a dList.
    // <context.old> returns the old sign text as a dList.
    // <context.material> returns the dMaterial of the sign.
    // <context.cuboids> returns a dList of notable cuboids surrounding the sign.
    //
    // @Determine
    // dList to change the lines (Uses escaping, see <@link language Property Escaping>)
    //
    // -->

    public PlayerChangesSignScriptEvent() {
        instance = this;
    }
    public static PlayerChangesSignScriptEvent instance;
    public dLocation location;
    public dList new_sign;
    public dList old_sign;
    public dMaterial material;
    public dList cuboids;
    private dList new_text;
    public SignChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String sign = CoreUtilities.getXthArg(2, lower);
        return lower.startsWith("player changes")
                && (sign.equals("sign") || dMaterial.matches(sign));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        String mat = CoreUtilities.getXthArg(2, lower);
        if (!mat.equals("sign")
                && (!mat.equals(material.identifyNoIdentifier()) && !(event.getBlock().getState() instanceof Sign))) {
            return false;
        }

        if (CoreUtilities.xthArgEquals(3, lower, "in")) {
            String it = CoreUtilities.getXthArg(4, lower);
            if (dCuboid.matches(it)) {
                dCuboid cuboid = dCuboid.valueOf(it);
                if (!cuboid.isInsideCuboid(location)) {
                    return false;
                }
            }
            else if (dEllipsoid.matches(it)) {
                dEllipsoid ellipsoid = dEllipsoid.valueOf(it);
                if (!ellipsoid.contains(location)) {
                    return false;
                }
            }
            else {
                dB.echoError("Invalid event 'IN ...' check [" + getName() + "]: '" + s + "' for " + scriptContainer.getName());
                return false;
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerChangesSign";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        SignChangeEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (determination.length() > 0 && !determination.equalsIgnoreCase("none")) {
            new_text = dList.valueOf(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("material", material);
        context.put("new_sign", new_sign);
        context.put("old_sign", old_sign);
        context.put("cuboids", cuboids);
        return context;
    }

    @EventHandler
    public void onPlayerChangesSign(SignChangeEvent event) {
        Sign sign = (Sign) event.getBlock().getState();
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        location = new dLocation(event.getBlock().getLocation());
        cuboids = new dList();
        for (dCuboid cuboid: dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
        old_sign = new dList(Arrays.asList(sign.getLines()));
        new_sign = new dList(Arrays.asList(event.getLines()));
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        if (new_text != null) {
            for (int i = 0; i < 4 && i < new_text.size(); i++) {
                event.setLine(i, EscapeTags.unEscape(new_text.get(i)));
            }
        }
    }

}
