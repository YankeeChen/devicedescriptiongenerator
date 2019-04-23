package edu.neu.ece.devicedescriptiongenerator;

import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;


public class Test1 {
	private static final String SAREF_IRI = "http://ontology.tno.nl/saref.ttl";
	
	/** This example shows how to examine the restrictions on a class.
	 * 
	 * @throws OWLOntologyCreationException */
    public void processRestrictions() throws OWLOntologyCreationException {
        // Create our manager
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        // Load the SAREF ontology
        OWLOntology ont = man.loadOntologyFromOntologyDocument(IRI.create(SAREF_IRI));
        System.out.println("Loaded: " + ont.getOntologyID());
        // We want to examine the restrictions on Device class. To do this,
        // we need to obtain a reference to the Device class. In this
        // case, we know the URI for the class.
        IRI deviceIRI = IRI.create("https://w3id.org/saref#Device");
        OWLClass device = man.getOWLDataFactory()
                .getOWLClass(deviceIRI);
        // Now we want to collect the properties which are used in existential
        // restrictions on the class. To do this, we will create a utility class
        // - COWLClassExpressionVisitor, which acts as a filter for existential
        // restrictions. This uses the Visitor Pattern (google Visitor Design
        // Pattern for more information on this design pattern, or see
        // http://en.wikipedia.org/wiki/Visitor_pattern)
        COWLClassExpressionVisitor restrictionVisitor = new COWLClassExpressionVisitor();
        // In this case, restrictions are used as (anonymous) superclasses, so
        // to get the restrictions on the Device class we need to obtain the
        // subclass axioms for the Device class.
        for (OWLSubClassOfAxiom ax : ont.subClassAxiomsForSubClass(device).collect(Collectors.toSet())) {
            OWLClassExpression superCls = ax.getSuperClass();
            System.out.print(superCls.getClassExpressionType().getName() + "\t");
            // Ask our superclass to accept a visit from the COWLClassExpressionVisitor
            // - if it is an existential restriction then our visitor
            // will answer it - if not our visitor will ignore it
            superCls.accept(restrictionVisitor);
        }
    }
    
    /** Visits existential restrictions */
    public class COWLClassExpressionVisitor implements OWLClassExpressionVisitor {

        @Override
        public void visit(OWLClass ce) {
        	    print(ce);
        }
  
      	@Override
        public void visit(OWLObjectIntersectionOf ce) {
      		print(ce);
        }

    	    @Override
       	public void visit(OWLObjectUnionOf ce) {
    	    	    print(ce);
    	    }

    		@Override
    		public void visit(OWLObjectComplementOf ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLObjectOneOf ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLObjectSomeValuesFrom ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLObjectAllValuesFrom ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLObjectHasValue ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLObjectHasSelf ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLObjectMinCardinality ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLObjectMaxCardinality ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLObjectExactCardinality ce) {
    			print(ce);
    		}
    
    		@Override
    		public void visit(OWLDataSomeValuesFrom ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLDataAllValuesFrom ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLDataHasValue ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLDataMinCardinality ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLDataMaxCardinality ce) {
    			print(ce);
    		}
    		
    		@Override
    		public void visit(OWLDataExactCardinality ce) {
    			print(ce);
    		}
    		
    		private void print(OWLClassExpression ce) {
    			System.out.println(ce.getClass().getSimpleName());
    		}
    }
    
	/**
	 * Main function, entrance to the program.
	 * 
	 * @param args
	 *            Arguments from console.
	 * @throws OWLOntologyCreationException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException {
		Test1 t1 = new Test1();
		t1.processRestrictions();
	}
}
