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
 * This class defines conceptual model of OWL named class.
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
	 * Direct super named classes of this object
	 */
	private Set<COWLClassImpl> directSuperClasses = new HashSet<>();

	/**
	 * Super named classes (direct and inferred) of this object.
	 */
	private Set<COWLClassImpl> superClasses = new HashSet<>();

	/**
	 * Direct super anonymous classes of this object.
	 */
	private Set<OWLAnonymousClassExpression> directAnonymousSuperClasses = new HashSet<>();

	/**
	 * Anonymous classes of the class inherited from its superclasses.
	 * 
	 * @deprecated
	 */
	private Set<OWLAnonymousClassExpression> superAnonymousSuperClasses = new HashSet<>();

	/**
	 * Anonymous super classes of this object, including direct anonymous super
	 * classes and super anonymous super classes.
	 */
	private Set<OWLAnonymousClassExpression> anonymousSuperClasses = new HashSet<>();

	/**
	 * Special class constraints from its anonymous class expressions that need to
	 * be considered for dataset generation.
	 */
	private Set<OWLAnonymousClassExpression> specialClassRestrictions = new HashSet<>();

	/**
	 * Direct named subclasses of this object.
	 */
	private Set<COWLClassImpl> directSubClasses = new HashSet<>();

	/**
	 * Named subclasses (direct and inferred) of this object.
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
	 * Object property and ranges pairs bounded to this object.
	 */
	private Map<OWLObjectProperty, OWLClassExpression> objectPropertyRangesPairs = new HashMap<>();

	/**
	 * Data property and ranges pairs bounded to this object.
	 */
	private Map<OWLDataProperty, OWLDataRange> dataPropertyRangesPairs = new HashMap<>();

	/**
	 * Named individuals asserted to be type of this OWL named class.
	 */
	private LinkedList<OWLNamedIndividual> individuals = new LinkedList<>();

	/**
	 * Used to detect whether this object is visited when generating RDF instance
	 * data.
	 */
	private boolean isVisited = false;

	/**
	 * A counter that traces the index of next OWL named individual that is of type
	 * of this OWL named class.
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
	 * Get super named classes (direct and indirect) of this object.
	 * 
	 * @return Super named classes (direct and inferred).
	 */
	public Set<COWLClassImpl> getSuperClasses() {
		return superClasses;
	}

	/**
	 * Get direct anonymous super classes of this object.
	 * 
	 * @return Direct anonymous super classes.
	 */
	public Set<OWLAnonymousClassExpression> getDirectAnonymousSuperClasses() {
		return directAnonymousSuperClasses;
	}

	/**
	 * Get anonymous classes of the OWL named class inherited from its superclasses.
	 * 
	 * @return Anonymous classes the OWL named class inherited from its
	 *         superclasses.
	 * @deprecated
	 */
	public Set<OWLAnonymousClassExpression> getSuperAnonymousSuperClasses() {
		return superAnonymousSuperClasses;
	}

	/**
	 * Get direct named subclasses of this object.
	 * 
	 * @return Direct named subclasses.
	 */
	public Set<COWLClassImpl> getDirectSubClasses() {
		return directSubClasses;
	}

	/**
	 * Get named subclasses of this object.
	 * 
	 * @return Named subclasses (direct and inferred).
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
	 * Get object property and ranges pairs bounded to this object.
	 * 
	 * @return Object property and ranges pairs.
	 */
	public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyRangesPairs() {
		return objectPropertyRangesPairs;
	}

	/**
	 * Get data property and ranges pairs bounded to this object.
	 * 
	 * @return Data property and ranges pairs.
	 */
	public Map<OWLDataProperty, OWLDataRange> getDataPropertyRangesPairs() {
		return dataPropertyRangesPairs;
	}

	/**
	 * Get named individuals asserted to be type of this OWL named class.
	 * 
	 * @return Named individuals asserted to be type of this OWL named class.
	 */
	public LinkedList<OWLNamedIndividual> getNamedIndividuals() {
		return individuals;
	}

	/*
	 * public List<Set<OWLAnonymousClassExpression>>
	 * getListOfAnonymousClassExpressionsSet() { return
	 * listOfAnonymousClassExpressionsSet; }
	 * 
	 * public void setListOfAnonymousClassExpressionsSet(ArrayList<Set<
	 * OWLAnonymousClassExpression>> listOfSet) { listOfAnonymousClassExpressionsSet
	 * = listOfSet; }
	 */
	/**
	 * Get anonymous super classes of this object, including direct anonymous super
	 * classes and super anonymous super classes.
	 * 
	 * @return Anonymous super classes.
	 */
	public Set<OWLAnonymousClassExpression> getAnonymousSuperClasses() {
		return anonymousSuperClasses;
	}

	/**
	 * Set anonymous super classes of this object, including direct anonymous super
	 * classes and super anonymous super classes.
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
	 * 
	 */
	public Set<OWLAnonymousClassExpression> getSpecialClassRestrictions() {
		return specialClassRestrictions;
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

	/*
	 * public ArrayList<Set<Entry<COWLDataPropertyImpl, OWLDataRange>>>
	 * getListOfDataPropertyAndRangePairsSet() { return
	 * listOfDataPropertyAndRangePairsSet; }
	 * 
	 * public void setListOfDataPropertyAndRangePairsSet(ArrayList<Set<Entry<
	 * COWLDataPropertyImpl, OWLDataRange>>> listOfSet) {
	 * listOfDataPropertyAndRangePairsSet = listOfSet; }
	 * 
	 * public ArrayList<Set<Entry<COWLObjectPropertyImpl, OWLClassExpression>>>
	 * getListOfObjectPropertyAndRangePairsSet() { return
	 * listOfObjectPropertyAndRangePairsSet; }
	 * 
	 * public void setListOfObjectPropertyAndRangePairsSet(ArrayList<Set<Entry<
	 * COWLObjectPropertyImpl, OWLClassExpression>>> listOfSet) {
	 * listOfObjectPropertyAndRangePairsSet = listOfSet; }
	 */

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
	 * Function used to coherently increase the counter of instances.
	 *
	 * @return The index of next OWL named individual that is of type of this OWL
	 *         named class.
	 */
	public long getNextInstanceNumber() {
		return nextInstanceIndex++;
	}

	/**
	 * Bind an object property and range pair to this object.
	 * 
	 * @param oop
	 *            Object property.
	 * @param classExp
	 *            Object property value (class expression).
	 */
	public void addAnObjectPropertyRangesPair(OWLObjectProperty oop, OWLClassExpression classExp) {
		objectPropertyRangesPairs.put(oop, classExp);
	}

	/**
	 * Bind a data property and range pair to this object.
	 * 
	 * @param odp
	 *            Data property.
	 * @param ran
	 *            Data property value (data range).
	 */
	public void addADataPropertyRangesPair(OWLDataProperty odp, OWLDataRange ran) {
		dataPropertyRangesPairs.put(odp, ran);
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
}
