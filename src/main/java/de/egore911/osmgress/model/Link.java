package de.egore911.osmgress.model;

import de.egore911.osmgress.dao.PortalDao;

public class Link {

	private Long id;
	private Long sourceOsmId;
	private Portal source;
	private Long targetOsmId;
	private Portal target;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Long getSourceOsmId() {
		return sourceOsmId;
	}

	public void setSourceOsmId(Long sourceOsmId) {
		this.sourceOsmId = sourceOsmId;
		source = null;
	}
	
	public Portal getSource() {
		if (source == null) {
			source = PortalDao.getById(sourceOsmId);
		}
		return source;
	}

	public Long getTargetOsmId() {
		return targetOsmId;
	}

	public void setTargetOsmId(Long targetOsmId) {
		this.targetOsmId = targetOsmId;
		target = null;
	}
	
	public Portal getTarget() {
		if (target == null) {
			target = PortalDao.getById(targetOsmId);
		}
		return target;
	}


}
