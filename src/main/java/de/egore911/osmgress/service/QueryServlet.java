package de.egore911.osmgress.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.egore911.osmgress.dao.LinkDao;
import de.egore911.osmgress.dao.PortalDao;

public class QueryServlet extends HttpServlet {

	private static final long serialVersionUID = 6670846638516835427L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		JsonParser parser = new JsonParser();
		JsonElement jsonElement = parser.parse(new InputStreamReader(req
				.getInputStream()));

		if (jsonElement.isJsonObject()) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();

			double lat_min = jsonObject.get("lat_min").getAsDouble();
			double lat_max = jsonObject.get("lat_max").getAsDouble();
			double lon_min = jsonObject.get("lon_min").getAsDouble();
			double lon_max = jsonObject.get("lon_max").getAsDouble();

			loadPortals(resp, lat_min, lat_max, lon_min, lon_max);
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		double lat_min = Double.parseDouble(req.getParameter("lat_min"));
		double lat_max = Double.parseDouble(req.getParameter("lat_max"));
		double lon_min = Double.parseDouble(req.getParameter("lon_min"));
		double lon_max = Double.parseDouble(req.getParameter("lon_max"));

		loadPortals(resp, lat_min, lat_max, lon_min, lon_max);
	}

	private void loadPortals(HttpServletResponse resp, double lat_min,
			double lat_max, double lon_min, double lon_max) throws IOException,
			ServletException {

		Map<String, Object> result = new HashMap<>();

		result.put("portals",
				PortalDao.getWithin(lat_min, lat_max, lon_min, lon_max));
		result.put("links",
				LinkDao.getWithin(lat_min, lat_max, lon_min, lon_max));

		resp.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = resp.getWriter();
		writer.write(new Gson().toJson(result));
	}

}
