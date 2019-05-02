package edu.neu.ece.devicedescriptiongenerator.entity.classes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * This class defines customization of the OWL API class OWLClassImpl.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-28
 */
public class COWLClassImpl implements HasIRI {

	/**
	 * Class IRI.
	 */
	private IRI iri;

	/**
	 * Direct super classes of this object
	 */
	private Set<COWLClassImpl> directSuperClasses = new HashSet<>();

	/**
	 * Super classes (direct and inferred) of this object.
	 */
	private Set<COWLClassImpl> superClasses = new HashSet<>();

	/**
	 * Direct anonymous super class expressions of this object.
	 */
	private Set<OWLAnonymousClassExpression> directAnonymousSuperClasses = new HashSet<>();

	/**
	 * Anonymous super classes of this object, including direct anonymous super
	 * classes and anonymous super classes inherited from its super classes.
	 */
	private Set<OWLAnonymousClassExpression> anonymousSuperClasses = new HashSet<>();

	/**
	 * Anonymous super class expressions that are specially considered to avoid
	 * generating inconsistent datasets.
	 */
	private Set<OWLAnonymousClassExpression> specialClassRestrictions = new HashSet<>();

	/**
	 * Direct subclasses of this object.
	 */
	private Set<COWLClassImpl> directSubClasses = new HashSet<>();

	/**
	 * Subclasses (direct and inferred) of this object.
	 */
	private Set<COWLClassImpl> subClasses = new HashSet<>();

	/**
	 * Equivalent class expressions (direct and inferred) of this object.
	 */
	private Set<OWLClassExpression> equivalentClasses = new HashSet<>();

	/**
	 * Disjoint class expressions (direct and inferred) of this object.
	 */
	private Set<OWLClassExpression> disjointClasses = new HashSet<>();

	/**
	 * Container that stores key-value pairs, where object property is the key and
	 * the property range (class expression) is the value.
	 */
	private Map<OWLObjectProperty, OWLClassExpression> objectPropertyRangesPairs = new HashMap<>();

	/**
	 * Container that stores key-value pairs, where data property is the key and the
	 * property range (data range) is the value.
	 */
	private Map<OWLDataProperty, OWLDataRange> dataPropertyRangesPairs = new HashMap<>();

	/**
	 * Named individuals of type of this object.
	 */
	private LinkedList<OWLNamedIndividual> individuals = new LinkedList<>();

	/**
	 * Detect whether this object is visited when generating RDF instance data.
	 */
	private boolean isVisited = false;

	/**
	 * A counter that traces the index of next OWL named individual of type of this
	 * object.
	 */
	private long nextInstanceIndex = 0;

	/**
	 * Constructor.
	 * 
	 * @param iri
	 *            class IRI.
	 */
	public COWLClassImpl(IRI iri) {
		this.iri = iri;
	}

	/**
	 * Get class IRI.
	 */
	@Override
	public IRI getIRI() {
		return iri;
	}

	/**
	 * Get direct super classes of this object.
	 * 
	 * @return Direct super classes.
	 */
	public Set<COWLClassImpl> getDirectSuperClasses() {
		return directSuperClasses;
	}

	/**
	 * Get super classes (direct and indirect) of this object.
	 * 
	 * @return Super classes (direct and inferred).
	 */
	public Set<COWLClassImpl> getSuperClasses() {
		return superClasses;
	}

	/**
	 * Get direct anonymous super class expressions of this object.
	 * 
	 * @return Direct anonymous super class expressions.
	 */
	public Set<OWLAnonymousClassExpression> getDirectAnonymousSuperClasses() {
		return directAnonymousSuperClasses;
	}

	/**
	 * Get anonymous super classes of this object, including direct anonymous super
	 * classes and anonymous super classes inherited from super classes.
	 * 
	 * @return Anonymous super classes.
	 */
	public Set<OWLAnonymousClassExpression> getAnonymousSuperClasses() {
		return anonymousSuperClasses;
	}

	/**
	 * Set anonymous super classes of this object.
	 * 
	 * @param set
	 *            Anonymous super classes.
	 */
	public void setAnonymousSuperClasses(Set<OWLAnonymousClassExpression> set) {
		anonymousSuperClasses = set;
	}

	/**
	 * Get special class restrictions.
	 * 
	 * @return Special class restrictions.
	 */
	public Set<OWLAnonymousClassExpression> getSpecialClassRestrictions() {
		return specialClassRestrictions;
	}

	/**
	 * Get direct subclasses of this object.
	 * 
	 * @return Direct subclasses.
	 */
	public Set<COWLClassImpl> getDirectSubClasses() {
		return directSubClasses;
	}

	/**
	 * Get subclasses of this object.
	 * 
	 * @return Subclasses (direct and inferred).
	 */
	public Set<COWLClassImpl> getSubClasses() {
		return subClasses;
	}

	/**
	 * Get equivalent class expressions of this object.
	 * 
	 * @return Equivalent class expressions.
	 */
	public Set<OWLClassExpression> getEquivalentClasses() {
		return equivalentClasses;
	}

	/**
	 * Get disjoint class expressions (direct and inferred) of this object.
	 * 
	 * @return Disjoint class expressions.
	 */
	public Set<OWLClassExpression> getDisjointClasses() {
		return disjointClasses;
	}

	/**
	 * Get the container that stores key-value pairs, where object property is the
	 * key and the property range (class expression) is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyRangesPairs() {
		return objectPropertyRangesPairs;
	}

	/**
	 * Get the container that stores key-value pairs, where data property is the key
	 * and the property range (data range) is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLDataProperty, OWLDataRange> getDataPropertyRangesPairs() {
		return dataPropertyRangesPairs;
	}

	/**
	 * Get named individuals of type of this object.
	 * 
	 * @return Named individuals of type of this object.
	 */
	public LinkedList<OWLNamedIndividual> getNamedIndividuals() {
		return individuals;
	}

	/**
	 * Detect whether this object is visited.
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
	 * Function used to coherently increase the counter that traces the index of
	 * next OWL named individual of type of this object.
	 * 
	 * @return The index of next OWL named individual of type of this object.
	 */
	public long getNextInstanceNumber() {
		return nextInstanceIndex++;
	}

	/**
	 * Get subclasses of this object and itself.
	 * 
	 * @return Subclasses of this object and itself.
	 */
	public Set<COWLClassImpl> getSubClassesandItself() {
		Set<COWLClassImpl> subAndItSelf = new HashSet<>();
		subAndItSelf.addAll(subClasses);
		subAndItSelf.add(this);
		return subAndItSelf;
	}

	/**
	 * Add a special class restriction to this object.
	 * 
	 * @param exp
	 *            An anonymous class expression.
	 */
	public void addASpecialClassRestriction(OWLAnonymousClassExpression exp) {
		specialClassRestrictions.add(exp);
	}

	/**
	 * Add a key-value pair to the container, where object property is the key and
	 * the property range (class expression) is the value.
	 * 
	 * @param oop
	 *            The key (object property).
	 * @param classExp
	 *            The value (class expression).
	 */
	public void addAnObjectPropertyRangesPair(OWLObjectProperty oop, OWLClassExpression classExp) {
		objectPropertyRangesPairs.put(oop, classExp);
	}

	/**
	 * Add a key-value pair to the container, where data property is the key and the
	 * property range (data range) is the value.
	 * 
	 * @param odp
	 *            The key (data property).
	 * @param ran
	 *            The value (data range).
	 */
	public void addADataPropertyRangesPair(OWLDataProperty odp, OWLDataRange ran) {
		dataPropertyRangesPairs.put(odp, ran);
	}
}
