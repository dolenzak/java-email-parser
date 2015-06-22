package com.twc.fatcaone.service;

import java.io.File;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class ReadNotification {
	
	//public static void main(String a[]){
	 public static void getNotification(){
		 System.out.println("Read Notification Start");
try{
    //Database Connection
  	DB db = new DataBaseConnection().dbConnection();
  	DBCollection collection = null;
  	DBObject document  = null;
  	collection =db.getCollection("fatcaFile");
	document = new BasicDBObject();
	document.put("fileType", "zip");
	document.put("country", "US");
	DBCursor cursor=collection.find(document);
	String collectionName="icmmMessageNotification";
    //Save FATCA Notification Header Group data in TWC database
	collection = db.getCollection(collectionName);
	 while(cursor.hasNext()) {
		 DBObject dbObject = cursor.next();
     	String countryCode = dbObject.get("country").toString();
     	String hostName = dbObject.get("ipAddress").toString();
		 File[] xmlFiles=new ReadFile().getFiles(db,"fatcaFile",hostName,countryCode,"zip");
		 if(xmlFiles!=null){
		 for (File notificationXmlFile : xmlFiles) {
			 if(parseXml(notificationXmlFile,collection,db)){
				 if(notificationXmlFile.exists()){
					 notificationXmlFile.delete();
				 }
			 }
	 }
		 }
	 }
	}catch(Exception e){
		System.out.println("Read Notification Exception : "+e);
	}
}
	
	public static boolean parseXml(File notificationXml,DBCollection collection,DB db){
				boolean parsedXml= false;
		try {
			  DBObject document = new BasicDBObject("_id",collection.count()+1);
			  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			  DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
			  Document doc = documentBuilder.parse(notificationXml);
			  doc.getDocumentElement().normalize();
			  NodeList notificationHeaderGrp = doc.getElementsByTagName("FATCANotificationHeaderGrp");
			  NodeList originalFileMetadataGrp = doc.getElementsByTagName("OriginalFileMetadataGrp");
			  NodeList actionRequestedGrp = doc.getElementsByTagName("ActionRequestedGrp");
			  NodeList notificationContent = doc.getElementsByTagName("NotificationContentTxt");
			  NodeList originalFileProcessingDataGrp = doc.getElementsByTagName("OriginalFileProcessingDataGrp");
			  NodeList createDate=null,notificationRefId=null,senderId=null,idesTransmissionId=null,idesSendingDate=null,senderFileId=null,actionRequested=null,actionRequestedDueDate=null,notification=null,notificationCode=null,financialInstitutionCount=null,recordCount=null,accountReportRecordCount=null,pooledReportRecordCount=null;
			  //for (int s = 0; s < notificationHeaderGrp.getLength(); s++) {

			    Node fstNode = notificationHeaderGrp.item(0);
			    
			    if (fstNode!=null && fstNode.getNodeType() == Node.ELEMENT_NODE) {
			  
			      Element fstElmnt = (Element) fstNode;
			      
			      NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("FATCANotificationCreateTs");
			      if(fstNmElmntLst!=null && fstNmElmntLst.getLength()>0){
			      Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
			      createDate = fstNmElmnt.getChildNodes();
			      }
			      NodeList secNmElmntLst = fstElmnt.getElementsByTagName("FATCANotificationRefId");
			      if(secNmElmntLst!=null && secNmElmntLst.getLength()>0){
			      Element secNmElmnt = (Element) secNmElmntLst.item(0);
			      notificationRefId = secNmElmnt.getChildNodes();
			      }
			      NodeList thNmElmntLst = fstElmnt.getElementsByTagName("FATCAEntitySenderId");
			      if(thNmElmntLst!=null && thNmElmntLst.getLength()>0){
			      Element thNmElmnt = (Element) thNmElmntLst.item(0);
			      senderId = thNmElmnt.getChildNodes();
			    }
			      NodeList frNmElmntLst = fstElmnt.getElementsByTagName("FATCANotificationCd");
			      if(frNmElmntLst!=null && frNmElmntLst.getLength()>0){
			      Element frNmElmnt = (Element) frNmElmntLst.item(0);
			      notificationCode = frNmElmnt.getChildNodes();
			    }
			    }
			  //}
			  //for (int s = 0; s < originalFileMetadataGrp.getLength(); s++) {

				     fstNode = originalFileMetadataGrp.item(0);
				    
				    if (fstNode!=null &&  fstNode.getNodeType() == Node.ELEMENT_NODE) {
				  
				      Element fstElmnt = (Element) fstNode;
				      
				      NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("IDESTransmissionId");
				      if(fstNmElmntLst!=null && fstNmElmntLst.getLength()>0){
				      Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
				      idesTransmissionId = fstNmElmnt.getChildNodes();
				      }
				      NodeList secNmElmntLst = fstElmnt.getElementsByTagName("IDESSendingTs");
				      if(secNmElmntLst!=null && secNmElmntLst.getLength()>0){
				      Element secNmElmnt = (Element) secNmElmntLst.item(0);
				      idesSendingDate = secNmElmnt.getChildNodes();
				      }
				      NodeList thNmElmntLst = fstElmnt.getElementsByTagName("SenderFileId");
				      if(thNmElmntLst!=null && thNmElmntLst.getLength()>0){
				      Element thNmElmnt = (Element) thNmElmntLst.item(0);
				      senderFileId = thNmElmnt.getChildNodes();
				    }
				    }
				 // }
			  //for (int s = 0; s < actionRequestedGrp.getLength(); s++) {

				     fstNode = actionRequestedGrp.item(0);
				    
				    if (fstNode!=null && fstNode.getNodeType() == Node.ELEMENT_NODE) {
				  
				      Element fstElmnt = (Element) fstNode;
				      
				      NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("ActionRequestedTxt");
				      if(fstNmElmntLst!=null && fstNmElmntLst.getLength()>0){
				      Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
				      actionRequested = fstNmElmnt.getChildNodes();
				      }
				      NodeList secNmElmntLst = fstElmnt.getElementsByTagName("ActionRequestedDueDateTxt");
				      if(secNmElmntLst!=null && secNmElmntLst.getLength()>0){
				      Element secNmElmnt = (Element) secNmElmntLst.item(0);
				      actionRequestedDueDate = secNmElmnt.getChildNodes();
				    }
				    }
				  //}
			  
			      if(notificationContent!=null && notificationContent.getLength()>0){
			      Element fstNmElmnt = (Element) notificationContent.item(0);
			      notification = fstNmElmnt.getChildNodes();
			      }
			      
			      //Get the submitted record counts
			         fstNode = originalFileProcessingDataGrp.item(0);
				    
				    if (fstNode!=null && fstNode.getNodeType() == Node.ELEMENT_NODE) {
				  
				      Element fstElmnt = (Element) fstNode;
				      
				      NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("FinancialInstitutionCnt");
				      if(fstNmElmntLst!=null && fstNmElmntLst.getLength()>0){
				      Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
				      financialInstitutionCount = fstNmElmnt.getChildNodes();
				      }
				      NodeList secNmElmntLst = fstElmnt.getElementsByTagName("RecordCnt");
				      if(secNmElmntLst!=null && secNmElmntLst.getLength()>0){
				      Element secNmElmnt = (Element) secNmElmntLst.item(0);
				      recordCount = secNmElmnt.getChildNodes();
				      }
				      NodeList thNmElmntLst = fstElmnt.getElementsByTagName("AccountReportRecordCnt");
				      if(thNmElmntLst!=null && thNmElmntLst.getLength()>0){
				      Element thNmElmnt = (Element) thNmElmntLst.item(0);
				      accountReportRecordCount = thNmElmnt.getChildNodes();
				    }
				      NodeList frNmElmntLst = fstElmnt.getElementsByTagName("PooledReportRecordCnt");
				      if(frNmElmntLst!=null && frNmElmntLst.getLength()>0){
				      Element frNmElmnt = (Element) frNmElmntLst.item(0);
				      pooledReportRecordCount = frNmElmnt.getChildNodes();
				    }
				    }
			      
			      
			      //Save data to database
			      if(notificationRefId!=null && notificationRefId.getLength()>0){
			    	  document.put("notificationRefId", ((Node) notificationRefId.item(0)).getNodeValue());
			      }
			      if(idesTransmissionId!=null && idesTransmissionId.getLength()>0){
			    	  document.put("idesTransmissionId", ((Node) idesTransmissionId.item(0)).getNodeValue());
			      }
			      if(senderId!=null && senderId.getLength()>0){
			    	  document.put("senderId", ((Node) senderId.item(0)).getNodeValue());
			      }
			      if(senderFileId!=null && senderFileId.getLength()>0){
			    	  document.put("senderFileId", ((Node) senderFileId.item(0)).getNodeValue());
			      }
			      if(notification!=null && notification.getLength()>0){
			    	  document.put("notification", ((Node) notification.item(0)).getNodeValue());
			      }
			      if(actionRequested!=null && actionRequested.getLength()>0){
			    	  document.put("actionRequested", ((Node) actionRequested.item(0)).getNodeValue());
			      }
			      if(actionRequestedDueDate!=null && actionRequestedDueDate.getLength()>0){
			    	  document.put("actionRequestedDueDate", ((Node) actionRequestedDueDate.item(0)).getNodeValue());
			      }
			      if(createDate!=null && createDate.getLength()>0){
			    	  document.put("createDate", ((Node) createDate.item(0)).getNodeValue());
			      }
			      if(idesSendingDate!=null && idesSendingDate.getLength()>0){
			    	  document.put("idesSendingDate", ((Node) idesSendingDate.item(0)).getNodeValue());
			      }
			      if(financialInstitutionCount!=null && financialInstitutionCount.getLength()>0){
			    	  document.put("financialInstitutionCount", ((Node) financialInstitutionCount.item(0)).getNodeValue());
			      }
			      if(recordCount!=null && recordCount.getLength()>0){
			    	  document.put("recordCount", ((Node) recordCount.item(0)).getNodeValue());
			      }
			      if(accountReportRecordCount!=null && accountReportRecordCount.getLength()>0){
			    	  document.put("accountReportRecordCount", ((Node) accountReportRecordCount.item(0)).getNodeValue());
			      }
			      if(pooledReportRecordCount!=null && pooledReportRecordCount.getLength()>0){
			    	  document.put("pooledReportRecordCount", ((Node) pooledReportRecordCount.item(0)).getNodeValue());
			      }
			      if(document!=null){
			      collection.save(document);
			      parsedXml=true;
			      }
			      
			      //Store Notification Code in IRSDashboad
			      if(notificationCode!=null){
			        saveNotificationCode(db,((Node) notificationCode.item(0)).getNodeValue(),((Node) senderFileId.item(0)).getNodeValue());
			        //Send Error Notification
				       if(!((Node) notificationCode.item(0)).getNodeValue().equalsIgnoreCase("NIM")){
				    	   //get Mail Template from resourcebundle
				    	   collection =db.getCollection("resourceBundle");
					        DBObject contentObj = new BasicDBObject();
					        contentObj.put("msg_id", "errorNotification");
					        DBCursor cursor = collection.find(contentObj);
					        String mail_subject = null;
					        String mail_body = null;
					        
					        while(cursor.hasNext()) {
					        	DBObject dbObject = cursor.next();
					        	mail_subject = dbObject.get("mail_subject").toString();
					        	mail_body = dbObject.get("mail_body").toString();
					        }
					        if (mail_body == null || mail_body.length() == 0) {
					        	return false;
					        }
				    	   
					       //Get Mail Authentication details from fatcaFile
				    	   collection = db.getCollection("fatcaFile");
				    	   DBObject dbObject = new BasicDBObject();
				    	   	dbObject.put("fileType", "mail");
				    	   	dbObject.put("protocol", "smtp");
					        DBCursor dbCursor = collection.find(dbObject);
					        String username=null,password=null,to=null,host=null,protocol=null,from=null;
					        int port=587;
					        while(dbCursor.hasNext()) {
					        	DBObject dbObj = dbCursor.next();
					        	username = dbObj.get("username").toString();
					        	password= dbObj.get("password").toString();
					        	host = dbObj.get("ipAddress").toString();
					        	port = Integer.parseInt(dbObj.get("port").toString());
					        	protocol = dbObj.get("protocol").toString();
					        }
					        if(username!=null){
					        	from = username;
					        }
					        // get sending Mail address
					        collection = db.getCollection("irsMailAddress");
					    	   DBObject dbObje = new BasicDBObject();
					    	   dbObje.put("country", "US");
					    	   dbObje.put("type", "ERROR");
						        DBCursor dbCur = collection.find(dbObje);
				  	      // Get system properties
				  	      Properties properties = new Properties();

				  	      // Setup mail server
				  	      properties.put("mail.smtp.auth", "true");
				  	      properties.put("mail.smtp.starttls.enable", "true");
				  	      properties.put("mail.smtp.host", host);
				  	      properties.put("mail.smtp.port", port);
				  	      
				  	      String imagePath=System.getProperty("user.dir")+File.separatorChar+"images/twc-logo-002.png";
				  	      //Append mail data in mail template
				  	      mail_body = new String(mail_body.replaceAll("%msg", ((Node) notification.item(0)).getNodeValue()));
				  	      mail_body = new String(mail_body.replaceAll("%actionRequest",((Node) actionRequested.item(0)).getNodeValue()));
				  	      mail_body = new String(mail_body.replaceAll("%actionDueDate", ((Node) actionRequestedDueDate.item(0)).getNodeValue()));
				  	      mail_body = new String(mail_body.replaceAll("%timestamp", ((Node) createDate.item(0)).getNodeValue()));
				  	      mail_body = new String(mail_body.replaceAll("%refId", ((Node) notificationRefId.item(0)).getNodeValue()));
				  	      mail_body = new String(mail_body.replaceAll("%senderId", ((Node) senderId.item(0)).getNodeValue()));
				  	      mail_body = new String(mail_body.replaceAll("%transmissionId", ((Node) idesTransmissionId.item(0)).getNodeValue()));
				  	      mail_body = new String(mail_body.replaceAll("%sendingTimestamp", ((Node) idesSendingDate.item(0)).getNodeValue()));
				  	      mail_body = new String(mail_body.replaceAll("%fileId", ((Node) senderFileId.item(0)).getNodeValue()));
				  	      //mail_body = new String(mail_body.replaceAll("%logo", "<img src='"+imagePath+"' alt='TWC' width='100px' height='100px' style='float:left;'/>"));
				  	      // Get the default Session object.
				  	        Session session = Session.getInstance(properties);
				  	     // Session session = Session.getInstance(properties);
				  	      try{
				  	         // Send message
				  	         Transport trans =session.getTransport(protocol);
				  	         trans.connect(host, username, password);
				  	         Message message = new MimeMessage(session);
				  	         message.setFrom(new InternetAddress(from));
				  	         message.addRecipient(Message.RecipientType.TO,new InternetAddress(dbCur.next().get("mailId").toString()));
				  	         message.setSubject(mail_subject);
				  	         message.setContent(mail_body,"text/html" );
				  			 trans.sendMessage(message,message.getAllRecipients());
				  	         System.out.println("Sent message successfully....");
				  	      }catch (MessagingException mex) {
				  	         mex.printStackTrace();
				  	      }
				       }
			      }
			      System.out.println("Read Notification End");
			  } catch (Exception e) {
			    e.printStackTrace();
			  }
		return parsedXml;
	}
	
	//Update NotificationCode in IRSDashboard collection
	public static void saveNotificationCode(DB db,String notificationCode,String senderFileId){
		DBCollection collection =db.getCollection("IRSDashboard");
		DBObject query = new BasicDBObject("idesFile", senderFileId);
		DBObject update = new BasicDBObject();
        update.put("$set", new BasicDBObject("notificationCode",notificationCode));
        collection.update(query, update);
	}
}
