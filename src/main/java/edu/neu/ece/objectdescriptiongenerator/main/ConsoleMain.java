package edu.neu.ece.objectdescriptiongenerator.main;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.objectdescriptiongenerator.controller.Controller;
import edu.neu.ece.objectdescriptiongenerator.utility.FileUtil;

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
	 * Input ontology (TBox) URI option name on console.
	 */
	private static final String ONTOLOGY_URI = "ontologyURI";

	/**
	 * Ontology IRI to document IRI mapping option name on console.
	 */
	private static final String MAPPING = "IRIMapping";

	/**
	 * The number of object descriptions option name on console.
	 */
	private static final String OBJ_NUMBER = "objNumber";

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
	 * Super class selection probability option name on console.
	 */
	private static final String SUPER_CLASS_SELECTION_PROBABILITY = "superClassSelectionProbability";

	/**
	 * Equivalent object property selection probability option name on console.
	 */
	private static final String EQUIVALENT_OBJECT_PROPERTY_SELECTION_PROBABILITY = "equivalentObjectPropertySelectionProbability";

	/**
	 * Equivalent data property selection probability option name on console.
	 */
	private static final String EQUIVALENT_DATA_PROPERTY_SELECTION_PROBABILITY = "equivalentDataPropertySelectionProbability";

	/**
	 * Disjoint object property selection probability option name on console.
	 */
	private static final String DISJOINT_OBJECT_PROPERTY_SELECTION_PROBABILITY = "disjointObjectPropertySelectionProbability";

	/**
	 * Disjoint data property selection probability option name on console.
	 */
	private static final String DISJOINT_DATA_PROPERTY_SELECTION_PROBABILITY = "disjointDataPropertySelectionProbability";

	/**
	 * Super object property selection probability option name on console.
	 */
	private static final String SUPER_OBJECT_PROPERTY_SELECTION_PROBABILITY = "superObjectPropertySelectionProbability";

	/**
	 * Super data property selection probability option name on console.
	 */
	private static final String SUPER_DATA_PROPERTY_SELECTION_PROBABILITY = "superDataPropertySelectionProbability";

	/**
	 * Inverse object property selection probability option name on console.
	 */
	private static final String INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY = "inverseObjectPropertySelectionProbability";

	/**
	 * Object property symmetric characteristic selection probability option name on
	 * console.
	 */
	private static final String SYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY = "symmetricObjectPropertySelectionProbability";

	/**
	 * Object property asymmetric characteristic selection probability option name
	 * on console.
	 */
	private static final String ASYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY = "asymmetricObjectPropertySelectionProbability";

	/**
	 * Object property irreflexive characteristic selection probability option name
	 * on console.
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
	 * @throws URISyntaxException
	 *             If ontology IRI creation failed.
	 */
	public static void main(String[] args) throws URISyntaxException {
		new ConsoleMain().parseCommandLine(args);
	}

	/**
	 * Parse arguments from console.
	 * 
	 * @param args
	 *            Arguments from console.
	 * @throws URISyntaxException
	 *             If ontology IRI creation failed.
	 */
	public void parseCommandLine(String[] args) throws URISyntaxException {
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
		String ontologyURIString = line.getOptionValue(ONTOLOGY_URI);

		IRI ontologyIRI = IRI.create(new URI(ontologyURIString));
		// IRI ontologyIRI = IRI.create(new File(datasetURIString));
		Controller.Builder builder = new Controller.Builder(ontologyIRI, rootIRIString);

		logger.info(ROOT_CLASS_IRI + " = " + rootIRIString);
		logger.info(ONTOLOGY_URI + " = " + ontologyURIString);
		if (line.hasOption(MAPPING)) {
			String mapping = line.getOptionValue(MAPPING);
			Map<String, String> ontologyIRIMapper = new HashMap<>();
			String[] IRIMapperString = mapping.split(";");
			String[] bindingPair;
			for (int i = 0; i < IRIMapperString.length; i++) {
				bindingPair = IRIMapperString[i].split(",");
				if (bindingPair.length != 2) {
					logger.error("Incorrect format for ontology IRI to document IRI mapping.");
					System.exit(1);
				}
				// logger.info(bindingPair[0] + "\t" + bindingPair[1]);
				ontologyIRIMapper.put(bindingPair[0], bindingPair[1]);
			}
			logger.info(MAPPING + " = " + mapping);
			builder.setOntologyIRIMapper(ontologyIRIMapper);
		}

		int objNumber = 1;
		if (line.hasOption(OBJ_NUMBER)) {
			objNumber = Integer.parseInt(line.getOptionValue(OBJ_NUMBER));
			if (objNumber < 0) {
				logger.error("Object number must be a positive number.");
				System.exit(1);
			}
			logger.info(OBJ_NUMBER + " = " + objNumber);
			builder.setObjNumber(objNumber);
		}

		if (line.hasOption(RAM_SEED)) {
			int baseSeed = Integer.parseInt(line.getOptionValue(RAM_SEED));
			logger.info(RAM_SEED + " = " + baseSeed);
			builder.setSeed(baseSeed);
		}

		if (line.hasOption(OUTPUT_FILE_PATH)) {
			File outputFile = FileUtil.createFile(line.getOptionValue(OUTPUT_FILE_PATH));
			logger.info(OUTPUT_FILE_PATH + " = " + line.getOptionValue(OUTPUT_FILE_PATH));
			builder.setOutputFile(outputFile);
		}

		if (line.hasOption(CLASS_CONSTRAINT_SELECTION_PROBABILITY)) {
			double classConstraintSelectionProbability = Double
					.parseDouble(line.getOptionValue(CLASS_CONSTRAINT_SELECTION_PROBABILITY));
			if (classConstraintSelectionProbability < 0.0 || classConstraintSelectionProbability > 1.0) {
				logger.error("Class constraint selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(CLASS_CONSTRAINT_SELECTION_PROBABILITY + " = " + classConstraintSelectionProbability);
			builder.setClassConstraintSelectionProbability(classConstraintSelectionProbability);
		}

		if (line.hasOption(NEW_INDIVIDUAL_PROBABILITY)) {
			double newIndividualProbability = Double.parseDouble(line.getOptionValue(NEW_INDIVIDUAL_PROBABILITY));
			if (newIndividualProbability < 0.0 || newIndividualProbability > 1.0) {
				logger.error("New individual probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(NEW_INDIVIDUAL_PROBABILITY + " = " + newIndividualProbability);
			builder.setNewIndividualProbability(newIndividualProbability);
		}

		if (line.hasOption(CLASS_ASSERTION_PROBABILITY)) {
			double classAssertionProbability = Double.parseDouble(line.getOptionValue(CLASS_ASSERTION_PROBABILITY));
			if (classAssertionProbability < 0.0 || classAssertionProbability > 1.0) {
				logger.error("Class assertion probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(CLASS_ASSERTION_PROBABILITY + " = " + classAssertionProbability);
			builder.setClassAssertionProbability(classAssertionProbability);
		}

		if (line.hasOption(OBJECT_PROPERTY_ASSERTION_PROBABILITY)) {
			double objectPropertyAssertionProbability = Double
					.parseDouble(line.getOptionValue(OBJECT_PROPERTY_ASSERTION_PROBABILITY));
			if (objectPropertyAssertionProbability < 0.0 || objectPropertyAssertionProbability > 1.0) {
				logger.error("Object property assertion probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(OBJECT_PROPERTY_ASSERTION_PROBABILITY + " = " + objectPropertyAssertionProbability);
			builder.setObjectPropertyAssertionProbability(objectPropertyAssertionProbability);
		}

		if (line.hasOption(DATA_PROPERTY_ASSERTION_PROBABILITY)) {
			double dataPropertyAssertionProbability = Double
					.parseDouble(line.getOptionValue(DATA_PROPERTY_ASSERTION_PROBABILITY));
			if (dataPropertyAssertionProbability < 0.0 || dataPropertyAssertionProbability > 1.0) {
				logger.error("Data property assertion probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(DATA_PROPERTY_ASSERTION_PROBABILITY + " = " + dataPropertyAssertionProbability);
			builder.setDataPropertyAssertionProbability(dataPropertyAssertionProbability);
		}

		if (line.hasOption(SUPER_CLASS_SELECTION_PROBABILITY)) {
			double superClassSelectionProbability = Double
					.parseDouble(line.getOptionValue(SUPER_CLASS_SELECTION_PROBABILITY));
			if (superClassSelectionProbability < 0.0 || superClassSelectionProbability > 1.0) {
				logger.error("Super class selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(SUPER_CLASS_SELECTION_PROBABILITY + " = " + superClassSelectionProbability);
			builder.setSuperClassSelectionProbability(superClassSelectionProbability);
		}

		if (line.hasOption(EQUIVALENT_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double equivalentObjectPropertySelectionProbability = Double
					.parseDouble(line.getOptionValue(EQUIVALENT_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (equivalentObjectPropertySelectionProbability < 0.0
					|| equivalentObjectPropertySelectionProbability > 1.0) {
				logger.error("Equivalent object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(EQUIVALENT_OBJECT_PROPERTY_SELECTION_PROBABILITY + " = "
					+ equivalentObjectPropertySelectionProbability);
			builder.setEquivalentObjectPropertySelectionProbability(equivalentObjectPropertySelectionProbability);
		}

		if (line.hasOption(EQUIVALENT_DATA_PROPERTY_SELECTION_PROBABILITY)) {
			double equivalentDataPropertySelectionProbability = Double
					.parseDouble(line.getOptionValue(EQUIVALENT_DATA_PROPERTY_SELECTION_PROBABILITY));
			if (equivalentDataPropertySelectionProbability < 0.0 || equivalentDataPropertySelectionProbability > 1.0) {
				logger.error("Equivalent data property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(EQUIVALENT_DATA_PROPERTY_SELECTION_PROBABILITY + " = "
					+ equivalentDataPropertySelectionProbability);
			builder.setEquivalentDataPropertySelectionProbability(equivalentDataPropertySelectionProbability);
		}

		if (line.hasOption(DISJOINT_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double disjointObjectPropertySelectionProbability = Double
					.parseDouble(line.getOptionValue(DISJOINT_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (disjointObjectPropertySelectionProbability < 0.0 || disjointObjectPropertySelectionProbability > 1.0) {
				logger.error("Disjoint object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(DISJOINT_OBJECT_PROPERTY_SELECTION_PROBABILITY + " = "
					+ disjointObjectPropertySelectionProbability);
			builder.setDisjointObjectPropertySelectionProbability(disjointObjectPropertySelectionProbability);
		}

		if (line.hasOption(DISJOINT_DATA_PROPERTY_SELECTION_PROBABILITY)) {
			double disjointDataPropertySelectionProbability = Double
					.parseDouble(line.getOptionValue(DISJOINT_DATA_PROPERTY_SELECTION_PROBABILITY));
			if (disjointDataPropertySelectionProbability < 0.0 || disjointDataPropertySelectionProbability > 1.0) {
				logger.error("Disjoint data property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(
					DISJOINT_DATA_PROPERTY_SELECTION_PROBABILITY + " = " + disjointDataPropertySelectionProbability);
			builder.setDisjointDataPropertySelectionProbability(disjointDataPropertySelectionProbability);
		}

		if (line.hasOption(SUPER_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double superObjectPropertySelectionProbability = Double
					.parseDouble(line.getOptionValue(SUPER_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (superObjectPropertySelectionProbability < 0.0 || superObjectPropertySelectionProbability > 1.0) {
				logger.error("Super object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(SUPER_OBJECT_PROPERTY_SELECTION_PROBABILITY + " = " + superObjectPropertySelectionProbability);
			builder.setSuperObjectPropertySelectionProbability(superObjectPropertySelectionProbability);
		}

		if (line.hasOption(SUPER_DATA_PROPERTY_SELECTION_PROBABILITY)) {
			double superDataPropertySelectionProbability = Double
					.parseDouble(line.getOptionValue(SUPER_DATA_PROPERTY_SELECTION_PROBABILITY));
			if (superDataPropertySelectionProbability < 0.0 || superDataPropertySelectionProbability > 1.0) {
				logger.error("Super data property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(SUPER_DATA_PROPERTY_SELECTION_PROBABILITY + " = " + superDataPropertySelectionProbability);
			builder.setSuperDataPropertySelectionProbability(superDataPropertySelectionProbability);
		}

		if (line.hasOption(INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double inverseObjectPropertySelectionProbability = Double
					.parseDouble(line.getOptionValue(INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (inverseObjectPropertySelectionProbability < 0.0 || inverseObjectPropertySelectionProbability > 1.0) {
				logger.error("Inverse object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(
					INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY + " = " + inverseObjectPropertySelectionProbability);
			builder.setInverseObjectPropertySelectionProbability(inverseObjectPropertySelectionProbability);
		}

		if (line.hasOption(SYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double symmetricObjectPropertySelectionProbability = Double
					.parseDouble(line.getOptionValue(SYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (symmetricObjectPropertySelectionProbability < 0.0
					|| symmetricObjectPropertySelectionProbability > 1.0) {
				logger.error("Symmetric object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(SYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY + " = "
					+ symmetricObjectPropertySelectionProbability);
			builder.setSymmetricObjectPropertySelectionProbability(symmetricObjectPropertySelectionProbability);
		}

		if (line.hasOption(ASYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double asymmetricObjectPropertySelectionProbability = Double
					.parseDouble(line.getOptionValue(ASYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (asymmetricObjectPropertySelectionProbability < 0.0
					|| asymmetricObjectPropertySelectionProbability > 1.0) {
				logger.error("Asymmetric object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(ASYMMETRIC_OBJECT_PROPERTY_SELECTION_PROBABILITY + " = "
					+ asymmetricObjectPropertySelectionProbability);
			builder.setAsymmetricObjectPropertySelectionProbability(asymmetricObjectPropertySelectionProbability);
		}

		if (line.hasOption(IRREFLEXIVE_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double irreflexiveObjectPropertySelectionProbability = Double
					.parseDouble(line.getOptionValue(IRREFLEXIVE_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (irreflexiveObjectPropertySelectionProbability < 0.0
					|| irreflexiveObjectPropertySelectionProbability > 1.0) {
				logger.error("Irreflexive object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(IRREFLEXIVE_OBJECT_PROPERTY_SELECTION_PROBABILITY + " = "
					+ irreflexiveObjectPropertySelectionProbability);
			builder.setIrreflexiveObjectPropertySelectionProbability(irreflexiveObjectPropertySelectionProbability);
		}

		builder.build().generateObjectDescriptions();
	}

	/**
	 * Create Option objects from option names.
	 * 
	 * @return Option objects.
	 */
	private Options createOptions() {
		Options options = new Options();
		options.addOption(Option.builder(HELP_OPTION_NAME).desc("Views this help text").build());

		Option opt = Option.builder(ROOT_CLASS_IRI).argName("IRI").hasArg().desc("root class IRI as string").build();
		opt.setRequired(true);
		options.addOption(opt);

		opt = Option.builder(ONTOLOGY_URI).argName("URI").hasArg()
				.desc("URI that relates to the background knowledge (TBox)").build();
		opt.setRequired(true);
		options.addOption(opt);

		options.addOption(Option.builder(MAPPING).argName("IRIMapping1,IRIMapping2,IRIMapping3...").hasArg().desc(
				"Ontology IRIs to document IRIs mapping. Mappings are splitted by semicolon whereas ontology IRI and document IRI within each mapping are splitted by comma.")
				.build());
		options.addOption(Option.builder(OBJ_NUMBER).argName("NUMBER").hasArg()
				.desc("The number of object descriptions; 1 by default").build());
		options.addOption(Option.builder(RAM_SEED).argName("SEED").hasArg()
				.desc("Random seed used for random object description generation; 0 by default").build());
		options.addOption(Option.builder(OUTPUT_FILE_PATH).argName("PATH").hasArg().desc(
				"The local path to the output RDF object descriptions; ./instancedata/ObjectDescription<NUMBER>.rdf by default")
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
		helpFormatter.printHelp("java -jar objectdescriptiongenerator-1.0-SNAPSHOT.jar", options);
	}

}
