# Benchmark on Metrics-based Comparison of XML and OWL based Approaches to Representing and Querying Cognitive Radio Capabilities 

Version 1.0


Requirements
---
*   Java 8 (or higher)
*   Maven

Instructions for using evaluationframework JAR
---

## Step 1: Initialize repositories

### 1.1 Install, setup and run Fuseki2

* Download a binary distribution of [Fuseki2](https://jena.apache.org/download/)

* Unpack the archive

* Navigate to the unpacked directory

* Create a directory, e.g. tmp

* Run the Fuseki2 server using the following command (replace `<dir>` with the name of the directory created above):

  In UNIX/Linux:
`java -jar fuseki-server.jar -update -loc ./<dir> /ds`

  In Windows:
`java -jar fuseki-server.jar -update -loc .\<dir> /ds`

### 1.2 Run DeVISor

* Run DeVISor-1.1.2.jar which is located in ./divisor by default

  **Example**: `java -jar deVISor-1.1.2.jar`

### 1.3 Install, setup and run BaseX

* Download the latest version of [BaseX](http://basex.org/products/download/all-downloads/) jar file

* Run BaseX jar file

  **Example**: `java -cp BaseX841.jar org.basex.BaseXServer`

## Step 2: Run benchmark

* Run evaluationframework-1.0.jar with the following parameters:

  `java -jar evaluationframework-1.0.jar -file <path> -devNumber <devNum> -queryNumber <queryNum> -seed <seed>`

* Options:

  -file: The path to the loaded ontology, which must be .\devisor\onts\<file name> format
   
  -devNumber: Number of device descriptions needed to generate; 1 by default
   
  -queryNumber: Number of queries needed to generate; 10 by default
   
  -seed: Random seed used for random device description generation; 0 by default
   
  **Example**: `java -jar evaluationframework-1.0.jar -file ontologies/Groundtruth_Ontology/USRPTest.owl -devNumber 50 -queryNumber 50 -seed 0`


Contact
---
Yanji Chen 

Lab of Info and Software Fusion 

ECE Department, Northeastern University, MA, USA

Email:chen.yanj@husky.neu.edu

Personal website: https://sites.google.com/view/yanjichen0101/home?authuser=0
