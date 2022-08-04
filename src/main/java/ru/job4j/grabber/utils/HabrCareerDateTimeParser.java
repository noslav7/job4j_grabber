package ru.job4j.grabber.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) throws IOException {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(parse);
        return zonedDateTime.toLocalDateTime();
    }
}
