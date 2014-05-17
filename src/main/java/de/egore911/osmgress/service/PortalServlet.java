package de.egore911.osmgress.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import de.egore911.osmgress.dao.PortalDao;
import de.egore911.osmgress.dao.UserDao;
import de.egore911.osmgress.model.Portal;
import de.egore911.osmgress.model.User;

public class PortalServlet extends HttpServlet {

	private static final long serialVersionUID = -4235963329087474977L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		long portalId = Long.parseLong(req.getParameter("portal"));
		long ownerId = Long.parseLong(req.getParameter("owner"));

		Portal portal = PortalDao.getById(portalId);
		User owner = UserDao.getById(ownerId);

		Map<String, Object> result = new HashMap<>();

		if (portal == null) {
			result.put("status", "error");
			result.put("message", "Portal does not exist");
		} else if (portal.getOwner() != null) {
			result.put("status", "error");
			result.put("message", "Portal is already owned");
		} else {
			portal = PortalDao.own(portal, owner);
			result.put("status", "ok");
			result.put("portal", portal);
		}

		resp.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = resp.getWriter();
		writer.write(new Gson().toJson(result));
	}

}
