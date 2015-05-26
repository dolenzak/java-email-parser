package com.twc.fatcaone.service;
/*
	Read Email java application
	DAO 4/15/2014

	Read IRS email, parse it, and put it in MongoDB.  If the message is
	a RC021 (message delivered from the IRS), download the message and
	insert it in MongoDB.
*/

import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public final class ReadEmail {
    public static void main(String[] a) {
        System.out.println("Starting...");
        ReadEmail readEmail = new ReadEmail();
        readEmail.getEmail();
        System.out.println("Ending...");
    }

    public void getEmail() {
        
        System.out.println("Reading Email...");
		//Database Connection
        String collectionName="idesAlertMessage";
		DB db = new DataBaseConnection().dbConnection();
		DBCollection collection = db.getCollection(collectionName);
		
		//Get Mail Authentication
		DBCollection mailCollection = db.getCollection("fatcaFile");
		DBObject mailDocument = new BasicDBObject();
		mailDocument.put("fileType","mail");
		mailDocument.put("country","US");
		DBCursor cursor = mailCollection.find(mailDocument);
		String ipAddress=null,username=null,password=null,filePath=null,country=null,fileType="mail",protocol="imps";
        int port=465;
        
        while(cursor.hasNext()) {
        	DBObject dbObject = cursor.next();
        	ipAddress=dbObject.get("ipAddress").toString();
        	username=dbObject.get("username").toString();
        	password=dbObject.get("password").toString();
        	port=Integer.parseInt(dbObject.get("port").toString());
        	filePath=dbObject.get("filePath").toString();
        	fileType=dbObject.get("fileType").toString();
        	country=dbObject.get("country").toString();
        	protocol=dbObject.get("protocol").toString();
        	
        }
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", protocol);
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect(ipAddress, username, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            //Message msg = inbox.getMessage(inbox.getMessageCount());
            Message unreadMessages[] = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            System.out.println(unreadMessages.length);
            for(int i=unreadMessages.length; i>0;i--){
            	Message msg = unreadMessages[i-1];
                Multipart mp = (Multipart) msg.getContent();
                BodyPart bp = mp.getBodyPart(0);
                String bodyContent = bp.getContent().toString();
                bodyContent=bodyContent.substring(bodyContent.indexOf('\n')+1);
                Document doc = (Document) Jsoup.parse(bodyContent);
    			Element table = (Element) ((org.jsoup.nodes.Element) doc).select("table").first();
    			List<Element> listOfTD = table.select("td");
             // Save FATCA Notification Header Group data in TWC database
        		DBObject document = new BasicDBObject("_id",collection.count()+1);
        		document.put("sentDate", msg.getSentDate());
        		document.put("subject", msg.getSubject());
        		document.put("content", bodyContent);
        		if(listOfTD.get(0).text().equalsIgnoreCase("RETURNCODE")){
        		document.put("returnCode", listOfTD.get(1).text().toString());
        		if(!listOfTD.get(1).text().toString().equalsIgnoreCase("RC021") && !listOfTD.get(1).text().toString().equalsIgnoreCase("RC024"))	
        			if(listOfTD.get(8).text().equalsIgnoreCase("FATCASENDERID")){
        			updateMessageCode(db,listOfTD.get(1).text().toString(),listOfTD.get(15).text().toString());
        			}
        		}
        		if(listOfTD.get(6).text().equalsIgnoreCase("IDESTRANSID")){
        		document.put("idesTransactionId", listOfTD.get(7).text().toString());
        		}
        		if(listOfTD.get(8).text().equalsIgnoreCase("FATCASENDERID")){
        		document.put("senderId", listOfTD.get(9).text().toString());
        		}
        		if(listOfTD.get(14).text().equalsIgnoreCase("SENDERFILEID")){
        		document.put("senderFileId", listOfTD.get(15).text().toString());
        		}
        		if(listOfTD.get(16).text().equalsIgnoreCase("SENDERFILETS")){
        		document.put("senderFileTimestamp", listOfTD.get(17).text().toString());
        		}
        		if(listOfTD.get(18).text().equalsIgnoreCase("ALERTTS")){
        		document.put("alertTimestamp", listOfTD.get(19).text().toString());
        		}
        		collection.save(document);
            }
			Flags flags = new Flags();
			flags.add(Flag.DELETED);
			inbox.setFlags(unreadMessages, flags , true);
            inbox.close(true);
            store.close();
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }
    
    public static void updateMessageCode(DB db,String returnCode,String senderFileId){
    	DBCollection collection = db.getCollection("IRSDashboard");
		DBObject query = new BasicDBObject("idesFile", senderFileId);
		DBObject update = new BasicDBObject();
        update.put("$set", new BasicDBObject("messageCode",returnCode));
        collection.update(query, update);
    }
}

