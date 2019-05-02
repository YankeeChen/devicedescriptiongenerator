package edu.neu.ece.devicedescriptiongenerator.entity.properties;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * This class defines customization of the OWL API interface OWLProperty.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-29
 */
public class COWLPropertyImpl implements HasIRI {

	/**
	 * Property IRI.
	 */
	protected IRI iri;

	/**
	 * Detect whether this object is visited.
	 */
	protected boolean isVisited = false;

	/**
	 * Direct super properties of this object.
	 */
	protected Set<COWLPropertyImpl> directSuperOWLProperties = new HashSet<>();

	/**
	 * Super properties (direct and inferred) of this object.
	 */
	protected Set<COWLPropertyImpl> superOWLProperties = new HashSet<>();

	/**
	 * Direct subproperties of this object.
	 */
	protected Set<COWLPropertyImpl> directSubOWLProperties = new HashSet<>();

	/**
	 * Subproperties (direct and inferred) of this object.
	 */
	protected Set<COWLPropertyImpl> subOWLProperties = new HashSet<>();

	/**
	 * Equivalent properties (direct and inferred) of this object.
	 */
	protected Set<COWLPropertyImpl> equivalentProperties = new HashSet<>();

	/**
	 * Direct disjoint properties of this object.
	 */
	protected Set<COWLPropertyImpl> directDisjointProperties = new HashSet<>();

	/**
	 * Disjoint properties (direct and inferred) of this object.
	 */
	protected Set<COWLPropertyImpl> disjointProperties = new HashSet<>();

	/**
	 * Characteristics of the property. For object properties, supported
	 * characteristics include functional, symmetric property, asymmetric property,
	 * reflexive and irreflexive property axioms, whereas Function property axioms
	 * for data properties.
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
	 * Get visit status of this object.
	 * 
	 * @return Visit status of this object.
	 */
	public boolean isVisited() {
		return isVisited;
	}

	/**
	 * Set visit status of this object.
	 * 
	 * @param visited
	 *            Visit status of this object.
	 */
	public void setVisited(boolean visited) {
		isVisited = visited;
	}

	/**
	 * Get direct super properties of this object.
	 * 
	 * @return Direct super properties of this object.
	 */
	public Set<COWLPropertyImpl> getDirectSuperOWLProperties() {
		return directSuperOWLProperties;
	}

	/**
	 * Get super properties of this object.
	 * 
	 * @return Super properties (direct and inferred) of this object.
	 */
	public Set<COWLPropertyImpl> getSuperOWLProperties() {
		return superOWLProperties;
	}

	/**
	 * Get direct subproperties of this object.
	 * 
	 * @return Direct subproperties of this object.
	 */
	public Set<COWLPropertyImpl> getDirectSubOWLProperties() {
		return directSubOWLProperties;
	}

	/**
	 * Get subproperties (direct and inferred) of this object.
	 * 
	 * @return Subproperties of this object.
	 */
	public Set<COWLPropertyImpl> getSubOWLProperties() {
		return subOWLProperties;
	}

	/**
	 * Get equivalent properties (direct and inferred) of this object.
	 * 
	 * @return Equivalent properties (direct and inferred) of this object.
	 */
	public Set<COWLPropertyImpl> getEquivalentProperties() {
		return equivalentProperties;
	}

	/**
	 * Get direct disjoint properties of this object.
	 *
	 * @return Direct disjoint properties of this object.
	 */
	public Set<COWLPropertyImpl> getDirectDisjointProperties() {
		return directDisjointProperties;
	}

	/**
	 * Get disjoint properties (direct and inferred) of this object.
	 * 
	 * @return Disjoint properties (direct and inferred) of this object.
	 */
	public Set<COWLPropertyImpl> getDisjointProperties() {
		return disjointProperties;
	}

	/**
	 * Set disjoint properties (directed and inferred) of this object.
	 * 
	 * @param disjointProperties
	 *            Disjoint properties.
	 */
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
	 *            Target characteristic intended to be added into the property.
	 */
	public void addAPropertyAttribute(AxiomType<? extends OWLAxiom> type) {
		propertyAttributes.add(type);
	}
}
