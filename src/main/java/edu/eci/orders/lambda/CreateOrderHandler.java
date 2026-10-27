package edu.eci.orders.lambda;
import edu.eci.orders.*;
import com.amazonaws.services.lambda.runtime.*;
public class CreateOrderHandler implements RequestHandler<CreateOrderRequest,Order> {
 private final OrderService service=OrderService.seeded();
 public Order handleRequest(CreateOrderRequest input,Context context){return service.createOrder(input);}
}