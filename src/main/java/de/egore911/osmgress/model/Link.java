package de.egore911.osmgress.model;

public class Link {

	private Long id;
	private Portal source;
	private Portal target;
	private User owner;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Portal getSource() {
		return source;
	}

	public void setSource(Portal source) {
		this.source = source;
	}

	public Portal getTarget() {
		return target;
	}

	public void setTarget(Portal target) {
		this.target = target;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

}
