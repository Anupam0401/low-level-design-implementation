package implement.lld.state;

import implement.lld.VendingMachine;
import implement.lld.exception.InvalidInputException;
import implement.lld.money.Denomination;
import implement.lld.product.Product;

public class DispenseVendingMoneyState implements IVendingMoneyState {
    private final VendingMachine vendingMachine;

    public DispenseVendingMoneyState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void selectProduct(Product product) {
        throw new InvalidInputException("Dispensing product. Please wait");
    }

    @Override
    public void insertMoney(int quantity, Denomination denomination) {
        throw new InvalidInputException("Dispensing product. Please wait");
    }

    @Override
    public void dispenseProduct() {
        vendingMachine.getInventoryManager().decreaseProductQuantity(vendingMachine.getSelectedProduct());
        vendingMachine.setVendingMoneyState(getNextState());
        System.out.println("Product " + vendingMachine.getSelectedProduct() + " dispensed!");
    }

    @Override
    public void returnChange() {
        System.out.println("Please collect the product. Returning change in progress.");
    }

    @Override
    public void cancelTransaction() {
        throw new InvalidInputException("Payment is complete. Cannot cancel transaction");
    }

    @Override
    public IVendingMoneyState getNextState() {
        return vendingMachine.getReturnMoneyVendingMoneyState();
    }
}
