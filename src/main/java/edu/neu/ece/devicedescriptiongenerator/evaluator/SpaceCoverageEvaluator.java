package edu.neu.ece.devicedescriptiongenerator.evaluator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.devicedescriptiongenerator.entity.classes.COWLClassImpl;
import edu.neu.ece.devicedescriptiongenerator.entity.properties.COWLDataPropertyImpl;
import edu.neu.ece.devicedescriptiongenerator.entity.properties.COWLObjectPropertyImpl;
import edu.neu.ece.devicedescriptiongenerator.entity.properties.COWLPropertyImpl;
import edu.neu.ece.devicedescriptiongenerator.generator.DeviceDescriptionGenerator;
import edu.neu.ece.devicedescriptiongenerator.utility.MathUtil;
import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataExactCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataHasValueImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataMaxCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataMinCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNegativeDataPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNegativeObjectPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectExactCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectHasSelfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectHasValueImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectInverseOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMaxCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMinCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectOneOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;

/**
 * This class is defined exclusively for space coverage evaluation.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2019-04-16
 *
 */
public class SpaceCoverageEvaluator {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * The number of device descriptions.
	 */
	private final int devNumber;

	/**
	 * OWL data factory for creating entities, class expressions and axioms.
	 */
	private final OWLDataFactory factory;

	/**
	 * Output ontology.
	 */
	private final OWLOntology outputOntology;

	/**
	 * Container that stores key-value pairs, where OWL API interface OWLClass is
	 * the key and the customized class COWLClassImpl is the value.
	 */
	private final Map<OWLClass, COWLClassImpl> classMap;

	/**
	 * Container that stores key-value pairs, where OWL API interface
	 * OWLDataProperty is the key and the customized class COWLDataPropertyImpl is
	 * the value.
	 */
	private final Map<OWLDataProperty, COWLDataPropertyImpl> dataPropertyMap;

	/**
	 * Container that stores key-value pairs, where OWL API interface
	 * OWLObjectProperty is the key and the customized class COWLObjectPropertyImpl
	 * is the value.
	 */
	private final Map<OWLObjectProperty, COWLObjectPropertyImpl> objectPropertyMap;

	/**
	 * Mapping from focus class to its count (the number of OWL individuals that are
	 * asserted to the type of the class) in datasets.
	 */
	private Map<OWLClassImpl, Integer> targetClassAndCountMap = new TreeMap<>();

	/**
	 * Non-focus classes that are used in the datasets for testing purpose.
	 */
	private Set<OWLClassImpl> nonTargetClasses = new TreeSet<>();

	/**
	 * Mapping from focus object property to its count (the number of triples whose
	 * predicates are the object property) in the datasets.
	 */
	private Map<OWLObjectPropertyImpl, Integer> targetObjectPropertyAndCountMap = new TreeMap<>();

	/**
	 * Non-focus object properties that are used in the datasets for testing
	 * purpose.
	 */
	private Set<OWLObjectPropertyImpl> nonTargetObjectProperties = new TreeSet<>();

	/**
	 * Mapping from focus data property to its count (the number of triples whose
	 * predicates are the data property) in the datasets.
	 */
	private Map<OWLDataPropertyImpl, Integer> targetDataPropertyAndCountMap = new TreeMap<>();

	/**
	 * Non-focus data properties that are used in the datasets.
	 */
	private Set<OWLDataPropertyImpl> nonTargetDataProperties = new TreeSet<>();

	/**
	 * The nodes that are visited while traversing the model graph from the root
	 * class .
	 */
	private Set<Object> visitedNodes = new HashSet<>();

	/**
	 * Constructor
	 * 
	 * @param generator
	 *            Device description generator.
	 */
	public SpaceCoverageEvaluator(DeviceDescriptionGenerator generator) {
		this.devNumber = generator.getDevNumber();
		factory = generator.getFactory();
		outputOntology = generator.getOutputOntology();

		classMap = generator.getClassMap();
		dataPropertyMap = generator.getDataPropertyMap();
		objectPropertyMap = generator.getObjectPropertyMap();
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLClass is the key and the customized class COWLClassImpl is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLClass, COWLClassImpl> getClassMap() {
		return Collections.unmodifiableMap(classMap);
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLDataProperty is the key and the customized class COWLDataPropertyImpl is
	 * the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLDataProperty, COWLDataPropertyImpl> getDataPropertyMap() {
		return Collections.unmodifiableMap(dataPropertyMap);
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLObjectProperty is the key and the customized class COWLObjectPropertyImpl
	 * is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLObjectProperty, COWLObjectPropertyImpl> getObjectPropertyMap() {
		return Collections.unmodifiableMap(objectPropertyMap);
	}

	/**
	 * Get the mapping from target OWL class to its count (the number of OWL
	 * individuals that are asserted to the type of the class) in datasets.
	 * 
	 * @return The container.
	 */
	public Map<OWLClassImpl, Integer> getTargetClassAndCountMap() {
		return targetClassAndCountMap;
	}

	/**
	 * Get a set of OWL classes that are not selected as target OWL classes, but are
	 * used in the datasets.
	 * 
	 * @return Non target OWL classes.
	 */
	public Set<OWLClassImpl> getNonTargetClasses() {
		return nonTargetClasses;
	}

	/**
	 * Get the mapping from focus object property to its count (the number of
	 * triples whose predicates are the object property) in the datasets.
	 * 
	 * @return Focus object property to count mapper.
	 */
	public Map<OWLObjectPropertyImpl, Integer> getTargetObjectPropertyAndCountMap() {
		return targetObjectPropertyAndCountMap;
	}

	/**
	 * Get non-focus object properties that are used in the datasets for testing
	 * purpose.
	 * 
	 * @return Non-focus object properties.
	 */
	public Set<OWLObjectPropertyImpl> getNonTargetObjectProperties() {
		return nonTargetObjectProperties;
	}

	/**
	 * Mapping from focus data property to its count (the number of triples whose
	 * predicates are the data property) in the datasets.
	 * 
	 * @return Focus data property to count mapper.
	 */
	public Map<OWLDataPropertyImpl, Integer> getTargetDataPropertyAndCountMap() {
		return targetDataPropertyAndCountMap;
	}

	/**
	 * Get non-focus data properties that are used in the datasets for testing
	 * purpose.
	 * 
	 * @return Non-focus data properties.
	 */
	public Set<OWLDataPropertyImpl> getNonTargetDataProperties() {
		return nonTargetDataProperties;
	}

	/**
	 * This function defines control flow of space coverage evaluation.
	 * 
	 * @param ocImpl
	 *            OWL class.
	 */
	public void evaluateSpaceCoverage(COWLClassImpl ocImpl) {
		logger.info("Begin evaluating space coverage of datasets...");
		findTargetSignatures(factory.getOWLClass(ocImpl.getIRI()));
		collectMetrics();
	}

	/**
	 * This function utilizes DFS algorithm to find target signatures in which
	 * concepts are reachable from the root class.
	 * 
	 * @param <T>
	 *            The class of the node.
	 * @param node
	 *            Node of generic type T.
	 */
	public <T> void findTargetSignatures(T node) {
		if (node == null)
			return;
		visitedNodes.add(node);

		if (node instanceof OWLClassImpl) {
			OWLClassImpl clsImpl = (OWLClassImpl) node;
			if (clsImpl.isOWLThing() || clsImpl.isOWLNothing())
				return;
			targetClassAndCountMap.put(clsImpl, new Integer(0));
			COWLClassImpl owlClsImpl = classMap.get(clsImpl);
			for (COWLClassImpl sub : owlClsImpl.getSubClasses()) {
				OWLClass cls = factory.getOWLClass(sub.getIRI());
				if (!visitedNodes.contains(cls))
					findTargetSignatures(cls);
			}
			for (COWLClassImpl sup : owlClsImpl.getSuperClasses()) {
				OWLClass cls = factory.getOWLClass(sup.getIRI());
				if (!visitedNodes.contains(cls))
					findTargetSignatures(cls);
			}
			for (OWLClassExpression exp : owlClsImpl.getEquivalentClasses())
				if (!visitedNodes.contains(exp))
					findTargetSignatures(exp);
			for (OWLClassExpression exp : owlClsImpl.getDisjointClasses())
				if (!visitedNodes.contains(exp))
					findTargetSignatures(exp);
			for (OWLClassExpression exp : owlClsImpl.getAnonymousSuperClasses())
				if (!visitedNodes.contains(exp))
					findTargetSignatures(exp);
			for (Entry<OWLObjectProperty, OWLClassExpression> entry : owlClsImpl.getObjectPropertyRangesPairs()
					.entrySet())
				if (!visitedNodes.contains(entry))
					findTargetSignatures(entry);
			for (Entry<OWLDataProperty, OWLDataRange> entry : owlClsImpl.getDataPropertyRangesPairs().entrySet())
				if (!visitedNodes.contains(entry))
					findTargetSignatures(entry);
		} else if (node instanceof OWLObjectIntersectionOfImpl) {
			for (OWLClassExpression exp : ((OWLObjectIntersectionOfImpl) node).operands().collect(Collectors.toSet()))
				if (!visitedNodes.contains(exp))
					findTargetSignatures(exp);
		} else if (node instanceof OWLObjectUnionOfImpl) {
			for (OWLClassExpression exp : ((OWLObjectUnionOfImpl) node).operands().collect(Collectors.toSet()))
				if (!visitedNodes.contains(exp))
					findTargetSignatures(exp);
		} else if (node instanceof OWLObjectComplementOfImpl) {
			OWLClassExpression exp = ((OWLObjectComplementOfImpl) node).getOperand();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
		} else if (node instanceof OWLObjectOneOfImpl) {
			return;
		} else if (node instanceof OWLObjectSomeValuesFromImpl) {
			OWLObjectSomeValuesFromImpl res = (OWLObjectSomeValuesFromImpl) node;
			OWLClassExpression exp = res.getFiller();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectAllValuesFromImpl) {
			OWLObjectAllValuesFromImpl res = (OWLObjectAllValuesFromImpl) node;
			OWLClassExpression exp = res.getFiller();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectHasValueImpl) {
			OWLObjectHasValueImpl res = (OWLObjectHasValueImpl) node;
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectHasSelfImpl) {
			OWLObjectHasSelfImpl res = (OWLObjectHasSelfImpl) node;
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectMinCardinalityImpl) {
			OWLObjectMinCardinalityImpl res = (OWLObjectMinCardinalityImpl) node;
			OWLClassExpression exp = res.getFiller();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectMaxCardinalityImpl) {
			OWLObjectMaxCardinalityImpl res = (OWLObjectMaxCardinalityImpl) node;
			OWLClassExpression exp = res.getFiller();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectExactCardinalityImpl) {
			OWLObjectExactCardinalityImpl res = (OWLObjectExactCardinalityImpl) node;
			OWLClassExpression exp = res.getFiller();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataSomeValuesFromImpl) {
			OWLDataPropertyExpression prop = ((OWLDataSomeValuesFromImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataAllValuesFromImpl) {
			OWLDataPropertyExpression prop = ((OWLDataAllValuesFromImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataHasValueImpl) {
			OWLDataPropertyExpression prop = ((OWLDataHasValueImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataMinCardinalityImpl) {
			OWLDataPropertyExpression prop = ((OWLDataMinCardinalityImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataMaxCardinalityImpl) {
			OWLDataPropertyExpression prop = ((OWLDataMaxCardinalityImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataExactCardinalityImpl) {
			OWLDataPropertyExpression prop = ((OWLDataExactCardinalityImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectInverseOfImpl) {
			OWLObjectProperty prop = ((OWLObjectInverseOfImpl) node).getInverse();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectPropertyImpl) {
			OWLObjectPropertyImpl propImpl = (OWLObjectPropertyImpl) node;
			if (propImpl.isOWLTopObjectProperty() || propImpl.isOWLBottomObjectProperty())
				return;
			targetObjectPropertyAndCountMap.put(propImpl, new Integer(0));
			COWLObjectPropertyImpl owlPropImpl = objectPropertyMap.get(propImpl);

			for (COWLPropertyImpl sub : owlPropImpl.getSubOWLProperties()) {
				OWLObjectProperty prop = factory.getOWLObjectProperty(sub.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl sup : owlPropImpl.getSuperOWLProperties()) {
				OWLObjectProperty prop = factory.getOWLObjectProperty(sup.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl eq : owlPropImpl.getEquivalentProperties()) {
				OWLObjectProperty prop = factory.getOWLObjectProperty(eq.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl dis : owlPropImpl.getDisjointProperties()) {
				OWLObjectProperty prop = factory.getOWLObjectProperty(dis.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl inv : owlPropImpl.getInverseProperties()) {
				OWLObjectProperty prop = factory.getOWLObjectProperty(inv.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
		} else if (node instanceof OWLDataPropertyImpl) {
			OWLDataPropertyImpl propImpl = (OWLDataPropertyImpl) node;
			if (propImpl.isOWLTopDataProperty() || propImpl.isOWLBottomDataProperty())
				return;
			targetDataPropertyAndCountMap.put(propImpl, new Integer(0));
			COWLDataPropertyImpl owlPropImpl = dataPropertyMap.get(propImpl);
			for (COWLPropertyImpl sub : owlPropImpl.getSubOWLProperties()) {
				OWLDataProperty prop = factory.getOWLDataProperty(sub.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl sup : owlPropImpl.getSuperOWLProperties()) {
				OWLDataProperty prop = factory.getOWLDataProperty(sup.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl eq : owlPropImpl.getEquivalentProperties()) {
				OWLDataProperty prop = factory.getOWLDataProperty(eq.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl dis : owlPropImpl.getDisjointProperties()) {
				OWLDataProperty prop = factory.getOWLDataProperty(dis.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
		} else if (node instanceof Entry<?, ?>) {
			Entry<?, ?> entry = (Entry<?, ?>) node;
			Object key = entry.getKey();
			if (!visitedNodes.contains(key))
				findTargetSignatures(key);
			Object value = entry.getValue();
			if (!visitedNodes.contains(value))
				findTargetSignatures(value);
		} else if (node instanceof OWLDataRange)
			return;
		else {
			logger.error("Node of type " + node.getClass().getName()
					+ " is ignored in the process of finding target signatures!");
		}
	}

	/**
	 * Collect space coverage metrics. The metrics include distribution of
	 * individuals per class (DIPC), distribution of data properties (DDP),
	 * distribution of object properties (DOP), class coverage (CC), data property
	 * coverage (DPC) and object property coverage (OPC).
	 */
	private void collectMetrics() {
		for (OWLAxiom axiom : outputOntology.axioms(Imports.EXCLUDED).collect(Collectors.toSet())) {
			if (axiom instanceof OWLClassAssertionAxiomImpl) {
				OWLClassExpression clsExp = ((OWLClassAssertionAxiomImpl) axiom).getClassExpression();
				if (!clsExp.isAnonymous()) {
					OWLClass cls = clsExp.asOWLClass();
					if (targetClassAndCountMap.containsKey(cls)) {
						int count = targetClassAndCountMap.get(cls).intValue();
						targetClassAndCountMap.replace((OWLClassImpl) cls, new Integer(++count));
					} else
						nonTargetClasses.add((OWLClassImpl) cls);
				}
			} else if (axiom instanceof OWLObjectPropertyAssertionAxiomImpl) {
				OWLObjectPropertyExpression propExp = ((OWLObjectPropertyAssertionAxiomImpl) axiom).getProperty();
				if (!propExp.isAnonymous()) {
					OWLObjectProperty prop = propExp.asOWLObjectProperty();
					if (targetObjectPropertyAndCountMap.containsKey(prop)) {
						int count = targetObjectPropertyAndCountMap.get(prop).intValue();
						targetObjectPropertyAndCountMap.replace((OWLObjectPropertyImpl) prop, new Integer(++count));
					} else
						nonTargetObjectProperties.add((OWLObjectPropertyImpl) prop);
				}
			} else if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiomImpl) {
				OWLObjectPropertyExpression propExp = ((OWLNegativeObjectPropertyAssertionAxiomImpl) axiom)
						.getProperty();
				if (!propExp.isAnonymous()) {
					OWLObjectProperty prop = propExp.asOWLObjectProperty();
					if (targetObjectPropertyAndCountMap.containsKey(prop)) {
						int count = targetObjectPropertyAndCountMap.get(prop).intValue();
						targetObjectPropertyAndCountMap.replace((OWLObjectPropertyImpl) prop, new Integer(++count));
					} else
						nonTargetObjectProperties.add((OWLObjectPropertyImpl) prop);
				}
			} else if (axiom instanceof OWLDataPropertyAssertionAxiomImpl) {
				OWLDataProperty prop = ((OWLDataPropertyAssertionAxiomImpl) axiom).getProperty().asOWLDataProperty();
				if (targetDataPropertyAndCountMap.containsKey(prop)) {
					int count = targetDataPropertyAndCountMap.get(prop).intValue();
					targetDataPropertyAndCountMap.replace((OWLDataPropertyImpl) prop, new Integer(++count));
				} else
					nonTargetDataProperties.add((OWLDataPropertyImpl) prop);
			} else if (axiom instanceof OWLNegativeDataPropertyAssertionAxiomImpl) {
				OWLDataProperty prop = ((OWLNegativeDataPropertyAssertionAxiomImpl) axiom).getProperty()
						.asOWLDataProperty();
				if (targetDataPropertyAndCountMap.containsKey(prop)) {
					int count = targetDataPropertyAndCountMap.get(prop).intValue();
					targetDataPropertyAndCountMap.replace((OWLDataPropertyImpl) prop, new Integer(++count));
				} else
					nonTargetDataProperties.add((OWLDataPropertyImpl) prop);
			}
		}

		try {
			double dipc = MathUtil.calculateStandardDeviationOfNormalizedDistribution(targetClassAndCountMap.values());
			double ddp = MathUtil
					.calculateStandardDeviationOfNormalizedDistribution(targetDataPropertyAndCountMap.values());
			double dop = MathUtil
					.calculateStandardDeviationOfNormalizedDistribution(targetObjectPropertyAndCountMap.values());
			double cc = MathUtil.calculateSpaceCoverage(targetClassAndCountMap.values());
			double dpc = MathUtil.calculateSpaceCoverage(targetDataPropertyAndCountMap.values());
			double opc = MathUtil.calculateSpaceCoverage(targetObjectPropertyAndCountMap.values());
			printToFile(dipc, ddp, dop, cc, dpc, opc);
		} catch (Exception e) {
			logger.error("There was an error while calcuating space coverage evaluation metrics.", e);
		}
	}

	/**
	 * Dump metrics information to file.
	 * 
	 * @param dipc
	 *            Distribution of individuals per class (DIPC).
	 * @param ddp
	 *            Distribution of data properties (DDP).
	 * @param dop
	 *            Distribution of object properties (DOP).
	 * @param cc
	 *            Class coverage (CC).
	 * @param dpc
	 *            Data property coverage (DPC).
	 * @param opc
	 *            Object property coverage (OPC).
	 */
	private void printToFile(double dipc, double ddp, double dop, double cc, double dpc, double opc) {
		StringBuffer outputs = new StringBuffer();
		// Output format
		String format = "%-8d %-100s\n";
		outputs.append(targetClassAndCountMap.keySet().size() + " out of " + classMap.keySet().size()
				+ " OWL classes are selected:\n");
		for (Entry<OWLClassImpl, Integer> classEntry : targetClassAndCountMap.entrySet()) {
			outputs.append(String.format(format, classEntry.getValue().intValue(),
					classEntry.getKey().getIRI().getIRIString()));
		}
		if (!nonTargetClasses.isEmpty()) {
			outputs.append(
					"\n\n" + nonTargetClasses.size() + " OWL classes are not selected but are used in the datasets:\n");
			for (OWLClass cls : nonTargetClasses)
				outputs.append(cls.getIRI().getIRIString() + "\n");
		}
		outputs.append("\n\n" + targetObjectPropertyAndCountMap.keySet().size() + " out of "
				+ objectPropertyMap.keySet().size() + " OWL object properties are selected:\n");
		for (Entry<OWLObjectPropertyImpl, Integer> propertyEntry : targetObjectPropertyAndCountMap.entrySet()) {
			outputs.append(String.format(format, propertyEntry.getValue().intValue(),
					propertyEntry.getKey().getIRI().getIRIString()));
		}
		if (!nonTargetObjectProperties.isEmpty()) {
			outputs.append("\n\n" + nonTargetObjectProperties.size()
					+ " OWL object properties are not selected but are used in the datasets:\n");
			for (OWLObjectProperty prop : nonTargetObjectProperties)
				outputs.append(prop.getIRI().getIRIString() + "\n");
		}
		outputs.append("\n\n" + targetDataPropertyAndCountMap.keySet().size() + " out of "
				+ dataPropertyMap.keySet().size() + " OWL data properties are selected:\n");
		for (Entry<OWLDataPropertyImpl, Integer> propertyEntry : targetDataPropertyAndCountMap.entrySet()) {
			outputs.append(String.format(format, propertyEntry.getValue().intValue(),
					propertyEntry.getKey().getIRI().getIRIString()));
		}
		if (!nonTargetDataProperties.isEmpty()) {
			outputs.append("\n\n" + nonTargetDataProperties.size()
					+ " OWL data properties are not selected but are used in the datasets:\n");
			for (OWLDataProperty prop : nonTargetDataProperties)
				outputs.append(prop.getIRI().getIRIString() + "\n");
		}

		DecimalFormat df = new DecimalFormat("#.####");
		// df.setRoundingMode(RoundingMode.CEILING);
		outputs.append("\n\nSpace coverage of datasets are summarized below:\n");
		outputs.append("Distribution of individuals per class (DIPC) = " + df.format(dipc) + "\n");
		outputs.append("Distribution of data properties (DDP) = " + df.format(ddp) + "\n");
		outputs.append("Distribution of object properties (DOP) = " + df.format(dop) + "\n");
		df = new DecimalFormat("#,##0.00%");
		outputs.append("Class coverage (CC) = " + df.format(cc) + "\n");
		outputs.append("Data property coverage (DPC) = " + df.format(dpc) + "\n");
		outputs.append("Object property coverage (OPC) = " + df.format(opc));

		String evaluationFilePath = "evaluationresults" + File.separator
				+ "SpaceCoverageEvaluationResults_DeviceDescription" + devNumber + ".txt";
		File outputFile = new File(evaluationFilePath);
		try {
			FileUtils.writeStringToFile(outputFile, outputs.toString(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("There was an error while dumping into evaluation results file.", e);
		}
		logger.info("Evaluation results are dumped into local file: " + outputFile.getAbsolutePath());
		logger.info("Done!");
	}
}
