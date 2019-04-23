package edu.neu.ece.devicedescriptiongenerator.entity.properties;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

/**
 * This class defines conceptual model of OWL object property.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-29
 *
 */
public class COWLObjectPropertyImpl extends COWLPropertyImpl {

	/**
	 * Inverse properties of the property.
	 */
	private Set<COWLObjectPropertyImpl> inverseProperties = new HashSet<>();

	/**
	 * Constructor.
	 * 
	 * @param iri
	 *            Object property IRI.
	 */
	public COWLObjectPropertyImpl(IRI iri) {
		super(iri);
	}

	/**
	 * Get inverse properties of the property.
	 * 
	 * @return Inverse properties of the property.
	 */
	public Set<COWLObjectPropertyImpl> getInverseProperties() {
		return inverseProperties;
	}

}
