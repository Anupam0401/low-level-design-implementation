package implement.lld.post;

import java.util.Arrays;
import java.util.List;

public enum PostType {
    QUESTION,
    ANSWER,
    COMMENT;

    public static List<PostType> getAllPostTypes() {
        return Arrays.asList(PostType.values());
    }
}
