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

#include <string>
#include <stdio.h>
#include <iostream>
#include <vector>
#include <map>

using namespace std;

class Node;

struct Datum {
	int val;
	Datum * left;
	Datum * right;
}; // Element of a linked list containing all the data used in batch learning

Datum * insert(Datum * list, Datum * d) { // insert a single element into a circular linked list
	if (list == 0) {
		list = d;
		d->left  = d;
		d->right = d;
	} else {
		d->right = list->right;
		d->left  = list;
		list->right->left = d;
		list->right       = d;
	}
	return list;
}

void remove(Datum * d) {
	if(d->right != d) {
		d->right->left = d->left;
		d->left->right = d->right;
	}
}

Datum * splice(Datum * d1, Datum * d2) {
	if(d1 == 0) { // if there's no data for this edge
		return d2; // just drop in the data from the other edge
	} else if (d2 != 0) { // otherwise splice the two lists together
		d1->right->left = d2;
		d2->right->left = d1;
		Datum * foo = d2->right; // placeholder
		d2->right = d1->right;
		d1->right = foo;
	}
	return d1;
}

struct Edge {
	Node * tail;
	Node * head;
	int label;
	double weight;
	int count;
	Datum * data; // circular linked list of every data point that was emitted from this edge
	Edge * left;
	Edge * right;
	Edge() {
		head = 0;
		left = this;
		right = this;
		weight = 1.0;
		count = 0;
		data = 0;
	}
}; 
// Edges are stored in two places: an array in the tail node and a circular linked list in the head node. 
// This allows constant insertion and deletion into the head list both when manipulating a single edge and when
// merging all the edges from a single node.

class Node {
	double cumsum; // normalization factor

	int alphalen;
	Edge * forward; // All the edges of which this node is the tail node.
	Edge * back; // Root of a circular linked list of all the edges of which this node is the tail.

	bool blocked; // when recursively traversing the graph, eg in deleting or merging, 
	              // indicates whether the particular function is in the process of being 
	              // applied to this Node.
	public:
		char * name;
		Node(char * c, int n) {
			name = c;
			alphalen = n;

			blocked = false;
			cumsum = alphalen;

			forward = new Edge[alphalen];
			back = 0;
			for(int i = 0; i < alphalen; i++) {
				forward[i].tail  = this;
				forward[i].label = i;
			}
		}

		~Node() {
			blocked = true;
			cout << "Deleting " << name << "\n";
			for (int i = 0; i < alphalen; i++) {
				if (next(i) != 0 && !next(i)->blocked) {
					delete next(i);
				}
			}
			delete forward;
		}

		Node * next(int i) {
			return forward[i].head;
		}

		void clear() {
			blocked = true;
			for (int i = 0; i < alphalen; i++) {
				forward[i].count = 0;
				forward[i].data = 0;
				if (next(i) != 0 && !next(i)->blocked) {
					next(i)->clear();
				}
			}
		}

		Node * update(Datum * d, int i, bool b) {
			forward[i].count++;
			if (b) {
				forward[i].data = insert(forward[i].data, d);
			}
			return next(i);
		}

		double weight(int i) {
			return forward[i].weight;
		}

		// Note! This only unlinks the nodes. It does not delete them.
		// Also note: link and unlink do not change the count and data field of each Edge object.
		Node * unlink(int i) {
			Node * n = next(i);
			if (n != 0) {
				if(forward[i].left == &forward[i]) { // If the tail node is only linked to by a single edge, then the backwards edge list has only a single element
					n->back = 0; // dereference the linked list
				} else {
					forward[i].left->right = forward[i].right;
					forward[i].right->left = forward[i].left;
					if (n->back == &forward[i]) {
						n->back = forward[i].left;
					}
				}
				forward[i].head = 0;
			}
			return n;
		}

		Node * link(Node * n, int i, double d) {
			Node * old = unlink(i);
			if (d != 0.0) {
				cumsum += d - forward[i].weight;
				forward[i].weight = d;
			}
			forward[i].head = n;
			if (n->back == 0) { // If this is the first edge with n as its head...
				n->back = &forward[i]; // ...then make this edge the root of the backward-facing edge linked list
			} else {
				forward[i].right     = n->back->right;
				forward[i].left      = n->back;
				n->back->right->left = &forward[i];
				n->back->right       = &forward[i];
			}
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

		Node * create_node(int label, char * name) {
			Node * n = new Node(name, alphalen);
			link(n, label, 0.0);
			return n;
		}

		void merge(Node * n) {
			if (n != this) {
				while (n->back != 0) { // unlink the incoming edges from n until there are none left
					n->back->tail->link(this, n->back->label, 0.0);
				}
				for (int i = 0; i < alphalen; i++) {
					if (n->next(i) != 0) { 
						forward[i].count += n->forward[i].count;
						forward[i].data = splice(forward[i].data, n->forward[i].data);
						if (next(i) != 0 ) { // if there's a conflict between edges 
							next(i)->merge(n->next(i));
						} else {
							forward[i].head = n->next(i);
						}
					}
				}
				// really need to figure out why this isn't working. it's clearly a huge memory leak if we don't delete this.
				n->clear();
				// delete n;
			}
		}

		Node * split(Edge ** ptr_backward, int num_backward, char * name) {
			// still need to test this, implement splitting of counts and indices.
			Node * node = new Node(name, alphalen); 
			for(int i = 0; i < num_backward; i++) {
				ptr_backward[i]->tail->link(node, ptr_backward[i]->label, 0.0);
				Datum * d = ptr_backward[i]->data;
				if (d != 0) {
					for(int j = 0; j < ptr_backward[i]->count; j++) {
						int val = (d + 1)->val;
						if (val != -1) {
							forward[val].count--;
							remove(d+1);
							node->forward[val].count++;
							node->forward[val].data = insert(node->forward[val].data, d+1);
						}
						d = d->left;
					}
					if(d != ptr_backward[i]->data) {
						cout << "This should never happen. Edge.count should equal size of Edge.data if Edge.data is not zero.\n";
					}
				}
			}
			for(int i = 0; i < alphalen; i++) {
				node->link(next(i), i, weight(i));
			}
			return node;
		}

		void write_gv(FILE * f, char * alph) {
			blocked = true;
			for (int i = 0; i < alphalen; i++) {
				if (next(i) != 0) {
					fwrite("\t",            sizeof(char), 1,                     f);
					fwrite(name,            sizeof(char), strlen(name),          f);
					fwrite(" -> ", 	        sizeof(char), 4,                     f);
					fwrite(next(i)->name,   sizeof(char), strlen(next(i)->name), f);
					fwrite(" [ label = \"", sizeof(char), 12,                    f);
					fwrite(alph+i,          sizeof(char), 1,                     f);
					fwrite("\" ];\n",       sizeof(char), 5,                     f);
					if (!next(i)->blocked) {
						next(i)->write_gv(f,alph);
					}
				}
			}
		}
};

class Automata {
	bool doIndex; // do we index pointers to the data, or just count?
	vector<Datum*> data;
	char * alphabet;
	map<char,int> alphamap;
	int alphalen;
	public:
		Node * start;
		Automata (char * alph) {
			alphalen = strlen(alph);
			alphabet = alph;
			start = new Node("0",alphalen);
			doIndex = true;
			for (int i = 0; i < alphalen; i++) {
				alphamap[alphabet[i]] = i;
				start->link(start, i, 0.0);
			}
		}

		~Automata () {
			delete start;
		}

		void run(char * c) {
			Node * current = start;
			int len = strlen(c);
			Datum * line = new Datum[len+1];
			data.push_back(line);
			for(int i = 0; i < len; i++) {
				int val = alphamap[c[i]];
				line[i].val = val;
				current = current->update(&line[i], val, doIndex);
			}
			line[len].val = -1; // identifies the end of a line
		}

		void clear() {
			start->clear();
			start->unblock();
		}
		
		int write_gv(char * fname) {
			// Write the graph to a .gv file
			char * filename = new char[strlen(fname) + 3];
			strcpy(filename, fname);
			strcat(filename, ".gv");
			FILE * fout = fopen(filename,"w");
			if (fout != 0) {
				char * header = "digraph finite_state_machine {\n\trankdir=LR;\n\tsize=\"8,5\"\n\tnode [shape = doublecircle]; 0;\n\tnode [shape = circle];\n";
				fwrite(header, sizeof(char), strlen(header), fout);
				start->write_gv(fout, alphabet);
				start->unblock();
				fwrite("}\n", sizeof(char), 2, fout);
				fclose(fout);
				return 0;
			} else {
				return -1;
			}
		}
};

class Even: public Automata {
	public:
		Even(): Automata("AB") {

		}
};

class Reber: public Automata {
	public:
		Reber(): Automata("BTPSVXE") {

		}
};
class Feldman: public Automata {
	public:
		Feldman(): Automata("AB") {

		}
};

int main(int argc, char ** argv) {
	Automata * foo;
	foo = new Automata ("ab");
	Node * n1 = foo->start->create_node(0, "1");
	Node * n2 = foo->start->create_node(1, "2");
	n2->link(n1, 1, 0.0);
	Node * n3 = n1->create_node(0, "3");
	Node * n4 = n2->create_node(0, "4");
	Node * n5 = n3->create_node(1, "5");
	n4->link(n2, 1, 0.0);
	n3->link(foo->start, 0, 0.0);
	foo->write_gv(argv[1]);
	n1->merge(n2);
	foo->write_gv(argv[2]);
	delete foo;
}