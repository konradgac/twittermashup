package model;

import akka.actor.AbstractActor;
import akka.actor.Props;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MemcachedJava extends AbstractActor  {

    private MemcachedClient mcc;

    public MemcachedJava(){
        try {
            this.mcc = new MemcachedClient(new
                    InetSocketAddress("127.0.0.1", 11211));
        }
        catch (IOException e)
        {
            System.out.println("Some problems occured");
        }

    }

    static public Props props() {
        return Props.create(MemcachedJava.class, () -> new MemcachedJava());
    }

    static public class InsertKey
    {
        private final String keyword;
        public InsertKey(String keyword) {
            this.keyword=keyword;
        }
    }

    static public class DecrementKey
    {
        private final String keyword;
        public DecrementKey(String keyword) {
            this.keyword=keyword;
        }
    }

    static public class GetValue
    {
        private final String keyword;
        public GetValue(String keyword) {
            this.keyword=keyword;
        }
    }

    static public class ClearData
    {
        private final String keyword;
        public ClearData(String keyword) {
            this.keyword=keyword;
        }
    }



    @Override
    public AbstractActor.Receive createReceive() {

            return receiveBuilder()
                    .match(InsertKey.class, x -> {
                        System.out.println(mcc.get(x.keyword)); // to remove
                        if (mcc.get(x.keyword) == null) {
                            //System.out.println("set" +mcc.set(x.keyword, 0, 0));
                            mcc.set(x.keyword,0,1);
                        }
                        int value = (int) mcc.get(x.keyword);
                        value++;
                        mcc.delete(x.keyword);
                        mcc.set(x.keyword, 0, value);
                    })
                    .match(DecrementKey.class, x -> {
                        int value = (int) mcc.get(x.keyword);
                        value--;
                        mcc.delete(x.keyword);
                        mcc.set(x.keyword, 0, value);
                        if (mcc.get(x.keyword).equals(0)) mcc.delete(x.keyword);
                    })
                    .match(GetValue.class, x -> {
                        Object tmp = mcc.get(x.keyword);
                        if(tmp!=null)
                        System.out.println("get"+ tmp); //mcc.get(x.keyword);

                    })
                    .match(ClearData.class, x -> {
                        mcc.delete(x.keyword);

                    })
                    .build();
        }

    }


