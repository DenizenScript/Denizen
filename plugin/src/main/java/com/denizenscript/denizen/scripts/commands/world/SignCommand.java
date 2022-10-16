package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.properties.material.MaterialDirectional;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class SignCommand extends AbstractCommand {

    public SignCommand() {
        setName("sign");
        setSyntax("sign (type:{automatic}/sign_post/wall_sign) (material:<material>) [<line>|...] [<location>] (direction:north/east/south/west)");
        setRequiredArguments(1, 5);
        isProcedural = false;
    }

    // <--[command]
    // @Name Sign
    // @Syntax sign (type:{automatic}/sign_post/wall_sign) (material:<material>) [<line>|...] [<location>] (direction:north/east/south/west)
    // @Required 1
    // @Maximum 5
    // @Short Modifies a sign.
    // @Group world
    //
    // @Description
    // Modifies a sign that replaces the text shown on it. If no sign is at the location, it replaces the location with the modified sign.
    //
    // Specify 'automatic' as a type to use whatever sign type and direction is already placed there.
    // If there is not already a sign there, defaults to a sign_post.
    //
    // Optionally specify a material to use. If not specified, will use an oak sign (unless the block is already a sign, and 'type' is 'automatic').
    //
    // The direction argument specifies which direction the sign should face.
    // If a direction is not specified, and there is not already a sign there for 'automatic', the direction defaults to south.
    // If a sign_post is placed, you can specify any specific blockface value as the direction, eg "SOUTH_WEST".
    // See also <@link tag MaterialTag.valid_directions> (test in-game for example via "/ex narrate <material[oak_sign].valid_directions>").
    //
    // @Tags
    // <LocationTag.sign_contents>
    //
    // @Usage
    // Use to edit some text on an existing sign.
    // - sign "Hello|this is|some|text" <context.location>
    //
    // @Usage
    // Use to show the time on a sign and ensure that it points north.
    // - sign "I point|North.|System Time<&co>|<util.time_now.formatted>" <[location]> direction:north
    //
    // @Usage
    // Use to place a new wall_sign regardless of whether there is already a sign there.
    // - sign type:wall_sign "Player<&co>|<player.name>|Online Players<&co>|<server.online_players.size>" <player.location>
    //
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addNotesOfType(LocationTag.class);
    }

    private enum Type {AUTOMATIC, SIGN_POST, WALL_SIGN}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.class)) {
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
            else if (!scriptEntry.hasObject("material")
                    && arg.matchesPrefix("material")
                    && arg.matchesArgumentType(MaterialTag.class)) {
                scriptEntry.addObject("material", arg.asType(MaterialTag.class));
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a Sign location!");
        }
        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Must specify sign text!");
        }
        scriptEntry.defaultObject("type", new ElementTag(Type.AUTOMATIC));
    }

    public void setWallSign(Block sign, BlockFace bf, MaterialTag material) {
        sign.setType(material == null ? Material.OAK_WALL_SIGN : material.getMaterial(), false);
        MaterialTag signMaterial = new MaterialTag(sign);
        MaterialDirectional.getFrom(signMaterial).setFacing(bf);
        sign.setBlockData(signMaterial.getModernData());
    }

    public static boolean isStandingSign(Material material) {
        switch (material) {
            case CRIMSON_SIGN:
            case WARPED_SIGN:
            case ACACIA_SIGN:
            case BIRCH_SIGN:
            case DARK_OAK_SIGN:
            case JUNGLE_SIGN:
            case OAK_SIGN:
            case SPRUCE_SIGN:
                return true;
            default:
                return false;
        }
    }

    public static boolean isWallSign(Material material) {
        switch (material) {
            case CRIMSON_WALL_SIGN:
            case WARPED_WALL_SIGN:
            case ACACIA_WALL_SIGN:
            case BIRCH_WALL_SIGN:
            case DARK_OAK_WALL_SIGN:
            case JUNGLE_WALL_SIGN:
            case OAK_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAnySign(Material material) {
        return isStandingSign(material) || isWallSign(material);
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        String direction = scriptEntry.hasObject("direction") ? ((ElementTag) scriptEntry.getObject("direction")).asString() : null;
        ElementTag typeElement = scriptEntry.getElement("type");
        ListTag text = scriptEntry.getObjectTag("text");
        LocationTag location = scriptEntry.getObjectTag("location");
        MaterialTag material = scriptEntry.getObjectTag("material");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), typeElement, location, db("direction", direction), material, text);
        }
        Type type = Type.valueOf(typeElement.asString().toUpperCase());
        Block sign = location.getBlock();
        if (type != Type.AUTOMATIC || !isAnySign(sign.getType())) {
            if (type == Type.WALL_SIGN) {
                BlockFace bf;
                if (direction != null) {
                    bf = Utilities.chooseSignRotation(direction);
                }
                else {
                    bf = Utilities.chooseSignRotation(sign);
                }
                setWallSign(sign, bf, material);
            }
            else {
                sign.setType(material == null ? Material.OAK_SIGN : material.getMaterial(), false);
                if (direction != null) {
                    Utilities.setSignRotation(sign.getState(), direction);
                }
            }
        }
        else if (!isAnySign(sign.getType())) {
            if (sign.getRelative(BlockFace.DOWN).getType().isSolid()) {
                sign.setType(material == null ? Material.OAK_SIGN : material.getMaterial(), false);
            }
            else {
                BlockFace bf = Utilities.chooseSignRotation(sign);
                setWallSign(sign, bf, material);
            }
        }
        BlockState signState = sign.getState();
        Utilities.setSignLines((Sign) signState, text.toArray(new String[4]));
    }
}
