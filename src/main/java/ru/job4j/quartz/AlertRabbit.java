package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.*;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("src/main/resources/rabbit.properties");
        Properties config = new Properties();
        try (InputStream in = Rabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int interval = getInterval(file);
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    private static int getInterval(File file) {
        int interval;
        String str;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((str = reader.readLine()) != null) {
                stringBuilder.append(str);
            }
            String[] intervalString = stringBuilder.toString().split("=");
            interval = Integer.parseInt(intervalString[1]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return interval;
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
        }
    }
}
