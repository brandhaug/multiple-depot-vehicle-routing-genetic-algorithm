## MDVRP solved by GA
##### by Trygve Vang and Martin Brandhaug


### MDVRP
The Multi-Depot Vehicle Routing Problem (MDVRP), an extension of classical VRP, is a
NP-hard problem for simultaneously determining the routes for several vehicles from multiple depots to
a set of customers and then return to the same depot. 

The objective of the problem is to find routes for
vehicles to service all the customers at a minimal cost in terms of number of routes and total travel
distance, without violating the capacity and travel time constraints of the vehicles.

### Genetic Algorithm
The Genetic Algorithm (GA) is a _metaheuristic_ (a higher-level procedure or heuristic designed to find, generate, or select a heuristic). It is is based on a parallel search mechanism, which makes it more efficient than other
                                                                                                                                                classical optimization techniques such as branch and bound, tabu search method and simulated annealing

The algorithm is inspired by the process of natural selection that belongs to the larger class of evolutionary algorithms (EA). 

GAs are commonly used to generate high-quality solutions to optimization and search problems by relying on bio-inspired operators such as mutation, crossover and selection.

##### Idea
Survival of the fittest through natural selection

* Generate a set of random solutions
* Repeat until best solution is good enough:
  * Test each solution in the set (rank them)
  * Remove some bad solutions from set
  * Duplicate some good solutions
  * Make small changes to some of them
  
##### Components
* **Gene**: Customer or TODO: Depot
* **DNA**: List of genes/Sublist of Chromosome/Part of a route
* **Chromosome**: Full DNA/List of merged DNAs/Full Route for one Vehicle
* **Population**: List of all Vehicles

* **Gene**: Customer or TODO: Depot
* **DNA**: List of genes/Sublist of Chromosome/Part of a route
* **Chromosome**: Full DNA/List of merged DNAs/Full Route for one Vehicle
* **Solution**: Full route for all Vehicles
* **Population**: List of all solutions

##### Operators
* **Initial Chromosome**: Randomly shuffled gene list
* **Crossover**: Mixes the DNA of two chromosomes
* **Mutation**: Shuffles two genes in a chromosome
* **Selection**: Selects the best from the last population

##### Decision Making stages
* Grouping: Customers are clustered based on distance between customers and depots
* Routing: 
* Scheduling: Starting from the first customer, the delivery sequence is chosen such that the next customer is as close as to the previous customer. This process is repeated until all the unselected customers are sequenced.
* Optimization
 
##### Fitness evaluation
