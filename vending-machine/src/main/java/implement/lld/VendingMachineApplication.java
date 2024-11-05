package implement.lld;

import implement.lld.money.Denomination;
import implement.lld.product.Product;

public class VendingMachineApplication {
    public static void main(String[] args) {
        try {
            VendingMachine vendingMachine = VendingMachine.getInstance();
            // stock up inventory
            Product coke = new Product("Coke", 1.25);
            Product pepsi = new Product("Pepsi", 1.25);
            Product lays = new Product("Lays", 1);
            Product doritos = new Product("Doritos", 1);
            Product snickers = new Product("Snickers", 2);
            Product dairyMilk = new Product("DairyMilk", 100);
            vendingMachine.getInventoryManager().addProduct(coke, 10);
            vendingMachine.getInventoryManager().addProduct(pepsi, 10);
            vendingMachine.getInventoryManager().addProduct(lays, 10);
            vendingMachine.getInventoryManager().addProduct(doritos, 10);
            vendingMachine.getInventoryManager().addProduct(snickers, 10);
            vendingMachine.getInventoryManager().addProduct(dairyMilk, 5);

            // start vending machine
            vendingMachine.selectProduct("Pepsi");
            vendingMachine.insertMoney(2, Denomination.TEN);
            vendingMachine.dispenseProduct();
            vendingMachine.returnChange();
            vendingMachine.getInventoryManager().showAllAvailableProducts();

            vendingMachine.getInventoryManager().restockProduct(coke, 10);
            vendingMachine.getInventoryManager().removeProduct(pepsi);

            vendingMachine.getInventoryManager().showAllAvailableProducts();

            vendingMachine.selectProduct("Pepsi");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}