from enum import IntEnum
from typing import List
from itertools import combinations
from copy import deepcopy


# Do not print anything besides the tree in your submission.
# Implement all methods, the __str__ methods are optional (for nice labels).
# However if you wish, you can completely change structure of the code.
# What we care about is that the tree is exported in valid format.

class HistoryType(IntEnum):
    decision = 1
    chance = 2
    terminal = 3


class Player(IntEnum):
    agent = 0
    bandit = 1


class ActionType(IntEnum):
    placeBandits = 0
    moveBandit = 1
    nothing = 2
    goAgent = 3
    killAgent = 4
    survive = 5


class Action:
    def __init__(self, type: ActionType, data=None):
        self.data = data
        self.type = type

    def __str__(self):
        return f'{self.type.name}' + "" if self.data is None else f'{self.data}'  # action label


class Infoset:
    def index(self) -> int:
        raise NotImplementedError

    def __str__(self):
        raise NotImplementedError


class History:
    def __init__(self, obstacles, dangerous, golds, playerPos, destination, banditCount, P_attack_success):
        self.unusedBanditCount = banditCount
        self.P_attack_success = P_attack_success
        self.destination = destination
        self.playerPos = playerPos
        self.golds = golds
        self.dangerous = dangerous
        self.obstacles = obstacles
        self.bandits = set()
        self.player_known_bandits = set()
        self.alarm = False
        self.pickedGolds = 0
        self.player = Player.bandit
        self.type = HistoryType.decision
        self.visited = {playerPos}

    def type(self) -> HistoryType:
        return self.type

    def clone(self) -> 'History':
        return deepcopy(self)

    def current_player(self) -> Player:
        return self.player

    # infoset index: histories with the same infoset index belong to the same infoset
    def infoset(self) -> Infoset:
        raise NotImplementedError

    def getValidNeighbors(self, fromPos):
        dirs = [(1, 0), (0, -1), (0, 1), (-1, 0)]
        neighs = [tuple(map(sum, zip(fromPos, d))) for d in dirs]
        return [n for n in neighs if n not in self.obstacles]

    def actions(self) -> List[Action]:
        if self.player == Player.bandit:
            if self.unusedBanditCount > 0:
                bandit_placements = combinations(self.dangerous, self.unusedBanditCount)
                return [Action(ActionType.placeBandits, bandit_placement) for bandit_placement in bandit_placements]
            else:
                actions = [Action(ActionType.nothing)]
                freePositions = [place for place in self.dangerous if
                                 place not in self.bandits and place != self.playerPos]
                actions.extend([Action(ActionType.moveBandit, data=(bandit, pos)) for bandit in self.bandits for pos in
                                freePositions])
                return actions
        elif self.player == Player.agent:
            neighs = self.getValidNeighbors(self.playerPos)
            return [Action(ActionType.goAgent, place) for place in neighs]
        else:
            return [Action(ActionType.killAgent), Action(ActionType.survive)]

    # for player 1
    def utility(self) -> float:
        raise NotImplementedError

    def chance_prob(self, action: Action) -> float:
        if action.type == ActionType.killAgent:
            return self.P_attack_success
        else:
            return 1 - self.P_attack_success

    def child(self, action: Action) -> 'History':
        new_hist = self.clone()
        if self.player == Player.bandit:
            if action.type == ActionType.placeBandits:
                new_hist.bandits.update(action.data)
                new_hist.unusedBanditCount = 0
                new_hist.player = Player.agent
                return new_hist
            elif action.type == ActionType.moveBandit:
                b_from, b_to = action.data
                new_hist.bandits.remove(b_from)
                new_hist.bandits.add(b_to)
                new_hist.alarm = True
                new_hist.player = Player.agent
                return new_hist
            elif action == ActionType.nothing:
                new_hist.player = Player.agent
                return new_hist
            else:
                raise NotImplementedError("BAD ACTIONTYPE FOR BANDIT")
        elif self.player == Player.agent:
            pass
        else:
            pass

    def __str__(self):
        return ""  # history label


# read the maze from input and return the root node
def create_root() -> History:
    rows = int(input())
    cols = int(input())
    obstacles = set()
    player = None
    dangerous = set()
    golds = set()
    destination = None
    for row in range(rows):
        line = input()
        for col, type in enumerate(line):
            if type == '#':
                obstacles.add((row, col))
            elif type == 'E':
                dangerous.add((row, col))
            elif type == 'D':
                destination = (row, col)
            elif type == 'S':
                player = (row, col)
            elif type == 'G':
                golds.add((row, col))
    bandits = int(input())
    P_attack_success = float(int())


## Following is an example implementation of the game of simple poker.
#
# from copy import deepcopy
#
# class HistoryType(IntEnum):
#   decision = 1
#   chance = 2
#   terminal = 3
#
#
# class Player(IntEnum):
#   first = 0
#   second = 1
#   chance = 2
#   terminal = 3
#
#
# class Action(IntEnum):
#   CardsJJ = 0
#   CardsJQ = 1
#   CardsQJ = 2
#   CardsQQ = 3
#   Fold = 4
#   Bet = 5
#   Call = 6
#
#   def __str__(self):
#     return self.name  # action label
#
#
# def action_to_cards(action):
#   return {
#     Action.CardsJJ: ["J", "J"],
#     Action.CardsJQ: ["J", "Q"],
#     Action.CardsQJ: ["Q", "J"],
#     Action.CardsQQ: ["Q", "Q"],
#   }[action]
#
#
# class Infoset:
#   def __init__(self, card: str, player: int):
#     self.card = card
#     self.player = player
#
#   def index(self) -> int:
#     return ord(self.card) * (self.player+1)
#
#   def __str__(self):
#     return self.card
#
#
# class History:
#   def __init__(self):
#     self.player = Player.chance
#     self.player_cards = []
#     self.action_history = []
#
#   def type(self) -> HistoryType:
#     if self.player == Player.chance:
#       return HistoryType.chance
#     elif self.player == Player.terminal:
#       return HistoryType.terminal
#     else:
#       return HistoryType.decision
#
#   def current_player(self) -> Player:
#     return self.player
#
#   # infoset index: histories with the same infoset index belong to the same infoset
#   def infoset(self) -> Infoset:
#     return Infoset(self.player_cards[self.player], self.player)
#
#   def actions(self) -> List[Action]:
#     if self.player == Player.chance:
#       return [Action.CardsJJ, Action.CardsJQ, Action.CardsQJ, Action.CardsQQ]
#     if self.player == Player.first:
#       return [Action.Fold, Action.Bet]
#     if self.player == Player.second:
#       return [Action.Fold, Action.Call]
#
#   # for player 1
#   def utility(self) -> float:
#     if self.action_history[1] == Action.Fold:
#       return -1
#     if self.action_history[2] == Action.Fold:
#       return 1
#     # otherwise it was bet followed by call:
#     if self.action_history[0] in [Action.CardsJJ, Action.CardsQQ]:
#       return 0
#     if self.action_history[0] == Action.CardsJQ:
#       return -3
#     if self.action_history[0] == Action.CardsQJ:
#       return 3
#
#   def chance_prob(self, action: Action) -> float:
#     if action in [Action.CardsJJ, Action.CardsQQ]:
#       return 1 / 6.
#     else:
#       return 1 / 3.
#
#   def child(self, action: Action) -> 'History':
#     next = self.clone()
#     next.action_history.append(action)
#
#     if self.player == Player.chance:
#       next.player = Player.first
#       next.player_cards = action_to_cards(action)
#     elif self.player == Player.first:
#       next.player = Player.second
#     elif self.player == Player.second:
#       next.player = Player.terminal
#
#     if action == Action.Fold:
#       next.player = Player.terminal
#
#     return next
#
#   def clone(self) -> 'History':
#     return deepcopy(self)
#
#   def __str__(self):
#     return ""  # history label
#
#
# def create_root() -> History:
#   return History()


########### Do not modify code below.

def export_gambit(root_history: History) -> str:
    players = ' '.join([f"\"Pl{i}\"" for i in range(2)])
    ret = f"EFG 2 R \"\" {{ {players} }} \n"

    terminal_idx = 1
    chance_idx = 1

    def build_tree(history, depth):
        nonlocal ret, terminal_idx, chance_idx

        ret += " " * depth  # add nice spacing

        if history.type() == HistoryType.terminal:
            util = history.utility()
            ret += f"t \"{history}\" {terminal_idx} \"\" "
            ret += f"{{ {util}, {-util} }}\n"
            terminal_idx += 1
            return

        if history.type() == HistoryType.chance:
            ret += f"c \"{history}\" {chance_idx} \"\" {{ "
            ret += " ".join([f"\"{str(action)}\" {history.chance_prob(action):.3f}"
                             for action in history.actions()])
            ret += " } 0\n"
            chance_idx += 1

        else:  # player node
            player = int(history.current_player()) + 1  # cannot be indexed from 0
            infoset = history.infoset()
            ret += f"p \"{history}\" {player} {infoset.index()} \"\" {{ "
            ret += " ".join([f"\"{str(action)}\"" for action in history.actions()])
            ret += " } 0\n"

        for action in history.actions():
            child = history.child(action)
            build_tree(child, depth + 1)

    build_tree(root_history, 0)
    return ret


if __name__ == '__main__':
    print(export_gambit(create_root()))
