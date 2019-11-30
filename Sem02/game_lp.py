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
  raise NotImplementedError


########### Do not modify code below.

if __name__ == '__main__':
  # read input specification in the body of this function
  root_history = create_root()
  # additionally specify for which player it should be solved
  player = int(input())

  print(game_value(root_history, player))
