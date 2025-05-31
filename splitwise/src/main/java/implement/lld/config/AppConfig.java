package implement.lld.config;

import implement.lld.repository.InMemoryTransactionRepository;
import implement.lld.repository.impl.InMemoryBalanceRepository;
import implement.lld.repository.impl.InMemoryGroupRepository;
import implement.lld.repository.impl.InMemoryUserRepository;
import implement.lld.repository.interfaces.IBalanceRepository;
import implement.lld.repository.interfaces.IGroupRepository;
import implement.lld.repository.interfaces.ITransactionRepository;
import implement.lld.repository.interfaces.IUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Application configuration class that sets up all the beans for the application.
 * This configuration uses in-memory implementations of the repositories, but can be
 * easily switched to use database implementations in the future.
 */
@Configuration
public class AppConfig {

    /**
     * Creates a user repository bean
     * @return an implementation of IUserRepository
     */
    @Bean
    @Primary
    public IUserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    /**
     * Creates a group repository bean
     * @return an implementation of IGroupRepository
     */
    @Bean
    @Primary
    public IGroupRepository groupRepository() {
        return new InMemoryGroupRepository();
    }

    /**
     * Creates a transaction repository bean
     * @return an implementation of ITransactionRepository
     */
    @Bean
    @Primary
    public ITransactionRepository transactionRepository() {
        return new InMemoryTransactionRepository();
    }

    /**
     * Creates a balance repository bean
     * @return an implementation of IBalanceRepository
     */
    @Bean
    @Primary
    public IBalanceRepository balanceRepository() {
        return new InMemoryBalanceRepository();
    }
}
