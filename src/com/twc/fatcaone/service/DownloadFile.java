package com.twc.fatcaone.service;

import java.io.File;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class DownloadFile {

	public String downloadFile(String host,int port,String username, String password, String filePath,String fileFormat,String fileType) {
        Session session = null;
        Channel channel = null;
        String homeDirectory = System.getProperty("user.home");
        String localDownloadRootDir = homeDirectory+File.separatorChar+host;
        String localDownloadDir = localDownloadRootDir+File.separatorChar+fileType;
		try {
			JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            System.out.println("Host connected.");
            channel = session.openChannel("sftp");
            channel.connect();
        } catch(JSchException e) {
            System.out.println("Could not connect: " + e.toString());

        }

        if (channel.isConnected()) {
        	
            int grabCount=0;
            try {   
                ChannelSftp c = (ChannelSftp) channel;  
                c.cd(filePath);
                File rootPath = new File(localDownloadRootDir); 
                File downloadPath = new File(localDownloadDir);
                if(!rootPath.exists()){
                	rootPath.mkdir();
                }
                if(!downloadPath.exists()){
                	downloadPath.mkdir();
                }
				c.lcd(localDownloadDir);
                System.out.println("lcd " + c.lpwd());
                
                // Get a listing of the remote directory
                @SuppressWarnings("unchecked")
                Vector<ChannelSftp.LsEntry> list = c.ls("."); 

                // iterate through objects in list, identifying specific file names
                for (ChannelSftp.LsEntry oListItem : list) {
                    // output each item from directory listing for logs
                     
                    if(oListItem.getFilename().contains(fileFormat)){
                    	System.out.println(oListItem.toString());
                    // If it is a file (not a directory)
                    if (!oListItem.getAttrs().isDir()) {
                        // Grab the remote file ([remote filename], [local path/filename to write file to])

                    	System.out.println("get " + oListItem.getFilename());
                        c.get(oListItem.getFilename(), oListItem.getFilename());  // while testing, disable this or all of your test files will be grabbed

                        grabCount++; 

                        // Delete remote file
                        	c.rm(oListItem.getFilename());  // Note for SFTP grabs from this remote host, deleting the file is unnecessary, 
                                                          //   as their system automatically moves an item to the 'downloaded' subfolder
                                                          //   after it has been grabbed.  For other target hosts, un comment this line to remove any downloaded files from the inbox.
                    }
                }
                }

                // Report files grabbed to log
                if (grabCount == 0) { 
                    System.out.println("Found no new files to grab.");
                } else {
                	System.out.println("Retrieved " + grabCount + " new files.");
                }                           
            } catch(SftpException e) {
            	System.out.println(e.toString());
            } finally {
                // disconnect session.  If this is not done, the job will hang and leave log files locked
                session.disconnect();
                System.out.println("Session Closed");
            }
	}
		return localDownloadDir;

}
}