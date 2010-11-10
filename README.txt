Self-Adjusting Top Trees Implementation and Demonstration
=========================================================-

Library:
--------
Maintaining a forest that changes over time through edge insertions and deletions is a well known problem. This implementation focuses on the simplified Top Tree interface which allows to solve a number of interesting graph problems like finding common ancestors, the heaviest edge, maintaining the diameter, center, or median and other (mostly network flow) problems. Using Top Tree interfaces all of outline problems can be solved in a clean declarative way. Our implementation uses adapting ST-trees as the underlying data structure achieving O(log n) time per expose(v,w) operation.

See: R. E. Tarjan, R. F. Werneck: Self-Adjusting Top Trees -- http://www.cs.princeton.edu/~rwerneck/docs/TW05.pdf (paper)
     R. E. Tarjan, R. F. Werneck: Self-Adjusting Top Trees -- http://www.cs.princeton.edu/~rwerneck/docs/TW05_p.pdf (presentation)


Demonstration
-------------
The demo shows a number of scripts in a simplified programming language TFL (translated to pure Java implementation) containing full solution of graph problems as outlined by Alstroup et. al in Maintaining Information in Fully-Dynamic Trees with Top Trees in O(log n) time. It also shows an easy way how to do an integration of toptree library and the host application.

See: S. Alstrup, J. Holm, K. de Lichtenberg, M. Thorup: Maintaining Information in Fully Dynamic Trees with Top Trees -- http://arxiv.org/abs/cs.DS/0310065


Technical Info
--------------
The library and the demonstration requires Java 5 and Maven 2.x or later versions.
