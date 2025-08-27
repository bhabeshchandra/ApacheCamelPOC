Url:
1: http://localhost:8081/camel/aggregate/data  
2: http://localhost:8081/camel/service/process?a=10&b=20 
3: http://localhost:8081/camel/soapenpoint/add?a=10&b=250 
4: POST: http://localhost:8081/camel/transform/jsonToXml 
     {
  "name": "Bhabesh",
  "age": 30
} 

5: 
Camel has two ways to do things in parallel:
multicast().parallelProcessing() → sends to all endpoints concurrently (multi-threaded).
parallelProcessing(false) (default) → calls them sequentially (one by one).

------------------------------------------------------------------------------------------
6. API list configurable from application.yml instead of hardcoding.

✅ Step 1 – Add APIs in application.yml
camel:
  external-apis:
    - https://jsonplaceholder.typicode.com/todos/1
    - https://jsonplaceholder.typicode.com/todos/2
    - https://jsonplaceholder.typicode.com/todos/3

✅ Step 2 – Create a @ConfigurationProperties class
package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "camel")
public class ApiConfig {

    private List<String> externalApis;

    public List<String> getExternalApis() {
        return externalApis;
    }

    public void setExternalApis(List<String> externalApis) {
        this.externalApis = externalApis;
    }
}

✅ Step 3 – Update RouteBuilder to use config dynamically
package com.example.camelroutes;

import com.example.config.ApiConfig;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.springframework.stereotype.Component;

import java.util.*;
@Component
public class MyRouteBuilder extends RouteBuilder {
    private final ApiConfig apiConfig;
    public MyRouteBuilder(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }
    @Override
    public void configure() {
        // REST endpoint
        rest("/aggregate")
            .get("/data")
            .to("direct:aggregateData");

        // Main aggregation route
        from("direct:aggregateData")
            .multicast(new JsonListAggregationStrategy())
                .parallelProcessing()
                .to(apiConfig.getExternalApis()
                        .stream()
                        .map(api -> "direct:callApi?api=" + api)
                        .toArray(String[]::new))
            .end()
            .marshal().json(JsonLibrary.Jackson)
            .log("Final Aggregated JSON Response: ${body}");

        // Dynamic "direct:callApi" route for all configured APIs
        from("direct:callApi")
            .process(exchange -> {
                String api = exchange.getFromEndpoint().getEndpointUri().split("api=")[1];
                exchange.getMessage().setHeader(Exchange.HTTP_METHOD, "GET");
                exchange.getMessage().setHeader("apiUrl", api);
            })
            .toD("${header.apiUrl}?bridgeEndpoint=true")
            .unmarshal().json(JsonLibrary.Jackson);
    }

    /**
     * Aggregation strategy to collect JSON responses into a list.
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
