package implement.lld.controller;

import implement.lld.dto.request.ExpenseRequestDto;
import implement.lld.dto.request.GroupRequestDto;
import implement.lld.dto.request.UserRequestDto;
import implement.lld.dto.response.*;
import implement.lld.helper.DebtSimplificationHelper;
import implement.lld.mapper.*;
import implement.lld.model.Group;
import implement.lld.model.Transaction;
import implement.lld.model.User;
import implement.lld.model.expense.Expense;
import implement.lld.service.SplitWiseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SplitWiseController {

    private final SplitWiseService splitWiseService;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final ExpenseMapper expenseMapper;
    private final TransactionMapper transactionMapper;
    private final BalanceMapper balanceMapper;
    private final DebtSimplificationHelper debtSimplificationHelper;

    // User Management Endpoints

    @PostMapping("/users")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        log.info("Creating user: {}", userRequestDto);
        User user = splitWiseService.createUser(userRequestDto.getName(), userRequestDto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(user));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable UUID userId) {
        log.info("Getting user: {}", userId);
        User user = splitWiseService.getUser(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.info("Getting all users");
        List<User> users = splitWiseService.getAllUsers();
        return ResponseEntity.ok(userMapper.toDtoList(users));
    }

    // Group Management Endpoints

    @PostMapping("/groups")
    public ResponseEntity<GroupResponseDto> createGroup(@Valid @RequestBody GroupRequestDto groupRequestDto) {
        log.info("Creating group: {}", groupRequestDto);
        Group group = splitWiseService.createGroup(
                groupRequestDto.getName(),
                groupRequestDto.getDescription(),
                groupRequestDto.getOwnerId(),
                groupRequestDto.getMemberIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(groupMapper.toDto(group));
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<GroupResponseDto> getGroup(@PathVariable UUID groupId) {
        log.info("Getting group: {}", groupId);
        Group group = splitWiseService.getGroup(groupId);
        if (group == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groupMapper.toDto(group));
    }

    @PostMapping("/groups/{groupId}/members/{userId}")
    public ResponseEntity<Void> addMemberToGroup(@PathVariable UUID groupId, @PathVariable UUID userId) {
        log.info("Adding member {} to group {}", userId, groupId);
        boolean success = splitWiseService.addMemberToGroup(groupId, userId);
        if (!success) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    // Expense Management Endpoints

    @PostMapping("/expenses/personal")
    public ResponseEntity<Void> addPersonalExpense(
            @RequestParam UUID payerId,
            @RequestParam UUID receiverId,
            @RequestParam double amount,
            @RequestParam String description) {
        log.info("Adding personal expense: {} -> {}, amount: {}", payerId, receiverId, amount);
        splitWiseService.addPersonalExpense(payerId, receiverId, amount, description);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/expenses/non-group")
    public ResponseEntity<Void> addNonGroupExpense(@Valid @RequestBody ExpenseRequestDto expenseRequestDto) {
        log.info("Adding non-group expense: {}", expenseRequestDto);
        splitWiseService.addNonGroupExpense(
                expenseRequestDto.getPayerId(),
                expenseRequestDto.getAmount(),
                expenseRequestDto.getDescription(),
                expenseMapper.toSplitEntities(expenseRequestDto.getSplits(), expenseRequestDto.getSplitType()),
                expenseRequestDto.getSplitType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/expenses/group")
    public ResponseEntity<Void> addGroupExpense(@Valid @RequestBody ExpenseRequestDto expenseRequestDto) {
        log.info("Adding group expense: {}", expenseRequestDto);
        if (expenseRequestDto.getGroupId() == null) {
            return ResponseEntity.badRequest().build();
        }
        splitWiseService.addGroupExpense(
                expenseRequestDto.getGroupId(),
                expenseRequestDto.getPayerId(),
                expenseRequestDto.getAmount(),
                expenseRequestDto.getDescription(),
                expenseMapper.toSplitEntities(expenseRequestDto.getSplits(), expenseRequestDto.getSplitType()),
                expenseRequestDto.getSplitType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/expenses/users/{userId}")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByUserId(@PathVariable UUID userId) {
        log.info("Getting expenses for user: {}", userId);
        List<Expense> expenses = splitWiseService.getExpensesByUserId(userId);
        return ResponseEntity.ok(expenseMapper.toDtoList(expenses));
    }

    @GetMapping("/expenses/groups/{groupId}")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByGroupId(@PathVariable UUID groupId) {
        log.info("Getting expenses for group: {}", groupId);
        List<Expense> expenses = splitWiseService.getExpensesByGroupId(groupId);
        return ResponseEntity.ok(expenseMapper.toDtoList(expenses));
    }

    // Balance Management Endpoints

    @GetMapping("/balances/users/{userId1}/users/{userId2}")
    public ResponseEntity<Double> getBalance(@PathVariable UUID userId1, @PathVariable UUID userId2) {
        log.info("Getting balance between users: {} and {}", userId1, userId2);
        double balance = splitWiseService.getBalance(userId1, userId2);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/balances/users/{userId}")
    public ResponseEntity<BalanceResponseDto> getUserBalances(@PathVariable UUID userId) {
        log.info("Getting balances for user: {}", userId);
        Map<UUID, Double> balances = splitWiseService.getUserBalances(userId);
        return ResponseEntity.ok(balanceMapper.toDto(userId, balances));
    }

    @GetMapping("/balances/groups/{groupId}")
    public ResponseEntity<Map<UUID, Double>> getGroupNetBalances(@PathVariable UUID groupId) {
        log.info("Getting net balances for group: {}", groupId);
        Map<UUID, Double> balances = splitWiseService.getGroupNetBalances(groupId);
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/balances/groups/{groupId}/simplify")
    public ResponseEntity<List<DebtSimplificationHelper.SimplifiedTransaction>> getSimplifiedGroupDebts(@PathVariable UUID groupId) {
        log.info("Getting simplified debts for group: {}", groupId);
        Map<UUID, Double> balances = splitWiseService.getGroupNetBalances(groupId);
        List<DebtSimplificationHelper.SimplifiedTransaction> simplifiedTransactions = debtSimplificationHelper.simplifyDebts(balances);
        return ResponseEntity.ok(simplifiedTransactions);
    }

    @GetMapping("/balances/users/{userId}/simplify")
    public ResponseEntity<List<DebtSimplificationHelper.SimplifiedTransaction>> getSimplifiedUserDebts(@PathVariable UUID userId) {
        log.info("Getting simplified debts for user: {}", userId);
        Map<UUID, Double> balances = splitWiseService.getUserBalances(userId);
        List<DebtSimplificationHelper.SimplifiedTransaction> simplifiedTransactions = debtSimplificationHelper.simplifyDebts(balances);
        return ResponseEntity.ok(simplifiedTransactions);
    }

    // Transaction Management Endpoints

    @PostMapping("/transactions")
    public ResponseEntity<Boolean> performTransaction(
            @RequestParam UUID senderId,
            @RequestParam UUID receiverId,
            @RequestParam double amount) {
        log.info("Performing transaction: {} -> {}, amount: {}", senderId, receiverId, amount);
        boolean success = splitWiseService.performTransaction(senderId, receiverId, amount);
        return ResponseEntity.ok(success);
    }

    @GetMapping("/transactions/users/{userId}")
    public ResponseEntity<List<TransactionResponseDto>> getUserTransactions(@PathVariable UUID userId) {
        log.info("Getting transactions for user: {}", userId);
        List<Transaction> transactions = splitWiseService.getUserTransactions(userId);
        return ResponseEntity.ok(transactionMapper.toDtoList(transactions));
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionResponseDto> getTransaction(@PathVariable UUID transactionId) {
        log.info("Getting transaction: {}", transactionId);
        Transaction transaction = splitWiseService.getTransaction(transactionId);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transactionMapper.toDto(transaction));
    }
}
