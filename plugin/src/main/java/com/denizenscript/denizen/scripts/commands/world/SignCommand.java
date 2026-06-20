package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.properties.material.MaterialDirectional;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.*;

public class SignCommand extends AbstractCommand {

    public SignCommand() {
        setName("sign");
        setSyntax("sign (type:{automatic}/sign_post/wall_sign/hanging) (material:<material>) [<line>|...] [<location>] (direction:north/east/south/west)");
        setRequiredArguments(1, 5);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Sign
    // @Syntax sign (type:{automatic}/sign_post/wall_sign/hanging) (material:<material>) [<line>|...] [<location>] (direction:north/east/south/west)
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

    public enum Type {AUTOMATIC, SIGN_POST, WALL_SIGN, HANGING}

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("type") @ArgPrefixed @ArgDefaultText("automatic") Type type,
                                   @ArgName("material") @ArgPrefixed @ArgDefaultNull MaterialTag material,
                                   @ArgName("text") @ArgLinear @ArgDefaultNull ListTag text,
                                   @ArgName("location") @ArgLinear @ArgDefaultNull LocationTag location,
                                   @ArgName("direction") @ArgPrefixed @ArgDefaultNull String direction) {
        if (location == null) {
            throw new InvalidArgumentsRuntimeException("Must specify a Sign location!");
        }
        if (text == null) {
            throw new InvalidArgumentsRuntimeException("Must specify sign text!");
        }
        Block sign = location.getBlock();
        if (type != Type.AUTOMATIC || !isAnySign(sign.getType())) {
            if (type == Type.WALL_SIGN || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && type == Type.HANGING)) {
                BlockFace bf;
                if (direction != null) {
                    bf = Utilities.chooseSignRotation(direction);
                }
                else {
                    bf = Utilities.chooseSignRotation(sign);
                }
                if (type == Type.WALL_SIGN) {
                    setWallSign(sign, bf, material);
                }
                else {
                    setHangingSign(sign, bf, material);
                }
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

    public static void setWallSign(Block sign, BlockFace bf, MaterialTag material) {
        sign.setType(material == null ? Material.OAK_WALL_SIGN : material.getMaterial(), false);
        MaterialTag signMaterial = new MaterialTag(sign);
        MaterialDirectional.getFrom(signMaterial).setFacing(bf);
        sign.setBlockData(signMaterial.getModernData());
    }

    public static void setHangingSign(Block sign, BlockFace bf, MaterialTag material) {
        sign.setType(material == null ? Material.OAK_HANGING_SIGN : material.getMaterial(), false);
        MaterialTag signMaterial = new MaterialTag(sign);
        MaterialDirectional.getFrom(signMaterial).setFacing(bf);
        sign.setBlockData(signMaterial.getModernData());
    }

    public static boolean isStandingSign(Material material) {
        for (Material signType : Tag.STANDING_SIGNS.getValues()) {
            if (signType == material) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWallSign(Material material) {
        for (Material signType : Tag.WALL_SIGNS.getValues()) {
            if (signType == material) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHangingSign(Material material) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
            return false;
        }
        for (Material signType : Tag.ALL_HANGING_SIGNS.getValues()) {
            if (signType == material) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAnySign(Material material) {
        return isStandingSign(material) || isWallSign(material) || isHangingSign(material);
    }
}
