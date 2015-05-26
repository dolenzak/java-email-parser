package com.twc.fatcaone.service;

import java.io.File;
import java.io.FileInputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class FileTransfer {

	 public boolean sftpFileTransfer(String fileName,DB db,String countryCode, String fileType){
		 	boolean fileTransfer=false;
		 	DBCollection collection = db.getCollection("fatcaFile");
	        DBObject document = new BasicDBObject();
	        document.put("country", countryCode);
	        document.put("fileType",fileType);
	        DBCursor cursor = collection.find(document);
	        String ipAddress=null,username=null,password=null,filePath=null,country=null;
	        int port=22;
	        
	        while(cursor.hasNext()) {
	        	DBObject dbObject = cursor.next();
	        	ipAddress=dbObject.get("ipAddress").toString();
	        	username=dbObject.get("username").toString();
	        	password=dbObject.get("password").toString();
	        	port=Integer.parseInt(dbObject.get("port").toString());
	        	filePath=dbObject.get("filePath").toString();
	        	fileType=dbObject.get("fileType").toString();
	        	country=dbObject.get("country").toString();
	        }
		    /*String SFTPHOST = "54.172.126.52";
	        int SFTPPORT = 4022;
	        String SFTPUSER = "dolenzak";
	        String SFTPPASS = "TWCfatca1!";
	        String SFTPWORKINGDIR = "/Outbox/840";*/

	        Session session = null;
	        Channel channel = null;
	        ChannelSftp channelSftp = null;
	        System.out.println("preparing the host information for sftp.");
	        try {
	            JSch jsch = new JSch();
	            session = jsch.getSession(username, ipAddress, port);
	            session.setPassword(password);
	            java.util.Properties config = new java.util.Properties();
	            config.put("StrictHostKeyChecking", "no");
	            session.setConfig(config);
	            session.connect();
	            System.out.println("Host connected.");
	            channel = session.openChannel("sftp");
	            channel.connect();
	            System.out.println("sftp channel opened and connected.");
	            channelSftp = (ChannelSftp) channel;
	            channelSftp.cd(filePath);
	            File f = new File(System.getProperty("user.dir")+"/"+fileName);
	            channelSftp.put(new FileInputStream(f), f.getName());
	            fileTransfer = true;
	           System.out.println("File transfered successfully to host.");
	        } catch (Exception ex) {
	             System.out.println("Exception found while tranfer the response.");
	        }
	        finally{
	        	if(channel!=null){
	            channelSftp.exit();
	            System.out.println("sftp Channel exited.");
	            channel.disconnect();
	            System.out.println("Channel disconnected.");
	            session.disconnect();
	            System.out.println("Host Session disconnected.");
	        	}else{
	        		System.out.println("Host Could Not connected");
	        	}
	        }
	        return fileTransfer;
	 }
}
