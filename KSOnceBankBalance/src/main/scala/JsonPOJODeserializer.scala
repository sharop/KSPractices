package com.sharop.scala.kafka.stream


import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.apache.kafka.common.serialization.Deserializer
import com.typesafe.scalalogging.LazyLogging


class JsonPOJODeserializer[T >: Null] (implicit val m : Manifest[T]) extends Deserializer[T] with LazyLogging {
  val objectMapper = new ObjectMapper with ScalaObjectMapper

  objectMapper.registerModule(DefaultScalaModule)




  def configure(props: util.Map[String, _], isKey: Boolean): Unit = {
    props.get("JsonPOJOClass").asInstanceOf[T]
  }

  def close(): Unit = {}

  def deserialize(topic: String, bytes: Array[Byte]): T = bytes match{
    case _ =>
      try {
         val op = objectMapper.readValue(bytes, m.runtimeClass.asInstanceOf[Class[T]])
        op
    }catch{
        case e: Exception =>
          val jsonStr = new String(bytes, "UTF-8")
          logger.warn(s"Failed parsing ${jsonStr}", e)
          null
    }

  }
}


//https://github.com/TanUkkii007/kafka-book-scala/blob/master/src/main/scala/tanukkii/kafkabook/example/stream/pageview/JsonPOJODeserializer.scala