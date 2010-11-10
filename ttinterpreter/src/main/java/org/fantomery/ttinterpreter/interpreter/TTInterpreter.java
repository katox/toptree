/*
 *  Prototype program Top Tree Interpreter
 * 
 *  The package interpreter implements prototype program Top Tree Interpreter. It was developed
 *  as user interface over Top Tree, TFL and TQL to provide complex solution for working with
 *  Top Trees.
 *    
 *  Copyright (C) 2008  Michal Vajbar
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *  
 *  Developed by:	Michal Vajbar
 *  				Charles University in Prague
 *  				Faculty of Mathematics and Physics
 *					michal.vajbar@tiscali.cz
 */
package org.fantomery.ttinterpreter.interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.fantomery.ttinterpreter.ttlangs.tfl.TFLCompiler;
import org.fantomery.ttinterpreter.ttlangs.tql.Launcher;
import org.fantomery.ttinterpreter.ttlangs.tql.TQLError;
import org.fantomery.ttinterpreter.ttlangs.tql.TQLException;
import org.fantomery.ttinterpreter.ttlangs.tql.TQLInterpreter;

/**
 * This class represents prototype program Top Tree Interpreter. It was developed as user interface
 * over Top Tree, TFL and TQL to provide complex solution for working with Top Trees.
 * <p>
 * The class contains main method for running the prototype program. It also contains some private
 * methods that prints information for user to system (error) output.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since 1.0
 */
public class TTInterpreter {

	/** A regular expression for file names. */
	private static final String FILE_NAME = "[a-zA-Z][a-zA-Z0-9_\\\\]*";
	/** A regular expression for folders. */
	private static final String FOLDER_PATH = "(.+/)*";
	/** A regular expression for TFL files. */
	private static final String TFL_REGEX = FOLDER_PATH + FILE_NAME + "\\.(tfl|TFL)";
	/** A regular expression for JAVA class. */
	private static final String JAVA_REGEX = FILE_NAME;
	/** A regular expression for TQL files. */
	private static final String TQL_REGEX = FOLDER_PATH + FILE_NAME + "\\.(tql|TQL)";
	
	/** A folder where generated java classes are saved in. */
	private static final String OUTPUT_PATH = "algorithms/";
	/** A name of a package of generated java classed. */
	private static final String PACKAGE_NAME = "algorithms";

	/** A name of package where user defined functions for TQL are saves in. */
	private static final String FUNCTIONS_LOCATION = "func";
	/** A name of java class with user defined functions for TQL. */
	private static final String FUNCTIONS_NAME = "Functions";

	/** A string of prompt. */
	private static final String PROMPT = "TQL>";
	/** An exit command. */
	private static final String EXIT = "exit;";
	
	/** A string for generating vertex info class name. */
	private static final String VERTEX_INFO = "VertexInfo";
	/** A string for generating cluster info class name. */
	private static final String CLUSTER_INFO = "ClusterInfo";

	/** A name of a package of Top Tree which TQL works with. */
	private static final String TOPTREE_PACKAGE	= "temp";
	
	/**
	 * An empty constructor.
	 */
	private TTInterpreter() {
	}
	
	/**
	 * This method prints help instructions to system output.
	 */
	private static void showHelp() {
		// just print instructions how to run the program
		StringBuilder message = new StringBuilder();
		message.append("\nTop Tree Interpreter HELP:\n\n");
		message.append("Synopsis:\n");
		message.append("[-h | -? | {-f TFL_FILE | -e TFL_FILE | -j JAVA_CLASS} | -q TQL_FILE]\n");
		message.append("\nOptions:\n");		
		message.append("\t-h -?\tProgram displays help.\n");
		message.append("\t-f\tProgram generates java files, compiles them and starts\n\t\t")
		.append("TQL interpreter.\n");
		message.append("\t-e\tProgram only generates java files.\n");
		message.append("\t-j\tProgram compiles java files and starts TQL interpreter.\n");
		message.append("\t-q\tProgram executes commands from TQL_FILE after TQL interpreter\n\t\tstarts.\n");
		message.append("\nArguments:\n");		
		message.append("\tTFL_FILE\tA file containing description of an algorithm in Top\n\t\t\t")
		.append("Tree Friendly Language.\n");
		message.append("\t\t\tAccepted name format: [a-zA-Z][a-zA-Z0-9_]*\\.(tfl|TFL)\n");
		message.append("\tJAVA_CLASS\tA name of algorithm class. There must also exist files\n\t\t\t")
		.append("with vertex and cluster info classes.\n");
		message.append("\t\t\tAccepted name format: [a-zA-Z][a-zA-Z0-9_]*\n");
		message.append("\tTQL_FILE\tA file containing commands in Top Tree Query Language.\n");
		message.append("\t\t\tAccepted name format: [a-zA-Z][a-zA-Z0-9_]*\\.(tql|TQL)\n");
		System.out.println(message.toString());
	}

	/**
	 * This method prints information about missing argument of the option to error system output.
	 * 
	 * @param c A name of the option.
	 */
	private static void showMissingParameter(char c) {
		// print information about missing argument
		System.err.println("\nMissing argument for -" + c + ". For help use option '-h' or '-?'\n");
	}

	/**
	 * This method prints information about wrong argument format of the option to error system output.
	 * 
	 * @param c A name of the option.
	 */
	private static void showWrongFileNameFormat(char c) {
		// print information about wrong file name format
		System.err.println("\nWrong file name format for -" + c + ". For help use option '-h' or '-?'\n");
	}

	/**
	 * This method prints information about multiple usage of the option to error system output.
	 * 
	 * @param c A name of the option.
	 */
	private static void showMultipleUsage(char c) {
		// print information about wrong usage
		System.err.println("\nOption -" + c + " used more than once. For help use option '-h' or '-?'\n");
	}

	/**
	 * This method prints information about wrong usage of options to error system output.
	 */
	private static void showCommonEFJUsage() {
		// print information about wrong usage of options
		System.err.println("\nOnly one of '-f', '-e' and '-j' can be used."
				+ " For help use option '-h' or '-?'\n");
	}

	/**
	 * This method prints information about not allowed options to error system output.
	 * 
	 * @param str Not allowed option.
	 */
	private static void showDeniedOption(String str) {
		// print information about wrong option
		System.err.println("\nNot allowed option '" + str + "'. For help use option '-h' or '-?'\n");
	}

	/**
	 * This method prints information that none options was used to error system output.
	 */
	private static void showNoArgument() {
		// print information that no argument from -f, -e, and -j used 
		System.err.println("\nOne of '-f', '-e' and '-j' must be used. For help use option '-h' or '-?'\n");
	}

	/**
	 * This method just prints valediction with user to system output.
	 */
	private static void bye() {
		// print valediction
		System.out.println("Bye.\n");
	}

	
	/**
	 * The method returns true if the file exists and false if it doesn't exist.
	 * 
	 * @param file_name A name of the file.
	 * @return true if the file exists and false if it doesn't exist.
	 */
	private static boolean fileExists(String file_name) {
		// look if the file exists
	    File f = new File(file_name);
	    return f.exists();
	}

	/**
	 * The method deletes the file.
	 * 
	 * @param file_name A name of the file.
	 */
	private static void deleteFile(String file_name) {
		// delete the file
	    File f = new File(file_name);
		f.delete();
	}

	/**
	 * This method deletes *.class files generated by Java compiler for *.java files generated by TFL
	 * compiler. If files doesn't exist then nothing happens.
	 * 
	 * @param java_file A name of algorithm class.
	 */
	private static void deleteTempFiles(String java_file) {
		// delete *.class files if their exist
		if (java_file != null) {
			// prepare names of files
			String vertex_file 		= PACKAGE_NAME + "/" + java_file + VERTEX_INFO  + ".class";
			String cluster_file		= PACKAGE_NAME + "/" + java_file + CLUSTER_INFO + ".class";
			String algortithm_file1	= PACKAGE_NAME + "/" + java_file + ".class";
			String algortithm_file2	= PACKAGE_NAME + "/" + java_file + "$1.class";
			// delete files
			if (fileExists(vertex_file)) deleteFile(vertex_file);
			if (fileExists(cluster_file)) deleteFile(cluster_file);
			if (fileExists(algortithm_file1)) deleteFile(algortithm_file1);
			if (fileExists(algortithm_file2)) deleteFile(algortithm_file2);
		}
	}

	/**
	 * This method provides user interface over Top Trees, TFL and TQL.
	 * <p>
	 * First, it parses arguments from command line to recognize what should be done:
	 * <dl>
	 *   <dt>-?</dt><dd>prints help</dd>
	 *   <dt>-h</dt><dd>prints help</dd>
	 *   <dt>-f TFL_FILE</dt><dd>compiles TFL file and runs TQL interpreter</dd>
	 *   <dt>-e TFL_FILE</dt><dd>compiles TFL file and exits</dd>
	 *   <dt>-j JAVA_CLASS</dt><dd>runs TQL interpreter over defined java classes</dd>
	 *   <dt>-q TQL_FILE</dt><dd>after the TQL interpreter start, it runs defined TQL script</dd>
	 * </dl>
	 * If -e or -f was used then the method compiles TFL file and generates three java files *.java.
	 * If -e was used then the method exits. Else the method read vertex identifier from system input
	 * and prepare TQL interpreter. If -q was used then it executes command from the file.
	 * Finally the method reads and executes commands from system input until exit command occurs.
	 * Before the method exits, it deletes *.class files that were generated by Java compiler from 
	 * *.java classes generated by TFL compiler.
	 * 
	 * @param args Arguments from command line.
	 */
	public static void main(String[] args) {
		
//		String[] args = {"-e", "scripts/nearestMarkedVertex.tfl"};
//		String[] args = {"-f", "scripts/nearestMarkedVertex.tfl", "-q", "scripts/nearestMarkedVertex.tql"};
//		String[] args = {"-f", "scripts/length.tfl", "-q", "scripts/length.tql"};
//		String[] args = {"-f", "scripts/maxEdgeInTree.tfl", "-q", "scripts/maxEdgeInTree.tql"};
//		String[] args = {"-f", "scripts/diameter.tfl", "-q", "scripts/diameter.tql"};
//		String[] args = {"-f", "scripts/median.tfl", "-q", "scripts/median.tql"};
//		String[] args = {"-f", "scripts/diameter.tfl", "-q", "scripts/exampleDiameter.tql"};
//		String[] args = {"-f", "scripts/median.tfl", "-q", "scripts/exampleMedian.tql"};
//		String[] args = {"-f", "scripts/maxEdgeOnWay.tfl", "-q", "scripts/maxEdgeOnWay.tql"};
//		String[] args = {"-f", "scripts/center.tfl", "-q", "scripts/center.tql"};
//		String[] args = {"-f", "scripts/ancestor.tfl", "-q", "scripts/ancestor.tql"};
		// there must be some arguments
		if (args.length == 0) {
			showNoArgument();
			return;
		}
		
		// storages for file names and other informations
		String tfl_file = null;					// name of *.tfl
		String java_file = null;				// name of java algorithm class
		String tql_file = null;					// name of *.tql
		boolean run_tql_interpreter = true;		// switcher if run TQLInterpreter
		
		// a cursor for parsing command line arguments
		int i = 0;
		while(true) {
			// help switchers
			if (args[i].matches("-[h\\?]")) {
				showHelp();
				return;
			}
			// other allowed switchers
			if (!args[i].matches("-[efjq]")) {
				showDeniedOption(args[i]);
				return;
			}
			// take just a switcher
			char c = args[i].charAt(1);
			// move cursor to the argument
			i++;
			// each switcher must have an argument
			if (i >= args.length) {
				showMissingParameter(c);
				return;
			}
			/* Check the argument and remember it. First check if the parameter was already used
			 * already. If wasn't then check its format and remember it. */
			switch (c) {
			case 'j':	// java class
				if (java_file != null) {
					showMultipleUsage(c);
					return;
				}
				if (tfl_file != null) {
					showCommonEFJUsage();
					return;
				}
				if (!args[i].matches(JAVA_REGEX)) {
					showWrongFileNameFormat(c);
					return;
				}
				java_file = args[i];
				break;
			case 'q':	// tql file
				if (tql_file != null) {
					showMultipleUsage(c);
					return;
				}
				if (!args[i].matches(TQL_REGEX)) {
					showWrongFileNameFormat(c);
					return;
				}
				tql_file = args[i];
				break;
			default:	// tfl file and don't run interpreter
				assert c == 'e' || c == 'f';
				if (run_tql_interpreter == false) {
					showMultipleUsage(c);
					return;
				}
				if (tfl_file != null && c == 'f') {
					showMultipleUsage(c);
					return;
				}
				if (tfl_file != null || java_file != null) {
					showCommonEFJUsage();
					return;
				}
				if (!args[i].matches(TFL_REGEX)) {
					showWrongFileNameFormat(c);
					return;
				}
				tfl_file = args[i];
				if (c == 'e') {
					run_tql_interpreter = false;
				}
			}
			
			i++;
			// if end of the array then break parsing of the command line
			if (i >= args.length) {
				break;
			}
		}
		
		// if no tfl-file and no java class then the arguments were used in wrong way
		if (tfl_file == null && java_file == null) {
			showNoArgument();
			return;
		}

		// if *.tfl file then try to generate java files
		if (tfl_file != null) {
			try {
				// remember name of algorithm class
				java_file = TFLCompiler.compileTFL(tfl_file, OUTPUT_PATH, PACKAGE_NAME);
			} catch (Exception e) {
				deleteTempFiles(java_file); // delete temporary files
				// if any exception occurs then print it
				System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
				return;
			}
		}
		
		// if argument -e used then finish now
		if (!run_tql_interpreter) {
			deleteTempFiles(java_file); // delete temporary files
			bye();
			return;
		}
		
		assert java_file != null;
		
		String vertex_class		= java_file + VERTEX_INFO;
		String cluster_class	= java_file + CLUSTER_INFO;
		String algortithm_class	= java_file;
		String vertex_file 		= PACKAGE_NAME + "/"  + vertex_class + ".java";
		String cluster_file		= PACKAGE_NAME + "/"  + cluster_class + ".java";
		String algortithm_file	= PACKAGE_NAME + "/"  + algortithm_class + ".java";
		String vertex_package	= PACKAGE_NAME + "." + vertex_class;
		String cluster_package	= PACKAGE_NAME + "." + cluster_class;
		String functions_file	= FUNCTIONS_LOCATION + "/" + FUNCTIONS_NAME + ".java";

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		// compile TFL to java
		TQLInterpreter interpreter = null;
		try {
			Launcher.CompileFiles(vertex_file, cluster_file, algortithm_file, functions_file);

			HashMap<String, Method> functions = 
				Launcher.CheckFunctions(FUNCTIONS_LOCATION, FUNCTIONS_NAME);
			
			interpreter = Launcher.createInterpreter(
					vertex_package, cluster_package, functions, TOPTREE_PACKAGE);

	        System.out.print("\nInsert a name of an unique node field name: ");
            while (true) {
            	String input = in.readLine();
    	        if (input == null || input.matches(EXIT)) {
    				deleteTempFiles(java_file); // delete temporary files
					bye();
    	        	return;
    	        }
    	        if (interpreter.prepareTopTree(input, vertex_class, cluster_class, algortithm_class,
    	        		PACKAGE_NAME)) {
    		        System.out.println("");
    	        	break;
    	        }
			}
		} catch (Exception e) {
			deleteTempFiles(java_file); // delete temporary files
			// if any exception occurs then print it
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
			return;
		}		
        
		// execute command from TQL file if any
		if (tql_file != null) {
			try {
				BufferedReader input_file = new BufferedReader(new FileReader(tql_file));
				String line = input_file.readLine();
				while(line != null) {
		   			System.out.println(PROMPT + line);

					// make semantic analysis of the command and try to execute it
					if (interpreter.execute(line)) {
						// interpreter has returned true so we finish the program
						input_file.close();
						deleteTempFiles(java_file); // delete temporary files
						bye();
						return;
					}

					// read next line
					line = input_file.readLine();
				}
				// close file
				input_file.close();
			} 
			catch (Exception e) {
				deleteTempFiles(java_file); // delete temporary files
				// if any exception occurs then print it
				System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
				return;
			}
		}
		
		// execute all command until exit command is used
		while(true) {
			try {
	   			System.out.print(PROMPT);

                                String line = in.readLine();
				if (line == null || interpreter.execute(line)) {
					deleteTempFiles(java_file); // delete temporary files
					bye();
					break;
				}
			}
			catch (TQLException e) {
				// if any exception then print it
				System.out.println(e.getMessage());
			}
			catch (Exception e) {
				// if any exception then print it
				if (e.getMessage() != null || e.getCause() == null) {
					System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
				} else {
					Throwable t = e.getCause();
					while (t != null && t.getMessage() == null)
						t = t.getCause();
					if (t != null) {
						System.out.println(t.getClass().getSimpleName() + ": " + t.getMessage());
					} else {
						e.printStackTrace();
					}
				}
			}
			catch (TQLError e) {
				// if any exception then print it
				System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
	}

}
