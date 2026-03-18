package id.petrikshop.web.service;

import id.petrikshop.web.model.Order;

import java.util.List;


public interface OrderService {
    public Order createOrder(Order order);
    public Order updateStatus(String orderId, String status);
    public Order findById(String orderId);
    public List<Order> findAllByAuthor(String author);
}
