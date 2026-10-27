package edu.eci.orders;
import edu.eci.orders.lambda.*;
import com.amazonaws.services.lambda.runtime.events.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class OrdersTest {
 @Test void createAndRetrieve(){var s=new OrderService();var o=s.createOrder(new CreateOrderRequest("CUST-42","MacBook Air",1));assertEquals(o,s.getOrder(o.id()));assertTrue(o.id().startsWith("ORD-"));}
 @Test void missing(){assertThrows(OrderNotFoundException.class,()->new OrderService().getOrder("missing"));}
 @Test void search(){var s=OrderService.seeded();assertEquals(1,s.getOrdersByCustomer("CUST-42").size());assertEquals("ORD-100",s.getOrdersByCustomer("CUST-100").get(0).id());assertTrue(s.getOrdersByCustomer("none").isEmpty());}
 @Test void validation(){var s=new OrderService();assertThrows(IllegalArgumentException.class,()->s.createOrder(null));for(var r:List.of(new CreateOrderRequest(" ","P",1),new CreateOrderRequest("C","",1),new CreateOrderRequest("C","P",0),new CreateOrderRequest("C","P",null)))assertThrows(IllegalArgumentException.class,()->s.createOrder(r));assertThrows(IllegalArgumentException.class,()->s.getOrdersByCustomer(null));assertThrows(IllegalArgumentException.class,()->s.getOrder(" "));}
 @Test void delete(){var s=OrderService.seeded();s.deleteOrder("ORD-42");assertThrows(OrderNotFoundException.class,()->s.getOrder("ORD-42"));assertThrows(OrderNotFoundException.class,()->s.deleteOrder("ORD-42"));assertThrows(IllegalArgumentException.class,()->s.deleteOrder(null));}
 private APIGatewayProxyRequestEvent event(String method,String resource){return new APIGatewayProxyRequestEvent().withHttpMethod(method).withResource(resource).withPathParameters(Map.of("id","ORD-42"));}
 @Test void proxyLifecycle() throws Exception {
  var h=new ProxyHandler(new OrderService());var post=event("POST","/orders").withBody("{\"customerId\":\"CUST-42\",\"product\":\"MacBook Air\",\"quantity\":1}");
  var created=h.handleRequest(post,null);assertEquals(201,created.getStatusCode());
  String id=new com.fasterxml.jackson.databind.ObjectMapper().readTree(created.getBody()).get("id").asText();assertEquals("/orders/"+id,created.getHeaders().get("Location"));
  var get=event("GET","/orders/{id}").withPathParameters(Map.of("id",id));assertEquals(200,h.handleRequest(get,null).getStatusCode());
  var del=get.withHttpMethod("DELETE");var deleted=h.handleRequest(del,null);assertEquals(204,deleted.getStatusCode());assertEquals("",deleted.getBody());assertEquals(404,h.handleRequest(del,null).getStatusCode());assertEquals(404,h.handleRequest(get.withHttpMethod("GET"),null).getStatusCode());
 }
 @Test void proxyInvalidBodies(){var h=new ProxyHandler();for(String body:List.of("{","{}","null","{\"customerId\":\"C\",\"product\":\"P\",\"quantity\":1.5}","{} {}")){var r=h.handleRequest(event("POST","/orders").withBody(body),null);assertEquals(400,r.getStatusCode(),body);assertTrue(r.getBody().contains("INVALID_INPUT"));}}
 @Test void proxyQueryAndMissing(){var h=new ProxyHandler();assertEquals(400,h.handleRequest(event("GET","/orders"),null).getStatusCode());assertEquals(200,h.handleRequest(event("GET","/orders").withQueryStringParameters(Map.of("customerId","CUST-42")),null).getStatusCode());assertEquals(404,h.handleRequest(event("GET","/orders/{id}").withPathParameters(Map.of("id","missing")),null).getStatusCode());assertEquals(405,h.handleRequest(event("PATCH","/orders"),null).getStatusCode());}
 @Test void unexpectedError(){var h=new ProxyHandler(new OrderService(){@Override public Order getOrder(String id){throw new IllegalStateException("secret");}});var r=h.handleRequest(event("GET","/orders/{id}"),null);assertEquals(500,r.getStatusCode());assertFalse(r.getBody().contains("secret"));}
 @Test void customLifecycle(){var h=new CustomHandler(new OrderService());var i=new CustomHandler.Input();i.operation="CREATE";i.order=new CreateOrderRequest("C","P",1);var r=h.handleRequest(i,null);assertEquals("CREATED",r.outcome());i.id=((Order)r.data()).id();i.operation="GET";assertEquals("OK",h.handleRequest(i,null).outcome());i.operation="SEARCH";i.customerId="C";assertEquals(1,((List<?>)h.handleRequest(i,null).data()).size());i.operation="DELETE";assertEquals("DELETED",h.handleRequest(i,null).outcome());assertEquals("NOT_FOUND",h.handleRequest(i,null).outcome());assertEquals("INVALID",h.handleRequest(null,null).outcome());}
 @Test void directHandler(){var o=new CreateOrderHandler().handleRequest(new CreateOrderRequest("C","P",2),null);assertEquals(2,o.quantity());}
}
