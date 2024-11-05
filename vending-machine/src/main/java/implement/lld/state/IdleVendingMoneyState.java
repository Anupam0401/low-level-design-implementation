package implement.lld.state;

import implement.lld.VendingMachine;
import implement.lld.exception.InvalidInputException;
import implement.lld.money.Denomination;
import implement.lld.product.Product;

public class IdleVendingMoneyState implements IVendingMoneyState {
    private final VendingMachine vendingMachine;

    public IdleVendingMoneyState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void selectProduct(Product product) {
        if (validateAvailability(product)) {
            vendingMachine.setSelectedProduct(product);
            vendingMachine.setCurrentPayment(0);
            vendingMachine.setVendingMoneyState(getNextState());
        } else {
            System.out.println("Selected product is not present, please try again with different product");
            vendingMachine.setVendingMoneyState(vendingMachine.getIdleVendingMoneyState());
        }
    }

    @Override
    public void insertMoney(int quantity, Denomination denomination) {
        System.out.println("Please select a product first");
    }

    @Override
    public void dispenseProduct() {
        throw new InvalidInputException("Please select a product first");
    }

    @Override
    public void returnChange() {
        throw new InvalidInputException("Please select a product first");
    }

    @Override
    public void cancelTransaction() {
        throw new InvalidInputException("No transaction to cancel");
    }

    @Override
    public IVendingMoneyState getNextState() {
        return vendingMachine.getReadyVendingMoneyState();
    }

    private boolean validateAvailability(Product product) {
        return vendingMachine.getInventoryManager().getProductQuantity(product.getName()) > 0;
    }
}
