package com.campus.vote.web;

import com.campus.vote.dao.UserDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trim(request.getParameter("username"));
        String password = trim(request.getParameter("password"));
        String displayName = trim(request.getParameter("displayName"));
        String role = "ADMIN".equals(request.getParameter("role")) ? "ADMIN" : "USER";

        if (username.isEmpty() || password.length() < 6 || displayName.isEmpty()) {
            request.setAttribute("registerError", "请填写昵称、账号，并设置至少 6 位密码。");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        try {
            UserDao.CreateUserResult result = userDao.createUser(username, password, displayName, role);
            if (result == UserDao.CreateUserResult.DUPLICATE_USERNAME) {
                request.setAttribute("registerError", "该账号已被注册，请更换账号。");
                request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                return;
            }
            request.setAttribute("registerSuccess", "注册成功，请使用账号登录。");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
