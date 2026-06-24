package com.campus.vote.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

public final class Database {
    private static final String JDBC_URL = "jdbc:sqlite:./data/vote-db.sqlite";

    private Database() {
    }

    public static Connection getConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection(JDBC_URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initialize() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            executeSchema(statement);
            seedDemoPoll(connection);
        } catch (Exception e) {
            throw new IllegalStateException("数据库初始化失败", e);
        }
    }

    private static void executeSchema(Statement statement) throws Exception {
        try (InputStream input = Database.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (input == null) {
                throw new IllegalStateException("找不到 schema.sql");
            }
            String sql = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            for (String part : sql.split(";")) {
                String command = part.trim();
                if (!command.isEmpty()) {
                    statement.execute(command);
                }
            }
        }
    }

    private static void seedDemoPoll(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM polls")) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        }

        PollDao dao = new PollDao();
        dao.createPoll(
                "校园活动满意度调查",
                "请选择你对本次校园活动的整体评价，系统会实时统计投票结果。",
                new String[]{"非常满意", "满意", "一般", "需要改进"}
        );
    }
}
