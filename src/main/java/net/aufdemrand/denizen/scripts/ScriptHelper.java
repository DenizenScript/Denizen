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
import java.io.FilenameFilter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.utilities.Utilities;

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
        } catch (InvalidConfigurationException e) {
            dB.echoError("Could not load scripts!");
            e.printStackTrace();
        }

        ScriptRegistry._buildCoreYamlScriptContainers(getScripts());
        Bukkit.getServer().getPluginManager().callEvent(new dScriptReloadEvent());
    }

    public static FileConfiguration _gs() {
        return getScripts();
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
            List<File> files = Utilities.listDScriptFiles(file, Settings.LoadScriptsInSubfolders());

            if (files.size() > 0) {
                StringBuilder sb = new StringBuilder();

                YamlConfiguration yaml;
                dB.echoDebug("Processing 'util.dscript'... ");
                yaml = YamlConfiguration.loadConfiguration(DenizenAPI.getCurrentInstance().getResource("util.dscript"));
                sb.append(yaml.saveToString() + "\r\n");
                dB.echoDebug("Processing 'genies.dscript'... ");
                yaml = YamlConfiguration.loadConfiguration(DenizenAPI.getCurrentInstance().getResource("genies.dscript"));
                sb.append(yaml.saveToString() + "\r\n");

                for (File f : files){
                    String fileName = f.getName();
                    dB.echoDebug("Processing '" + fileName + "'... ");

                    try {
                        yaml = YamlConfiguration.loadConfiguration(f);
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