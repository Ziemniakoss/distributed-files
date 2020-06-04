package com.ziemniakoss.distributed.client.services;

import com.ziemniakoss.distributed.client.models.Directory;
import com.ziemniakoss.distributed.client.models.File;
import com.ziemniakoss.distributed.client.models.Server;
import com.ziemniakoss.distributed.client.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FilesManager {
	private final Logger log = LoggerFactory.getLogger(FilesManager.class);
	private final IDirectoryRepository directoryRepository;
	private final IFileRepository fileRepository;
	private final IServerRepository serverRepository;
	private final IServerFilesRepository serverFilesRepository;

	public FilesManager(IDirectoryRepository directoryRepository, IFileRepository fileRepository, IServerRepository serverRepository, IServerFilesRepository serverFilesRepository) {
		this.directoryRepository = directoryRepository;
		this.fileRepository = fileRepository;
		this.serverRepository = serverRepository;
		this.serverFilesRepository = serverFilesRepository;
	}

	public List<File> getAllInDirectory(Integer directoryId) throws DirectoryDoesNotExistsException {
		Directory d = null;
		if (directoryId != null) {
			d = directoryRepository.get(directoryId).orElseThrow(DirectoryDoesNotExistsException::new);
		}
		return fileRepository.getAllInDirectory(d);
	}

	public void add(MultipartFile file, List<Integer> serverIds, Integer directoryId) throws IOException, DirectoryDoesNotExistsException {
		File f = new File();
		byte[] fileContent = file.getBytes();
		f.setSize((int) file.getSize());
		f.setName(file.getOriginalFilename());
		String hash = calculateMd5Hash(file);
		log.debug("Calculated hash for new file '" + hash + '\'');
		System.out.println(hash);
		f.setHash(hash);
		Directory dir = null;
		if (directoryId != null) {
			dir = directoryRepository.get(directoryId).orElseThrow(DirectoryDoesNotExistsException::new);
		}
		int id = fileRepository.add(f, dir);
		f.setId(id);

		//teraz rozsyłamy plik
		List<Server> servers = serverIds.parallelStream().
				map(e -> serverRepository.get(e).orElse(null)).
				filter(Objects::nonNull).
				peek(server -> sendTo(server, fileContent, f)).
				collect(Collectors.toList());
	}

	private void sendTo(Server server, byte[] file, File fileData) {
		log.debug("Sending file " + fileData.getId() + " to server " + server.getUrl());
		RestTemplate rs = new RestTemplate();
		rs.getMessageConverters().add(new FormHttpMessageConverter());
		MultiValueMap<String, Object> requestParameterMap = new LinkedMultiValueMap<>();
		requestParameterMap.add("attachement", new MultipartByteArrayResource(file, fileData.getName()));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity entity = new HttpEntity(requestParameterMap, headers);
		rs.postForObject("http://"+server.getName()+":8080/files/" + fileData.getId(), entity, String.class);//TODO tutaj na potrzeby dockera jest getName ale w przyszłosci lepsze byłoby po url
		try {
			serverFilesRepository.addToServer(fileData, server);
		} catch (ServerDoesNotExistsException e) {
			log.error("File {} was just sent to non existing server {}", fileData.getId(), server.getUrl());
		} catch (FileDoesNotExistsException e) {
			log.error("File that does not exist in database(id={}) was sent to server {}", fileData.getId(), server.getUrl());
			log.error("Please check if this file was not sent to another servers");
		}
	}

	private String getServerUrl(Server server){
		return server.getName()+":"+ server.getUrl().substring(server.getUrl().lastIndexOf(':'));
	}

	private String calculateMd5Hash(MultipartFile file) throws IOException {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] digested = md.digest(file.getBytes());
		StringBuilder sb = new StringBuilder(32);
		for (byte b : digested) {
			if ((0xff & b) < 0x10) {
				sb.append('0');
				sb.append(Integer.toHexString((0xFF & b)));
			} else {
				sb.append(Integer.toHexString(0xFF & b));
			}
		}
		return sb.toString();
	}

	public File getFile(int id) throws FileDoesNotExistsException {
		return fileRepository.get(id).orElseThrow(FileDoesNotExistsException::new);
	}

	public List<Server> getAllWithFile(int id) throws FileDoesNotExistsException{
		return serverFilesRepository.getAllWith(fileRepository.get(id).orElseThrow(FileDoesNotExistsException::new));
	}

	private class MultipartByteArrayResource extends ByteArrayResource {
		private String filename;

		public MultipartByteArrayResource(byte[] byteArray, String filename) {
			super(byteArray);
			this.filename = filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		@Override
		public String getFilename() {
			return filename;
		}
	}
}
