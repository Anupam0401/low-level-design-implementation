package implement.lld.product;

import implement.lld.exception.InvalidProductException;

import java.util.HashMap;

public class InventoryManager {
    private final Inventory inventory;

    public InventoryManager(Inventory inventory) {
        this.inventory = inventory;
    }

    public void addProduct(Product product, int quantity) {
        inventory.addProduct(product, quantity);
    }

    public void removeProduct(Product product) {
        inventory.removeProduct(product);
    }

    public void restockProduct(Product product, int quantity) {
        inventory.restockProduct(product, quantity);
    }

    public void decreaseProductQuantity(Product product) {
        inventory.decreaseProductQuantity(product);
    }

    public void changeProductPrice(Product product, double price) {
        inventory.changeProductPrice(product, price);
    }

    public int getProductQuantity(String productName) {
        HashMap<Product, Integer> products = inventory.getAllAvailableProducts();
        for (Product product : products.keySet()) {
            if (product.getName().equals(productName)) {
                return products.get(product);
            }
        }
        throw new InvalidProductException("Product " + productName + " is not available");
    }

    public void showAllAvailableProducts() {
        HashMap<Product, Integer> products = inventory.getAllAvailableProducts();
        for (Product product : products.keySet()) {
            System.out.println(product.getName() + " : " + products.get(product));
        }
    }
}
