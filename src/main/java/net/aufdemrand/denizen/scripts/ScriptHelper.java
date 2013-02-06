package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.events.dScriptReloadEvent;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptHelper {

    /*
     * Reloads and retrieves information from the Denizen/scripts.yml.
     */

    private static YamlConfiguration _yamlScripts = null;

    public static void reloadScripts() {
        String concatenated = _concatenateCoreScripts();
        _yamlScripts = new YamlConfiguration();

        try {
            _yamlScripts.loadFromString(concatenated);
            Bukkit.getServer().getPluginManager().callEvent(new dScriptReloadEvent());

        } catch (InvalidConfigurationException e) {
            dB.echoError("Could not load scripts!");
            e.printStackTrace();
        }

        ScriptRegistry._buildCoreYamlScriptContainers(getScripts());
    }

    private static FileConfiguration getScripts() {
        if (_yamlScripts == null) {
            reloadScripts();
        }
        return _yamlScripts;
    }

    private static String _concatenateCoreScripts() {

        try {
            File file = new File(DenizenAPI.getCurrentInstance()
                    .getDataFolder() + File.separator + "scripts");
            if(!file.exists()) {
                dB.echoError("No script folder found, please create one.");
                return "";
            }

            // Get files
            File[] files = file.listFiles();

            if (files.length > 0) {
                StringBuilder sb = new StringBuilder();

                for (File f : files){
                    String fileName = f.getName();
                    if (fileName.substring(fileName.lastIndexOf('.') + 1).equalsIgnoreCase("YML")
                            || fileName.substring(fileName.lastIndexOf('.') + 1).equalsIgnoreCase("DSCRIPT")
                            || fileName.substring(fileName.lastIndexOf('.') + 1).equalsIgnoreCase("YAML")
                            && !fileName.startsWith(".")) {
                        dB.echoDebug("Processing '" + fileName + "'... ");

                        try {
                            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
                            if (yaml != null)
                                sb.append(yaml.saveToString() + "\r\n");
                            else dB.echoError(ChatColor.RED + "Woah! Error parsing " + fileName + "! This script has been skipped. See console for YAML errors.");

                        } catch (RuntimeException e) {
                            dB.echoError(ChatColor.RED + "Woah! Error parsing " + fileName + "!");
                            if (dB.showStackTraces) {
                                dB.echoDebug("STACKTRACE follows:");
                                e.printStackTrace();
                            }
                            else dB.echoDebug("Use '/denizen debug -s' for the nitty-gritty.");
                        }
                    }
                }

                dB.echoApproval("All scripts loaded!");
                return yamlKeysToUpperCase(sb.toString());
            } else dB.echoError(ChatColor.RED + "Woah! No scripts in /plugins/Denizen/scripts/ to load!");

        } catch (Exception error) {
            dB.echoError(ChatColor.RED + "Woah! No script folder found in /plugins/Denizen/scripts/");
            if (dB.showStackTraces) error.printStackTrace();
        }

        return "";
    }


    /**
     * Changes YAML 'keys' to all Upper Case to de-sensitize case sensitivity when
     * reading and parsing scripts.
     */

    private static String yamlKeysToUpperCase(String string) {
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("(^[^:-]*?[^\\s]:)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(string);
        while (matcher.find())
            matcher.appendReplacement(sb, matcher.group().toUpperCase());
        matcher.appendTail(sb);
        return sb.toString();
    }

}