package edu.neu.ece.devicedescriptiongenerator.entity.properties;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataRange;

/**
 * This class defines conceptual model of OWL data property.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-29
 */
public class COWLDataPropertyImpl extends COWLPropertyImpl {

	/**
	 * OWL data range.
	 */
	private OWLDataRange range;

	/**
	 * Constructor
	 * 
	 * @param iri
	 *            Data property IRI.
	 */
	public COWLDataPropertyImpl(IRI iri) {
		super(iri);
	}

	/**
	 * Get OWL data range.
	 * 
	 * @return OWL data range of the property.
	 */
	public OWLDataRange getOWLDataRange() {
		return range;
	}

	/**
	 * Set OWL data range.
	 * 
	 * @param range
	 *            OWL data range of the property.
	 */
	public void setOWLDataRange(OWLDataRange range) {
		this.range = range;
	}

}
