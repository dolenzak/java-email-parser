package com.twc.fatcaone.service;

import java.io.File;

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
	
	public static void main(String a[]){
try{
    //Database Connection
  	DB db = new DataBaseConnection().dbConnection();
  	DBCollection collection = null;
  	DBObject document  = null;
  	collection =db.getCollection("fatcaFile");
	document = new BasicDBObject();
	document.put("fileType", "zip");
	DBCursor cursor=collection.find(document);
	String collectionName="icmmMessageNotification";
    //Save FATCA Notification Header Group data in TWC database
	collection = db.getCollection(collectionName);
	 while(cursor.hasNext()) {
		 DBObject dbObject = cursor.next();
     	String countryCode = dbObject.get("country").toString();
     	String hostName = dbObject.get("ipAddress").toString();
		 File[] xmlFiles=new ReadFile().getFiles(db,"fatcaFile",hostName,countryCode,"zip");
		 for (File notificationXmlFile : xmlFiles) {
			 if(parseXml(notificationXmlFile,collection,db)){
				 if(notificationXmlFile.exists()){
					 notificationXmlFile.delete();
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
			  NodeList createDate=null,notificationRefId=null,senderId=null,idesTransmissionId=null,idesSendingDate=null,senderFileId=null,actionRequested=null,actionRequestedDueDate=null,notification=null,notificationCode=null;
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
			      if(document!=null){
			      collection.save(document);
			      parsedXml=true;
			      }
			      
			      //Store Notification Code in IRSDashboad
			        saveNotificationCode(db,((Node) notificationCode.item(0)).getNodeValue(),((Node) senderFileId.item(0)).getNodeValue());
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
