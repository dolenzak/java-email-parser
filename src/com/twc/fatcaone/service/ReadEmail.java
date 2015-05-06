package com.twc.fatcaone.service;
/*
	Read Email java application
	DAO 4/15/2014

	Read IRS email, parse it, and put it in MongoDB.  If the message is
	a RC021 (message delivered from the IRS), download the message and
	insert it in MongoDB.
*/

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

public final class ReadEmail {
    public static void main(String[] a) {
        System.out.println("Starting...");
        ReadEmail readEmail = new ReadEmail();
        readEmail.getEmail();
        System.out.println("Ending...");
    }

    public void getEmail() {
        
        System.out.println("Reading Email...");

        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.zoho.com", "ides@transworldcompliance.com", "welcome1");
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message msg = inbox.getMessage(inbox.getMessageCount());
            Address[] in = msg.getFrom();
            for (Address address : in) {
                System.out.println("FROM:" + address.toString());
            }
            Multipart mp = (Multipart) msg.getContent();
            BodyPart bp = mp.getBodyPart(0);
            System.out.println("SENT DATE:" + msg.getSentDate());
            System.out.println("SUBJECT:" + msg.getSubject());
            System.out.println("CONTENT:" + bp.getContent());
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }
}

