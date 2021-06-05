package edu.neu.ece.objectdescriptiongenerator.controller;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.objectdescriptiongenerator.evaluator.SpaceCoverageEvaluator;
import edu.neu.ece.objectdescriptiongenerator.extractor.OntologyExtractor;
import edu.neu.ece.objectdescriptiongenerator.generator.ObjectDescriptionGenerator;
import edu.neu.ece.objectdescriptiongenerator.utility.FileUtil;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyIRIMapperImpl;

/**
 * This class is used for controlling the whole object description generation
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
	 * Ontology root class IRI as string.
	 */
	private String rootIRIString;

	/**
	 * Input ontology (TBox) IRI.
	 */
	private IRI ontologyIRI;

	/**
	 * Mapping ontology IRIs to document IRIs; null by default.
	 */
	private OWLOntologyIRIMapperImpl IRIMapper;

	/**
	 * The number of object descriptions; 1 by default.
	 */
	private int objNumber;

	/**
	 * Random seed for generating randomized object descriptions; 1 by default.
	 */
	private long seed;

	/**
	 * The generated object descriptions as file.
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
	 * individual; 1.0 by default.
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
	 * Inner static Builder class.
	 */
	public static class Builder {
		// Required parameters
		/**
		 * Ontology root class IRI as string.
		 */
		private final String rootIRIString;

		/**
		 * Input ontology IRI.
		 */
		private final IRI ontologyIRI;

		// Optional parameters - initialized to default values
		/**
		 * Mapping ontology IRIs to document IRIs; null by default.
		 */
		private OWLOntologyIRIMapperImpl IRIMapper = null;

		/**
		 * The number of object descriptions; 1 by default.
		 */
		private int objNumber = 1;

		/**
		 * Random seed for generating randomized object descriptions; 1 by default.
		 */
		private long seed = objNumber;

		/**
		 * The generated object descriptions as file.
		 */
		private File outputFile = FileUtil
				.createFile("instancedata" + File.separator + "ObjectDescription" + objNumber + ".rdf");

		/**
		 * The probability of selecting an OWL class constraint (anonymous super class
		 * expression) of an OWL class; 0.9 by default.
		 */
		private double classConstraintSelectionProbability = 0.9;

		/**
		 * The probability of creating an OWL named individual; 0.5 by default.
		 */
		private double newIndividualProbability = 0.5;

		/**
		 * The probability of generating class assertion axioms for each named
		 * individual; 1.0 by default.
		 */
		private double classAssertionProbability = 1.0;

		/**
		 * The probability of creating an object property assertion axiom; 0.5 by
		 * default.
		 */
		private double objectPropertyAssertionProbability = 0.5;

		/**
		 * The probability of creating a data property assertion axiom; 0.5 by default.
		 */
		private double dataPropertyAssertionProbability = 0.5;

		/**
		 * The probability of selecting a super class (direct or inferred) of a class
		 * for class assertion generation; 0.5 by default.
		 */
		private double superClassSelectionProbability = 0.5;

		/**
		 * The probability of selecting an equivalent object property (direct or
		 * inferred) of an object property for object property assertion generation; 0.5
		 * by default.
		 */
		private double equivalentObjectPropertySelectionProbability = 0.5;

		/**
		 * The probability of selecting an equivalent data property (direct or inferred)
		 * of a data property for data property assertion generation; 0.5 by default.
		 */
		private double equivalentDataPropertySelectionProbability = 0.5;

		/**
		 * The probability of selecting a disjoint object property (direct or inferred)
		 * of an object property for negative object property assertion generation; 0.8
		 * by default.
		 */
		private double disjointObjectPropertySelectionProbability = 0.8;

		/**
		 * The probability of selecting a disjoint data property (directed or inferred)
		 * of a data property for negative data property assertion generation; 0.8 by
		 * default.
		 */
		private double disjointDataPropertySelectionProbability = 0.8;

		/**
		 * The probability of selecting a super object property (directed or inferred)
		 * of an object property for object property assertion generation; 0.5 by
		 * default.
		 */
		private double superObjectPropertySelectionProbability = 0.5;

		/**
		 * The probability of selecting a super data property (directed or inferred) of
		 * a data property for data property assertion generation; 0.5 by default.
		 */
		private double superDataPropertySelectionProbability = 0.5;

		/**
		 * The probability of selecting an inverse property (direct or inferred) of an
		 * object property for object property assertion generation; 0.8 by default.
		 */
		private double inverseObjectPropertySelectionProbability = 0.8;

		/**
		 * The probability of selecting symmetric characteristic of an object property
		 * for object property assertion generation; 0.8 by default.
		 */
		private double symmetricObjectPropertySelectionProbability = 0.8;

		/**
		 * The probability of selecting asymmetric characteristic of an object property
		 * for negative object property assertion generation; 0.8 by default.
		 */
		private double asymmetricObjectPropertySelectionProbability = 0.8;

		/**
		 * The probability of selecting irreflexive characteristic of an object property
		 * for negative object property assertion generation; 0.8 by default.
		 */
		private double irreflexiveObjectPropertySelectionProbability = 0.8;

		/**
		 * Constructor.
		 * 
		 * @param ontologyIRI
		 *            Input ontology IRI.
		 * @param rootIRIString
		 *            Ontology root class IRI as string.
		 */
		public Builder(IRI ontologyIRI, String rootIRIString) {
			this.ontologyIRI = ontologyIRI;
			this.rootIRIString = rootIRIString;
		}

		/**
		 * Set ontology IRI mapper.
		 * 
		 * @param mapper
		 *            Ontology IRI mapper.
		 * @return Current Builder object.
		 * @throws URISyntaxException
		 *             If document URI creation fails.
		 */
		public Builder setOntologyIRIMapper(Map<String, String> mapper) throws URISyntaxException {
			IRIMapper = new OWLOntologyIRIMapperImpl();
			for (Entry<String, String> entry : mapper.entrySet())
				IRIMapper.addMapping(IRI.create(entry.getKey()), IRI.create(new URI(entry.getValue())));
			return this;
		}

		/**
		 * Set the number of object descriptions.
		 * 
		 * @param objNumber
		 *            The number of object descriptions.
		 * @return Current Builder object.
		 */
		public Builder setObjNumber(int objNumber) {
			this.objNumber = objNumber;
			outputFile = FileUtil
					.createFile("instancedata" + File.separator + "ObjectDescription" + this.objNumber + ".rdf");
			this.seed = this.objNumber;
			return this;
		}

		/**
		 * Set the random seed for generating randomized object descriptions.
		 * 
		 * @param baseSeed
		 *            Random base seed.
		 * @return Current Builder object.
		 */
		public Builder setSeed(int baseSeed) {
			long seed = baseSeed * (Integer.MAX_VALUE + 1) + objNumber;
			this.seed = seed;
			return this;
		}

		/**
		 * Set output ontology file.
		 * 
		 * @param outputFile
		 *            Output ontology file.
		 * @return Current Builder object.
		 */
		public Builder setOutputFile(File outputFile) {
			this.outputFile = outputFile;
			return this;
		}

		/**
		 * Set the probability of selecting an OWL class constraint (anonymous super
		 * class expression) of an OWL class
		 * 
		 * @param classConstraintSelectionProbability
		 *            The probability of selecting an OWL class constraint (anonymous
		 *            super class expression) of an OWL class.
		 * @return Current Builder object.
		 */
		public Builder setClassConstraintSelectionProbability(double classConstraintSelectionProbability) {
			this.classConstraintSelectionProbability = classConstraintSelectionProbability;
			return this;
		}

		/**
		 * Set the probability of creating an OWL named individual.
		 * 
		 * @param newIndividualProbability
		 *            The probability of creating an OWL named individual.
		 * @return Current Builder object.
		 */
		public Builder setNewIndividualProbability(double newIndividualProbability) {
			this.newIndividualProbability = newIndividualProbability;
			return this;
		}

		/**
		 * Set the probability of generating class assertion axioms for each named
		 * individual.
		 * 
		 * @param classAssertionProbability
		 *            The probability of generating class assertion axioms for each
		 *            named individual.
		 * @return Current Builder object.
		 */
		public Builder setClassAssertionProbability(double classAssertionProbability) {
			this.classAssertionProbability = classAssertionProbability;
			return this;
		}

		/**
		 * Set the probability of creating an object property assertion axiom.
		 * 
		 * @param objectPropertyAssertionProbability
		 *            The probability of creating an object property assertion axiom.
		 * @return Current Builder object.
		 */
		public Builder setObjectPropertyAssertionProbability(double objectPropertyAssertionProbability) {
			this.objectPropertyAssertionProbability = objectPropertyAssertionProbability;
			return this;
		}

		/**
		 * Set the probability of creating a data property assertion axiom.
		 * 
		 * @param dataPropertyAssertionProbability
		 *            The probability of creating a data property assertion axiom.
		 * @return Current Builder object.
		 */
		public Builder setDataPropertyAssertionProbability(double dataPropertyAssertionProbability) {
			this.dataPropertyAssertionProbability = dataPropertyAssertionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting a super class (direct or inferred) of a
		 * class for class assertion generation
		 * 
		 * @param superClassSelectionProbability
		 *            The probability of selecting a super class (direct or inferred) of
		 *            a class for class assertion generation.
		 * @return Current Builder object.
		 */
		public Builder setSuperClassSelectionProbability(double superClassSelectionProbability) {
			this.superClassSelectionProbability = superClassSelectionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting an equivalent object property (direct or
		 * inferred) of an object property for object property assertion generation
		 * 
		 * @param equivalentObjectPropertySelectionProbability
		 *            The probability of selecting an equivalent object property (direct
		 *            or inferred) of an object property for object property assertion
		 *            generation.
		 * @return Current Builder object.
		 */
		public Builder setEquivalentObjectPropertySelectionProbability(
				double equivalentObjectPropertySelectionProbability) {
			this.equivalentObjectPropertySelectionProbability = equivalentObjectPropertySelectionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting an equivalent data property (direct or
		 * inferred) of a data property for data property assertion generation.
		 * 
		 * @param equivalentDataPropertySelectionProbability
		 *            The probability of selecting an equivalent data property (direct
		 *            or inferred) of a data property for data property assertion
		 *            generation.
		 * @return Current Builder object.
		 */
		public Builder setEquivalentDataPropertySelectionProbability(
				double equivalentDataPropertySelectionProbability) {
			this.equivalentDataPropertySelectionProbability = equivalentDataPropertySelectionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting a disjoint object property (direct or
		 * inferred) of an object property for negative object property assertion
		 * generation.
		 * 
		 * @param disjointObjectPropertySelectionProbability
		 *            The probability of selecting a disjoint object property (direct or
		 *            inferred) of an object property for negative object property
		 *            assertion generation.
		 * @return Current Builder object.
		 */
		public Builder setDisjointObjectPropertySelectionProbability(
				double disjointObjectPropertySelectionProbability) {
			this.disjointObjectPropertySelectionProbability = disjointObjectPropertySelectionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting a disjoint data property (direct or
		 * inferred) of a data property for negative data property assertion generation.
		 * 
		 * @param disjointDataPropertySelectionProbability
		 *            The probability of selecting a disjoint data property (direct or
		 *            inferred) of a data property for negative data property assertion
		 *            generation.
		 * @return Current Builder object.
		 */
		public Builder setDisjointDataPropertySelectionProbability(double disjointDataPropertySelectionProbability) {
			this.disjointDataPropertySelectionProbability = disjointDataPropertySelectionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting a super object property (directed or
		 * inferred) of an object property for object property assertion generation.
		 * 
		 * @param superObjectPropertySelectionProbability
		 *            The probability of selecting a super object property (directed or
		 *            inferred) of an object property for object property assertion
		 *            generation.
		 * @return Current Builder object.
		 */
		public Builder setSuperObjectPropertySelectionProbability(double superObjectPropertySelectionProbability) {
			this.superObjectPropertySelectionProbability = superObjectPropertySelectionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting a super data property (directed or inferred)
		 * of a data property for data property assertion generation
		 * 
		 * @param superDataPropertySelectionProbability
		 *            The probability of selecting a super data property (directed or
		 *            inferred) of a data property for data property assertion
		 *            generation.
		 * @return Current Builder object.
		 */
		public Builder setSuperDataPropertySelectionProbability(double superDataPropertySelectionProbability) {
			this.superDataPropertySelectionProbability = superDataPropertySelectionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting an inverse property (direct or inferred) of
		 * an object property for object property assertion generation
		 * 
		 * @param inverseObjectPropertySelectionProbability
		 *            The probability of selecting an inverse property (direct or
		 *            inferred) of an object property for object property assertion
		 *            generation.
		 * @return Current Builder object.
		 */
		public Builder setInverseObjectPropertySelectionProbability(double inverseObjectPropertySelectionProbability) {
			this.inverseObjectPropertySelectionProbability = inverseObjectPropertySelectionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting symmetric characteristic of an object
		 * property for object property assertion generation.
		 * 
		 * @param symmetricObjectPropertySelectionProbability
		 *            The probability of selecting symmetric characteristic of an object
		 *            property for object property assertion generation.
		 * @return Current Builder object.
		 */
		public Builder setSymmetricObjectPropertySelectionProbability(
				double symmetricObjectPropertySelectionProbability) {
			this.symmetricObjectPropertySelectionProbability = symmetricObjectPropertySelectionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting asymmetric characteristic of an object
		 * property for negative object property assertion generation.
		 * 
		 * @param asymmetricObjectPropertySelectionProbability
		 *            The probability of selecting asymmetric characteristic of an
		 *            object property for negative object property assertion generation.
		 * @return Current Builder object.
		 */
		public Builder setAsymmetricObjectPropertySelectionProbability(
				double asymmetricObjectPropertySelectionProbability) {
			this.asymmetricObjectPropertySelectionProbability = asymmetricObjectPropertySelectionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting irreflexive characteristic of an object
		 * property for negative object property assertion generation.
		 * 
		 * @param irreflexiveObjectPropertySelectionProbability
		 *            The probability of selecting irreflexive characteristic of an
		 *            object property for negative object property assertion generation.
		 * @return Current Builder object.
		 */
		public Builder setIrreflexiveObjectPropertySelectionProbability(
				double irreflexiveObjectPropertySelectionProbability) {
			this.irreflexiveObjectPropertySelectionProbability = irreflexiveObjectPropertySelectionProbability;
			return this;
		}

		/**
		 * Create an instance of Controller with Builder.
		 * 
		 * @return An instance of Controller.
		 */
		public Controller build() {
			return new Controller(this);
		}
	}

	/**
	 * Private constructor.
	 * 
	 * @param builder
	 *            Builder that instantiates Controller using Builder design pattern.
	 */
	private Controller(Builder builder) {
		this.ontologyIRI = builder.ontologyIRI;
		this.rootIRIString = builder.rootIRIString;
		this.IRIMapper = builder.IRIMapper;
		this.objNumber = builder.objNumber;
		this.seed = builder.seed;
		this.outputFile = builder.outputFile;
		this.classConstraintSelectionProbability = builder.classConstraintSelectionProbability;
		this.newIndividualProbability = builder.newIndividualProbability;
		this.classAssertionProbability = builder.classAssertionProbability;
		this.objectPropertyAssertionProbability = builder.objectPropertyAssertionProbability;
		this.dataPropertyAssertionProbability = builder.dataPropertyAssertionProbability;
		this.superClassSelectionProbability = builder.superClassSelectionProbability;
		this.equivalentObjectPropertySelectionProbability = builder.equivalentObjectPropertySelectionProbability;
		this.equivalentDataPropertySelectionProbability = builder.equivalentDataPropertySelectionProbability;
		this.disjointObjectPropertySelectionProbability = builder.disjointObjectPropertySelectionProbability;
		this.disjointDataPropertySelectionProbability = builder.disjointDataPropertySelectionProbability;
		this.superObjectPropertySelectionProbability = builder.superObjectPropertySelectionProbability;
		this.superDataPropertySelectionProbability = builder.superDataPropertySelectionProbability;
		this.inverseObjectPropertySelectionProbability = builder.inverseObjectPropertySelectionProbability;
		this.symmetricObjectPropertySelectionProbability = builder.symmetricObjectPropertySelectionProbability;
		this.asymmetricObjectPropertySelectionProbability = builder.asymmetricObjectPropertySelectionProbability;
		this.irreflexiveObjectPropertySelectionProbability = builder.irreflexiveObjectPropertySelectionProbability;
	}

	/**
	 * This function defines the whole control flow of object description generation
	 * process.
	 */
	public void generateObjectDescriptions() {
		try {
			loadOntology();
			OntologyExtractor extractor = new OntologyExtractor(ont, reasoner);
			extractor.extract();
			long timeStart = System.currentTimeMillis();
			ObjectDescriptionGenerator generator = new ObjectDescriptionGenerator(rootIRIString, objNumber, seed,
					outputFile, classConstraintSelectionProbability, newIndividualProbability,
					classAssertionProbability, objectPropertyAssertionProbability, dataPropertyAssertionProbability,
					superClassSelectionProbability, equivalentObjectPropertySelectionProbability,
					equivalentDataPropertySelectionProbability, disjointObjectPropertySelectionProbability,
					disjointDataPropertySelectionProbability, superObjectPropertySelectionProbability,
					superDataPropertySelectionProbability, inverseObjectPropertySelectionProbability,
					symmetricObjectPropertySelectionProbability, asymmetricObjectPropertySelectionProbability,
					irreflexiveObjectPropertySelectionProbability, manager, ont, extractor);
			generator.generateRandomRDFObjectDescriptionInstances();
			long totalTime = System.currentTimeMillis() - timeStart;
			logger.info("The time for generating " + objNumber + " object descripitons is: " + totalTime + " ms.");
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

		if (IRIMapper != null)
			manager.getIRIMappers().add(IRIMapper);
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
			logger.info("Ontology IRI is anonymous. Use loaded document URI instead.");
			// logger.error("No ontolgoy IRI : The loaded ontology doesn't have an ontology
			// IRI");
			// throw new OWLOntologyCreationException();
		}
		logger.info("Ontologies loaded successfully! Main Ontology: " + logOntoName);
	}
}
