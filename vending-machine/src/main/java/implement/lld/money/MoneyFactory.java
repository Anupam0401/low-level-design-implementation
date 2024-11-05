package implement.lld.money;

import implement.lld.util.ChangeCalculatorUtil;

import java.util.List;

import static implement.lld.money.MoneyType.CASH;
import static implement.lld.money.MoneyType.COIN;

public class MoneyFactory {
    public static Money createMoney(int quantity, Denomination denomination) {
        return switch (determineType(denomination)) {
            case COIN -> new Coin(quantity, denomination);
            case CASH -> new Cash(quantity, denomination);
        };
    }

    private static MoneyType determineType(Denomination denomination) {
        return switch (denomination) {
            case ONE, TWO, FIVE, TEN, TWENTY, FIFTY, HUNDRED, TWO_HUNDRED, FIVE_HUNDRED, THOUSAND -> CASH;
            case CENT, NICKEL, DIME, QUARTER -> COIN;
        };
    }

    public static List<Money> calculateChange(double totalAmount, double productPrice) {
        return ChangeCalculatorUtil.calculateChange(totalAmount, productPrice);
    }
}
