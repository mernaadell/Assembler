package assemblerPKJ;

import static assemblerPKJ.FirstPass.*;
import static assemblerPKJ.SecondPass.WriteSicFile2;

import java.io.File;
import java.io.PrintWriter;

public class Driver {
	private static final String FILENAME = "SIC.txt";
	private static final String FILENAME1 = "Formatted!.txt";
	// private static final String FILENAME2 = "pre_pass2.txt";

	public static void main(String[] args) throws Exception {
		initializeOPCODE();
		File file = new File(FILENAME);
		File file1 = new File(FILENAME1);
		formatTheFileNow(file);
		System.out.println("File formatted syccessfully!");
		try (PrintWriter out = new PrintWriter("pass1.txt")) {
			String[] columns = readSicFile(file1).toString().split("\n");
			for (int i = 0; i < columns.length; i++)
				if (columns[i].length() != 0)
					out.println(columns[i]);
			out.print(lastline);
			System.out.println("Pass1 process completed successfully!");

		}
		catch(Exception e) {
			System.err.println("Erorr occured @ pass1");
		}
		try (PrintWriter out = new PrintWriter("SYMTAB.txt")) {
			for (String key : SYMTAB.keySet())
				out.println(key + " = " + SYMTAB.get(key) + "\n");
			System.out.println("SYMTAB process completed successfully!");
		}
		catch(Exception e) {
			System.err.println("Erorr occured @ SYMTAB");
		}
		try (PrintWriter out = new PrintWriter("pass2.txt")) {
			String[] columns = WriteSicFile2(file1).toString().split("\n");
			for (int i = 0; i < columns.length; i++)
				if (columns[i].length() != 0)
					out.println(columns[i]);
			System.out.println("Pass2 process completed successfully!");

		}
		catch(Exception e) {
			System.err.println("Erorr occured @ pass2");
		}
	}
}