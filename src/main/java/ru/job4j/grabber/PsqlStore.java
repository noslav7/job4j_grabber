package ru.job4j.grabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    public static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());
    private Connection cnn;

    public static void main(String[] args) throws SQLException {
        Post postJunior = new Post("junior java developer", "https://career.habr.com/vacancies/1000115597",
                "responsibilities: task completion", LocalDateTime.now());
        Post postMiddle = new Post("middle java developer", "https://career.habr.com/vacancies/1000113405",
                "responsibilities: project creation and execution", LocalDateTime.now());
        Post postSenior = new Post("senior java developer", "https://career.habr.com/vacancies/1000113428",
                "responsibilities: problem solving", LocalDateTime.now());


        try (InputStream input = PsqlStore.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties properties = new Properties();
            properties.load(input);

            PsqlStore store = new PsqlStore(properties);
            store.save(postJunior);
            store.save(postMiddle);
            store.save(postSenior);

            System.out.println(store.getAll());
            System.out.println(store.findById(2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        cnn = DriverManager.getConnection(
                cfg.getProperty("url"),
                cfg.getProperty("username_value"),
                cfg.getProperty("password")
        );
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = cnn.prepareStatement(
                "insert into posts(title, link, description, created) values (?, ?, ?, ?)"
                        + " ON CONFLICT (link) DO UPDATE SET link = excluded.link || ';' || posts.link;",
                Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getLink());
            statement.setString(3, post.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet set = statement.getGeneratedKeys()) {
                if (set.next()) {
                    post.setId(set.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> rsl = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement(
                "select * from posts;"
        )) {
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    rsl.add(getPost(set));
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return rsl;
    }

    @Override
    public Post findById(int id) {
        Post rsl = null;
        try (PreparedStatement statement = cnn.prepareStatement(
                "select * from posts where id = ?;"
        )) {
            statement.setInt(1, id);
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    rsl = getPost(set);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return rsl;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post getPost(ResultSet set) throws SQLException {
        return new Post(
                set.getInt("id"),
                set.getString("title"),
                set.getString("link"),
                set.getString("description"),
                set.getTimestamp("created").toLocalDateTime()
        );
    }
}
