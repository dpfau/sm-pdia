#include <iostream>
#include <vector>
#include <map>

#define NAME_LEN 256  // maximum length of a name for a node

using namespace std;

class Node;
class Automata;

typedef int (Node::*NodeFn)(); // function pointer for member functions of Node.

class AutomataIterator;
class Scorer;
class Counter;
class Generator;

// Element of a circular linked list containing all the data used in batch learning.
// The linked list contains every datum that transitions on that edge of the automata.
struct Datum {
	int val;
	Datum * left;
	Datum * right;
};

Datum * insert(Datum * list, Datum * d);
void remove(Datum * d);
Datum * splice(Datum * d1, Datum * d2);
Automata * load(char * fname);