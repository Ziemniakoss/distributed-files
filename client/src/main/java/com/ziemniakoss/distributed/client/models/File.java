package com.ziemniakoss.distributed.client.models;

import java.sql.Timestamp;
import java.util.Objects;

public class File {
	private int id;
	private String name;
	private Timestamp creationDate;
	private int size;
	private String hash;
	private Directory directory;

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
		this.name = name;
	}

	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Directory getDirectory() {
		return directory;
	}

	public void setDirectory(Directory directory) {
		this.directory = directory;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		File file = (File) o;
		return id == file.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
