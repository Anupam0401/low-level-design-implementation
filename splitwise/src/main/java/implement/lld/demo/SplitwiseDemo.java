package implement.lld.demo;

import implement.lld.model.Transaction;
import implement.lld.model.User;
import implement.lld.model.split.EqualSplit;
import implement.lld.model.split.Split;
import implement.lld.model.split.SplitType;
import implement.lld.service.SplitWiseService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Demonstration class that shows how to use the Splitwise application.
 * This class implements CommandLineRunner to run the demo when the application starts.
 */
@Component
@Log4j2
public class SplitwiseDemo implements CommandLineRunner {

    private final SplitWiseService splitWiseService;

    @Autowired
    public SplitwiseDemo(SplitWiseService splitWiseService) {
        this.splitWiseService = splitWiseService;
    }

    @Override
    public void run(String... args) {
        log.info("Starting Splitwise Demo");

        // Create users
        User alice = splitWiseService.createUser("Alice", "alice@example.com");
        User bob = splitWiseService.createUser("Bob", "bob@example.com");
        User charlie = splitWiseService.createUser("Charlie", "charlie@example.com");
        User dave = splitWiseService.createUser("Dave", "dave@example.com");

        log.info("Created users: {}, {}, {}, {}", alice.getId(), bob.getId(), charlie.getId(), dave.getId());

        // Create a group
        List<UUID> memberIds = new ArrayList<>();
        memberIds.add(bob.getId());
        memberIds.add(charlie.getId());
        memberIds.add(dave.getId());

        splitWiseService.createGroup("Trip to Mountains", "Weekend trip expenses", alice.getId(), memberIds);
        log.info("Created group with Alice as owner and Bob, Charlie, and Dave as members");

        // Add a personal expense
        splitWiseService.addPersonalExpense(alice.getId(), bob.getId(), 500.0, "Lunch");
        log.info("Added personal expense: Alice paid for Bob's lunch (₹500)");

        // Add a non-group expense with equal splits
        List<Split> splits = new ArrayList<>();
        splits.add(new EqualSplit(alice.getId()));
        splits.add(new EqualSplit(bob.getId()));
        splits.add(new EqualSplit(charlie.getId()));

        splitWiseService.addNonGroupExpense(alice.getId(), 3000.0, "Dinner", splits, SplitType.EQUAL);
        log.info("Added non-group expense: Alice paid for dinner (₹3000) split equally with Bob and Charlie");

        // Check balances
        Map<UUID, Double> aliceBalances = splitWiseService.getUserBalances(alice.getId());
        log.info("Alice's balances:");
        aliceBalances.forEach((userId, amount) -> {
            User user = splitWiseService.getUser(userId);
            log.info("{} owes Alice: ₹{}", user.getName(), amount);
        });

        // Settle up
        splitWiseService.performTransaction(bob.getId(), alice.getId(), 1500.0);
        log.info("Bob settled ₹1500 with Alice");

        // Check updated balances
        aliceBalances = splitWiseService.getUserBalances(alice.getId());
        log.info("Alice's updated balances:");
        aliceBalances.forEach((userId, amount) -> {
            User user = splitWiseService.getUser(userId);
            log.info("{} owes Alice: ₹{}", user.getName(), amount);
        });

        // View transactions
        List<Transaction> aliceTransactions = splitWiseService.getUserTransactions(alice.getId());
        log.info("Alice's transactions:");
        aliceTransactions.forEach(transaction -> {
            User sender = splitWiseService.getUser(transaction.getSenderId());
            User receiver = splitWiseService.getUser(transaction.getReceiverId());
            log.info("{} -> {}: ₹{}", sender.getName(), receiver.getName(), transaction.getAmount());
        });

        log.info("Splitwise Demo completed");
    }
}
