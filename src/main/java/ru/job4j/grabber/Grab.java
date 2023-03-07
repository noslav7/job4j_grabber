package ru.job4j.grabber;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public interface Grab {

    void start(HabrCareerParse habrCareerParse, Store start_store, Scheduler scheduler) throws SchedulerException;
}
