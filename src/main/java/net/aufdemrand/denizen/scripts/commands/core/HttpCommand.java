package net.aufdemrand.denizen.scripts.commands.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

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
    private final List<CloseableHttpResponse> responses = Lists.newCopyOnWriteArrayList();
    @Override
    public void onDisable() {
        for (CloseableHttpResponse response : responses) {
            try {
                response.close();
            } catch (IOException e) {
            }
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
                    && arg.matches("get")) {
                scriptEntry.addObject("action", new Element("GET"));
                scriptEntry.addObject("server", arg.asElement());
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
        final List<NameValuePair> args = (List<NameValuePair>) scriptEntry.getObject("args");

        dB.report(scriptEntry, getName(),
                action.debug()
                + server.debug());

        Bukkit.getScheduler().runTaskLaterAsynchronously(DenizenAPI.getCurrentInstance(), new Runnable() {
            @Override
            public void run() {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpRequestBase req = action.asString().equals("POST") ? new HttpPost(server.asString())
                        : new HttpGet(server.asString());
                if (req instanceof HttpPost && !args.isEmpty()) {
                    try {
                        ((HttpPost)req).setEntity(new UrlEncodedFormEntity(args));
                    } catch (final Exception e) {
                        reportExceptionLater(scriptEntry, e);
                        return;
                    }
                }
                try {
                    CloseableHttpResponse response = client.execute(req);
                    responses.add(response);
                    try {
                        HttpEntity ent = response.getEntity();
                        InputStream stream = ent.getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                        try {
                            scriptEntry.addObject("result", CharStreams.toString(reader));
                        } finally {
                            reader.close();
                        }
                        EntityUtils.consume(ent);
                    } finally {
                        response.close();
                        responses.remove(response);
                    }
                } catch (final Exception e) {
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
