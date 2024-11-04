package implement.lld;

import implement.lld.money.Denomination;
import implement.lld.product.Product;

public class VendingMachineApplication {
    public static void main(String[] args) {
        VendingMachine vendingMachine = VendingMachine.getInstance();
        // stock up inventory
        vendingMachine.getInventoryManager().addProduct(new Product("Coke", 1.25), 10);
        vendingMachine.getInventoryManager().addProduct(new Product("Pepsi", 1.25), 10);
        vendingMachine.getInventoryManager().addProduct(new Product("Lays", 1), 10);
        vendingMachine.getInventoryManager().addProduct(new Product("Doritos", 1), 10);
        vendingMachine.getInventoryManager().addProduct(new Product("Snickers", 2), 10);
        vendingMachine.getInventoryManager().addProduct(new Product("DairyMilk", 100), 5);

        // start vending machine
        vendingMachine.selectProduct(new Product("Coke", 1.25));
        vendingMachine.insertMoney(2, Denomination.ONE.name());
        vendingMachine.dispenseProduct();
        vendingMachine.returnChange();
    }
}