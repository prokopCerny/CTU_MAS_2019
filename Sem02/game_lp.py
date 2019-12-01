# You can use your own implementation of game tree for testing.
# It should satisfy the original interfaces in game_tree.py
#
# The game tree implementation does not have to be the bandits game:
# you can use any game implementation, for example the simple poker.
# You have constructed the LP by hand at class, so you can check
# that for debugging.
#
# Note that the LP must support any EFG, especially including ones where
# the player makes multiple moves before it's opponent's turn.
#
# For automatic evaluation, testing version of game_tree will be imported.
# In  your solution, submit only this file, i.e. game_lp.py
from game_tree import *


# Following packages are supported:
# Solvers:
# import gurobipy # == 8.1
# import cvxopt # == 1.2.3
#
# Matrix manipulation:
# import numpy as np


# Do not print anything besides the final output in your submission.
# Implement the LP as a function of the game tree.
#
# You MUST SPECIFY the LP for each of the players separately!
# Do not use the dual, or zero-sum property.
# If you don't satisfy this, you will be heavily penalized.
#
# At the course webpage, we have calculated some testing game values for you.
# You can use them to check if your LP has been well specified.
import numpy as np
from cvxopt import matrix, solvers

class Node:
    def __init__(self, val: float, type: HistoryType, player: int, p1seq, p2seq, infoset: int):
        self.infoset = infoset
        self.p2seq = p2seq
        self.p1seq = p1seq
        self.player = player
        self.type = type
        self.val = val
        self.children = []


def transform_tree(root: History):
    p1seq_counter = -1
    p1seq_to_index = dict()
    p1info_counter = -1
    p1info_to_index = dict()
    p2seq_counter = -1
    p2seq_to_index = dict()
    p2info_counter = -1
    p2info_to_index = dict()

    def transform_tree_helper(root: History, chance: float, p1seq, p2seq):
        nonlocal p1seq_counter, p1info_counter, p2seq_counter, p2info_counter

        if p1seq not in p1seq_to_index:
            p1seq_counter += 1
            p1seq_to_index[p1seq] = p1seq_counter
        if p2seq not in p2seq_to_index:
            p2seq_counter += 1
            p2seq_to_index[p2seq] = p2seq_counter

        if root.type() == HistoryType.decision:
            infoset = root.infoset().index()
            if int(root.current_player()) == 0:
                if infoset not in p1info_to_index:
                    p1info_counter += 1
                    p1info_to_index[infoset] = p1info_counter
            else:
                if infoset not in p2info_to_index:
                    p2info_counter += 1
                    p2info_to_index[infoset] = p2info_counter
        else:
            infoset = -1

        value: float = float(chance if root.type() != HistoryType.terminal else chance * root.utility())
        cur_player = int(root.current_player()) if root.type() == HistoryType.decision else -1

        node = Node(value,
                    root.type(),
                    cur_player,
                    p1seq,
                    p2seq,
                    infoset)

        if root.type() == HistoryType.terminal:
            return node
        else:
            for action in root.actions():
                new_p1seq = p1seq if cur_player != 0 else p1seq + (f'{str(action)}{p1info_to_index[infoset]}',)
                new_p2seq = p2seq if cur_player != 1 else p2seq + (f'{str(action)}{p2info_to_index[infoset]}',)
                new_chance = value if root.type() != HistoryType.chance else value * root.chance_prob(action)
                node.children.append(transform_tree_helper(root.child(action), new_chance, new_p1seq, new_p2seq))
            return node

    new_root = transform_tree_helper(root, 1.0, ("",), ("",))
    return new_root, p1seq_to_index, p2seq_to_index, p1info_to_index, p2info_to_index


def create_matrices(root: Node, player: int, p_seq_to_index, op_info_to_index, p_info_to_index, op_seq_to_index):
    p_info_count = len(p_info_to_index)
    p_seq_count = len(p_seq_to_index)
    op_info_count = len(op_info_to_index)
    op_seq_count = len(op_seq_to_index)

    A_dimensions = (p_info_count+1, p_seq_count + op_info_count)
    b_len = p_info_count+1
    G1_dimensions = (2*(p_seq_count-1), p_seq_count + op_info_count)
    h1_len = 2*(p_seq_count-1)
    Gp_dimensions = (op_seq_count - 1, p_seq_count + op_info_count)
    hp_len = op_seq_count - 1
    c_len = p_seq_count + op_info_count

    op_info_shift = {info: index + p_seq_count for (info, index) in op_info_to_index.items()}
    op_seq_shift = {seq: index-1 for (seq, index) in op_seq_to_index.items()}

    A = np.zeros(A_dimensions)
    Gp = np.zeros(Gp_dimensions)
    c = np.zeros(c_len)

    def handle_opponent_child(node: Node, infoset: int):
        if node.type == HistoryType.terminal:
            p_seq = node.p1seq if player == 0 else node.p2seq
            op_seq = node.p1seq if player == 1 else node.p2seq
            Gp[op_seq_shift[op_seq], p_seq_to_index[p_seq]] = -node.val
            Gp[op_seq_shift[op_seq], op_info_shift[infoset]] = 1
        elif node.type == HistoryType.chance:
            for child in node.children:
                handle_opponent_child(child, infoset)
        else:
            if node.player != player:
                op_seq = node.p1seq if player == 1 else node.p2seq
                Gp[op_seq_shift[op_seq], op_info_shift[infoset]] = 1
                Gp[op_seq_shift[op_seq], op_info_shift[node.infoset]] = -1
            else:
                for child in node.children:
                    handle_opponent_child(child, infoset)

    def handle_node(node: Node):
        if node.player == player:
            cur_seq = node.p1seq if player == 0 else node.p2seq
            for child in node.children:
                child_seq = child.p1seq if player == 0 else child.p2seq
                A[p_info_to_index[node.infoset], p_seq_to_index[child_seq]] = 1
                A[p_info_to_index[node.infoset], p_seq_to_index[cur_seq]] = -1
        elif 0 <= node.player < 2:
            cur_seq = node.p1seq if node.player == 0 else node.p2seq
            if cur_seq == ("",):
                c[op_info_shift[node.infoset]] = -1
            for child in node.children:
                handle_opponent_child(child, node.infoset)

        for child in node.children:
            handle_node(child)

    handle_node(root)

    tmp = np.array([[1], [-1]])
    G1 = np.kron(np.eye(p_seq_count-1), tmp)+0
    G1 = np.c_[np.zeros(h1_len), G1, np.zeros((h1_len, op_info_count))]
    tmp = np.array([1, 0])
    h1 = np.kron(np.ones(p_seq_count-1), tmp)
    if player == 1:
        c = -c
        Gp = -Gp

    hp = np.zeros(hp_len)
    G = np.r_[G1, Gp]
    h = np.concatenate([h1, hp])

    A[-1, 0] = 1
    b = np.zeros(b_len)
    b[-1] = 1

    return A, b, G, h, c


def game_value(root: History, player: Player) -> float:
    """
    Create sequence-form LP from supplied EFG tree and solve it.

    Do not rely on any specifics of the original maze problem.
    Your LP should solve this problem for any EFG tree, if it is described
    by the original interface.

    The LP must be constructed for the given player:
    you should also compute the realization plan for that player.

    Return the expected utility in the root node for the player.
    So for the first player this will be the game value.

    :param root: root history of the EFG tree
    :param player: zero-indexed player: first player has index 0,
                   second player has index 1
    :return: expected value in the root for given player
    """
    new_root, p1seq_to_index, p2seq_to_index, p1info_to_index, p2info_to_index = transform_tree(root)
    p_sti = p1seq_to_index if int(player) == 0 else p2seq_to_index
    p_iti = p1info_to_index if int(player) == 0 else p2info_to_index
    op_sti = p1seq_to_index if int(player) == 1 else p2seq_to_index
    op_iti = p1info_to_index if int(player) == 1 else p2info_to_index
    A, b, G, h, c = create_matrices(new_root, player, p_sti, op_iti, p_iti, op_sti)
    Aopt, bopt, Gopt, hopt, copt = matrix(A), matrix(b), matrix(G), matrix(h), matrix(c)
    solvers.options['show_progress'] = False
    sol = solvers.lp(c=copt, G=Gopt, h=hopt, A=Aopt, b=bopt)
    x = np.squeeze(np.array(sol['x']))
    val = np.dot(c, x)
    return -val


########### Do not modify code below.

if __name__ == '__main__':
    # read input specification in the body of this function
    root_history = create_root()
    # additionally specify for which player it should be solved
    player = int(input())
    # player = 0

    print(game_value(root_history, player))
