package ru.job4j.grabber;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public interface Grab {

    void start(HabrCareerParse habrCareerParse, Store startStore, Scheduler scheduler) throws SchedulerException;
}
