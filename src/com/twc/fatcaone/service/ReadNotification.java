package com.twc.fatcaone.service;

import java.io.File;
import java.net.UnknownHostException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.twc.fatcaone.notification.FATCAFileErrorNotificationType;
import com.twc.fatcaone.notification.FATCANotificationHeaderGrpType;
import com.twc.fatcaone.notification.OriginalFileMetadataGrpType;



public class ReadNotification {
	
	public static void main(String a[]){
	try {
	JAXBContext jc = JAXBContext.newInstance(FATCAFileErrorNotificationType.class);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    File xml = new File("/home/hacker/Music/FileErrorNotificationSample.xml");
    FATCAFileErrorNotificationType fatcaFileErrorNotificationType = (FATCAFileErrorNotificationType) unmarshaller.unmarshal(xml);
	System.out.println(fatcaFileErrorNotificationType.getFATCANotificationHeaderGrp().getFATCANotificationRefId());
	System.out.println(fatcaFileErrorNotificationType.getFATCANotificationHeaderGrp().getFATCANotificationCd());
	System.out.println(fatcaFileErrorNotificationType.getFATCANotificationHeaderGrp().getFATCAEntitySenderId());
	System.out.println(fatcaFileErrorNotificationType.getFATCANotificationHeaderGrp().getFATCAEntityReceiverId());
	try {
		/*MongoClient mongo = new MongoClient("localhost", 27017);
		DB db = mongo.getDB("twcdb");
		DBCollection col = db.getCollection("message");*/
		//MongoClientURI uri  = new MongoClientURI("mongodb://user:pass@host:port/db"); 
		MongoClientURI uri  = new MongoClientURI("mongodb://localhost:27017/twcdb"); 
        MongoClient client = new MongoClient(uri);
        DB db = client.getDB(uri.getDatabase());
        DBCollection collection = null;
        DBObject document = null;
        
        // Save FATCA Notification Header Group data in TWC database
        collection = db.getCollection("fatcanotificationheader");
        document = new BasicDBObject();
        FATCANotificationHeaderGrpType fatcaNotificationHeaderGrpType = fatcaFileErrorNotificationType.getFATCANotificationHeaderGrp();
        document.put("refId",fatcaNotificationHeaderGrpType.getFATCANotificationRefId());
        document.put("senderId", fatcaNotificationHeaderGrpType.getFATCAEntitySenderId());
        document.put("receiverId", fatcaNotificationHeaderGrpType.getFATCAEntityReceiverId());
        document.put("notificationCode",fatcaNotificationHeaderGrpType.getFATCANotificationCd().toString());
        document.put("copiedToFATCAEntityId", fatcaNotificationHeaderGrpType.getCopiedToFATCAEntityId());
        document.put("contactInformationTxt", fatcaNotificationHeaderGrpType.getContactInformationTxt());
        document.put("createTime", fatcaNotificationHeaderGrpType.getFATCANotificationCreateTs());
        collection.save(document);
        
        //Save original File Metadata Group data in TWC database
        collection = db.getCollection("fatcanotificationheader");
        document = new BasicDBObject();
        OriginalFileMetadataGrpType originalFileMetadataGrpType =  fatcaFileErrorNotificationType.getOriginalFileMetadataGrp();
        document.put("idesTransmissionId", originalFileMetadataGrpType.getIDESTransmissionId());
        document.put("idesSendingTime", originalFileMetadataGrpType.getIDESSendingTs());
        document.put("originalIDESTransmissionId", originalFileMetadataGrpType.getOriginalIDESTransmissionId());
        document.put("senderFileId", originalFileMetadataGrpType.getSenderFileId());
        document.put("uncompressedFileSizeKBQty", originalFileMetadataGrpType.getUncompressedFileSizeKBQty());
        collection.save(document);

        
        
		client.close();
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	} catch (JAXBException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
}
