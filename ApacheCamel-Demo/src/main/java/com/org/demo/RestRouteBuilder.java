package com.org.demo;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

import com.org.demo.routes.Person;

@Component
public class RestRouteBuilder extends RouteBuilder{
	  @Override
	    public void configure() throws Exception {
		  System.out.println("Camel process started !!!!");
		  // Define REST configuration
	        restConfiguration()
	            .component("servlet")
	            .contextPath("/camel")
	            //.apiContextPath("/api-doc")
	            .apiProperty("api.title", "Camel REST API")
	            .apiProperty("api.version", "1.0");

	        // Define REST endpoint and route
	        rest("/api")
	            .get("/greet/{name}")
	           .to("direct:helloRoute");
			
			  from("direct:helloRoute")
			  .setBody(constant("Hello, World from Apache Camel Finnaly!!!"))
			  .log("Processed REST request to /api/greet/{name}");
			 
			  
			  // REST endpoint: POST JSON → returns XML
		        rest("/transform")
		            .post("/jsonToXml")
		                .consumes("application/json")
		                .produces("application/xml")
		                .type(Person.class)   // input JSON mapped to Person
		                .to("direct:jsonToXml");

		        from("direct:jsonToXml")
		        .log("Incoming body (raw): ${body}")
	            .unmarshal().json(Person.class)
	            .log("After JSON unmarshal: ${body}")
	            .marshal().jacksonXml(Person.class)
	            .log("After XML marshal: ${body}");
		        
		        
		        
		        // Local REST endpoint
		        rest("/httpenpoint")
		            .get()
		                .to("direct:getFact");

		        // Call external service
		        from("direct:getFact")
		            .log("Calling external API...")
		            //.to("https://catfact.ninja/fact?bridgeEndpoint=true")
		            .to("http://localhost:8080/camelendpoint?bridgeEndpoint=true")
		            .log("External response: ${body}");
		    
	        
				/*
				 * from("direct:helloRoute") .log("Received request with name: ${header.name}")
				 * .transform().simple("Hello, ${header.name}! Greetings from Apache Camel.");
				 * 
				 * from("timer:foo?period=5000") .log("Camel is running...");
				 */
	        
	        /**restConfiguration().component("servlet").port(8080).bindingMode(RestBindingMode.json);

	        rest("/api")
	                .get("/hello")
	                .to("direct:helloRoute");

	        from("direct:helloRoute")
	                .setBody(constant("Hello, World from Apache Camel!"))
	                .log("Processed REST request to /api/hello");
	                **/
	    }
}
