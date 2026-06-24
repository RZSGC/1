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
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
    List<Poll> polls = (List<Poll>) request.getAttribute("polls");
    Integer pollCount = (Integer) request.getAttribute("pollCount");
    Integer totalVotes = (Integer) request.getAttribute("totalVotes");
    int safePollCount = pollCount == null ? 0 : pollCount;
    int safeTotalVotes = totalVotes == null ? 0 : totalVotes;
    User currentUser = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>发起投票</title>
    <link rel="stylesheet" href="<%= ctx %>/assets/style.css">
</head>
<body class="admin-page">
<header class="topbar admin-bar">
    <div class="topbar-inner">
        <div class="brand-block">
            <div class="brand">校园在线投票系统</div>
            <div class="bar-subtitle">管理员端 · 投票发布后台</div>
        </div>
        <nav class="nav">
            <span class="role-pill">管理员：<%= h(currentUser == null ? "" : currentUser.getDisplayName()) %></span>
            <a class="secondary" href="<%= ctx %>/">返回首页</a>
            <a class="ghost-link" href="<%= ctx %>/logout">退出登录</a>
        </nav>
    </div>
</header>

<main class="page">
    <section class="hero admin-hero">
        <div>
            <p class="eyebrow">Admin System</p>
            <h1>创建和管理校园投票</h1>
            <p>管理员负责配置投票标题、活动说明和候选选项。系统会在数据库中保存主题、选项和投票记录。</p>
            <div class="hero-actions">
                <a class="button light" href="#create">创建新投票</a>
                <a class="button outline-light" href="#published">查看已发布投票</a>
            </div>
        </div>
        <div class="hero-stats" aria-label="管理员流程">
            <div>
                <strong><%= safePollCount %></strong>
                <span>已发布投票</span>
            </div>
            <div>
                <strong><%= safeTotalVotes %></strong>
                <span>累计票数</span>
            </div>
            <div>
                <strong>实时</strong>
                <span>结果统计</span>
            </div>
        </div>
    </section>

    <section class="admin-summary">
        <article>
            <span>发布流程</span>
            <strong>填写主题 → 设置选项 → 查看结果</strong>
        </article>
        <article>
            <span>防重复控制</span>
            <strong>后端校验 + 数据库联合唯一索引</strong>
        </article>
        <article>
            <span>展示方式</span>
            <strong>票数比例驱动 CSS 进度条</strong>
        </article>
    </section>

    <section class="workbench" id="create">
        <aside class="side-panel">
            <p class="eyebrow">Design</p>
            <h2>后台职责</h2>
            <ul class="feature-list">
                <li>创建校园活动、民意调查或娱乐互动投票。</li>
                <li>至少填写两个选项，保证投票数据有效。</li>
                <li>提交后跳转到投票详情页，便于现场展示。</li>
            </ul>
        </aside>

        <section class="panel form-panel">
            <p class="eyebrow">Create Poll</p>
            <h2>投票信息</h2>
            <p class="muted panel-intro">发布前请确认标题清晰、说明完整，并至少填写两个有效选项。</p>
        <% if (error != null && !error.trim().isEmpty()) { %>
            <div class="message error"><%= error %></div>
        <% } %>
        <% if (success != null && !success.trim().isEmpty()) { %>
            <div class="message success"><%= success %></div>
        <% } %>
        <form class="form-grid" method="post" action="<%= ctx %>/admin">
            <input type="hidden" name="action" value="create">
            <label>
                投票标题
                <input name="title" maxlength="120" placeholder="例如：校园歌手大赛最受欢迎节目" required>
            </label>
            <label>
                投票说明
                <textarea name="description" maxlength="500" placeholder="补充活动背景或投票规则"></textarea>
            </label>
            <div>
                <strong>投票选项</strong>
                <div class="option-grid">
                    <input name="options" maxlength="120" placeholder="选项一" required>
                    <input name="options" maxlength="120" placeholder="选项二" required>
                    <input name="options" maxlength="120" placeholder="选项三">
                    <input name="options" maxlength="120" placeholder="选项四">
                    <input name="options" maxlength="120" placeholder="选项五">
                </div>
            </div>
            <div class="actions">
                <button class="button" type="submit">创建投票</button>
            </div>
        </form>
        </section>
    </section>

    <section class="section-head admin-list-head" id="published">
        <div>
            <p class="eyebrow">Published Polls</p>
            <h2>已发布投票</h2>
        </div>
        <span class="soft-tag">管理员可查看全部主题与结果</span>
    </section>

    <section class="surface">
        <div class="admin-poll-list">
            <% if (polls == null || polls.isEmpty()) { %>
                <div class="empty-state">
                    <h2>暂无已发布投票</h2>
                    <p class="muted">请先在上方创建一个投票主题，系统会自动在这里显示管理入口。</p>
                </div>
            <% } else {
                for (Poll poll : polls) {
                    String description = poll.getDescription() == null || poll.getDescription().trim().isEmpty()
                            ? "管理员暂未填写说明。"
                            : poll.getDescription();
            %>
                <article class="admin-poll-item">
                    <div>
                        <div class="poll-item-header">
                            <div>
                                <span class="meta-label">投票编号 #<%= poll.getId() %></span>
                                <h2><%= h(poll.getTitle()) %></h2>
                            </div>
                            <span class="badge"><%= poll.getTotalVotes() %> 票</span>
                        </div>
                        <p class="muted"><%= h(description) %></p>
                    </div>
                    <div class="admin-poll-actions">
                        <a class="button secondary" href="<%= ctx %>/">用户首页</a>
                        <a class="button" href="<%= ctx %>/poll?id=<%= poll.getId() %>">查看结果</a>
                        <form method="post" action="<%= ctx %>/admin">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="pollId" value="<%= poll.getId() %>">
                            <button class="button danger" type="submit" onclick="return confirm('确认删除这个投票吗？删除后票数和记录也会一并清除。');">删除投票</button>
                        </form>
                    </div>
                </article>
            <%  }
            } %>
        </div>
    </section>
</main>
</body>
</html>
