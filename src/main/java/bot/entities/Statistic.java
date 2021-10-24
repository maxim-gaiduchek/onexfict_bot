package bot.entities;

import bot.utils.Formatter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "statistics")
public class Statistic {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "record_date") // TODO: 25.10.2021 fix that bug
    @Temporal(TemporalType.DATE)
    private Date date = new Date();

    @Column(name = "posts")
    private int posts;

    @Column(name = "likes")
    private int likes;

    protected Statistic() {
    }

    public Statistic(Statistic old) {
        posts = old.posts;
        likes = old.likes;
    }

    // getters

    public int getPosts() {
        return posts;
    }

    public int getLikes() {
        return likes;
    }

    public float getLikesPerPost() {
        return posts == 0 ? 0 : Formatter.round((float) likes / posts, 2);
    }

    public Date getDate() {
        return date;
    }

    // setters

    public void incrementPosts() {
        posts++;
    }

    public void incrementLikes() {
        likes++;
    }

    public void decrementLikes() {
        likes--;
    }

    // core

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Statistic statistic = (Statistic) o;

        if (id != statistic.id) return false;
        return date.equals(statistic.date);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + date.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Statistic{" +
                "id=" + id +
                ", date=" + date +
                ", posts=" + posts +
                ", likes=" + likes +
                '}';
    }
}
