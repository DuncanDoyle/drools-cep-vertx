package org.drools.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

/**
 * TODO: Consume from Kafka topic and store in MongoDB ..... or consume from EventBus and store in MongoDb.
 * 
 * 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class MongoEventStoreVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(MongoEventStoreVerticle.class);
	
	private static final int GET_EVENT_FAILURE_CODE = 1;
	
	private MongoClient mongoClient;

	@Override
	public void start() throws Exception {
	
		mongoClient = MongoClient.createShared(vertx, getMongoConfig());
		
		// Register for Events on the EventBus.
		vertx.eventBus().<String> consumer(Constants.EVENT_STORE_EVENT_BUS_ADDRESS, message -> {
			storeEvent(message, result -> {
				if (result.succeeded()) {
					LOGGER.info("Event succesfully stored in MongoDB with id: " + result.result());
				} else {
					//LOG an error. TODO: Maybe we should provide some retry logic ...???
					LOGGER.error("Error storing event in MongoDB.");
				}
				
			});
		});
		
		// Register for requests to retrieve events.
		vertx.eventBus().<String>consumer(Constants.GET_EVENTS_EVENT_BUS_ADDRESS, message -> {	
			//Retrieve the event and set it as a reply to this message.
			//TODO: We might also be able to do this with composable futures ..... or RX.
			getEvent(message, result -> {
				if (result.succeeded()) {
					message.reply(result.result());
				} else {
					message.fail(GET_EVENT_FAILURE_CODE, result.cause().getMessage());
				}
			});
		});
		
	}

		
	private void getEvent(Message<String> message, Handler<AsyncResult<String>> handler) {
		//The message is simply the id as a String
		String id = message.body();
		
		//We return the JSON String representation of the event.
		JsonObject query = new JsonObject().put("id", id);
		
		mongoClient.find("events", query, res -> {
		  if (res.succeeded()) {
			//Should be a single entry and empty if nothing is there.
			handler.handle(Future.succeededFuture(res.result().stream().findFirst().orElse(new JsonObject()).encodePrettily()));
		  } else {
			  handler.handle(Future.failedFuture(res.cause()));
		  }
		});
	}
		
	private void storeEvent(Message<String> message, Handler<AsyncResult<String>> handler) {
		JsonObject document = new JsonObject(message.body());
		mongoClient.save("events", document, res -> {
			if (res.succeeded()) {
				String id = res.result();
				handler.handle(Future.succeededFuture(res.result()));
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});
	}
	
	private JsonObject getMongoConfig() {
		
		JsonObject config = new JsonObject();
		config.put("host", "127.0.0.1").put("port", 27017);
		return config;
		/*
		{
			  // Single Cluster Settings
			  "host" : "127.0.0.1", // string
			  "port" : 27017,      // int

			  // Multiple Cluster Settings
			  "hosts" : [
			    {
			      "host" : "cluster1", // string
			      "port" : 27000       // int
			    },
			    {
			      "host" : "cluster2", // string
			      "port" : 28000       // int
			    },
			    ...
			  ],
			  "replicaSet" :  "foo",    // string
			  "serverSelectionTimeoutMS" : 30000, // long

			  // Connection Pool Settings
			  "maxPoolSize" : 50,                // int
			  "minPoolSize" : 25,                // int
			  "maxIdleTimeMS" : 300000,          // long
			  "maxLifeTimeMS" : 3600000,         // long
			  "waitQueueMultiple"  : 10,         // int
			  "waitQueueTimeoutMS" : 10000,      // long
			  "maintenanceFrequencyMS" : 2000,   // long
			  "maintenanceInitialDelayMS" : 500, // long

			  // Credentials / Auth
			  "username"   : "john",     // string
			  "password"   : "passw0rd", // string
			  "authSource" : "some.db"   // string
			  // Auth mechanism
			  "authMechanism"     : "GSSAPI",        // string
			  "gssapiServiceName" : "myservicename", // string

			  // Socket Settings
			  "connectTimeoutMS" : 300000, // int
			  "socketTimeoutMS"  : 100000, // int
			  "sendBufferSize"    : 8192,  // int
			  "receiveBufferSize" : 8192,  // int
			  "keepAlive" : true           // boolean

			  // Heartbeat socket settings
			  "heartbeat.socket" : {
			  "connectTimeoutMS" : 300000, // int
			  "socketTimeoutMS"  : 100000, // int
			  "sendBufferSize"    : 8192,  // int
			  "receiveBufferSize" : 8192,  // int
			  "keepAlive" : true           // boolean
			  }

			  // Server Settings
			  "heartbeatFrequencyMS" :    1000 // long
			  "minHeartbeatFrequencyMS" : 500 // long
			}
			*/

	}

}
