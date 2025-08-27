/*
 * package com.org.demo.routes;
 * 
 * import org.apache.camel.builder.RouteBuilder; import
 * org.apache.camel.model.rest.RestBindingMode; import
 * org.springframework.stereotype.Component;
 * 
 * @Component public class RestRouteBuilder extends RouteBuilder{
 * 
 * @Override public void configure() throws Exception {
 * 
 * // Define REST configuration restConfiguration() .component("servlet")
 * .contextPath("/camel") .apiContextPath("/api-doc") .apiProperty("api.title",
 * "Camel REST API") .apiProperty("api.version", "1.0");
 * 
 * // Define REST endpoint and route rest("/api") .get("/greet/{name}")
 * .to("direct:helloRoute");
 * 
 * from("direct:helloRoute")
 * .setBody(constant("Hello, World from Apache Camel!"))
 * .log("Processed REST request to /api/greet/{name}");
 * 
 * 
 * from("direct:helloRoute") .log("Received request with name: ${header.name}")
 * .transform().simple("Hello, ${header.name}! Greetings from Apache Camel.");
 * 
 * from("timer:foo?period=5000") .log("Camel is running...");
 * 
 *//**
	 * restConfiguration().component("servlet").port(8080).bindingMode(RestBindingMode.json);
	 * 
	 * rest("/api") .get("/hello") .to("direct:helloRoute");
	 * 
	 * from("direct:helloRoute") .setBody(constant("Hello, World from Apache
	 * Camel!")) .log("Processed REST request to /api/hello");
	 **//*
		 * } }
		 */