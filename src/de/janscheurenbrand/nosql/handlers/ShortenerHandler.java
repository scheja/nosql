package de.janscheurenbrand.nosql.handlers;

import de.janscheurenbrand.nosql.util.Params;
import de.janscheurenbrand.nosql.util.Template;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Tuple;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by janscheurenbrand on 12/04/15.
 */
public class ShortenerHandler implements HttpHandler {
    JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        switch (exchange.getRelativePath()) {
            case "": handleIndex(exchange); break;
            case "/stats": handleStats(exchange); break;
            case "/create": handleCreate(exchange); break;
            default: handleRedirect(exchange);
        }
    }

    private void handleCreate(HttpServerExchange exchange) throws Exception {
        String url = Params.getRequestParams(exchange).getOrDefault("url","");
        String shortURL;
        try (Jedis jedis = pool.getResource()) {
            Long id = jedis.incr("shortener:next.id");
            shortURL = Long.toHexString(1000 + id);
            jedis.set("shortener:" + shortURL + ":url", url);
        }
        HashMap<String, String> data = new HashMap<>();
        data.put("url", url);
        data.put("short_url", shortURL);
        exchange.getResponseSender().send(Template.yield("shortener/create", data));
    }

    private void handleIndex(HttpServerExchange exchange) throws Exception {
        exchange.getResponseSender().send(Template.yield("shortener/index", null));
    }

    private void handleRedirect(HttpServerExchange exchange) throws Exception {
        String shortURL = exchange.getRelativePath().substring(1);
        String url = "";
        try (Jedis jedis = pool.getResource()) {
            for (int i = 0; i < 1000; i++) {
                url = jedis.get("shortener:" + shortURL + ":url");
                if (url.length() > 0) {
                    jedis.zincrby("shortener:visits", 1, shortURL);
                }
            }
        }
        exchange.setResponseCode(StatusCodes.FOUND);
        exchange.getResponseHeaders().put(Headers.LOCATION, url);
        exchange.endExchange();
    }

    private void handleStats(HttpServerExchange exchange) throws Exception {
        Set<Tuple> tuples = null;
        try (Jedis jedis = pool.getResource()) {
            for (int i = 0; i < 10; i++) {
                tuples = jedis.zrevrangeByScoreWithScores("shortener:visits", "+inf", "-inf", 0, 1000);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();

        tuples.stream().forEach((tuple) -> {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td><a href='/shortener/");
            stringBuilder.append(tuple.getElement());
            stringBuilder.append("'>");
            stringBuilder.append(tuple.getElement());
            stringBuilder.append("</a></td>");
            stringBuilder.append("<td>");
            stringBuilder.append((int)tuple.getScore());
            stringBuilder.append("</td>");
            stringBuilder.append("</tr>");
        });

        HashMap<String, String> data = new HashMap<>();
        data.put("rows", stringBuilder.toString());
        exchange.getResponseSender().send(Template.yield("shortener/stats", data));
    }
}
