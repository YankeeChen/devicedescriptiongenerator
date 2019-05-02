package edu.neu.ece.devicedescriptiongenerator.entity.properties;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

/**
 * This class defines customization of the OWL API class OWLObjectPropertImpl.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-29
 */
public class COWLObjectPropertyImpl extends COWLPropertyImpl {

	/**
	 * Inverse properties of this object.
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
	 * Get inverse properties of this object.
	 * 
	 * @return Inverse properties of this object.
	 */
	public Set<COWLObjectPropertyImpl> getInverseProperties() {
		return inverseProperties;
	}

}
