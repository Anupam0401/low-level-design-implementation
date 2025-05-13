package implement.lld.helper;

import implement.lld.service.BalanceService;
import implement.lld.service.expense.GroupExpenseService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.UUID;

@Log4j2
@Component
public class DebtSimplificationHelper {
    private final BalanceService balanceService;
    private final GroupExpenseService groupExpenseService;

    public DebtSimplificationHelper(BalanceService balanceService, GroupExpenseService groupExpenseService) {
        this.balanceService = balanceService;
        this.groupExpenseService = groupExpenseService;
    }

    /**
     * Represents a simplified transaction between two users
     */
    @Data
    @AllArgsConstructor
    public static class SimplifiedTransaction {
        private UUID fromUserId;
        private UUID toUserId;
        private double amount;
    }

    /**
     * Simplifies debts within a group by minimizing transactions.
     * Updates the balances in the database.
     * @param groupId the group ID
     */
    public void simplifyDebts(UUID groupId) {
        Map<UUID, Double> netBalance = groupExpenseService.getGroupNetBalance(groupId);

        PriorityQueue<UserBalance> debtors = new PriorityQueue<>(Comparator.comparingDouble(UserBalance::getAmount));
        PriorityQueue<UserBalance> creditors = new PriorityQueue<>(
            (a, b) -> Double.compare(b.getAmount(), a.getAmount())
        );

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
            double settleAmount = Objects.nonNull(creditor)
                ? Math.min(-debtor.getAmount(), creditor.getAmount())
                : -debtor.getAmount();

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

    /**
     * Simplify debts between users without updating the database.
     * Returns a list of simplified transactions.
     * @param balances map of user IDs to their net balances (positive means they are owed money, negative means they owe money)
     * @return list of simplified transactions
     */
    public List<SimplifiedTransaction> simplifyDebts(Map<UUID, Double> balances) {
        List<SimplifiedTransaction> transactions = new ArrayList<>();
        
        // Separate users into creditors (positive balance) and debtors (negative balance)
        PriorityQueue<Map.Entry<UUID, Double>> creditors = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue));
        PriorityQueue<Map.Entry<UUID, Double>> debtors = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue));
        
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            if (Math.abs(entry.getValue()) < 0.01) {
                // Skip users with zero balance
                continue;
            }
            
            if (entry.getValue() > 0) {
                // User is owed money
                creditors.add(entry);
            } else {
                // User owes money
                debtors.add(entry);
            }
        }
        
        // Process all debts
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Map.Entry<UUID, Double> maxCreditor = creditors.poll();
            Map.Entry<UUID, Double> maxDebtor = debtors.poll();
            
            double creditorAmount = maxCreditor.getValue();
            double debtorAmount = -maxDebtor.getValue(); // Convert to positive
            
            double transactionAmount = Math.min(creditorAmount, debtorAmount);
            
            // Create a transaction from debtor to creditor
            transactions.add(new SimplifiedTransaction(
                    maxDebtor.getKey(),
                    maxCreditor.getKey(),
                    transactionAmount
            ));
            
            // Update remaining balances
            double creditorRemaining = creditorAmount - transactionAmount;
            double debtorRemaining = debtorAmount - transactionAmount;
            
            // If creditor still has remaining balance, add back to the queue
            if (creditorRemaining > 0.01) {
                maxCreditor.setValue(creditorRemaining);
                creditors.add(maxCreditor);
            }
            
            // If debtor still has remaining debt, add back to the queue
            if (debtorRemaining > 0.01) {
                maxDebtor.setValue(-debtorRemaining); // Convert back to negative
                debtors.add(maxDebtor);
            }
        }
        
        return transactions;
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
