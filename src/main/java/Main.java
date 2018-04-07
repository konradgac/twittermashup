import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import model.CassandraWriter;
import model.Collector;
import model.Streamer;
import model.MemcachedJava;
import net.spy.memcached.MemcachedClient;

import java.net.InetSocketAddress;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends Application {

    private AtomicInteger tick = new AtomicInteger(0);


    @Override
    public void start(Stage stage) {
        final String keyword = "facebook";
        final String langCode = "en";
        final int period = 1;
        final int mod =1;

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setAnimated(false);
        //xAxis.setLabel("Tick");

        yAxis.setAnimated(false);
        //yAxis.setLabel("Value");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Values");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.getData().add(series);

        Scene scene = new Scene(chart, 1400, 900);
        stage.setScene(scene);
        stage.show();

        final ActorSystem system = ActorSystem.create("System");

        //#create-actors



        final ActorRef cacheActor =
                system.actorOf(MemcachedJava.props(), "cacheActor");

        final ActorRef collectorActor =
                system.actorOf(Collector.props(period,cacheActor), "collectorActor");

        final ActorRef writerActor =
                system.actorOf(CassandraWriter.props(), "writerActor");

        final ActorRef streamerActor =
                system.actorOf(Streamer.props(collectorActor,writerActor), "streamerActor");

        //#create-actors


        //#main-send-messages

        Thread updateThread = new Thread(() -> {
            try {
                final MemcachedClient mcc = new MemcachedClient(new InetSocketAddress("127.0.0.1", 11211));

            while (true) {
                try {
                    Thread.sleep(1000);
                    Object object = mcc.get(langCode);
                    int val = object == null ? 0 : (int) object;
                    Platform.runLater(() -> series.getData().add(new XYChart.Data<>(tick.incrementAndGet(), val)));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }catch(Exception e ){
            System.out.println("Some Problem");}
        });

        Thread plotThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20);
                    collectorActor.tell(new Collector.UpdateQueue(),ActorRef.noSender());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        switch (mod){
            case 1:
                //#main-send-messages
                streamerActor.tell(new Streamer.StreamByKeyword(keyword), ActorRef.noSender());
                updateThread.setDaemon(true);
                updateThread.start();
                plotThread.start();

                break;
            case 2:
                //#main-send-messages
                streamerActor.tell(new Streamer.ShowStream(keyword), ActorRef.noSender());
                break;
            case 3:
                streamerActor.tell(new Streamer.Top9(keyword), ActorRef.noSender());
                break;

        }

         }


    public static void main(String[] args) {
        launch(args);
    }
}