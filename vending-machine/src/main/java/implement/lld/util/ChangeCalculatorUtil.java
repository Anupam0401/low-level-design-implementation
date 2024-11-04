package implement.lld.util;

import implement.lld.money.Money;

import java.util.List;

public class ChangeCalculatorUtil {

    public static List<Money> calculateChange(double totalAmount, double productPrice) {
        double change = totalAmount - productPrice;
        long changeInCents = Math.round(change * 100);
        // TODO: to implement
        return List.of();
    }
}
