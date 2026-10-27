package edu.eci.orders.lambda;
import edu.eci.orders.*;
import com.amazonaws.services.lambda.runtime.*;
public class CustomHandler implements RequestHandler<CustomHandler.Input,CustomHandler.Result> {
 public static class Input {
  public String operation; public String id; public String customerId; public CreateOrderRequest order;
 }
 public record Result(String outcome,Object data) {}
 private final OrderService service;
 public CustomHandler(){this(OrderService.seeded());} public CustomHandler(OrderService service){this.service=service;}
 public Result handleRequest(Input input,Context context){
  try{
   if(input==null || input.operation==null)throw new IllegalArgumentException("operation is required");
   return switch(input.operation){
    case "CREATE" -> new Result("CREATED",service.createOrder(input.order));
    case "GET" -> new Result("OK",service.getOrder(input.id));
    case "SEARCH" -> new Result("OK",service.getOrdersByCustomer(input.customerId));
    case "DELETE" -> {service.deleteOrder(input.id);yield new Result("DELETED",null);}
    default -> throw new IllegalArgumentException("Unknown operation");
   };
  }catch(OrderNotFoundException e){return new Result("NOT_FOUND",new ProxyHandler.ApiError("ORDER_NOT_FOUND",e.getMessage()));}
  catch(IllegalArgumentException e){return new Result("INVALID",new ProxyHandler.ApiError("INVALID_INPUT",e.getMessage()));}
  catch(Exception e){if(context!=null)context.getLogger().log(e.toString());return new Result("ERROR",new ProxyHandler.ApiError("INTERNAL_ERROR","Unexpected internal error"));}
 }
}