package com.org.demo;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;



@Component
public class SoapRouteBuilder extends RouteBuilder{

	  @Override
	    public void configure() throws Exception {
		  System.out.println("Camel process started !!!!");
	        restConfiguration()
	            .component("servlet")
	            .contextPath("/camel")
	            .apiProperty("api.title", "Camel REST API")
	            .apiProperty("api.version", "1.0");

	        rest("/soapenpoint")
            .get("/add")
            .param().name("a").type(RestParamType.query).required(true).endParam()
            .param().name("b").type(RestParamType.query).required(true).endParam()
            .to("direct:callSoap");

        from("direct:callSoap")
            .process(exchange -> {
                int a = Integer.parseInt(exchange.getIn().getHeader("a", String.class));
                int b = Integer.parseInt(exchange.getIn().getHeader("b", String.class));

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
            .setHeader(Exchange.HTTP_METHOD, constant("POST")) // force POST
            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml; charset=utf-8"))
            .setHeader("SOAPAction", constant("\"http://tempuri.org/Add\""))
            .setHeader("Accept-Encoding", constant("identity"))
            .to("http://www.dneonline.com/calculator.asmx?bridgeEndpoint=true")
            .log("SOAP Raw Response: ${body}");

						        
		        
		  ///----------------------------------------------------
		        
		        String soapRequest =
		                "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
		              + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
		              + "  <soap:Body>"
		              + "    <Add xmlns=\"http://tempuri.org/\">"
		              + "      <intA>110</intA>"
		              + "      <intB>220</intB>"
		              + "    </Add>"
		              + "  </soap:Body>"
		              + "</soap:Envelope>";

		        from("timer://foo?repeatCount=1")  // just trigger once for demo
		            .setBody(constant(soapRequest))
		            .setHeader("Content-Type", constant("text/xml; charset=utf-8"))
		            .setHeader("SOAPAction", constant("http://tempuri.org/Add"))
		            .to("http://www.dneonline.com/calculator.asmx") // use http4 or http component
		            .log("SOAP Response: ${body}");
		    
		    }

}
