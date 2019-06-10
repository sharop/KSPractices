package com.sharop.scala.kafka.stream

import java.util.Properties
import java.lang

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{KStream, KTable, KeyValueMapper, Materialized}
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsBuilder, StreamsConfig}
import org.apache.log4j.{Level, LogManager}


object ColorCounter {

  val logger = LogManager.getLogger("ColorCounter")

  def main(args: Array[String]) = {

    logger.setLevel(Level.INFO)

    val bootstrapServers = "localhost:9092"
    val src_topic = "cin"
    val dst_topic = "cout"
    val app_id_config = "color1"


    val stream = CreateInstance(bootstrapServers,app_id_config, src_topic, dst_topic)

    stream.cleanUp()


    stream.start()
    print(stream.toString)

    // shutdown hook to correctly close the streams application
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = {
        stream.close()
      }
    })

    logger.info("Proceso iniciado.")
  }

  def CreateInstance(bootstrapServers:String,
                     app_id_config:String,
                     src_topic:String,
                     dst_topic:String
                    ): KafkaStreams ={

    var builder = new StreamsBuilder

    val   streamingConfig = {
      val settings = new Properties()
      settings.put(StreamsConfig.APPLICATION_ID_CONFIG, app_id_config)
      settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      // Specify default (de)serializers for record keys and for record values.
      settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
      settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)

      //Disable cache not recomendable in prof
      settings.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")

      settings
    }


    val sourceUCO: KStream[String, String] = builder
      .stream(src_topic)
      .filter((k: String, v:String) =>{

      val splitEvent = v.split(",")(1) match {
          case "red" => true
          case "blue" => true
          case "green" => true
          case _ => false
        }
      splitEvent
    })
      .selectKey[String]((_, value) => value.split(",")(0).toLowerCase)
      .map[String, String]{
      new KeyValueMapper[String, String, KeyValue[String, String]] {
        override def apply(key: String, value: String): KeyValue[String, String] = {
          new KeyValue(value.split(',')(0),value.split(',')(1))
        }
      }
    }
    val interTopic = "topointermedio"

    sourceUCO.to(interTopic)

    val ucoTable: KTable[String, String] = builder.table(interTopic)

    val tableUCO: KTable[String, lang.Long] = ucoTable
      .groupBy((key: String, colour: String) => new KeyValue[String, String](colour, colour))
      .count(Materialized.as("counterstore"))

    tableUCO.toStream().to( dst_topic)

    val stream: KafkaStreams = new KafkaStreams(builder.build(), streamingConfig)

    stream

  }
}
