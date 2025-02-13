package implement.lld.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class User {
    private final UUID id;
    @Setter
    private String name;
    @Setter
    private String email;

    public User(UUID id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
