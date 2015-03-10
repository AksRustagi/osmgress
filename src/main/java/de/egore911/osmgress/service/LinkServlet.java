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
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import de.egore911.osmgress.dao.LinkDao;
import de.egore911.osmgress.dao.PortalDao;
import de.egore911.osmgress.dao.UserDao;
import de.egore911.osmgress.model.Link;
import de.egore911.osmgress.model.Portal;
import de.egore911.osmgress.model.User;

public class LinkServlet extends HttpServlet {

	private static final long serialVersionUID = 1898699940959369111L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		long fromId = Long.parseLong(req.getParameter("from"));
		long toId = Long.parseLong(req.getParameter("to"));

		Portal from = PortalDao.getById(fromId);
		Portal to = PortalDao.getById(toId);
		User owner = UserDao.getByName(req.getRemoteUser());

		Map<String, Object> result = new HashMap<>();

		if (from.getOwner() == null) {
			result.put("status", "error");
			result.put("message", "From is unowned");
		} else if (from.getOwner().getFaction() != owner.getFaction()) {
			result.put("status", "error");
			result.put("message", "From is owned by different faction");
		} else if (to.getOwner() == null) {
			result.put("status", "error");
			result.put("message", "To is unowned");
		} else if (to.getOwner().getFaction() != owner.getFaction()) {
			result.put("status", "error");
			result.put("message", "To is owned by different faction");
		} else {

			Link existing = LinkDao.findBySourceTarget(from, to);
			if (existing != null) {
				result.put("status", "error");
				result.put("message", "Link already exists");
			} else {
				existing = LinkDao.findBySourceTarget(to, from);
				if (existing != null) {
					result.put("status", "error");
					result.put("message", "Inverse link already exists");
				} else {
					// TODO check incoming and outgoing link count

					result.put("status", "ok");
					result.put("link", LinkDao.create(from, to, owner));
				}
			}
		}

		resp.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = resp.getWriter();
		writer.write(new Gson().toJson(result));
	}

}
