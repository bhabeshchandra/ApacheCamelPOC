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
