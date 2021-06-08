package edu.neu.ece.objectdescriptiongenerator.visitor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLQuantifiedDataRestriction;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.objectdescriptiongenerator.entity.classes.COWLClassImpl;
import edu.neu.ece.objectdescriptiongenerator.generator.ObjectDescriptionGenerator;
import edu.neu.ece.objectdescriptiongenerator.generator.IRIGenerator;
import edu.neu.ece.objectdescriptiongenerator.utility.CollectionUtil;
import edu.neu.ece.objectdescriptiongenerator.utility.MathUtil;

/**
 * Instances of this class generates datasets based on specified class
 * expression axiom, including ObjectIntersectionOf axiom, ObjectUnionOf axiom,
 * ObjectComplementOf axiom, ObjectOneOf axiom, ObjectSomeValuesFrom axiom,
 * ObjectAllValuesFrom axiom, ObjectHasValue axiom, ObjectHasSelf axiom,
 * ObjectMinCardinality axiom, ObjectMaxCardinality axiom,
 * ObjectExactCardinality axiom, DataSomeValuesFrom axiom, DataAllValuesFrom
 * axiom, DataHasValue axiom, DataMinCardinality axiom, DataMaxCardinality axiom
 * and DataExactCardinality axiom.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class COWLClassExpressionVisitor implements OWLClassExpressionVisitor {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Object description generator.
	 */
	private final ObjectDescriptionGenerator generator;

	/**
	 * Ontology IRI in string of generated RDF object descriptions.
	 */
	private final String OUTPUT_ONTOLOGY_IRI_IN_STRING;

	/**
	 * OWL named individual of type of the specified class expression.
	 */
	private final OWLNamedIndividual ind;

	/**
	 * RDF type of the OWL named individual ind, null if not known.
	 */
	private OWLClass owlCls;

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
	 * Selected root class in the input ontology as the entry for dataset
	 * generation.
	 */
	private COWLClassImpl rootClass;

	/**
	 * Used to generate a stream of pesudorandom numbers.
	 */
	private final Random ran;

	/**
	 * Hold of an ontology manager.
	 */
	private final OWLOntologyManager manager;

	/**
	 * An OWL data factory object used to create entities, class expressions and
	 * axioms.
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
	 * Created OWL named individuals.
	 */
	private final Set<OWLNamedIndividual> newIndividuals;

	/**
	 * Constructor
	 * 
	 * @param ind
	 *            OWL named individual of type of the specified class expression.
	 * @param owlClass
	 *            RDF type of the OWL named individual ind.
	 * @param generator
	 *            Object description generator.
	 */
	public COWLClassExpressionVisitor(OWLNamedIndividual ind, OWLClass owlClass, ObjectDescriptionGenerator generator) {
		this.ind = ind;
		this.owlCls = owlClass;
		this.generator = generator;
		OUTPUT_ONTOLOGY_IRI_IN_STRING = generator.getOutputOntologyIRIInString();
		objectPropertyAssertionProbability = generator.getObjectPropertyAssertionProbability();
		dataPropertyAssertionProbability = generator.getDataPropertyAssertionProbability();
		// devNumber = generator.getDevNumber();
		rootClass = generator.getRootClass();
		ran = generator.getRan();
		manager = generator.getManager();
		factory = generator.getFactory();
		outputOntology = generator.getOutputOntology();
		classMap = generator.getClassMap();
		newIndividuals = generator.getNewIndividuals();
	}

	@Override
	public void doDefault(Object object) {
		logger.warn("Unsupported class expression: " + object);
	}

	@Override
	public void visit(OWLObjectIntersectionOf ce) {
		Set<OWLClassExpression> operands = ce.operands().collect(Collectors.toSet());
		Set<OWLClassExpression> selectedOperands = CollectionUtil
				.getARandomElementFromList(CollectionUtil.getAllSubSetsOfASet(operands), ran);
		for (OWLClassExpression exp : selectedOperands)
			processNaryBooleanClassExpression(exp);
	}

	@Override
	public void visit(OWLObjectUnionOf ce) {
		OWLClassExpression exp = CollectionUtil.getARandomElementFromSet(ce.operands().collect(Collectors.toSet()),
				ran);
		processNaryBooleanClassExpression(exp);
	}

	/**
	 * This function defines how to process nary boolean class expression, including
	 * OWLObjectIntersectionOf axiom and OWLObjectUnionOf axiom.
	 * 
	 * @param exp
	 *            Nary boolean class expression.
	 */
	private void processNaryBooleanClassExpression(OWLClassExpression exp) {
		if (owlCls != null) {
			COWLClassImpl owlClsImpl = classMap.get(owlCls);
			if (owlClsImpl.getDisjointClasses().contains(exp))
				return;
		}
		if (!exp.isAnonymous()) {
			OWLClass owlClass = exp.asOWLClass();
			COWLClassImpl owlClassImpl = classMap.get(owlClass);
			if (rootClass.getSubClassesAndItself().contains(owlClassImpl)) {
				/*
				 * if (owlCls != null) { OWLClassAssertionAxiom classAssertionAxiom =
				 * factory.getOWLClassAssertionAxiom(exp.asOWLClass(), ind);
				 * manager.addAxiom(outputOntology, classAssertionAxiom); return; } else { for
				 * (COWLClassImpl sub : owlClassImpl.getSubClassesandItself()) { if
				 * (!sub.getNamedIndividuals().isEmpty()) { OWLNamedIndividual individual =
				 * CollectionUtil .getARandomElementFromList(sub.getNamedIndividuals(), ran);
				 * OWLSameIndividualAxiom sameIndividualAxiom =
				 * factory.getOWLSameIndividualAxiom(ind, individual);
				 * manager.addAxiom(outputOntology, sameIndividualAxiom); return; } } return;
				 * 
				 * }
				 */
				return;
			}
			if (owlCls == null)
				owlCls = owlClass;
			generator.generateClassAssertionAxiom(ind, owlClass);
		} else
			exp.accept(this);
	}

	@Override
	public void visit(OWLObjectComplementOf ce) {
		OWLClassExpression exp = ce.getOperand();
		if (!exp.isAnonymous()) {
			LinkedList<OWLNamedIndividual> individuals = classMap.get(exp.asOWLClass()).getNamedIndividuals(true);
			if (!individuals.isEmpty()) {
				OWLNamedIndividual individual = CollectionUtil.getARandomElementFromList(individuals, ran);
				OWLDifferentIndividualsAxiom differentIndividualAxiom = factory
						.getOWLDifferentIndividualsAxiom(individual, ind);
				manager.addAxiom(outputOntology, differentIndividualAxiom);
			}
		} else
			logger.warn("Anonymous class expression from OWLObjectComplementOf " + ce + "will be ignored");
	}

	@Override
	public void visit(OWLObjectOneOf ce) {
		OWLIndividual individual = CollectionUtil.getARandomElementFromSet(ce.individuals().collect(Collectors.toSet()),
				ran);
		if (!individual.isAnonymous()) {
			OWLSameIndividualAxiom sameIndividualAxiom = factory.getOWLSameIndividualAxiom(ind, individual);
			manager.addAxiom(outputOntology, sameIndividualAxiom);
		} else
			logger.warn("Anonymous individual from OWLObjectOneOf " + ce + "will be ignored");
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom ce) {
		processObjectPropertyQuantificationRestriction(ce);
	}

	@Override
	public void visit(OWLObjectAllValuesFrom ce) {
		processObjectPropertyQuantificationRestriction(ce);
	}

	/**
	 * This function defines how to process cardinality-based data property
	 * restriction, including ObjectSomeValuesFrom axiom, ObjectAllValuesFrom axiom,
	 * ObjectMinCardinality axiom, ObjectMaxCardinality axiom and
	 * ObjectExactCardinality axiom.
	 * 
	 * @param ce
	 *            Cardinality-based object property restriction.
	 */
	private void processObjectPropertyQuantificationRestriction(OWLQuantifiedObjectRestriction ce) {
		if (ran.nextDouble() < objectPropertyAssertionProbability) {
			OWLObjectPropertyExpression objectPropertyExp = ce.getProperty();
			OWLObjectProperty objectProperty;
			// Process object property expression.
			if (!objectPropertyExp.isAnonymous())
				objectProperty = objectPropertyExp.asOWLObjectProperty();
			else {
				OWLObjectPropertyExpression simplified = objectPropertyExp.getSimplified();
				if (simplified.isAnonymous())
					objectProperty = simplified.getInverseProperty().asOWLObjectProperty();
				else
					objectProperty = simplified.asOWLObjectProperty();
			}

			OWLClassExpression classExp = ce.getFiller();
			OWLNamedIndividual individual = null;
			// OWLObjectPropertyAssertionAxiom propertyAssertion;
			if (!classExp.isAnonymous()) {
				OWLClass owlClass = classExp.asOWLClass();
				// COWLClassImpl owlClassImpl = generator.getClassMap().get(owlClass);

				COWLClassImpl owlClassImpl = classMap.get(owlClass);
				if (rootClass.getSubClassesAndItself().contains(owlClassImpl)) {
					for (COWLClassImpl sub : owlClassImpl.getSubClassesAndItself())
						if (!sub.getNamedIndividuals(true).isEmpty()) {
							individual = CollectionUtil.getARandomElementFromList(sub.getNamedIndividuals(true), ran);
							generator.generateObjectPropertyAssertionAxiom(objectProperty, ind,
									(OWLNamedIndividual) individual);
							return;
						}
					return;
				}

				if (owlClassImpl != null) {
					for (COWLClassImpl clsImpl : owlClassImpl.getSubClassesAndItself()) {
						for (OWLAnonymousClassExpression anon : clsImpl.getSpecialClassRestrictions()) {
							if (anon instanceof OWLObjectMaxCardinality) {
								OWLClassExpression exp = ((OWLObjectMaxCardinality) anon).getFiller();
								OWLObjectPropertyExpression objectPropExp = ((OWLObjectMaxCardinality) anon)
										.getProperty();
								OWLObjectProperty objectProp;
								int cardinality = ((OWLObjectMaxCardinality) anon).getCardinality();
								if (!objectPropExp.isAnonymous())
									objectProp = objectPropExp.asOWLObjectProperty();
								else {
									OWLObjectPropertyExpression simplified = objectPropExp.getSimplified();
									if (simplified.isAnonymous())
										objectProp = simplified.getInverseProperty().asOWLObjectProperty();
									else
										objectProp = simplified.asOWLObjectProperty();
								}
								if (generator.getObjectPropertyMap().get(objectProp).getInverseProperties()
										.contains(generator.getObjectPropertyMap().get(objectProperty))
										&& !exp.isAnonymous() && owlCls != null
										&& generator.getClassMap().get(exp.asOWLClass()).getSubClassesAndItself()
												.contains(generator.getClassMap().get(owlCls))) {
									for (OWLNamedIndividual localInd1 : generator.getReasoner()
											.getInstances(factory.getOWLClass(clsImpl.getIRI()), false).entities()
											.collect(Collectors.toSet())) {
										int count = 0;
										/*
										 * for (OWLNamedIndividual localInd2 : generator.getReasoner()
										 * .getInstances(exp, false).entities().collect(Collectors.toSet())) {
										 */
										LinkedList<OWLNamedIndividual> testIndividuals = classMap.get(exp.asOWLClass())
												.getNamedIndividuals(false);
										for (OWLNamedIndividual ind : generator.getReasoner().getInstances(exp, false)
												.entities().collect(Collectors.toSet()))
											if (!testIndividuals.contains(ind))
												testIndividuals.add(ind);
										for (OWLNamedIndividual localInd2 : testIndividuals) {
											boolean flag = false;
											flag = generator.containsRelevantObjectPropertyAssertionAxiom(
													generator.getObjectPropertyMap().get(objectProp), localInd1,
													localInd2, new HashSet<OWLObjectPropertyAssertionAxiom>());
											if (flag == true)
												count++;
											if (count == cardinality)
												break;
										}
										logger.info("Class restriction: " + anon.toString() + ", count = " + count
												+ ", cardinality = " + cardinality);
										if (count < cardinality) {
											individual = localInd1;
											break;
										}
									}
									if (individual == null) {
										individual = IRIGenerator.generateOWLIndividual(OUTPUT_ONTOLOGY_IRI_IN_STRING,
												factory, clsImpl);
										newIndividuals.add(individual);
										clsImpl.getNamedIndividuals(true).add(individual);
										clsImpl.setVisited(true);
										// Create OWL class assertion axiom if the condition satisfies.
										generator.generateClassAssertionAxiom(individual,
												factory.getOWLClass(clsImpl.getIRI()));
									}
									break;
								}
							} else if (anon instanceof OWLObjectAllValuesFrom) {
								OWLClassExpression exp = ((OWLObjectAllValuesFrom) anon).getFiller();
								OWLObjectPropertyExpression objectPropExp = ((OWLObjectAllValuesFrom) anon)
										.getProperty();
								OWLObjectProperty objectProp;

								if (!objectPropExp.isAnonymous())
									objectProp = objectPropExp.asOWLObjectProperty();
								else {
									OWLObjectPropertyExpression simplified = objectPropExp.getSimplified();
									if (simplified.isAnonymous())
										objectProp = simplified.getInverseProperty().asOWLObjectProperty();
									else
										objectProp = simplified.asOWLObjectProperty();
								}
								if (generator.getObjectPropertyMap().get(objectProp).getInverseProperties()
										.contains(generator.getObjectPropertyMap().get(objectProperty))
										&& !exp.isAnonymous() && owlCls != null
										&& generator.getClassMap().get(exp.asOWLClass()).getSubClassesAndItself()
												.contains(generator.getClassMap().get(owlCls))) {
									if (!clsImpl.getNamedIndividuals(true).isEmpty()) {
										individual = CollectionUtil
												.getARandomElementFromList(clsImpl.getNamedIndividuals(true), ran);
									} else {
										individual = IRIGenerator.generateOWLIndividual(OUTPUT_ONTOLOGY_IRI_IN_STRING,
												factory, clsImpl);
										newIndividuals.add(individual);
										clsImpl.getNamedIndividuals(true).add(individual);
										clsImpl.setVisited(true);
										// Create OWL class assertion axiom if the condition satisfies.
										generator.generateClassAssertionAxiom(individual,
												factory.getOWLClass(clsImpl.getIRI()));
									}
									break;
								}
							}
						}
						if (individual != null)
							break;
					}
				}
				if (individual == null) {
					individual = generator.createResursiveLinkedRDFNode(owlClass, false);
				}
				if (individual != null)
					generator.generateObjectPropertyAssertionAxiom(objectProperty, ind,
							(OWLNamedIndividual) individual);
			} else {
				/*
				 * if (classExp instanceof OWLObjectOneOf) { individual =
				 * CollectionUtil.getARandomElementFromSet( ((OWLObjectOneOf)
				 * classExp).individuals().collect(Collectors.toSet()), ran); if
				 * (!individual.isAnonymous()) {
				 * generator.processObjectPropertyAssertionAxiom(objectProperty, ind,
				 * (OWLNamedIndividual) individual); } else
				 * logger.warn("Anonymous individual from OWLObjectOneOf " + ce +
				 * "will be ignored"); } else {
				 */
				/*
				 * if (classExp instanceof OWLObjectIntersectionOf) { for (OWLClassExpression
				 * cls : ((OWLObjectIntersectionOf) classExp).operands()
				 * .collect(Collectors.toSet())) { if (!cls.isAnonymous()) { COWLClassImpl
				 * owlClass = classMap.get(cls.asOWLClass()); if
				 * (rootClass.getSubClassesandItself().contains(owlClass)) { for (COWLClassImpl
				 * sub : owlClass.getSubClassesandItself()) if
				 * (!sub.getNamedIndividuals().isEmpty()) { individual =
				 * CollectionUtil.getARandomElementFromList(sub.getNamedIndividuals(), ran);
				 * generator.generateObjectPropertyAssertionAxiom(objectProperty, ind,
				 * (OWLNamedIndividual) individual); return; } } } } }
				 */
				individual = IRIGenerator.generateOWLIndividual(OUTPUT_ONTOLOGY_IRI_IN_STRING, factory);
				newIndividuals.add((OWLNamedIndividual) individual);
				// Recursively visit class expressions as filler of the class expression
				classExp.accept(new COWLClassExpressionVisitor((OWLNamedIndividual) individual, null, generator));
				generator.generateObjectPropertyAssertionAxiom(objectProperty, ind, (OWLNamedIndividual) individual);
			}
		}
	}

	@Override
	public void visit(OWLObjectHasValue ce) {
		if (ran.nextDouble() < objectPropertyAssertionProbability) {
			OWLObjectPropertyExpression objectPropertyExp = ce.getProperty();
			OWLObjectProperty objectProperty;
			if (!objectPropertyExp.isAnonymous())
				objectProperty = objectPropertyExp.asOWLObjectProperty();
			else {
				OWLObjectPropertyExpression simplified = objectPropertyExp.getSimplified();
				if (simplified.isAnonymous())
					objectProperty = simplified.getInverseProperty().asOWLObjectProperty();
				else
					objectProperty = simplified.asOWLObjectProperty();
			}
			OWLIndividual individual = ce.getFiller();
			// OWLObjectPropertyAssertionAxiom propertyAssertion;
			if (!individual.isAnonymous()) {
				generator.generateObjectPropertyAssertionAxiom(objectProperty, ind, individual.asOWLNamedIndividual());
			} else
				logger.warn("Anonymous individual from OWLObjectHasValue " + ce + "will be ignored");
		}
	}

	@Override
	public void visit(OWLObjectHasSelf ce) {
		if (ran.nextDouble() < objectPropertyAssertionProbability) {
			OWLObjectPropertyExpression objectPropertyExp = ce.getProperty();
			OWLObjectProperty objectProperty;
			if (!objectPropertyExp.isAnonymous())
				objectProperty = objectPropertyExp.asOWLObjectProperty();
			else {
				OWLObjectPropertyExpression simplified = objectPropertyExp.getSimplified();
				if (simplified.isAnonymous())
					objectProperty = simplified.getInverseProperty().asOWLObjectProperty();
				else
					objectProperty = simplified.asOWLObjectProperty();
			}
			generator.generateObjectPropertyAssertionAxiom(objectProperty, ind, ind);
		}
	}

	@Override
	public void visit(OWLObjectMinCardinality ce) {
		int count = ce.getCardinality();
		try {
			count = MathUtil.getRandomIntegerInRange(count, 2 * count, ran);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < count; i++)
			processObjectPropertyQuantificationRestriction(ce);
	}

	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		int count = ce.getCardinality();
		try {
			count = MathUtil.getRandomIntegerInRange(0, count + 1, ran);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < count; i++)
			processObjectPropertyQuantificationRestriction(ce);
	}

	@Override
	public void visit(OWLObjectExactCardinality ce) {
		int count = ce.getCardinality();
		for (int i = 0; i < count; i++)
			processObjectPropertyQuantificationRestriction(ce);
	}

	@Override
	public void visit(OWLDataSomeValuesFrom ce) {
		processDataPropertyQuantificationRestriction(ce);
	}

	@Override
	public void visit(OWLDataAllValuesFrom ce) {
		processDataPropertyQuantificationRestriction(ce);
	}

	/**
	 * This function defines how to process cardinality-based data property
	 * restriction, including DataSomeValuesFrom axiom, DataAllValuesFrom axiom,
	 * DataMinCardinality axiom, DataMaxCardinality axiom and DataExactCardinality
	 * axiom.
	 * 
	 * @param ce
	 *            Cardinality-based data property restriction.
	 */
	private void processDataPropertyQuantificationRestriction(OWLQuantifiedDataRestriction ce) {
		if (ran.nextDouble() < dataPropertyAssertionProbability
				|| ce.getClassExpressionType() == ClassExpressionType.DATA_EXACT_CARDINALITY) {
			OWLDataProperty dataProperty = ce.getProperty().asOWLDataProperty();

			OWLDataRange dataRange = ce.getFiller();
			// OWLDataPropertyAssertionAxiom propertyAssertion;
			dataRange.accept(new COWLDataRangeVisitor(ind, dataProperty, generator));
		}
	}

	@Override
	public void visit(OWLDataHasValue ce) {
		if (ran.nextDouble() < dataPropertyAssertionProbability) {
			OWLDataProperty dataProperty = ce.getProperty().asOWLDataProperty();
			OWLLiteral literal = ce.getFiller();
			OWLDataPropertyAssertionAxiom propertyAssertion;
			propertyAssertion = factory.getOWLDataPropertyAssertionAxiom(dataProperty, ind, literal);
			manager.addAxiom(outputOntology, propertyAssertion);
		}
	}

	@Override
	public void visit(OWLDataMinCardinality ce) {
		int count = ce.getCardinality();
		try {
			count = MathUtil.getRandomIntegerInRange(count, 2 * count, ran);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < count; i++)
			processDataPropertyQuantificationRestriction(ce);
	}

	@Override
	public void visit(OWLDataMaxCardinality ce) {
		int count = ce.getCardinality();
		try {
			count = MathUtil.getRandomIntegerInRange(0, count + 1, ran);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < count; i++)
			processDataPropertyQuantificationRestriction(ce);
	}

	@Override
	public void visit(OWLDataExactCardinality ce) {
		int count = ce.getCardinality();
		for (int i = 0; i < count; i++)
			processDataPropertyQuantificationRestriction(ce);
	}
}