package com.twc.fatcaone.service;
/*
	Read Email java application
	DAO 4/15/2014

	Read IRS email, parse it, and put it in MongoDB.  If the message is
	a RC021 (message delivered from the IRS), download the message and
	insert it in MongoDB.
*/

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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
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
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.zoho.com", "ides@transworldcompliance.com", "welcome1");
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            //Message msg = inbox.getMessage(inbox.getMessageCount());
            Message unreadMessages[] = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            System.out.println(unreadMessages.length);
            for(int i=1; i<=unreadMessages.length;i++){
            	Message msg = inbox.getMessage(i);
                Multipart mp = (Multipart) msg.getContent();
                BodyPart bp = mp.getBodyPart(0);
                String bodyContent = bp.getContent().toString();
                bodyContent=bodyContent.substring(bodyContent.indexOf('\n')+1);
             // Save FATCA Notification Header Group data in TWC database
        		DBObject document = new BasicDBObject("_id",collection.count()+1);
        		document.put("sentDate", msg.getSentDate());
        		document.put("subject", msg.getSubject());
        		document.put("content", bodyContent);
        		collection.save(document);
            }
			Flags flags = new Flags();
			flags.add(Flag.SEEN);
			inbox.setFlags(unreadMessages, flags , true);
            inbox.close(true);
            store.close();
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }
}

