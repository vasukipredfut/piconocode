package com.aria.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import com.aria.config.Constants;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Configuration
public class SFTPConfig {

//	@Bean
////	@RequestScope
//	public FTPClient ftpConnection() throws IOException {
//		FTPClient ftp = new FTPClient();
//		ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
//		ftp.connect(Constants.FTP_HOST, Constants.FTP_PORT); // mention host and port for server
//
//		System.out.println("ftp" + ftp.getReplyCode());
////        ftp.login("ec2-user", "vvs$2022"); //enter your FTP server username and password here
//		ftp.enterLocalPassiveMode(); // important!
//		ftp.login(Constants.FTP_USERNAME, Constants.FTP_PASSWORD);
//		ftp.changeWorkingDirectory(Constants.FTP_MEDIA_BASE_PATH);
//
//		ftp.setFileType(FTP.BINARY_FILE_TYPE);
//
//		return ftp;
//
//	}
//	private JSch jsch;
//	private final String host;
//	private final int port;
//	private final String username;
//	private ChannelSftp channel;
//	private Session session;
	
//	public SFTPConfig(String host, int port, String username) {
//		this.host = host;
//		this.port = port;
//		this.username = username;
//		jsch = new JSch();
//	}
//	
//	public SFTPConfig(String host, String username) {
//		this(host, Constants.FTP_PORT, username);
//	}
	
	public ChannelSftp setupSFTPSession() throws JSchException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(Constants.FTP_USERNAME, Constants.FTP_HOST);
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.setPassword(Constants.FTP_PASSWORD);
		session.connect();
		return (ChannelSftp) session.openChannel("sftp");
	}
	

}
