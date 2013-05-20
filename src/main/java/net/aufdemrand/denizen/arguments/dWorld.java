package net.aufdemrand.denizen.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dWorld implements dScriptArgument {

    /**
     *
     * @param string  the string or dScript argument String
     * @return  a dScript dList
     *
     */
    @ObjectFetcher("w")
    public static dWorld valueOf(String string) {
        if (string == null) return null;

        // Make sure string matches what this interpreter can accept.
        final Pattern world_object_pattern =
                Pattern.compile("(.+?:|)((w@|)(.+))",
                        Pattern.CASE_INSENSITIVE);

        Matcher m = world_object_pattern.matcher(string);

        if (m.matches()) {
            String prefix = m.group(1);
            String world_name = m.group(2).split("@")[1];

            for (World world : Bukkit.getWorlds())
                if (world.getName().equalsIgnoreCase(world_name)) return new dWorld(prefix, world);
        }

        dB.echoError("World '" + m.group(2) + "' is invalid or does not exist.");
        return null;
    }

    public World getWorld() {
        return Bukkit.getWorld(world_name);
    }

    private String prefix;
    String world_name;

    public dWorld(World world) {
        this(null, world);
    }

    public dWorld(String prefix, World world) {
        if (prefix == null) this.prefix = "World";
        else this.prefix = prefix;
        this.world_name = world.getName();
    }

    @Override
    public String getDefaultPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + world_name + "<G>'  ");
    }

    @Override
    public String as_dScriptArg() {
        return prefix + ":" + world_name;
    }

    public String dScriptArgValue() {
        return world_name;
    }

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

//        getWorld().canGenerateStructures())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getName())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getAllowAnimals())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getAllowMonsters())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getAmbientSpawnLimit())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getAnimalSpawnLimit())
//        .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("highest_block")) {
            // TODO: finish
            int x = 1;
            int z = 1;

            return new dLocation(getWorld().getHighestBlockAt(x, z).getLocation())
                    .getAttribute(attribute.fulfill(1));
        }

//        getWorld().getChunkAt()

        if (attribute.startsWith("difficulty"))
            return new Element(getWorld().getDifficulty().name())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("players")) {
            List<String> players = new ArrayList<String>();
            for(Player player : getWorld().getPlayers())
                players.add(player.getName());

            return new dList(players)
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("sea_level"))
            return new Element(String.valueOf(getWorld().getSeaLevel()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("seed"))
            return new Element(String.valueOf(getWorld().getSeed()))
                    .getAttribute(attribute.fulfill(1));

//        getWorld().getEntities())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getEntitiesByClass())
//        .getAttribute(attribute.fulfill(1));
//
//        getWorld().getEntitiesByClasses())
//        .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("time"))
            return new Element(String.valueOf(getWorld().getTime()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("weather_duration"))
            return Duration.valueOf(String.valueOf(getWorld().getWeatherDuration()) + "t")
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("has_storm"))
            return new Element(String.valueOf(getWorld().hasStorm()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("debug.log")) {
            dB.log(debug());
            return new Element(Boolean.TRUE.toString())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug")) {
            return new Element(debug())
                    .getAttribute(attribute.fulfill(1));
        }

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
