package model;

import akka.actor.AbstractActor;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;


public class CassandraReader extends AbstractActor {

    SparkConf conf = new SparkConf()
            .setAppName( "My application");
    SparkContext sc = new SparkContext(conf);


    @Override
    public Receive createReceive() {
        return null;
    }


}