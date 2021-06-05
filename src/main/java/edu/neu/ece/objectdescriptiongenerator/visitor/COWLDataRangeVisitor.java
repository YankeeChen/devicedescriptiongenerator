package edu.neu.ece.objectdescriptiongenerator.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataRangeVisitor;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.AxiomAnnotations;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mifmif.common.regex.Generex;

import edu.neu.ece.objectdescriptiongenerator.entity.properties.COWLDataPropertyImpl;
import edu.neu.ece.objectdescriptiongenerator.entity.properties.COWLPropertyImpl;
import edu.neu.ece.objectdescriptiongenerator.generator.ObjectDescriptionGenerator;
import edu.neu.ece.objectdescriptiongenerator.utility.CollectionUtil;
import edu.neu.ece.objectdescriptiongenerator.utility.MathUtil;

/**
 * An instance of this class generates datasets based on specified OWL data
 * range, including DataComplementOf, DataOneOf, Datatype, Datatype restriction,
 * DataIntersectionOf and DataUnionOf.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class COWLDataRangeVisitor implements OWLDataRangeVisitor {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * OWL named individual.
	 */
	private final OWLNamedIndividual ind;

	/**
	 * Data property.
	 */
	private final OWLDataProperty dataProperty;

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
	 * Container that stores key-value pairs, where OWL API interface
	 * OWLDataProperty is the key and the customized class COWLDataPropertyImpl is
	 * the value.
	 */
	private final Map<OWLDataProperty, COWLDataPropertyImpl> dataPropertyMap;

	/**
	 * The probability of selecting an equivalent data property (direct or inferred)
	 * of a data property for data property assertion generation; 0.5 by default.
	 */
	private final double equivalentDataPropertySelectionProbability;

	/**
	 * The probability of selecting a disjoint data property (directed or inferred)
	 * of a data property for negative data property assertion generation; 0.8 by
	 * default.
	 */
	private final double disjointDataPropertySelectionProbability;

	/**
	 * The probability of selecting a super data property (directed or inferred) of
	 * a data property for data property assertion generation; 0.5 by default.
	 */
	private final double superDataPropertySelectionProbability;

	/**
	 * Constructor
	 * 
	 * @param ind
	 *            OWL named individual.
	 * @param dataProperty
	 *            Data property.
	 * @param generator
	 *            Object description generator.
	 */
	public COWLDataRangeVisitor(OWLNamedIndividual ind, OWLDataProperty dataProperty,
			ObjectDescriptionGenerator generator) {
		this.ind = ind;
		this.dataProperty = dataProperty;
		ran = generator.getRan();
		manager = generator.getManager();
		factory = generator.getFactory();
		outputOntology = generator.getOutputOntology();
		dataPropertyMap = generator.getDataPropertyMap();
		equivalentDataPropertySelectionProbability = generator.getEquivalentDataPropertySelectionProbability();
		disjointDataPropertySelectionProbability = generator.getDisjointDataPropertySelectionProbability();
		superDataPropertySelectionProbability = generator.getSuperDataPropertySelectionProbability();
	}

	@Override
	public void doDefault(Object object) {
		logger.warn("Unsupported data range: " + object);
	}

	@Override
	public void visit(OWLDataComplementOf dr) {
		OWLDataRange dataRange = dr.getDataRange();
		if (dataRange.isOWLDatatype()) {
			OWLDatatype dt = dataRange.asOWLDatatype();
			if (dt.isBuiltIn()) {
				OWL2Datatype d2t = dt.getBuiltInDatatype();
				OWLLiteral literal;
				try {
					literal = getARandomOWLLiteral(d2t);
					if (literal != null) {
						OWLNegativeDataPropertyAssertionAxiom negativePropertyAssertion = factory
								.getOWLNegativeDataPropertyAssertionAxiom(dataProperty, ind, literal);
						manager.addAxiom(outputOntology, negativePropertyAssertion);
					}
					return;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.warn("None built-in data type " + dt + " is not supported.");
			return;
		}
		logger.warn("Unsupported anonymous OWLDataComplmentOfAxiom: " + dr);
	}

	@Override
	public void visit(OWLDataOneOf dr) {
		OWLLiteral literal = CollectionUtil.getARandomElementFromSet(dr.values().collect(Collectors.toSet()), ran);
		generateDataPropertyAssertionAxiom(literal);
	}

	@Override
	public void visit(OWLDatatype dr) {
		OWLDataRange range = dataPropertyMap.get(dataProperty).getOWLDataRange();
		if (range != null && range instanceof OWLDatatype && !range.equals(dr))
			dr = (OWLDatatype) range;
		if (!dr.isBuiltIn()) {
			logger.warn("None built-in data type " + dr + " is not supported.");
			return;
		}
		OWL2Datatype d2t = dr.getBuiltInDatatype();
		OWLLiteral literal;
		try {
			literal = getARandomOWLLiteral(d2t);
			generateDataPropertyAssertionAxiom(literal);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void visit(OWLDatatypeRestriction dr) {
		OWLDatatype dt = dr.getDatatype();
		if (!dt.isBuiltIn()) {
			logger.warn("None built-in data type " + dr + " is not supported.");
			return;
		}
		OWL2Datatype d2t = dt.getBuiltInDatatype();
		Set<OWLFacetRestriction> facetRestrictions = dr.facetRestrictions().collect(Collectors.toSet());
		OWLLiteral literal;
		try {
			literal = getARandomOWLLiteral(d2t, facetRestrictions);
			generateDataPropertyAssertionAxiom(literal);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get a random OWL literal from the specified OWL 2 datatype.
	 * 
	 * @param dt
	 *            The specified OWL 2 datatype.
	 * @return Literal in OWL 2 specification.
	 * @throws Exception
	 */
	private OWLLiteral getARandomOWLLiteral(OWL2Datatype dt) throws Exception {
		OWLLiteral literal = null;
		switch (dt) {
		case RDFS_LITERAL:
			literal = factory.getOWLLiteral("literal", OWL2Datatype.RDFS_LITERAL);
			break;
		case XSD_BOOLEAN:
			literal = factory.getOWLLiteral(MathUtil.getRandomBoolean(ran));
			break;
		case XSD_DECIMAL:
			literal = factory.getOWLLiteral(MathUtil.getRandomDecimalInString(-100, 100, ran),
					OWL2Datatype.XSD_DECIMAL);
			break;
		case XSD_DOUBLE:
			literal = factory.getOWLLiteral(MathUtil.getRandomDoubleInRange(-100, 100, ran));
			break;
		case XSD_FLOAT:
			literal = factory.getOWLLiteral(MathUtil.getRandomFloatInRange(-100, 100, ran));
			break;
		case XSD_INT:
			literal = factory.getOWLLiteral(
					String.valueOf(MathUtil.getRandomIntegerInRange(Integer.MIN_VALUE, Integer.MAX_VALUE, ran)),
					OWL2Datatype.XSD_INT);
			break;
		case XSD_INTEGER:
			literal = factory.getOWLLiteral(
					String.valueOf(MathUtil.getRandomLongInRange(Long.MIN_VALUE, Long.MAX_VALUE, ran)),
					OWL2Datatype.XSD_INTEGER);
			break;
		case XSD_NON_NEGATIVE_INTEGER:
			literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(0, Long.MAX_VALUE, ran)),
					OWL2Datatype.XSD_INTEGER);
			break;
		case XSD_POSITIVE_INTEGER:
			literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(1, Long.MAX_VALUE, ran)),
					OWL2Datatype.XSD_INTEGER);
			break;
		case XSD_STRING:
			literal = factory.getOWLLiteral(MathUtil.getRandomString(1, 10, ran));
			break;
		default:
			logger.warn("Unsupported OWL 2 data type: " + dt);
		}
		return literal;
	}

	/**
	 * Get a random OWL literal from the specified OWL 2 datatype restriction.
	 * 
	 * @param dt
	 *            The specified OWL 2 datatype restriction.
	 * @param facetRestrictions
	 *            OWL facet restrictions.
	 * @return Literal in the OWL 2 specification.
	 * @throws Exception
	 */
	private OWLLiteral getARandomOWLLiteral(OWL2Datatype dt, Set<OWLFacetRestriction> facetRestrictions)
			throws Exception {
		OWLLiteral literal = null;
		Map<OWLFacet, OWLLiteral> facets = new HashMap<>();
		for (OWLFacetRestriction facetRestriction : facetRestrictions)
			facets.put(facetRestriction.getFacet(), facetRestriction.getFacetValue());
		switch (dt) {
		case XSD_DECIMAL:
			if (facets.size() == 2) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(
							MathUtil.getRandomDecimalInString(
									Double.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).doubleValue(),
									Double.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).doubleValue(), ran),
							OWL2Datatype.XSD_DECIMAL);
				} else if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(
							MathUtil.getRandomDecimalInString(
									Double.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).doubleValue(),
									Double.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).doubleValue(), ran),
							OWL2Datatype.XSD_DECIMAL);
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(
							MathUtil.getRandomDecimalInString(
									Double.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).doubleValue()
											+ 0.0001,
									Double.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).doubleValue(), ran),
							OWL2Datatype.XSD_DECIMAL);
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(
							MathUtil.getRandomDecimalInString(
									Double.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).doubleValue()
											+ 0.0001,
									Double.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).doubleValue(), ran),
							OWL2Datatype.XSD_DECIMAL);
				}
			} else if (facets.size() == 1) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE))
					literal = factory.getOWLLiteral(MathUtil.getRandomDecimalInString(
							Double.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).doubleValue(), 10000, ran),
							OWL2Datatype.XSD_DECIMAL);
				else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE))
					literal = factory.getOWLLiteral(MathUtil.getRandomDecimalInString(
							Double.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).doubleValue() + 0.0001,
							10000, ran), OWL2Datatype.XSD_DECIMAL);
				else if (facets.containsKey(OWLFacet.MAX_INCLUSIVE))
					literal = factory.getOWLLiteral(
							MathUtil.getRandomDecimalInString(-10000,
									Double.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).doubleValue(), ran),
							OWL2Datatype.XSD_DECIMAL);
				else
					literal = factory.getOWLLiteral(
							MathUtil.getRandomDecimalInString(-10000,
									Double.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).doubleValue(), ran),
							OWL2Datatype.XSD_DECIMAL);
			}
			break;

		case XSD_DOUBLE:
			if (facets.size() == 2) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(MathUtil.getRandomDoubleInRange(
							Double.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).doubleValue(),
							Double.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).doubleValue(), ran));
				} else if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(MathUtil.getRandomDoubleInRange(
							Double.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).doubleValue(),
							Double.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).doubleValue(), ran));
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(MathUtil.getRandomDoubleInRange(
							Double.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).doubleValue() + 0.0001,
							Double.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).doubleValue(), ran));
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(MathUtil.getRandomDoubleInRange(
							Double.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).doubleValue() + 0.0001,
							Double.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).doubleValue(), ran));
				}
			} else if (facets.size() == 1) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE))
					literal = factory.getOWLLiteral(MathUtil.getRandomDoubleInRange(
							Double.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).doubleValue(), 10000, ran));
				else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE))
					literal = factory.getOWLLiteral(MathUtil.getRandomDoubleInRange(
							Double.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).doubleValue() + 0.0001,
							10000, ran));
				else if (facets.containsKey(OWLFacet.MAX_INCLUSIVE))
					literal = factory.getOWLLiteral(MathUtil.getRandomDoubleInRange(-10000,
							Double.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).doubleValue(), ran));
				else
					literal = factory.getOWLLiteral(MathUtil.getRandomDoubleInRange(-10000,
							Double.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).doubleValue(), ran));
			}
			break;

		case XSD_FLOAT:
			if (facets.size() == 2) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(MathUtil.getRandomFloatInRange(
							Float.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).floatValue(),
							Float.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).floatValue(), ran));
				} else if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(MathUtil.getRandomFloatInRange(
							Float.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()),
							Float.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()), ran));
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(MathUtil.getRandomFloatInRange(
							Float.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()) + 0.0001f,
							Float.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()), ran));
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(MathUtil.getRandomFloatInRange(
							Float.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()) + 0.0001f,
							Float.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()), ran));
				}
			} else if (facets.size() == 1) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE))
					literal = factory.getOWLLiteral(MathUtil.getRandomFloatInRange(
							Float.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()), 10000f, ran));
				else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE))
					literal = factory.getOWLLiteral(MathUtil.getRandomFloatInRange(
							Float.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()) + 0.0001f, 10000f, ran));
				else if (facets.containsKey(OWLFacet.MAX_INCLUSIVE))
					literal = factory.getOWLLiteral(MathUtil.getRandomFloatInRange(-10000f,
							Float.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()), ran));
				else
					literal = factory.getOWLLiteral(MathUtil.getRandomFloatInRange(-10000f,
							Float.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()), ran));
			}
			break;

		case XSD_INT:
			if (facets.size() == 2) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomIntegerInRange(
							Integer.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).intValue(),
							Integer.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).intValue() + 1, ran)),
							OWL2Datatype.XSD_INT);
				} else if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomIntegerInRange(
									Integer.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).intValue(),
									Integer.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).intValue(), ran)),
							OWL2Datatype.XSD_INT);
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomIntegerInRange(
							Integer.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).intValue() + 1,
							Integer.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).intValue() + 1, ran)),
							OWL2Datatype.XSD_INT);
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomIntegerInRange(
									Integer.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).intValue() + 1,
									Integer.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).intValue(), ran)),
							OWL2Datatype.XSD_INT);
				}
			} else if (facets.size() == 1) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE))
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomIntegerInRange(
							Integer.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).intValue(),
							Integer.MAX_VALUE, ran)), OWL2Datatype.XSD_INT);
				else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE))
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomIntegerInRange(
							Integer.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()) + 1, Integer.MAX_VALUE,
							ran)), OWL2Datatype.XSD_INT);
				else if (facets.containsKey(OWLFacet.MAX_INCLUSIVE))
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomIntegerInRange(Integer.MIN_VALUE,
							Integer.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).intValue() + 1, ran)),
							OWL2Datatype.XSD_INT);
				else
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomIntegerInRange(Integer.MIN_VALUE,
									Integer.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).intValue(), ran)),
							OWL2Datatype.XSD_INT);
			}
			break;

		case XSD_INTEGER:
			if (facets.size() == 2) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).longValue(),
							Long.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).longValue() + 1, ran)),
							OWL2Datatype.XSD_INTEGER);
				} else if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomLongInRange(
									Long.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).longValue(),
									Long.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).longValue(), ran)),
							OWL2Datatype.XSD_INTEGER);
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).longValue() + 1,
							Long.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).longValue() + 1, ran)),
							OWL2Datatype.XSD_INTEGER);
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomLongInRange(
									Long.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).longValue() + 1,
									Long.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).longValue(), ran)),
							OWL2Datatype.XSD_INTEGER);
				}
			} else if (facets.size() == 1) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE))
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).longValue(), Long.MAX_VALUE,
							ran)), OWL2Datatype.XSD_INTEGER);
				else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE))
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()) + 1, Long.MAX_VALUE, ran)),
							OWL2Datatype.XSD_INTEGER);
				else if (facets.containsKey(OWLFacet.MAX_INCLUSIVE))
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(Long.MIN_VALUE,
							Long.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).longValue() + 1, ran)),
							OWL2Datatype.XSD_INTEGER);
				else
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomLongInRange(Long.MIN_VALUE,
									Long.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).longValue(), ran)),
							OWL2Datatype.XSD_INTEGER);
			}
			break;

		case XSD_NON_NEGATIVE_INTEGER:
			if (facets.size() == 2) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).longValue(),
							Long.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).longValue() + 1, ran)),
							OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
				} else if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomLongInRange(
									Long.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).longValue(),
									Long.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).longValue(), ran)),
							OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).longValue() + 1,
							Long.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).longValue() + 1, ran)),
							OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomLongInRange(
									Long.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).longValue() + 1,
									Long.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).longValue(), ran)),
							OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
				}
			} else if (facets.size() == 1) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE))
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).longValue(), Long.MAX_VALUE,
							ran)), OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
				else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE))
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()) + 1, Long.MAX_VALUE, ran)),
							OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
				else if (facets.containsKey(OWLFacet.MAX_INCLUSIVE))
					literal = factory
							.getOWLLiteral(
									String.valueOf(
											MathUtil.getRandomLongInRange(0,
													Long.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral())
															.longValue() + 1,
													ran)),
									OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
				else
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomLongInRange(0,
									Long.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).longValue(), ran)),
							OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
			}
			break;
		case XSD_POSITIVE_INTEGER:
			if (facets.size() == 2) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).longValue(),
							Long.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).longValue() + 1, ran)),
							OWL2Datatype.XSD_POSITIVE_INTEGER);
				} else if (facets.containsKey(OWLFacet.MIN_INCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomLongInRange(
									Long.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).longValue(),
									Long.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).longValue(), ran)),
							OWL2Datatype.XSD_POSITIVE_INTEGER);
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_INCLUSIVE)) {
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).longValue() + 1,
							Long.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).longValue() + 1, ran)),
							OWL2Datatype.XSD_POSITIVE_INTEGER);
				} else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE) && facets.containsKey(OWLFacet.MAX_EXCLUSIVE)) {
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomLongInRange(
									Long.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()).longValue() + 1,
									Long.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).longValue(), ran)),
							OWL2Datatype.XSD_POSITIVE_INTEGER);
				}
			} else if (facets.size() == 1) {
				if (facets.containsKey(OWLFacet.MIN_INCLUSIVE))
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_INCLUSIVE).getLiteral()).longValue(), Long.MAX_VALUE,
							ran)), OWL2Datatype.XSD_POSITIVE_INTEGER);
				else if (facets.containsKey(OWLFacet.MIN_EXCLUSIVE))
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(
							Long.valueOf(facets.get(OWLFacet.MIN_EXCLUSIVE).getLiteral()) + 1, Long.MAX_VALUE, ran)),
							OWL2Datatype.XSD_POSITIVE_INTEGER);
				else if (facets.containsKey(OWLFacet.MAX_INCLUSIVE))
					literal = factory.getOWLLiteral(String.valueOf(MathUtil.getRandomLongInRange(1,
							Long.valueOf(facets.get(OWLFacet.MAX_INCLUSIVE).getLiteral()).longValue() + 1, ran)),
							OWL2Datatype.XSD_POSITIVE_INTEGER);
				else
					literal = factory.getOWLLiteral(
							String.valueOf(MathUtil.getRandomLongInRange(1,
									Long.valueOf(facets.get(OWLFacet.MAX_EXCLUSIVE).getLiteral()).longValue(), ran)),
							OWL2Datatype.XSD_POSITIVE_INTEGER);
			}
			break;
		case XSD_STRING:
			if (facets.size() == 2) {
				literal = factory.getOWLLiteral(MathUtil.getRandomString(
						Integer.valueOf(facets.get(OWLFacet.MIN_LENGTH).getLiteral()).intValue(),
						Integer.valueOf(facets.get(OWLFacet.MAX_LENGTH).getLiteral()).intValue() + 1, ran));
			} else if (facets.size() == 1) {
				if (facets.containsKey(OWLFacet.LENGTH))
					literal = factory.getOWLLiteral(MathUtil.getRandomString(
							Integer.valueOf(facets.get(OWLFacet.LENGTH).getLiteral()).intValue(),
							Integer.valueOf(facets.get(OWLFacet.LENGTH).getLiteral()).intValue() + 1, ran));
				else if (facets.containsKey(OWLFacet.MIN_LENGTH))
					literal = factory.getOWLLiteral(MathUtil.getRandomString(
							Integer.valueOf(facets.get(OWLFacet.MIN_LENGTH).getLiteral()).intValue(), 100, ran));
				else if (facets.containsKey(OWLFacet.MAX_LENGTH))
					literal = factory.getOWLLiteral(MathUtil.getRandomString(1,
							Integer.valueOf(facets.get(OWLFacet.MAX_LENGTH).getLiteral()).intValue() + 1, ran));
				else {
					Generex generator = new Generex(facets.get(OWLFacet.PATTERN).getLiteral());
					literal = factory.getOWLLiteral(generator.random());
				}
			}
		default:
			logger.warn("Unsupported OWL 2 data type: " + dt);
		}
		return literal;
	}

	@Override
	public void visit(OWLDataIntersectionOf dr) {
		doDefault(dr);
	}

	@Override
	public void visit(OWLDataUnionOf dr) {
		OWLDataRange dataRange = CollectionUtil.getARandomElementFromSet(dr.operands().collect(Collectors.toSet()),
				ran);
		if (dataRange.isOWLDatatype()) {
			OWLDatatype dt = dataRange.asOWLDatatype();
			if (dt.isBuiltIn()) {
				OWL2Datatype d2t = dt.getBuiltInDatatype();
				OWLLiteral literal;
				try {
					literal = getARandomOWLLiteral(d2t);
					generateDataPropertyAssertionAxiom(literal);
					return;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.warn("None built-in data type " + dt + " is not supported.");
			return;
		}
		logger.warn("Unsupported nested anonymous OWLDataRange " + dataRange + " from " + dr);
	}

	/**
	 * Generate data property assertion axiom.
	 * 
	 * @param literal
	 *            OWL literal.
	 */
	private void generateDataPropertyAssertionAxiom(OWLLiteral literal) {
		if (literal == null)
			return;

		COWLPropertyImpl dataPropertyImpl = dataPropertyMap.get(dataProperty);
		Set<COWLPropertyImpl> disjointProperties = dataPropertyImpl.getDisjointProperties();

		for (COWLPropertyImpl disjointProp : disjointProperties)
			if (containsAxiom(disjointProp, ind, literal))
				return;

		OWLDataPropertyAssertionAxiom propertyAssertion = factory.getOWLDataPropertyAssertionAxiom(dataProperty, ind,
				literal);
		manager.addAxiom(outputOntology, propertyAssertion);

		if (!disjointProperties.isEmpty() && ran.nextDouble() < disjointDataPropertySelectionProbability) {
			OWLNegativeDataPropertyAssertionAxiom negativePropertyAssertion = factory
					.getOWLNegativeDataPropertyAssertionAxiom(
							factory.getOWLDataProperty(
									CollectionUtil.getARandomElementFromSet(disjointProperties, ran).getIRI()),
							ind, literal);
			manager.addAxiom(outputOntology, negativePropertyAssertion);
		}

		OWLDataProperty dataProp = dataProperty;
		Set<COWLPropertyImpl> superProperties = dataPropertyImpl.getSuperOWLProperties();
		if (!superProperties.isEmpty() && ran.nextDouble() < superDataPropertySelectionProbability) {
			dataProp = factory
					.getOWLDataProperty(CollectionUtil.getARandomElementFromSet(superProperties, ran).getIRI());
			propertyAssertion = factory.getOWLDataPropertyAssertionAxiom(dataProp, ind, literal);
			manager.addAxiom(outputOntology, propertyAssertion);
		}

		Set<COWLPropertyImpl> equivalentProperties = dataPropertyImpl.getEquivalentProperties();
		if (!equivalentProperties.isEmpty() && ran.nextDouble() < equivalentDataPropertySelectionProbability) {
			dataProp = factory
					.getOWLDataProperty(CollectionUtil.getARandomElementFromSet(equivalentProperties, ran).getIRI());
			propertyAssertion = factory.getOWLDataPropertyAssertionAxiom(dataProp, ind, literal);
			manager.addAxiom(outputOntology, propertyAssertion);
		}
	}

	/**
	 * This function detects whether the output ontology contains the specified data
	 * property assertion and its relevant data property assertions.
	 * 
	 * @param property
	 *            Customization of OWL object property.
	 * @param ind
	 *            OWL named individual.
	 * @param literal
	 *            OWL literal.
	 * @return true if there exists such data property assertions, false otherwise.
	 */
	public boolean containsAxiom(COWLPropertyImpl property, OWLNamedIndividual ind, OWLLiteral literal) {
		if (property == null || ind == null || literal == null)
			return false;
		Set<COWLPropertyImpl> properties = new HashSet<>();
		properties.add(property);
		properties.addAll(property.getSubOWLProperties());
		properties.addAll(property.getEquivalentProperties());

		OWLDataPropertyAssertionAxiom propertyAssertion;

		for (COWLPropertyImpl prop : properties) {
			propertyAssertion = factory.getOWLDataPropertyAssertionAxiom(factory.getOWLDataProperty(prop.getIRI()), ind,
					literal);
			if (outputOntology.containsAxiom(propertyAssertion, Imports.INCLUDED,
					AxiomAnnotations.IGNORE_AXIOM_ANNOTATIONS)) {
				return true;
			}
		}

		return false;
	}
}
