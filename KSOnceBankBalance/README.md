bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic transaction


bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic interaccount --config cleanup.policy=compact

bin/kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic finalbalance \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
    
    
bin/kafka-console-producer --broker-list localhost:9092 --topic cin

bin/kafka-topics --list --zookeeper localhost:2181