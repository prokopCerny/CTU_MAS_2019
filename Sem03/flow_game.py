#!/usr/bin/env python3

from math import factorial
import numpy as np
import gurobipy as g

from copy import copy

### TEMPLATE CODE COPY
class Base:
  def __init__(self, id):
    self.id = id

  def __eq__(self, other):
    return self.id == other.id

  def __neq__(self, other):
    return self.id != other.id

  def __hash__(self):
    return hash(self.id)

  def __repr__(self):
    return str(self.id)

class Node(Base):
  def __init__(self, id, incoming=None, outgoing=None):
    '''Takes an integer id,
    a set of incoming edges E- s.t. ∀(u, v) ∈ E-: v = self,
    a set of outgoing edges E+ s.t. ∀(v, w) ∈ E+: v = self.'''
    super(Node, self).__init__(id)
    self.id = id
    self.incoming = set() if incoming is None else incoming
    self.outgoing = set() if outgoing is None else outgoing

  def __copy__(self):
    return type(self)(id=self.id, incoming=copy(self.incoming), outgoing=copy(self.outgoing))

  def add_outgoing_edge(self, e):
    self.outgoing |= {e}

  def add_incoming_edge(self, e):
    self.incoming |= {e}

  def remove_outgoing_edges(self, E):
    self.outgoing -= E

  def remove_incoming_edges(self, E):
    self.incoming -= E

  def get_incoming_edges(self):
    return self.incoming

  def get_outgoing_edges(self):
    return self.outgoing

class DirEdge:
  def __init__(self, tail, head, l, u):
    '''Takes a Node object tail, and a Node object head, where e = (tail, head).
    The values l, u correspond to the lower (l), and upper (u) capacities.'''
    self.tail = tail
    self.head = head
    self.l = l
    self.u = u
    self.tail.add_outgoing_edge(self)
    self.head.add_incoming_edge(self)

  def __repr__(self):
    return str((self.tail.id, self.head.id, self.l, self.u))

  def __eq__(self, other):
    return self.tail == other.tail \
      and self.head == other.head \
      and self.l == other.l \
      and self.u == other.u

  def __neq__(self, other):
    return self.tail != other.tail \
      or self.head != other.head \
      or self.l != other.l \
      or self.u != other.u

  def __hash__(self):
    return hash((self.tail.id, self.head.id, self.l, self.u))

  def get_flow_requirements(self):
    return self.l, self.u

  def get_vertices(self):
    return self.tail, self.head

class Graph:
  def __init__(self, V=None, E=None, **kwargs):
    '''Takes a set V of Node objects, and a set E of Edge objects.'''
    self.V = set() if V is None else V
    self.E = set() if E is None else E

  def add_edge(self, e):
    '''Takes an Edge object e and adds it to the graph.'''
    self.V |= {e.tail, e.head}
    self.E |= {e}

  def get_max_flow(self):
    '''Constructs a linear program that computes the maximum flow of the graph.
    Returns a nonnegative real valued number.'''

    ''' IMPLEMENTATION NOTE ABOUT SOURCE AND SINK NODES:
    These nodes are virtual; that is, they will match the real ones by id, but they
    are not the same objects, and therefore, don't contain the same content.
    For instance: V & {source} = {source}, which is virtual, and shouldn't be used.
    However, V - (V - {source}) = {real_source}.'''
    source = Node(id=0)
    sink = Node(id=1)
    model = g.Model()
    model.setParam('OutputFlag', 0)
    x = {e: model.addVar(lb=e.l, ub=e.u, vtype=g.GRB.CONTINUOUS, name=str(e)) for e in self.E}
    model.update()
    for v in self.V - {source, sink}:
      inflow = sum([x[e] for e in v.get_incoming_edges()])
      outflow = sum([x[e] for e in v.get_outgoing_edges()])
      model.addConstr(inflow == outflow, name=str(v))
    model.setObjective(sum([x[e] for s in self.V - (self.V - {source}) for e in s.get_outgoing_edges()]), sense=g.GRB.MAXIMIZE)
    model.optimize()
    # print([x[e].x for e in self.E])
    return model.objVal

  def get_induced_graph_by_edges(self, E=None):
    '''Takes a set E of Edge objects (belonging to the graph).
    Returns a new graph G', which is the vertex-induced graph constructed from all the vertices in E.'''
    V = {copy(e.tail) for e in E} | {copy(e.head) for e in E}
    for v in V:
      v.remove_incoming_edges(self.E - E)
      v.remove_outgoing_edges(self.E - E)
    return Graph(V=V, E=E)

class Agent(Base):
  def __init__(self, id):
    '''Takes an integer id.'''
    super(Agent, self).__init__(id)

  def get_id(self):
    return self.id


### END TEMPLATE CODE COPY





class Game:
    def banzhaf(self):
        pass

    def shapley(self):
        pass

    def shapley_approx(self, m):
        pass


class FlowGame(Graph, Game):
    def __init__(self, A=None, G=None):
        super().__init__(A=A, G=G)
        self.A = A
        self.n = len(A)
        self.G = G

    def get_worth_of_coalition(self, A_ids=None):
        if A_ids:
            edges = {edge for agent_id in A_ids for edge in self.A[Agent(agent_id)]}
            G_sub = self.G.get_induced_graph_by_edges(E=edges)
            return G_sub.get_max_flow()
        else:
            return float(0)

    def compute_values_of_coalitions(self):
        pass

    def shapley_single_player_approx(self, m, i):
        players = list(range(1, self.n+1))
        shapley = float(0)
        for _ in range(m):
            perm = list(np.random.permutation(players))
            coalition = perm[:perm.index(i)+1]
            coalition_without_i = coalition[:-1]
            cur_marginal_contribution = self.get_worth_of_coalition(coalition) - self.get_worth_of_coalition(coalition_without_i)
            shapley += cur_marginal_contribution
        return shapley/m

    def shapley_approx(self, m):
        return [self.shapley_single_player_approx(m, i) for i in range(1, self.n+1)]




def prepare(args):
    input_file = open(args.input, "r")
    size_of_V, size_of_E, size_of_N = map(int, input_file.readline().split(" "))
    G = Graph()
    V = {id: Node(id) for id in range(size_of_V)}
    A = {Agent(id): set() for id in range(1, size_of_N + 1)}
    for i in range(size_of_E):
        tail_id, head_id, cap_e, agent_id = map(int, input_file.readline().split(" "))
        a = Agent(agent_id)
        u, v = V[tail_id], V[head_id]
        e = DirEdge(tail=u, head=v, l=0, u=cap_e)
        G.add_edge(e)
        A[a] |= {e}
    return A, G


def run(FG):
    m = factorial(FG.n) // 2
    print(" ".join(map(str, FG.shapley_approx(m))))



if __name__ == "__main__":
    # Parse arguments
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument("--input", default="input.io", type=str, help="Name of file to read.")
    parser.add_argument("--output", default="output.io", type=str, help="Name of file to write to.")
    args = parser.parse_args()

    A, G = prepare(args)
    FG = FlowGame(A, G)
    run(FG)
