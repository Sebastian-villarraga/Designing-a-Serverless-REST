package edu.eci.orders;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class OrderService {
 private final Map<String,Order> orders=new ConcurrentHashMap<>();
 public static OrderService seeded(){
  OrderService s=new OrderService();
  s.orders.put("ORD-42",new Order("ORD-42","CUST-42","Keyboard",2));
  s.orders.put("ORD-100",new Order("ORD-100","CUST-100","Monitor",1));return s;
 }
 public Order createOrder(CreateOrderRequest r){
  if(r==null)throw new IllegalArgumentException("Request is required");
  String customer=required(r.getCustomerId(),"customerId"), product=required(r.getProduct(),"product");
  if(r.getQuantity()==null || r.getQuantity()<1)throw new IllegalArgumentException("quantity must be a positive integer");
  Order o=new Order("ORD-"+UUID.randomUUID(),customer,product,r.getQuantity());orders.put(o.id(),o);return o;
 }
 public Order getOrder(String id){Order o=orders.get(required(id,"id"));if(o==null)throw new OrderNotFoundException(id);return o;}
 public List<Order> getOrdersByCustomer(String customerId){String c=required(customerId,"customerId");return orders.values().stream().filter(o->o.customerId().equals(c)).sorted(Comparator.comparing(Order::id)).toList();}
 public void deleteOrder(String id){if(orders.remove(required(id,"id"))==null)throw new OrderNotFoundException(id);}
 private static String required(String v,String name){if(v==null || v.isBlank())throw new IllegalArgumentException(name+" is required");return v.trim();}
}