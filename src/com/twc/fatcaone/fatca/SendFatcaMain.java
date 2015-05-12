package com.twc.fatcaone.fatca;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.twc.fatcaone.service.DataBaseConnection;
import com.twc.fatcaone.service.FileTransfer;
import com.twc.fatcaone.service.ReadFile;


public class SendFatcaMain {
	protected static Logger logger = Logger.getLogger(SendFatcaMain.class.getName());

	
	
	private FATCAXmlSigner signer = null;
	private FATCAPackager pkger = null;

	// sender FFI or HCTA
	private String canadaGiin = "000000.00000.TA.124";
	private PrivateKey canadaSigKey = null;
	private X509Certificate canadaPubCert = null;
	
	// receiver
	private String usaGiin = "000000.00000.TA.840";
	private X509Certificate usaCert = null;
	private PrivateKey usaPrivateKey = null; 
	
	// approver - for model1 option2
	private String mexicoGiin = "000000.00000.TA.484";
	private X509Certificate mexicoPubCert = null;
	private PrivateKey mexicoPrivateKey = null; 
	
	//Newly Added Code
	
	private String reciverGIIN = "000000.00000.TA.840";
	private String senderGIIN = "C34VPZ.00000.SP.840";
	private X509Certificate cert = null;
	private PrivateKey key = null;
	//End
	
	public SendFatcaMain() throws Exception{
		signer = new FATCAXmlSigner();
		pkger = new FATCAPackager();
	}
	
	public static void main(String[] args) throws Exception {
		String collectionName="fatcaFile";
		//Database Connection
		DB db = new DataBaseConnection().dbConnection();
		
		//Get All XML Country Code 
		DBCollection collection = db.getCollection(collectionName);
		DBObject document = new BasicDBObject();
		document.put("country","FR");
		document.put("fileType","xml");
        DBCursor cursor = collection.find(document);
		
        while(cursor.hasNext()) {
        	DBObject dbObject = cursor.next();
        	String countryCode = dbObject.get("country").toString();
        	String hostName = dbObject.get("ipAddress").toString();
        	//Get the File Path From MongoDB
        	File[] xmlFiles=new ReadFile().getFiles(db,collectionName,hostName,countryCode,"xml");
        	File[] certFile=new ReadFile().getFiles(db,collectionName,hostName,countryCode,"crt");
        	File[] keyFile=new ReadFile().getFiles(db,collectionName,hostName,countryCode,"key");
        //File Processing
        for (File sendXmlFiles : xmlFiles) {
        	String signedXml = sendXmlFiles + ".signed";
        	FATCAPackager.isCanonicalization = false;
        	X509Certificate cert=loadPublicX509(certFile[0]);
    		PrivateKey key=getPemPrivateKey(keyFile[0]);
    		SendFatcaMain m = new SendFatcaMain();
    		m.signer.signStreaming(sendXmlFiles.toString(), signedXml, key, cert);
    		m.signer.signDOM(sendXmlFiles.toString(), signedXml, key, cert);
    		
    		String idesOutFile = m.pkger.createPkg(signedXml, m.senderGIIN, m.reciverGIIN, cert, 2014);
    		// Transfer File Using SFTP
            boolean fileTransfered =new FileTransfer().sftpFileTransfer(idesOutFile,db);
            
            //After Transfer the File Remove .zip file, .xml file and signed xml file
            new File(signedXml).deleteOnExit();
            new File(idesOutFile).deleteOnExit();
            if(fileTransfered){
            sendXmlFiles.deleteOnExit();
            }
		}
       }
	}
	
	//Get The Private key from der file
	public  static PrivateKey getPemPrivateKey(File keyFile) throws Exception {
		      FileInputStream fis = new FileInputStream(keyFile);
		      DataInputStream dis = new DataInputStream(fis);
		      byte[] keyBytes = new byte[(int) keyFile.length()];
		      dis.readFully(keyBytes);
		      dis.close();

		      String temp = new String(keyBytes);
		      String privKeyPEM = temp.replace("-----BEGIN RSA PRIVATE KEY-----\n", "");
		      privKeyPEM = privKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
		      
		      	dis = new DataInputStream(new FileInputStream(keyFile));
	            byte[] privKeyBytes = new byte[(int)keyFile.length()];
	            dis.read(privKeyBytes);
	            dis.close();
	            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privKeyBytes);
	            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	            RSAPrivateKey privKey = (RSAPrivateKey) keyFactory.generatePrivate(privSpec);
	            /*X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(privKeyBytes);
	            RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(pubSpec);*/
	            return privKey;
		      }
	
	//Get the Certificate from der file/public key file
	public static X509Certificate loadPublicX509(File certificateFile) 
	        throws GeneralSecurityException, FileNotFoundException {
	    InputStream is = null;
	    X509Certificate crt = null;
	    try {
	        is = new FileInputStream(certificateFile);
	        CertificateFactory cf = CertificateFactory.getInstance("X.509");
	        crt = (X509Certificate)cf.generateCertificate(is);
	    } finally {
	        closeSilent(is);
	    }
	    return crt;
	}
	public static void closeSilent(final InputStream is) {
	    if (is == null) return;
	    try { is.close(); } catch (Exception ign) {}
	}
	
	}

