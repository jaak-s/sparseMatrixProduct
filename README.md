sparseMatrixProduct
===================

Example Java code for implementing very fast crossproduct for sparse (boolean) matrixes.

Benchmark of crossproduct
---------
> **Summary:** SparseBoolMatrix is 3.5x faster compared to Matrix package in R.

Two sparse boolean matrices:
- m1 of size 204717 x 2759 with **345,434** non-zeros
- m2 of size 204717 x 4000 with **414,863** non-zeros

Calculation of crossproduct between m1 and m2 ( ```t(m1) %*% m2``` ) using Intel i7-4600M and Ubuntu 13.10.
- SparseBoolMatrix takes **190ms** (Java 7).
- R Matrix package takes **700ms** (R 3.1.0, Matrix_1.1-3).

Measured Java code:
```java
m1.prod( m2 );
```

Measured R code:
```s
crossprod(m1, m2)
```

Notes:
- In R the matrices m1 and m2 are sparse column-oriented matrices (created using sparseMatrix).
- The Matrix package in R uses SuiteSparse under its hood, so the crossproduct is running in optimised C code.
- The measured computation time is only for crossproduct (not for loading the matrices or other stuff).
