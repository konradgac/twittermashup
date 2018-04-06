import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import model.Collector;
import model.Streamer;
import model.MemcachedJava;

public class Main {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("System");

            //#create-actors



        final ActorRef cacheActor =
                system.actorOf(MemcachedJava.props(), "cacheActor");

        final ActorRef collectorActor =
                system.actorOf(Collector.props(10,cacheActor), "collectorActor");

        final ActorRef streamerActor =
                system.actorOf(Streamer.props(collectorActor), "streamerActor");

            //#create-actors

            //#main-send-messages
            streamerActor.tell(new Streamer.StreamByKeyword("duda"), ActorRef.noSender());

            //#main-send-messages
            while(true){

                try{ Thread.sleep(10);}catch(Exception en){
                    System.out.println("Sleep exception");
                    break;}
                collectorActor.tell(new Collector.UpdateQueue(),ActorRef.noSender());
                cacheActor.tell(new MemcachedJava.GetValue("en"),ActorRef.noSender());
            }

    }
}
