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
        vendingMachine.selectProduct(new Product("Lays", 1));
        vendingMachine.insertMoney(1, Denomination.ONE);
        vendingMachine.dispenseProduct();
        vendingMachine.returnChange();

        vendingMachine.getInventoryManager().showAllAvailableProducts();

//        List<Money> moneyList = MoneyFactory.calculateChange(10, 1.25);
//        moneyList.forEach(System.out::println);
    }
}