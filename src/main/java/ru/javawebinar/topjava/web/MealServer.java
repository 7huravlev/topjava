package ru.javawebinar.topjava.web;

import org.slf4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.ServerException;

import static org.slf4j.LoggerFactory.getLogger;

public class MealServer extends HttpServlet {
    private static final Logger log = getLogger(MealServer.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServerException, IOException {
        log.debug("redirect to meals");

        response.sendRedirect("meals.jsp");
    }
}
