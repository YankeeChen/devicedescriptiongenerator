package edu.neu.ece.devicedescriptiongenerator.entity.properties;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataRange;

/**
 * This class defines customization of the OWL API class OWLDataPropertImpl.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-29
 */
public class COWLDataPropertyImpl extends COWLPropertyImpl {

	/**
	 * Data property range.
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
	 * Get data property range.
	 * 
	 * @return Data property range.
	 */
	public OWLDataRange getOWLDataRange() {
		return range;
	}

	/**
	 * Set data property range.
	 * 
	 * @param range
	 *            Data property range.
	 */
	public void setOWLDataRange(OWLDataRange range) {
		this.range = range;
	}

}
