package net.aufdemrand.denizen.scripts.commands.core;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.bukkit.Bukkit;
import org.json.simple.JSONAware;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.aufdemrand.denizencore.utilities.debugging.dB;

public class HttpCommand extends AbstractCommand implements Holdable {
    private final List<Request> responses = Lists.newCopyOnWriteArrayList();
    @Override
    public void onDisable() {
        for (Request response : responses) {
            response.abort();
        }
        responses.clear();
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        scriptEntry.addObject("args", new ArrayList<NameValuePair>());
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
             if (!scriptEntry.hasObject("action")
                    && arg.matchesPrefix("post")) {
                scriptEntry.addObject("action", new Element("POST"));
                scriptEntry.addObject("server", arg.asElement());
            }
            else if (!scriptEntry.hasObject("action")
                    && arg.matchesPrefix("get")) {
                scriptEntry.addObject("action", new Element("GET"));
                scriptEntry.addObject("server", arg.asElement());
            }
            else if (!scriptEntry.hasObject("json") && arg.matchesPrefix("json")) {
                scriptEntry.addObject("json", arg.asElement());
            }
            else {
                ((List<NameValuePair>) scriptEntry.getObject("args")).add(new BasicNameValuePair(arg.asElement().getPrefix(), arg.asElement().asString()));
            }
        }

        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify an action!");
        }
        if (!scriptEntry.hasObject("server")) {
            throw new InvalidArgumentsException("Must specify a server!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        final Element action = scriptEntry.getElement("action");
        final Element server = scriptEntry.getElement("server");
        final Element json = scriptEntry.getElement("json");
        final List<NameValuePair> args = (List<NameValuePair>) scriptEntry.getObject("args");

        dB.report(scriptEntry, getName(),
                action.debug()
                + server.debug());

        Bukkit.getScheduler().runTaskLaterAsynchronously(DenizenAPI.getCurrentInstance(), new Runnable() {
            @Override
            public void run() {
                Request req =  action.asString().equals("POST") ? Request.Post(server.asString())
                        : Request.Get(server.asString());
                if (json != null) {
                    try {
                        Object jsonObj = new JSONParser().parse(json.asString());
                        if (jsonObj instanceof JSONAware) {
                            req.bodyString(((JSONAware) jsonObj).toJSONString(), ContentType.APPLICATION_JSON);
                        }
                    } catch (ParseException e) {
                        reportExceptionLater(scriptEntry, e);
                        return;
                    }
                } else if (!args.isEmpty()) {
                    try {
                        req.body(new UrlEncodedFormEntity(args));
                    } catch (UnsupportedEncodingException e) {
                        reportExceptionLater(scriptEntry, e);
                        return;
                    }
                }
                try {
                    responses.add(req);
                    scriptEntry.addObject("result", req.execute().returnContent().asString());
                    responses.remove(req);
                } catch (Exception e) {
                    reportExceptionLater(scriptEntry, e);
                    return;
                }
                Bukkit.getScheduler().runTaskLater(DenizenAPI.getCurrentInstance(), new Runnable() {
                    @Override
                    public void run() {
                        scriptEntry.setFinished(true);
                        if (dB.verbose) {
                            dB.echoDebug(scriptEntry, "Got a result of " + scriptEntry.getObject("result"));
                        }
                    }
                }, 1);
            }

            public void reportExceptionLater(final ScriptEntry scriptEntry, final Exception e) {
                Bukkit.getScheduler().runTaskLater(DenizenAPI.getCurrentInstance(), new Runnable() {
                    @Override
                    public void run() {
                        dB.echoError(scriptEntry.getResidingQueue(), "HTTP Exception: " + e.getMessage());
                        scriptEntry.setFinished(true);
                        if (dB.verbose) {
                            dB.echoError(scriptEntry.getResidingQueue(), e);
                        }
                    }
                }, 1);
            }
        }, 1);
    }
}
