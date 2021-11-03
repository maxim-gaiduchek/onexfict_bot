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

    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private Date date = Formatter.formatDate(new Date());

    @Column(name = "posts")
    private int posts = 0;

    @Column(name = "likes")
    private int likes = 0;

    @Column(name = "subscribers")
    private int subscribers = 0;

    public Statistic() {
    }

    // getters

    public Date getDate() {
        return date;
    }

    public int getPosts() {
        return posts;
    }

    public int getLikes() {
        return likes;
    }

    public float getLikesPerPost() {
        return posts == 0 ? 0 : Formatter.round((float) likes / posts, 2);
    }

    public int getSubscribers() {
        return subscribers;
    }

    // setters

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setSubscribers(int subscribers) {
        this.subscribers = subscribers;
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
