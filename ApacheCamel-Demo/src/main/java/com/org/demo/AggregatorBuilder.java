package com.org.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class AggregatorBuilder extends RouteBuilder{
	 @Override
	    public void configure() throws Exception {
		 restConfiguration()
         .component("servlet")
         .contextPath("/camel")
         .apiProperty("api.title", "Camel REST API")
         .apiProperty("api.version", "1.0");
		 
	        // REST endpoint exposed by our Spring Boot app
	        rest("/aggregate")
	            .get("/data")
	            .to("direct:aggregateData");

	        
	     // Main aggregation route
	        from("direct:aggregateData")
	            .multicast(new JsonListAggregationStrategy())   // 👈 custom aggregation
	                .parallelProcessing()
	                .to("direct:api1", "direct:api2", "direct:api3")
	            .end()
	            .marshal().json(JsonLibrary.Jackson)
	            .log("Final Aggregated JSON Response: ${body}");
	        // External API 1
	        from("direct:api1")
	            .setHeader(Exchange.HTTP_METHOD, constant("GET"))
	            .setHeader("Accept-Encoding", constant("identity"))
	            .to("https://jsonplaceholder.typicode.com/todos/1?bridgeEndpoint=true")
	            .convertBodyTo(String.class, "UTF-8")
	            .unmarshal().json(JsonLibrary.Jackson); // parse JSON into Map

	        // External API 2
	        from("direct:api2")
	            .setHeader(Exchange.HTTP_METHOD, constant("GET"))
	            .setHeader("Accept-Encoding", constant("identity"))
	            .to("https://jsonplaceholder.typicode.com/todos/2?bridgeEndpoint=true")
	            .convertBodyTo(String.class, "UTF-8")
	            .unmarshal().json(JsonLibrary.Jackson);

	        // External API 3
	        from("direct:api3")
	            .setHeader(Exchange.HTTP_METHOD, constant("GET"))
	            .setHeader("Accept-Encoding", constant("identity"))
	            .to("https://jsonplaceholder.typicode.com/todos/3?bridgeEndpoint=true")
	            .convertBodyTo(String.class, "UTF-8")
	            .unmarshal().json(JsonLibrary.Jackson);
	    }
	 
	 
	 /**
	     * Aggregation strategy to collect all JSON objects into a list.
	     */
	    static class JsonListAggregationStrategy implements AggregationStrategy {
	        @Override
	        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
	            List<Object> list;
	            if (oldExchange == null) {
	                list = new ArrayList<>();
	                list.add(newExchange.getMessage().getBody());
	                newExchange.getMessage().setBody(list);
	                return newExchange;
	            } else {
	                list = oldExchange.getMessage().getBody(List.class);
	                list.add(newExchange.getMessage().getBody());
	                return oldExchange;
	            }
	        }
	    }
}
