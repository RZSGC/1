<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
    String loginError = (String) request.getAttribute("loginError");
    String registerError = (String) request.getAttribute("registerError");
    String registerSuccess = (String) request.getAttribute("registerSuccess");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>校园在线投票系统登录</title>
    <link rel="stylesheet" href="<%= ctx %>/assets/style.css">
</head>
<body class="auth-page">
<main class="auth-shell">
    <section class="auth-hero">
        <p class="eyebrow">Campus Voting Platform</p>
        <h1>校园在线投票系统</h1>
        <p>登录后系统会根据账号角色进入对应页面：普通用户参与投票，管理员创建投票并查看数据。</p>
        <div class="auth-features">
            <span>账号注册登录</span>
            <span>用户与管理员分权</span>
            <span>实时票数统计</span>
        </div>
    </section>

    <section class="auth-panel">
        <div class="auth-grid">
            <article class="auth-card">
                <p class="eyebrow">Sign In</p>
                <h2>账号登录</h2>
                <% if (loginError != null) { %>
                    <div class="message error"><%= loginError %></div>
                <% } %>
                <% if (registerSuccess != null) { %>
                    <div class="message success"><%= registerSuccess %></div>
                <% } %>
                <form class="form-grid" method="post" action="<%= ctx %>/login">
                    <label>
                        账号
                        <input name="username" maxlength="80" placeholder="请输入账号" required>
                    </label>
                    <label>
                        密码
                        <input type="password" name="password" maxlength="80" placeholder="请输入密码" required>
                    </label>
                    <button class="button" type="submit">登录系统</button>
                </form>
            </article>

            <article class="auth-card">
                <p class="eyebrow">Create Account</p>
                <h2>注册账号</h2>
                <% if (registerError != null) { %>
                    <div class="message error"><%= registerError %></div>
                <% } %>
                <form class="form-grid" method="post" action="<%= ctx %>/register">
                    <label>
                        姓名或昵称
                        <input name="displayName" maxlength="80" placeholder="例如：张三" required>
                    </label>
                    <label>
                        登录账号
                        <input name="username" maxlength="80" placeholder="例如：zhangsan" required>
                    </label>
                    <label>
                        登录密码
                        <input type="password" name="password" maxlength="80" placeholder="至少 6 位" required>
                    </label>
                    <label>
                        账号角色
                        <select name="role">
                            <option value="USER">普通用户</option>
                            <option value="ADMIN">管理员</option>
                        </select>
                    </label>
                    <button class="button secondary" type="submit">注册账号</button>
                </form>
            </article>
        </div>
    </section>
</main>
</body>
</html>
