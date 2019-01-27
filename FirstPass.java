package assemblerPKJ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirstPass {
	static String locator = "0";
	static String temporary;
	static String pattern1 = "(\\S{1,})"; // RSUB <-- Example
	static String pattern2 = "(\\S{1,})(\\s+)(\\S{1,})|(\\S{1,})(\\s+)(\\S{1,})(,X)"; // LDX ZERO | LDCH BUFFER,X
	static String pattern3 = "(\\S{1,})(\\s+)(\\S{1,})(\\s+)(\\S+)"; // WRREC LDX ZERO <-- Example
	static StringBuffer intermediateFile = new StringBuffer();
	static HashMap<String, String> OPCode = new HashMap<String, String>(); // Add in OPCODE
	static HashMap<String, String> SYMTAB = new HashMap<String, String>(); // Add in SYMTAB
	static String line, lastline, lastloc;// Maybe we'll need ObjectHashMap
	static Pattern patten1 = Pattern.compile(pattern1);
	static Pattern patten2 = Pattern.compile(pattern2);
	static Pattern patten3 = Pattern.compile(pattern3);

	static StringBuffer readSicFile(File file) {
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null) {
				lastloc = locator;
				lastline = line;
				if (line.matches(pattern3)) {
					Matcher matcher3 = patten3.matcher(line); // Matches the first line that contains locator
					if (matcher3.find()) {
						if (matcher3.group(3).equals("START")) {
							locator = Integer.toHexString(Integer.parseInt(matcher3.group(5), 16)); // first Location
							temporary = String.format("%06X", Integer.parseInt(locator, 16));
							checkTheSYMTAB_Now(matcher3.group(1));
							appendItNow(line);
						} else if (matcher3.group(3).equals("WORD")) {
							appendItNow(line);
							checkTheSYMTAB_Now(matcher3.group(1));
							locator = Integer.toHexString(Integer.parseInt(locator, 16) + 3).toUpperCase();
						}

						else if (matcher3.group(3).equals("RESW")) {
							checkTheSYMTAB_Now(matcher3.group(1));
							appendItNow(line);
							locator = Integer.toHexString(Integer.parseInt(locator, 16) + Integer.parseInt(matcher3.group(5)) * 3).toUpperCase();
						}
						else if (matcher3.group(3).equals("RESB")) {
							checkTheSYMTAB_Now(matcher3.group(1));
							appendItNow(line);
							locator = Integer.toHexString(Integer.parseInt(locator, 16) + Integer.parseInt(matcher3.group(5))).toUpperCase();
						}

						else if (matcher3.group(3).equals("BYTE")) {
							checkTheSYMTAB_Now(matcher3.group(1));
							appendItNow(line);
							if (matcher3.group(5).charAt(0) == 'C')
								locator = Integer
										.toHexString(Integer.parseInt(locator, 16) + (matcher3.group(5).length() - 3))
										.toUpperCase();
							else {
								locator = Integer
										.toHexString(
												Integer.parseInt(locator, 16) + ((matcher3.group(5).length() - 3) / 2))
										.toUpperCase();
							}

						} else {
							checkTheSYMTAB_Now(matcher3.group(1));
							appendItNow(line);
							locator = Integer.toHexString(Integer.parseInt(locator, 16) + 3).toUpperCase();
						}
					}
				} else if (line.matches(pattern2)) {
					appendItNow(line);
					Matcher matcher2 = patten2.matcher(line);
					if (matcher2.find()) {
						locator = Integer.toHexString(Integer.parseInt(locator, 16) + 3).toUpperCase();
					}
				} else if (line.matches(pattern1)) { // Check for condition Like RSUB/JSUB
					Matcher matcher1 = patten1.matcher(line);
					if (matcher1.find() && OPCode.containsKey(matcher1.group(0))) {
						appendItNow(line);
						locator = Integer.toHexString(Integer.parseInt(locator, 16) + 3).toUpperCase();
					}
				}
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return intermediateFile;
	}

	static StringBuffer appendItNow(String Buffer) {
		if (!Buffer.contains("END")) {
			intermediateFile.append(locator);
			intermediateFile.append(" " + Buffer);
			intermediateFile.append("\n");
		}

		return intermediateFile;
	}

	static void initializeOPCODE() { // OPCodes
		OPCode.put("ADD", "18");
		OPCode.put("AND", "40");
		OPCode.put("COMP", "28");
		OPCode.put("DIV", "24");
		OPCode.put("J", "3C");
		OPCode.put("JEQ", "30");
		OPCode.put("JGT", "34");
		OPCode.put("JLT", "38");
		OPCode.put("JSUB", "48");
		OPCode.put("LDCH", "50");
		OPCode.put("LDA", "00");
		OPCode.put("LDL", "08");
		OPCode.put("LDX", "04");
		OPCode.put("MUL", "20");
		OPCode.put("OR", "44");
		OPCode.put("RD", "D8");
		OPCode.put("RSUB", "4C");
		OPCode.put("STA", "0C");
		OPCode.put("STCH", "54");
		OPCode.put("STL", "14");
		OPCode.put("STX", "10");
		OPCode.put("SUB", "1C");
		OPCode.put("TD", "E0");
		OPCode.put("TIX", "2C");
		OPCode.put("WD", "DC");
	}

	static StringBuffer formatTheFileNow(File file) {
		try {
			try (PrintWriter out = new PrintWriter("Formatted!.txt")) {
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					line = line.replaceAll("(?m)^[\\s&&[^\\n]]+|[\\s+&&[^\\n]]+$", "");
					if (line.length() != 0)
						out.println(line);
				}
				fileReader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return intermediateFile;
	}

	static void checkTheSYMTAB_Now(String test) {
		if (!SYMTAB.containsKey(test))
			SYMTAB.put(test, (String.format("%04X", Integer.parseInt(locator,16) | 0x0000)));
		else {
			System.out.println("Duplicated " + test);
			System.exit(0);
		}
	}
}
