package implement.lld.helper;

import implement.lld.service.BalanceService;
import implement.lld.service.expense.GroupExpenseService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

@Log4j2
public class DebtSimplificationHelper {
    private final BalanceService balanceService;
    private final GroupExpenseService groupExpenseService;

    public DebtSimplificationHelper(BalanceService balanceService, GroupExpenseService groupExpenseService) {
        this.balanceService = balanceService;
        this.groupExpenseService = groupExpenseService;
    }

    /**
     * Simplifies debts within a group by minimizing transactions.
     * Returns a list of transactions that should be performed.
     */
    public void simplifyDebts(UUID groupId) {
        Map<UUID, Double> netBalance = groupExpenseService.getGroupNetBalance(groupId);

        PriorityQueue<UserBalance> debtors = new PriorityQueue<>(Comparator.comparingDouble(UserBalance::getAmount));
        PriorityQueue<UserBalance> creditors = new PriorityQueue<>((a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        for (Map.Entry<UUID, Double> entry : netBalance.entrySet()) {
            UUID userId = entry.getKey();
            double amount = entry.getValue();
            if (amount < 0) {
                debtors.add(new UserBalance(userId, amount));
            } else if (amount > 0) {
                creditors.add(new UserBalance(userId, amount));
            }
        }

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            UserBalance debtor = debtors.poll();
            UserBalance creditor = creditors.poll();
            double settleAmount = Math.min(-debtor.getAmount(), creditor.getAmount());

            // Update the balances
            balanceService.updateBalance(debtor.getUserId(), creditor.getUserId(), settleAmount);
            balanceService.updateBalance(creditor.getUserId(), debtor.getUserId(), -settleAmount);

            // Adjust remaining balances
            debtor.setAmount(debtor.getAmount() + settleAmount);
            creditor.setAmount(creditor.getAmount() - settleAmount);

            if (debtor.getAmount() < 0) debtors.add(debtor);
            if (creditor.getAmount() > 0) creditors.add(creditor);
        }
        log.info("Debts simplified for group {}!", groupId);
    }

    @Getter
    private static class UserBalance {
        UUID userId;
        @Setter
        double amount;

        public UserBalance(UUID userId, double amount) {
            this.userId = userId;
            this.amount = amount;
        }
    }


}
