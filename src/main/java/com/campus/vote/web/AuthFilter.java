package com.campus.vote.web;

import com.campus.vote.model.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getServletPath();

        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (path.startsWith("/admin") && !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        if ("".equals(path) && user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/assets")
                || "/login".equals(path)
                || "/register".equals(path);
    }
}
