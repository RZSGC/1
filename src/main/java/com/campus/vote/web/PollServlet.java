package com.campus.vote.web;

import com.campus.vote.dao.PollDao;
import com.campus.vote.model.Poll;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/poll")
public class PollServlet extends HttpServlet {
    private final PollDao pollDao = new PollDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = parseLong(request.getParameter("id"));
        try {
            Poll poll = pollDao.findById(id);
            if (poll == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "投票不存在");
                return;
            }
            request.setAttribute("poll", poll);
            request.getRequestDispatcher("/WEB-INF/jsp/poll.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return -1L;
        }
    }
}
