package com.sharop.scala.kafka.stream

import java.nio.charset.Charset
import java.util

import org.apache.kafka.common.serialization.Serializer
import spray.json.{JsonWriter, _}

class JsonPOJOSerializer[T](implicit writer: JsonWriter[T]) extends Serializer[T] {

  val utf8 = Charset.forName("UTF-8")

  def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {
    configs.get("JsonPOJOClass").asInstanceOf[T]

  }

  def serialize(topic: String, data: T): Array[Byte] = data.toJson.prettyPrint.getBytes(utf8)

  def close(): Unit = {}
}

///https://github.com/TanUkkii007/kafka-book-scala/blob/master/src/main/scala/tanukkii/kafkabook/example/stream/pageview/JsonPOJOSerializer.scala