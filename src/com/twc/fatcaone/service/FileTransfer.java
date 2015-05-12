package com.twc.fatcaone.service;

import java.io.File;
import java.io.FileInputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mongodb.DB;

public class FileTransfer {

	 public boolean sftpFileTransfer(String fileName,DB db){
		 boolean fileTransfer=false;
		    String SFTPHOST = "54.172.126.52";
	        int SFTPPORT = 4022;
	        String SFTPUSER = "dolenzak";
	        String SFTPPASS = "TWCfatca1!";
	        String SFTPWORKINGDIR = "/Outbox/840";

	        Session session = null;
	        Channel channel = null;
	        ChannelSftp channelSftp = null;
	        System.out.println("preparing the host information for sftp.");
	        try {
	            JSch jsch = new JSch();
	            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
	            session.setPassword(SFTPPASS);
	            java.util.Properties config = new java.util.Properties();
	            config.put("StrictHostKeyChecking", "no");
	            session.setConfig(config);
	            session.connect();
	            System.out.println("Host connected.");
	            channel = session.openChannel("sftp");
	            channel.connect();
	            System.out.println("sftp channel opened and connected.");
	            channelSftp = (ChannelSftp) channel;
	            channelSftp.cd(SFTPWORKINGDIR);
	            File f = new File(System.getProperty("user.dir")+"/"+fileName);
	            channelSftp.put(new FileInputStream(f), f.getName());
	            fileTransfer = true;
	           System.out.println("File transfered successfully to host.");
	        } catch (Exception ex) {
	             System.out.println("Exception found while tranfer the response.");
	        }
	        finally{

	            channelSftp.exit();
	            System.out.println("sftp Channel exited.");
	            channel.disconnect();
	            System.out.println("Channel disconnected.");
	            session.disconnect();
	            System.out.println("Host Session disconnected.");
	        }
	        return fileTransfer;
	 }
}
