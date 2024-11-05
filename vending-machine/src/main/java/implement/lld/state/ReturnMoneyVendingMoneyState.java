package implement.lld.state;

import implement.lld.VendingMachine;
import implement.lld.exception.InvalidInputException;
import implement.lld.money.Denomination;
import implement.lld.money.Money;
import implement.lld.money.MoneyFactory;

import java.util.List;

public class ReturnMoneyVendingMoneyState implements IVendingMoneyState {
    private final VendingMachine vendingMachine;

    public ReturnMoneyVendingMoneyState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void selectProduct(String productName) {
        throw new InvalidInputException("Please wait... Returning change (if any)");
    }

    @Override
    public void insertMoney(int quantity, Denomination denomination) {
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
        if (!calculatedChange.isEmpty()) {
            System.out.println("Returning change: ");
            printChangeToReturn(calculatedChange);
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
            System.out.printf("%s %s, Number: %d\n",
                money.getDenomination(), money.getMoneyType(), money.getQuantity());
        });
        System.out.println();
    }
}
