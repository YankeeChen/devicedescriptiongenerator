package edu.neu.ece.devicedescriptiongenerator.generator;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import edu.neu.ece.devicedescriptiongenerator.entity.classes.COWLClassImpl;

/**
 * A generator for generating OWL individuals.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class IRIGenerator {
	/**
	 * The last index of the created OWL named individual of anonymous super classes
	 * type.
	 */
	private static long individualIndex = 0;

	/**
	 * Generate an OWL named individual of type of an OWL anonymous class.
	 * 
	 * @param OUTPUT_ONTOLOGY_IRI_IN_STRING
	 *            Output ontology IRI in string.
	 * @param factory
	 *            OWL data factory used to create entities, class expressions and
	 *            axioms.
	 * @return OWL named individual.
	 */
	public static OWLNamedIndividual generateOWLIndividual(final String OUTPUT_ONTOLOGY_IRI_IN_STRING,
			OWLDataFactory factory) {
		return factory.getOWLNamedIndividual(OUTPUT_ONTOLOGY_IRI_IN_STRING + "#Instance" + individualIndex++);
	}

	/**
	 * Generate an OWL named individual of type of an OWL class.
	 * 
	 * @param OUTPUT_ONTOLOGY_IRI_IN_STRING
	 *            Output ontology IRI in string.
	 * @param factory
	 *            OWL data factory used to create entities, class expressions and
	 *            axioms.
	 * @param cls
	 *            OWL class.
	 * @return OWL named individual.
	 */
	public static OWLNamedIndividual generateOWLIndividual(final String OUTPUT_ONTOLOGY_IRI_IN_STRING,
			OWLDataFactory factory, COWLClassImpl cls) {
		return factory.getOWLNamedIndividual(OUTPUT_ONTOLOGY_IRI_IN_STRING + "#" + cls.getIRI().getShortForm()
				+ "_instance" + cls.getNextInstanceNumber());
	}
}
