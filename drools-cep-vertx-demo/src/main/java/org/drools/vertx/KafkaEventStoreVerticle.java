package org.drools.vertx;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;


//TODO: To re-process events in the correct order, events of related sessions should go into the same Kafka Partition.
public class KafkaEventStoreVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEventStoreVerticle.class);
	
	private static final String KAFKA_CONSUMER_GROUP_ID = "drools-vertx-kafka-consumer";
	
	private static final String EVENTS_KAFKA_TOPIC = "events";
	
	private KafkaProducer<String, String> producer;
	
	/**
	 * Starts the EventBus consumer and Kafka producer.
	 */
	@Override
	public void start() throws Exception {

		LOGGER.info("Starting Drools CEP Kafka Event Store verticle.");

		// Register to GET_EVENTS EventBus address. This address is used by other Verticles to retrieve Events from the store.
		/*
		vertx.eventBus().<String>consumer(Constants.GET_EVENTS_EVENT_BUS_ADDRESS, message -> {	
			//Retrieve the event and set it as a reply to this message.
			message.reply(getEvent(message));
		});
		*/
		producer = KafkaProducer.create(vertx, getKafkaProducerConfig());
		
		//Register for Events on the EventBus.
		vertx.eventBus().<String> consumer(Constants.EVENT_STORE_EVENT_BUS_ADDRESS, message -> {
			handleEvent(message);
		});
	}
	
	/**
	 * Closes the Kafka producer and shuts down the verticle.
	 */
	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		LOGGER.info("Stopping Verticle. Cleaning up resources.");
		producer.close(res -> {
			if (res.succeeded()) {
				stopFuture.complete();
				LOGGER.info("Kafka producer sucessfully stopped.");
				
			} else {
				LOGGER.error("Error stopping Kafka producer.");
				stopFuture.fail(res.cause());
			}
		});
	}

	
	/**
	 * Sends the event as JSON to Kafka.
	 * 
	 * @param message
	 */
	private void handleEvent(Message<String> message) {
		LOGGER.info("Handling event.");
		
		//No need to deserialize into Event as we simply want to store the JSON String.
		String eventJson = message.body();
		String id = new JsonObject(eventJson).getString("id");
		
		storeEvent(id, eventJson);
	}
	
	
	private void storeEvent(String key, String value) {
		LOGGER.info("Storing event with key '" + key + "' and value '" + value + "' in Kafka.");
		
		//KafkaProducerRecord<String, String> record = new KafProducerRecord<>(EVENTS_KAFKA_TOPIC, key, value);
		/*
		KafkaProducerRecord<String, String> record = new KafkaProducerR
		producer.send(record, (recordMetadata, exception) -> {
			if (exception != null) {
				LOGGER.error("Error sending Kafka message: " + exception);
			}
		});
		*/
		
		KafkaProducerRecord<String, String> record = KafkaProducerRecord.create(EVENTS_KAFKA_TOPIC, key, value);
		producer.write(record, res -> {
			if (res.succeeded()) {
				RecordMetadata recordMetadata = res.result();
			      LOGGER.info("Message " + record.value() + " written on topic=" + recordMetadata.getTopic() +
			        ", partition=" + recordMetadata.getPartition() +
			        ", offset=" + recordMetadata.getOffset());
			}
			//TOOD: What do we do when a record is not correctly stored? 
		});
	}
	
	private Map<String, String> getKafkaProducerConfig() {
		Map<String, String> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		config.put(ProducerConfig.RETRIES_CONFIG, "0");	
		config.put(ProducerConfig.ACKS_CONFIG, "1");
		return config;
	}
}
