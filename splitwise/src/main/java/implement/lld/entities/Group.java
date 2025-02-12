package implement.lld.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class Group {
    @Setter
    private String name;
    @Setter
    private String description;
    private final User owner;
    private final List<User> members;

    public Group(User owner, String description, String name) {
        this.members = new CopyOnWriteArrayList<>();
        this.owner = owner;
        this.description = description;
        this.name = name;
    }

    public void addMember(User user) {
        members.add(user);
    }
}
