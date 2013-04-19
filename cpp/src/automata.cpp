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

#include "automata.h"
#include <fstream>

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
	d->right->left = d->left;
	d->left->right = d->right;
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

// Edges are stored in two places: an array in the tail node and a circular linked list in the head node. 
// This allows constant insertion and deletion into the head list both when manipulating a single edge and when
// merging all the edges from a single node.

class Node {
	struct Edge {
		Node * tail; // the node for which this is an outgoing edge
		Node * head; // the node for which this is an incoming edge
		int label; // the emission symbol for the edge
		double weight; // the weight or number of pseudocounts for this edge
		int count; // the number of counts in the data, ie the length of the linked list "data"
		Datum * data; // circular linked list of every data point that was emitted from this edge
		Edge * left; // elements of a circular linked list of every incoming edge to the node "head"
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

	double weight; // normalization factor
	int count; // total counts, summed over all edges
	int alphalen;

	Edge * forward; // All the edges of which this node is the tail node. Size is fixed as alphalen.
	Edge * back; // Root of a circular linked list of all the edges of which this node is the tail. Size is variable.

	Node * merge_to; // If merge has already been called on this node, indicates the node into which this is being merged.
	bool blocked; // when recursively traversing the graph, eg in deleting or counting,
	              // indicates whether this node has already been visited.
	public:
		const char * name;
		friend Automata * load(char*);
		Node(const char * c, int n) {
			if (strlen(c) > NAME_LEN) {
				cout << "No node should have a name longer than 256 characters and no computer needs more than 64K of memory.\n";
			}
			name = c;
			alphalen = n;

			blocked = false;
			merge_to = this;
			weight = alphalen;
			count = 0;

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
			count++;
			if (b) {
				forward[i].data = insert(forward[i].data, d);
			}
			return next(i);
		}

		double get_weight(int i) {
			if (i >= 0 && i < alphalen) {
				return forward[i].weight;
			} else if (i == -1) {
				return weight;
			} else {
				return 0.0;
			}
		}

		void set_weight(int i, double d) {
			if (d >= 0) {
				weight += d - forward[i].weight;
				forward[i].weight = d;				
			}
		}

		int get_count(int i) {
			if (i >= 0 && i < alphalen) {
				return forward[i].count;
			} else if (i == -1) {
				return count;
			} else {
				return 0;
			}
		}

		int count_all() {
			blocked = true;
			int count_all = count;
			for (int i = 0; i < alphalen; i++) {
				if (next(i) != 0 && !next(i)->blocked) {
					count_all += next(i)->count_all();
				}
			}
			return count_all;
		}

		// for debugging only, should give same answer as count_all()
		int count_data() {
			blocked = true;
			int count_data = 0;
			for (int i = 0; i < alphalen; i++) {
				Datum * current = forward[i].data;
				do {
					count_data++;
					current = current->left;
				} while (current != forward[i].data);
				if (next(i) != 0 && !next(i)->blocked) {
					count_data += next(i)->count_data();
				}
			}
			return count_data;
		}

		Edge * edge(int i) {
			return &forward[i];
		}

		int sample() {
			double r = rand()/double(RAND_MAX);
			double cdf = 0.0;
			for(int i = 0; i < alphalen; i++) {
				cdf += (get_weight(i) + get_count(i))/(get_weight(-1) + get_count(-1));
				if(cdf > r) return i;
			}
			return alphalen;
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
			set_weight(i, d);
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
			link(n, label, -1);
			return n;
		}

		void merge(Node * n) {
			// should rewrite this to return parameters that can be passed to split to undo the merge.
			if (n != this) {
				if (strcmp(n->name,"0") == 0) { // never merge the start node into something
					n->merge(this);
				} else if (n->merge_to != n) { 
					merge(n->merge_to);
				} else {
					n->merge_to = this;
					cout << name << " <- " << n->name << '\n';
					while (n->back != 0) { // unlink the incoming edges from n until there are none left
						n->back->tail->link(this, n->back->label, -1);
					}
					for (int i = 0; i < alphalen; i++) {
						cout << name << ", " << i << ": " << forward[i].count << '\n';
						if (n->next(i) != 0) { 
							cout << n->name << ", " << i << ": " << n->forward[i].count << '\n';
							merge_data(n, i);
							if (next(i) != 0 ) { // if there's a conflict between edges 
								merge_to->next(i)->merge(n->next(i));
							} else {
								merge_to->forward[i].head = n->next(i);
							}
						}
					}
					for (int i = 0; i < n->alphalen; i++) {
						n->forward[i].head = 0;
					}
					delete n;
				}
			}
		}

		void merge_data(Node * n, int i) { // helper function for merge
			if (merge_to == this) {
				forward[i].count += n->forward[i].count;
				count            += n->forward[i].count;
				forward[i].data = splice(forward[i].data, n->forward[i].data);
			} else {
				merge_to->merge_data(n, i);
			}
		}

		void unmerge();
		void endmerge(); // would be really cool if we could use these as callbacks that we pass to the sampler.

		Node * split(Edge ** ptr_backward, int num_backward, char * name) {
			// still need to test this
			Node * node = new Node(name, alphalen); 
			for(int i = 0; i < num_backward; i++) {
				if (ptr_backward[i]->head == this) { // Just a safety here. Really shouldn't ever pass a pointer to an edge that isn't linked to this node.
					ptr_backward[i]->tail->link(node, ptr_backward[i]->label, -1);
					Datum * d = ptr_backward[i]->data;
					if (d != 0) {
						for(int j = 0; j < ptr_backward[i]->count; j++) {
							int val = (d + 1)->val;
							if (val != -1) {
								forward[val].count--;
								remove(d+1);
								if (d+1 == forward[val].data) {
									if ((d+1)->left == (d+1)) {
										forward[val].data = 0;
									} else {
										forward[val].data = (d+1)->left;
									}
								}
								node->count++;
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
			}
			for(int i = 0; i < alphalen; i++) {
				if (next(i) != 0) {
					node->link(next(i), i, get_weight(i));
				}
			}
			return node;
		}

		void unsplit();
		void endsplit();

		void write(FILE * f) {
			blocked = true;
			fprintf(f, "%s\n", name);
			for (int i = 0; i < alphalen; i++) {
				if (next(i) == 0) {
					fprintf(f, "\t%lg\t%d\tNONE\n",   forward[i].weight, forward[i].count);
				} else {
					fprintf(f, "\t%lg\t%d\t%s\n", forward[i].weight, forward[i].count, next(i)->name);
				}
			}
			for (int i = 0; i < alphalen; i++) {
				if (next(i) != 0 && !next(i)->blocked) {
					next(i)->write(f);
				}
			}
		}

		void write_gv(FILE * f, char * alph, bool pw) {
			blocked = true;
			for (int i = 0; i < alphalen; i++) {
				if (next(i) != 0) {
					if (pw) {
						fprintf(f, "\t%s -> %s [ label = \"%c/%g\" ];\n", name, next(i)->name, alph[i], get_weight(i)/weight);
					} else {
						fprintf(f, "\t%s -> %s [ label = \"%c\" ];\n",    name, next(i)->name, alph[i]);						
					}
					if (!next(i)->blocked) {
						next(i)->write_gv(f,alph,pw);
					}
				}
			}
		}

		bool check_data() {
			// checks that the data structure forward[i].data has the expected length
			blocked = true;
			for (int i = 0; i < alphalen; i++) {
				if (forward[i].data != 0) {
					Datum * current = forward[i].data->left;
					for (int j = 1; j < forward[i].count; j++) {
						if (current == forward[i].data) {
							cout << name << ", " << i << ", " << j << '\n';
							return false;
						}
						current = current->left;
					}
					if (current != forward[i].data) {
						cout << name << ", " << i << '\n';
						return false;
					}
					if (next(i)!=0 && !next(i)->blocked && !next(i)->check_data()) {
						return false;
					}
				}
			}
			return true;
		}
};

class Automata {
	public:
		bool doIndex; // do we index pointers to the data, or just count?
		vector<Datum*> data;
		vector<int> len; // length of each line
		map<char,int> alphamap;
		int alphalen;
		char * alphabet;
		Node * start;
		Automata (char * alph) {
			alphalen = strlen(alph);
			alphabet = alph;
			start = new Node("0",alphalen);
			doIndex = true;
			for (int i = 0; i < alphalen; i++) {
				alphamap[alphabet[i]] = i;
				start->link(start, i, -1);
			}
		}

		~Automata () {
			delete start;
		}

		/*void run(char * c) {
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
		}*/

		// see, this block of functions here could probably be turned into one if I used function pointers.
		void clear() {
			start->clear();
			start->unblock();
		}

		int count() {
			int count = start->count_all();
			start->unblock();
			return count;
		}

		// this should return the same value as above, so this is just being used for debugging purposes
		int count_data() {
			int count = start->count_data();
			start->unblock();
			return count;
		}

		bool check_data() {
			bool b = start->check_data();
			start->unblock();
			return b;
		}
		
		int write(char * fname) {
			FILE * fout = fopen(fname,"w");
			if (fout != 0) {
				fwrite(alphabet, 1, alphalen, fout);
				fwrite("\n", 1, 1, fout);
				start->write(fout);
				start->unblock();
				fclose(fout);
				return 0;
			}
			return -1;
		}

		int write_gv(char * fname, bool pw) {
			// Write the graph to a .gv file
			// if pw is true, print weights of the edges
			char * filename = new char[strlen(fname) + 3];
			strcpy(filename, fname);
			strcat(filename, ".gv");
			FILE * fout = fopen(filename,"w");
			if (fout != 0) {
				char * header = "digraph finite_state_machine {\n\trankdir=LR;\n\tsize=\"8,5\"\n\tnode [shape = doublecircle]; 0;\n\tnode [shape = circle];\n";
				fwrite(header, 1, strlen(header), fout);
				start->write_gv(fout, alphabet, pw);
				start->unblock();
				fwrite("}\n", 1, 2, fout);
				fclose(fout);
				return 0;
			} 
			return -1;
		}
};

class AutomataIterator{
	protected:
		Automata * parent;
		Node * current;
		int i, iter, len;
	public:
		AutomataIterator(Automata * a) { 
			parent = a;
			current = parent->start;
			iter = 0;
		}
		AutomataIterator& operator++();
		AutomataIterator  operator++(int);
		bool end() {
			return (current ==  0 || iter >= len);
		}
};

class Scorer: public AutomataIterator {
	const char * line;
	void value() {
		if (current == 0) {
			val = 1.0;
		} else {
			i = parent->alphamap[line[iter]];
			val = (current->get_weight(i) + current->get_count(i))/(current->get_weight(-1) + current->get_count(-1));
		}
	}
	public:
		double val;
		Scorer(Automata * a, const char * c): AutomataIterator(a) {
			len = strlen(c);
			line = c;
			value();
		}

		Scorer& operator++() {
			if (current != 0) {
				current = current->next(i);
				iter++;
				value();
			}
			return *this;
		}

		Scorer operator++(int) {
			Scorer tmp(*this);
			operator++();
			return tmp;
		}
};

class Counter: public AutomataIterator {
	const char * line;
	Datum * data;
	void count() {
		if (!end()) {
			i = parent->alphamap[line[iter]];
			data->val = i;
			current->update(data, i, parent->doIndex);
		}
	}
	public:
		Counter(Automata * a, const char * c): AutomataIterator(a) {
			len = strlen(c);
			line = c;
			data = new Datum[len];
			parent->data.push_back(data);
			parent->len.push_back(len);
			count();
		}

		Counter& operator++() {
			if (current != 0) {
				current = current->next(i);
				data++;
				iter++;
				count();
			}
			return *this;
		}

		Counter operator++(int) {
			Counter tmp(*this);
			operator++();
			return tmp;
		}
};

class Generator: public AutomataIterator {
	int i;
	public:
		char val;
		Generator(Automata * a, int n): AutomataIterator(a) {
			len = n;
			i = current->sample();
			val = parent->alphabet[i];
		}

		Generator& operator++() {
			if (current != 0) {
				current = current->next(i);
				if (current == 0) {
					val = '\0';
				} else {
					i = current->sample();
					val = parent->alphabet[i];
				}
			}
			iter++;
			return *this;
		}

		Generator operator++(int) {
			Generator tmp(*this);
			operator++();
			return tmp;
		}
};

Automata * load(char * fname) {
	// Right now, if a file isn't properly parsed it won't throw an error, it'll just get stuck forever.
	FILE * f = fopen(fname,"r");
	Node * n;
	map<string, Node*>   nodemap;
	map<string, string*> edgemap;
	if (f != 0) {
		vector<char> line;
		char c;
		while((c = fgetc(f)) != '\n') {
			line.push_back(c);
		}
		line.push_back('\0');
		
		int len = strlen(&line[0]);
		char * alph = new char[len];
		strcpy(alph, &line[0]); // since we're constantly creating and destroying the vector line, we should copy anything we want to keep.
		Automata * a = new Automata(alph);
		int idx = -1;
		string * first;
		while((c = fgetc(f)) != EOF) {
			line.clear();
			line.push_back(c);
			while((c = fgetc(f)) != '\n') {
				line.push_back(c);
			}
			line.push_back('\0');

			if (idx == -1) {
				first = new string(&line[0]);
				if (strcmp(&line[0],"0")==0) { // If this is the initial node
					n = a->start;
				} else {
					n = new Node(first->c_str(),len);
				}
				nodemap[*first] = n;
				edgemap[*first] = new string[len];
			} else {
				char second[NAME_LEN];
				sscanf(&line[0], "\t%lg\t%d\t%s", &(n->forward[idx].weight), &(n->forward[idx].count), second);
				edgemap[*first][idx] = *(new string(second));
			}
			idx++;
			if (idx == len) {
				n->weight = 0;
				for (int i = 0; i < len; i++) {
					n->weight += n->get_weight(i);
					n->count  += n->get_count(i);
				}
				idx = -1; // reset once we've scanned all the edges of this node
			}
		}
		fclose(f);
		map<string, Node*>::iterator p;
		for (p = nodemap.begin(); p != nodemap.end(); p++) {
			for (int i = 0; i < len; i++) {
				if (edgemap[p->first][i] == "NONE") {
					p->second->forward[i].head = 0;
				} else {
					p->second->link(nodemap[edgemap[p->first][i]], i, -1);
				}
			}
		}
		edgemap.clear();
		return a;
	}
	return 0;
}

int main(int argc, char ** argv) {
	if (argc > 1) {
		Automata * aut = load(argv[1]);
		char use = 'c'; // for now do this by hand
		if (aut != 0) {
			switch(use) {
				case 'a':
					break;
				case 's': // would be nice if we could write another case that pipes it in from the command line (cin instead of fin?)
				{
					ifstream fin;
					string data;
					fin.open(argv[2]);
					fin >> data;
					for (Scorer s(aut,data.c_str()); !s.end(); s++) {
						cout << s.val << '\n';
					}
					break;
				}
				case 'c':
				{
					ifstream fin;
					string data;
					fin.open(argv[2]);
					fin >> data;
					cout << aut->check_data() << '\n';
					for (Counter c(aut,data.c_str()); !c.end(); c++);
					for (Counter c(aut,data.c_str()); !c.end(); c++);
					cout << aut->count() << '\n';
					cout << aut->count_data() << '\n';
					cout << aut->check_data() << '\n';
					aut->start->next(0)->merge(aut->start->next(1));
					cout << aut->count() << '\n';
					cout << aut->count_data() << '\n';
					cout << aut->check_data() << '\n';
					break;
				}
				case 'g':
					for (Generator g(aut,atoi(argv[2])); !g.end(); g++) {
						cout << g.val;
					}
					break;
				case 'p':
					aut->write_gv(argv[2],true);
					break;
			}
		} else {
			cout << "Automata file " << argv[1] << " could not be parsed.\n";
		}
	} else {
		cout << "You need to pass me some arguments, bro.\n";
		cout << "Usage (not really, I haven't actually implemented this yet):\n";
		cout << "  -a load automata file from provided file name.\n";
		cout << "  -s score data loaded from filename.\n";
		cout << "  -c count data loaded from filename.\n";
		cout << "  -g generate a certain number of characters from the automata.\n";
		cout << "  -p print automata via graphviz with specified format.\n";
	}
}