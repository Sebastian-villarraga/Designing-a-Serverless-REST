package edu.eci.orders.lambda;
import edu.eci.orders.*;
import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.events.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.*;
import java.nio.charset.StandardCharsets;
public class ProxyHandler implements RequestHandler<APIGatewayProxyRequestEvent,APIGatewayProxyResponseEvent> {
 private static final ObjectMapper JSON=new ObjectMapper().disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT).enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
 private final OrderService service;
 public record ApiError(String error,String message) {}
 public ProxyHandler(){this(OrderService.seeded());} public ProxyHandler(OrderService service){this.service=service;}
 public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent e,Context context){
  try {
   if(e==null)throw new IllegalArgumentException("Request is required");
   String method=e.getHttpMethod(),resource=e.getResource();
   if("/orders".equals(resource)){
    if("GET".equals(method))return response(200,service.getOrdersByCustomer(e.getQueryStringParameters()==null?null:e.getQueryStringParameters().get("customerId")));
    if("POST".equals(method)){
     String body=e.getBody();if(body==null || body.isBlank())throw new IllegalArgumentException("JSON body is required");
     if(Boolean.TRUE.equals(e.getIsBase64Encoded()))body=new String(Base64.getDecoder().decode(body),StandardCharsets.UTF_8);
     Order o=service.createOrder(JSON.readValue(body,CreateOrderRequest.class));
     APIGatewayProxyResponseEvent result=response(201,o);result.getHeaders().put("Location","/orders/"+o.id());return result;
    }
    return methodNotAllowed("GET, POST");
   }
   if("/orders/{id}".equals(resource)){
    String id=e.getPathParameters()==null?null:e.getPathParameters().get("id");
    if("GET".equals(method))return response(200,service.getOrder(id));
    if("DELETE".equals(method)){service.deleteOrder(id);return response(204,null);}
    return methodNotAllowed("GET, DELETE");
   }
   return response(404,new ApiError("ROUTE_NOT_FOUND","Route does not exist"));
  }catch(OrderNotFoundException ex){return response(404,new ApiError("ORDER_NOT_FOUND",ex.getMessage()));}
  catch(JsonProcessingException | IllegalArgumentException ex){return response(400,new ApiError("INVALID_INPUT","Invalid JSON or input: "+(ex instanceof JsonProcessingException?"Check body fields and types":ex.getMessage())));}
  catch(Exception ex){if(context!=null)context.getLogger().log(ex.toString());return response(500,new ApiError("INTERNAL_ERROR","Unexpected internal error"));}
 }
 private static APIGatewayProxyResponseEvent methodNotAllowed(String allow){var r=response(405,new ApiError("METHOD_NOT_ALLOWED","Method is not supported"));r.getHeaders().put("Allow",allow);return r;}
 private static APIGatewayProxyResponseEvent response(int status,Object body){
  try{return new APIGatewayProxyResponseEvent().withStatusCode(status).withHeaders(new HashMap<>(Map.of("Content-Type","application/json"))).withIsBase64Encoded(false).withBody(status==204?"":JSON.writeValueAsString(body));}
  catch(JsonProcessingException ex){throw new IllegalStateException("Cannot serialize response",ex);}
 }
}