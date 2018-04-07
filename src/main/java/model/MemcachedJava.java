package model;

import akka.actor.AbstractActor;
import akka.actor.Props;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class MemcachedJava extends AbstractActor  {

    private MemcachedClient mcc;
    private final String keyTab[]={"one", "two","three","four","five","six","seven","eight","nine","ten"};

    public MemcachedJava(){
        try {
            this.mcc = new MemcachedClient(new
                    InetSocketAddress("127.0.0.1", 11211));
            this.mcc.flush();
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

    static  class DecrementKey
    {
        private final String keyword;
        DecrementKey(String keyword) {
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

    static public class Insert
    {
        private final int followers;
        private final String keyword;
        public Insert(String keyword,int followers) {
            this.keyword=keyword;
            this.followers=followers;
        }
    }

    @Override
    public AbstractActor.Receive createReceive() {

            return receiveBuilder()
                    .match(InsertKey.class, x -> {
                        //System.out.println(mcc.get(x.keyword)); // to remove
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
                    .match(Insert.class, x -> {
                        //System.out.println(mcc.get(x.keyword)); // to remove

                        int[] tabFollowers = new int[10];

                        for(int i =0;i<10;i++){
                            Object tmp= mcc.get(keyTab[i]);
                            tabFollowers[i]= tmp == null ? 0 : (int)tmp;
                        }

                        if(x.followers > tabFollowers[0] ) {
                            tabFollowers[0] = x.followers;
                            Arrays.sort(tabFollowers);
                            for(int i =0;i<10;i++){
                                mcc.delete(keyTab[0]);
                                mcc.set(keyTab[i], 0, tabFollowers[i]);
                            }

                            for(int i =1;i<10;i++){
                                System.out.print( tabFollowers[i]+"; ");

                            }
                            System.out.println("end");
                        }

                    })

                    .match(ClearData.class, x -> {
                        mcc.delete(x.keyword);

                    })
                    .build();
        }

    }


