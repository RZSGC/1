package com.campus.vote.web;

import com.campus.vote.dao.UserDao;
import com.campus.vote.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user != null) {
            response.sendRedirect(request.getContextPath() + (user.isAdmin() ? "/admin" : "/"));
            return;
        }
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trim(request.getParameter("username"));
        String password = trim(request.getParameter("password"));
        if (username.isEmpty() || password.isEmpty()) {
            request.setAttribute("loginError", "请输入账号和密码。");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            return;
        }

        try {
            User user = userDao.findByCredentials(username, password);
            if (user == null) {
                request.setAttribute("loginError", "账号或密码错误，请重新输入。");
                request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
                return;
            }
            request.getSession().setAttribute("currentUser", user);
            response.sendRedirect(request.getContextPath() + (user.isAdmin() ? "/admin" : "/"));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
