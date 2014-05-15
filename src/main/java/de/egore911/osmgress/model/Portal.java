package de.egore911.osmgress.model;

import de.egore911.osmgress.dao.UserDao;

public class Portal {

	private Long osmId;
	private Long ownerId;
	private User owner;

	public Long getOsmId() {
		return osmId;
	}

	public void setOsmId(Long osmId) {
		this.osmId = osmId;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
		owner = null;
	}

	public User getOwner() {
		if (owner == null) {
			owner = UserDao.getById(ownerId);
		}
		return owner;
	}
}
