/*
* automata.cpp
* 
* God Help me, I'm going to try to write this automata class in C++.
*
* Should be able to use it on the command line like so:
*
* cat alice_in_wonderland.txt | automat foo.aut > foo.cnt
*
* which takes the file alice_in_wonderland.txt, runs it through the automata
* saved in foo.aut and outputs the results to a file foo.cnt which contains
* a data structure that maps each state to the points in the text that are
* in that state.
*
* David Pfau, 2013
*/

#define WITH_CGRAPH

#include <gvc.h>

using namespace std;

// Really writing this for speed instead of generality. That means fixing the alphabet at compile time.
static const int alphalen = 27;
static const char alphabet[] = "abcdefghijklmnopqrstuvwxyz ";

class Node {
	double weight[alphalen]; // unnormalized emission probabilities
	double cumsum; // normalization factor
	Node *next[alphalen]; // transitions
	public:
		Agraph_t *g; // pointer to top-level graph
		Agnode_t *gnode; // graphviz node pointer
		bool blocked; // when recursively traversing the graph, eg in deleting or merging, 
		              // indicates whether the particular function is in the process of being 
		              // applied to this Node.
		Node(char* name, Agraph_t *G) {
			g = G;
			gnode = agnode(G, name, 1);
			blocked = false;
			cumsum = alphalen;
			for(int i = 0; i < alphalen; i++) {
				weight[i] = 1.0;
				next[i] = this;
			}
		}

		~Node() {
			blocked = true;
			for (int i = 0; i < alphalen; i++) {
				if (next[i] != 0 && !next[i]->blocked) {
					delete next[i];
				}
			}
			blocked = false;
		}

		void unlink(int i) {

		}

		void link(Node *n, int i, double d) {

		}

		void unblock() {
			blocked = false;
			for (int i = 0; i < alphalen; i++) {
				if (next[i] != 0 && next[i]->blocked) {
					next[i]->unblock();
				}
			}
		}

		void merge(Node *n) {
			if (n!=this) {

			}
		}
};

class Automata {
	Agraph_t *G;
	GVC_t *gvc; // graphviz graph and context
	public:
		Automata (char* fname) {
			Node start ();
			G = agopen(fname, Agdirected, 0);
			gvc = gvContext();
		}
		~Automata () {
			agclose(G); 
			gvFreeContext(gvc);
		}
		void split (Node*);
		double run(int** data);

		void viz (char* gname) {
			gvLayout (gvc, G, "sfdp");
			// drawGraph (G);
			gvFreeLayout(gvc, G); 
		}
};

void load_automata() {
}

void save_automata() {
}

int main(int argc, char** argv) {
	char name[] = "foo";
	Automata * foo;
	foo = new Automata (name);
	delete foo;
}