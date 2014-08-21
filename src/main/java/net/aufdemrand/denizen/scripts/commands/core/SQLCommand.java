package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SQLCommand extends AbstractCommand {

    public static Map<String, Connection> connections = new HashMap<String, Connection>();

    @Override
    public void onDisable() {
        for (Map.Entry<String, Connection> entry: connections.entrySet()) {
            try {
                entry.getValue().close();
            }
            catch (SQLException e) {
                dB.echoError(e);
            }
        }
        connections.clear();
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("sqlid")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("sqlid", arg.asElement());
            }

            else if (!scriptEntry.hasObject("action")
                    && arg.matchesPrefix("connect")) {
                scriptEntry.addObject("action", new Element("CONNECT"));
                scriptEntry.addObject("server", arg.asElement());
            }

            else if (!scriptEntry.hasObject("action")
                    && arg.matches("disconnect")) {
                scriptEntry.addObject("action", new Element("DISCONNECT"));
            }

            else if (!scriptEntry.hasObject("query")
                    && arg.matchesPrefix("query")) {
                scriptEntry.addObject("action", new Element("QUERY"));
                scriptEntry.addObject("query", arg.asElement());
            }

            else if (!scriptEntry.hasObject("query")
                    && arg.matchesPrefix("update")) {
                scriptEntry.addObject("action", new Element("UPDATE"));
                scriptEntry.addObject("query", arg.asElement());
            }

            else if (!scriptEntry.hasObject("username")
                    && arg.matchesPrefix("username")) {
                scriptEntry.addObject("username", arg.asElement());
            }

            else if (!scriptEntry.hasObject("password")
                    && arg.matchesPrefix("password")) {
                scriptEntry.addObject("password", arg.asElement());
            }

            else arg.reportUnhandled();
        }

        if (!scriptEntry.hasObject("sqlid")) {
            throw new InvalidArgumentsException("Must specify an ID!");
        }

        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify an action!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element action = scriptEntry.getElement("action");
        Element server = scriptEntry.getElement("server");
        Element username = scriptEntry.getElement("username");
        Element password = scriptEntry.getElement("password");
        Element sqlID = scriptEntry.getElement("sqlid");
        Element query = scriptEntry.getElement("query");

        dB.report(scriptEntry, getName(), sqlID.debug()
                                        + action.debug()
                                        + (server != null ? server.debug(): "")
                                        + (username != null ? username.debug(): "")
                                        + (password != null ? aH.debugObj("password", "NotLogged"): "")
                                        + (query != null ? query.debug(): ""));

        try {
            if (action.asString().equalsIgnoreCase("connect")) {
                if (server == null) {
                    dB.echoError("Must specify a server!");
                    return;
                }
                if (username == null) {
                    dB.echoError("Must specify a username!");
                    return;
                }
                if (password == null) {
                    dB.echoError("Must specify a password!");
                    return;
                }
                if (connections.containsKey(sqlID.asString().toUpperCase())) {
                    dB.echoError("Already connected to a server with ID '" + sqlID.asString() + "'!");
                    return;
                }
                Connection con = getConnection(username.asString(), password.asString(), server.asString());
                connections.put(sqlID.asString().toUpperCase(), con);
                dB.echoDebug(scriptEntry, "Successfully connected to " + server);
            }
            else if (action.asString().equalsIgnoreCase("disconnect")) {
                Connection con = connections.get(sqlID.asString().toUpperCase());
                if (con == null) {
                    dB.echoError("Not connected to server with ID '" + sqlID.asString() + "'!");
                    return;
                }
                con.close();
                connections.remove(sqlID.asString().toUpperCase());
                dB.echoDebug(scriptEntry, "Disconnected from '" + sqlID.asString() + "'.");
            }
            else if (action.asString().equalsIgnoreCase("query")) {
                if (query == null) {
                    dB.echoError("Must specify a query!");
                    return;
                }
                Connection con = connections.get(sqlID.asString().toUpperCase());
                if (con == null) {
                    dB.echoError("Not connected to server with ID '" + sqlID.asString() + "'!");
                    return;
                }
                dB.echoDebug(scriptEntry, "Running query " + query.asString());
                Statement statement = con.createStatement();
                ResultSet set = statement.executeQuery(query.asString());
                ResultSetMetaData rsmd = set.getMetaData();
                int columns = rsmd.getColumnCount();
                dB.echoDebug(scriptEntry, "Got a query result of " + columns + " columns");
                int count = 0;
                dList rows = new dList();
                while (set.next()) {
                    count++;
                    StringBuilder current = new StringBuilder();
                    for (int i = 0; i < columns; i++) {
                        current.append(set.getString(i + 1)).append("/");
                    }
                    rows.add(current.toString());
                }
                scriptEntry.addObject("result", rows);
                dB.echoDebug(scriptEntry, "Got a query result of " + count + " rows");
            }
            else if (action.asString().equalsIgnoreCase("update")) {
                if (query == null) {
                    dB.echoError("Must specify an update query!");
                    return;
                }
                Connection con = connections.get(sqlID.asString().toUpperCase());
                if (con == null) {
                    dB.echoError("Not connected to server with ID '" + sqlID.asString() + "'!");
                    return;
                }
                dB.echoDebug(scriptEntry, "Running update " + query.asString());
                Statement statement = con.createStatement();
                int affected = statement.executeUpdate(query.asString());
                dB.echoDebug(scriptEntry, "Updated " + affected + " rows");
            }
            else {
                dB.echoError("Unknown action '" + action.asString() + "'");
            }
        }
        catch (SQLException e) {
            dB.echoError("SQL Exception: " + e.getMessage());
        }
    }

    public Connection getConnection(String userName, String password, String server) throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);
        return DriverManager.getConnection("jdbc:mysql://" + server, connectionProps);
    }
}
