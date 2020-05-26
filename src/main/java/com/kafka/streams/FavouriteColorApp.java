package com.kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public class FavouriteColorApp {
    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "favourite-color");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // process
        StreamsBuilder builder = new StreamsBuilder();
        // 1. stream from kafka
        KStream<String, String> favouriteColorInput = builder.stream("favourite-color-input");
        // 2. filter the bad value
        KStream<String, String> userAndColor = favouriteColorInput.filter((key, value) -> value.contains(","))
                // 3. select the key will be the user id
                .selectKey((key, value) -> value.split(",")[0].toLowerCase())
                // 4. get color from the value
                .mapValues(value -> value.split(",")[1].toLowerCase())
                // 5. filter out undesired color
                .filter((user, color) -> Arrays.asList("green", "blue", "red").contains(color));

        userAndColor.to("user-and-color");

        // read the topic as a KTable
        KTable<String, String> userAndColorTable = builder.table("user-and-color");
        // count the occurrences of the colors
        KTable<String, Long> favouriteColor =  userAndColorTable
                .groupBy((user, color) -> new KeyValue<>(color, color))
                .count(Materialized.as("countByColors"));
        
        // output the results to a kafka topic
        favouriteColor.toStream().to("favourite-color-output", Produced.with(Serdes.String(), Serdes.Long()));

        builder.build();

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.cleanUp();
        streams.start();
        // print the topology
        System.out.println(streams.toString());

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));



    }
}
