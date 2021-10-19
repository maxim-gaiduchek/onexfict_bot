package bot.entities;

import bot.datasource.converters.StringToIntList;
import bot.datasource.converters.StringToStringList;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "creator_user_id", referencedColumnName = "chat_id")
    private BotUser creator;

    @Column(name = "images_files_ids")
    @Convert(converter = StringToStringList.class)
    private List<String> imagesFilesIds = new ArrayList<>();

    @Column(name = "text")
    private String text;

    @Column(name = "by")
    private String by;

    @Column(name = "source")
    private String source;

    @Column(name = "likes")
    @Convert(converter = StringToIntList.class)
    private List<Integer> likes = new ArrayList<>();

    @Column(name = "agrees")
    @Convert(converter = StringToIntList.class)
    private List<Integer> agrees = new ArrayList<>();

    @Column(name = "is_posted")
    private boolean isPosted = false;

    @Column(name = "posted")
    @Temporal(TemporalType.DATE)
    private Date posted;

    protected Post() {
    }

    public Post(BotUser creator) {
        this.creator = creator;
    }

    // getters

    public int getId() {
        return id;
    }

    public Long getCreatorId() {
        return creator.getChatId();
    }

    public List<String> getImagesFilesIds() {
        return imagesFilesIds;
    }

    public String getPostText() {
        return (text != null ? (text + "\n\n") : "") +
                (by != null ? ("by " + by + "\n\n") : "") +
                (source != null ? ("[источник](" + source + ")\n\n") : "") +
                "@onexfict";
    }

    public int getLikesCount() {
        return likes.size();
    }

    public int getAgreesCount() {
        return agrees.size();
    }

    public String getWhoHasAgreed() {
        StringBuilder sb = new StringBuilder("[этим](tg://user?id=").append(agrees.get(0)).append(")");

        for (int i = 1; i < agrees.size(); i++) {
            sb.append(", [этим](tg://user?id=").append(agrees.get(i)).append(")");
        }

        return sb.toString();
    }

    public boolean isNotPosted() {
        return !isPosted;
    }

    // setters

    public void addImageFileId(String imageFileId) {
        imagesFilesIds.add(imageFileId);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setBy(String from) {
        this.by = from;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void switchLike(Integer userId) {
        if (likes.contains(userId)) {
            likes.remove(userId);
        } else {
            likes.add(userId);
        }
    }

    public void switchAgree(Integer userId) {
        if (agrees.contains(userId)) {
            agrees.remove(userId);
        } else {
            agrees.add(userId);
        }
    }

    public void setPosted() {
        posted = new Date();
        isPosted = true;
    }

    // core

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        if (id != post.id) return false;
        if (isPosted != post.isPosted) return false;
        if (!Objects.equals(imagesFilesIds, post.imagesFilesIds))
            return false;
        if (!Objects.equals(text, post.text)) return false;
        if (!Objects.equals(by, post.by)) return false;
        if (!Objects.equals(source, post.source)) return false;
        if (!Objects.equals(likes, post.likes)) return false;
        return Objects.equals(agrees, post.agrees);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (imagesFilesIds != null ? imagesFilesIds.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (by != null ? by.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (likes != null ? likes.hashCode() : 0);
        result = 31 * result + (agrees != null ? agrees.hashCode() : 0);
        result = 31 * result + (isPosted ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", creator=" + creator.getChatId() +
                ", imagesFilesIds=" + imagesFilesIds +
                ", text='" + text + '\'' +
                ", by='" + by + '\'' +
                ", source='" + source + '\'' +
                ", likes=" + likes +
                ", agrees=" + agrees +
                ", isPosted=" + isPosted +
                ", posted=" + posted +
                '}';
    }
}
