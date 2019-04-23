package edu.neu.ece.devicedescriptiongenerator.main;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.devicedescriptiongenerator.controller.Controller;
import edu.neu.ece.devicedescriptiongenerator.utility.FileUtil;

/**
 * Main class, entry to the program.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class ConsoleMain {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Relative path to configuration directory.
	 */
	private static final String CONFIG_PATH = "conf" + File.separator;

	/**
	 * Help option name on console.
	 */
	private static final String HELP_OPTION_NAME = "h";

	/**
	 * Ontology root class IRI option name on console.
	 */
	private static final String ROOT_CLASS_IRI = "rootIRI";

	/**
	 * Input IRI option name on console.
	 */
	private static final String INPUT_IRI = "inputIRI";

	/**
	 * Input file option name on console.
	 */
	private static final String INPUT_FILE_PATH = "inputFilePath";

	/**
	 * The number of device descriptions option name on console.
	 */
	private static final String DEV_NUMBER = "devNumber";

	/**
	 * Random seed option name on console.
	 */
	private static final String RAM_SEED = "ramSeed";

	/**
	 * Output file path option name on console.
	 */
	private static final String OUTPUT_FILE_PATH = "outputFilePath";

	/**
	 * Class constraint selection probability option name on console.
	 */
	private static final String CLASS_CONSTRAINT_SELECTION_PROBABILITY = "classConstraintSelectionProbability";

	/**
	 * New individual probability option name on console.
	 */
	private static final String NEW_INDIVIDUAL_PROBABILITY = "newIndividualProbability";

	/**
	 * Class assertion probability option name on console.
	 */
	private static final String CLASS_ASSERTION_PROBABILITY = "classAssertionProbability";

	/**
	 * Object property assertion probability option name on console.
	 */
	private static final String OBJECT_PROPERTY_ASSERTION_PROBABILITY = "objectPropertyAssertionProbability";

	/**
	 * Data property assertion probability option name on console.
	 */
	private static final String DATA_PROPERTY_ASSERTION_PROBABILITY = "dataPropertyAssertionProbability";

	/**
	 * Super class selection option name on console.
	 */
	private static final String SUPER_CLASS_SELECTION_PROBABILITY = "superClassSelectionProbability";

	/**
	 * Equivalent object property selection option name on console.
	 */
	private static final String EQUIVALENT_OBJECT_PROPERTY_SELECTION_PROBABILITY = "equivalentObjectPropertySelectionProbability";

	/**
	 * Equivalent data property selection option name on console.
	 */
	private static final String EQUIVALENT_DATA_PROPERTY_SELECTION_PROBABILITY = "equivalentDataPropertySelectionProbability";

	/**
	 * Disjoint object property selection option name on console.
	 */
	private static final String DISJOINT_OBJECT_PROPERTY_SELECTION_PROBABILITY = "disjointObjectPropertySelectionProbability";

	/**
	 * Disjoint data property selection option name on console.
	 */
	private static final String DISJOINT_DATA_PROPERTY_SELECTION_PROBABILITY = "disjointDataPropertySelectionProbability";

	/**
	 * Super object property selection option name on console.
	 */
	private static final String SUPER_OBJECT_PROPERTY_SELECTION_PROBABILITY = "superObjectPropertySelectionProbability";

	/**
	 * Super data property selection option name on console.
	 */
	private static final String SUPER_DATA_PROPERTY_SELECTION_PROBABILITY = "superDataPropertySelectionProbability";

	/**
	 * Inverse object property selection option name on console.
	 */
	private static final String INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY = "inverseObjectPropertySelectionProbability";

	/**
	 * Object property symmetric selection option name on console.
	 */
	private static final String SYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY = "symmetricObjectPropertySelectionProbability";

	/**
	 * Object property asymmetric characteristic selection option name on console.
	 */
	private static final String ASYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY = "asymmetricObjectPropertySelectionProbability";

	/**
	 * Object property irreflexive characteristic selection option name on console.
	 */
	private static final String IRREFLEXIVE_OBJECT_PROPERTY_SELECTION_PROBABILITY = "irreflexiveObjectPropertySelectionProbability";

	// Static block
	static {
		PropertyConfigurator.configure(CONFIG_PATH + "log4j.properties");
	}

	/**
	 * Main function, entrance to the program.
	 * 
	 * @param args
	 *            Arguments from console.
	 */
	public static void main(String[] args) {
		new ConsoleMain().parseCommandLine(args);
	}

	/**
	 * Parse arguments from console.
	 * 
	 * @param args
	 *            Arguments from console.
	 */
	public void parseCommandLine(String[] args) {
		CommandLine line = null;
		Options options = createOptions();

		try {
			line = new DefaultParser().parse(options, args);
		} catch (ParseException exp) {
			logger.error("Parsing failed. Reason:" + exp.getMessage());
			printHelpMenu(options);
			System.exit(1);
		}

		if (line.hasOption(HELP_OPTION_NAME)) {
			printHelpMenu(options);
			System.exit(1);
		}

		String rootIRIString = line.getOptionValue(ROOT_CLASS_IRI);

		IRI ontologyIRI;
		File inputFile = null;

		if (line.hasOption(INPUT_FILE_PATH)) {
			inputFile = new File(line.getOptionValue(INPUT_FILE_PATH));
			ontologyIRI = IRI.create(inputFile);
		} else {
			// rename the file here
			String iriString = line.getOptionValue(INPUT_IRI);
			String out = iriString.replace(" ", "%20");
			ontologyIRI = IRI.create(out);
		}

		/*
		 * if (line.hasOption(DEPENDENCIES)) { for (String path :
		 * line.getOptionValues(DEPENDENCIES)) { File dependency = new File(path);
		 * dependencies.add(IRI.create(dependency)); } }
		 */

		int devNumber = 1;
		if (line.hasOption(DEV_NUMBER))
			devNumber = Integer.parseInt(line.getOptionValue(DEV_NUMBER));

		int baseSeed = 0;
		if (line.hasOption(RAM_SEED))
			baseSeed = Integer.parseInt(line.getOptionValue(RAM_SEED));
		long seed = baseSeed * (Integer.MAX_VALUE + 1) + devNumber;

		String outputFilePath = "instancedata" + File.separator + "DeviceDescription" + devNumber + ".rdf";
		File outputFile = line.hasOption(OUTPUT_FILE_PATH) ? FileUtil.createFile(line.getOptionValue(OUTPUT_FILE_PATH))
				: FileUtil.createFile(outputFilePath);

		double classConstraintSelectionProbability = 0.9;
		if (line.hasOption(CLASS_CONSTRAINT_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(CLASS_CONSTRAINT_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Class constraint selection probability is out of range [0, 1]");
				System.exit(1);
			}
			classConstraintSelectionProbability = temp;
		}

		double newIndividualProbability = 0.5;
		if (line.hasOption(NEW_INDIVIDUAL_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(NEW_INDIVIDUAL_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("New individual probability is out of range [0, 1]");
				System.exit(1);
			}
			newIndividualProbability = temp;
		}

		double classAssertionProbability = 1.0;
		if (line.hasOption(CLASS_ASSERTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(CLASS_ASSERTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Class assertion probability is out of range [0, 1]");
				System.exit(1);
			}
			classAssertionProbability = temp;
		}

		double objectPropertyAssertionProbability = 0.5;
		if (line.hasOption(OBJECT_PROPERTY_ASSERTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(OBJECT_PROPERTY_ASSERTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Object property assertion probability is out of range [0, 1]");
				System.exit(1);
			}
			objectPropertyAssertionProbability = temp;
		}

		double dataPropertyAssertionProbability = 0.5;
		if (line.hasOption(DATA_PROPERTY_ASSERTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(DATA_PROPERTY_ASSERTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Data property assertion probability is out of range [0, 1]");
				System.exit(1);
			}
			dataPropertyAssertionProbability = temp;
		}

		double superClassSelectionProbability = 0.5;
		if (line.hasOption(SUPER_CLASS_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(SUPER_CLASS_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Super class selection probability is out of range [0, 1]");
				System.exit(1);
			}
			superClassSelectionProbability = temp;
		}

		double equivalentObjectPropertySelectionProbability = 0.5;
		if (line.hasOption(EQUIVALENT_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(EQUIVALENT_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Equivalent object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			equivalentObjectPropertySelectionProbability = temp;
		}

		double equivalentDataPropertySelectionProbability = 0.5;
		if (line.hasOption(EQUIVALENT_DATA_PROPERTY_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(EQUIVALENT_DATA_PROPERTY_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Equivalent data property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			equivalentDataPropertySelectionProbability = temp;
		}

		double disjointObjectPropertySelectionProbability = 0.8;
		if (line.hasOption(DISJOINT_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(DISJOINT_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Disjoint object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			disjointObjectPropertySelectionProbability = temp;
		}

		double disjointDataPropertySelectionProbability = 0.8;
		if (line.hasOption(DISJOINT_DATA_PROPERTY_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(DISJOINT_DATA_PROPERTY_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Disjoint data property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			disjointDataPropertySelectionProbability = temp;
		}

		double superObjectPropertySelectionProbability = 0.5;
		if (line.hasOption(SUPER_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(SUPER_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Super object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			superObjectPropertySelectionProbability = temp;
		}

		double superDataPropertySelectionProbability = 0.5;
		if (line.hasOption(SUPER_DATA_PROPERTY_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(SUPER_DATA_PROPERTY_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Super data property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			superDataPropertySelectionProbability = temp;
		}

		double inverseObjectPropertySelectionProbability = 0.8;
		if (line.hasOption(INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Inverse object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			inverseObjectPropertySelectionProbability = temp;
		}

		double symmetricObjectPropertySelectionProbability = 0.8;
		if (line.hasOption(SYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(SYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Symmetric object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			symmetricObjectPropertySelectionProbability = temp;
		}

		double asymmetricObjectPropertySelectionProbability = 0.8;
		if (line.hasOption(ASYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(ASYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Asymmetric object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			asymmetricObjectPropertySelectionProbability = temp;
		}

		double irreflexiveObjectPropertySelectionProbability = 0.8;
		if (line.hasOption(IRREFLEXIVE_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double temp = Double.parseDouble(line.getOptionValue(IRREFLEXIVE_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (temp < 0.0 || temp > 1.0) {
				logger.error("Irreflexive object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			irreflexiveObjectPropertySelectionProbability = temp;
		}

		new Controller(ontologyIRI, inputFile, rootIRIString, devNumber, seed, outputFile,
				classConstraintSelectionProbability, newIndividualProbability, classAssertionProbability,
				objectPropertyAssertionProbability, dataPropertyAssertionProbability, superClassSelectionProbability,
				equivalentObjectPropertySelectionProbability, equivalentDataPropertySelectionProbability,
				disjointObjectPropertySelectionProbability, disjointDataPropertySelectionProbability,
				superObjectPropertySelectionProbability, superDataPropertySelectionProbability,
				inverseObjectPropertySelectionProbability, symmetricObjectPropertySelectionProbability,
				asymmetricObjectPropertySelectionProbability, irreflexiveObjectPropertySelectionProbability)
						.generateDeviceDescriptions();
	}

	/**
	 * Create Option objects from option names.
	 * 
	 * @return Option objects.
	 */
	private Options createOptions() {
		Options options = new Options();
		options.addOption(Option.builder(HELP_OPTION_NAME).desc("Views this help text").build());

		OptionGroup inputOptions = new OptionGroup();
		inputOptions.setRequired(true);
		inputOptions
				.addOption(Option.builder(INPUT_IRI).argName("IRI").hasArg().desc("the iri of an ontology").build());
		inputOptions.addOption(
				Option.builder(INPUT_FILE_PATH).argName("PATH").hasArg().desc("The local path to an ontology").build());
		options.addOptionGroup(inputOptions);

		Option opt = Option.builder(ROOT_CLASS_IRI).argName("IRI").hasArgs().desc("root class IRI as string").build();
		opt.setRequired(true);
		options.addOption(opt);

		// options.addOption(Option.builder(DEPENDENCIES).argName("PATHS").hasArgs().desc("paths
		// to dependencies of a local ontology").build());
		options.addOption(Option.builder(DEV_NUMBER).argName("NUMBER").hasArg()
				.desc("The number of device descriptions; 1 by default").build());
		options.addOption(Option.builder(RAM_SEED).argName("SEED").hasArgs()
				.desc("Random seed used for random device description generation; 0 by default").build());
		options.addOption(Option.builder(OUTPUT_FILE_PATH).argName("PATH").hasArg().desc(
				"The local path to the output RDF device descriptions; ./instancedata/DeviceDescription<NUMBER>.rdf by default")
				.build());
		options.addOption(Option.builder(CLASS_CONSTRAINT_SELECTION_PROBABILITY).argName("PROBABILITY").hasArg().desc(
				"The probability of selecting an OWL class constraint (anonymous class expression) of an OWL named class; 0.9 by default")
				.build());
		options.addOption(Option.builder(NEW_INDIVIDUAL_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of creating a new OWL named individual; 0.5 by default").build());
		options.addOption(Option.builder(CLASS_ASSERTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of generating class assertion axiom for each named individual; 1.0 by default")
				.build());
		options.addOption(Option.builder(OBJECT_PROPERTY_ASSERTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of creating a new object property assertion axiom; 0.5 by default").build());
		options.addOption(Option.builder(DATA_PROPERTY_ASSERTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of creating a new data property assertion axiom; 0.5 by default").build());
		options.addOption(Option.builder(SUPER_CLASS_SELECTION_PROBABILITY).argName("PROBABILITY").hasArg().desc(
				"The probability of selecting a super class (direct or inferred) of a class for class assertion generation; 0.5 by default")
				.build());
		options.addOption(Option.builder(EQUIVALENT_OBJECT_PROPERTY_SELECTION_PROBABILITY).argName("PROBABILITY")
				.hasArg()
				.desc("The probability of selecting an equivalent object property (direct or inferred) of a property for object property assertion generation; 0.5 by default")
				.build());
		options.addOption(Option.builder(EQUIVALENT_DATA_PROPERTY_SELECTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of selecting an equivalent data property (direct or inferred) of a property for data property assertion generation; 0.5 by default")
				.build());
		options.addOption(Option.builder(DISJOINT_OBJECT_PROPERTY_SELECTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of selecting a disjoint object property for negative object property assertion generation; 0.8 by default")
				.build());
		options.addOption(Option.builder(DISJOINT_DATA_PROPERTY_SELECTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of selecting a disjoint data property for negative data property assertion generation; 0.8 by default")
				.build());
		options.addOption(Option.builder(SUPER_OBJECT_PROPERTY_SELECTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of selecting a super object property while generating object property assertion; 0.5 by default")
				.build());
		options.addOption(Option.builder(SUPER_DATA_PROPERTY_SELECTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of selecting a super data property while generating data property assertion; 0.5 by default")
				.build());
		options.addOption(Option.builder(INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of selecting an inverse property (direct or inferred) of a property for object property assertion generation; 0.8 by default")
				.build());
		options.addOption(Option.builder(SYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY).argName("PROBABILITY")
				.hasArg()
				.desc("The probability of selecting symmetric characteristic of an object property for object property assertion generation; 0.8 by default")
				.build());
		options.addOption(Option.builder(ASYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY).argName("PROBABILITY")
				.hasArg()
				.desc("The probability of selecting asymmetric characteristic of an object property for negative object property assertion generation; 0.8 by default")
				.build());
		options.addOption(Option.builder(IRREFLEXIVE_OBJECT_PROPERTY_SELECTION_PROBABILITY).argName("PROBABILITY")
				.hasArg()
				.desc("The probability of selecting irreflexive characteristic of an object property for negative object property assertion generation; 0.8 by default")
				.build());
		return options;
	}

	/**
	 * Print help menu, in the case when invalid input occurs.
	 * 
	 * @param options
	 *            Option objects.
	 */
	private void printHelpMenu(Options options) {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("java -jar devicedescriptiongenerator-1.0-SNAPSHOT.jar", options);
	}

}
