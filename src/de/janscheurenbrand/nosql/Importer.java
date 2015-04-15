package de.janscheurenbrand.nosql;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by janscheurenbrand on 14/04/15.
 */
public class Importer {
    static final String AB = "0123456789abcdefghijklmnopqrstuvwxyz";
    static Random rnd = new Random();

    public static void main(String[] args) {
        importURLs();
    }

    private static void importURLs() {
        JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
        try (Jedis jedis = pool.getResource()) {
            for (int i = 0; i < 1000000; i++) {
                String url = "http://"+randomString(100)+".com/?"+randomString(150)+"="+randomString(350)+"&"+randomString(150)+"="+randomString(380);
                Long id = jedis.incr("shortener:next.id");
                String shortURL = Long.toHexString(1000 + id);
                jedis.set("shortener:" + shortURL + ":url", url);
            }

        }

    }


    private static String randomString( int len ) {
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < rnd.nextInt(len)+2; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    private static void importCassandraStuff() {
        Cluster cluster = Cluster.builder().addContactPoint("192.168.10.112").build();
        Session session = cluster.connect("Seminar");

        List<Path> files = new ArrayList<>();
        Path dir = FileSystems.getDefault().getPath("/Users/janscheurenbrand/Downloads/cassandra-data");

        System.out.println("Loading list of files...");
        getFileNames(files, dir);
        System.out.println("Got List of Files...");

        final int[] a = {37608};
        int b = files.size()-1;

        List<Path> mini = files.subList(a[0],b);

        System.out.println("Starting Import");

        long startTime = System.currentTimeMillis();

        mini.stream().forEach(file -> {
            String file_name = file.getFileName().toString();
            a[0]++;

            if (file_name.endsWith(".csv")) {
                String series_id = file_name.substring(0, file_name.indexOf("."));

                try {
                    List<String> lines = Files.readAllLines(file);

                    lines.subList(1, lines.size()-1).forEach(line -> {

                        String[] rows = line.split(",");

                        String string = String.format("INSERT INTO series (series_id, series_time, series_value) VALUES ('%s', '%s', '%s')", series_id, rows[0], rows[1]);

                        //System.out.println(string);
                        session.execute(string);


                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("" + a[0] + " --- " + (double)a[0]/(double)b);


        });

        long endTime = System.currentTimeMillis();

        System.out.println("Insert execution time: " + (endTime-startTime) + "ms");


    }

    private static List<Path> getFileNames(List<Path> fileNames, Path dir) {
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if(path.toFile().isDirectory()) {
                    getFileNames(fileNames, path);
                } else {
                    fileNames.add(path.toAbsolutePath());
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return fileNames;
    }
}
