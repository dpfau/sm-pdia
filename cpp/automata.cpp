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
char alphabet[] = "abcdefghijklmnopqrstuvwxyz ";

struct loop {
	Agedge_t * val;
	loop * next;
	//loop * last;
}; // link in a circular linked list

class Node {
	double weight[alphalen]; // unnormalized emission probabilities
	double cumsum; // normalization factor
	Node * next[alphalen]; // transitions
	loop * back; // root of a circular linked list with pointers to all the edges that map into this Node
	Agraph_t * g; // pointer to top-level graph
	Agnode_t * gnode; // graphviz node pointer
	Agedge_t * edges[alphalen]; // makes things faster at the cost of higher memory, by storing edges instead of searching for them
	bool blocked; // when recursively traversing the graph, eg in deleting or merging, 
	              // indicates whether the particular function is in the process of being 
	              // applied to this Node.
	public:
		Node(char * name, Agraph_t *G) {
			g = G;
			gnode = agnode(G, name, 1);
			blocked = false;
			cumsum = alphalen;
			for(int i = 0; i < alphalen; i++) {
				weight[i] = 1.0;
			}
			for(int i = 0; i < alphalen; i++) {
				link(this, i, 1.0);
			}
			back = new loop;
			back->val = edges[1];
			loop * loop1 = back;
			for(int i = 1; i < alphalen; i++) {
				loop1->next = new loop;
				loop1->next->val = edges[i];
				// loop1->next->last = loop1;
				loop1 = loop1->next;
			}
			loop1->next = back;
			// back->last = loop1;
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

		// Note! This only unlinks the nodes. It does not delete them.
		Node * unlink(int i) {
			Node * n = next[i];
			if (n != 0) {
				next[i] = 0;
				agdeledge(g, edges[i]);
			}
			return n;
		}

		Node * link(Node * n, int i, double d) {
			Node * old = unlink(i);
			cumsum += d - weight[i];
			weight[i] = d;
			next[i] = n;
			edges[i] = agedge(g, gnode, n->gnode, &alphabet[i], 1);
			return old;
		}

		void unblock() {
			blocked = false;
			for (int i = 0; i < alphalen; i++) {
				if (next[i] != 0 && next[i]->blocked) {
					next[i]->unblock();
				}
			}
		}

		void merge(Node * n) {
			if (n!=this) {

			}
		}
};

class Automata {
	Agraph_t * G; // graphviz graph
	GVC_t * gvc; // graphviz context
	public:
		Automata (char* fname) {
			G = agopen(fname, Agdirected, 0);
			gvc = gvContext();
			Node root("Start", G);
		}

		~Automata () {
			agclose(G); 
			gvFreeContext(gvc);
		}

		void split(Node*);
		
		double run(int ** data);

		void viz(char * gname) {
			gvLayout (gvc, G, "sfdp");
			// drawGraph (G);
			gvFreeLayout(gvc, G); 
		}
};

void load_automata() {
}

void save_automata() {
}

int main(int argc, char ** argv) {
	char name[] = "foo";
	Automata * foo;
	foo = new Automata (name);
	delete foo;
}