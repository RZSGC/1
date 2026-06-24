package com.campus.vote.web;

import com.campus.vote.dao.PollDao;
import com.campus.vote.model.Poll;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private final PollDao pollDao = new PollDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showAdminPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = trim(request.getParameter("action"));
        if ("delete".equals(action)) {
            handleDelete(request, response);
            return;
        }

        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));
        String[] rawOptions = request.getParameterValues("options");
        List<String> options = new ArrayList<>();

        if (rawOptions != null) {
            for (String option : rawOptions) {
                String text = trim(option);
                if (!text.isEmpty()) {
                    options.add(text);
                }
            }
        }

        if (title.isEmpty() || options.size() < 2) {
            request.setAttribute("error", "请输入投票标题，并至少填写两个选项。");
            showAdminPage(request, response);
            return;
        }

        try {
            long pollId = pollDao.createPoll(title, description, options.toArray(new String[0]));
            response.sendRedirect(request.getContextPath() + "/poll?id=" + pollId);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long pollId = parseLong(request.getParameter("pollId"));
        if (pollId <= 0) {
            response.sendRedirect(request.getContextPath() + "/admin?error=" + urlEncode("要删除的投票不存在。"));
            return;
        }

        try {
            boolean deleted = pollDao.deletePoll(pollId);
            if (deleted) {
                response.sendRedirect(request.getContextPath() + "/admin?success=" + urlEncode("投票已删除。"));
            } else {
                response.sendRedirect(request.getContextPath() + "/admin?error=" + urlEncode("要删除的投票不存在。"));
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return -1L;
        }
    }

    private String urlEncode(String text) throws IOException {
        return java.net.URLEncoder.encode(text, "UTF-8");
    }

    private void showAdminPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Poll> polls = pollDao.findAll();
            int totalVotes = polls.stream().mapToInt(Poll::getTotalVotes).sum();

            if (request.getAttribute("error") == null) {
                request.setAttribute("error", trim(request.getParameter("error")));
            }
            if (request.getAttribute("success") == null) {
                request.setAttribute("success", trim(request.getParameter("success")));
            }
            request.setAttribute("polls", polls);
            request.setAttribute("pollCount", polls.size());
            request.setAttribute("totalVotes", totalVotes);
            request.getRequestDispatcher("/WEB-INF/jsp/admin.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
