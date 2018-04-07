package model;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import twitter4j.HashtagEntity;

import java.text.SimpleDateFormat;

public class CassandraWriter extends AbstractActor {

    static public Props props() {
        return Props.create(CassandraWriter.class, () -> new CassandraWriter());
    }



    private Cluster cluster;
    private Session session;
    private String SERVERIP = "127.0.0.1";
    private String KEYSPACE = "tweets_db ";
    private String TABLE = "Tweet";
    private String ATTRIBUTES = "(Id, date, language, hashtag, content) ";

    private void connect() {
        cluster = Cluster.builder()
                .addContactPoints(SERVERIP)
                .build();

        session = cluster.connect(KEYSPACE);
    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        session.close();
        cluster.close();
    }

    public CassandraWriter(){
        connect();
        System.out.println("connected");
    }

    public void write(Tweet tweet){
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        StringBuilder sb = new StringBuilder("INSERT INTO ").
                append(TABLE).append(ATTRIBUTES).
                append("VALUES ").append("( ").
                append(tweet.getId()).append(", '").
                append(DATE_FORMAT.format(tweet.getDate())).append("', '").
                append(tweet.getLanguage()).append("', '").
                append(tweet.getHashtag()).append("', '").
                append(tweet.getContent()).
                append("');");
        System.out.println(sb.toString());
        String query = sb.toString();
        session.execute(query);
    }


    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Tweet.class, tweet -> {
                    System.out.println(tweet);
                    write(tweet);
                    System.out.println("Writted!!!");
                })
                .build();
    }

}