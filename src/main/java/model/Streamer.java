package model;
import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.Props;
import akka.actor.ActorRef;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import java.time.LocalDateTime;


public class Streamer extends AbstractActor {

    static public Props props(ActorRef collector,ActorRef writer) {
        return Props.create(Streamer.class, () -> new Streamer(collector,writer));
    }

    //#model.Streamer-messages


    static public class StreamByKeyword {
        private final String keyword;

        public StreamByKeyword(String keyword) {
            this.keyword=keyword;

        }
    }

    static public class ShowStream {
        private final String keyword;

        public ShowStream(String keyword) {
            this.keyword=keyword;

        }
    }

    static public class Top9 {
        private final String keyword;

        public Top9(String keyword) {
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
    private final ActorRef writer;

    private ActorRef getCollector(){
        return collector;
    }

    private ActorRef getWriter(){
        return writer;
    }

    private Streamer(ActorRef collector,ActorRef writer) {
        this.collector=collector;
        this.writer=writer;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StreamByKeyword.class, sbk -> {
                    TwitterStream twitterStream = new TwitterStreamFactory(configuration).getInstance();
                    twitterStream.addListener(new CountryListener(getCollector(),getWriter()));
                    twitterStream.filter(new FilterQuery().track(sbk.keyword));
                })
                .match(Kill.class, x -> {

                })
                .match(ShowStream.class, sbk -> {
                    TwitterStream twitterStream = new TwitterStreamFactory(configuration).getInstance();
                    twitterStream.addListener(new BasicListener(getWriter()));
                    twitterStream.filter(new FilterQuery().track(sbk.keyword));
                })
                .match(Top9.class, sbk -> {
                    TwitterStream twitterStream = new TwitterStreamFactory(configuration).getInstance();
                    twitterStream.addListener(new Top10Listener(getWriter(),collector));
                    twitterStream.filter(new FilterQuery().track(sbk.keyword));
                })
                .build();
    }

    //Listener Class for processing stream
    private class CountryListener implements StatusListener{

        private final ActorRef collector;
        private final ActorRef writer;

        private CountryListener(ActorRef collector, ActorRef writer){
          this.collector =collector;
          this.writer=writer;
        }

        public void onStatus(Status status) {
            //System.out.println(status.getLang());
            collector.tell(new Collector.AddTweet(status.getLang(), LocalDateTime.now()), ActorRef.noSender());
            Tweet tweet = new Tweet(status.getId(), Tweet.hashtagString(status.getHashtagEntities()), status.getLang(), status.getText(), status.getCreatedAt());
            writer.tell(tweet, ActorRef.noSender());
        }

        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

        public void onScrubGeo(long l, long l1) {}

        public void onStallWarning(StallWarning stallWarning) {}

        public void onException(Exception ex) {
            ex.printStackTrace();
        }
    }

    private class Top10Listener implements StatusListener{


        private final ActorRef writer;
        private final ActorRef collector;

        private Top10Listener(ActorRef writer, ActorRef collector){
            this.writer=writer;
            this.collector = collector;
        }

        public void onStatus(Status status) {

            collector.tell(new Collector.UpdateTop10(status.getId()+"",status.getUser().getFollowersCount()),ActorRef.noSender());
            Tweet tweet = new Tweet(status.getId(), Tweet.hashtagString(status.getHashtagEntities()), status.getLang(), status.getText(), status.getCreatedAt());
            writer.tell(tweet, ActorRef.noSender());
        }


        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

        public void onScrubGeo(long l, long l1) {}

        public void onStallWarning(StallWarning stallWarning) {}

        public void onException(Exception ex) {
            ex.printStackTrace();
        }
    }

    private class BasicListener implements StatusListener{


        private final ActorRef writer;

        private BasicListener( ActorRef writer){
            this.writer=writer;
        }

        public void onStatus(Status status) {
            System.out.println(status.getId()+ " "+status.getUser().getName()+ " "+ status.getText()+ " ");
            Tweet tweet = new Tweet(status.getId(), Tweet.hashtagString(status.getHashtagEntities()), status.getLang(), status.getText(), status.getCreatedAt());
            writer.tell(tweet, ActorRef.noSender());
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
