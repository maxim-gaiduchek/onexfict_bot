package bot.entities;

import bot.datasource.converters.StringToIntList;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class BotUser {

    @Id
    @Column(name = "chat_id")
    private long chatId;

    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private Status status = Status.INACTIVE;

    @OneToOne
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private Post post;

    @Column(name = "created_posts_ids")
    @Convert(converter = StringToIntList.class)
    private List<Integer> createdPostsIds = new ArrayList<>();

    protected BotUser() {
    }

    public BotUser(long chatId) {
        this.chatId = chatId;
    }

    // getters

    public long getChatId() {
        return chatId;
    }

    public Status getStatus() {
        return status;
    }

    public Post getPost() {
        return post;
    }

    public List<Integer> getCreatedPostsIds() {
        return createdPostsIds;
    }

    // setters

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public void addCreatedPost(int postId) {
        createdPostsIds.add(postId);
    }

    // status

    public enum Status {
        INACTIVE,
        IS_ADDING_PHOTO, IS_ADDING_TEXT, IS_ADDING_BY, IS_ADDING_SOURCE,
        IS_EDITING_PHOTO, IS_EDITING_TEXT, IS_EDITING_BY, IS_EDITING_SOURCE,
    }

    // core

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BotUser botUser = (BotUser) o;

        if (chatId != botUser.chatId) return false;
        if (status != botUser.status) return false;
        return Objects.equals(post, botUser.post);
    }

    @Override
    public int hashCode() {
        int result = (int) (chatId ^ (chatId >>> 32));
        result = 31 * result + status.hashCode();
        result = 31 * result + (post != null ? post.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BotUser{" +
                "chatId=" + chatId +
                ", status=" + status +
                ", post=" + post +
                ", createdPostsIds=" + createdPostsIds +
                '}';
    }
}
