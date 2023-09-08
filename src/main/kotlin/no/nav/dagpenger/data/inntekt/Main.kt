package no.nav.dagpenger.data.inntekt

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import no.nav.dagpenger.data.inntekt.grunnbeløp.GGrunnbeløp
import no.nav.helse.rapids_rivers.RapidApplication
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.Properties

private val aivenKafka: AivenConfig = AivenConfig.default
private val avroProducerConfig = Properties().apply {
    val schemaRegistryUser =
        requireNotNull(System.getenv("KAFKA_SCHEMA_REGISTRY_USER")) { "Expected KAFKA_SCHEMA_REGISTRY_USER" }
    val schemaRegistryPassword =
        requireNotNull(System.getenv("KAFKA_SCHEMA_REGISTRY_PASSWORD")) { "Expected KAFKA_SCHEMA_REGISTRY_PASSWORD" }

    put(
        KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
        requireNotNull(System.getenv("KAFKA_SCHEMA_REGISTRY")) { "Expected KAFKA_SCHEMA_REGISTRY" }
    )
    put(KafkaAvroSerializerConfig.USER_INFO_CONFIG, "$schemaRegistryUser:$schemaRegistryPassword")
    put(KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO")
    put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
    put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
}

fun main() {
    val env = System.getenv()
    val dataTopic by lazy {
        DataTopic(createProducer(aivenKafka.producerConfig(avroProducerConfig)))
    }

    RapidApplication.create(env) { _, rapidsConnection ->
        rapidsConnection.seekToBeginning()
        InntektRiver(rapidsConnection, dataTopic, GGrunnbeløp(timeToLive = Duration.ofHours(4)))
    }.start()
}

private fun <K, V> createProducer(producerConfig: Properties = Properties()) =
    KafkaProducer<K, V>(producerConfig).also {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                it.close()
            }
        )
    }
