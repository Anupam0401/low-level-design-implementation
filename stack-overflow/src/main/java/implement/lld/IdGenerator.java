package implement.lld;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static final AtomicInteger postIdCounter = new AtomicInteger(0);
    private static final AtomicInteger userIdCounter = new AtomicInteger(0);

    public static long generatePostId() {
        return postIdCounter.incrementAndGet();
    }

    public static long generateUserId() {
        return userIdCounter.incrementAndGet();
    }
}
