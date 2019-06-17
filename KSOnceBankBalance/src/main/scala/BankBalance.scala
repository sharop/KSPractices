package com.sharop.scala.kafka.stream

import java.time.{Instant, ZonedDateTime}
import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized._
import org.apache.kafka.streams.kstream.{Consumed, KTable, Produced}
import org.apache.kafka.streams.state.{KeyValueStore, SessionStore}
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsBuilder, StreamsConfig}
import org.apache.log4j.{Level, LogManager}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat






object BankBalance {
  case class Transaction(name: String, amount: Float, time: String)
  case class Balance(count: Long = 0, amount: Float =0, time: String = "")

  val logger = LogManager.getLogger("BankBalance")

  def main(args: Array[String]) = {
    logger.setLevel(Level.INFO)

    val bootstrapServers = "localhost:9092"
    val src_topic = "transaction"
    val dst_topic = "finalbalance"
    val app_id = "balanceapp_4"

    val stream = CreateInstance(bootstrapServers, app_id, src_topic, dst_topic)

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

  implicit val TransactionJsonFormat: RootJsonFormat[Transaction] = jsonFormat3(Transaction)
 implicit val BalanceJsonFormat: RootJsonFormat[Balance] = jsonFormat3(Balance)


  def CreateInstance(bootstrapServers: String,
                     app_id_config: String,
                     src_topic: String,
                     dst_topic: String
                    ): KafkaStreams = {

    var builder = new StreamsBuilder

    val serdeprops = new java.util.HashMap[String, Any]()


    val tSerializer = new JsonPOJOSerializer[Transaction]()
    serdeprops.put("JsonPOJOClass", classOf[Transaction])
    tSerializer.configure(serdeprops, false)
    val tDeserializer = new JsonPOJODeserializer[Transaction]()
    tDeserializer.configure(serdeprops, false)
    val transactionSerde = Serdes.serdeFrom(tSerializer, tDeserializer)


    val bSerializer = new JsonPOJOSerializer[Balance]()
    serdeprops.put("JsonPOJOClass", classOf[Balance])
    bSerializer.configure(serdeprops, false)
    val bDeserializer = new JsonPOJODeserializer[Balance]()
    bDeserializer.configure(serdeprops, false)
    val balanceSerde = Serdes.serdeFrom(bSerializer, bDeserializer)


    val streamingConfig = {
      val settings = new Properties()
      settings.put(StreamsConfig.APPLICATION_ID_CONFIG, app_id_config)
      settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      // Specify default (de)serializers for record keys and for record values.
      settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
      settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Float.getClass.getName)
      settings.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)

      //Disable cache not recomendable in prof
      settings.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")

      settings
    }


    val sourceAccount: KTable[String, Balance] = builder
      .stream(src_topic, Consumed.`with`(Serdes.String(), transactionSerde))
      .groupByKey()
      .aggregate(
          () => Balance(),
          (aggKey, trans, bal) => CalculateBalance(trans, bal),
          as[String, Balance, KeyValueStore[Bytes, Array[Byte]]]("StoreBalance").withValueSerde(balanceSerde)
      )


    sourceAccount.toStream.to(dst_topic )

    val stream: KafkaStreams = new KafkaStreams(builder.build(), streamingConfig)
    stream

  }
  def CalculateBalance(trans: Transaction, bal: Balance):Balance = {

    val balEpoch = bal.time match {
      case "" => Instant.now().toEpochMilli
      case _ => Instant.parse(bal.time).toEpochMilli
    }

    val traEpoch = ZonedDateTime.parse(trans.time).toInstant().toEpochMilli
    val newBalanceTime = Instant.ofEpochMilli(Math.max(balEpoch,traEpoch))
    val newBalance = Balance(bal.count+1, bal.amount+trans.amount,time = newBalanceTime.toString)
    newBalance
  }



}
