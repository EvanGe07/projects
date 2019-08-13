package bearmaps.utils.graph;

import bearmaps.utils.pq.DoubleMapPQ;
import bearmaps.utils.pq.PriorityQueue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {

    private AStarGraph<Vertex> myGraph;
    private Vertex start;
    private Vertex end;

    private PriorityQueue<Vertex> fringe = new DoubleMapPQ<>();
    private Map<Vertex, Double> distTo = new HashMap<>();
    private Map<Vertex, Vertex> lastNode = new HashMap<>();
    private Set<Vertex> finalized;

    private SolverOutcome result;
    private LinkedList<Vertex> solution = new LinkedList<>();
    private double totalWeight;
    private int statesExplored = 0;
    private long solverTime;
    private double endBefore;

    /**
     * Constructor which finds the solution,
     * Compute everything necessary for all other methods to return their results in constant time.
     * Note that timeout passed in is in seconds.
     *
     * @param input   The graph
     * @param start   Starting point
     * @param end     End point
     * @param timeout Time restriction for this activity.
     */
    public AStarSolver(AStarGraph<Vertex> input, Vertex start, Vertex end, double timeout) {
        this.myGraph = input;
        this.start = start;
        this.end = end;
        finalized = new HashSet<>();

        distTo.put(start, 0.0);
        finalized.add(start);
        fringe.insert(start, myGraph.estimatedDistanceToGoal(start, end));
        endBefore = System.currentTimeMillis() + timeout * 1000.0;
        solve();
    }

    /**
     * Solve the trace.
     */
    private void solve() {
        long startTime = System.currentTimeMillis();
        while (!finalized.contains(end)) {
            if (fringe.size() == 0) {
                result = SolverOutcome.UNSOLVABLE;
                return;
            }
            if (System.currentTimeMillis() > endBefore) {
                result = SolverOutcome.TIMEOUT;
                return;
            }
            Vertex curr = fringe.poll();
            statesExplored += 1;
            finalized.add(curr);
            if (curr.equals(end)) {
                gather();
                return;
            }
            List<WeightedEdge<Vertex>> currNeighbours = myGraph.neighbors(curr);
            for (WeightedEdge<Vertex> we : currNeighbours) {
                if (finalized.contains(we.to())) {
                    continue;
                }
                Vertex to = we.to();
                if (distTo.containsKey(to)) {
                    double prevDistance = distTo.get(to);
                    double myDistance = distTo.get(curr) + we.weight();
                    if (myDistance < prevDistance) {
                        distTo.put(to, myDistance);
                        lastNode.put(to, curr);
                        fringe.changePriority(to, getPriority(to, myDistance));
                    }
                } else {
                    distTo.put(to, distTo.get(curr) + we.weight());
                    fringe.insert(to, getPriority(to, we.weight() + distTo.get(curr)));
                    lastNode.put(to, curr);
                }
            }
        }
        solverTime = System.currentTimeMillis() - startTime;
    }

    private double getPriority(Vertex v, double dist) {
        return myGraph.estimatedDistanceToGoal(v, end) + dist;
    }

    private void gather() {
        solution = new LinkedList<>();
        Vertex curr = end;
        while (!curr.equals(start)) {
            solution.addFirst(curr);
            curr = lastNode.get(curr);
        }
        solution.addFirst(start);
        result = SolverOutcome.SOLVED;
    }

    /**
     * Returns one of SolverOutcome.SOLVED, SolverOutcome.TIMEOUT, or SolverOutcome.UNSOLVABLE.
     * Should be SOLVED if the AStarSolver was able to complete all work in the time given.
     * UNSOLVABLE if the priority queue became empty before finding the solution.
     * TIMEOUT if the solver ran out of time.
     * You should check to see if you have run out of time every time you dequeue.
     *
     * @return SolverOutcome.SOLVED, SolverOutcome.TIMEOUT, or SolverOutcome.UNSOLVABLE
     */
    public SolverOutcome outcome() {
        return result;
    }

    /**
     * A list of vertices corresponding to a solution.
     * Should be empty if result was TIMEOUT or UNSOLVABLE.
     *
     * @return A list of vertices corresponding to a solution.
     */
    public List<Vertex> solution() {
        return solution;
    }

    /**
     * The total weight of the given solution, taking into account edge weights.
     * Should be 0 if result was TIMEOUT or UNSOLVABLE.
     *
     * @return The total weight of the given solution.
     */
    public double solutionWeight() {
        LinkedList<Vertex> solCopy = new LinkedList<>(this.solution);
        totalWeight = 0;
        for (int i = 0; i < solCopy.size(); i++) {
            Vertex curnode = solCopy.poll();
            Vertex nextnode = solCopy.peek();
            totalWeight += myGraph.neighbors(curnode).parallelStream()
                .filter((we) -> we.to().equals(nextnode))
                .collect(Collectors.toList()).get(0).weight();
        }
        return totalWeight;
    }

    /**
     * The total number of priority queue poll() operations.
     * Should be the number of states explored so far if result was TIMEOUT or UNSOLVABLE.
     *
     * @return The total number of priority queue poll() operations.
     */
    public int numStatesExplored() {
        return statesExplored;
    }

    /**
     * The total time spent in seconds by the constructor.
     *
     * @return Time spent on solving the path.
     */
    public double explorationTime() {
        return (double) solverTime / 1000;
    }
}
