package edu.neu.ece.devicedescriptiongenerator.extractor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLQuantifiedDataRestriction;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.devicedescriptiongenerator.entity.classes.COWLClassImpl;
import edu.neu.ece.devicedescriptiongenerator.entity.properties.COWLDataPropertyImpl;
import edu.neu.ece.devicedescriptiongenerator.entity.properties.COWLObjectPropertyImpl;
import edu.neu.ece.devicedescriptiongenerator.entity.properties.COWLPropertyImpl;
import edu.neu.ece.devicedescriptiongenerator.metric.OntologyMetric;
import edu.neu.ece.devicedescriptiongenerator.visitor.COWLClassAxiomVisitor;
import edu.neu.ece.devicedescriptiongenerator.visitor.COWLDataPropertyAxiomVisitor;
import edu.neu.ece.devicedescriptiongenerator.visitor.COWLEntityVisitor;
import edu.neu.ece.devicedescriptiongenerator.visitor.COWLIndividualAxiomVisitor;
import edu.neu.ece.devicedescriptiongenerator.visitor.COWLObjectPropertyAxiomVisitor;

/**
 * As one of the most critical classes of the program, OntologyExtractor defines
 * ontology extractor used for extracting OWL axioms from input ontology.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-29
 *
 */
public class OntologyExtractor {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Mapping from OWL named class to its conceptual model.
	 */
	private Map<OWLClass, COWLClassImpl> classMap = new HashMap<>();

	/**
	 * Mapping from OWL data property to its conceptual model.
	 */
	private Map<OWLDataProperty, COWLDataPropertyImpl> dataPropertyMap = new HashMap<>();

	/**
	 * Mapping from OWL object property to its conceptual model.
	 */
	private Map<OWLObjectProperty, COWLObjectPropertyImpl> objectPropertyMap = new HashMap<>();

	/**
	 * OWL named individuals in TBox.
	 */
	private Set<OWLNamedIndividual> existingIndividuals = new HashSet<>();

	/**
	 * Hold of input ontology.
	 */
	private OWLOntology ont;

	/**
	 * An build-in OWL reasoner from OWL API.
	 */
	private OWLReasoner reasoner;

	/**
	 * OWL metric class that records the number of OWL named class, data properties,
	 * object properties and individuals.
	 */
	private OntologyMetric metric = new OntologyMetric();

	private static Map<ClassExpressionType, Integer> quantifiedObjectRestrictionPriority = new HashMap<>();

	private static Map<ClassExpressionType, Integer> quantifiedDataRestrictionPriority = new HashMap<>();

	static {
		quantifiedObjectRestrictionPriority.put(ClassExpressionType.OBJECT_EXACT_CARDINALITY, new Integer(1));
		quantifiedObjectRestrictionPriority.put(ClassExpressionType.OBJECT_MAX_CARDINALITY, new Integer(2));
		quantifiedObjectRestrictionPriority.put(ClassExpressionType.OBJECT_MIN_CARDINALITY, new Integer(2));
		quantifiedObjectRestrictionPriority.put(ClassExpressionType.OBJECT_ALL_VALUES_FROM, new Integer(3));
		quantifiedObjectRestrictionPriority.put(ClassExpressionType.OBJECT_SOME_VALUES_FROM, new Integer(3));

		quantifiedDataRestrictionPriority.put(ClassExpressionType.DATA_EXACT_CARDINALITY, new Integer(1));
		quantifiedDataRestrictionPriority.put(ClassExpressionType.DATA_MAX_CARDINALITY, new Integer(2));
		quantifiedDataRestrictionPriority.put(ClassExpressionType.DATA_MIN_CARDINALITY, new Integer(2));
		quantifiedDataRestrictionPriority.put(ClassExpressionType.DATA_ALL_VALUES_FROM, new Integer(3));
		quantifiedDataRestrictionPriority.put(ClassExpressionType.DATA_SOME_VALUES_FROM, new Integer(3));
	}

	/**
	 * Constructor
	 * 
	 * @param ont
	 *            Hold of ontology.
	 * @param reasoner
	 *            OWL reasoner.
	 */
	public OntologyExtractor(OWLOntology ont, OWLReasoner reasoner) {
		this.ont = ont;
		this.reasoner = reasoner;
	}

	/**
	 * Get mapping from OWL named class to its conceptual model.
	 * 
	 * @return The class mapping.
	 */
	public Map<OWLClass, COWLClassImpl> getClassMap() {
		return Collections.unmodifiableMap(classMap);
	}

	/**
	 * Get mapping from OWL data property to its conceptual model.
	 * 
	 * @return The data property mapping.
	 */
	public Map<OWLDataProperty, COWLDataPropertyImpl> getDataPropertyMap() {
		return Collections.unmodifiableMap(dataPropertyMap);
	}

	/**
	 * Get mapping from OWL object property to its conceptual model.
	 * 
	 * @return The object property mapping.
	 */
	public Map<OWLObjectProperty, COWLObjectPropertyImpl> getObjectPropertyMap() {
		return Collections.unmodifiableMap(objectPropertyMap);
	}

	/**
	 * Get OWL named individuals in TBox.
	 * 
	 * @return OWL named individuals in TBox.
	 */
	public Set<OWLNamedIndividual> getExistingIndividuals() {
		return Collections.unmodifiableSet(existingIndividuals);
	}

	/**
	 * Get OWL metric object that records the number of OWL named class, data
	 * properties, object properties and individuals.
	 * 
	 * @return The OWL metric object.
	 */
	public OntologyMetric getOntologyMetric() {
		return metric;
	}

	/**
	 * Get conceptual model of specified OWL named class.
	 * 
	 * @param owlClass
	 *            Specified OWL named class.
	 * @return Conceptual model of the specified class.
	 */
	public COWLClassImpl getCOWLClassImpl(OWLClass owlClass) {
		return classMap.get(owlClass);
	}

	/**
	 * This function controls the whole ontology extraction process, including
	 * ontology preparsing, ontology parsing and ontology postparsing.
	 */
	public void extract() {
		preParsing();
		parsing();
		postParsing();
	}

	/**
	 * This function contains ontology preparsing process.
	 */
	private void preParsing() {
		logger.info("Begin extracting OWL entities...");
		OWLOntologyWalker walker = new OWLOntologyWalker(ont.importsClosure().collect(Collectors.toSet()));
		// Here we use visitor design pattern to visit ontology entities through
		// COWLEntityVistor.
		COWLEntityVisitor oev = new COWLEntityVisitor(this);
		walker.walkStructure(oev);
		logger.info(metric.toString());
		logger.info("Extract OWL entities successfully!");
	}

	/**
	 * This function contains ontology extraction process. In general, OWL class
	 * axioms, OWL object property axioms, OWL data property axioms and OWL
	 * individual axioms are extracted in order into conceptual model.
	 */
	private void parsing() {
		processClassAxioms();
		processObjectPropertyAxioms();
		processDataPropertyAxioms();
		processIndividualAxioms();
	}

	/**
	 * This function processes OWL class axioms extraction.
	 */
	private void processClassAxioms() {
		logger.info("Begin extracting OWL class axioms...");
		for (OWLClass owlClass : ont.classesInSignature(Imports.INCLUDED).collect(Collectors.toSet())) {
			// Here we use visitor design pattern to visit OWL class axioms of the specified
			// OWL name class through COWLClassAxiomVisitor.
			COWLClassAxiomVisitor visitor = new COWLClassAxiomVisitor(this, owlClass);
			for (OWLClassAxiom owlClassAxiom : ont.axioms(owlClass, Imports.INCLUDED).collect(Collectors.toSet())) {
				owlClassAxiom.accept(visitor);
			}
		}
		logger.info("Extract OWL class axioms successfully!");
	}

	/**
	 * This function processes OWL object property axioms extraction.
	 */
	private void processObjectPropertyAxioms() {
		logger.info("Begin extracting object property axioms...");
		for (OWLObjectProperty owlObjectProperty : ont.objectPropertiesInSignature(Imports.INCLUDED)
				.collect(Collectors.toSet())) {
			// logger.info("The IRI of the object property is " +
			// owlObjectProperty.getIRI().getIRIString());
			// COWLObjectPropertyImpl cowlObjectPropertyImpl =
			// getObjectPropertyMap().get(owlObjectProperty);
			// Here we use visitor design pattern to visit OWL object property axioms of the
			// specified OWL object property through COWLObjectPropertyAxiomVisitor.
			COWLObjectPropertyAxiomVisitor cowlObjectPropertyAxiomVisitor = new COWLObjectPropertyAxiomVisitor(this,
					owlObjectProperty);
			Set<COWLClassImpl> cowlDomSet = new HashSet<>();
			// Set<COWLClassImpl> cowlRanSet = new HashSet<>();
			OWLClassExpression range = null;
			for (OWLObjectPropertyAxiom owlObjectPropertyAxiom : ont.axioms(owlObjectProperty, Imports.INCLUDED)
					.collect(Collectors.toSet())) {
				if (owlObjectPropertyAxiom.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
					// Set<OWLClass> domSet =
					// reasoner.objectPropertyDomains(owlObjectProperty).collect(Collectors.toSet());
					Set<OWLClass> domSet = owlObjectPropertyAxiom.classesInSignature().collect(Collectors.toSet());
					for (OWLClass oc : domSet) {
						COWLClassImpl cowlClass = getClassMap().get(oc);
						if (cowlClass == null) {
							logger.error("An object property " + owlObjectProperty.getIRI().getIRIString()
									+ " has an invalid domain: " + oc.getIRI().getIRIString());
							continue;
						}
						// logger.info("\t\tOne of the domain is " + oc.getIRI().getIRIString());
						cowlDomSet.add(cowlClass);
					}
				} else if (owlObjectPropertyAxiom.isOfType(AxiomType.OBJECT_PROPERTY_RANGE)) {
					// Set<OWLClass> ranSet =
					// reasoner.objectPropertyRanges(owlObjectProperty).collect(Collectors.toSet());
					/*
					 * Set<OWLClass> ranSet =
					 * owlObjectPropertyAxiom.classesInSignature().collect(Collectors.toSet());
					 * for(OWLClass oc : ranSet) { COWLClassImpl cowlClass = getClassMap().get(oc);
					 * if(cowlClass == null) { logger.error("An object property " +
					 * owlObjectProperty.getIRI().getIRIString() + " has an invalid range: " +
					 * oc.getIRI().getIRIString()); continue; }
					 * //logger.info("\t\tOne of the range is " + oc.getIRI().getIRIString());
					 * cowlRanSet.add(cowlClass); }
					 */
					range = ((OWLObjectPropertyRangeAxiom) owlObjectPropertyAxiom).getRange();
				} else
					owlObjectPropertyAxiom.accept(cowlObjectPropertyAxiomVisitor);
			}
			if (cowlDomSet.isEmpty() || range == null) {
				logger.warn("An object property " + owlObjectProperty.getIRI().getIRIString()
						+ " has no domains or ranges and will be ignored");
				continue;
			}
			for (COWLClassImpl ds : cowlDomSet)
				ds.addAnObjectPropertyRangesPair(owlObjectProperty, range);
		}
		logger.info("Extract object property axioms successfully!");
	}

	/**
	 * This function processes OWL data property axioms extraction.
	 */
	private void processDataPropertyAxioms() {
		logger.info("Begin extracting data property axioms...");
		for (OWLDataProperty owlDataProperty : ont.dataPropertiesInSignature(Imports.INCLUDED)
				.collect(Collectors.toSet())) {
			// logger.info("The IRI of the data property is " +
			// owlDataProperty.getIRI().getIRIString());
			// COWLDataPropertyImpl cowlDataPropertyImpl =
			// getDataPropertyMap().get(owlDataProperty);
			// Here we use visitor design pattern to visit OWL data property axioms of the
			// specified OWL data property through COWLDataPropertyAxiomVisitor.
			COWLDataPropertyAxiomVisitor cowlDataPropertyAxiomVisitor = new COWLDataPropertyAxiomVisitor(this,
					owlDataProperty);
			Set<COWLClassImpl> cowlDomSet = new HashSet<>();
			OWLDataRange ran = null;
			for (OWLDataPropertyAxiom owlDataPropertyAxiom : ont.axioms(owlDataProperty, Imports.INCLUDED)
					.collect(Collectors.toSet())) {
				if (owlDataPropertyAxiom.isOfType(AxiomType.DATA_PROPERTY_DOMAIN)) {
					Set<OWLClass> domSet = owlDataPropertyAxiom.classesInSignature().collect(Collectors.toSet());
					// Set<OWLClass> domSet =
					// reasoner.dataPropertyDomains(owlDataProperty).collect(Collectors.toSet());
					for (OWLClass oc : domSet) {
						COWLClassImpl cowlClass = getClassMap().get(oc);
						if (cowlClass == null) {
							logger.error("A data property " + owlDataProperty.getIRI().getIRIString()
									+ " has an invalid domain: " + oc.getIRI().getIRIString());
							continue;
						}
						// logger.info("\t\tOne of the domain is " + oc.getIRI().getIRIString());
						cowlDomSet.add(cowlClass);
					}
				} else if (owlDataPropertyAxiom.isOfType(AxiomType.DATA_PROPERTY_RANGE)) {
					ran = ((OWLDataPropertyRangeAxiom) owlDataPropertyAxiom).getRange();
					dataPropertyMap.get(owlDataProperty).setOWLDataRange(ran);
					// logger.info("\t\tThe range is " + ran.toString());
				} else
					owlDataPropertyAxiom.accept(cowlDataPropertyAxiomVisitor);
			}
			if (cowlDomSet.isEmpty() || ran == null) {
				logger.warn("An data property " + owlDataProperty.getIRI().getIRIString()
						+ " has no domains or ranges and will be ignored");
				continue;
			}
			for (COWLClassImpl ds : cowlDomSet)
				ds.addADataPropertyRangesPair(owlDataProperty, ran);
		}
		logger.info("Extract data property axioms successfully!");
	}

	/**
	 * This function processes OWL individual axioms extraction.
	 */
	private void processIndividualAxioms() {
		logger.info("Begin extracting individual axioms...");
		for (OWLNamedIndividual ind : existingIndividuals) {
			// Here we use visitor design pattern to visit OWL individual axioms of the
			// specified OWL named individual through COWLIndividualAxiomVisitor.
			COWLIndividualAxiomVisitor cowlIndividualAxiomVisitor = new COWLIndividualAxiomVisitor(this, ind);
			for (OWLIndividualAxiom owlIndividualAxiom : ont.axioms(ind, Imports.INCLUDED).collect(Collectors.toSet()))
				owlIndividualAxiom.accept(cowlIndividualAxiomVisitor);
		}
		logger.info("Extract individual axioms successfully!");
	}

	/**
	 * This function contains ontology postparsing process, including deriving
	 * inferred facts from existing conceptual model.
	 */
	private void postParsing() {
		logger.info("Begin extracting implicit knowledge...");
		for (Entry<OWLClass, COWLClassImpl> classEntry : classMap.entrySet()) {
			OWLClass owlClass = classEntry.getKey();
			COWLClassImpl cowlClassImpl = classEntry.getValue();
			// Get subclasses excluding owl:Nothing of each class
			for (OWLClass subClass : reasoner.subClasses(owlClass).collect(Collectors.toSet())) {
				if (!subClass.isOWLNothing())
					cowlClassImpl.getSubClasses().add(classMap.get(subClass));
			}
			// Get superclasses excluding owl:Thing of each class
			for (OWLClass superClass : reasoner.superClasses(owlClass).collect(Collectors.toSet())) {
				if (!superClass.isOWLThing())
					cowlClassImpl.getSuperClasses().add(classMap.get(superClass));
			}
			// Get equivalent classes of each class
			for (OWLClass equivalentClass : reasoner.equivalentClasses(owlClass).collect(Collectors.toSet())) {
				if (!owlClass.equals(equivalentClass))
					cowlClassImpl.getEquivalentClasses().add(equivalentClass);
			}
			// Get disjoint classes of each class
			for (OWLClass disjointClass : reasoner.disjointClasses(owlClass).collect(Collectors.toSet())) {
				if (!disjointClass.equals(owlClass))
					cowlClassImpl.getDisjointClasses().add(disjointClass);
			}
			// Get instances of each class
			for (OWLNamedIndividual ind : reasoner.instances(owlClass).collect(Collectors.toSet())) {
				LinkedList<OWLNamedIndividual> individuals = cowlClassImpl.getNamedIndividuals();
				if (!individuals.contains(ind))
					individuals.add(ind);
			}

			/*
			 * Set<OWLAnonymousClassExpression> anonyClassExpSet = new
			 * HashSet<>(cowlClassImpl.getAnonymousSuperClasses().size() +
			 * cowlClassImpl.getSuperAnonymousSuperClasses().size());
			 * anonyClassExpSet.addAll(cowlClassImpl.getAnonymousSuperClasses());
			 * anonyClassExpSet.addAll(cowlClassImpl.getSuperAnonymousSuperClasses());
			 * cowlClassImpl.setAnonymousSuperClasses(anonyClassExpSet);
			 */
		}

		for (Entry<OWLDataProperty, COWLDataPropertyImpl> propertyEntry : dataPropertyMap.entrySet()) {
			OWLDataProperty owlDataProperty = propertyEntry.getKey();
			COWLDataPropertyImpl cowlDataPropertyImpl = propertyEntry.getValue();
			// Get subdataproperties of each data property
			for (OWLDataProperty subProperty : reasoner.subDataProperties(owlDataProperty)
					.collect(Collectors.toSet())) {
				if (!subProperty.isOWLBottomDataProperty())
					cowlDataPropertyImpl.getSubOWLProperties().add(dataPropertyMap.get(subProperty));
			}
			// Get superdataproperties of each data property
			for (OWLDataProperty superProperty : reasoner.superDataProperties(owlDataProperty)
					.collect(Collectors.toSet())) {
				if (!superProperty.isOWLTopDataProperty())
					cowlDataPropertyImpl.getSuperOWLProperties().add(dataPropertyMap.get(superProperty));
			}
			// Get equivalent properties of each data property
			for (OWLDataProperty equivalentProperty : reasoner.equivalentDataProperties(owlDataProperty)
					.collect(Collectors.toSet())) {
				if (!owlDataProperty.equals(equivalentProperty))
					cowlDataPropertyImpl.getEquivalentProperties().add(dataPropertyMap.get(equivalentProperty));
			}

			// Get disjoint properties of each data property
			/*
			 * for (OWLDataProperty disjointProperty :
			 * reasoner.disjointDataProperties(owlDataProperty)
			 * .collect(Collectors.toSet()))
			 * cowlDataPropertyImpl.getDisjointProperties().add(dataPropertyMap.get(
			 * disjointProperty));
			 */
		}

		for (Entry<OWLObjectProperty, COWLObjectPropertyImpl> propertyEntry : objectPropertyMap.entrySet()) {
			OWLObjectProperty owlObjectProperty = propertyEntry.getKey();
			COWLObjectPropertyImpl cowlObjectPropertyImpl = propertyEntry.getValue();
			// Get subobjectproperties of each object property
			// logger.info("Subproperties (direct and inferred) of " +
			// owlObjectProperty.getIRI().getShortForm() + " are shown as follows:");
			for (OWLObjectPropertyExpression subProperty : reasoner.subObjectProperties(owlObjectProperty)
					.collect(Collectors.toSet())) {
				if (subProperty instanceof OWLObjectProperty && !subProperty.isOWLBottomObjectProperty())
					// logger.info("\t" + subProperty.getNamedProperty().getIRI().getIRIString());
					cowlObjectPropertyImpl.getSubOWLProperties().add(objectPropertyMap.get(subProperty));
			}

			// Get superobjectproperties of each object property
			// logger.info("Superproperties (direct and inferred) of " +
			// owlObjectProperty.getIRI().getShortForm() + " are shown as follows:");
			for (OWLObjectPropertyExpression superProperty : reasoner.superObjectProperties(owlObjectProperty)
					.collect(Collectors.toSet())) {
				if (superProperty instanceof OWLObjectProperty && !superProperty.isOWLTopObjectProperty())
					// logger.info("\t" + superProperty.getNamedProperty().getIRI().getIRIString());
					cowlObjectPropertyImpl.getSuperOWLProperties().add(objectPropertyMap.get(superProperty));
			}
			// Get equivalent properties of each object property
			// logger.info("Equivalalent properties of " +
			// owlObjectProperty.getIRI().getShortForm() + " are shown as follows:");
			for (OWLObjectPropertyExpression equivalentProperty : reasoner.equivalentObjectProperties(owlObjectProperty)
					.collect(Collectors.toSet())) {
				if (equivalentProperty instanceof OWLObjectProperty && !owlObjectProperty.equals(equivalentProperty))
					// logger.info("\t" +
					// equivalentProperty.getNamedProperty().getIRI().getIRIString());
					cowlObjectPropertyImpl.getEquivalentProperties().add(objectPropertyMap.get(equivalentProperty));
			}
			// Get disjoint properties of each object property
			// logger.info("Disjoint properties of " +
			// owlObjectProperty.getIRI().getShortForm() + " are shown as follows:");
			/*
			 * for (OWLObjectPropertyExpression disjointProperty :
			 * reasoner.disjointObjectProperties(owlObjectProperty)
			 * .collect(Collectors.toSet())) { if (disjointProperty instanceof
			 * OWLObjectProperty)
			 * cowlObjectPropertyImpl.getDisjointProperties().add(objectPropertyMap.get(
			 * disjointProperty)); }
			 */

			// Get inverse properties of each object property
			// logger.info("Inverse properties of " +
			// owlObjectProperty.getIRI().getShortForm() + " are shown as follows:");

			for (OWLObjectPropertyExpression inverseProperty : reasoner.inverseObjectProperties(owlObjectProperty)
					.collect(Collectors.toSet())) {
				if (inverseProperty instanceof OWLObjectProperty)
					// logger.info("\t" +
					// inverseProperty.getNamedProperty().getIRI().getIRIString());
					cowlObjectPropertyImpl.getInverseProperties().add(objectPropertyMap.get(inverseProperty));
			}
		}

		for (COWLDataPropertyImpl propImpl : dataPropertyMap.values()) {
			recursiveExtractImplicitDisjointProperties(propImpl);
			processEquivalentProperties(propImpl);
		}

		for (COWLDataPropertyImpl propImpl : dataPropertyMap.values())
			propImpl.setVisited(false);

		for (COWLObjectPropertyImpl propImpl : objectPropertyMap.values()) {
			recursiveExtractImplicitDisjointProperties(propImpl);
			processEquivalentProperties(propImpl);
		}

		for (COWLObjectPropertyImpl propImpl : objectPropertyMap.values())
			propImpl.setVisited(false);

		for (COWLClassImpl cowlClassImpl : classMap.values()) {
			recursiveExtractAnonymousSuperClasses(cowlClassImpl);
			getSpecialClassRestrictions(cowlClassImpl);
		}

		for (COWLClassImpl cowlClassImpl : classMap.values())
			cowlClassImpl.setVisited(false);

		logger.info("Extract implicit knowledge successfully!");
		logger.info("\n" + toString());
	}

	/**
	 * This function recursively extracts anonymous super classes from direct super
	 * classes of the specified OWL named class whose conceptual model is passed by
	 * argument. The extracted anonymous super classes are added into the specified
	 * OWL named class as its anonymous super classes.
	 * 
	 * @param owlClassImpl
	 *            Conceptual model of an OWL named class.
	 */
	private void recursiveExtractAnonymousSuperClasses(COWLClassImpl owlClassImpl) {
		if (owlClassImpl.isVisited())
			return;

		Set<OWLAnonymousClassExpression> superAnonymousSuperClasses = new HashSet<>();
		for (COWLClassImpl superClass : owlClassImpl.getDirectSuperClasses()) {
			if (!superClass.isVisited())
				recursiveExtractAnonymousSuperClasses(superClass);
			// owlClassImpl.getAnonymousSuperClasses().addAll(superClass.getAnonymousSuperClasses());
			superAnonymousSuperClasses.addAll(superClass.getAnonymousSuperClasses());
		}
		// owlClassImpl.getAnonymousSuperClasses().addAll(owlClassImpl.getDirectAnonymousSuperClasses());
		Set<OWLAnonymousClassExpression> anonymousSuperClasses = new HashSet<>();
		anonymousSuperClasses.addAll(superAnonymousSuperClasses);
		anonymousSuperClasses.addAll(owlClassImpl.getDirectAnonymousSuperClasses());
		Set<OWLAnonymousClassExpression> selectedAnonymousSuperClasses = new HashSet<>();
		for (OWLAnonymousClassExpression anon : anonymousSuperClasses)
			removeRedundantClassConstraints(selectedAnonymousSuperClasses, anon);

		owlClassImpl.getAnonymousSuperClasses().addAll(selectedAnonymousSuperClasses);
		owlClassImpl.setVisited(true);
	}

	/**
	 * Filter out redundant anonymous super class expression of an OWL class.
	 * 
	 * @param selectedClassConstraints
	 *            Selected anonymous super class expressions.
	 * @param exp
	 *            Candidate anonymous super class expression.
	 */
	private void removeRedundantClassConstraints(Set<OWLAnonymousClassExpression> selectedClassConstraints,
			OWLAnonymousClassExpression exp) {
		OWLProperty targetProperty;
		OWLProperty testProperty;
		// Set<OWLAnonymousClassExpression> removedClassConstraints = new HashSet<>();
		if (exp instanceof OWLQuantifiedObjectRestriction
				&& !((OWLQuantifiedObjectRestriction) exp).getProperty().isAnonymous()) {
			targetProperty = ((OWLQuantifiedObjectRestriction) exp).getProperty().asOWLObjectProperty();
			Iterator<OWLAnonymousClassExpression> iterator = selectedClassConstraints.iterator();
			OWLAnonymousClassExpression anon;
			while (iterator.hasNext()) {
				anon = (OWLAnonymousClassExpression) iterator.next();
				if (anon instanceof OWLQuantifiedObjectRestriction
						&& !((OWLQuantifiedObjectRestriction) anon).getProperty().isAnonymous()) {
					testProperty = ((OWLQuantifiedObjectRestriction) anon).getProperty().asOWLObjectProperty();
					OWLClassExpression targetClassExp = ((OWLQuantifiedObjectRestriction) exp).getFiller();
					OWLClassExpression testClassExp = ((OWLQuantifiedObjectRestriction) anon).getFiller();

					if (anon.getClassExpressionType().equals(exp.getClassExpressionType())) {
						/*
						 * If a target class expression and a test class expression have the same OWL
						 * quantified object restriction type and have the same filler: Case 1: Remove
						 * the test class expression. If and only if its property is a super property of
						 * the one in the target class expression. Case 2: Ignore the target class
						 * expression. If and only if its property is a super property of the one in the
						 * test class expression.
						 */
						if (targetClassExp.equals(testClassExp)) {
							if (objectPropertyMap.get(testProperty).getSubOWLProperties()
									.contains(objectPropertyMap.get(targetProperty)))
								iterator.remove();
							else if (objectPropertyMap.get(targetProperty).getSubOWLProperties()
									.contains(objectPropertyMap.get(testProperty)))
								// selectedClassConstraints.removeAll(removedClassConstraints);
								return;
						} else {
							/*
							 * If a target class expression and a test class expression have the same OWL
							 * quantified object restriction type: Case 1: Remove the test class expression.
							 * If and only if its property is a super property of or equivalent to the one
							 * in the target class expression, and its filler is the super class of the one
							 * of target class expression. Case 2: Ignore the target class expression. If
							 * and only if its property is a super property of or equivalent to the one in
							 * the test class expression, and its filler is the super class of the one of
							 * test class expression.
							 */
							if (!targetClassExp.isAnonymous() && !testClassExp.isAnonymous()) {
								OWLClass targetClass = targetClassExp.asOWLClass();
								OWLClass testClass = testClassExp.asOWLClass();

								if (classMap.get(targetClass).getSubClasses().contains(classMap.get(testClass))) {
									// selectedClassConstraints.removeAll(removedClassConstraints);
									if (objectPropertyMap.get(targetProperty).getSubOWLProperties().contains(
											objectPropertyMap.get(testProperty)) || testProperty.equals(targetProperty))
										return;
								} else if (classMap.get(testClass).getSubClasses()
										.contains(classMap.get(targetClass))) {
									// selectedClassConstraints.remove(anon);
									if (objectPropertyMap.get(testProperty).getSubOWLProperties()
											.contains(objectPropertyMap.get(targetProperty))
											|| testProperty.equals(targetProperty))
										iterator.remove();
								}
							}
						}
					} else if (quantifiedObjectRestrictionPriority.get(anon.getClassExpressionType())
							.intValue() > quantifiedObjectRestrictionPriority.get(exp.getClassExpressionType())
									.intValue()) {
						if (targetClassExp.equals(testClassExp)) {
							if (testProperty.equals(targetProperty) || objectPropertyMap.get(testProperty)
									.getSubOWLProperties().contains(objectPropertyMap.get(targetProperty)))
								iterator.remove();
						}
						// TODO Other cases
					} else if (quantifiedObjectRestrictionPriority.get(anon.getClassExpressionType())
							.intValue() < quantifiedObjectRestrictionPriority.get(exp.getClassExpressionType())
									.intValue()) {
						if (targetClassExp.equals(testClassExp)) {
							if (testProperty.equals(targetProperty) || objectPropertyMap.get(targetProperty)
									.getSubOWLProperties().contains(objectPropertyMap.get(testProperty)))
								return;
						}
						// TODO Other cases
					}
				}

				else if (anon instanceof OWLObjectHasValue && !((OWLObjectHasValue) anon).getProperty().isAnonymous()) {
					testProperty = ((OWLObjectHasValue) anon).getProperty().asOWLObjectProperty();
					if (testProperty.equals(targetProperty)) {
						// selectedClassConstraints.removeAll(removedClassConstraints);
						return;
					}
					// TODO Other cases
				}
			}
		} else if (exp instanceof OWLObjectHasValue && !((OWLObjectHasValue) exp).getProperty().isAnonymous()) {
			targetProperty = ((OWLObjectHasValue) exp).getProperty().asOWLObjectProperty();
			Iterator<OWLAnonymousClassExpression> iterator = selectedClassConstraints.iterator();
			while (iterator.hasNext()) {
				OWLAnonymousClassExpression anon = iterator.next();
				if (anon instanceof OWLQuantifiedObjectRestriction
						&& !((OWLQuantifiedObjectRestriction) anon).getProperty().isAnonymous()) {
					testProperty = ((OWLQuantifiedObjectRestriction) anon).getProperty().asOWLObjectProperty();
					// Remove the tested class expression if target class expression is HasValue
					// constraint and tested class expression is an OWL quantified object
					// restriction, and both have the same property.
					if (testProperty.equals(targetProperty)) {
						// selectedClassConstraints.remove(anon);
						iterator.remove();
					}
					// TODO Other cases
				}
				/*
				 * If a target class expression and a tested class expression are both HasValue
				 * constraint: Case 1: Remove the tested class expression. If and only if its
				 * property is a super property of the one in the target class expression. Case
				 * 2: Ignore the target class expression. If its property is a super property of
				 * the one in the tested class expression. Case 3: Ignore the target class
				 * expression. If its property is the same as the one in the tested class
				 * expression and target class expression and tested class expression have same
				 * filler.
				 */
				else if (anon instanceof OWLObjectHasValue && !((OWLObjectHasValue) anon).getProperty().isAnonymous()) {
					testProperty = ((OWLObjectHasValue) anon).getProperty().asOWLObjectProperty();
					if (objectPropertyMap.get(testProperty).getSubOWLProperties()
							.contains(objectPropertyMap.get(targetProperty)))
						// selectedClassConstraints.remove(anon);
						iterator.remove();
					else if (objectPropertyMap.get(targetProperty).getSubOWLProperties()
							.contains(objectPropertyMap.get(testProperty)))
						// selectedClassConstraints.removeAll(removedClassConstraints);
						return;
					else if (testProperty.equals(targetProperty)) {
						if (((OWLObjectHasValue) anon).getFiller().equals(((OWLObjectHasValue) exp).getFiller()))
							return;
					}
				}
			}
		} else if (exp instanceof OWLQuantifiedDataRestriction
				&& !((OWLQuantifiedDataRestriction) exp).getProperty().isAnonymous()) {
			targetProperty = ((OWLQuantifiedDataRestriction) exp).getProperty().asOWLDataProperty();
			Iterator<OWLAnonymousClassExpression> iterator = selectedClassConstraints.iterator();
			OWLAnonymousClassExpression anon;
			while (iterator.hasNext()) {
				anon = (OWLAnonymousClassExpression) iterator.next();
				if (anon instanceof OWLQuantifiedDataRestriction
						&& !((OWLQuantifiedDataRestriction) anon).getProperty().isAnonymous()) {
					testProperty = ((OWLQuantifiedDataRestriction) anon).getProperty().asOWLDataProperty();
					OWLDataRange targetDataRange = ((OWLQuantifiedDataRestriction) exp).getFiller();
					OWLDataRange testDataRange = ((OWLQuantifiedDataRestriction) anon).getFiller();

					if (anon.getClassExpressionType().equals(exp.getClassExpressionType())) {
						/*
						 * If a target class expression and a test class expression have the same OWL
						 * quantified data restriction type and have the same filler: Case 1: Remove the
						 * test class expression. If and only if its property is a super property of the
						 * one in the target class expression. Case 2: Ignore the target class
						 * expression. If and only if its property is a super property of the one in the
						 * test class expression.
						 */
						if (targetDataRange.equals(testDataRange)) {
							if (dataPropertyMap.get(testProperty).getSubOWLProperties()
									.contains(dataPropertyMap.get(targetProperty)))
								iterator.remove();
							else if (dataPropertyMap.get(targetProperty).getSubOWLProperties()
									.contains(dataPropertyMap.get(testProperty)))
								// selectedClassConstraints.removeAll(removedClassConstraints);
								return;
						}
						// TODO Other cases
					} else if (quantifiedDataRestrictionPriority.get(anon.getClassExpressionType())
							.intValue() > quantifiedDataRestrictionPriority.get(exp.getClassExpressionType())
									.intValue()) {
						if (targetDataRange.equals(testDataRange)) {
							if (testProperty.equals(targetProperty) || dataPropertyMap.get(testProperty)
									.getSubOWLProperties().contains(dataPropertyMap.get(targetProperty)))
								iterator.remove();
						}
						// TODO Other cases
					} else if (quantifiedDataRestrictionPriority.get(anon.getClassExpressionType())
							.intValue() < quantifiedDataRestrictionPriority.get(exp.getClassExpressionType())
									.intValue()) {
						if (targetDataRange.equals(testDataRange)) {
							if (testProperty.equals(targetProperty) || dataPropertyMap.get(targetProperty)
									.getSubOWLProperties().contains(dataPropertyMap.get(testProperty)))
								return;
						}
						// TODO Other cases
					}
				}

				else if (anon instanceof OWLDataHasValue && !((OWLDataHasValue) anon).getProperty().isAnonymous()) {
					testProperty = ((OWLDataHasValue) anon).getProperty().asOWLDataProperty();
					if (testProperty.equals(targetProperty)) {
						// selectedClassConstraints.removeAll(removedClassConstraints);
						return;
					}
					// TODO Other cases
				}
			}
		} else if (exp instanceof OWLDataHasValue && !((OWLDataHasValue) exp).getProperty().isAnonymous()) {
			targetProperty = ((OWLDataHasValue) exp).getProperty().asOWLDataProperty();
			Iterator<OWLAnonymousClassExpression> iterator = selectedClassConstraints.iterator();
			while (iterator.hasNext()) {
				OWLAnonymousClassExpression anon = iterator.next();
				if (anon instanceof OWLQuantifiedDataRestriction
						&& !((OWLQuantifiedDataRestriction) anon).getProperty().isAnonymous()) {
					testProperty = ((OWLQuantifiedDataRestriction) anon).getProperty().asOWLDataProperty();
					// Remove the tested class expression if target class expression is HasValue
					// constraint and tested class expression is an OWL quantified data
					// restriction, and both have the same property.
					if (testProperty.equals(targetProperty)) {
						// selectedClassConstraints.remove(anon);
						iterator.remove();
					}
					// TODO Other cases
				}
				/*
				 * If a target class expression and a tested class expression are both HasValue
				 * constraint: Case 1: Remove the tested class expression. If and only if its
				 * property is a super property of the one in the target class expression. Case
				 * 2: Ignore the target class expression. If its property is a super property of
				 * the one in the tested class expression. Case 3: Ignore the target class
				 * expression. If its property is the same as the one in the tested class
				 * expression and target class expression and tested class expression have same
				 * filler.
				 */
				else if (anon instanceof OWLDataHasValue && !((OWLDataHasValue) anon).getProperty().isAnonymous()) {
					testProperty = ((OWLDataHasValue) anon).getProperty().asOWLDataProperty();
					if (dataPropertyMap.get(testProperty).getSubOWLProperties()
							.contains(dataPropertyMap.get(targetProperty)))
						// selectedClassConstraints.remove(anon);
						iterator.remove();
					else if (dataPropertyMap.get(targetProperty).getSubOWLProperties()
							.contains(dataPropertyMap.get(testProperty)))
						// selectedClassConstraints.removeAll(removedClassConstraints);
						return;
					else if (testProperty.equals(targetProperty)) {
						if (((OWLObjectHasValue) anon).getFiller().equals(((OWLObjectHasValue) exp).getFiller()))
							return;
					}
				}
			}
		}

		selectedClassConstraints.add(exp);
		return;
	}

	/**
	 * This function retrieves inferred disjoint/inverse properties of a property
	 * from its equivalent properties.
	 * 
	 * @param propImpl
	 *            Conceptual model of an OWL property.
	 */
	private void processEquivalentProperties(COWLPropertyImpl propImpl) {
		if (!propImpl.getEquivalentProperties().isEmpty()) {
			if (!propImpl.getDisjointProperties().isEmpty()) {
				Set<COWLPropertyImpl> disjointProperties = new HashSet<>();
				disjointProperties.addAll(propImpl.getDisjointProperties());
				for (COWLPropertyImpl propImpl1 : propImpl.getEquivalentProperties())
					disjointProperties.addAll(propImpl1.getDisjointProperties());
				propImpl.setDisjointProperties(disjointProperties);
				for (COWLPropertyImpl propImpl2 : propImpl.getEquivalentProperties())
					propImpl2.setDisjointProperties(disjointProperties);
			}
			if (propImpl instanceof COWLObjectPropertyImpl
					&& !((COWLObjectPropertyImpl) propImpl).getInverseProperties().isEmpty()) {
				Set<COWLPropertyImpl> inverseProperties = new HashSet<>();
				inverseProperties.addAll(((COWLObjectPropertyImpl) propImpl).getInverseProperties());
				for (COWLPropertyImpl propImpl1 : propImpl.getEquivalentProperties())
					inverseProperties.addAll(((COWLObjectPropertyImpl) propImpl1).getInverseProperties());
				propImpl.setDisjointProperties(inverseProperties);
				for (COWLPropertyImpl propImpl2 : propImpl.getEquivalentProperties())
					propImpl2.setDisjointProperties(inverseProperties);
			}
		}
	}

	/**
	 * This function generates special class restrictions of the class used for
	 * consistency check.
	 * 
	 * @param cowlClassImpl
	 *            Conceptual model of an OWL named class.
	 */
	private void getSpecialClassRestrictions(COWLClassImpl cowlClassImpl) {
		for (OWLAnonymousClassExpression anon : cowlClassImpl.getAnonymousSuperClasses()) {
			if (anon instanceof OWLObjectMaxCardinality || anon instanceof OWLObjectAllValuesFrom)
				cowlClassImpl.addASpecialClassRestriction(anon);
			else if (anon instanceof OWLObjectIntersectionOf)
				for (OWLClassExpression exp : ((OWLObjectIntersectionOf) anon).operands().collect(Collectors.toSet()))
					if (exp instanceof OWLObjectMaxCardinality || exp instanceof OWLObjectAllValuesFrom)
						cowlClassImpl.addASpecialClassRestriction((OWLAnonymousClassExpression) exp);
		}
	}

	/**
	 * This function recursively extracts disjoint properties from direct super
	 * properties of the specified OWL property whose conceptual model is passed by
	 * argument. The extracted disjoint properties are added into the specified OWL
	 * properties as its disjoint properties.
	 * 
	 * @param property
	 *            Conceptual model of an OWL property.
	 */
	private void recursiveExtractImplicitDisjointProperties(COWLPropertyImpl property) {
		if (property.isVisited())
			return;

		for (COWLPropertyImpl superProp : property.getDirectSuperOWLProperties()) {
			if (!superProp.isVisited())
				recursiveExtractImplicitDisjointProperties(superProp);
			property.getDisjointProperties().addAll(superProp.getDisjointProperties());
		}

		for (COWLPropertyImpl prop : property.getDirectDisjointProperties()) {
			property.getDisjointProperties().add(prop);
			for (COWLPropertyImpl sub : prop.getSubOWLProperties()) {
				property.getDisjointProperties().add(sub);
			}
		}
		property.setVisited(true);
	}

	/**
	 * Add an OWL named class and its conceptual model into class mapping
	 * collection.
	 * 
	 * @param oc
	 *            An OWL named class.
	 * @param cocImpl
	 *            Conceptual model of the OWL named class.
	 */
	public void addClass(OWLClass oc, COWLClassImpl cocImpl) {
		classMap.put(oc, cocImpl);
	}

	/**
	 * Add an OWL object property and its conceptual model into object property
	 * mapping collection.
	 * 
	 * @param oop
	 *            An OWL object property.
	 * @param coopImpl
	 *            Conceptual model of the OWL object property.
	 */
	public void addObjectProperty(OWLObjectProperty oop, COWLObjectPropertyImpl coopImpl) {
		objectPropertyMap.put(oop, coopImpl);
	}

	/**
	 * Add an OWL data property and its conceptual model into data property mapping
	 * collection.
	 * 
	 * @param odp
	 *            An OWL data property.
	 * @param codpImpl
	 *            Conceptual model of the OWL data property.
	 */
	public void addDataProperty(OWLDataProperty odp, COWLDataPropertyImpl codpImpl) {
		dataPropertyMap.put(odp, codpImpl);
	}

	/**
	 * Add an OWL individual into TBox individuals collection.
	 * 
	 * @param ind
	 *            An OWL named individual.
	 */
	public void addAnIndividual(OWLNamedIndividual ind) {
		existingIndividuals.add(ind);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Summary of the extracted ontology:\n");

		for (COWLClassImpl owlClassImpl : classMap.values()) {
			sb.append("A class IRI is : <" + owlClassImpl.getIRI().getIRIString() + ">\n");
			Set<COWLClassImpl> directSubClasses = owlClassImpl.getDirectSubClasses();
			if (!directSubClasses.isEmpty()) {
				sb.append("\t Direct subclasses of the class in IRI short form are shown as follows:\n");
				for (COWLClassImpl ocImpl : directSubClasses)
					sb.append("\t\t" + ocImpl.getIRI().getShortForm() + "\n");
			}

			Set<COWLClassImpl> subClasses = owlClassImpl.getSubClasses();
			if (!subClasses.isEmpty()) {
				sb.append("\t Subclasses (direct and inferred) of the class in IRI short form are shown as follows:\n");
				for (COWLClassImpl ocImpl : subClasses)
					sb.append("\t\t" + ocImpl.getIRI().getShortForm() + "\n");
			}

			Set<COWLClassImpl> directSuperClasses = owlClassImpl.getDirectSuperClasses();
			if (!directSuperClasses.isEmpty()) {
				sb.append("\t Direct superclasses of the class in IRI short form are shown as follows:\n");
				for (COWLClassImpl ocImpl : directSuperClasses)
					sb.append("\t\t" + ocImpl.getIRI().getShortForm() + "\n");
			}

			Set<COWLClassImpl> superClasses = owlClassImpl.getSuperClasses();
			if (!superClasses.isEmpty()) {
				sb.append(
						"\t Superclasses (direct and inferred) of the class in IRI short form are shown as follows:\n");
				for (COWLClassImpl ocImpl : superClasses)
					sb.append("\t\t" + ocImpl.getIRI().getShortForm() + "\n");
			}

			Set<OWLAnonymousClassExpression> anonymousSuperClasses = owlClassImpl.getAnonymousSuperClasses();
			if (!anonymousSuperClasses.isEmpty()) {
				sb.append("\t Anonymous superclasses (directed and inferred) of the class are shown as follows:\n");
				for (OWLAnonymousClassExpression acImpl : anonymousSuperClasses)
					sb.append("\t\t" + acImpl.toString() + "\n");
			}

			Set<OWLAnonymousClassExpression> directAnonymousSuperClasses = owlClassImpl
					.getDirectAnonymousSuperClasses();
			if (!directAnonymousSuperClasses.isEmpty()) {
				sb.append("\t Direct anonymous superclasses of the class are shown as follows:\n");
				for (OWLAnonymousClassExpression acImpl : directAnonymousSuperClasses)
					sb.append("\t\t" + acImpl.toString() + "\n");
			}

			/*
			 * Set<OWLAnonymousClassExpression> superAnonymousSuperClasses =
			 * owlClassImpl.getSuperAnonymousSuperClasses(); if
			 * (!superAnonymousSuperClasses.isEmpty()) { sb.
			 * append("\t Super anonymous superclasses of the class are shown as follows:\n"
			 * ); for (OWLAnonymousClassExpression acImpl : superAnonymousSuperClasses)
			 * sb.append("\t\t" + acImpl.toString() + "\n"); }
			 */

			Set<OWLAnonymousClassExpression> specialClassRestrictions = owlClassImpl.getSpecialClassRestrictions();
			if (!specialClassRestrictions.isEmpty()) {
				sb.append("\t Special anonymous class restrictions of the class are shown as follows:\n");
				for (OWLAnonymousClassExpression acImpl : specialClassRestrictions)
					sb.append("\t\t" + acImpl.toString() + "\n");
			}

			Set<OWLClassExpression> equivalentClasses = owlClassImpl.getEquivalentClasses();
			if (!equivalentClasses.isEmpty()) {
				sb.append("\t Equivalent classes (direct and inferred) of the class are shown as follows:\n");
				for (OWLClassExpression exp : equivalentClasses)
					sb.append("\t\t" + exp.toString() + "\n");
			}

			Set<OWLClassExpression> disjointClasses = owlClassImpl.getDisjointClasses();
			if (!disjointClasses.isEmpty()) {
				sb.append("\t Disjoint classes (direct and inferred) of the class are shown as follows:\n");
				for (OWLClassExpression exp : disjointClasses)
					sb.append("\t\t" + exp.toString() + "\n");
			}

			Set<Entry<OWLObjectProperty, OWLClassExpression>> objectPropertyEntrySet = owlClassImpl
					.getObjectPropertyRangesPairs().entrySet();
			if (!objectPropertyEntrySet.isEmpty()) {
				sb.append("\t Object properties of the class are shown as follows:\n");
				for (Entry<OWLObjectProperty, OWLClassExpression> ope : objectPropertyEntrySet) {
					sb.append(
							"\t\t Object property in IRI short form : " + ope.getKey().getIRI().getShortForm() + "\n");
					sb.append("\t\t\t Property value is : " + ope.getValue().toString() + "\n");
				}
			}

			Set<Entry<OWLDataProperty, OWLDataRange>> dataPropertyEntrySet = owlClassImpl.getDataPropertyRangesPairs()
					.entrySet();
			if (!dataPropertyEntrySet.isEmpty()) {
				sb.append("\t Data properties of the class are shown as follows:\n");
				for (Entry<OWLDataProperty, OWLDataRange> dpe : dataPropertyEntrySet) {
					sb.append("\t\t Data property in IRI short form : " + dpe.getKey().getIRI().getShortForm() + "\n");
					sb.append("\t\t\t Property value is : " + dpe.getValue().toString() + "\n");
				}
			}

			LinkedList<OWLNamedIndividual> individuals = owlClassImpl.getNamedIndividuals();
			if (!individuals.isEmpty()) {
				sb.append(
						"\t Named individuals assert to be of the type of the class in IRI short form are shown as follows:\n");
				for (OWLNamedIndividual ind : individuals) {
					sb.append("\t\t" + ind.getIRI().getShortForm() + "\n");
				}
			}

		}

		sb.append("\n=====================================================\n\n");
		for (COWLObjectPropertyImpl cowlObjectPropertyImpl : getObjectPropertyMap().values()) {
			sb.append("An object property in IRI short form is : " + cowlObjectPropertyImpl.getIRI().getShortForm()
					+ "\n");
			Set<COWLPropertyImpl> directSubObjectProperties = cowlObjectPropertyImpl.getDirectSubOWLProperties();
			if (!directSubObjectProperties.isEmpty()) {
				sb.append("\t Direct subproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl subProperty : directSubObjectProperties)
					sb.append("\t\t" + subProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> subObjectProperties = cowlObjectPropertyImpl.getSubOWLProperties();
			if (!subObjectProperties.isEmpty()) {
				sb.append("\t Subproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl subProperty : subObjectProperties)
					sb.append("\t\t" + subProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> directSuperObjectProperties = cowlObjectPropertyImpl.getDirectSuperOWLProperties();
			if (!directSuperObjectProperties.isEmpty()) {
				sb.append("\t Direct superproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl superProperty : directSuperObjectProperties)
					sb.append("\t\t" + superProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> superObjectProperties = cowlObjectPropertyImpl.getSuperOWLProperties();
			if (!superObjectProperties.isEmpty()) {
				sb.append("\t Superproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl superProperty : superObjectProperties)
					sb.append("\t\t" + superProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> equivalentObjectProperties = cowlObjectPropertyImpl.getEquivalentProperties();
			if (!equivalentObjectProperties.isEmpty()) {
				sb.append(
						"\t Equivalent properties (direct and inferred) of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl equivalentProperty : equivalentObjectProperties)
					sb.append("\t\t" + equivalentProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> disjointObjectProperties = cowlObjectPropertyImpl.getDisjointProperties();
			if (!disjointObjectProperties.isEmpty()) {
				sb.append(
						"\t Disjoint properties (direct and inferred) of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl disjointProperty : disjointObjectProperties)
					sb.append("\t\t" + disjointProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> directDisjointObjectProperties = cowlObjectPropertyImpl.getDirectDisjointProperties();
			if (!directDisjointObjectProperties.isEmpty()) {
				sb.append("\t Direct disjoint properties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl disjointProperty : directDisjointObjectProperties)
					sb.append("\t\t" + disjointProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLObjectPropertyImpl> inverseProperties = cowlObjectPropertyImpl.getInverseProperties();
			if (!inverseProperties.isEmpty()) {
				sb.append(
						"\t Inverse properties (direct and inferred) of the property in IRI short form are shown as follows:\n");
				for (COWLObjectPropertyImpl inverseProperty : inverseProperties)
					sb.append("\t\t" + inverseProperty.getIRI().getShortForm() + "\n");
			}

			Set<AxiomType<? extends OWLAxiom>> propertyAttributes = cowlObjectPropertyImpl.getPropertyAttributes();
			if (!propertyAttributes.isEmpty()) {
				sb.append("\t Characteristics of the property are shown as follows:\n");
				for (AxiomType<? extends OWLAxiom> axiomType : propertyAttributes)
					sb.append("\t\t" + axiomType.getName() + "\n");
			}
		}

		sb.append("\n=====================================================\n\n");
		for (COWLDataPropertyImpl cowlDataPropertyImpl : getDataPropertyMap().values()) {
			sb.append("A data property in IRI short form is : " + cowlDataPropertyImpl.getIRI().getShortForm() + "\n");
			Set<COWLPropertyImpl> directSubDataProperties = cowlDataPropertyImpl.getDirectSubOWLProperties();
			if (!directSubDataProperties.isEmpty()) {
				sb.append("\t Direct subproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl subProperty : directSubDataProperties)
					sb.append("\t\t" + subProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> subDataProperties = cowlDataPropertyImpl.getSubOWLProperties();
			if (!subDataProperties.isEmpty()) {
				sb.append("\t Subproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl subProperty : subDataProperties)
					sb.append("\t\t" + subProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> directSuperDataProperties = cowlDataPropertyImpl.getDirectSuperOWLProperties();
			if (!directSuperDataProperties.isEmpty()) {
				sb.append("\t Direct superproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl superProperty : directSuperDataProperties)
					sb.append("\t\t" + superProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> superDataProperties = cowlDataPropertyImpl.getSuperOWLProperties();
			if (!superDataProperties.isEmpty()) {
				sb.append("\t Superproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl superProperty : superDataProperties)
					sb.append("\t\t" + superProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> equivalentDataProperties = cowlDataPropertyImpl.getEquivalentProperties();
			if (!equivalentDataProperties.isEmpty()) {
				sb.append(
						"\t Equivalent properties (direct and inferred) of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl equivalentProperty : equivalentDataProperties)
					sb.append("\t\t" + equivalentProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> disjointDataProperties = cowlDataPropertyImpl.getDisjointProperties();
			if (!disjointDataProperties.isEmpty()) {
				sb.append(
						"\t Disjoint properties (direct and inferred) of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl disjointProperty : disjointDataProperties)
					sb.append("\t\t" + disjointProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> directDisjointDataProperties = cowlDataPropertyImpl.getDisjointProperties();
			if (!directDisjointDataProperties.isEmpty()) {
				sb.append("\t Direct disjoint properties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl disjointProperty : directDisjointDataProperties)
					sb.append("\t\t" + disjointProperty.getIRI().getShortForm() + "\n");
			}

			Set<AxiomType<? extends OWLAxiom>> propertyAttributes = cowlDataPropertyImpl.getPropertyAttributes();
			if (!propertyAttributes.isEmpty()) {
				sb.append("\t Characteristics of the property are shown as follows:\n");
				for (AxiomType<? extends OWLAxiom> axiomType : propertyAttributes)
					sb.append("\t\t" + axiomType.getName() + "\n");
			}
		}

		return sb.toString();
	}
}
