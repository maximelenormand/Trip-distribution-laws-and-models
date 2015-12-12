Systematic comparison of trip distribution laws and models
========================================================================

 Copyright 2015 Maxime Lenormand. All rights reserved. Code under License GPLv3.
______________________________________________________________________________________


## Introduction 

This package is created to generate spatial networks as described in [[1]](http://arxiv.org/abs/1506.04889). In this paper, we propose 
a comparison between the gravity and the intervening opportunities approaches widely used to simulate mobility flows. To fairly compare the two 
approaches, we need to differentiate the trip distribution laws, gravity or intervening opportunities, and the modeling approach used to generate 
the flows from the laws. Indeed, both the gravity and the intervening opportunities laws can be express as the probability of having a trip from one place to 
another and based on these probabilities, the total number of commuters or migrants can then be calculated using different level of constrained 
models.

First, we compute the probability ***pij*** to have a trip from region ***i*** to another region ***j*** based on the "travel demand" ***mi*** of 
the region ***i*** (the population is typically used as a surrogate), the "attractivity" ***mj*** of the region ***j*** (usually related to the 
population in ***j*** too) and the distance ***dij*** between the two regions. In this package we consider six probabilistic laws:

* Gravity law with an exponential distance decay function (**GravExp**).
* Normalized gravity law with an exponential distance decay function (**NGravExp**).
* Gravity law with a power distance decay function (**GravPow**).
* Normalized gravity law with a power distance decay function (**NGravPow**).
* Schneider's intervening opportunities law (**Schneider**) [[2]](http://onlinelibrary.wiley.com/doi/10.1111/j.1435-5597.1959.tb01665.x/abstract).
* Radiation law (**Rad**) [[3]](http://www.nature.com/nature/journal/v484/n7392/full/nature10856.html).
* Extended radiation law (**RadExt**) [[4]](http://www.nature.com/srep/2014/140711/srep05662/full/srep05662.html).
* Uniform law (**Rand**).

The importance of the distance and/or the scale is adjusted with a parameter **beta** (except for the original radiation law and the uniform law).  

Second, several constrained models are proposed to generate a spatial network from these distribution of probability respecting different level of 
constraints (preserving the marginals ***Oi*** and/or ***Dj*** of the observed OD matrix) according to the model:
 
* Unconstrained model (**UM**).
* Production constrained model (**PCM**).
* Attraction constrained model (**ACM**).
* Doubly constrained model (**DCM**).

## Contents of the package

<<<<<<< HEAD
All the inputs and outputs files are in **csv** format with column names column names in the first row and no row names (the value separator 
is a semicolon **";"**). Note that the row number and the column number for the matrices is an implicit ID. See the example for more detailed. 

**Inputs**: 
=======
**Inputs**: 

* *Parameters.csv*: File containing the four parameters:
   * Law: **GravExp**, **NGravExp**, **GravPow**, **NGravPow**, **Schneider**, **Rad**, **RadExt** or **Rand**.
   * Model: **UM**, **PCM**, **ACM** or **DCM**.
   * Beta: Parameter used to adjust the importance of the distance and/or the scale. Not necessary for the original radiation law.
   * Replication: Number of replications *r*.
* *Inputs.csv*: File with n lines (n represents the number of regions) and 4 columns (***mi*** and ***mj***, ***Oi*** and ***Dj***).
* *Distance.csv*: n x n distance matrix.
* *OD.csv*: n x n observed OD matrix.
* *sij.csv*: n x n "opportunity" matrix. Only for the radiation laws, can be generated with the function *Sij.java*.
>>>>>>> origin/master

* ***Parameters.csv***: File containing the four parameters:
   * *Law:* **GravExp**, **NGravExp**, **GravPow**, **NGravPow**, **Schneider**, **Rad**, **RadExt** or **Rand**.
   * *Model:* **UM**, **PCM**, **ACM** or **DCM**.
   * *Beta:* Parameter used to adjust the importance of the distance and/or the scale. Not necessary for the original radiation law.
   * *Replication:* Number of replications *r*.
   * *Write_pij:* *true* to write the matrix of probabilities ***pij*** in a csv file. 
* ***Inputs.csv:*** File with n lines (n represents the number of regions) and 4 columns (***mi*** and ***mj***, ***Oi*** and ***Dj***).
* ***Distance.csv:*** n x n distance matrix.
* ***OD.csv:*** n x n observed OD matrix.
* ***sij.csv:*** n x n "opportunity" matrix. Only for the intervening opportunities laws, can be generated with the function ***Sij.java***.

**Functions**:
 
* ***TDLM.java:*** This class takes as inputs all the inputs described above (except ***sij.csv*** depending of the case). 
It returns *r* simulated OD matrices ***S_1.csv, ..., S_r.csv***.
* ***Sij.java:*** This class takes as inputs the files ***Inputs.csv*** and ***Distance.csv*** and it returns the "opportunity" matrix ***sij.csv***. 
* ***GOF.java:*** This class takes as inputs the files ***Inputs.csv***, ***OD.csv***, ***Distance.csv*** and *r* simulated OD matrices ***S_1.csv, ..., S_r.csv***. 
It returns a file ***GOF.csv*** containing the three goodness-of-fit measures described in the paper 
(**CPC** [[5]](http://jasss.soc.surrey.ac.uk/15/2/6.html) [[6]](http://journals.plos.org/plosone/article?id=10.1371/journal.pone.0045985) 
[[7]](https://www.jtlu.org/index.php/jtlu/article/view/360), **CPL** and **CPCd**) between the observed OD matrix and each of the simulated 
OD matrices. 

## Compiling

The package contains the source code (Java) and an example of inputs (see below). Java Code can be easily run and compiled with IDEs such as 
[Netbeans](https://netbeans.org/kb/docs/java/quickstart.html) or [Eclipse](https://eclipse.org/). If you are 
not familiar with these softwares you can also compile and run Java Code from a Command Line.

Before the Java virtual machine can run a Java program, the source code must be compiled into byte-code using the javac compiler 
using the command: 

*javac RadGrav.java*

Once you have successfully compiled your Java Code, you can run the code using the command:

*java RadGrav*

The Java files and the inputs must be in the same directory.

## Example

A zip file containing all the inputs of the **USA** case study is available [here](https://www.dropbox.com/s/it4h5d7wsfryorg/Example.zip?raw=1). 

* ***Parameters.csv:***
   * *Law:* **GravExp**
   * *Model:* **UM**
   * *Beta:* 0.0374466723531956
   * *Replication:* 5
   * *Write_pij:* true

* ***Inputs.csv:*** File with 3018 lines and 4 columns:
    * ***mi*** = population in county ***i***.
    * ***mj*** = population in county ***j***.
    * ***Oi*** = number of out-commuters in county ***i***.
    * ***Dj*** = number of in-commuters in county ***j***.

* ***Distance.csv:*** 3018 x 3018 distance matrix. Great circle distance between the centroids of the counties.
* ***OD.csv:*** 3018 x 3018 observed OD commuting matrix. 

The inputs come from the United State Census Bureau. The commuting trips between United States counties in 2000 are available 
[online](https://www.census.gov/population/www/cen2000/commuting/index.html). This dataset is the **USA** dataset in 
the paper and it has also been used in [[3]](http://www.nature.com/nature/journal/v484/n7392/full/nature10856.html) and 
[[6]](http://journals.plos.org/plosone/article?id=10.1371/journal.pone.0045985).

## References

[1] Lenormand *et al.* (2015) [Systematic comparison of trip distribution laws and models.](http://arxiv.org/abs/1506.04889). *Arxiv e-print*, arXiv:1501.07788.

[2] Schneider (1959)[Gravity models and trip distribution theory](http://onlinelibrary.wiley.com/doi/10.1111/j.1435-5597.1959.tb01665.x/abstract). *Papers of the regional science association* 5, 51-58.

[3] Simini *et al.* (2012) [A universal model for mobility and migration patterns](http://www.nature.com/nature/journal/v484/n7392/full/nature10856.html). *Nature 484, 96-100. 

[4] Yang *et al.* (2014) [Limits of Predictability in Commuting Flows in the Absence of Data for Calibration](http://www.nature.com/srep/2014/140711/srep05662/full/srep05662.html). *Scientific Reports* 4, 5662.

[5] Gargiulo *et al.* (2012) [Commuting network model: getting to the essentials](http://jasss.soc.surrey.ac.uk/15/2/6.html). *Journal of Artificial Societies and Social Simulation* 15, 6.

[6] Lenormand *et al.* (2012) [A Universal Model of Commuting Networks](http://journals.plos.org/plosone/article?id=10.1371/journal.pone.0045985). *PLoS ONE* 7, e45985.

[7] Lenormand *et al.* (2014) [Generating French Virtual Commuting Network at Municipality Level](https://www.jtlu.org/index.php/jtlu/article/view/360). *Journal of Transport and Land Use* 7, 43-55.

##Citation

If you use this code, please cite:

Lenormand M, Bassolas A & Ramasco JJ (2015) Systematic comparison of trip distribution laws and models. *Arxiv e-print*, arXiv:1501.07788.

If you need help, find a bug, want to give me advice or feedback, please contact me!
You can reach me at maxime[at]ifisc.uib-csic.es
