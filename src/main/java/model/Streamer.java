package model;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import java.time.LocalDateTime;


public class Streamer extends AbstractActor {

    static public Props props(ActorRef collector) {
        return Props.create(Streamer.class, () -> new Streamer(collector));
    }

    //#model.Streamer-messages


    static public class StreamByKeyword {
        private final String keyword;

        public StreamByKeyword(String keyword) {
            this.keyword=keyword;

        }
    }

    static public class Kill {
        public Kill() { }
    }

    //#model.Streamer-messages

    //#Configuration of twitter stream
    private final Configuration configuration = new ConfigurationBuilder()
            .setDebugEnabled(true)
            .setOAuthConsumerKey("72Wsxiaa34YWUsvFiWeOYcYtc")
            .setOAuthConsumerSecret("mACzkZTpcn7NZVV3epLA4mT633kqhllICic5Eh7HWUnGujGYFH")
            .setOAuthAccessToken("981949032242253824-JSb3CAoTEfCR4Gl9klArll9NhHymORO")
            .setOAuthAccessTokenSecret("bYkRiddbTvGqQyjaYuxKG0qFKZRIa6ygOvz1mY32IyZzc")
            .build();

    private final ActorRef collector;
    private ActorRef getCollector(){
        return collector;
    }

    private Streamer(ActorRef collector) {
        this.collector=collector;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StreamByKeyword.class, sbk -> {
                    TwitterStream twitterStream = new TwitterStreamFactory(configuration).getInstance();
                    twitterStream.addListener(new CountryListener(getCollector()));
                    twitterStream.filter(new FilterQuery().track(sbk.keyword));
                })
                .match(Kill.class, x -> {

                })

                .build();
    }

    //Listener Class for processing stream
    private class CountryListener implements StatusListener{

        private final ActorRef collector;

        private CountryListener(ActorRef collector){
          this.collector =collector;
        }

        public void onStatus(Status status) {
            System.out.println(status.getLang());
                collector.tell(new Collector.AddTweet(status.getLang(), LocalDateTime.now()), ActorRef.noSender());

        }

        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

        public void onScrubGeo(long l, long l1) {}

        public void onStallWarning(StallWarning stallWarning) {}

        public void onException(Exception ex) {
            ex.printStackTrace();
        }
    }


}
