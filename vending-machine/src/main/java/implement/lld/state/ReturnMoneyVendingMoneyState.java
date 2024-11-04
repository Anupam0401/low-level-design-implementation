package implement.lld.state;

import implement.lld.VendingMachine;
import implement.lld.exception.InvalidInputException;
import implement.lld.money.Money;
import implement.lld.money.MoneyFactory;
import implement.lld.product.Product;

import java.util.List;

public class ReturnMoneyVendingMoneyState implements IVendingMoneyState {
    private final VendingMachine vendingMachine;

    public ReturnMoneyVendingMoneyState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void selectProduct(Product product) {
        throw new InvalidInputException("Please wait... Returning change (if any)");
    }

    @Override
    public void insertMoney(int quantity, String denomination) {
        throw new InvalidInputException("Please wait... Returning change (if any)");
    }

    @Override
    public void dispenseProduct() {
        throw new InvalidInputException("Product already dispensed. Returning change (if any)");
    }

    @Override
    public void returnChange() {
        List<Money> calculatedChange = MoneyFactory.calculateChange(
            vendingMachine.getCurrentPayment(),
            vendingMachine.getSelectedProduct().getPrice()
        );
        if (calculatedChange != null && !calculatedChange.isEmpty()) {
            System.out.println("Returning change: ");
            printChangeToReturn(calculatedChange);
            calculatedChange.forEach(System.out::println);
        } else {
            System.out.println("No change to return!");
        }
        vendingMachine.resetPayment();
        vendingMachine.resetSelectedProduct();
        vendingMachine.setVendingMoneyState(getNextState());
    }

    @Override
    public void cancelTransaction() {
        throw new InvalidInputException("Transaction completed, cannot be cancelled now.");
    }

    @Override
    public IVendingMoneyState getNextState() {
        return vendingMachine.getIdleVendingMoneyState();
    }

    private void printChangeToReturn(List<Money> calculatedChange) {
        calculatedChange.forEach(money -> {
            System.out.printf("%s: Denomination: %s, Number: %d\n",
                money.getMoneyType(), money.getDenomination(), money.getQuantity());
        });
    }
}
