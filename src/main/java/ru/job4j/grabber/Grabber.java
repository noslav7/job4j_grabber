package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private static final Logger LOG = LoggerFactory.getLogger(Grabber.class.getName());
    private final Properties cfg = new Properties();
    private Parse parse;
    private Store store;
    private Scheduler scheduler;
    private int time;

    public Grabber() {
    }

    public Store store() throws SQLException {
        return new PsqlStore(cfg);
    }

    public void scheduler() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
    }

    public void cfg() throws IOException {
        try (InputStream in = Grabber.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            cfg.load(in);
        }
    }

    @Override
    public void start() throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(time)
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static class GrabJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            try {
                for (Post post : parse.list(
                        "https://career.habr.com/vacancies/java_developer?page=", 1)) {
                    store.save(post);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(cfg.getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes());
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg();
        grab.scheduler();
        grab.scheduler.start();
        grab.time = Integer.parseInt(grab.cfg.getProperty("time"));
        grab.parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        grab.store = grab.store();
        grab.start();
        grab.web(grab.store);
    }
}
