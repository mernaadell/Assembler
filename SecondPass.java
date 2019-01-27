package assemblerPKJ;

import static assemblerPKJ.FirstPass.OPCode;
import static assemblerPKJ.FirstPass.SYMTAB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;

public class SecondPass {
	FirstPass x = new FirstPass();
	static StringBuffer internalfile = new StringBuffer();
	static StringBuffer intermediateFilefinal = new StringBuffer();
	static String valueX = "1000000000000000";
	static String firstlocator;
	static String temp = FirstPass.temporary;
	static int counter = 0;
	static int count = 1;

	private static BufferedReader bufferedReader;

	static StringBuffer WriteSicFile2(File file) {
		try {
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);

			String line2;
			while ((line2 = bufferedReader.readLine()) != null) {
				Matcher matcher3 = FirstPass.patten3.matcher(line2);
				if (count > 10) {
					internalfile.insert(9,
							(String.format("%02X", Integer.parseInt(Integer.toHexString(counter), 16) | 0x00) + " ")
									.toUpperCase());
					temp = Integer.toHexString(Integer.parseInt(temp, 16) + counter).toUpperCase();
					if (counter != 0)
						intermediateFilefinal.append(internalfile + "\n");
					internalfile.delete(0, internalfile.length());
					internalfile.append("T " + String.format("%06X", Integer.parseInt(temp, 16) | 0x000000) + " ");
					counter = 0;
					count = 1;
				}
				if (line2.matches(FirstPass.pattern3)) {
					if (matcher3.find()) {
						if (matcher3.group(3).equals("RESB") || matcher3.group(3).equals("RESW")) {
							internalfile
									.insert(9,
											(String.format("%02X",
													Integer.parseInt(Integer.toHexString(counter), 16) | 0x00) + " ")
															.toUpperCase());
							if (counter != 0)
								intermediateFilefinal.append(internalfile + "\n");
							internalfile.delete(0, internalfile.length());
							if (matcher3.group(3).equals("RESB"))
								temp = Integer.toHexString(Integer.parseInt(SYMTAB.get(matcher3.group(1)), 16)
										+ Integer.parseInt(matcher3.group(5)));
							else
								temp = Integer.toHexString(Integer.parseInt(SYMTAB.get(matcher3.group(1)), 16)
										+ 3 * Integer.parseInt(matcher3.group(5)));
							internalfile
									.append("T " + String.format("%06X", Integer.parseInt(temp, 16) | 0x000000) + " ");
							count = 1;
							counter = 0;
						} else if (matcher3.group(3).equals("START")) {
							firstlocator = matcher3.group(5);
							intermediateFilefinal.append(("H " + matcher3.group(1) + " "
									+ String.format("%06X", Integer.parseInt(firstlocator, 16) | 0x000000) + " "
									+ String.format("%06X",
											Integer.parseInt(Integer.toHexString(Integer.parseInt(FirstPass.lastloc, 16)
													- Integer.parseInt(firstlocator, 16)), 16) | 0x000000)
									+ "\n").toUpperCase());
							internalfile
									.append("T " + String.format("%06X", Integer.parseInt(temp, 16) | 0x000000) + " ");
							continue;

						} else if (matcher3.group(3).equals("BYTE")) {
							if (matcher3.group(5).charAt(0) == 'C') {
								for (int i = 2; i < matcher3.group(5).length() - 1; i++) {
									internalfile.append(Integer.toHexString((int) (matcher3.group(5).charAt(i))));
									counter += (Integer.toHexString((int) (matcher3.group(5).charAt(i)))).length() / 2;
								}
								internalfile.append(" ");
								count++;

							} else if (matcher3.group(5).charAt(0) == 'X') {
								internalfile
										.append(matcher3.group(5).substring(2, matcher3.group(5).length() - 1) + " ");
								counter += (matcher3.group(5).substring(2, matcher3.group(5).length() - 1) + " ")
										.length() / 2;
								count++;
							}
						} else if (matcher3.group(3).equals("WORD")) {
							internalfile.append((String.format("%06X",
									Integer.parseInt(Integer.toHexString(Integer.parseInt(matcher3.group(5))), 16)
											| 0x000000))
									+ " ");
							counter += (String.format("%06d", Integer.parseInt(matcher3.group(5), 16)) + " ").length()
									/ 2;
							count++;
						} else if (line2.matches("(\\S+)(\\s+)(\\S+)(\\s+)(\\S+)(,X)")) {
							if (OPCode.containsKey(matcher3.group(3)) && SYMTAB
									.containsKey(matcher3.group(5).substring(0, matcher3.group(5).length() - 2))) {
								internalfile
										.append((OPCode.get(matcher3.group(3))
												+ Integer.toHexString((Integer
														.parseInt(SYMTAB.get(matcher3.group(5).substring(0,
																matcher3.group(5).length() - 2)), 16)
														+ Integer.parseInt(valueX, 2)))
												+ " ").toUpperCase());
								counter += 3;
								count++;
							} else
								defineError(line2);
						} else if (OPCode.containsKey(matcher3.group(3)) && SYMTAB.containsKey(matcher3.group(5))) {
							internalfile.append(OPCode.get(matcher3.group(3)) + SYMTAB.get(matcher3.group(5)) + " ");
							counter += 3;
							count++;
						} else if (!SYMTAB.containsKey(matcher3.group(5))
								&& !matcher3.group(5).matches("[-+]?\\d*\\.?\\d+"))
							defineError(line2);
					}
				} else if (line2.matches(FirstPass.pattern2)) {
					Matcher matcher2 = FirstPass.patten2.matcher(line2);
					if (matcher2.find()) {
						if (line2.matches("\\S+\\s+\\S+,X")) {
							if (OPCode.containsKey(matcher2.group(1)) && SYMTAB
									.containsKey(matcher2.group(3).substring(0, matcher2.group(3).length() - 2))) {
								internalfile
										.append(OPCode.get(matcher2.group(1))
												+ Integer.toHexString((Integer
														.parseInt(SYMTAB.get(matcher2.group(3).substring(0,
																matcher2.group(3).length() - 2)), 16)
														+ Integer.parseInt(valueX, 2)))
												+ " ");
								counter += 3;
								count++;
							} else {
								defineError(line2);
							}
						} else if (OPCode.containsKey(matcher2.group(1)) && SYMTAB.containsKey(matcher2.group(3))) {
							internalfile.append(OPCode.get(matcher2.group(1)) + SYMTAB.get(matcher2.group(3)) + " ");
							counter += 3;
							count++;
						} else if ((!SYMTAB.containsKey(matcher2.group(3)) || !OPCode.containsKey(matcher2.group(1)))
								&& !line2.contains("END")) {
							defineError(line2);
						}

					}

				} else if (line2.matches(FirstPass.pattern1)) {
					Matcher matcher1 = FirstPass.patten1.matcher(line2);
					if (matcher1.find() && OPCode.containsKey(matcher1.group(1))) {
						internalfile.append(OPCode.get(matcher1.group(1)) + "0000 ");
						counter += 3;
						count++;
					} else if (!SYMTAB.containsKey(matcher1.group(1)) && !(matcher1.group(1).contains("."))) {
						defineError(line2);
					}
				} else {
					if (line2.charAt(0) != ('.')) {
						defineError(line2);
					}
				}

			}
			if (internalfile.length() > 0) {
				internalfile.insert(9,
						(String.format("%02X", Integer.parseInt(Integer.toHexString(counter), 16) | 0x00).toUpperCase())
								+ " ");
				intermediateFilefinal.append(internalfile);
			}
			intermediateFilefinal
					.append("\n" + "E " + String.format("%06X", Integer.parseInt(firstlocator, 16) | 0x000000));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return intermediateFilefinal;
	}

	static void defineError(String line) {
		System.out.println(line);
		System.out.println("ERROR!/UNDEFINED");
		System.exit(0);
	}
}