package de.egore911.osmgress.service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import de.egore911.osmgress.dao.UserDao;
import de.egore911.osmgress.model.User;

public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = -1255599882710642514L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		User user = UserDao.getByName(req.getRemoteUser());

		PrintWriter writer = resp.getWriter();
		writer.write(new Gson().toJson(user));
	}

}
