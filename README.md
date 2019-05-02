# RDDG - Random Device Description Generator
RDDG is a generic RDF instance generator. It is implemented in Java with the use of the OWL API, a high level Application Programming Interface for working with OWL  ontologies. It is able to generate large numbers of synthetic random RF device descriptions in RDF/XML from an RF ontology schema.

## RDFUnit Command Line Interface

#### Download RDDG
You can get a copy of it stored in Github repository with the following command:
```console
$ git clone https://github.com/YankeeChen/devicedescriptiongenerator.git
```

#### Usage
```console
# argument help to see all available options
$ java -jar devicedescriptiongenerator-1.0-SNAPSHOT.jar -h

# Simple call (Load input ontology from ontology IRI or a local file)
$ java -jar devicedescriptiongenerator-1.0-SNAPSHOT.jar -rootIRI <IRI> {-inputFilePath <PATH> | -inputIRI <IRI>} [-devNumber <NUMBER>] [-outputFilePath <PATH>] [-ramSeed <SEED>] [-newIndividualProbability <PROBABILITY>] [-classConstraintSelectionProbability <PROBABILITY>] [-dataPropertyAssertionProbability <PROBABILITY>] [-objectPropertyAssertionProbability <PROBABILITY>] [-classAssertionProbability <PROBABILITY>] [-superClassSelectionProbability <PROBABILITY>] [-superDataPropertySelectionProbability <PROBABILITY>] [-superObjectPropertySelectionProbability <PROBABILITY>] [-disjointDataPropertySelectionProbability <PROBABILITY>] [-disjointObjectPropertySelectionProbability <PROBABILITY>] [-equivalentDataPropertySelectionProbability <PROBABILITY>] [-equivalentObjectPropertySelectionProbability <PROBABILITY>] [-inverseObjectPropertySelectionProbability <PROBABILITY>] [-asymmetricObjectPropertySelectionProbability <PROBABILITY>] [-symmetricObjectPropertySelectionProbability <PROBABILITY>] [-irreflexiveObjectPropertySelectionProbability <PROBABILITY>] 

```
`-rootIRI <IRI>` e.g. http://www.loa-cnr.it/ontologies/DUL.owl#PhysicalObject 
is required in all cases and states a IRI that relates to the root class of the input ontology. 

`-inputFilePath <PATH> | -inputIRI <IRI>` e.g. ontologies/IoTOntology/IoT.owl, http://ontology.tno.nl/saref.ttl
is required in all cases and states an ontology IRI **OR** a local path that relates to the input ontology.

`-devNumber <NUMBER>` 
is optional and states the number of device descriptions; 1 by default.

`-outputFilePath <PATH>` 
is optional and states a local path that relates to the output ontology that describes device descriptons; instancedata/DeviceDescription<NUMBER>.rdf by default.

`-ramSeed <SEED>` 
is optional and states random seed used for random device description generation; 0 by default.

`-newIndividualProbability <PROBABILITY>`
is optional and states the probability of creating an OWL named individual; 0.5 by default.

`-classConstraintSelectionProbability <PROBABILITY>`
is optional and states the probability of selecting an OWL class constraint (anonymous super class expression) of an OWL class; 0.9 by default.

`-dataPropertyAssertionProbability <PROBABILITY>`
is optional and states the probability of creating a data property assertion axiom; 0.5 by default.

`-objectPropertyAssertionProbability <PROBABILITY>`
is optional and states the probability of creating an object property assertion axiom; 0.5 by default.

`-classAssertionProbability <PROBABILITY>`
is optional and states the probablity of generating class assertion axioms for each named individual; 1.0 by default.

`-superClassSelectionProbability <PROBABILITY>`
is optional and states the probability of selecting a super class (direct or inferred) of a class for generating class assertion axioms for each named individual; 0.5 by default.

`-superDataPropertySelectionProbability <PROBABILITY>`
is optional and states the probability of selecting a super data property (direct or inferred) of a data property for data property assertion generation; 0.5 by default.

`-superObjectPropertySelectionProbability <PROBABILITY>`
is optional and states the probability of selecting a super object property (direct or inferred) of an object property for object property assertion generation; 0.5 by default.

`-disjointDataPropertySelectionProbability <PROBABILITY>`
is optional and states the probability of selecting a disjoint data property (direct or inferred) of a data property for negative data property assertion generation; 0.8 by default. 

`-disjointObjectPropertySelectionProbability <PROBABILITY>`
is optional and states the probability of selecting a disjoint object property (direct or inferred) of an object property for negative object property assertion generation; 0.8 by default. 

`-equivalentDataPropertySelectionProbability <PROBABILITY>`
is optional and states the probability of selecting an equivalent data property (direct or inferred) of a data property for data property assertion generation; 0.5 by default.

`-equivalentObjectPropertySelectionProbability <PROBABILITY>`
is optional and states the probability of selecting an equivalent object property (direct or inferred) of an object property for object property assertion generation; 0.5 by default.

`-inverseObjectPropertySelectionProbability <PROBABILITY>`
is optional and states the probability of selecting an inverse property (direct or inferred) of an object property for object property assertion generation; 0.8 by default.

`-asymmetricObjectPropertySelectionProbability <PROBABILITY>`
is optional and states the probabilty of selecting asymmetric characteristic of an object property for negative object property assertion generation; 0.8 by default.

`-symmetricObjectPropertySelectionProbability <PROBABILITY>`
is optional and states the probabilty of selecting symmetric characteristic of an object property for object property assertion generation; 0.8 by default.

`-irreflexiveObjectPropertySelectionProbability <PROBABILITY>`
is optional and states the probabilty of selecting irreflexive characteristic of an object property for negative object property assertion generation; 0.8 by default.

What RDDG will do is:
1. Load ontology.
2. Process ontology (that includes Process Entities, Process Axioms and Infer New Knowledge). 
3. Generate device descriptions.
4. Collect space coverage evaluation metrics and dump them into file with local path evaluationresults/SpaceCoverageEvaluationResults_DeviceDescription<DEVICE-NUMBER>.txt

## Contact
Yanji Chen

Lab of Info and Software Fusion

ECE Department, Northeastern University, MA, USA

Email:chen.yanj@husky.neu.edu

Personal website: https://sites.google.com/view/yanjichen0101/home?authuser=0

