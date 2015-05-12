package com.twc.fatcaone.service;

import java.io.File;
import java.io.FilenameFilter;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class ReadFile {

	public File[] getFiles(DB db,String collectionName,String host,String countryCode,String fileType){
		DBCollection collection = db.getCollection(collectionName);
        DBObject document = new BasicDBObject();
        document.put("ipAddress", host);
        document.put("country", countryCode);
        document.put("fileType",fileType);
        DBCursor cursor = collection.find(document);
        String ipAddress=null,username=null,password=null,filePath=null,country=null;
        int port=22;
        
        while(cursor.hasNext()) {
        	DBObject dbObject = cursor.next();
        	ipAddress=dbObject.get("ipAddress").toString();
        	username=dbObject.get("username").toString();
        	password=dbObject.get("password").toString();
        	port=Integer.parseInt(dbObject.get("port").toString());
        	filePath=dbObject.get("filePath").toString();
        	fileType=dbObject.get("fileType").toString();
        	country=dbObject.get("country").toString();
        }
        File[] files = null;
        String fileFormat = null;
        if(fileType.equalsIgnoreCase("xml")){
        	fileFormat = ".xml";
        }else{
        	fileFormat=".der";
        }
        if(ipAddress.equalsIgnoreCase("localhost")){
        	File file = new File(filePath);
        	files = getAllFileUsingFileFormat(file,fileFormat);
        }else{
        		String downloadFilePath=new DownloadFile().downloadFile(ipAddress, port, username, password, filePath,fileFormat,fileType);
        		File file = new File(downloadFilePath);
            	files = getAllFileUsingFileFormat(file,fileFormat);
        }
        
        return files;
	}
        
	public File[] getAllFileUsingFileFormat(File file,final String fileFormat){
		File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File folder, String name) {
                return name.toLowerCase().endsWith(fileFormat);
            }
        });
		return files;
	}
}
