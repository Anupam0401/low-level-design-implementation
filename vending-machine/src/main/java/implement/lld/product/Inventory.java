package implement.lld.product;

import implement.lld.exception.InvalidProductException;
import implement.lld.exception.OutOfStockException;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Inventory {
    private final ConcurrentHashMap<Product, Integer> products = new ConcurrentHashMap<>();

    protected void addProduct(Product product, int quantity) {
        try {
            products.merge(product, quantity, Integer::sum);
            System.out.println(quantity + " units of product " + product.getName() + " added successfully!");
        } catch (Exception e) {
            System.out.println("Error while adding product: " + e.getMessage());
        }
    }

    protected void removeProduct(Product product) {
        try {
            products.remove(product);
            System.out.println("Product " + product.getName() + " removed successfully!");
        } catch (Exception e) {
            throw new InvalidProductException("Product " + product.getName() + " is not available");
        }
    }

    protected void restockProduct(Product product, int quantity) {
        if (products.get(product) == null) {
            throw new InvalidProductException("Product " + product.getName() + " is not available");
        }
        products.computeIfPresent(product, (oldProduct, oldQuantity) -> oldQuantity + quantity);
        System.out.println(quantity + " units of product " + product.getName() + " restocked successfully!");
    }

    protected void decreaseProductQuantity(Product product) {
        products.computeIfPresent(product, (oldProduct, oldQuantity) -> {
            if (oldQuantity <= 1) {
                throw new OutOfStockException("Product " + product.getName() + " is out of stock");
            }
            return oldQuantity - 1;
        });
        System.out.println("Quantity of product " + product.getName() + " decreased successfully!");
    }


    protected void changeProductPrice(Product product, double price) {
        if (products.get(product) == null) {
            throw new InvalidProductException("Product " + product.getName() + " is not available");
        }
        product.setPrice(price);
        System.out.println("Price of product " + product.getName() + " changed successfully!");
    }

    protected HashMap<Product, Integer> getAllAvailableProducts() {
        return new HashMap<>(products);
    }

    protected boolean isProductAvailableByName(String productName) {
        for (Product product : products.keySet()) {
            if (product.getName().equals(productName)) {
                return true;
            }
        }
        return false;
    }

    protected Product findProductByName(String productName) {
        for (Product product : products.keySet()) {
            if (product.getName().equals(productName)) {
                return product;
            }
        }
        throw new InvalidProductException("Product " + productName + " is not available");
    }
}
