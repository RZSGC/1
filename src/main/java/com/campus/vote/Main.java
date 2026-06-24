package com.campus.vote;

import com.campus.vote.web.AdminServlet;
import com.campus.vote.web.AppInitializer;
import com.campus.vote.web.AuthFilter;
import com.campus.vote.web.EncodingFilter;
import com.campus.vote.web.HomeServlet;
import com.campus.vote.web.LoginServlet;
import com.campus.vote.web.LogoutServlet;
import com.campus.vote.web.PollServlet;
import com.campus.vote.web.RegisterServlet;
import com.campus.vote.web.VoteServlet;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = getPort(args);

        Server server = new Server(port);
        WebAppContext context = new WebAppContext();
        context.setContextPath("/vote");
        context.setResourceBase("src/main/webapp");
        context.setParentLoaderPriority(true);
        context.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
        context.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        context.addBean(new ServletContainerInitializersStarter(context), true);

        context.addEventListener(new AppInitializer());
        context.addFilter(new FilterHolder(new EncodingFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));
        context.addFilter(new FilterHolder(new AuthFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(new ServletHolder(new HomeServlet()), "");
        context.addServlet(new ServletHolder(new LoginServlet()), "/login");
        context.addServlet(new ServletHolder(new RegisterServlet()), "/register");
        context.addServlet(new ServletHolder(new LogoutServlet()), "/logout");
        context.addServlet(new ServletHolder(new AdminServlet()), "/admin");
        context.addServlet(new ServletHolder(new PollServlet()), "/poll");
        context.addServlet(new ServletHolder(new VoteServlet()), "/vote");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(rootRedirectHandler());
        handlers.addHandler(context);
        server.setHandler(handlers);
        server.start();

        System.out.println("在线投票系统已启动：http://localhost:" + port + "/vote/login");
        server.join();
    }

    private static int getPort(String[] args) {
        if (args.length == 0) {
            return 8080;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return 8080;
        }
    }

    private static List<ContainerInitializer> jspInitializers() {
        return Collections.singletonList(new ContainerInitializer(new JettyJasperInitializer(), null));
    }

    private static ContextHandler rootRedirectHandler() {
        ContextHandler root = new ContextHandler("/");
        root.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
                if ("/".equals(target)) {
                    response.sendRedirect(request.getContextPath() + "/vote/login");
                    baseRequest.setHandled(true);
                }
            }
        });
        return root;
    }
}
