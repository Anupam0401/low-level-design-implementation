package implement.lld.service;

import implement.lld.model.Group;
import implement.lld.model.Transaction;
import implement.lld.model.User;
import implement.lld.model.expense.Expense;
import implement.lld.model.split.Split;
import implement.lld.model.split.SplitType;
import implement.lld.repository.interfaces.IExpenseRepository;
import implement.lld.repository.interfaces.IGroupRepository;
import implement.lld.repository.interfaces.IUserRepository;
import implement.lld.service.expense.GroupExpenseService;
import implement.lld.service.expense.NonGroupExpenseService;
import implement.lld.service.expense.PersonalExpenseService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Log4j2
public class SplitWiseService {
    private final BalanceService balanceService;
    private final TransactionService transactionService;
    private final IExpenseRepository expenseRepository;
    private final GroupExpenseService groupExpenseService;
    private final NonGroupExpenseService nonGroupExpenseService;
    private final PersonalExpenseService personalExpenseService;
    private final IUserRepository userRepository;
    private final IGroupRepository groupRepository;

    @Autowired
    public SplitWiseService(
        BalanceService balanceService,
        TransactionService transactionService,
        IExpenseRepository expenseRepository,
        GroupExpenseService groupExpenseService,
        NonGroupExpenseService nonGroupExpenseService,
        PersonalExpenseService personalExpenseService,
        IUserRepository userRepository,
        IGroupRepository groupRepository
    ) {
        this.balanceService = balanceService;
        this.transactionService = transactionService;
        this.expenseRepository = expenseRepository;
        this.groupExpenseService = groupExpenseService;
        this.nonGroupExpenseService = nonGroupExpenseService;
        this.personalExpenseService = personalExpenseService;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }
    
    // User Management
    
    /**
     * Create a new user
     * @param name user name
     * @param email user email
     * @return the created user
     */
    public User createUser(String name, String email) {
        User user = new User(UUID.randomUUID(), name, email);
        return userRepository.save(user);
    }
    
    /**
     * Get a user by ID
     * @param userId user ID
     * @return the user if found, null otherwise
     */
    public User getUser(UUID userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * Get all users
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Group Management
    
    /**
     * Create a new group
     * @param name group name
     * @param description group description
     * @param ownerId owner user ID
     * @param memberIds list of member user IDs
     * @return the created group
     */
    public Group createGroup(String name, String description, UUID ownerId, List<UUID> memberIds) {
        User owner = userRepository.findById(ownerId);
        if (owner == null) {
            log.error("Owner not found: {}", ownerId);
            throw new IllegalArgumentException("Owner not found");
        }
        
        Group group = new Group(owner, description, name);
        
        // Add members to the group
        for (UUID memberId : memberIds) {
            User member = userRepository.findById(memberId);
            if (member != null) {
                group.addMember(member);
            } else {
                log.warn("Member not found: {}", memberId);
            }
        }
        
        // Add the owner as a member if not already added
        if (!group.getMembers().contains(owner)) {
            group.addMember(owner);
        }
        
        UUID groupId = UUID.randomUUID();
        return groupRepository.save(group, groupId);
    }
    
    /**
     * Get a group by ID
     * @param groupId group ID
     * @return the group if found, null otherwise
     */
    public Group getGroup(UUID groupId) {
        return groupRepository.findById(groupId);
    }
    
    /**
     * Add a member to a group
     * @param groupId group ID
     * @param userId user ID to add
     * @return true if successful, false otherwise
     */
    public boolean addMemberToGroup(UUID groupId, UUID userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            return false;
        }
        return groupRepository.addMember(groupId, user);
    }
    
    // Expense Management
    
    /**
     * Add a personal expense
     * @param payerId payer user ID
     * @param receiverId receiver user ID
     * @param amount expense amount
     * @param description expense description
     */
    public void addPersonalExpense(UUID payerId, UUID receiverId, double amount, String description) {
        personalExpenseService.addPersonalExpense(payerId, receiverId, amount, description);
        log.info("Added personal expense: {} -> {}, amount: {}", payerId, receiverId, amount);
    }
    
    /**
     * Add a non-group expense
     * @param payerId payer user ID
     * @param amount expense amount
     * @param description expense description
     * @param splits list of splits
     * @param splitType type of split
     */
    public void addNonGroupExpense(UUID payerId, double amount, String description, List<Split> splits, SplitType splitType) {
        nonGroupExpenseService.addNonGroupExpense(payerId, amount, description, splits, splitType);
        log.info("Added non-group expense: payer={}, amount={}", payerId, amount);
    }
    
    /**
     * Add a group expense
     * @param groupId group ID
     * @param payerId payer user ID
     * @param amount expense amount
     * @param description expense description
     * @param splits list of splits
     * @param splitType type of split
     */
    public void addGroupExpense(UUID groupId, UUID payerId, double amount, String description, List<Split> splits, SplitType splitType) {
        groupExpenseService.addGroupExpense(groupId, payerId, amount, description, splits, splitType);
        log.info("Added group expense: group={}, payer={}, amount={}", groupId, payerId, amount);
    }
    
    /**
     * Get expenses by user ID
     * @param userId user ID
     * @return list of expenses
     */
    public List<Expense> getExpensesByUserId(UUID userId) {
        return expenseRepository.findExpensesByUserId(userId);
    }
    
    /**
     * Get expenses by group ID
     * @param groupId group ID
     * @return list of expenses
     */
    public List<Expense> getExpensesByGroupId(UUID groupId) {
        return expenseRepository.findExpensesByGroupId(groupId);
    }
    
    // Balance Management
    
    /**
     * Get balance between two users
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @return balance amount
     */
    public double getBalance(UUID userId1, UUID userId2) {
        return balanceService.getBalance(userId1, userId2);
    }
    
    /**
     * Get all balances for a user
     * @param userId user ID
     * @return map of user IDs to balance amounts
     */
    public Map<UUID, Double> getUserBalances(UUID userId) {
        return balanceService.getUserBalances(userId);
    }
    
    /**
     * Get net balances for a group
     * @param groupId group ID
     * @return map of user IDs to net balance amounts
     */
    public Map<UUID, Double> getGroupNetBalances(UUID groupId) {
        return groupExpenseService.getGroupNetBalance(groupId);
    }
    
    // Transaction Management
    
    /**
     * Perform a transaction between two users
     * @param senderId sender user ID
     * @param receiverId receiver user ID
     * @param amount transaction amount
     * @return true if successful, false otherwise
     */
    public boolean performTransaction(UUID senderId, UUID receiverId, double amount) {
        return transactionService.performTransaction(senderId, receiverId, amount);
    }
    
    /**
     * Get all transactions for a user
     * @param userId user ID
     * @return list of transactions
     */
    public List<Transaction> getUserTransactions(UUID userId) {
        return transactionService.getAllTransactions(userId);
    }
    
    /**
     * Get a transaction by ID
     * @param transactionId transaction ID
     * @return the transaction if found, null otherwise
     */
    public Transaction getTransaction(UUID transactionId) {
        return transactionService.getTransaction(transactionId);
    }
}
