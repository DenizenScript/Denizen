package net.aufdemrand.denizen.utilities.debugging;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.AbstractLoggerWrapper;
import org.bukkit.ChatColor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Intercepts system.out operations for the sake of blocking messages at request.
 * Disabled by default in config.yml
 */
public class LogInterceptor extends PrintStream {
    boolean redirected = false;
    public PrintStream standardOut;

    public LogInterceptor() {
        super(new LoggerOutputStream(new LoggerOutputIntercept((AbstractLogger) LogManager.getRootLogger()),
                Level.INFO), true);
    }

    public static String cleanse(String input) {
        String esc = String.valueOf((char) 0x1b);
        String repc = String.valueOf(ChatColor.COLOR_CHAR);
        if (input.contains(esc)) {
            input = StringUtils.replace(input, esc + "[0;30;22m", repc + "0");
            input = StringUtils.replace(input, esc + "[0;34;22m", repc + "1");
            input = StringUtils.replace(input, esc + "[0;32;22m", repc + "2");
            input = StringUtils.replace(input, esc + "[0;36;22m", repc + "3");
            input = StringUtils.replace(input, esc + "[0;31;22m", repc + "4");
            input = StringUtils.replace(input, esc + "[0;35;22m", repc + "5");
            input = StringUtils.replace(input, esc + "[0;33;22m", repc + "6");
            input = StringUtils.replace(input, esc + "[0;37;22m", repc + "7");
            input = StringUtils.replace(input, esc + "[0;30;1m", repc + "8");
            input = StringUtils.replace(input, esc + "[0;34;1m", repc + "9");
            input = StringUtils.replace(input, esc + "[0;32;1m", repc + "a");
            input = StringUtils.replace(input, esc + "[0;36;1m", repc + "b");
            input = StringUtils.replace(input, esc + "[0;31;1m", repc + "c");
            input = StringUtils.replace(input, esc + "[0;35;1m", repc + "d");
            input = StringUtils.replace(input, esc + "[0;33;1m", repc + "e");
            input = StringUtils.replace(input, esc + "[0;37;1m", repc + "f");
            input = StringUtils.replace(input, esc + "[5m", repc + "k");
            input = StringUtils.replace(input, esc + "[21m", repc + "l");
            input = StringUtils.replace(input, esc + "[9m", repc + "m");
            input = StringUtils.replace(input, esc + "[4m", repc + "n");
            input = StringUtils.replace(input, esc + "[3m", repc + "o");
            input = StringUtils.replace(input, esc + "[m", repc + "r");
        }
        return input;
    }

    public void redirectOutput() {
        if (redirected) {
            return;
        }
        standardOut = System.out;
        System.setOut(this);
    }

    public void standardOutput() {
        if (!redirected) {
            return;
        }
        System.setOut(standardOut);
    }

    private static class LoggerOutputIntercept extends AbstractLoggerWrapper {
        private final Logger logger;

        private LoggerOutputIntercept(AbstractLogger logger) {
            super(logger, logger.getName(), logger.getMessageFactory());
            this.logger = logger;
        }

        // <--[event]
        // @Events
        // bukkit console output
        //
        // @Warning Disable debug on this event or you'll get an infinite loop!
        //
        // @Triggers when any message is printed to the Bukkit-powered portion of the console. (Requires <@link mechanism server.redirect_logging> be set true.)
        // @Context
        // <context.message> returns the message that is being printed to console.
        // <context.level> returns the log level the message is being printed at.
        //
        // @Determine
        // "CANCELLED" to disable the output.
        //
        // -->
        @Override
        public void log(Marker marker, String fqcn, Level level, Message data, Throwable t) {
            HashMap<String, dObject> context = new HashMap<String, dObject>();
            context.put("message", new Element(cleanse(data.getFormattedMessage())));
            context.put("level", new Element(level.name()));
            List<String> Determinations = OldEventManager.doEvents(Arrays.asList("console output"),
                    new BukkitScriptEntryData(null, null), context);
            for (String str : Determinations) {
                if (str.equalsIgnoreCase("cancelled")) {
                    return;
                }
            }
            super.log(marker, fqcn, level, data, t);
        }
    }

    private static class LoggerOutputStream extends ByteArrayOutputStream {
        private final String separator = System.getProperty("line.separator");
        private final Logger logger;
        private final Level level;

        public LoggerOutputStream(Logger logger, Level level) {
            this.logger = logger;
            this.level = level;
        }

        public void flush() throws IOException {
            synchronized(this) {
                super.flush();
                String record = this.toString();
                super.reset();
                if(record.length() > 0 && !record.equals(this.separator)) {
                    this.logger.log(this.level, record);
                }

            }
        }
    }
}
