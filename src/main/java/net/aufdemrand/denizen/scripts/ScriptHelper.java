package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.events.ScriptReloadEvent;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
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
        Bukkit.getServer().getPluginManager().callEvent(new ScriptReloadEvent());
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
    private static boolean HadAnError = false;
    public static boolean getHadAnError() {
        return HadAnError;
    }
    public static void resetHadAnError() {
        HadAnError = false;
    }
    private static String _concatenateCoreScripts() {

        try {
            File file = new File(DenizenAPI.getCurrentInstance()
                    .getDataFolder() + File.separator + "scripts");
            if(!file.exists()) {
                dB.echoError("No script folder found, please create one.");
                HadAnError = true;
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

                dB.echoDebug("Processing outside scripts... ");
                for (FileConfiguration outsideConfig : ScriptRegistry.outside_scripts) {
                    try {
                        sb.append(outsideConfig.saveToString() + "\r\n");
                    } catch (Exception e) {
                        dB.echoError("Woah! Error parsing outside scripts!");
                        HadAnError = true;
                    }
                }

                for (File f : files){
                    String fileName = f.getAbsolutePath().substring(file.getAbsolutePath().length());
                    dB.echoDebug("Processing '" + fileName + "'... ");

                    try {
                        yaml = YamlConfiguration.loadConfiguration(f);
                        String saved = yaml.saveToString();
                        if (yaml != null && saved.length() > 0)
                            sb.append(saved + "\r\n");
                        else {
                            dB.echoError(ChatColor.RED + "Woah! Error parsing " + fileName + "! This script has been skipped. See console for YAML errors.");
                            HadAnError = true;
                        }

                    } catch (RuntimeException e) {
                        dB.echoError(ChatColor.RED + "Woah! Error parsing " + fileName + "!");
                        HadAnError = true;
                        if (dB.showStackTraces) {
                            dB.echoDebug("STACKTRACE follows:");
                            e.printStackTrace();
                        }
                        else dB.echoDebug("Use '/denizen debug -s' for the nitty-gritty.");
                    }
                }

                dB.echoApproval("All scripts loaded!");
                return yamlKeysToUpperCase(sb.toString());
            } else {
                dB.echoError(ChatColor.RED + "Woah! No scripts in /plugins/Denizen/scripts/ to load!");
                HadAnError = true;
            }

        } catch (Exception error) {
            dB.echoError(ChatColor.RED + "Woah! No script folder found in /plugins/Denizen/scripts/");
            HadAnError = true;
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
        Pattern pattern = Pattern.compile("(^.*?[^\\s](:\\s))", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(string);
        while (matcher.find())
            matcher.appendReplacement(sb, matcher.group().toUpperCase());
        matcher.appendTail(sb);
        return sb.toString();
    }

}
