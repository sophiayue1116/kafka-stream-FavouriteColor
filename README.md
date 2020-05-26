# kafka-stream-FavouriteColor
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic favourite-color-input
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic user-and-color --config cleanup.policy=compact
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic favourite-color-output --config cleanup.policy=compact

bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic favourite-color-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserialzer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer


bin/kafka-console-producer.sh --broker-list localhost:9092 --topic favourite-color-input
sophie,red
Tom,blue
Grace,white
Pennyhi
sophie,green

with .jar:
java -jar com.kafka.stream-1.0-SNAPSHOT-jar-with-dependencies.jar
