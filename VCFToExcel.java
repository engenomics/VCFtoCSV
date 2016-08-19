import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;


//assumes same qScore throughout VCF segment

public class VCFToExcel {
	
	//type in input and output file paths as required here
	private String readFilePath = "C:/Adithya/PRIMES/Data/VCF Slices/ch7vcf-3.txt";
	private String outputFilePath = "C:/Adithya/PRIMES/Data/CSVs/Original/ch7vcf-3Gaps.txt";
	private BufferedWriter writer;
	
	
	private String outputFilePathInsAndDel = "C:/Adithya/PRIMES/Data/CSVs/Original/ch7vcf-3ID.txt";
	private BufferedWriter writerInsAndDel;
	
	
	public static void main(String [] args) {
		VCFToExcel test = new VCFToExcel();
		test.convertGapToExcel();
	}
	
	

	//Reads input VCF and produces CSV of all the gaps
	public void convertGapToExcel() {
	
		try {
			//open writer, initialize reader
			openWriter();
			BufferedReader reader = new BufferedReader(new FileReader(readFilePath));
		
			//initialize current position to 0
			int currentPos = 0;
	
			//loop through all the lines in the input VCF
			while(true) {
				String outputLine = "";
				String inputLine = reader.readLine();
				
			
				if(inputLine == null) break; //once current line is null, we know end of file has been reached
				
				//delimit the current line based on tabs
				String [] lineArray = inputLine.split("\t");
				
				//skip all comment lines at the top of the file
				while(inputLine.contains("#") || inputLine.contains("Var")) {
					System.out.println(inputLine);
					inputLine = reader.readLine();
				}
					
				//figure out current "gap" by subtracting position of previous variant from the position of current variant
				outputLine += (Integer.parseInt(lineArray[1]) - currentPos) + ",";
				currentPos = Integer.parseInt(lineArray[1]); //update current position
				
				writeToFile(outputLine.substring(0, outputLine.length() - 1)); //write the gap to CSV
	
			}
			closeWriter(); //close writer
		}
		
		//catch any I/O issues, print "Failure"
		catch(Exception e) {
			System.out.println("Failure");
		}
	}
	
	
	
	//Reads input CSV and produces CSV of all SNP variants
	public void convertToExcel() {
		
		ArrayList<String> insertionsAndDeletions = new ArrayList<String>(); //Initialize ArrayList of all nonSNPs
		
		try {
			//open writer, initialize reader
			openWriter();
			BufferedReader reader = new BufferedReader(new FileReader(readFilePath));
			
			boolean chromNumWritten = false; //only write chrom # once
			while(true) {
				String outputLine = "";
				String currentLine = reader.readLine();
				if(currentLine == null) break; //if current line is null end of file has been reached
				
				//skip all commented lines
				while(currentLine.contains("#") || currentLine.contains("Var"))
					currentLine = reader.readLine();
				
				
				//Delimit current line by tabs
				String [] splitString = currentLine.split("\t");
				String chromNum = splitString[0]; //chromosome number is the first entry
				String qScore = splitString[4]; //quality scores are the fourth entry
			
				if(!chromNumWritten) {
					writeToFile(chromNum + "," + qScore); //only write to file the first time
					chromNumWritten = true;
					
				}
				
				//translate the reference base to corresponding digit
				if(splitString[2].equals("A")) outputLine += "1,";
				else if(splitString[2].equals("C")) outputLine += "2,";
				else if(splitString[2].equals("T")) outputLine += "3,";
				else if(splitString[2].equals("G")) outputLine += "4,";
				
				//if not only 1 base, write 0 to file and store in the non-SNP array list
				else {
					outputLine += "0,";
					insertionsAndDeletions.add(splitString[2]);
				}
				
				
				//translate the alternate base to corresponding digit
				if(splitString[3].equals("A")) outputLine += "1,";
				else if(splitString[3].equals("C")) outputLine += "2,";
				else if(splitString[3].equals("T")) outputLine += "3,";
				else if(splitString[3].equals("G")) outputLine += "4,";
				
				//if not only 1 base, write 0 to file and store in the non-SNP array list
				else {
					outputLine += "0,";
					insertionsAndDeletions.add(splitString[3]);
				}
				
				
				writeToFile(outputLine.substring(0, outputLine.length() - 1)); //write line to CSV
			}
			closeWriter();
		}
		
		//if failure, print stack trace of exception along with "Failure"
		catch(Exception e) {
			System.out.println(e.toString());
			System.out.println("failure in method");
		}
		
		//convert array list to array and write all the non-SNPs to separate CSV using helper method
		String [] insertionsAndDeletionsArray = new String[insertionsAndDeletions.size()];
		for(int i = 0; i< insertionsAndDeletionsArray.length; i ++) insertionsAndDeletionsArray[i] = insertionsAndDeletions.get(i);
		
		writeInsertionsAndDeletionsToExcel(insertionsAndDeletionsArray); //call helper method to write non-SNPs to separate CSVs
	}

	
	//opens writer
	private void openWriter() {
		
		try {
		    writer = new BufferedWriter(new FileWriter(outputFilePath));
		}
		
		catch(Exception e) {
			   System.out.println("unsuccessful open");
		}
	}
	
	//closes writer
	private void closeWriter() {
		
		try {
			writer.close();
		}
		
		catch(Exception e) {
			System.out.println("unsucessful close");
		}
	}
	
	//writes line to file
	private void writeToFile(String ln) {
		
		try {
		    writer.write(ln);
		    writer.newLine();    
		} 
		
		catch(Exception e) {
			System.out.println("Failure: \t" + ln);
		    System.out.println("unsuccessful write");
		}
	}
	
	
	
	//takes array of non-SNPs as input and writes them all to a separate CSV
	private void writeInsertionsAndDeletionsToExcel(String [] insAndDel) {
		
		
		openWriterID(); //open writer
	
		//loop through all the non-SNPs in the array
		for(String s : insAndDel) {
			
			//go through each character in the string
			for(int i = 0; i < s.length(); i ++) {
				char c = s.charAt(i);
				
				//translate the bases to their corresponding digits and write them to CSV
				if(c == 'A') writeToFileID("1");
				else if(c == 'C') writeToFileID("2");
				else if(c == 'T') writeToFileID("3");
				else if(c == 'G') writeToFileID("4");
				else if(c == ',') writeToFileID("5"); //comma, which represents alternate alleles, is a 5
			}
					
			writeToFileID("0"); //write a 0 at the end of each non-SNP so when decoding, separate non-SNPs can be distinguished
		}
			

		closeWriterID(); //close writer
	}
	
	
	//opens writer
	private void openWriterID() {
		
		try {
		    writerInsAndDel = new BufferedWriter(new FileWriter(outputFilePathInsAndDel));
		}
		
		catch(Exception e) {
			   System.out.println("unsuccessful open");
		}
	}
	
	//closes writer
	private void closeWriterID() {
		
		try {
			writerInsAndDel.close();
		}
		
		catch(Exception e) {
			System.out.println("unsucessful close");
		}
	}
	
	//writes line to file
	private void writeToFileID(String ln) {
		
		try {
		    writerInsAndDel.write(ln);
		    writerInsAndDel.newLine();    
		} 
		
		catch(Exception e) {
		    System.out.println("unsuccessful write");
		}
	}
	
}
