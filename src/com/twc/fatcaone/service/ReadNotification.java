package com.twc.fatcaone.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.fatca.notification.FATCAFileErrorNotificationType;
import com.fatca.notification.FATCANotificationHeaderGrpType;
import com.fatca.notification.FATCARecordErrorDetailGrpType;
import com.fatca.notification.FATCARecordErrorFIGrpType;
import com.fatca.notification.FATCARecordErrorGrpType;
import com.fatca.notification.FATCAValidFileNotificationType;
import com.fatca.notification.FieldErrorGrpType;
import com.fatca.notification.OriginalFileMessageSpecGrpType;
import com.fatca.notification.OriginalFileMetadataGrpType;
import com.fatca.notification.OriginalFileProcessingDataGrpType;
import com.fatca.notification.Signature;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class ReadNotification {
	
	/*public static void main(String a[]){
		DB db = new DataBaseConnection().dbConnection();
		//Sample Sucess Record
	 //System.out.println(parseXml(new File("/media/hacker/C5FC-C0E3/TWC/IssuesDoc/27/successful.xml"),db.getCollection("icmmMessageNotification"),db));
		//Sample Error Record
		/System.out.println(parseXml(new File("/media/hacker/C5FC-C0E3/TWC/IssuesDoc/27/New/12-06-10-44-14-Payload.xml"),db.getCollection("icmmMessageNotification"),db));
		//System.out.println(parseXml(new File("/home/hacker/ggts-bundle/Downloads/XMLPayload_0.xml"),db.getCollection("icmmMessageNotification"),db));
	}*/
	 public static boolean getNotification(String idesTransactionId){
		 System.out.println("Read Notification Start");
		 boolean parsedXml = false;
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
		 String filePath = dbObject.get("filePath").toString();
		 File[] xmlFiles=new ReadFile().getFiles(db,"fatcaFile",hostName,countryCode,"zip", idesTransactionId);
		 if(xmlFiles!=null){
		 for (File notificationXmlFile : xmlFiles) {
			 parsedXml=parseXml(notificationXmlFile,collection,db);
			 if(parsedXml){
		        	File f = new File(filePath);
		        	if(!f.exists()){
		        		f.mkdir();
		        	}
		    	    if(!new File(f.getAbsolutePath() + "/backup/").exists()){
		    	    	new File(f.getAbsolutePath() + "/backup/").mkdir();
		    	    }

				 	System.out.println("We are going to back the .xml up at:" + filePath);

		    	   File dest=new File(f.getAbsolutePath()+"/backup/"+notificationXmlFile.getName());
		    	   notificationXmlFile.renameTo(dest);
				 if(notificationXmlFile!=null && notificationXmlFile.exists()){
					 notificationXmlFile.delete();
				 }
			 }
			 return parsedXml;
	     }
		 }
	 }
	}catch(Exception e){
		System.out.println("Read Notification Exception : "+e);
	}
	return parsedXml;
}
	
	public static boolean parseXml(File notificationXml,DBCollection collection,DB db){
				boolean parsedXml= false;
				boolean isEmailReadAndSave = false;
		try {
			  DBObject document = new BasicDBObject("_id",collection.count()+1);
			  
			  
			  // === Test ===
			  
			  JAXBContext jaxbContext = JAXBContext.newInstance(Signature.class);
			  Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			  Source source = new StreamSource(new FileInputStream(notificationXml));
			  JAXBElement<Signature> unmarshalledObject = unmarshaller.unmarshal(source,Signature.class);
			  Signature signature= unmarshalledObject.getValue();
				
			  //Create Object as per xml tag
			  FATCAValidFileNotificationType validNotificationType = signature.getObject().getFATCAValidFileNotification();
			  FATCAFileErrorNotificationType fatcaFileErrorNotification = signature.getObject().getFatcaFileErrorNotification();
			  FATCANotificationHeaderGrpType notificationHeaderGrpType = null;
			  OriginalFileMetadataGrpType metadataGrpType = null;
			  OriginalFileMessageSpecGrpType messageSpecGrpType = null;
			  OriginalFileProcessingDataGrpType fileProcessingDataGrpType = null;
			  FATCARecordErrorGrpType recordErrorGrpType = null;
			  List<String> actionRequestedDueDateTxt = new ArrayList<String>();
		      List<String> actionRequestedTxt = new ArrayList<String>();
		      List<String> fieldName = new ArrayList<String>();
		      List<String> fieldErrorTxt = new ArrayList<String>();
			  if(validNotificationType!=null){
			  notificationHeaderGrpType = validNotificationType.getFATCANotificationHeaderGrp();
			  metadataGrpType = validNotificationType.getOriginalFileMetadataGrp();
			  messageSpecGrpType = validNotificationType.getOriginalFileMessageSpecGrp();
			  fileProcessingDataGrpType = validNotificationType.getOriginalFileProcessingDataGrp();
			  recordErrorGrpType = validNotificationType.getFATCARecordErrorGrp();
		      
		      fetchActionRequestedGroup(recordErrorGrpType,actionRequestedDueDateTxt,actionRequestedTxt,fieldName,fieldErrorTxt);
			  }else if(fatcaFileErrorNotification!=null){
				  notificationHeaderGrpType = fatcaFileErrorNotification.getFATCANotificationHeaderGrp();
				  metadataGrpType =  fatcaFileErrorNotification.getOriginalFileMetadataGrp();
				  if(fatcaFileErrorNotification.getActionRequestedGrp()!=null){
				  actionRequestedTxt.add(fatcaFileErrorNotification.getActionRequestedGrp().getActionRequestedTxt());
				  actionRequestedDueDateTxt.add(fatcaFileErrorNotification.getActionRequestedGrp().getActionRequestedDueDateTxt());
				  }
			  }
			  //=== Copy Data ===
			  if(metadataGrpType!=null && metadataGrpType.getSenderFileId()!=null){
		    	  System.out.println("===Ides File from ICMMNotification==="+metadataGrpType.getSenderFileId());
	    			DBCollection irsDashboardCollection = db.getCollection("IRSDashboard");
	    			DBObject irsDashboardDocument = new BasicDBObject();
	    			irsDashboardDocument.put("idesFile",metadataGrpType.getSenderFileId());
	    			DBCursor cursor = irsDashboardCollection.find(irsDashboardDocument);
	    			String idesFile=null;
	    			while(cursor.hasNext()) {
	    	        	DBObject dbObject = cursor.next();
	    	        	idesFile=dbObject.get("idesFile").toString();
	    			}
	    			if(idesFile!=null){
	    				isEmailReadAndSave = true;
	    			}
		      }
			  
			  if(isEmailReadAndSave){
				//Save data to database
			      if(notificationHeaderGrpType!=null && notificationHeaderGrpType.getFATCANotificationRefId()!=null){
			    	  document.put("notificationRefId", notificationHeaderGrpType.getFATCANotificationRefId());
			      }
			      if(metadataGrpType!=null && metadataGrpType.getIDESTransmissionId()!=null){
			    	  document.put("idesTransmissionId", metadataGrpType.getIDESTransmissionId());
			      }
			      if(notificationHeaderGrpType!=null && notificationHeaderGrpType.getFATCAEntitySenderId()!=null){
			    	  document.put("senderId", notificationHeaderGrpType.getFATCAEntitySenderId());
			      }
			      if(metadataGrpType!=null && metadataGrpType.getSenderFileId()!=null){
			    	  document.put("senderFileId", metadataGrpType.getSenderFileId());
			      }
			      if(actionRequestedTxt!=null && actionRequestedTxt.size()>0){
			    	  document.put("actionRequested", actionRequestedTxt);
			      }
			      if(actionRequestedDueDateTxt!=null && actionRequestedDueDateTxt.size()>0){
			    	  document.put("actionRequestedDueDate", actionRequestedDueDateTxt);
			      }
			      if(notificationHeaderGrpType!=null && notificationHeaderGrpType.getFATCANotificationCreateTs()!=null){
			    	  document.put("createDate", notificationHeaderGrpType.getFATCANotificationCreateTs());
			      }
			      if(metadataGrpType!=null && metadataGrpType.getIDESSendingTs()!=null){
			    	  document.put("idesSendingDate", metadataGrpType.getIDESSendingTs());
			      }
			      if(fatcaFileErrorNotification!=null && fatcaFileErrorNotification.getNotificationContentTxt()!=null){
			    	  document.put("notification", fatcaFileErrorNotification.getNotificationContentTxt());
			      }
			      if(validNotificationType!=null){
			    	  if(validNotificationType.getNotificationContentTxt()!=null){
				    	  document.put("notification", validNotificationType.getNotificationContentTxt());
				      }
			    	  if(fileProcessingDataGrpType!=null && fileProcessingDataGrpType.getFinancialInstitutionCnt()!=null){
				    	  document.put("financialInstitutionCount", fileProcessingDataGrpType.getFinancialInstitutionCnt().toString());
				      }
				      if(fileProcessingDataGrpType!=null && fileProcessingDataGrpType.getRecordCnt()!=null){
				    	  document.put("recordCount", fileProcessingDataGrpType.getRecordCnt().toString());
				      }
				      if(fileProcessingDataGrpType!=null && fileProcessingDataGrpType.getDupAccountReportRecordCnt()!=null){
				    	  document.put("dupAccountReportRecordCount", fileProcessingDataGrpType.getDupAccountReportRecordCnt().toString());
				      }
				      if(fileProcessingDataGrpType!=null && fileProcessingDataGrpType.getNonDupAccountReportRecordCnt()!=null){
				    	  document.put("nonDupAccountReportRecordCount", fileProcessingDataGrpType.getNonDupAccountReportRecordCnt().toString());
				      }
				      if(fileProcessingDataGrpType!=null && fileProcessingDataGrpType.getPooledReportRecordCnt()!=null){
				    	  document.put("pooledReportRecordCount", fileProcessingDataGrpType.getPooledReportRecordCnt().toString());
				      }
				      if(fileProcessingDataGrpType!=null && fileProcessingDataGrpType.getPooledReportRecordCnt()!=null){
				    	  document.put("financialInstitutionCnt",fileProcessingDataGrpType.getFinancialInstitutionCnt().toString());
				      }
				      if(recordErrorGrpType!=null && recordErrorGrpType.getFATCARecordErrorFIGrp()!=null && recordErrorGrpType.getFATCARecordErrorFIGrp().size()>0){
				    	  document.put("reportingFIGIIN", recordErrorGrpType.getFATCARecordErrorFIGrp().get(0).getReportingFIGIIN());
				    	  document.put("reportingFIName", recordErrorGrpType.getFATCARecordErrorFIGrp().get(0).getReportingFINm());
				    	  if(recordErrorGrpType.getFATCARecordErrorFIGrp().get(0)!=null && recordErrorGrpType.getFATCARecordErrorFIGrp().get(0).getFATCARecordErrorDetailGrp()!=null && recordErrorGrpType.getFATCARecordErrorFIGrp().get(0).getFATCARecordErrorDetailGrp().size()>0){
				    		  document.put("reportTypeCode", recordErrorGrpType.getFATCARecordErrorFIGrp().get(0).getFATCARecordErrorDetailGrp().get(0).getFATCAReportTypeCd().value());
				    		  document.put("docRefId", recordErrorGrpType.getFATCARecordErrorFIGrp().get(0).getFATCARecordErrorDetailGrp().get(0).getDocRefId());
				    		  document.put("recordLevelErrCode", recordErrorGrpType.getFATCARecordErrorFIGrp().get(0).getFATCARecordErrorDetailGrp().get(0).getRecordLevelErrorCd());
				    	  }
				      }
				      if(fieldName!=null && fieldErrorTxt!=null && fieldName.size()>0 && fieldErrorTxt.size()>0){
				    	  document.put("errorFieldName", fieldName);
				    	  document.put("fieldErrorTxt", fieldErrorTxt);
				      }
			      }
			      if(document!=null){
			      collection.save(document);
			      parsedXml=true;
			      }
			  }
			  
			  
			  //Store Notification Code in IRSDashboad
		      if(notificationHeaderGrpType!=null && notificationHeaderGrpType.getFATCANotificationCd()!=null){
		        saveNotificationCode(db,notificationHeaderGrpType.getFATCANotificationCd().value(),metadataGrpType.getSenderFileId());
		        //Send Error Notification
			       if(!notificationHeaderGrpType.getFATCANotificationCd().value().equalsIgnoreCase("NVF") && !notificationHeaderGrpType.getFATCANotificationCd().value().equalsIgnoreCase("NIM")){
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
			  	      if(validNotificationType !=null){
			  	    	  mail_body = new String(mail_body.replaceAll("%msg", validNotificationType.getNotificationContentTxt()));
			  	      }else if(fatcaFileErrorNotification!=null){
			  	    	mail_body = new String(mail_body.replaceAll("%msg", fatcaFileErrorNotification.getNotificationContentTxt()));
			  	      }
			  	      for(String actionRequest:actionRequestedTxt)
			  	      mail_body = new String(mail_body.replaceAll("%actionRequest",actionRequest));
			  	      for(String actionRequestDueDate:actionRequestedDueDateTxt)
			  	      mail_body = new String(mail_body.replaceAll("%actionDueDate", actionRequestDueDate));
			  	      mail_body = new String(mail_body.replaceAll("%timestamp", notificationHeaderGrpType.getFATCANotificationCreateTs()));
			  	      mail_body = new String(mail_body.replaceAll("%refId", notificationHeaderGrpType.getFATCANotificationRefId()));
			  	      mail_body = new String(mail_body.replaceAll("%senderId", notificationHeaderGrpType.getFATCAEntitySenderId()));
			  	      mail_body = new String(mail_body.replaceAll("%transmissionId", metadataGrpType.getIDESTransmissionId()));
			  	      mail_body = new String(mail_body.replaceAll("%sendingTimestamp", metadataGrpType.getIDESSendingTs()));
			  	      mail_body = new String(mail_body.replaceAll("%fileId", metadataGrpType.getSenderFileId()));
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
			  			 //trans.sendMessage(message,message.getAllRecipients());
			  	         System.out.println("Sent message successfully....");
			  	      }catch (MessagingException mex) {
			  	         mex.printStackTrace();
			  	      }
			       }
		      }
		      
		      System.out.println("Read Notification End");
			  //=== End ====
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
	
	//Get Action Requested Group Values
	
	public static void fetchActionRequestedGroup(FATCARecordErrorGrpType recordErrorGrpType,List<String> actionRequestedDueDateTxt,List<String> actionRequestedTxt, List<String> fieldName, List<String> fieldErrorTxt){
		List<FATCARecordErrorFIGrpType> fiRecoredErrorType = recordErrorGrpType.getFATCARecordErrorFIGrp(); 
		for(int i=0;i<fiRecoredErrorType.size();i++){
			    FATCARecordErrorFIGrpType individualfiRecordError =  fiRecoredErrorType.get(i);
				List<FATCARecordErrorDetailGrpType> errorDetailGrpTypes = individualfiRecordError.getFATCARecordErrorDetailGrp();
				for(int j=0;j<errorDetailGrpTypes.size();j++){
					FATCARecordErrorDetailGrpType errorDetailGrp  = errorDetailGrpTypes.get(j);
					actionRequestedDueDateTxt.add(errorDetailGrp.getActionRequestedGrp().getActionRequestedDueDateTxt());
					actionRequestedTxt.add(errorDetailGrp.getActionRequestedGrp().getActionRequestedTxt());
					List<FieldErrorGrpType> filedErrorGrpTypes = errorDetailGrpTypes.get(j).getFieldErrorGrp();
					if(filedErrorGrpTypes!=null){
						for(int k=0; k<filedErrorGrpTypes.size();k++){
							fieldName.add(errorDetailGrp.getFieldErrorGrp().get(k).getFieldNm());
							fieldErrorTxt.add(errorDetailGrp.getFieldErrorGrp().get(k).getFieldErrorTxt());
						}
					}
				}
			
		}
		
	}
}
