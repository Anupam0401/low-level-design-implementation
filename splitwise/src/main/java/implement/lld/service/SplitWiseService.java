package implement.lld.service;

import implement.lld.repository.ExpenseRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class SplitWiseService {
    private static volatile SplitWiseService instance;

    private final BalanceService balanceService;
    private final TransactionService transactionService;
    private final ExpenseRepository expenseRepository;

    @Autowired
    public SplitWiseService(
        BalanceService balanceService,
        TransactionService transactionService,
        ExpenseRepository expenseRepository
    ) {
        this.balanceService = balanceService;
        this.transactionService = transactionService;
        this.expenseRepository = expenseRepository;
    }

    public static SplitWiseService getInstance() {
        if (instance == null) {
            synchronized (SplitWiseService.class) {
                if (instance == null) {
                    instance = new SplitWiseService(
                        new BalanceService(),
                        new TransactionService(),
                        new ExpenseRepository()
                    );
                }
            }
        }
        return instance;
    }
}
