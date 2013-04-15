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
#include <string>

using namespace std;

// Really writing this for speed instead of generality. That means fixing the alphabet at compile time.
static const int alphalen = 2;
char alphabet[] = "abcdefghijklmnopqrstuvwxyz ";

class Node;

struct Edge {
	Node * tail;
	Node * head;
	int label;
	double weight;
	Edge * left;
	Edge * right;
	Agedge_t * gedge; // Graphviz data structure. This means basically the entire graph structure is duplicated. Oh well.
	Edge() {
		head = 0;
		left = this;
		right = this;
		gedge = 0;
		weight = 1.0;
	}
}; 
// Edges are stored in two places: an array in the tail node and a circular linked list in the head node. 
// This allows constant insertion and deletion into the head list both when manipulating a single edge and when
// merging all the edges from a single node.

class Node {
	double cumsum; // normalization factor

	Edge forward[alphalen]; // All the edges of which this node is the tail node. Of a known size so we use a fixed array.
	Edge * back; // Root of a circular linked list of all the edges of which this node is the tail.

	Agraph_t * g; // pointer to top-level graph
	bool blocked; // when recursively traversing the graph, eg in deleting or merging, 
	              // indicates whether the particular function is in the process of being 
	              // applied to this Node.
	public:
		char * name;
		Agnode_t * gnode; // graphviz data structure
		Node(char * c, Agraph_t *G) {
			g = G;
			name = c;
			gnode = agnode(G, name, 1);
			blocked = false;
			cumsum = alphalen;
			back = 0;
			for(int i = 0; i < alphalen; i++) {
				forward[i].tail  = this;
				forward[i].label = i;
			}
			for(int i = 0; i < alphalen; i++) {
				link(this, i, 1.0);
			}
		}

		~Node() {
			blocked = true;
			for (int i = 0; i < alphalen; i++) {
				if (next(i) != 0 && !next(i)->blocked) {
					delete next(i);
				}
			}
			blocked = false;
		}

		Node * next(int i) {
			return forward[i].head;
		}

		double weight(int i) {
			return forward[i].weight;
		}

		// Note! This only unlinks the nodes. It does not delete them.
		Node * unlink(int i) {
			Node * n = next(i);
			if (n != 0) {
				if(forward[i].left == &forward[i]) { // If the tail node is only linked to by a single edge, then the backwards edge list has only a single element
					n->back = 0; // dereference the linked list
				} else {
					forward[i].left->right = forward[i].right;
					forward[i].right->left = forward[i].left;
				}
				agdeledge(g, forward[i].gedge);
				forward[i].gedge = 0;
				forward[i].head = 0;
			}
			return n;
		}

		Node * link(Node * n, int i, double d) {
			Node * old = unlink(i);
			if (d != 0.0) {
				cumsum += d - weight(i);
				forward[i].weight = d;
			}
			forward[i].head = n;
			if (n->back == 0) { // If this is the first edge with n as its head...
				n->back = &forward[i]; // ...then make this edge the root of the backward-facing edge linked list
			} else {
				forward[i].right = n->back->right;
				forward[i].left  = n->back;
				n->back->right   = &forward[i];
			}
			forward[i].gedge = agedge(g, gnode, n->gnode, &alphabet[i], 1);
			return old;
		}

		void unblock() {
			blocked = false;
			for (int i = 0; i < alphalen; i++) {
				if (next(i) != 0 && next(i)->blocked) {
					next(i)->unblock();
				}
			}
		}

		void merge(Node * n) {
			if (n != this) {
				while (n->back != 0) { // unlink the incoming edges from n until there are none left
					link(n->back->tail, n->back->label, 0.0);
				}
				for (int i = 0; i < alphalen; i++) {
					if (next(i) != 0 && n->next(i) != 0) { // if there's a conflict between edges 
						next(i)->merge(n->next(i));
					}
				}
				agdelnode(g, n->gnode);
			}
		}

		Node * split(Edge ** ptr_backward, int num_backward, char* name) {
			Node * node = new Node(name, g); 
			for(int i = 0; i < num_backward; i++) {
				ptr_backward[i]->tail->link(node, ptr_backward[i]->label, 0.0);
			}
			for(int i = 0; i < alphalen; i++) {
				node->link(next(i), i, weight(i));
			}
			return node;
		}
};

class Automata {
	Agraph_t * G; // graphviz graph
	GVC_t * gvc; // graphviz context
	public:
		Node * start;
		Automata (char* fname) {
			G = agopen(fname, Agdirected, 0);
			gvc = gvContext();
			start = new Node("_", G);
			agsafeset(start->gnode, "shape", "doublecircle", "ellipse"); // Indicate the root node with a double circle
		}

		~Automata () {
			agclose(G); 
			gvFreeContext(gvc);
		}

		void split(Node*);
		
		double run(int ** data);

		void viz(char * gname) {
			gvLayout (gvc, G, "dot");
			// drawGraph (G);
			gvFreeLayout(gvc, G); 
		}

		Node * create_node(Node * root, int label) {
			Node * n = new Node(root->name + alphabet[label], G);
			root->link(n, label, 0.0);
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
	foo->create_node(foo->start, 0);
	foo->create_node(foo->start, 1);
	delete foo;
}