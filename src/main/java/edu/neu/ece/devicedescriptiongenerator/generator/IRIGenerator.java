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
	// private static final String OUTPUT_ONTOLOGY_IRI_IN_STRING =
	// "http://ece.neu.edu/ontologies/DeviceDescription";

	// The last index of created OWL named individual of anonymous super classes
	// type.
	private static long individualIndex = 0;

	/**
	 * Generate an OWL named individual of type of an OWL anonymous class.
	 * 
	 * @param OUTPUT_ONTOLOGY_IRI_IN_STRING
	 * @param factory
	 * @return An OWL named individual.
	 */
	public static OWLNamedIndividual generateOWLIndividual(final String OUTPUT_ONTOLOGY_IRI_IN_STRING,
			OWLDataFactory factory) {
		return factory.getOWLNamedIndividual(OUTPUT_ONTOLOGY_IRI_IN_STRING + "#Instance" + individualIndex++);
	}

	/**
	 * Generate an OWL named individual of type of an OWL named class.
	 * 
	 * @param OUTPUT_ONTOLOGY_IRI_IN_STRING
	 *            Output ontology IRI in string.
	 * @param factory
	 *            An OWL data factory object used for creating entities, class
	 *            expressions and axioms.
	 * @param cls
	 *            The OWL named class.
	 * @return An OWL named individual.
	 */
	public static OWLNamedIndividual generateOWLIndividual(final String OUTPUT_ONTOLOGY_IRI_IN_STRING,
			OWLDataFactory factory, COWLClassImpl cls) {
		return factory.getOWLNamedIndividual(OUTPUT_ONTOLOGY_IRI_IN_STRING + "#" + cls.getIRI().getShortForm()
				+ "_instance" + cls.getNextInstanceNumber());
	}
}
