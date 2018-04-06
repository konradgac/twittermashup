package model;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import java.time.LocalDateTime;
import  java.util.concurrent.ConcurrentLinkedQueue;
import java.time.temporal.ChronoUnit;


public class Collector extends AbstractActor {

    static public Props props(long timePeriod,ActorRef memcachedActor) {
        return Props.create(Collector.class, () -> new Collector(timePeriod,memcachedActor));
    }

    //#collector-messages
    static public class AddTweet {
        private  String key;
        private  LocalDateTime timeMarker;
        public AddTweet(String key,LocalDateTime timeMarker){
            this.key=key;
            this.timeMarker = timeMarker;
        }
    }

    static public class UpdateQueue{
        public UpdateQueue(){}
    }
    //#printer-messages



    private Collector(long periodSec,ActorRef memcachedActor) {
        this.periodSec=periodSec;
        this.memcachedActor = memcachedActor;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(AddTweet.class, tweet -> {
                    queue.add(new TweetInfo(tweet.key,tweet.timeMarker));
                    memcachedActor.tell(new MemcachedJava.InsertKey(tweet.key),ActorRef.noSender());
                    //System.out.println("Adding tweet to query: "+queue.size());
                })
                .match(UpdateQueue.class, x -> {

                    while(!queue.isEmpty() && queue.peek().shouldRemove()){
                        System.out.println(queue.size());
                        TweetInfo ti = queue.poll();
                        memcachedActor.tell(new MemcachedJava.DecrementKey(ti.key),ActorRef.noSender());
                        //System.out.println("Removing tweet info: "+ti.key+ " "+queue.size());
                    }
                })
                .build();
    }

    private ConcurrentLinkedQueue<TweetInfo> queue = new ConcurrentLinkedQueue<>();

    private long  periodSec;

    private final ActorRef memcachedActor;

    private class TweetInfo{

        final private String key;
        final private LocalDateTime timeMarker;

        private TweetInfo(String key,LocalDateTime timeMarker){
            this.key=key;
            this.timeMarker = timeMarker;
        }

        private boolean shouldRemove(){
            LocalDateTime now = LocalDateTime.now();
            long p2 = ChronoUnit.SECONDS.between(timeMarker, now);
            return p2 > periodSec;
        }
    }
}

