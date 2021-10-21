package bot.datasource.repositories;

import bot.entities.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface StatisticsRepository extends JpaRepository<Statistic, Integer> {

    @Query("SELECT statistic FROM Statistic statistic WHERE statistic.date = CURRENT_DATE")
    Statistic getToday();

    @Query("SELECT statistic FROM Statistic statistic WHERE statistic.date = ?1")
    Statistic getByDate(Date date);
}
