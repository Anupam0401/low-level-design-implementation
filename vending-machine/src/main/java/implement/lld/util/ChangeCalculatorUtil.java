package implement.lld.util;

import implement.lld.money.Denomination;
import implement.lld.money.Money;
import implement.lld.money.MoneyFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChangeCalculatorUtil {

    public static List<Money> calculateChange(double totalAmount, double productPrice) {
        double change = totalAmount - productPrice;
        int changeInCents = (int) Math.round(change * 100);

        int[] allValuesInCents = Denomination.getAllValuesInCents();
        List<Integer>[] dp = new ArrayList[changeInCents + 1];
        dp[0] = new ArrayList<>(Collections.nCopies(allValuesInCents.length, 0));

        for (int i = 0; i < allValuesInCents.length; i++) {
            int denominationInCent = allValuesInCents[i];
            for (int amount = denominationInCent; amount <= changeInCents; amount++) {
                if (dp[amount - denominationInCent] != null) {
                    List<Integer> newCombination = new ArrayList<>(dp[amount - denominationInCent]);
                    newCombination.set(i, newCombination.get(i) + 1);
                    if (dp[amount] == null || totalCoins(newCombination) < totalCoins(dp[amount])) {
                        dp[amount] = newCombination;
                    }
                }
            }
        }

        if (dp[changeInCents] == null) {
            throw new IllegalArgumentException("Cannot dispense exact change with available allValuesInCents.");
        }

        return convertToMoneyList(dp[changeInCents], allValuesInCents);
    }

    // Helper to calculate total coins in a combination
    private static int totalCoins(List<Integer> combination) {
        return combination.stream().mapToInt(Integer::intValue).sum();
    }

    private static List<Money> convertToMoneyList(List<Integer> counts, int[] denominationsInCent) {
        List<Money> moneyList = new ArrayList<>();
        for (int i = 0; i < counts.size(); i++) {
            if (counts.get(i) > 0) {
                moneyList.add(MoneyFactory.createMoney(counts.get(i), Denomination.fromValue(denominationsInCent[i] / 100.0)));
            }
        }
        return moneyList;
    }

}
