package com.campus.vote.dao;

import com.campus.vote.model.Poll;
import com.campus.vote.model.PollOption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PollDao {
    public List<Poll> findAll() throws Exception {
        String sql = "SELECT p.id, p.title, p.description, p.created_at, COALESCE(SUM(o.vote_count), 0) AS total_votes "
                + "FROM polls p LEFT JOIN poll_options o ON p.id = o.poll_id "
                + "GROUP BY p.id, p.title, p.description, p.created_at "
                + "ORDER BY p.created_at DESC";

        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Poll> polls = new ArrayList<>();
            while (rs.next()) {
                Poll poll = mapPoll(rs);
                poll.setTotalVotes(rs.getInt("total_votes"));
                polls.add(poll);
            }
            return polls;
        }
    }

    public Poll findById(long id) throws Exception {
        String pollSql = "SELECT id, title, description, created_at FROM polls WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(pollSql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Poll poll = mapPoll(rs);
                List<PollOption> options = findOptions(connection, id);
                int total = options.stream().mapToInt(PollOption::getVoteCount).sum();
                for (PollOption option : options) {
                    double percent = total == 0 ? 0 : option.getVoteCount() * 100.0 / total;
                    option.setPercent(percent);
                }
                poll.setOptions(options);
                poll.setTotalVotes(total);
                return poll;
            }
        }
    }

    public long createPoll(String title, String description, String[] options) throws Exception {
        String pollSql = "INSERT INTO polls(title, description) VALUES(?, ?)";
        String optionSql = "INSERT INTO poll_options(poll_id, option_text) VALUES(?, ?)";

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement pollPs = connection.prepareStatement(pollSql, Statement.RETURN_GENERATED_KEYS)) {
                pollPs.setString(1, title);
                pollPs.setString(2, description);
                pollPs.executeUpdate();

                long pollId;
                try (ResultSet keys = pollPs.getGeneratedKeys()) {
                    keys.next();
                    pollId = keys.getLong(1);
                }

                try (PreparedStatement optionPs = connection.prepareStatement(optionSql)) {
                    for (String option : options) {
                        String text = option == null ? "" : option.trim();
                        if (!text.isEmpty()) {
                            optionPs.setLong(1, pollId);
                            optionPs.setString(2, text);
                            optionPs.addBatch();
                        }
                    }
                    optionPs.executeBatch();
                }
                connection.commit();
                return pollId;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public VoteResult vote(long pollId, long optionId, String voterKey) throws Exception {
        String insertVote = "INSERT INTO votes(poll_id, option_id, voter_key) VALUES(?, ?, ?)";
        String updateOption = "UPDATE poll_options SET vote_count = vote_count + 1 WHERE id = ? AND poll_id = ?";

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement votePs = connection.prepareStatement(insertVote);
                 PreparedStatement updatePs = connection.prepareStatement(updateOption)) {
                votePs.setLong(1, pollId);
                votePs.setLong(2, optionId);
                votePs.setString(3, voterKey);
                votePs.executeUpdate();

                updatePs.setLong(1, optionId);
                updatePs.setLong(2, pollId);
                int updated = updatePs.executeUpdate();
                if (updated == 0) {
                    throw new IllegalArgumentException("投票选项不存在");
                }

                connection.commit();
                return VoteResult.SUCCESS;
            } catch (SQLException e) {
                connection.rollback();
                if (isConstraintViolation(e)) {
                    return VoteResult.DUPLICATE;
                }
                throw e;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private List<PollOption> findOptions(Connection connection, long pollId) throws Exception {
        String sql = "SELECT id, poll_id, option_text, vote_count FROM poll_options WHERE poll_id = ? ORDER BY id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, pollId);
            try (ResultSet rs = ps.executeQuery()) {
                List<PollOption> options = new ArrayList<>();
                while (rs.next()) {
                    PollOption option = new PollOption();
                    option.setId(rs.getLong("id"));
                    option.setPollId(rs.getLong("poll_id"));
                    option.setText(rs.getString("option_text"));
                    option.setVoteCount(rs.getInt("vote_count"));
                    options.add(option);
                }
                return options;
            }
        }
    }

    private Poll mapPoll(ResultSet rs) throws Exception {
        Poll poll = new Poll();
        poll.setId(rs.getLong("id"));
        poll.setTitle(rs.getString("title"));
        poll.setDescription(rs.getString("description"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            poll.setCreatedAt(createdAt.toLocalDateTime());
        }
        return poll;
    }

    private boolean isConstraintViolation(SQLException e) {
        String message = e.getMessage();
        return message != null && message.toLowerCase().contains("constraint");
    }

    public enum VoteResult {
        SUCCESS,
        DUPLICATE
    }
}
