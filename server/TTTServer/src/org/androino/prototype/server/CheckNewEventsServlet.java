package org.androino.prototype.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CheckNewEventsServlet extends HttpServlet{

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	resp.setContentType("text/plain");
        String user  = req.getParameter("USER");
        String response = EventQueue.getInstance().consumeEvents(user);
        resp.getWriter().println(response);
    } 

}
