package de.janscheurenbrand.nosql.handlers;

import com.hp.hpl.jena.query.*;
import de.janscheurenbrand.nosql.util.Params;
import de.janscheurenbrand.nosql.util.Template;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.HashMap;


/**
 * Created by janscheurenbrand on 13/04/15.
 */
public class SparqlHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        switch (exchange.getRelativePath()) {
            case "": handleIndex(exchange); break;
            case "/query": handleQuery(exchange); break;
            default: handleIndex(exchange);
        }

    }

    private void handleIndex(HttpServerExchange exchange) {
        exchange.getResponseSender().send(Template.yield("sparql/index", null));
    }

    private void handleQuery(HttpServerExchange exchange) throws Exception {
        String sparql = Params.getRequestParams(exchange).getOrDefault("sparql","");
        System.out.println(sparql);
        Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);

        QueryExecution qExe = QueryExecutionFactory.sparqlService("http://10.211.55.7:8890/sparql", query);
        ResultSet results = qExe.execSelect();

        HashMap<String,String> data = new HashMap<>();
        data.put("sparql", escapeHTML(sparql));
        data.put("res", escapeHTML(ResultSetFormatter.asText(results)));
        exchange.getResponseSender().send(Template.yield("sparql/result", data));
    }

    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
