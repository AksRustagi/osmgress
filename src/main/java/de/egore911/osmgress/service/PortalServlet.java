/*
 *   Copyright 2014 Christoph Brill
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package de.egore911.osmgress.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import de.egore911.osmgress.dao.PortalDao;
import de.egore911.osmgress.dao.UserDao;
import de.egore911.osmgress.model.Direction;
import de.egore911.osmgress.model.Portal;
import de.egore911.osmgress.model.Slot;
import de.egore911.osmgress.model.User;

public class PortalServlet extends HttpServlet {

	private static final long serialVersionUID = -4235963329087474977L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String method = req.getRequestURI().substring(
				req.getRequestURI().lastIndexOf('/') + 1);

		long portalId = Long.parseLong(req.getParameter("portal"));

		Portal portal = PortalDao.getById(portalId);
		User owner = UserDao.getByName(req.getRemoteUser());

		Map<String, Object> result = new HashMap<>();

		switch (method) {
		case "deployResonator":
			if (portal == null) {
				result.put("status", "error");
				result.put("message", "Portal does not exist");
			} else if (portal.getSlots() != null
					&& portal.getSlots().size() >= 8) {
				result.put("status", "error");
				result.put("message", "Portal already full");
			} else if (portal.getOwner() != null
					&& owner.getFaction() != portal.getOwner().getFaction()) {
				result.put("status", "error");
				result.put("message", "Portal owned by other faction full");
			} else {
				if (portal.getSlots() == null) {
					portal.setSlots(new ArrayList<Slot>(1));
				}
				Slot slot = new Slot();
				slot.setDirection(getNextFreeDirection(portal.getSlots()));
				slot.setOwnerId(owner.getId());
				portal.getSlots().add(slot);
				portal = PortalDao.own(portal,
						portal.getOwner() == null ? owner : portal.getOwner());
				result.put("status", "ok");
				result.put("portal", portal);
			}
			break;
		default:
			result.put("status", "error");
			result.put("message", "Unknown");
		}
		resp.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = resp.getWriter();
		writer.write(new Gson().toJson(result));
	}

	private Direction getNextFreeDirection(Collection<Slot> slots) {
		// TODO this is O(n^2). Need to use a proper data structure here but for
		// now and for n=8 it's not that bad
		outer: for (Direction direction : Direction.values()) {
			for (Slot slot : slots) {
				if (slot.getDirection() == direction) {
					continue outer;
				}
			}
			return direction;
		}
		throw new IllegalArgumentException("No free slots available");
	}

}
