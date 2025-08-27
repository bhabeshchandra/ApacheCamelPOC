package com.org.demo;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;

import com.org.demo.config.ServiceConfig;

@Component
public class ConditionalRoute extends RouteBuilder{
	
	   private final ServiceConfig config;

	    public ConditionalRoute(ServiceConfig config) {
	        this.config = config;
	    }
	
	 @Override
	    public void configure() throws Exception {
		 System.out.println("Camel ConditionalRoute process started !!!!");
	        restConfiguration()
	            .component("servlet")
	            .contextPath("/camel")
	            .apiProperty("api.title", "Camel REST API")
	            .apiProperty("api.version", "1.0");
	        
	     // unified REST endpoint
	        rest("/service")
	            .get("/process")
	           // .param().name("a").type().query().required(false).endParam()
	           // .param().name("b").type().query().required(false).endParam()
	            .param().name("a").type(RestParamType.query).required(false).endParam()
	            .param().name("b").type(RestParamType.query).required(false).endParam()
	            .to("direct:entry");

	        // entry point
	        from("direct:entry")
	            .choice()
	                .when(exchange -> "rest".equalsIgnoreCase(config.getServiceType()))
	                    .to("direct:restService")
	                .when(exchange -> "soap".equalsIgnoreCase(config.getServiceType()))
	                    .to("direct:soapService")
	                .otherwise()
	                    .setBody(constant("{\"error\":\"Invalid configuration, use rest or soap\"}"))
	            .end();

	        // external REST API call
	        from("direct:restService")
	        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
	        .setHeader("Accept-Encoding", constant("identity"))
	        .to("https://jsonplaceholder.typicode.com/todos/1?bridgeEndpoint=true")
	        .convertBodyTo(String.class, "UTF-8")     
	        .log("REST Service JSON Response: ${body}")
	        .unmarshal().json(JsonLibrary.Jackson)  
	        .process(exchange -> {
	            Map<String, Object> response = new HashMap<>();
	            response.put("status", "success");
	            response.put("serviceType", "rest");
	            response.put("data", exchange.getIn().getBody(Map.class));
	            exchange.getIn().setBody(response);
	        })
	        .marshal().json(JsonLibrary.Jackson);

	        // external SOAP service call
	        from("direct:soapService")
	            .process(exchange -> {
	                int a = Integer.parseInt(exchange.getIn().getHeader("a", "10", String.class));
	                int b = Integer.parseInt(exchange.getIn().getHeader("b", "20", String.class));

	                String soapBody =
	                        "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
	                        "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	                        "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
	                        "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
	                        "<soap:Body>" +
	                        "<Add xmlns=\"http://tempuri.org/\">" +
	                        "<intA>" + a + "</intA>" +
	                        "<intB>" + b + "</intB>" +
	                        "</Add>" +
	                        "</soap:Body>" +
	                        "</soap:Envelope>";

	                exchange.getIn().setBody(soapBody);
	            })
	            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
	            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml; charset=utf-8"))
	            .setHeader("SOAPAction", constant("\"http://tempuri.org/Add\""))
	            .setHeader("Accept-Encoding", constant("identity"))
	            .to("http://www.dneonline.com/calculator.asmx?bridgeEndpoint=true")
	            .convertBodyTo(String.class)
	            .log("SOAP Service JSON Response: ${body}")
	            .process(exchange -> {
	                String soapResponse = exchange.getIn().getBody(String.class);

	                String resultValue = "unknown";
	                if (soapResponse.contains("<AddResult>")) {
	                    resultValue = soapResponse.split("<AddResult>")[1].split("</AddResult>")[0];
	                }

	                Map<String, Object> response = new HashMap<>();
	                response.put("status", "success");
	                response.put("serviceType", "soap");
	                response.put("data", Map.of("sum", resultValue));
	                exchange.getIn().setBody(response);
	            })
	            .marshal().json();
	        
	        
	        
	        
	        
	        

	        // REST API (our entrypoint)
	       /** rest("/api")
	            .get("/process")
	            .param().name("type").required(true).endParam()
	            .param().name("a").required(false).endParam()
	            .param().name("b").required(false).endParam()
	            .to("direct:routeBasedOnType");

	        from("direct:routeBasedOnType")
	            .choice()
	                .when(header("type").isEqualTo("rest"))
	                    .to("direct:callExternalRest")
	                .when(header("type").isEqualTo("soap"))
	                    .to("direct:callExternalSoap")
	                .otherwise()
	                    .setBody(constant("{\"error\":\"Unknown type\"}"))
	            .end();

	        // External REST Call Example
	        from("direct:callExternalRest")
	            .setHeader(Exchange.HTTP_METHOD, constant("GET"))
	            .to("https://jsonplaceholder.typicode.com/todos/1?bridgeEndpoint=true")
	            .log("REST API JSON Response: ${body}");
	           
	        // External SOAP Call Example
	        from("direct:callExternalSoap")
	            .process(exchange -> {
	                int a = Integer.parseInt(exchange.getIn().getHeader("a", "0", String.class));
	                int b = Integer.parseInt(exchange.getIn().getHeader("b", "0", String.class));

	                String soapBody =
	                        "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
	                      + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
	                      + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
	                      + "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
	                      + "  <soap:Body>"
	                      + "    <Add xmlns=\"http://tempuri.org/\">"
	                      + "      <intA>" + a + "</intA>"
	                      + "      <intB>" + b + "</intB>"
	                      + "    </Add>"
	                      + "  </soap:Body>"
	                      + "</soap:Envelope>";

	                exchange.getIn().setBody(soapBody);
	            })
	            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
	            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml; charset=utf-8"))
	            .setHeader("SOAPAction", constant("\"http://tempuri.org/Add\""))
	            .setHeader("Accept-Encoding", constant("identity"))
	            .to("http://www.dneonline.com/calculator.asmx?bridgeEndpoint=true")
	            .process(exchange -> {
	                // Parse SOAP XML and extract AddResult
	                String body = exchange.getIn().getBody(String.class);
	                Document doc = DocumentBuilderFactory.newInstance()
	                        .newDocumentBuilder()
	                        .parse(new ByteArrayInputStream(body.getBytes()));

	                String result = doc.getElementsByTagName("AddResult")
	                        .item(0)
	                        .getTextContent();

	                // Return JSON instead of SOAP
	                exchange.getIn().setBody("{\"AddResult\":" + result + "}");
	            })
	            .log("SOAP Service JSON Response: ${body}");
	            */
	    }
	}

