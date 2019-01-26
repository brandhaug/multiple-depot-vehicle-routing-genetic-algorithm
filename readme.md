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
The Genetic Algorithm (GA) is a _metaheuristic_ (a higher-level procedure or heuristic designed to find, generate, or select a heuristic). 

The algorithm is inspired by the process of natural selection that belongs to the larger class of evolutionary algorithms (EA). 

GAs are commonly used to generate high-quality solutions to optimization and search problems by relying on bio-inspired operators such as mutation, crossover and selection.

##### Components
* **Gene**: Customer or TODO: Depot
* **DNA**: List of genes/Sublist of Chromosome/Part of a route
* **Chromosome**: Full DNA/List of merged DNAs/Full Route for one Vehicle
* **Population**: List of all Vehicles

##### Operators
* **Initial Chromosome**: Randomly shuffled gene list
* **Crossover**: Mixes the DNA of two chromosomes
* **Mutation**: Shuffles two genes in a chromosome
* **Selection**: 


 