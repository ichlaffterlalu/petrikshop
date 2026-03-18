package id.petrikshop.web.service;

import id.petrikshop.web.model.Product;
import java.util.List;

public interface ProductService {
    public Product create(Product product);
    public List<Product> findAll();
}
