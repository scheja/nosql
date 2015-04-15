package de.janscheurenbrand.nosql.handlers;

import de.janscheurenbrand.nosql.util.Template;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * Created by janscheurenbrand on 13/04/15.
 */
public class IndexHandler implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Sender sender = exchange.getResponseSender();
        sender.send(Template.yield("index", null));
    }
}
