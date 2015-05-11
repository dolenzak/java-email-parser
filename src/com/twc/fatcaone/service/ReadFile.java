package com.twc.fatcaone.service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class ReadFile {

	public File[] getFiles(DB db,String collectionName,String countryCode,String fileFormat){
		DBCollection collection = db.getCollection(collectionName);
        DBObject document = new BasicDBObject();
        document.put("country", countryCode);
        document.put("fileType",fileFormat);
        DBCursor cursor = collection.find(document);
        //System.out.println("File Path: "+cursor.next().get("filePath"));
        String ipAddress=null,username=null,password=null,port=null,filePath=null,fileType=null,country=null;
        
        while(cursor.hasNext()) {
        	DBObject dbObject = cursor.next();
        	ipAddress=dbObject.get("password").toString();
        	username=dbObject.get("username").toString();
        	password=dbObject.get("password").toString();
        	port=dbObject.get("port").toString();
        	filePath=dbObject.get("filePath").toString();
        	fileType=dbObject.get("fileType").toString();
        	country=dbObject.get("country").toString();
        }
        
        File file = new File(filePath);
        File[] xmlFiles = file.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File folder, String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });
        List<File> fileList = new ArrayList<File>();
        return xmlFiles;
	}
	
	public String getKeyFile(DB db,String collectionName,String countryCode,String fileType){
		DBCollection collection = db.getCollection(collectionName);
        DBObject document = new BasicDBObject();
        document.put("country", countryCode);
        document.put("fileType",fileType);
        DBCursor cursor = collection.find(document);
        String filePath = cursor.next().get("filePath").toString();
		return filePath;
	}
}
