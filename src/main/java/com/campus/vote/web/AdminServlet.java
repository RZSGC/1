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

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void showAdminPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Poll> polls = pollDao.findAll();
            int totalVotes = polls.stream().mapToInt(Poll::getTotalVotes).sum();

            request.setAttribute("polls", polls);
            request.setAttribute("pollCount", polls.size());
            request.setAttribute("totalVotes", totalVotes);
            request.getRequestDispatcher("/WEB-INF/jsp/admin.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
