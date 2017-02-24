package engine;

import java.util.ArrayList;
import java.util.HashMap;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.metaheuristics.paes.PAES;
import jmetal.metaheuristics.spea2.SPEA2;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;

public class OptimizationMain {

    private Problem problem;
    private Algorithm algorithm;
    private Operator crossover;
    private Operator mutation;
    private Operator selection;

    private String problem_name;
    private String algorithm_name;
    HashMap parameters; // Operator parameters;
    
    private int populationSize=100;

    HashMap<String, ArrayList<Double>> results;

    public OptimizationMain(String pn, String an)
	    throws JMException, ClassNotFoundException {
	problem_name = pn;
	algorithm_name = an;
	results = new HashMap<String, ArrayList<Double>>();

	Object[] params = { "Real" };
	problem = (new ProblemFactory()).getProblem(problem_name, params);

	generateAlgorithm();

	// Execute the Algorithm
	long initTime = System.currentTimeMillis();
	SolutionSet population = algorithm.execute();
	long estimatedTime = System.currentTimeMillis() - initTime;
	Double time = (double) estimatedTime;

	ArrayList<Double> obj1 = new ArrayList<Double>();
	ArrayList<Double> obj2 = new ArrayList<Double>();
	ArrayList<Double> timelist = new ArrayList<Double>();

	for (int i = 0; i < population.size(); i++) {
	    obj1.add(population.get(i).getObjective(0));
	    obj2.add(population.get(i).getObjective(1));
	}
	
	timelist.add(time);
	results.put("Obj1", obj1);
	results.put("Obj2", obj2);
	results.put("Time", timelist);
    }

    public HashMap<String, ArrayList<Double>> getResults() {
	return results;
    }

    private void generateAlgorithm() throws JMException {
	if (algorithm_name.equals("PAES")) {
	    producePAES();
	} else if (algorithm_name.equals("SPEA2")) {
	    produceSPEA2();
	} else {
	    produceNSGAII();
	}
    }

    private void producePAES() throws JMException {

	algorithm = new PAES(problem);

	// Algorithm parameters
	algorithm.setInputParameter("archiveSize", populationSize);
	algorithm.setInputParameter("biSections", 5);
	algorithm.setInputParameter("maxEvaluations", 25000);

	// Mutation (Real variables)
	parameters = new HashMap();
	parameters.put("probability", 1.0 / problem.getNumberOfVariables());
	parameters.put("distributionIndex", 20.0);
	mutation = MutationFactory.getMutationOperator("PolynomialMutation",
		parameters);

	// Mutation (BinaryReal variables)
	// mutation = MutationFactory.getMutationOperator("BitFlipMutation");
	// mutation.setParameter("probability",0.1);

	// Add the operators to the algorithm
	algorithm.addOperator("mutation", mutation);

    }

    private void produceSPEA2() throws JMException {

	algorithm = new SPEA2(problem);

	// Algorithm parameters
	algorithm.setInputParameter("populationSize", populationSize);
	algorithm.setInputParameter("archiveSize", 100);
	algorithm.setInputParameter("maxEvaluations", 25000);

	// Mutation and Crossover for Real codification
	parameters = new HashMap();
	parameters.put("probability", 0.9);
	parameters.put("distributionIndex", 20.0);
	crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover",
		parameters);

	parameters = new HashMap();
	parameters.put("probability", 1.0 / problem.getNumberOfVariables());
	parameters.put("distributionIndex", 20.0);
	mutation = MutationFactory.getMutationOperator("PolynomialMutation",
		parameters);

	// Selection operator
	parameters = null;
	selection = SelectionFactory.getSelectionOperator("BinaryTournament",
		parameters);

	// Add the operators to the algorithm
	algorithm.addOperator("crossover", crossover);
	algorithm.addOperator("mutation", mutation);
	algorithm.addOperator("selection", selection);

    }

    private void produceNSGAII() throws JMException {
	algorithm = new NSGAII(problem);
	algorithm.setInputParameter("populationSize", populationSize);
	algorithm.setInputParameter("maxEvaluations", 25000);

	// Mutation and Crossover for Real codification
	parameters = new HashMap();
	parameters.put("probability", 0.9);
	parameters.put("distributionIndex", 20.0);
	crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover",
		parameters);

	parameters = new HashMap();
	parameters.put("probability", 1.0 / problem.getNumberOfVariables());
	parameters.put("distributionIndex", 20.0);
	mutation = MutationFactory.getMutationOperator("PolynomialMutation",
		parameters);

	// Selection Operator
	parameters = null;
	selection = SelectionFactory.getSelectionOperator("BinaryTournament2",
		parameters);

	// Add the operators to the algorithm
	algorithm.addOperator("crossover", crossover);
	algorithm.addOperator("mutation", mutation);
	algorithm.addOperator("selection", selection);

    }

}
