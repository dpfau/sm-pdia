#include <iostream>
#include <vector>
#include <map>

#define NAME_LEN 256  // maximum length of a name for a node

using namespace std;

class Node;
class Automata;

class AutomataIterator;
class Scorer:    public AutomataIterator;
class Counter:   public AutomataIterator;
class Generator: public AutomataIterator;

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