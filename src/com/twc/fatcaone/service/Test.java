package com.twc.fatcaone.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;



public class Test {

	public static void main(String[] args)  {
		// TODO Auto-generated method stub
		String abc = "a";
		System.out.println(abc.substring(0,1).toUpperCase()+abc.substring(1));
		//try{
			/*String filename = System.getProperty("user.dir")+"/"+"20150506T160655214Z_C34VPZ.00000.SP.840.zip";
		//send("/home/hacker/sathiyanTest.xml");
		System.out.println(filename);
		//send(filename);
		 // Base64 decode the data
		String privKeyPEM = System.getProperty("user.dir")+"/Keystore/serverfile/fatcaone_private_key.pem";
		FileInputStream fis = new FileInputStream(privKeyPEM);
        byte [] encoded = Base64.decode(privKeyPEM);

        // PKCS8 decode the encoded RSA private key

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);
        System.out.println(privKey);*/
			/*DBCollection collection =new DataBaseConnection().dbConnection().getCollection("resourceBundle");
	        DBObject contentObj = new BasicDBObject();
	        contentObj.put("msg_id", "sucessNotification");
	        DBCursor cursor = collection.find(contentObj);
	        String mail_subject = null;
	        String mail_body = null;
	        
	        while(cursor.hasNext()) {
	        	DBObject dbObject = cursor.next();
	        	mail_subject = dbObject.get("mail_subject").toString();
	        	mail_body = dbObject.get("mail_body").toString();
	        }
	        if (mail_body == null || mail_body.length() == 0) {
	        	System.out.println("False");
	        }
	        Properties properties = new Properties();
	        properties.put("mail.smtp.auth", "true");
	  	      properties.put("mail.smtp.starttls.enable", "true");
	  	      properties.put("mail.smtp.host",  "smtp.zoho.com");
	  	      properties.put("mail.smtp.port", 587);
	  	      
	  	      String imagePath=System.getProperty("user.dir")+File.separatorChar+"images/twc-logo-002.png";
	  	      //Append mail data in mail template
	  	      // Get the default Session object.
	  	        Session session = Session.getInstance(properties);
	  	     // Session session = Session.getInstance(properties);
	  	      try{
	  	         // Send message
	  	         Transport trans =session.getTransport("smtp");
	  	         trans.connect("smtp.zoho.com", "donotreply@fatcaone.com", "FATCAtwc1!");
	  	         Message message = new MimeMessage(session);
	  	         message.setFrom(new InternetAddress("donotreply@fatcaone.com"));
	  	         message.addRecipient(Message.RecipientType.TO,new InternetAddress("muthukrishnan.m@mitosistech.com"));
	  	         message.setSubject(mail_subject);
	  	         message.setContent(mail_body,"text/html" );
	  			 trans.sendMessage(message,message.getAllRecipients());
	  	         System.out.println("Sent message successfully....");
	  	      }catch (MessagingException mex) {
	  	         mex.printStackTrace();
	  	      }
		}catch(Exception e){
			System.out.println("Exception : "+e);
		}*/
	}
	/*public static void send (String fileName) {
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
            File f = new File(fileName);
            channelSftp.put(new FileInputStream(f), f.getName());
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
    }*/   
}
