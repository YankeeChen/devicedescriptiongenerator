package edu.neu.ece.devicedescriptiongenerator.controller;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.devicedescriptiongenerator.evaluator.SpaceCoverageEvaluator;
import edu.neu.ece.devicedescriptiongenerator.extractor.OntologyExtractor;
import edu.neu.ece.devicedescriptiongenerator.generator.DeviceDescriptionGenerator;

/**
 * This class is used for controlling the whole device description generation
 * process, including ontology loading, ontology extraction and dataset
 * generation.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-28
 */
public class Controller {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Input ontology IRI.
	 */
	private IRI ontologyIRI;

	/**
	 * Input ontology file.
	 */
	private File inputFile;

	/**
	 * Ontology root class IRI as string.
	 */
	private String rootIRIString;

	/**
	 * The number of device descriptions; 1 by default.
	 */
	private int devNumber;

	/**
	 * Random seed for generating randomized device descriptions; 0 by default.
	 */
	private long seed;

	/**
	 * The generated device descriptions as file.
	 */
	private File outputFile;

	/**
	 * The probability of selecting an OWL class constraint (anonymous super class
	 * expression) of an OWL class; 0.9 by default.
	 */
	private final double classConstraintSelectionProbability;

	/**
	 * The probability of creating an OWL named individual; 0.5 by default.
	 */
	private final double newIndividualProbability;

	/**
	 * The probability of generating class assertion axioms for each named
	 * individual; 0.5 by default.
	 */
	private final double classAssertionProbability;

	/**
	 * The probability of creating an object property assertion axiom; 0.5 by
	 * default.
	 */
	private final double objectPropertyAssertionProbability;

	/**
	 * The probability of creating a data property assertion axiom; 0.5 by default.
	 */
	private final double dataPropertyAssertionProbability;

	/**
	 * The probability of selecting a super class (direct or inferred) of a class
	 * for class assertion generation; 0.5 by default.
	 */
	private final double superClassSelectionProbability;

	/**
	 * The probability of selecting an equivalent object property (direct or
	 * inferred) of an object property for object property assertion generation; 0.5
	 * by default.
	 */
	private final double equivalentObjectPropertySelectionProbability;

	/**
	 * The probability of selecting an equivalent data property (direct or inferred)
	 * of a data property for data property assertion generation; 0.5 by default.
	 */
	private final double equivalentDataPropertySelectionProbability;

	/**
	 * The probability of selecting a disjoint object property (direct or inferred)
	 * of an object property for negative object property assertion generation; 0.8
	 * by default.
	 */
	private final double disjointObjectPropertySelectionProbability;

	/**
	 * The probability of selecting a disjoint data property (directed or inferred)
	 * of a data property for negative data property assertion generation; 0.8 by
	 * default.
	 */
	private final double disjointDataPropertySelectionProbability;

	/**
	 * The probability of selecting a super object property (directed or inferred)
	 * of an object property for object property assertion generation; 0.5 by
	 * default.
	 */
	private final double superObjectPropertySelectionProbability;

	/**
	 * The probability of selecting a super data property (directed or inferred) of
	 * a data property for data property assertion generation; 0.5 by default.
	 */
	private final double superDataPropertySelectionProbability;

	/**
	 * The probability of selecting an inverse property (direct or inferred) of an
	 * object property for object property assertion generation; 0.8 by default.
	 */
	private final double inverseObjectPropertySelectionProbability;

	/**
	 * The probability of selecting symmetric characteristic of an object property
	 * for object property assertion generation; 0.8 by default.
	 */
	private final double symmetricObjectPropertySelectionProbability;

	/**
	 * The probability of selecting asymmetric characteristic of an object property
	 * for negative object property assertion generation; 0.8 by default.
	 */
	private final double asymmetricObjectPropertySelectionProbability;

	/**
	 * The probability of selecting irreflexive characteristic of an object property
	 * for negative object property assertion generation; 0.8 by default.
	 */
	private final double irreflexiveObjectPropertySelectionProbability;

	/**
	 * Hold of an ontology manager.
	 */
	private OWLOntologyManager manager = null;

	/**
	 * Hold of input ontology.
	 */
	private OWLOntology ont = null;

	/**
	 * A build-in OWL reasoner from OWL API.
	 */
	private OWLReasoner reasoner = null;

	/**
	 * Constructor
	 * 
	 * @param ontologyIRI
	 *            Input ontology IRI.
	 * @param inputFile
	 *            Input ontology file.
	 * @param rootIRIString
	 *            Ontology root class IRI as string.
	 * @param devNumber
	 *            The number of device descriptions.
	 * @param seed
	 *            Random seed for generating randomized device descriptions.
	 * @param outputFile
	 *            The generated device description datasets.
	 * @param classConstraintSelectionProbability
	 *            The probability of selecting an OWL class constraint (anonymous
	 *            class expression) of an OWL named class.
	 * @param newIndividualProbability
	 *            The probability of creating a new OWL named individual.
	 * @param classAssertionProbability
	 *            The probability of generating class assertion axiom for each named
	 *            individual.
	 * @param objectPropertyAssertionProbability
	 *            The probability of creating a new object property assertion axiom.
	 * @param dataPropertyAssertionProbability
	 *            The probability of creating a new data property assertion axiom.
	 * @param superClassSelectionProbability
	 *            The probability of selecting a super class (direct or inferred) of
	 *            a class for class assertion generation.
	 * @param equivalentObjectPropertySelectionProbability
	 *            The probability of selecting an equivalent object property (direct
	 *            or inferred) of an object property for object property assertion
	 *            generation.
	 * @param equivalentDataPropertySelectionProbability
	 *            The probability of selecting an equivalent data property (direct
	 *            or inferred) of a data property for data property assertion
	 *            generation.
	 * @param disjointObjectPropertySelectionProbability
	 *            The probability of selecting a disjoint object property (direct or
	 *            inferred) of an object property for negative object property
	 *            assertion generation.
	 * @param disjointDataPropertySelectionProbability
	 *            The probability of selecting a disjoint data property (direct or
	 *            inferred) of a data property for negative object property
	 *            assertion generation.
	 * @param superObjectPropertySelectionProbability
	 *            The probability of selecting a super object property (direct or
	 *            inferred) of an object property for object property assertion
	 *            generation.
	 * @param superDataPropertySelectionProbability
	 *            The probability of selecting a super data property (direct or
	 *            inferred) of a data property for data property assertion
	 *            generation.
	 * @param inverseObjectPropertySelectionProbability
	 *            The probability of selecting an inverse property (direct or
	 *            inferred) of an object property for object property assertion
	 *            generation.
	 * @param symmetricObjectPropertySelectionProbability
	 *            The probability of selecting symmetric characteristic of an object
	 *            property for object property assertion generation.
	 * @param asymmetricObjectPropertySelectionProbability
	 *            The probability of selecting asymmetric characteristic of an
	 *            object property for negative object property assertion generation.
	 * @param irreflexiveObjectPropertySelectionProbability
	 *            The probability of selecting irreflexive characteristic of an
	 *            object property for negative object property assertion generation.
	 */
	public Controller(IRI ontologyIRI, File inputFile, String rootIRIString, int devNumber, long seed, File outputFile,
			double classConstraintSelectionProbability, double newIndividualProbability,
			double classAssertionProbability, double objectPropertyAssertionProbability,
			double dataPropertyAssertionProbability, double superClassSelectionProbability,
			double equivalentObjectPropertySelectionProbability, double equivalentDataPropertySelectionProbability,
			double disjointObjectPropertySelectionProbability, double disjointDataPropertySelectionProbability,
			double superObjectPropertySelectionProbability, double superDataPropertySelectionProbability,
			double inverseObjectPropertySelectionProbability, double symmetricObjectPropertySelectionProbability,
			double asymmetricObjectPropertySelectionProbability, double irreflexiveObjectPropertySelectionProbability) {
		this.ontologyIRI = ontologyIRI;
		this.inputFile = inputFile;
		this.rootIRIString = rootIRIString;
		this.devNumber = devNumber;
		this.seed = seed;
		this.outputFile = outputFile;
		this.classConstraintSelectionProbability = classConstraintSelectionProbability;
		this.newIndividualProbability = newIndividualProbability;
		this.classAssertionProbability = classAssertionProbability;
		this.objectPropertyAssertionProbability = objectPropertyAssertionProbability;
		this.dataPropertyAssertionProbability = dataPropertyAssertionProbability;
		this.superClassSelectionProbability = superClassSelectionProbability;
		this.equivalentObjectPropertySelectionProbability = equivalentObjectPropertySelectionProbability;
		this.equivalentDataPropertySelectionProbability = equivalentDataPropertySelectionProbability;
		this.disjointObjectPropertySelectionProbability = disjointObjectPropertySelectionProbability;
		this.disjointDataPropertySelectionProbability = disjointDataPropertySelectionProbability;
		this.superObjectPropertySelectionProbability = superObjectPropertySelectionProbability;
		this.superDataPropertySelectionProbability = superDataPropertySelectionProbability;
		this.inverseObjectPropertySelectionProbability = inverseObjectPropertySelectionProbability;
		this.symmetricObjectPropertySelectionProbability = symmetricObjectPropertySelectionProbability;
		this.asymmetricObjectPropertySelectionProbability = asymmetricObjectPropertySelectionProbability;
		this.irreflexiveObjectPropertySelectionProbability = irreflexiveObjectPropertySelectionProbability;
	}

	/**
	 * This function defines the whole control flow of device description generation
	 * process.
	 */
	public void generateDeviceDescriptions() {
		try {
			loadOntology();
			OntologyExtractor extractor = new OntologyExtractor(ont, reasoner);
			extractor.extract();
			long timeStart = System.currentTimeMillis();
			DeviceDescriptionGenerator generator = new DeviceDescriptionGenerator(rootIRIString, devNumber, seed,
					outputFile, classConstraintSelectionProbability, newIndividualProbability,
					classAssertionProbability, objectPropertyAssertionProbability, dataPropertyAssertionProbability,
					superClassSelectionProbability, equivalentObjectPropertySelectionProbability,
					equivalentDataPropertySelectionProbability, disjointObjectPropertySelectionProbability,
					disjointDataPropertySelectionProbability, superObjectPropertySelectionProbability,
					superDataPropertySelectionProbability, inverseObjectPropertySelectionProbability,
					symmetricObjectPropertySelectionProbability, asymmetricObjectPropertySelectionProbability,
					irreflexiveObjectPropertySelectionProbability, manager, ont, extractor);
			generator.generateRandomRDFDeviceDescriptionInstances();
			long totalTime = System.currentTimeMillis() - timeStart;
			logger.info("The time for generating " + devNumber + " device descripitons is: " + totalTime + " ms.");
			SpaceCoverageEvaluator evaluator = new SpaceCoverageEvaluator(generator);
			evaluator.evaluateSpaceCoverage(generator.getRootClass());

		} catch (OWLOntologyCreationException e) {
			logger.error("Error : Parsing ontologies failed. Reason: " + e.getMessage());
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function loads input ontology and checks consistency of the ontology
	 * using a build-in reasoner.
	 * 
	 * @throws OWLOntologyCreationException
	 *             if failed to load input ontology.
	 */
	public void loadOntology() throws OWLOntologyCreationException {
		logger.info("Begin loading ontologies...");
		// logger.info("The absolute path of the input ontology is " +
		// inputFile.getAbsolutePath());

		manager = OWLManager.createOWLOntologyManager();
		/*
		 * if (!dependencies.isEmpty()) { for (IRI externalIRI : dependencies) {
		 * manager.loadOntology(externalIRI); }
		 * logger.info("External ontologies loaded!"); }
		 */
		if (inputFile != null) {
			manager.getIRIMappers().add(new AutoIRIMapper(inputFile.getParentFile(), true));
		}
		// else
		// manager.getIRIMappers().add(new SimpleIRIMapper(ontologyIRI, ontologyIRI));
		// manager.getIRIMappers().add(new OWLZipClosureIRIMapper(f.getParentFile()));

		ont = manager.loadOntology(ontologyIRI);

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		// OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
		// System.out.println(reasonerFactory.getReasonerName());
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);

		// Create a reasoner that will reason over our ontology and its imports
		// closure.
		// Pass in the configuration.
		reasoner = reasonerFactory.createReasoner(ont, config);

		// Ask the reasoner to do all the necessary work now
		reasoner.precomputeInferences();
		if (!reasoner.isConsistent()) {
			logger.error("Ontology inconsistency : The loaded ontologies are inconsistent");
			throw new OWLOntologyCreationException();
		}
		// If an ontology doesn't have an ontology IRI then we say that it is
		// "anonymous"
		String logOntoName;
		if (!ont.isAnonymous()) {
			logOntoName = ont.getOntologyID().getOntologyIRI().get().toString();
		} else {
			logOntoName = ontologyIRI.toURI().toString();
			// System.out.println(logOntoName);
			logger.info("Ontology IRI is anonymous. Use loaded document URI/IRI instead.");
			// logger.error("No ontolgoy IRI : The loaded ontology doesn't have an ontology
			// IRI");
			// throw new OWLOntologyCreationException();
		}
		logger.info("Ontologies loaded successfully! Main Ontology: " + logOntoName);
	}
}
