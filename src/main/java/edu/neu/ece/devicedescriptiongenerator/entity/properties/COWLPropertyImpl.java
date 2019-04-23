package edu.neu.ece.devicedescriptiongenerator.entity.properties;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * This class defines conceptual model of OWL property.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-29
 *
 */
public class COWLPropertyImpl implements HasIRI {

	/**
	 * Property IRI.
	 */
	protected IRI iri;

	/**
	 * Detect whether the property is visited.
	 */
	protected boolean isVisited = false;

	/**
	 * Direct super properties of the property
	 */
	protected Set<COWLPropertyImpl> directSuperOWLProperties = new HashSet<>();

	/**
	 * Super properties (direct and inferred) of the property.
	 */
	protected Set<COWLPropertyImpl> superOWLProperties = new HashSet<>();

	/**
	 * Direct subproperties of the property.
	 */
	protected Set<COWLPropertyImpl> directSubOWLProperties = new HashSet<>();

	/**
	 * Subproperties (direct and inferred) of the property.
	 */
	protected Set<COWLPropertyImpl> subOWLProperties = new HashSet<>();

	/**
	 * Equivalent properties (direct and inferred) of the property.
	 */
	protected Set<COWLPropertyImpl> equivalentProperties = new HashSet<>();

	/**
	 * Direct disjoint properties of the property.
	 */
	protected Set<COWLPropertyImpl> directDisjointProperties = new HashSet<>();

	/**
	 * Disjoint properties (direct and inferred) of the property.
	 */
	protected Set<COWLPropertyImpl> disjointProperties = new HashSet<>();

	/**
	 * Characteristics of the property. For object properties, supported
	 * characteristics include Functional and Symmetric property axioms, whereas
	 * Function property axioms for data properties.
	 */
	protected Set<AxiomType<? extends OWLAxiom>> propertyAttributes = new HashSet<>();

	/**
	 * Constructor.
	 * 
	 * @param iri
	 *            Property IRI.
	 */
	public COWLPropertyImpl(IRI iri) {
		this.iri = iri;
	}

	@Override
	public IRI getIRI() {
		return iri;
	}

	/**
	 * Get visit status of the property
	 * 
	 * @return Visit status of the property
	 */
	public boolean isVisited() {
		return isVisited;
	}

	/**
	 * Set visit status of the property.
	 * 
	 * @param visited
	 *            Visit status of the property.
	 */
	public void setVisited(boolean visited) {
		isVisited = visited;
	}

	/**
	 * Get direct super properties of the property.
	 * 
	 * @return Direct super properties of the property.
	 */
	public Set<COWLPropertyImpl> getDirectSuperOWLProperties() {
		return directSuperOWLProperties;
	}

	/**
	 * Get super properties of the property.
	 * 
	 * @return Super properties (direct and inferred) of the property.
	 */
	public Set<COWLPropertyImpl> getSuperOWLProperties() {
		return superOWLProperties;
	}

	/**
	 * Get direct subproperties of the property.
	 * 
	 * @return Direct subproperties of the property.
	 */
	public Set<COWLPropertyImpl> getDirectSubOWLProperties() {
		return directSubOWLProperties;
	}

	/**
	 * Get subproperties (direct and inferred) of the property.
	 * 
	 * @return Subproperties of the property.
	 */
	public Set<COWLPropertyImpl> getSubOWLProperties() {
		return subOWLProperties;
	}

	/**
	 * Get equivalent properties (direct and inferred) of the property.
	 * 
	 * @return Equivalent properties (direct and inferred) of the property.
	 */
	public Set<COWLPropertyImpl> getEquivalentProperties() {
		return equivalentProperties;
	}

	/**
	 * Get direct disjoint properties of the property.
	 *
	 * @return Direct disjoint properties of the property.
	 */
	public Set<COWLPropertyImpl> getDirectDisjointProperties() {
		return directDisjointProperties;
	}

	/**
	 * Get disjoint properties (direct and inferred) of the property.
	 * 
	 * @return Disjoint properties (direct and inferred) of the property.
	 */
	public Set<COWLPropertyImpl> getDisjointProperties() {
		return disjointProperties;
	}

	public void setDisjointProperties(Set<COWLPropertyImpl> disjointProperties) {
		this.disjointProperties = disjointProperties;
	}

	/**
	 * Get characteristics of the property. For object properties, supported
	 * characteristics include functional, symmetric property, asymmetric property,
	 * reflexive and irreflexive property axioms, whereas Function property axioms
	 * for data properties.
	 * 
	 * @return Characteristics of the property as property axioms.
	 */
	public Set<AxiomType<? extends OWLAxiom>> getPropertyAttributes() {
		return propertyAttributes;
	}

	/**
	 * Add a characteristic into the property.
	 * 
	 * @param type
	 *            Target charactertic intended to be added into the property.
	 */
	public void addAPropertyAttribute(AxiomType<? extends OWLAxiom> type) {
		propertyAttributes.add(type);
	}
}
