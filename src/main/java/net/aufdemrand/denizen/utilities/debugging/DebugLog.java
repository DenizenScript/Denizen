/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.aufdemrand.denizen.utilities.debugging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * DebugLog
 *
 * Handles debug logs
 *
 * @uathor SpaceEmotion
 */
public class DebugLog extends Logger {
    private final static DebugFormatter formatter = new DebugFormatter();
    private FileHandler handler;

    public DebugLog(String l, String f) {
        super(l, null);

        try {
            handler = new FileHandler(f, true);
            addHandler(handler);
            setLevel(Level.ALL);
            handler.setFormatter(formatter);
        } catch (Exception e) {
            System.out.println("Error creating logger '" + l + "': ");
            e.printStackTrace();
        }
    }

    public void close() {
        if (this.handler != null) {
            this.handler.close();
        }
    }

    private static class DebugFormatter extends Formatter {
        private final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");

        @Override
        public String format(LogRecord rec) {
            Throwable exception = rec.getThrown();

            String out = this.date.format(Long.valueOf(rec.getMillis()));

            out += "[" + rec.getLevel().getName().toUpperCase() + "] ";
            out += rec.getMessage() + '\n';

            if (exception != null) {
                StringWriter writer = new StringWriter();
                exception.printStackTrace(new PrintWriter(writer));

                return out + writer;
            }

            return out;
        }
    }
    
}
