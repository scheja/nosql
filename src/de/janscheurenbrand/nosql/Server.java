package de.janscheurenbrand.nosql;

import de.janscheurenbrand.nosql.handlers.*;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;

import java.io.File;
import java.io.IOException;

/**
 * Created by janscheurenbrand on 12/04/15.
 */
public class Server {

    public static void main(final String[] args) throws IOException {

        AccessLogReceiver logReceiver = message -> {};

        FileResourceManager fileResourceManager = new FileResourceManager(new File("static/"), 100);

        ResourceHandler resourceHandler = Handlers.resource(fileResourceManager).addWelcomeFiles("application.html");
        resourceHandler.setCanonicalizePaths(true);
        resourceHandler.setCacheTime(6000);

        HttpHandler routingHandlers = Handlers.path()
                .addPrefixPath("/", new IndexHandler())
                .addPrefixPath("/static", resourceHandler)
                .addPrefixPath("/sparql", new SparqlHandler())
                .addPrefixPath("/products", new ProductsHandler())
                .addPrefixPath("/finance", new CassandraHandler())
                .addPrefixPath("/shortener", new ShortenerHandler())
                .addPrefixPath("/plain", exchange -> exchange.getResponseSender().send("hello"))
                .addPrefixPath("/exception", exchange -> {
                    throw new RuntimeException("test exception");
                });

        Undertow server = Undertow.builder()
                .addHttpListener(9090, "0.0.0.0")
                .setHandler(new AccessLogHandler(routingHandlers, logReceiver, "common", Server.class.getClassLoader()))
                .build();
        server.start();
    }
}
