package com.campus.vote.web;

import com.campus.vote.dao.PollDao;
import com.campus.vote.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/vote")
public class VoteServlet extends HttpServlet {
    private final PollDao pollDao = new PollDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long pollId = parseLong(request.getParameter("pollId"));
        long optionId = parseLong(request.getParameter("optionId"));
        User user = (User) request.getSession().getAttribute("currentUser");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (user.isAdmin()) {
            redirectWithMessage(request, response, pollId, "管理员账号仅用于后台管理，不能参与投票。");
            return;
        }
        if (pollId <= 0 || optionId <= 0) {
            redirectWithMessage(request, response, pollId, "请选择一个投票选项。");
            return;
        }

        String voterKey = "user:" + user.getId();
        try {
            PollDao.VoteResult result = pollDao.vote(pollId, optionId, voterKey);
            if (result == PollDao.VoteResult.DUPLICATE) {
                redirectWithMessage(request, response, pollId, "你已经参与过该投票，不能重复提交。");
            } else {
                redirectWithMessage(request, response, pollId, "投票成功，结果已实时更新。");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void redirectWithMessage(HttpServletRequest request, HttpServletResponse response, long pollId, String message)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/poll?id=" + pollId + "&message=" + urlEncode(message));
    }

    private String urlEncode(String text) throws IOException {
        return java.net.URLEncoder.encode(text, "UTF-8");
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return -1L;
        }
    }

}
