<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Locale" %>
<%@ page import="com.campus.vote.model.Poll" %>
<%@ page import="com.campus.vote.model.PollOption" %>
<%@ page import="com.campus.vote.model.User" %>
<%!
    private String h(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String pct(double value) {
        return String.format(Locale.US, "%.1f", value);
    }
%>
<%
    String ctx = request.getContextPath();
    Poll poll = (Poll) request.getAttribute("poll");
    String message = request.getParameter("message");
    User currentUser = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= h(poll.getTitle()) %></title>
    <link rel="stylesheet" href="<%= ctx %>/assets/style.css">
</head>
<body class="user-page">
<header class="topbar user-bar">
    <div class="topbar-inner">
        <div class="brand-block">
            <div class="brand">校园在线投票系统</div>
            <div class="bar-subtitle">用户端 · 投票与实时结果</div>
        </div>
        <nav class="nav">
            <span class="role-pill">用户：<%= h(currentUser == null ? "" : currentUser.getDisplayName()) %></span>
            <a class="secondary" href="<%= ctx %>/">返回首页</a>
            <a class="ghost-link" href="<%= ctx %>/logout">退出登录</a>
        </nav>
    </div>
</header>

<main class="page">
    <section class="hero user-hero compact-hero">
        <div>
            <p class="eyebrow">Voting Detail</p>
            <h1><%= h(poll.getTitle()) %></h1>
            <p><%= h(poll.getDescription() == null || poll.getDescription().trim().isEmpty() ? "请选择一个选项并填写姓名或学号，提交后即可查看实时统计结果。" : poll.getDescription()) %></p>
        </div>
        <div class="hero-stats" aria-label="投票概览">
            <div>
                <strong><%= poll.getTotalVotes() %></strong>
                <span>累计票数</span>
            </div>
            <div>
                <strong><%= poll.getOptions().size() %></strong>
                <span>候选选项</span>
            </div>
        </div>
    </section>

    <% if (message != null && !message.trim().isEmpty()) { %>
        <div class="message"><%= h(message) %></div>
    <% } %>

    <section class="<%= currentUser != null && currentUser.isAdmin() ? "result-layout" : "vote-layout" %>">
        <% if (currentUser == null || !currentUser.isAdmin()) { %>
            <section class="panel">
                <p class="eyebrow">User Vote</p>
                <h2>参与投票</h2>
                <p class="muted panel-intro">当前账号：<%= h(currentUser == null ? "" : currentUser.getDisplayName()) %>。请选择一个选项提交，同一账号只能参与一次。</p>
                <form class="form-grid" method="post" action="<%= ctx %>/vote">
                    <input type="hidden" name="pollId" value="<%= poll.getId() %>">
                    <div class="option-grid">
                        <% for (PollOption option : poll.getOptions()) { %>
                            <label class="radio-row">
                                <input type="radio" name="optionId" value="<%= option.getId() %>" required>
                                <span><%= h(option.getText()) %></span>
                            </label>
                        <% } %>
                    </div>
                    <div class="actions">
                        <button class="button" type="submit">提交投票</button>
                    </div>
                </form>
            </section>
        <% } %>

        <section class="panel">
            <p class="eyebrow">Live Result</p>
            <h2><%= currentUser != null && currentUser.isAdmin() ? "投票数据" : "实时结果" %></h2>
            <p class="muted panel-intro">票数比例由后端实时计算，并绑定到下方 CSS 进度条。</p>
            <div class="results">
                <% for (PollOption option : poll.getOptions()) {
                    String percent = pct(option.getPercent());
                %>
                    <div class="result-item">
                        <div class="result-head">
                            <strong><%= h(option.getText()) %></strong>
                            <span class="muted"><%= option.getVoteCount() %> 票 / <%= percent %>%</span>
                        </div>
                        <div class="progress" aria-label="<%= h(option.getText()) %>">
                            <div class="progress-bar" style="width: <%= percent %>%"></div>
                        </div>
                    </div>
                <% } %>
            </div>
        </section>
    </section>
</main>
</body>
</html>
