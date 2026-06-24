<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campus.vote.model.Poll" %>
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
%>
<%
    String ctx = request.getContextPath();
    List<Poll> polls = (List<Poll>) request.getAttribute("polls");
    User currentUser = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>在线投票系统</title>
    <link rel="stylesheet" href="<%= ctx %>/assets/style.css">
</head>
<body class="user-page">
<header class="topbar user-bar">
    <div class="topbar-inner">
        <div class="brand-block">
            <div class="brand">校园在线投票系统</div>
            <div class="bar-subtitle">用户端 · 活动投票大厅</div>
        </div>
        <nav class="nav">
            <span class="role-pill">用户：<%= h(currentUser == null ? "" : currentUser.getDisplayName()) %></span>
            <a class="ghost-link" href="<%= ctx %>/logout">退出登录</a>
        </nav>
    </div>
</header>

<main class="page">
    <section class="hero user-hero">
        <div>
            <p class="eyebrow">Campus Voting Platform</p>
            <h1>参与校园活动投票，实时查看民意结果</h1>
            <p>适用于社团活动、班级评选、满意度调查和娱乐互动。用户选择投票主题后提交，系统自动统计票数和比例。</p>
            <div class="hero-actions">
                <a class="button light" href="#polls">查看可参与投票</a>
            </div>
        </div>
        <div class="hero-stats" aria-label="系统特点">
            <div>
                <strong><%= polls == null ? 0 : polls.size() %></strong>
                <span>进行中投票</span>
            </div>
            <div>
                <strong>实时</strong>
                <span>结果统计</span>
            </div>
            <div>
                <strong>防重</strong>
                <span>提交控制</span>
            </div>
        </div>
    </section>

    <section class="section-head" id="polls">
        <div>
            <p class="eyebrow">User System</p>
            <h2>可参与投票</h2>
        </div>
        <span class="soft-tag">选择主题后进入投票页</span>
    </section>

    <section class="surface">
        <div class="poll-list">
            <% if (polls == null || polls.isEmpty()) { %>
                <div class="empty-state">
                    <h2>暂无投票</h2>
                    <p class="muted">当前还没有开放的投票，请等待管理员发布新的投票主题。</p>
                </div>
            <% } else {
                for (Poll poll : polls) {
            %>
                <article class="poll-item">
                    <div class="poll-item-header">
                        <div>
                            <span class="meta-label">投票编号 #<%= poll.getId() %></span>
                            <h2><%= h(poll.getTitle()) %></h2>
                        </div>
                        <span class="badge"><%= poll.getTotalVotes() %> 票</span>
                    </div>
                    <p class="muted"><%= h(poll.getDescription() == null || poll.getDescription().trim().isEmpty() ? "管理员暂未填写说明，点击进入后可直接参与投票。" : poll.getDescription()) %></p>
                    <div class="actions">
                        <a class="button" href="<%= ctx %>/poll?id=<%= poll.getId() %>">参与投票</a>
                    </div>
                </article>
            <%  }
            } %>
        </div>
    </section>
</main>
</body>
</html>
