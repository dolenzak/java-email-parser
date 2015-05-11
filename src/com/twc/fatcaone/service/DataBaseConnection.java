package com.twc.fatcaone.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class DataBaseConnection {
	
	public DB dbConnection(){
		DB db = null;
		try {
		 //loading properites from properties file
    	FileInputStream fis = new FileInputStream(System.getProperty("user.dir")+"/src/resources/database.properties");
		Properties properties = new Properties();
		properties.load(fis);
		MongoClientURI uri  = new MongoClientURI(properties.getProperty("mongo.url")); 
        MongoClient client = new MongoClient(uri);
        db = client.getDB(uri.getDatabase());
        String password = properties.getProperty("mongo.password");
        char dbPassword[] ; 
        if(password!=null && !password.isEmpty()){
        	dbPassword = password.toCharArray();
        	db.authenticate(properties.getProperty("mongo.username"),dbPassword);
        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return db;
	}
	

}
