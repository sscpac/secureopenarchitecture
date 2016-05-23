package mil.js.soaf.encryption;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import java.net.NetworkInterface;
import java.util.Enumeration;

@Component
public class EncryptionUtility {

    private final static String defaultMac = "AA-0C-29-F5-03-FF";
    private final static String salt = "FF0744940b5c36CC";
    private TextEncryptor textEncryptor;
    
    public EncryptionUtility() {
    	String macAddress = getMacAddress();
    	textEncryptor = Encryptors.text(macAddress, salt);
    }
    
	public String decrypt(String cipherTxt)  {
		return textEncryptor.decrypt(cipherTxt);
	}

	public String encrypt(String clearTxt)  {
	    return textEncryptor.encrypt(clearTxt);
	}
	
	private String getMacAddress() {

		try {
			for (Enumeration<NetworkInterface> enm = NetworkInterface.getNetworkInterfaces(); enm.hasMoreElements();) {
				NetworkInterface network = (NetworkInterface) enm.nextElement();
				if (null != network.getHardwareAddress()) {
					byte mac[] = network.getHardwareAddress();
					return macToString(mac);
				}
			}

			return defaultMac;

		} catch (Exception ex) {
			return defaultMac;
		}
	}
	
	private String macToString(byte[] mac) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			result.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
		}
		return result.toString();	
	}	

}

