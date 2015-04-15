package de.janscheurenbrand.nosql.handlers;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import de.janscheurenbrand.nosql.util.Template;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by janscheurenbrand on 14/04/15.
 */
public class CassandraHandler implements HttpHandler {
    Cluster cluster = Cluster.builder().addContactPoint("10.37.129.3").build();
    Session session = cluster.connect("Seminar");


    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        switch (exchange.getRelativePath()) {
            case "": handleIndex(exchange); break;
            case "/get": handleGetSeries(exchange); break;
            default: handleIndex(exchange);
        }
    }

    private void handleIndex(HttpServerExchange exchange) {
        exchange.getResponseSender().send(Template.yield("finance/index", null));
    }

    private void handleGetSeries(HttpServerExchange exchange) throws Exception {
        String series_id = exchange.getQueryString().substring(7);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        List<Row> res = null;
        List<Double> values = new ArrayList<>();

        for (int i = 0; i<100; i++) {
            ResultSet results = session.execute(String.format("SELECT * FROM series WHERE series_id='%s'",series_id));
            res = results.all();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table class='table'>");
        sb.append("<thead><tr>" +
                "<th>Datum</th><th>Wert</th>" +
                "</tr></thead>");

        sb.append("<tbody>");

        res.forEach(row -> {
            sb.append("<tr>");
            sb.append("<td>");
            sb.append(dateFormat.format(row.getDate("series_time")));
            sb.append("</td>");
            sb.append("<td>");
            sb.append(row.getString("series_value"));
            try {
                values.add(Double.valueOf(row.getString("series_value")));
            } catch (Exception e) {

            }
            sb.append("</td>");
            sb.append("</tr>");
        });

        sb.append("</tbody>");
        sb.append("</table>");

        final double[] sum = {0.0};
        values.stream().forEach(i -> sum[0] = sum[0] +i);

        HashMap<String, String> data = new HashMap<>();
        data.put("entries", String.valueOf(res.size()));
        data.put("avg", String.valueOf(sum[0] /values.size()));
        data.put("series_id", series_id);
        data.put("table", sb.toString());
        exchange.getResponseSender().send(Template.yield("finance/table", data));
    }

}
