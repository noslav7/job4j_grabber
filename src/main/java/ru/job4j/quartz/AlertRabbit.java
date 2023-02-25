package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRabbit.class.getName());

    public static void main(String[] args) throws FileNotFoundException {
        Properties properties = getProperties();
        try (Connection connection = getConnection(properties)) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("store", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(properties.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            LOG.error(se.getMessage(), se);
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
            Connection store = (Connection) context.getJobDetail().getJobDataMap().get("store");
            try (PreparedStatement statement = store.prepareStatement(
                    "insert into rabbit(created_date) values(?);")
            ) {
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.execute();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public static Properties getProperties() throws FileNotFoundException {
        Properties properties = new Properties();
        try (InputStream fis = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return properties;
    }

    private static Connection getConnection(Properties properties) throws Exception {
        Class.forName(properties.getProperty("driver-class-name"));
        return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username_value"),
                properties.getProperty("password")
        );
    }
}
