package com.coolioasjulio.topiccloud.servlets;

import com.coolioasjulio.topiccloud.TwitterAPI;
import com.google.gson.Gson;
import twitter4j.Twitter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class JSONTwitterServlet<Q,A> extends HttpServlet {
    private final Class<Q> reqClass;

    public JSONTwitterServlet(Class<Q> reqClass) {
        this.reqClass = reqClass;
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("application/json".equalsIgnoreCase(req.getContentType())) {
            Gson gson = new Gson();
            Q request = gson.fromJson(req.getReader(), reqClass);

            if (request != null && validRequest(request)) {
                TwitterAPI client = (TwitterAPI) req.getSession().getAttribute("twitterClient");
                // default to application only sign-in if this user hasn't signed in
                if (client == null) client = TwitterAPI.getSingleton();
                A response = handleRequest(client, request);
                String json = gson.toJson(response);
                resp.setStatus(200);
                resp.setContentType("application/json");
                resp.getWriter().print(json);
                resp.getWriter().flush();
            } else {
                resp.sendError(400, "Illegally formatted request!");
            }
        } else {
            resp.sendError(400, "Content type must be json!");
        }
    }

    protected abstract boolean validRequest(Q request);

    protected abstract A handleRequest(TwitterAPI client, Q request) throws ServletException;
}
