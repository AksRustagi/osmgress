/*
 *   Copyright 2014 Christoph Brill
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
