package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.MaterialCompat;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.blocks.DirectionalBlocksHelper;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class SignCommand extends AbstractCommand {

    // <--[command]
    // @Name Sign
    // @Syntax sign (type:{automatic}/sign_post/wall_sign) ["<line>|..."] [<location>] (direction:n/e/w/s)
    // @Required 1
    // @Short Modifies a sign.
    // @Group world
    //
    // @Description
    // Modifies a sign that replaces the text shown on it. If no sign is at the location, it replaces the location with the modified sign.
    // The direction arguement tells which direction the text shown. If a direction is not specified, it defaults to south.
    // Specify 'automatic' as a type to use whatever sign type and direction is already placed there.
    // If there is not already a sign there, defaults to a sign_post.
    //
    // @Tags
    // <LocationTag.block.sign_contents>
    //
    // @Usage
    // Use to edit some text on a sign
    // - sign type:automatic "Hello|this is|some|text" <player.location>
    //
    // @Usage
    // Use to show the time on a sign that points north
    // - sign type:automatic "I point|North.|System Time<&co>|<util.date.time>" l@233,65,123,world direction:n
    //
    // @Usage
    // Use to force a sign to be a wall_sign if no sign is found.
    // - sign type:wall_sign "Player<&co>|<player.name>|Online Players<&co>|<server.list_online_players.size>" l@233,65,123,world
    //
    // -->

    private enum Type {AUTOMATIC, SIGN_POST, WALL_SIGN}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.values())) {
                scriptEntry.addObject("type", arg.asElement());
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class).setPrefix("location"));
            }
            else if (!scriptEntry.hasObject("direction")
                    && arg.matchesPrefix("direction", "dir")) {
                scriptEntry.addObject("direction", arg.asElement());
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a Sign location!");
        }

        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Must specify sign text!");
        }

        // Default to AUTOMATIC
        scriptEntry.defaultObject("type", new ElementTag(Type.AUTOMATIC.name()));
    }

    public void setWallSign(Block sign, BlockFace bf) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            // TODO: 1.14 - allow new sign types?
            sign.setType(MaterialCompat.WALL_SIGN, false);
            DirectionalBlocksHelper.setFace(sign, bf);
        }
        else {
            // TODO: 1.14 - allow new sign types?
            org.bukkit.material.Sign sgntmp = new org.bukkit.material.Sign(MaterialCompat.WALL_SIGN);
            sgntmp.setFacingDirection(bf);
            BlockData blockData = NMSHandler.getInstance().getBlockHelper().getBlockData(MaterialCompat.WALL_SIGN, sgntmp.getData());
            blockData.setBlock(sign, false);
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {

        // Get objects
        String direction = scriptEntry.hasObject("direction") ? ((ElementTag) scriptEntry.getObject("direction")).asString() : null;
        ElementTag typeElement = scriptEntry.getElement("type");
        ListTag text = (ListTag) scriptEntry.getObject("text");
        LocationTag location = (LocationTag) scriptEntry.getObject("location");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), typeElement.debug()
                    + location.debug()
                    + text.debug());
        }

        Type type = Type.valueOf(typeElement.asString().toUpperCase());
        Block sign = location.getBlock();
        if (type != Type.AUTOMATIC
                || !MaterialCompat.isAnySign(sign.getType())) {
            if (type == Type.WALL_SIGN) {
                BlockFace bf;
                if (direction != null) {
                    bf = Utilities.chooseSignRotation(direction);
                }
                else {
                    bf = Utilities.chooseSignRotation(sign);
                }
                setWallSign(sign, bf);
            }
            else {
                // TODO: 1.14 - allow new sign types?
                sign.setType(MaterialCompat.SIGN, false);
                if (direction != null) {
                    Utilities.setSignRotation(LocationTag.getBlockStateFor(sign), direction);
                }
            }
        }
        else if (!MaterialCompat.isAnySign(sign.getType())) {
            if (sign.getRelative(BlockFace.DOWN).getType().isSolid()) {
                // TODO: 1.14 - allow new sign types?
                sign.setType(MaterialCompat.SIGN, false);
            }
            else {
                BlockFace bf = Utilities.chooseSignRotation(sign);
                setWallSign(sign, bf);
            }
        }
        BlockState signState = LocationTag.getBlockStateFor(sign);

        Utilities.setSignLines((Sign) signState, text.toArray(4));
    }
}
