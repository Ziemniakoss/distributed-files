package com.ziemniakoss.distributed.client.models;

import javax.validation.constraints.Pattern;
import java.util.Objects;

public class Directory {
	private int id;
	@Pattern(regexp = "[a-zA-Z0-9_\\- ]+", message = "Name can only contain numbers, letters, spaces and _-")
	private String name;
	private Directory parent;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name == null ? null : name.trim();
	}

	public Directory getParent() {
		return parent;
	}

	public void setParent(Directory parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "Directory{" +
				"id=" + id +
				", name='" + name + '\'' +
				", parent=" + parent +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Directory directory = (Directory) o;
		return id == directory.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
