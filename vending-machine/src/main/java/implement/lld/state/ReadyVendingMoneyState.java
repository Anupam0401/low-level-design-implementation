package implement.lld.state;

import implement.lld.VendingMachine;
import implement.lld.exception.InvalidInputException;
import implement.lld.money.Denomination;
import implement.lld.money.Money;
import implement.lld.money.MoneyFactory;
import implement.lld.product.Product;

import java.util.List;


public class ReadyVendingMoneyState implements IVendingMoneyState {
    private final VendingMachine vendingMachine;

    public ReadyVendingMoneyState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void selectProduct(Product product) {
        vendingMachine.setVendingMoneyState(vendingMachine.getIdleVendingMoneyState());
    }

    @Override
    public void insertMoney(int quantity, Denomination denomination) {
        Money money = MoneyFactory.createMoney(quantity, denomination);
        vendingMachine.setCurrentPayment(vendingMachine.getCurrentPayment() + money.getMoneyValue());
        vendingMachine.setVendingMoneyState(getNextState());
    }

    @Override
    public void dispenseProduct() {
        throw new InvalidInputException("Please make payment for the selected product first");
    }

    @Override
    public void returnChange() {
       throw new InvalidInputException("Please make payment for the selected product first");
    }

    @Override
    public void cancelTransaction() {
        vendingMachine.resetPayment();
        vendingMachine.resetSelectedProduct();
        vendingMachine.setVendingMoneyState(vendingMachine.getIdleVendingMoneyState());
        System.out.println("Transaction cancelled, please select a product.");
    }

    @Override
    public IVendingMoneyState getNextState() {
        return vendingMachine.getDispenseVendingMoneyState();
    }
}
