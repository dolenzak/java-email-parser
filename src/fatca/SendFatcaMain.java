package fatca;

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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


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
		/*canadaSigKey = UtilShared.getPrivateKey("jks", System.getProperty("user.dir")+"/Keystore/Canada_PrepTool/KSprivateCA.jks", "pwd123", "CAN2014", "CANADAcert");
		canadaPubCert = UtilShared.getCert("jks", System.getProperty("user.dir")+"/Keystore/Canada_PrepTool/KSpublicCA.jks", "pwd123", "CANADAcert");
		usaCert = UtilShared.getCert("jks", System.getProperty("user.dir")+"/Keystore/IRS_PrepTool/KSpublicUS.jks", "pwd123", "IRScert");
		mexicoPubCert = UtilShared.getCert("jks", System.getProperty("user.dir")+"/Keystore/Mexico_PrepTool/KSpublicMX.jks", "pwd123", "MEXICOcert");
		usaPrivateKey = UtilShared.getPrivateKey("jks", System.getProperty("user.dir")+"/Keystore/IRS_PrepTool/KSprivateUS.jks", "pwd123", "password", "IRScert");
		mexicoPrivateKey = UtilShared.getPrivateKey("jks", System.getProperty("user.dir")+"/Keystore/Mexico_PrepTool/KSprivateMX.jks", "pwd123", "MEX2014", "MEXICOcert");
		*/
		//key =UtilShared.getPrivateKey("jks", System.getProperty("user.dir")+"/Keystore/newcert/star_fatcaone_com.jks", "PQve34%10", "PQve34%10", "server");
		//cert = UtilShared.getCert("jks", System.getProperty("user.dir")+"/Keystore/newcert/star_fatcaone_com.jks", "PQve34%10", "server");
		//key =getPemPrivateKey("/home/hacker/EDI/TWC/fatcaone_private_key.der","RSA");
		//cert = 
	}
	
	public static void main(String[] args) throws Exception {
		logger.debug("test");
		System.out.println(System.getProperty("user.dir"));
		String xml = System.getProperty("user.dir")+"/individual1428648739278ns2.xml";
		String signedXml = xml + ".signed";
		
		FATCAPackager.isCanonicalization = false;
		X509Certificate cert=loadPublicX509(System.getProperty("user.dir")+"/Keystore/newcert/irspublickey.der");
		PrivateKey key=getPemPrivateKey(System.getProperty("user.dir")+"/Keystore/serverfile/fatcaone_private_key.der");
		SendFatcaMain m = new SendFatcaMain();
		m.signer.signStreaming(xml, signedXml, key, cert);
		m.signer.signDOM(xml, signedXml, key, cert);
		
		String idesOutFile = m.pkger.createPkg(signedXml, m.senderGIIN, m.reciverGIIN, cert, 2014);
		logger.debug(idesOutFile);

		//m.pkger.unpack(idesOutFile, key);
		
		/*idesOutFile = m.pkger.createPkgWithApprover(signedXml, m.senderGIIN, m.usaGiin, m.cert, m.reciverGIIN, m.cert, 2014);
		logger.debug(idesOutFile);

		m.pkger.unpackForApprover(idesOutFile, m.key);
		
		idesOutFile = m.pkger.signAndCreatePkg(xml, m.key, m.cert, m.reciverGIIN, m.usaGiin, m.cert, 2014);
		logger.debug(idesOutFile);

		m.pkger.unpack(idesOutFile, m.key);
		
		idesOutFile = m.pkger.signAndCreatePkgWithApprover(xml, m.key, m.cert, m.senderGIIN, m.usaGiin, m.cert, m.reciverGIIN, m.cert, 2014);
		logger.debug(idesOutFile);
	
		m.pkger.unpackForApprover(idesOutFile, m.key);*/
		
		
		//String idesOutFile = m.pkger.signAndCreatePkgWithApprover(canadaXml, m.usaPrivateKey, m.usaCert, m.senderGIIN, m.reciverGIIN, m.usaCert, m.reciverGIIN, m.usaCert, 2015);
		System.out.println("============ Test =========="+idesOutFile);
		
		// Transfer File Using SFTP
			String SFTPHOST = "54.172.126.52";
	        int SFTPPORT = 4022;
	        String SFTPUSER = "dolenzak";
	        String SFTPPASS = "TWCfatca1!";
	        String SFTPWORKINGDIR = "/Outbox/840";

	        Session session = null;
	        Channel channel = null;
	        ChannelSftp channelSftp = null;
	        System.out.println("preparing the host information for sftp.");
	        try {
	            JSch jsch = new JSch();
	            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
	            session.setPassword(SFTPPASS);
	            java.util.Properties config = new java.util.Properties();
	            config.put("StrictHostKeyChecking", "no");
	            session.setConfig(config);
	            session.connect();
	            System.out.println("Host connected.");
	            channel = session.openChannel("sftp");
	            channel.connect();
	            System.out.println("sftp channel opened and connected.");
	            channelSftp = (ChannelSftp) channel;
	            channelSftp.cd(SFTPWORKINGDIR);
	            File f = new File(System.getProperty("user.dir")+"/"+idesOutFile);
	            channelSftp.put(new FileInputStream(f), f.getName());
	           System.out.println("File transfered successfully to host.");
	        } catch (Exception ex) {
	             System.out.println("Exception found while tranfer the response.");
	        }
	        finally{

	            channelSftp.exit();
	            System.out.println("sftp Channel exited.");
	            channel.disconnect();
	            System.out.println("Channel disconnected.");
	            session.disconnect();
	            System.out.println("Host Session disconnected.");
	        }
	}
	
	/*public static PrivateKey loadPrivateKey(String fileName) 
	        throws IOException, GeneralSecurityException {
	    PrivateKey key = null;
	    InputStream is = null;
	    try {
	        //is = fileName.getClass().getResourceAsStream("/" + fileName);
	    	is = new FileInputStream(fileName);
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
	        StringBuilder builder = new StringBuilder();
	        boolean inKey = true;
 	        for (String line = br.readLine(); line != null; line = br.readLine()) {
	            if (!inKey) {
	                if (line.startsWith("-----BEGIN ")) {
	                    inKey = true;
	                }
	                continue;
	            }
	            else {
	                if (line.startsWith("-----END ")) {
	                    inKey = false;
	                    break;
	                }
	                builder.append(line);
	            }
	        }
	        //
	        byte[] encoded = DatatypeConverter.parseBase64Binary(builder.toString());
	        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        key = kf.generatePrivate(keySpec);
	    } finally {
	        closeSilent(is);
	    }
	    return key;
	}
	public static void closeSilent(final InputStream is) {
	    if (is == null) return;
	    try { is.close(); } catch (Exception ign) {}
	}*/
	
	   public  static PrivateKey getPemPrivateKey(String filename) throws Exception {
		      File f = new File(filename);
		      FileInputStream fis = new FileInputStream(f);
		      DataInputStream dis = new DataInputStream(fis);
		      byte[] keyBytes = new byte[(int) f.length()];
		      dis.readFully(keyBytes);
		      dis.close();

		      String temp = new String(keyBytes);
		      String privKeyPEM = temp.replace("-----BEGIN RSA PRIVATE KEY-----\n", "");
		      privKeyPEM = privKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
		      
		      	dis = new DataInputStream(new FileInputStream(f));
	            byte[] privKeyBytes = new byte[(int)f.length()];
	            dis.read(privKeyBytes);
	            dis.close();
	            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privKeyBytes);
	            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	            RSAPrivateKey privKey = (RSAPrivateKey) keyFactory.generatePrivate(privSpec);
	            /*X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(privKeyBytes);
	            RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(pubSpec);*/
	            return privKey;
		      }
	public static X509Certificate loadPublicX509(String fileName) 
	        throws GeneralSecurityException, FileNotFoundException {
	    InputStream is = null;
	    X509Certificate crt = null;
	    try {
	    	File f = new File(fileName);
	        is = new FileInputStream(f);
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

