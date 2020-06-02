package com.ziemniakoss.distributed.server;

import java.util.Optional;

public interface IFileRepository {
	/**
	 * Czy plik istnieje i czy jest zarejestrownay na tym serwerz
	 *
	 * @param id id pliku
	 */
	boolean existsAndRegistered(int id);

	Optional<File> get(int id);

	/**
	 * Czy plik istnieje w bazie danych?
	 *
	 * @param id pliku
	 */
	boolean exists(int id);

	/**
	 * Usuwa plik z rejestru plików na aktualnym serwerze ale nie sam plik z bazy danych
	 *
	 * @param f plik zarejestrowany na tym serwerze do usunięcia
	 */
	void unregister(File f);

	/**
	 * Rejestruje plik w bazie danych na aktualnym serwerze
	 *
	 * @param fileId plik do zarejestrowania
	 */
	void register(int fileId) throws FileDoesNotExist;
}
