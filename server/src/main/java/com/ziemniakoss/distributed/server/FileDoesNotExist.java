package com.ziemniakoss.distributed.server;

public class FileDoesNotExist extends Exception {
	private final int id;

	public FileDoesNotExist(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
