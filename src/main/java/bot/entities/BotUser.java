package bot.entities;

import bot.datasource.converters.StringToLongList;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
    @Convert(converter = StringToLongList.class)
    private List<Long> createdPostsIds = new ArrayList<>();

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

    public List<Long> getCreatedPostsIds() {
        return createdPostsIds;
    }

    // setters

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public void addCreatedPost(long postId) {
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

        return chatId == botUser.chatId;
    }

    @Override
    public int hashCode() {
        return (int) (chatId ^ (chatId >>> 32));
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
