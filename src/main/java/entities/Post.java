package entities;

import datasource.converters.StringToIntList;
import datasource.converters.StringToStringList;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "images_files_ids")
    @Convert(converter = StringToStringList.class)
    private List<String> imagesFilesIds = new ArrayList<>();

    @Column(name = "text")
    private String text;

    @Column(name = "likes")
    @Convert(converter = StringToIntList.class)
    private List<Integer> likes = new ArrayList<>();

    @Column(name = "agrees")
    @Convert(converter = StringToIntList.class)
    private List<Integer> agrees = new ArrayList<>();

    @Column(name = "is_posted")
    private boolean isPosted = false;

    public Post() {}

    // getters

    public int getId() {
        return id;
    }

    public List<String> getImagesFilesIds() {
        return imagesFilesIds;
    }

    public String getText() {
        return text;
    }

    public boolean hasText() {
        return text != null;
    }

    public int getLikesCount() {
        return likes.size();
    }

    public int getAgreesCount() {
        return agrees.size();
    }

    public String getWhoHasAgreed() {
        StringBuilder sb = new StringBuilder("[этим](tg://user?id=").append(agrees.get(0)).append(")");

        for (int i = 1; i < agrees.size() - 1; i++) {
            sb.append(", [этим](tg://user?id=").append(agrees.get(i)).append(")");
        }
        sb.append(" и [этим](tg://user?id=").append(agrees.get(agrees.size() - 1)).append(")");

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
        isPosted = true;
    }

    // core

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        if (id != post.id) return false;
        if (!imagesFilesIds.equals(post.imagesFilesIds)) return false;
        if (!Objects.equals(text, post.text)) return false;
        if (!likes.equals(post.likes)) return false;
        return agrees.equals(post.agrees);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + imagesFilesIds.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + likes.hashCode();
        result = 31 * result + agrees.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", imagesFilesIds=" + imagesFilesIds +
                ", text='" + text + '\'' +
                ", likes=" + likes +
                ", agrees=" + agrees +
                '}';
    }
}
