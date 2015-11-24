package mil.js.swif.encryption;

import java.util.Scanner;

public class EncryptionConsole {

	public static void main(String[] args) {
		try {

	    	// ask for clear text
	    	System.out.print("Please enter your clear text: ");
	    	 
	    	// read clear text from console
	    	Scanner console = new Scanner(System.in);
	    	String clearTxt = console.nextLine();
	    	console.close();  
	    	
	    	// echo clear text console input
	        System.out.print("The clear text that you entered was: ");
	        System.out.println(clearTxt);
	        System.out.print("Encrypting the clear text value");
	        System.out.print(" .");
	        for(int i=0;i<20;i++){
	        	System.out.print(".");
	        	Thread.sleep(40);
	        }
	        System.out.println(".");
	        
	        // encrypt clear text console input
	        EncryptionUtility encrypter = new EncryptionUtility();
	        String cypherTxt= encrypter.encrypt(clearTxt);
	        
	        // echo cypher text to console
	        System.out.print("The corresponding encrypted text is: ");
	        System.out.println(cypherTxt);
	        System.out.println("Copy and paste the encrypted valve into the property file");

			
		} catch(Exception ex) {
    		System.err.println("Sorry an error occurred while performing the encryption: " + ex.getMessage());
    		System.err.println("Error Message:" + ex.getMessage());
    		System.err.println("Stack Trace:");
    		ex.printStackTrace();

		}

	}

}
