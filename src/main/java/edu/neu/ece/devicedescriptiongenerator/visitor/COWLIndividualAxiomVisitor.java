package edu.neu.ece.devicedescriptiongenerator.visitor;

import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.devicedescriptiongenerator.entity.classes.COWLClassImpl;
import edu.neu.ece.devicedescriptiongenerator.extractor.OntologyExtractor;

/**
 * An instance of this class visits assertion axioms during ontology extraction
 * process, including class assertion axiom.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class COWLIndividualAxiomVisitor implements OWLAxiomVisitor {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Target OWL named individual.
	 */
	private final OWLNamedIndividual ind;

	/**
	 * Ontology extractor used for extracting OWL axioms from input ontology.
	 */
	private final OntologyExtractor oe;

	/**
	 * Constructor
	 * 
	 * @param oe
	 *            Ontology extractor used for extracting OWL axioms from input
	 *            ontology.
	 * @param ind
	 *            Target OWL named individual.
	 */
	public COWLIndividualAxiomVisitor(OntologyExtractor oe, OWLNamedIndividual ind) {
		this.ind = ind;
		this.oe = oe;
	}

	@Override
	public void doDefault(Object object) {
		// logger.warn("Unsupported assertion axiom: " + object);
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		OWLClassExpression exp = axiom.getClassExpression();
		if (exp.isAnonymous())
			logger.warn("Class assertion axiom for " + ind.getIRI().getShortForm()
					+ " will be ignored since it contains anonymous class");
		COWLClassImpl cowlClassImpl = oe.getClassMap().get(exp.asOWLClass());
		cowlClassImpl.getNamedIndividuals().add(ind);
	}
}
