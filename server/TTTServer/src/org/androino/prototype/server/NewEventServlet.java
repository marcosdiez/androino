package org.androino.prototype.server;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NewEventServlet extends HttpServlet{

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	resp.setContentType("text/plain");
        String message = req.getParameter("MSG");
        String user  = req.getParameter("USER");
        if (message.equals(EventQueue.CONNECT_EVENT_TOKEN)){
        	user = EventQueue.getInstance().connectEvent();
        	resp.getWriter().println(user);
        } else {
        	EventQueue.getInstance().addEvent(user, message);
        	resp.getWriter().println(EventQueue.NO_EVENT_TOKEN);
        }
    }
    
}
