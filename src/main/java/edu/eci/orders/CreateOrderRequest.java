package edu.eci.orders;
public class CreateOrderRequest {
 private String customerId; private String product; private Integer quantity;
 public CreateOrderRequest() {}
 public CreateOrderRequest(String customerId,String product,Integer quantity) { this.customerId=customerId;this.product=product;this.quantity=quantity; }
 public String getCustomerId(){return customerId;} public void setCustomerId(String v){customerId=v;}
 public String getProduct(){return product;} public void setProduct(String v){product=v;}
 public Integer getQuantity(){return quantity;} public void setQuantity(Integer v){quantity=v;}
}