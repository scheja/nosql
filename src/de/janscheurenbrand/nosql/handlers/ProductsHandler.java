package de.janscheurenbrand.nosql.handlers;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.janscheurenbrand.nosql.util.Params;
import de.janscheurenbrand.nosql.util.Template;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by janscheurenbrand on 14/04/15.
 */
public class ProductsHandler implements HttpHandler {

    MongoClient mongo = new MongoClient();
    MongoDatabase db = mongo.getDatabase("seminar");
    MongoCollection products = db.getCollection("products");

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        switch (exchange.getRelativePath()) {
            case "": handleIndex(exchange); break;
            case "/create": handleCreate(exchange); break;
            default: handleShow(exchange);
        }
    }

    private void handleCreate(HttpServerExchange exchange) throws Exception {
        String json = Params.getRequestParams(exchange).getOrDefault("json","");

        Document newProduct = Document.parse(json);

        products.insertOne(newProduct);

        exchange.setResponseCode(StatusCodes.FOUND);
        exchange.getResponseHeaders().put(Headers.LOCATION, "/products");
        exchange.endExchange();
    }

    private void handleIndex(HttpServerExchange exchange) {
        ArrayList<Document> productList = new ArrayList<>();

        products.find().into(productList);

        StringBuilder sb = new StringBuilder();
        sb.append("<table class='table'>");
        sb.append("<thead><tr>" +
                "<th>Name</th><th>Hersteller</th><th>Preis</th>" +
                "</tr></thead>");

        sb.append("<tbody>");

        productList.stream().forEach(product -> {
            sb.append("<tr>");
            sb.append("<td><a href='/products/");
            sb.append(product.get("_id"));
            sb.append("'>");
            sb.append(product.get("name"));
            sb.append("</a></td>");
            sb.append("<td>");
            sb.append(product.get("manufacturer"));
            sb.append("</td>");
            sb.append("<td>");
            sb.append(product.get("price"));
            sb.append("</td>");
            sb.append("</tr>");
        });


        sb.append("</tbody>");
        sb.append("</table>");

        HashMap<String, String> data = new HashMap<>();
        data.put("entries", String.valueOf(productList.size()));
        data.put("table", sb.toString());

        exchange.getResponseSender().send(Template.yield("products/index", data));
    }

    private void handleShow(HttpServerExchange exchange) throws Exception {
        String product_id = exchange.getRelativePath().substring(1);

        BasicDBObject query = new BasicDBObject("_id", new ObjectId(product_id));

        Document foundDocument = (Document) products.find(query).first();




        StringBuilder sb = new StringBuilder();
        sb.append("<table class='table'>");
        sb.append("<tbody>");

        foundDocument.entrySet().stream().forEach(entry -> {
            if (!entry.getKey().equals("_id") && !entry.getKey().equals("name") && !entry.getKey().equals("manufacturer") ) {
                sb.append("<tr>");
                sb.append("<td>");
                sb.append(entry.getKey());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(entry.getValue().toString());
                sb.append("</td>");
                sb.append("</tr>");
            }
        });

        sb.append("</tbody>");
        sb.append("</table>");

        HashMap<String, String> data = new HashMap<>();
        data.put("table", sb.toString());
        data.put("name", foundDocument.get("name").toString());
        data.put("manufacturer", foundDocument.get("manufacturer").toString());


        exchange.getResponseSender().send(Template.yield("products/show", data));

    }
}
