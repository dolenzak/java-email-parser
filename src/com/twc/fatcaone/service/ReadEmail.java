package com.twc.fatcaone.service;
/*
	Read Email java application
	DAO 4/15/2014

	Read IRS email, parse it, and put it in MongoDB.  If the message is
	a RC021 (message delivered from the IRS), download the message and
	insert it in MongoDB.
*/

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import com.jcraft.jsch.*;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import sun.misc.BASE64Decoder;

public final class ReadEmail {

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
		mailDocument.put("protocol","imaps");
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
    			
    			//Find Which Server Mail (US or Cayman)
    			boolean isReadAndSaveEmail=false;
    			isReadAndSaveEmail = findEmailServer(db,mailCollection,listOfTD,isReadAndSaveEmail);  
    			if(isReadAndSaveEmail){
             // Save FATCA Notification Header Group data in TWC database
        		DBObject document = new BasicDBObject("_id",collection.count()+1);
        		document.put("sentDate", msg.getSentDate());
        		document.put("subject", msg.getSubject());
        		document.put("content", bodyContent);
        		if(listOfTD.get(0).text().equalsIgnoreCase("RETURNCODE")){
        		document.put("returnCode", listOfTD.get(1).text().toString());
        		if(!listOfTD.get(1).text().toString().equalsIgnoreCase("RC021") && !listOfTD.get(1).text().toString().equalsIgnoreCase("RC024"))	
        			if(listOfTD.get(10).text().equalsIgnoreCase("FATCASENDERID")){
        			updateMessageCode(db,listOfTD.get(1).text().toString(),listOfTD.get(17).text().toString());
        			}
        		}
        		
        		if(listOfTD.get(8).text().equalsIgnoreCase("IDESTRANSID")){
        		document.put("idesTransactionId", listOfTD.get(9).text().toString());
        		}
        		if(listOfTD.get(10).text().equalsIgnoreCase("FATCASENDERID")){
        		document.put("senderId", listOfTD.get(11).text().toString());
        		}
        		if(listOfTD.get(16).text().equalsIgnoreCase("SENDERFILEID")){
        		document.put("senderFileId", listOfTD.get(17).text().toString());
        		}
        		if(listOfTD.get(18).text().equalsIgnoreCase("SENDERFILETS")){
        		document.put("senderFileTimestamp", listOfTD.get(19).text().toString());
        		}
        		if(listOfTD.get(20).text().equalsIgnoreCase("ALERTTS")){
        		document.put("alertTimestamp", listOfTD.get(21).text().toString());
        		}
        		collection.save(document);
            }
            }
			Flags flags = new Flags();
			flags.add(Flag.DELETED);
			//flags.add(Flag.SEEN);
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
    
    public boolean findEmailServer(DB db,DBCollection mailCollection,List<Element> listOfTD,boolean isReadAndSaveEmail) throws JSchException, SftpException, IOException {
    	if(listOfTD.get(1).text().toString().equalsIgnoreCase("RC001")){
    		String idesFile=null;
    		if(listOfTD.get(16).text().equalsIgnoreCase("SENDERFILEID")){
    			System.out.println("===Ides File==="+listOfTD.get(17).text().toString());
    			DBCollection irsDashboardCollection = db.getCollection("IRSDashboard");
    			DBObject irsDashboardDocument = new BasicDBObject();
    			irsDashboardDocument.put("idesFile",listOfTD.get(17).text().toString());
    			DBCursor cursor = irsDashboardCollection.find(irsDashboardDocument);
    			while(cursor.hasNext()) {
    	        	DBObject dbObject = cursor.next();
    	        	idesFile=dbObject.get("idesFile").toString();
    			}
    			if(idesFile!=null){
    				return true;
    			}
        		}
    	}
    	//IF Message Code(Reference Code) is "RC021" after that get the appropriate downloaded zip file from ICMM Server
    	else if(listOfTD.get(1).text().toString().equalsIgnoreCase("RC021")){
			try {
				String idesTransactionId = listOfTD.get(9).text().toString();

				translationFromSHtoJAVA(idesTransactionId);

				ReadNotification notification = new ReadNotification();
				return notification.getNotification(idesTransactionId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(listOfTD.get(1).text().toString().equalsIgnoreCase("RC024")){
			if(listOfTD.get(8).text().equalsIgnoreCase("IDESTRANSID")){
				System.out.println("===Ides Transaction Id==="+listOfTD.get(9).text().toString());
    			DBCollection idesAlertMessageCollection = db.getCollection("idesAlertMessage");
    			DBObject idesAlertMessageDocument = new BasicDBObject();
    			idesAlertMessageDocument.put("idesTransactionId",listOfTD.get(9).text().toString());
    			DBCursor cursor = idesAlertMessageCollection.find(idesAlertMessageDocument);
    			String idesTransactionId = null;
    			while(cursor.hasNext()) {
    	        	DBObject dbObject = cursor.next();
    	        	idesTransactionId=dbObject.get("idesTransactionId").toString();
    			}
    			if(idesTransactionId!=null){
    				return true;
    			}
        		}
		}
    	return isReadAndSaveEmail;
    }

	public void translationFromSHtoJAVA(String idesTransactionId) throws Exception {
		System.out.println("Start translationFromSHtoJAVA");

		try {
			String irsFilePath = getIRSFilePath();

			File uniqueFolder = new File(irsFilePath + File.separatorChar + idesTransactionId);
			System.out.println("Creating unique folder at: " + irsFilePath + File.separatorChar + idesTransactionId);
			boolean wasCreated = uniqueFolder.mkdir();
			System.out.println("Was the folder created? " + wasCreated);

			if (wasCreated) {
				File downloadedFile = downloadFileFromSftp(uniqueFolder);
				System.out.println("downloadFileFromSftp - step 1/6 done!");
				unZipIt(downloadedFile);
				System.out.println("unZipIt - step 2/6 done!");
				Map<String, byte[]> map = decryptRSAKeyFile(downloadedFile);
				System.out.println("decryptRSAKeyFile - step 3/6 done!");
				File messageZip = decryptFile(map.get("key32"), map.get("keyIV"), downloadedFile);
				System.out.println("decryptFile - step 4/6 done!");
				unZipIt(messageZip);
				System.out.println("unZipIt - step 5/6 done!");
				doSomeThings(uniqueFolder);
				System.out.println("doSomeThings - step 6/6 done!");
			}
			else {
				System.out.println("Stop process - unique folder could not been created");
			}
		}
		catch (Exception e) {
			System.out.println("Something went wrong in this method: translationFromSHtoJAVA()");
			System.out.println(e.getMessage());
			throw e;
		}

		System.out.println("End translationFromSHtoJAVA");
	}

	private static String getIRSFilePath() {
		DB db = new DataBaseConnection().dbConnection();
		DBCollection collection = db.getCollection("fatcaFile");
		DBObject document = new BasicDBObject();
		document.put("ipAddress", "localhost");
		document.put("country", "US");
		document.put("fileType", "zip");
		DBCursor cursor = collection.find(document);

		while(cursor.hasNext()) {
			DBObject dbObject = cursor.next();
			return dbObject.get("filePath").toString();
		}

		return null;
	}

	private File downloadFileFromSftp(File uniqueFolder) throws JSchException, SftpException, IOException {
		String ipAddress=null,username=null,password=null,filePath=null;
		int port = 4022;

		DB db = new DataBaseConnection().dbConnection();
		DBCollection collection = db.getCollection("fatcaFile");
		DBObject document = new BasicDBObject();
		document.put("fileType","sh");
		document.put("country","US");
		document.put("protocol","sftp");
		DBCursor sftpCursor = collection.find(document);

		while(sftpCursor.hasNext()) {
			DBObject dbObject = sftpCursor.next();
			ipAddress=dbObject.get("ipAddress").toString();
			username=dbObject.get("username").toString();
			password=dbObject.get("password").toString();
			port=Integer.parseInt(dbObject.get("port").toString());
			filePath=dbObject.get("filePath").toString();
		}

		JSch jsch = new JSch();
		com.jcraft.jsch.Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;
		File newFile = null;

		try {
			session = jsch.getSession(username, ipAddress, port);
			session.setPassword(password);

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			System.out.println("Establishing Connection...");
			session.connect();
			System.out.println("Connection established.");

			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp)channel;
			channelSftp.cd(filePath);
			channelSftp.lcd(uniqueFolder.getAbsolutePath());

			/*Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*.zip");
			for(ChannelSftp.LsEntry entry : list) {
				System.out.println(entry.getFilename());
			}*/

			byte[] buffer = new byte[1024];
			BufferedInputStream bis = new BufferedInputStream(channelSftp.get(uniqueFolder.getName() + ".zip"));
			newFile = new File(uniqueFolder.getAbsolutePath() + File.separatorChar + uniqueFolder.getName() + ".zip");
			OutputStream os = new FileOutputStream(newFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			int readCount;
			while( (readCount = bis.read(buffer)) > 0) {
				bos.write(buffer, 0, readCount);
			}
			bis.close();
			bos.close();

			System.out.println("File:" + newFile.getName() + ", has been downloaded.");
		} catch (Exception e) {
			System.out.println("Exception found while downloaded the .zip from sftp.");
			throw e;
		}
		finally {
			if (channel != null) {
				channelSftp.exit();
				System.out.println("sftp Channel exited.");
				channel.disconnect();
				System.out.println("Channel disconnected.");
				session.disconnect();
				System.out.println("Host Session disconnected.");
			} else {
				System.out.println("Host Could Not connected");
			}
		}

		return newFile;
	}

	public void unZipIt(File downloadedFile) throws IOException {
		byte[] buffer = new byte[1024];

		try {
			//get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(downloadedFile));
			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while(ze!=null){
				String fileName = ze.getName();
				File newFile = new File(downloadedFile.getParent() + File.separatorChar + fileName);
				System.out.println("file unzip : "+ newFile.getAbsoluteFile());

				//create all non exists folders
				//else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();
		} catch (IOException ex) {
			throw ex;
		}
	}

	private Map<String, byte[]> decryptRSAKeyFile(File downloadedFile) throws Exception {
		System.out.println("Start decrypting RSA Key");

		Map<String, byte[]> map = new HashMap<String, byte[]>();
		PrivateKey pk = readPrivateKey(new File("." + File.separatorChar + "wildcard_fatcaone_com.pem"));

		byte[] key = decryptRSAKey(pk, Files.readAllBytes(Paths.get(downloadedFile.getParent() + File.separatorChar + "F7LFXP.00011.ME.840_Key")));
		map.put("key32", Arrays.copyOfRange(key ,0 , 32));
		map.put("keyIV", Arrays.copyOfRange(key ,32 , 48));

		System.out.print("key32:");
		System.out.println(Base64.encode(map.get("key32")));
		System.out.println("key32 length:" + map.get("key32").length);

		System.out.print("keyIV:");
		System.out.println(Base64.encode(map.get("keyIV")));
		System.out.println("keyIV length:" + map.get("keyIV").length);

		System.out.println("End decrypting RSA Key");

		return map;
	}

	public PrivateKey readPrivateKey(File keyFile) throws Exception {
		// read key bytes
		FileInputStream in = new FileInputStream(keyFile);
		byte[] keyBytes = new byte[in.available()];
		in.read(keyBytes);
		in.close();

		String privateKey = new String(keyBytes, "UTF-8");
		privateKey = privateKey.replaceAll("(-+BEGIN PRIVATE KEY-+\\r?\\n|-+END PRIVATE KEY-+\\r?\\n?)", "");

		// don't use this for real projects!
		BASE64Decoder decoder = new BASE64Decoder();
		keyBytes = decoder.decodeBuffer(privateKey);

		// generate private key
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(spec);
	}

	private static byte[] decryptRSAKey(Key decryptionKey, byte[] buffer) {
		try {
			Cipher rsa;
			rsa = Cipher.getInstance("RSA");
			rsa.init(Cipher.DECRYPT_MODE, decryptionKey);

			return rsa.doFinal(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public File decryptFile(byte[] key, byte[] initVector, File downloadedFile) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
		try {
			System.out.println("Start decrypting Payload file");

			String encrypted = downloadedFile.getParent() + File.separatorChar + "000000.00000.TA.840_Payload";
			String output = downloadedFile.getParent() + File.separatorChar + "message.zip";

			IvParameterSpec iv = new IvParameterSpec(initVector);
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			com.sun.org.apache.xml.internal.security.Init.init();

			byte[] encryptedBytes = Files.readAllBytes(Paths.get(encrypted));

			byte[] original = cipher.doFinal(encryptedBytes);

			FileOutputStream outputStream = new FileOutputStream(output);
			outputStream.write(original);
			outputStream.close();

			System.out.println("End decrypting Payload file");

			return new File(output);
		} catch (Exception ex) {
			throw ex;
		}
	}

	private void doSomeThings(File uniqueFolder) throws IOException {
		File oldFile = new File(uniqueFolder.getAbsolutePath() + File.separatorChar + "000000.00000.TA.840_Payload.xml");
		File newFile = new File(uniqueFolder.getAbsolutePath(), "XMLPayload_" + uniqueFolder.getName() + ".xml");
		Files.move(oldFile.toPath(), newFile.toPath());

		appendToAFile("." + File.separatorChar + "filenames.txt", "Filename: " + uniqueFolder.getName());

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-hh-mm-ss");
		String date = simpleDateFormat.format(new Date());
		appendToAFile("." + File.separatorChar + "irsfilenames.txt", "Date: " + date);
		//Date: 06-23-03-21-05

		Files.copy(Paths.get(newFile.getAbsolutePath()), Paths.get("." + File.separatorChar + "results" + File.separatorChar + date + "_" + uniqueFolder.getName() + "-Payload.xml"));

		for (String file : uniqueFolder.list()) {
			if (file.endsWith("_Payload") || file.endsWith("_Key") || file.contains("_Metadata")) {
				File deleteFile = new File(uniqueFolder.getAbsolutePath() + File.separatorChar + file);
				if (deleteFile.exists()) {
					deleteFile.delete();
				}
			}
		}
	}

	private void appendToAFile(String filePath, String text) throws IOException {
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(filePath, true));
			bw.write(text);
			bw.newLine();
			bw.flush();
		}
		finally {
			if (bw != null) {
				try {
					bw.close();
				}
				finally {}
			}
		}
	}
}

