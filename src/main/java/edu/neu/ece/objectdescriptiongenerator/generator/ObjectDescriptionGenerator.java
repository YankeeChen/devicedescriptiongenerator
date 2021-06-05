package edu.neu.ece.objectdescriptiongenerator.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.AxiomAnnotations;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.objectdescriptiongenerator.entity.classes.COWLClassImpl;
import edu.neu.ece.objectdescriptiongenerator.entity.properties.COWLDataPropertyImpl;
import edu.neu.ece.objectdescriptiongenerator.entity.properties.COWLObjectPropertyImpl;
import edu.neu.ece.objectdescriptiongenerator.entity.properties.COWLPropertyImpl;
import edu.neu.ece.objectdescriptiongenerator.extractor.OntologyExtractor;
import edu.neu.ece.objectdescriptiongenerator.utility.CollectionUtil;
import edu.neu.ece.objectdescriptiongenerator.visitor.COWLClassExpressionVisitor;
import edu.neu.ece.objectdescriptiongenerator.visitor.COWLDataRangeVisitor;
import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * As one of the most critical classes of the program,
 * ObjectDescriptionGenerator defines ontology generator for generating outputs.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-01
 *
 */
public class ObjectDescriptionGenerator {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Output ontology IRI in string.
	 */
	private final String OUTPUT_ONTOLOGY_IRI_IN_STRING;

	/**
	 * The number of object descriptions; 1 by default.
	 */
	private int objNumber;

	/**
	 * Used to generate a stream of pesudorandom numbers.
	 */
	private Random ran;

	/**
	 * Generated output ontology as file.
	 */
	private File outputFile;

	/**
	 * Hold of an ontology manager.
	 */
	private OWLOntologyManager manager;

	/**
	 * A build-in OWL reasoner from OWL API.
	 */
	private OWLReasoner reasoner;

	/**
	 * An OWL data factory object used to create entities, class expressions and
	 * axioms.
	 */
	private OWLDataFactory factory;

	/**
	 * Input ontology.
	 */
	private OWLOntology inputOntology;

	/**
	 * Output ontology.
	 */
	private OWLOntology outputOntology = null;

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
	 * of a data property for data property assertion generation; 0.5 by default..
	 */
	private final double equivalentDataPropertySelectionProbability;

	/**
	 * The probability of selecting a disjoint object property (direct or inferred)
	 * of an object property for negative object property assertion generation; 0.8
	 * by default.
	 */
	private final double disjointObjectPropertySelectionProbability;

	/**
	 * The probability of selecting a disjoint data property (direct or inferred) of
	 * a data property for negative data property assertion generation; 0.8 by
	 * default.
	 */
	private final double disjointDataPropertySelectionProbability;

	/**
	 * The probability of selecting a super object property (direct or inferred) of
	 * an object property for object property assertion generation; 0.5 by default.
	 */
	private final double superObjectPropertySelectionProbability;

	/**
	 * The probability of selecting a super data property (direct or inferred) of a
	 * data property for data property assertion generation; 0.5 by default.
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
	 * Container that stores key-value pairs, where OWL API interface OWLClass is
	 * the key and the customized class COWLClassImpl is the value.
	 */
	private Map<OWLClass, COWLClassImpl> classMap;

	/**
	 * Container that stores key-value pairs, where OWL API interface
	 * OWLDataProperty is the key and the customized class COWLDataPropertyImpl is
	 * the value.
	 */
	private Map<OWLDataProperty, COWLDataPropertyImpl> dataPropertyMap;

	/**
	 * Container that stores key-value pairs, where OWL API interface
	 * OWLObjectProperty is the key and the customized class COWLObjectPropertyImpl
	 * is the value.
	 */
	private Map<OWLObjectProperty, COWLObjectPropertyImpl> objectPropertyMap;

	/**
	 * OWL named individuals of the input ontology.
	 */
	private Set<OWLNamedIndividual> existingIndividuals;

	/**
	 * Created OWL named individuals.
	 */
	private Set<OWLNamedIndividual> newIndividuals = new HashSet<>();

	/**
	 * Selected root class in the input ontology as the entry for dataset
	 * generation.
	 */
	private COWLClassImpl rootClass = null;

	/**
	 * Ontology root class IRI as string.
	 */
	private final String rootIRIString;

	/**
	 * Container that stores key-value pairs, where COWLClassImpl is the key and
	 * LinkedResource is the value.
	 */
	private Map<COWLClassImpl, LinkedResource> linkedResourcesForEachClass = new HashMap<>();

	/**
	 * Inner class of DeviceDescriptionGenerator, an instance of this class records
	 * all linked resources of the specified OWL class.
	 * 
	 * @author Yanji Chen
	 * @version 1.0
	 * @since 2018-10-01
	 */
	private class LinkedResource {

		/**
		 * A list of set of <OWLDataProperty, OWLDataRange> entries bound to the
		 * specified OWL class.
		 */
		public List<Set<Entry<OWLDataProperty, OWLDataRange>>> dataPropertyAndRangePairsList = new ArrayList<>();

		/**
		 * A list of set of <OWLObjectProperty, OWLClassExpression> entries bound to the
		 * specified OWL class.
		 */
		public List<Set<Entry<OWLObjectProperty, OWLClassExpression>>> objectPropertyAndRangePairsList = new ArrayList<>();

		/**
		 * Constructor
		 * 
		 * @param dataPropertyAndRangePairsList
		 *            A list of set of <OWLDataProperty, OWLDataRange> entries bound to
		 *            the specified OWL class.
		 * @param objectPropertyAndRangePairsList
		 *            A list of set of <OWLObjectProperty, OWLClassExpression> entries
		 *            bound to the specified OWL class.
		 */
		public LinkedResource(List<Set<Entry<OWLDataProperty, OWLDataRange>>> dataPropertyAndRangePairsList,
				List<Set<Entry<OWLObjectProperty, OWLClassExpression>>> objectPropertyAndRangePairsList) {
			this.dataPropertyAndRangePairsList = dataPropertyAndRangePairsList;
			this.objectPropertyAndRangePairsList = objectPropertyAndRangePairsList;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param rootIRIString
	 *            Ontology root class IRI as string.
	 * @param objNumber
	 *            The number of object descriptions.
	 * @param seed
	 *            Random seed for generating randomized object descriptions.
	 * @param outputFile
	 *            The output ontology as file.
	 * @param classConstraintSelectionProbability
	 *            The probability of selecting an OWL class constraint (anonymous
	 *            super class expression) of an OWL named class.
	 * @param newIndividualProbability
	 *            The probability of creating an OWL named individual.
	 * @param classAssertionProbability
	 *            The probability of generating class assertion axioms for each
	 *            named individual.
	 * @param objectPropertyAssertionProbability
	 *            The probability of creating an object property assertion axiom.
	 * @param dataPropertyAssertionProbability
	 *            The probability of creating a data property assertion axiom.
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
	 *            The probability of selecting a disjoint object property of an
	 *            object property for negative object property assertion generation.
	 * @param disjointDataPropertySelectionProbability
	 *            The probability of selecting a disjoint data property of a data
	 *            property for negative object property assertion generation.
	 * @param superObjectPropertySelectionProbability
	 *            The probability of selecting a super object property (directed or
	 *            inferred) of an object property for object property assertion
	 *            generation.
	 * @param superDataPropertySelectionProbability
	 *            The probability of selecting a super data property (directed or
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
	 * @param manager
	 *            Hold of an ontology manager.
	 * @param ont
	 *            Hold of input ontology.
	 * @param extractor
	 *            Ontology extractor used for extracting OWL axioms from input
	 *            ontology.
	 */
	public ObjectDescriptionGenerator(String rootIRIString, int objNumber, long seed, File outputFile,
			double classConstraintSelectionProbability, double newIndividualProbability,
			double classAssertionProbability, double objectPropertyAssertionProbability,
			double dataPropertyAssertionProbability, double superClassSelectionProbability,
			double equivalentObjectPropertySelectionProbability, double equivalentDataPropertySelectionProbability,
			double disjointObjectPropertySelectionProbability, double disjointDataPropertySelectionProbability,
			double superObjectPropertySelectionProbability, double superDataPropertySelectionProbability,
			double inverseObjectPropertySelectionProbability, double symmetricObjectPropertySelectionProbability,
			double asymmetricObjectPropertySelectionProbability, double irreflexiveObjectPropertySelectionProbability,
			OWLOntologyManager manager, OWLOntology ont, OntologyExtractor extractor) {

		this.objNumber = objNumber;
		OUTPUT_ONTOLOGY_IRI_IN_STRING = "http://ece.neu.edu/ontologies/ObjectDescription" + objNumber + ".owl";
		ran = new Random(seed);
		this.rootIRIString = rootIRIString;
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
		this.manager = manager;
		factory = manager.getOWLDataFactory();
		inputOntology = ont;

		classMap = extractor.getClassMap();
		dataPropertyMap = extractor.getDataPropertyMap();
		objectPropertyMap = extractor.getObjectPropertyMap();
		existingIndividuals = extractor.getExistingIndividuals();
	}

	/**
	 * Get output ontology IRI in string.
	 * 
	 * @return Ontology IRI in string.
	 */
	public String getOutputOntologyIRIInString() {
		return OUTPUT_ONTOLOGY_IRI_IN_STRING;
	}

	/**
	 * Get the number of object descriptions.
	 * 
	 * @return The number of object descriptions.
	 */
	public int getObjNumber() {
		return objNumber;
	}

	/**
	 * Get Random object.
	 * 
	 * @return Random object.
	 */
	public Random getRan() {
		return ran;
	}

	/**
	 * Get output ontology as file.
	 * 
	 * @return Output ontology as file.
	 */
	public File getOutputFile() {
		return outputFile;
	}

	/**
	 * Get the hold of ontology manager.
	 * 
	 * @return Hold of ontology manager.
	 */
	public OWLOntologyManager getManager() {
		return manager;
	}

	/**
	 * Get an OWL API build-in reasoner.
	 * 
	 * @return The OWL API build-in reasoner.
	 */
	public OWLReasoner getReasoner() {
		return reasoner;
	}

	/**
	 * Get ontology data factory used for creating entities, class expressions and
	 * axioms.
	 * 
	 * @return Ontology data factory.
	 */
	public OWLDataFactory getFactory() {
		return factory;
	}

	/**
	 * Get input ontology.
	 * 
	 * @return Input ontology.
	 */
	public OWLOntology getInputOntology() {
		return inputOntology;
	}

	/**
	 * Get output ontology.
	 * 
	 * @return Output ontology.
	 */
	public OWLOntology getOutputOntology() {
		return outputOntology;
	}

	/**
	 * Get the probability of selecting an OWL class constraint (anonymous super
	 * class expression) of an OWL class.
	 * 
	 * @return The probability of selecting an OWL class constraint (anonymous super
	 *         class expression) of an OWL named class.
	 */
	public double getClassConstraintSelectionProbability() {
		return classConstraintSelectionProbability;
	}

	/**
	 * Get the probability of creating an OWL named individual.
	 * 
	 * @return The probability of creating an OWL named individual.
	 */
	public double getNewIndividualProbability() {
		return newIndividualProbability;
	}

	/**
	 * Get the probability of creating a class assertion axiom.
	 * 
	 * @return The probability of creating a class assertion axiom.
	 */
	public double getClassAssertionProbability() {
		return classAssertionProbability;
	}

	/**
	 * Get the probability of creating an object property assertion axiom.
	 * 
	 * @return The probability of creating an object property assertion axiom.
	 */
	public double getObjectPropertyAssertionProbability() {
		return objectPropertyAssertionProbability;
	}

	/**
	 * Get the probability of creating a data property assertion axiom.
	 * 
	 * @return The probability of creating a data property assertion axiom.
	 */
	public double getDataPropertyAssertionProbability() {
		return dataPropertyAssertionProbability;
	}

	/**
	 * Get the probability of selecting a super class (direct or inferred) of a
	 * class for class assertion generation
	 * 
	 * @return The probability of selecting a super class (direct or inferred) of a
	 *         class for class assertion generation
	 */
	public double getSuperClassSelectionProbability() {
		return superClassSelectionProbability;
	}

	/**
	 * Get the probability of selecting an equivalent object property of an object
	 * property.
	 * 
	 * @return The probability of selecting an equivalent object property of an
	 *         object property.
	 */
	public double getEquivalentObjectPropertySelectionProbability() {
		return equivalentObjectPropertySelectionProbability;
	}

	/**
	 * Get the probability of selecting an equivalent data property of a data
	 * property.
	 * 
	 * @return The probability of selecting an equivalent data property of a data
	 *         property.
	 */
	public double getEquivalentDataPropertySelectionProbability() {
		return equivalentDataPropertySelectionProbability;
	}

	/**
	 * Get the probability of selecting a disjoint object property of an object
	 * property.
	 * 
	 * @return The probability of selecting a disjoint object property of an object
	 *         property.
	 */
	public double getDisjointObjectPropertySelectionProbability() {
		return disjointObjectPropertySelectionProbability;
	}

	/**
	 * Get the probability of selecting a disjoint data property of a data property.
	 * 
	 * @return The probability of selecting a disjoint data property of a data
	 *         property.
	 */
	public double getDisjointDataPropertySelectionProbability() {
		return disjointDataPropertySelectionProbability;
	}

	/**
	 * Get the probability of selecting a super object property of an object
	 * property.
	 * 
	 * @return The probability of selecting a super object property of an object
	 *         property.
	 */
	public double getSuperObjectPropertySelectionProbability() {
		return superObjectPropertySelectionProbability;
	}

	/**
	 * Get the probability of selecting a super data property of a data property.
	 * 
	 * @return The probability of selecting a super data property of a data
	 *         property.
	 */
	public double getSuperDataPropertySelectionProbability() {
		return superDataPropertySelectionProbability;
	}

	/**
	 * Get the probability of selecting an inverse object property of an object
	 * property.
	 * 
	 * @return The probability of selecting an inverse object property of an object
	 *         property.
	 */
	public double getInverseObjectPropertySelectionProbability() {
		return inverseObjectPropertySelectionProbability;
	}

	/**
	 * Get the probability of selecting symmetric characteristic of an object
	 * property.
	 * 
	 * @return The probability of selecting symmetric characteristic of an object
	 *         property.
	 */
	public double getSymmetricObjectPropertySelectionProbability() {
		return symmetricObjectPropertySelectionProbability;
	}

	/**
	 * Get the probability of selecting asymmetric characteristic of an object
	 * property.
	 * 
	 * @return The probability of selecting asymmetric characteristic of an object
	 *         property.
	 */
	public double getAsymmetricObjectPropertySelectionProbability() {
		return asymmetricObjectPropertySelectionProbability;
	}

	/**
	 * Get the probability of selecting irreflexive characteristic of an object
	 * property for negative object property assertion generation.
	 * 
	 * @return the probability of selecting irreflexive characteristic of an object
	 *         property for negative object property assertion generation.
	 */
	public double getIrreflexiveObjectPropertySelectionProbability() {
		return irreflexiveObjectPropertySelectionProbability;
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLClass is the key and the customized class COWLClassImpl is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLClass, COWLClassImpl> getClassMap() {
		return classMap;
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLDataProperty is the key and the customized class COWLDataPropertyImpl is
	 * the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLDataProperty, COWLDataPropertyImpl> getDataPropertyMap() {
		return dataPropertyMap;
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLObjectProperty is the key and the customized class COWLObjectPropertyImpl
	 * is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLObjectProperty, COWLObjectPropertyImpl> getObjectPropertyMap() {
		return objectPropertyMap;
	}

	/**
	 * Get OWL named individuals of the input ontology.
	 * 
	 * @return OWL named individuals.
	 */
	public Set<OWLNamedIndividual> getExistingIndividuals() {
		return existingIndividuals;
	}

	/**
	 * 
	 * Get created OWL named individuals.
	 *
	 * @return Created OWL named individuals.
	 */
	public Set<OWLNamedIndividual> getNewIndividuals() {
		return newIndividuals;
	}

	/**
	 * Get selected root class in the input ontology as the entry for dataset
	 * generation.
	 * 
	 * @return Selected root class in the input ontology.
	 */
	public COWLClassImpl getRootClass() {
		return rootClass;
	}

	/**
	 * Get ontology root class IRI as string.
	 * 
	 * @return Ontology root class IRI as string.
	 */
	public String getRootIRIString() {
		return rootIRIString;
	}

	/**
	 * This function defines the control flow of object description generation
	 * process.
	 * 
	 * @throws OWLOntologyStorageException
	 *             In case the output ontology fails to save as file.
	 * @throws OWLOntologyCreationException
	 *             In case the output ontology fails to create.
	 */
	public void generateRandomRDFObjectDescriptionInstances()
			throws OWLOntologyStorageException, OWLOntologyCreationException {
		logger.info("Begin generating RDF object descriptions...");

		IRI ontologyIRI = IRI.create(OUTPUT_ONTOLOGY_IRI_IN_STRING);
		outputOntology = manager.createOntology(ontologyIRI);

		OWLImportsDeclarationImpl dec = new OWLImportsDeclarationImpl(
				inputOntology.getOntologyID().getDefaultDocumentIRI().get());

		OWLAnnotation commentAnno = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral(
				"A sample of " + objNumber + " object " + ((objNumber == 1) ? "description" : "descriptions")));

		manager.applyChanges(new AddOntologyAnnotation(outputOntology, commentAnno),
				new AddImport(outputOntology, dec));
		/*
		 * OWLAnnotationAssertionAxiom ax =
		 * factory.getOWLAnnotationAssertionAxiom(ontology.getOntologyID().
		 * getOntologyIRI().get(), commentAnno);
		 * 
		 * 
		 * PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());
		 * 
		 * OWLClass device =
		 * factory.getOWLClass("http://ece.neu.edu/ontologies/SDR.owl#USRPN200");
		 * OWLNamedIndividual ind1 =
		 * factory.getOWLNamedIndividual("#USRPN200_Instance0", pm); OWLNamedIndividual
		 * ind2 = factory.getOWLNamedIndividual(
		 * "http://ece.neu.edu/ontologies/SDR.owl#XC3SD1800AFPGA_Instance0");
		 * OWLObjectProperty objectProperty =
		 * factory.getOWLObjectProperty("http://cogradio.org/ont/Nuvio.owl#aggregateOf")
		 * ;
		 * 
		 * OWLObjectPropertyAssertionAxiom propertyAssertion = factory
		 * .getOWLObjectPropertyAssertionAxiom(objectProperty, ind1, ind2);
		 * 
		 * OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(
		 * device, ind1);
		 * 
		 * manager.addAxiom(ontology, propertyAssertion); manager.addAxiom(ontology,
		 * classAssertion);
		 */
		// manager.addAxiom(ontology, ax);
		// manager.applyChange(new AddAxiom(ontology, ax));
		// manager.applyChange(new AddImport(ontology, dec));
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		// OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
		// System.out.println(reasonerFactory.getReasonerName());
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);

		// Create a reasoner that will reason over our ontology and its imports
		// closure.
		// Pass in the configuration.
		reasoner = reasonerFactory.createReasoner(outputOntology, config);

		// Ask the reasoner to do all the necessary work now
		reasoner.precomputeInferences();
		if (!reasoner.isConsistent()) {
			logger.error("Ontology inconsistency : The loaded ontologies are inconsistent");
			throw new OWLOntologyCreationException();
		}

		for (OWLClass oc : classMap.keySet()) {
			if (oc.getIRI().getIRIString().equals(rootIRIString)) {
				logger.info("Find out root class with IRI: " + rootIRIString);
				rootClass = classMap.get(oc);
				for (int i = 0; i < objNumber; i++) {
					createResursiveLinkedRDFNode(oc, true);
					resetClassStatus(classMap.values());
				}
				break;
			}
		}

		manager.saveOntology(outputOntology, new RDFXMLDocumentFormat(), IRI.create(outputFile));
		// outputOntology = manager.loadOntologyFromOntologyDocument(outputFile);
		// factory = manager.getOWLDataFactory();
		/*
		 * OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		 * ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		 * OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		 * OWLReasoner reasoner = reasonerFactory.createReasoner(outputOntology,
		 * config);
		 * 
		 * reasoner.precomputeInferences();
		 */
		logger.info(String.valueOf(newIndividuals.size()) + " OWL individuals have been created successfully!");
		logger.info("Done!");
	}

	/**
	 * This function recursively creates OWL named individuals by navigating through
	 * the model from the specified OWL class.
	 * 
	 * @param oc
	 *            OWL class.
	 * @param isFirstRecursion
	 *            True if the function is invoked for the first time, false
	 *            otherwise.
	 * @return OWL named individual.
	 */
	public OWLNamedIndividual createResursiveLinkedRDFNode(OWLClass oc, boolean isFirstRecursion) {
		if (oc == null || oc.isOWLThing())
			return null;
		COWLClassImpl ocImpl1 = classMap.get(oc);
		Set<COWLClassImpl> ocImplSet = ocImpl1.getSubClassesAndItself();

		// If any of its subclasses and itself are visited, return the latest created
		// OWL named individuals of the class.
		for (COWLClassImpl subClass : ocImplSet)
			if (subClass.isVisited())
				// return
				// CollectionUtil.getARandomElementFromList(subClass.getNamedIndividuals(),
				// ran);
				return subClass.getNamedIndividuals(true).getLast();

		// If the class is the root class or its subclasses, and the function is invoked
		// more than once, it randomly returns an individual of the type of the class if
		// any.
		if (rootClass.getSubClassesAndItself().contains(ocImpl1) && isFirstRecursion == false) {
			for (COWLClassImpl cls : ocImpl1.getSubClassesAndItself())
				if (!cls.getNamedIndividuals(true).isEmpty())
					return CollectionUtil.getARandomElementFromList(cls.getNamedIndividuals(true), ran);
			return null;
		}

		COWLClassImpl ocImpl2 = CollectionUtil.getARandomElementFromSet(ocImplSet, ran);
		// If there are any individuals of the type of the class, and the function
		// doesn't create new individuals, it randomly returns an individual of the type
		// of the class.
		if (ran.nextDouble() < 1 - newIndividualProbability && !ocImpl2.getNamedIndividuals(true).isEmpty()
				&& isFirstRecursion == false)
			return CollectionUtil.getARandomElementFromList(ocImpl2.getNamedIndividuals(true), ran);

		// Create new individuals if none of the above conditions satisfies.
		logger.info("Selected OWL class IRI is: " + ocImpl2.getIRI().getIRIString());
		OWLNamedIndividual ind = IRIGenerator.generateOWLIndividual(OUTPUT_ONTOLOGY_IRI_IN_STRING, factory, ocImpl2);
		newIndividuals.add(ind);
		ocImpl2.getNamedIndividuals(true).add(ind);
		ocImpl2.setVisited(true);

		// Create OWL class assertion axiom if the condition satisfies.
		if (isFirstRecursion == true) {
			OWLClassAssertionAxiom classAssertion = factory
					.getOWLClassAssertionAxiom(factory.getOWLClass(ocImpl2.getIRI()), ind);
			manager.addAxiom(outputOntology, classAssertion);

			Set<COWLClassImpl> superClasses = ocImpl2.getSuperClasses();
			if (!superClasses.isEmpty() && ran.nextDouble() < superClassSelectionProbability) {
				OWLClass sup = factory.getOWLClass(CollectionUtil.getARandomElementFromSet(superClasses, ran).getIRI());
				classAssertion = factory.getOWLClassAssertionAxiom(sup, ind);
				manager.addAxiom(outputOntology, classAssertion);
			}
		} else
			generateClassAssertionAxiom(ind, factory.getOWLClass(ocImpl2.getIRI()));

		// In the case when the class contains equivalent classes.
		if (!ocImpl2.getEquivalentClasses().isEmpty()) {
			OWLClassExpression exp = CollectionUtil.getARandomElementFromSet(ocImpl2.getEquivalentClasses(), ran);
			if (exp.isAnonymous()) {
				logger.info("\t The selected equivalent class restriction is: " + exp.toString());
				// Process equivalent anonymous class expression of the class.
				exp.accept(new COWLClassExpressionVisitor(ind, factory.getOWLClass(ocImpl2.getIRI()), this));
			} else
				generateClassAssertionAxiom(ind, exp.asOWLClass());
		}
		// In the case when the class doesn't contain equivalent class but contain
		// anonymous super classes.
		else if (!ocImpl2.getAnonymousSuperClasses().isEmpty()) {
			// Set<OWLAnonymousClassExpression> succinctClassConstraints = new HashSet<>();
			// for (OWLAnonymousClassExpression exp : ocImpl2.getAnonymousSuperClasses())
			// Remove redundant class constraints.
			// checkClassConstraintsRedundancy(succinctClassConstraints, exp);

			// Process selected class constraints.
			for (OWLAnonymousClassExpression exp : ocImpl2.getAnonymousSuperClasses()) {
				if (ran.nextDouble() < classConstraintSelectionProbability) {
					logger.info("\t One of the selected class restrictions is: " + exp.toString());
					exp.accept(new COWLClassExpressionVisitor(ind, factory.getOWLClass(ocImpl2.getIRI()), this));
				}
			}
		}
		// In the case when the class contains only property constraints
		else {
			// Get linked resources for each class. Note that they are dynamically
			// generated on the fly (at run time).
			Set<Entry<OWLDataProperty, OWLDataRange>> selectedDataPropertyAndRangePairs = new HashSet<>();
			Set<Entry<OWLObjectProperty, OWLClassExpression>> selectedObjectPropertyAndRangePairs = new HashSet<>();
			if (linkedResourcesForEachClass.containsKey(ocImpl2)) {
				LinkedResource res = linkedResourcesForEachClass.get(ocImpl2);
				if (res.dataPropertyAndRangePairsList.size() > 0)
					selectedDataPropertyAndRangePairs = (Set<Entry<OWLDataProperty, OWLDataRange>>) CollectionUtil
							.getARandomElementFromList(res.dataPropertyAndRangePairsList, ran);
				if (res.objectPropertyAndRangePairsList.size() > 0)
					selectedObjectPropertyAndRangePairs = (Set<Entry<OWLObjectProperty, OWLClassExpression>>) CollectionUtil
							.getARandomElementFromList(res.objectPropertyAndRangePairsList, ran);
			} else {
				List<Set<Entry<OWLDataProperty, OWLDataRange>>> listOfDataPropertyAndRangePairs = (List<Set<Entry<OWLDataProperty, OWLDataRange>>>) CollectionUtil
						.getAllSubSetsOfASet(ocImpl2.getDataPropertyRangesPairs().entrySet());
				if (listOfDataPropertyAndRangePairs.size() > 0)
					selectedDataPropertyAndRangePairs = (Set<Entry<OWLDataProperty, OWLDataRange>>) CollectionUtil
							.getARandomElementFromList(listOfDataPropertyAndRangePairs, ran);

				List<Set<Entry<OWLObjectProperty, OWLClassExpression>>> listOfObjectPropertyAndRangePairs = (List<Set<Entry<OWLObjectProperty, OWLClassExpression>>>) CollectionUtil
						.getAllSubSetsOfASet(ocImpl2.getObjectPropertyRangesPairs().entrySet());
				if (listOfObjectPropertyAndRangePairs.size() > 0)
					selectedObjectPropertyAndRangePairs = (Set<Entry<OWLObjectProperty, OWLClassExpression>>) CollectionUtil
							.getARandomElementFromList(listOfObjectPropertyAndRangePairs, ran);
				linkedResourcesForEachClass.put(ocImpl2,
						new LinkedResource(listOfDataPropertyAndRangePairs, listOfObjectPropertyAndRangePairs));
			}
			OWLDataProperty dataProperty;
			OWLDataRange dataRange;

			// Process data property constraints
			for (Entry<OWLDataProperty, OWLDataRange> dataPropertyAndRangePair : selectedDataPropertyAndRangePairs)
				if (ran.nextDouble() < dataPropertyAssertionProbability) {
					dataProperty = dataPropertyAndRangePair.getKey();
					dataRange = dataPropertyAndRangePair.getValue();
					// equivalentProperties =
					// dataPropertyMap.get(dataProperty).getEquivalentProperties();
					// if (!equivalentProperties.isEmpty() && ran.nextDouble() <
					// equivalentDataPropertySelectionProbability)
					// dataProperty = factory.getOWLDataProperty(
					// CollectionUtil.getARandomElementFromSet(equivalentProperties, ran).getIRI());
					dataRange.accept(new COWLDataRangeVisitor(ind, dataProperty, this));
				}

			OWLObjectProperty objectProperty;
			OWLClassExpression classExp;
			OWLNamedIndividual individual;
			// Process object property constraints.
			for (Entry<OWLObjectProperty, OWLClassExpression> objectPropertyAndRangePair : selectedObjectPropertyAndRangePairs)
				if (ran.nextDouble() < objectPropertyAssertionProbability) {
					objectProperty = objectPropertyAndRangePair.getKey();
					classExp = objectPropertyAndRangePair.getValue();
					if (classExp.isAnonymous()) {
						/*
						 * boolean flag = false; if (classExp instanceof OWLObjectIntersectionOf) { for
						 * (OWLClassExpression cls : ((OWLObjectIntersectionOf) classExp).operands()
						 * .collect(Collectors.toSet())) { if (!cls.isAnonymous()) { COWLClassImpl
						 * owlClass = classMap.get(cls.asOWLClass()); if
						 * (rootClass.getSubClassesandItself().contains(owlClass)) { for (COWLClassImpl
						 * sub : owlClass.getSubClassesandItself()) if
						 * (!sub.getNamedIndividuals().isEmpty()) { individual = CollectionUtil
						 * .getARandomElementFromList(sub.getNamedIndividuals(), ran);
						 * processObjectPropertyAssertionAxiom(objectProperty, ind, individual); //
						 * OWLObjectPropertyAssertionAxiom propertyAssertion = //
						 * factory.getOWLObjectPropertyAssertionAxiom(objectProperty, ind, //
						 * individual); // manager.addAxiom(outputOntology, propertyAssertion); break; }
						 * flag = true; break; } } } } if (flag) continue;
						 */
						/*
						 * boolean flag = false; for (OWLClass cls :
						 * classExp.classesInSignature().collect(Collectors.toSet())) { COWLClassImpl
						 * owlClass = classMap.get(cls); if
						 * (rootClass.getSubClassesandItself().contains(owlClass)) { for (COWLClassImpl
						 * sub : owlClass.getSubClassesandItself())
						 * if(!sub.getNamedIndividuals().isEmpty()) { individual =
						 * CollectionUtil.getARandomElementFromList(sub.getNamedIndividuals(), ran);
						 * processObjectPropertyAssertionAxiom(objectProperty, ind, individual); break;
						 * } flag = true; } } if (flag) continue;
						 */
						individual = IRIGenerator.generateOWLIndividual(OUTPUT_ONTOLOGY_IRI_IN_STRING, factory);
						newIndividuals.add((OWLNamedIndividual) individual);
						generateObjectPropertyAssertionAxiom(objectProperty, ind, individual);
						classExp.accept(new COWLClassExpressionVisitor(individual, null, this));
					} else {
						individual = createResursiveLinkedRDFNode(classExp.asOWLClass(), false);
						if (individual != null) {
							generateObjectPropertyAssertionAxiom(objectProperty, ind, individual);
						}
					}
				}
		}

		return ind;
	}

	/**
	 * This function generates a class assertion and randomly generates a set of
	 * inferred class assertions.
	 * 
	 * @param ind
	 *            OWL named individual.
	 * @param cls
	 *            OWL class.
	 */
	public void generateClassAssertionAxiom(OWLNamedIndividual ind, OWLClass cls) {
		if (ran.nextDouble() < classAssertionProbability) {
			OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(cls, ind);
			manager.addAxiom(outputOntology, classAssertion);

			COWLClassImpl clsImpl = classMap.get(cls);
			Set<COWLClassImpl> superClasses = clsImpl.getSuperClasses();
			if (!superClasses.isEmpty() && ran.nextDouble() < superClassSelectionProbability) {
				OWLClass sup = factory.getOWLClass(CollectionUtil.getARandomElementFromSet(superClasses, ran).getIRI());
				classAssertion = factory.getOWLClassAssertionAxiom(sup, ind);
				manager.addAxiom(outputOntology, classAssertion);
			}
		}
	}

	/**
	 * This function generates an object property assertion axiom and randomly
	 * generates a set of inferred object property assertions.
	 * 
	 * @param objectProperty
	 *            OWL object property.
	 * @param subject
	 *            OWL named individual.
	 * @param object
	 *            OWL named individual.
	 */
	public void generateObjectPropertyAssertionAxiom(OWLObjectProperty objectProperty, OWLNamedIndividual subject,
			OWLNamedIndividual object) {
		// logger.info("Property name: " + objectProperty.getIRI().getIRIString());
		if (objectProperty == null || subject == null || object == null)
			return;

		OWLNegativeObjectPropertyAssertionAxiom negativePropertyAssertion;
		COWLObjectPropertyImpl objectPropertyImpl = objectPropertyMap.get(objectProperty);

		if (containsRelevantNegativeObjectPropertyAssertionAxiom(objectPropertyImpl, subject, object,
				new HashSet<OWLNegativeObjectPropertyAssertionAxiom>()))
			return;

		if (subject.equals(object)) {
			if (objectPropertyImpl.getPropertyAttributes().contains(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY)) {
				if (ran.nextDouble() < irreflexiveObjectPropertySelectionProbability) {
					negativePropertyAssertion = factory.getOWLNegativeObjectPropertyAssertionAxiom(objectProperty,
							subject, object);
					manager.addAxiom(outputOntology, negativePropertyAssertion);
				}
				return;
			} else if (objectPropertyImpl.getPropertyAttributes().contains(AxiomType.ASYMMETRIC_OBJECT_PROPERTY)) {
				if (ran.nextDouble() < asymmetricObjectPropertySelectionProbability) {
					negativePropertyAssertion = factory.getOWLNegativeObjectPropertyAssertionAxiom(objectProperty,
							subject, object);
					manager.addAxiom(outputOntology, negativePropertyAssertion);
				}
				return;
			}
		}

		Set<COWLObjectPropertyImpl> inverseObjectProperties = objectPropertyImpl.getInverseProperties();
		for (COWLPropertyImpl inverseProp : inverseObjectProperties) {
			for (COWLPropertyImpl disjointProp : inverseProp.getDisjointProperties()) {
				if (containsRelevantObjectPropertyAssertionAxiom(disjointProp, object, subject,
						new HashSet<OWLObjectPropertyAssertionAxiom>()))
					return;
			}
		}

		Set<COWLPropertyImpl> disjointProperties = objectPropertyImpl.getDisjointProperties();

		for (COWLPropertyImpl disjointProp : disjointProperties) {
			if (containsRelevantObjectPropertyAssertionAxiom(disjointProp, subject, object,
					new HashSet<OWLObjectPropertyAssertionAxiom>()))
				return;
			/*
			 * for (COWLPropertyImpl inverseProp : ((COWLObjectPropertyImpl)
			 * disjointProp).getInverseProperties()) if (containsAxiom(inverseProp, object,
			 * subject)) return;
			 */
		}

		OWLObjectPropertyAssertionAxiom propertyAssertion;

		propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object);
		manager.addAxiom(outputOntology, propertyAssertion);

		if (!disjointProperties.isEmpty() && ran.nextDouble() < disjointObjectPropertySelectionProbability) {
			negativePropertyAssertion = factory.getOWLNegativeObjectPropertyAssertionAxiom(
					factory.getOWLObjectProperty(
							CollectionUtil.getARandomElementFromSet(disjointProperties, ran).getIRI()),
					subject, object);
			manager.addAxiom(outputOntology, negativePropertyAssertion);
		}

		if (!inverseObjectProperties.isEmpty() && ran.nextDouble() < inverseObjectPropertySelectionProbability) {
			OWLObjectProperty inverseProperty = factory.getOWLObjectProperty(
					CollectionUtil.getARandomElementFromSet(inverseObjectProperties, ran).getIRI());
			propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(inverseProperty, object, subject);
			manager.addAxiom(outputOntology, propertyAssertion);
		}

		OWLObjectProperty objectProp = objectProperty;
		Set<COWLPropertyImpl> superProperties = objectPropertyImpl.getSuperOWLProperties();
		if (!superProperties.isEmpty() && ran.nextDouble() < superObjectPropertySelectionProbability) {
			objectProp = factory
					.getOWLObjectProperty(CollectionUtil.getARandomElementFromSet(superProperties, ran).getIRI());
			propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(objectProp, subject, object);
			manager.addAxiom(outputOntology, propertyAssertion);
		}

		Set<COWLPropertyImpl> equivalentProperties = objectPropertyImpl.getEquivalentProperties();
		if (!equivalentProperties.isEmpty() && ran.nextDouble() < equivalentObjectPropertySelectionProbability) {
			objectProp = factory
					.getOWLObjectProperty(CollectionUtil.getARandomElementFromSet(equivalentProperties, ran).getIRI());
			propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(objectProp, subject, object);
			manager.addAxiom(outputOntology, propertyAssertion);
		}
		// outputOntology.containsAxiom(axiom, Imports.INCLUDED,
		// AxiomAnnotations.IGNORE_AXIOM_ANNOTATIONS);

		if (objectPropertyImpl.getPropertyAttributes().contains(AxiomType.ASYMMETRIC_OBJECT_PROPERTY)
				&& ran.nextDouble() < asymmetricObjectPropertySelectionProbability) {
			negativePropertyAssertion = factory.getOWLNegativeObjectPropertyAssertionAxiom(objectProperty, object,
					subject);
			manager.addAxiom(outputOntology, negativePropertyAssertion);
		}

		if (objectPropertyImpl.getPropertyAttributes().contains(AxiomType.SYMMETRIC_OBJECT_PROPERTY)
				&& ran.nextDouble() < symmetricObjectPropertySelectionProbability) {
			propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(objectProperty, object, subject);
			manager.addAxiom(outputOntology, propertyAssertion);
		}
	}

	/**
	 * This function detects whether the output ontology contains the specified
	 * object property assertion and its relevant object property assertions.
	 * 
	 * @param property
	 *            Customization of OWL object property.
	 * @param subject
	 *            OWL named individual.
	 * @param object
	 *            OWL named individual.
	 * @param propertyAssertions
	 *            Verified object property assertions that are not in the output
	 *            ontology.
	 * @return true if there exists such object property assertions, false
	 *         otherwise.
	 */
	public boolean containsRelevantObjectPropertyAssertionAxiom(COWLPropertyImpl property, OWLNamedIndividual subject,
			OWLNamedIndividual object, Set<OWLObjectPropertyAssertionAxiom> propertyAssertions) {
		if (property == null || subject == null || object == null)
			return false;

		OWLObjectPropertyAssertionAxiom propertyAssertion = factory
				.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(property.getIRI()), subject, object);

		if (propertyAssertions.contains(propertyAssertion))
			return false;

		if (outputOntology.containsAxiom(propertyAssertion, Imports.INCLUDED,
				AxiomAnnotations.IGNORE_AXIOM_ANNOTATIONS))
			return true;

		propertyAssertions.add(propertyAssertion);

		for (COWLPropertyImpl eq : ((COWLObjectPropertyImpl) property).getEquivalentProperties()) {
			if (containsRelevantObjectPropertyAssertionAxiom(eq, subject, object, propertyAssertions))
				return true;
		}

		for (COWLPropertyImpl sub : ((COWLObjectPropertyImpl) property).getSubOWLProperties()) {
			if (containsRelevantObjectPropertyAssertionAxiom(sub, subject, object, propertyAssertions))
				return true;
		}

		for (COWLPropertyImpl inv : ((COWLObjectPropertyImpl) property).getInverseProperties()) {
			if (containsRelevantObjectPropertyAssertionAxiom(inv, object, subject, propertyAssertions))
				return true;
		}

		return false;
	}

	/**
	 * This function detects whether the output ontology contains the specified
	 * negative object property assertion and its relevant negative object property
	 * assertions.
	 * 
	 * @param property
	 *            OWL object property.
	 * @param subject
	 *            OWL named individual.
	 * @param object
	 *            OWL named individual.
	 * @param propertyAssertions
	 *            Verified negative object property assertions that are in the
	 *            output ontology.
	 * 
	 * @return true if there exists such negative object property assertions, false
	 *         otherwise.
	 */
	public boolean containsRelevantNegativeObjectPropertyAssertionAxiom(COWLPropertyImpl property,
			OWLNamedIndividual subject, OWLNamedIndividual object,
			Set<OWLNegativeObjectPropertyAssertionAxiom> propertyAssertions) {
		if (property == null || subject == null || object == null)
			return false;

		OWLNegativeObjectPropertyAssertionAxiom propertyAssertion = factory.getOWLNegativeObjectPropertyAssertionAxiom(
				factory.getOWLObjectProperty(property.getIRI()), subject, object);

		if (propertyAssertions.contains(propertyAssertion))
			return false;

		if (outputOntology.containsAxiom(propertyAssertion, Imports.INCLUDED,
				AxiomAnnotations.IGNORE_AXIOM_ANNOTATIONS))
			return true;

		propertyAssertions.add(propertyAssertion);

		for (COWLPropertyImpl eq : ((COWLObjectPropertyImpl) property).getEquivalentProperties()) {
			if (containsRelevantNegativeObjectPropertyAssertionAxiom(eq, subject, object, propertyAssertions))
				return true;
		}

		for (COWLPropertyImpl sub : ((COWLObjectPropertyImpl) property).getSubOWLProperties()) {
			if (containsRelevantNegativeObjectPropertyAssertionAxiom(sub, subject, object, propertyAssertions))
				return true;
		}

		for (COWLPropertyImpl inv : ((COWLObjectPropertyImpl) property).getInverseProperties()) {
			if (containsRelevantNegativeObjectPropertyAssertionAxiom(inv, object, subject, propertyAssertions))
				return true;
		}

		return false;
	}

	/**
	 * Reset all classes as not visited.
	 * 
	 * @param classes
	 *            A collection of classes.
	 */
	public static void resetClassStatus(Collection<COWLClassImpl> classes) {
		for (COWLClassImpl tc : classes) {
			if (tc.isVisited())
				tc.setVisited(false);
			// tc.setLastInstanceIndex(0);
			/*
			 * for (Individual i : tc.getIndividuals()) { if (i.isVisited())
			 * i.setIsVisited(true); }
			 */
		}
	}

}
