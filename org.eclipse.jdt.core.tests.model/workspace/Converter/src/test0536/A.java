/*
 * Created on Jan 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package test0536;

import java.util.HashMap;
import java.util.LinkedList;

import SerSolver.SerBranchNode;
import SerSolver.SerChipExitNode;
import SerSolver.SerChipGoalNode;
import SerSolver.SerColor;
import SerSolver.SerGoalNode;
import SerSolver.SerNode;

import utilities.*;
import utilities.Move;
import utilities.NodeStatistics;

abstract public class A
{

	protected static final int STAGESIZE = 15;
	protected static int globalTargetDistance;
	protected static Move[] moves;
	static HashMap map;
	public static SerNode exitNode;
	static
	{
		globalTargetDistance = 10000;
		exitNode = null;
		moves = new Move[100];
		for (int i = 0; i < 100; i++)
		{
			moves[i] = new Move();
		}
		map = new HashMap(100000);
	}

	protected int chips;
	protected SerColor currentKey;
	protected int startIndex;
	protected int moveIndex;
	protected int targetDistance;
	protected LinkedList[] dependencies;
	protected A nextSolver;
	A(int _startIndex)
	{
		dependencies = new LinkedList[5];
		for (int i = 0; i < 5; i++)
			dependencies[i] = new LinkedList();
		startIndex = _startIndex;
	}
	/**
	 * @param moveIndex
	 * @param moves
	 * @param chips
	 * @param dependencies
	 * @return the best solution for the remainder of the board
	 */
	v
	public Solution subSolve(int _moveIndex, int _chips, LinkedList[] _dependencies)
	{
		assert (_moveIndex==startIndex);
		key.set(_dependencies);
		Solution solution = (Solution) map.get(key);
		if (solution == null)
		{
			chips = _chips;
			targetDistance = 10000;
			moveIndex = _moveIndex;
			currentKey = moves[startIndex - 1].node.getKey();
			for (int i = 1; i < 5; i++)
			{
				dependencies[i].clear();
				dependencies[i].addAll(_dependencies[i]);
			}

			solution = findSolution();
			Key saveKey = new Key(key);
			map.put(saveKey, solution);

		}
		else
		{
			solution.hits++;
		}
		return solution;
	}

	/**
	 * like the method in the main solution finder but it is
	 * only looking for the best end game
	 * @param chips
	 */
	abstract protected Solution findSolution();

	/**
	 * @return
	 */
	protected boolean pickNode(int index)
	{
		LinkedList list = dependencies[currentKey.getValue()];
		if (list.isEmpty())
			return false;
		else
		{
			SerNode node = (SerNode) list.get(index);
			Move lastMove = moves[moveIndex - 1];
			int distance = NodeStatistics.nodeDistance(lastMove.node, node);
			int cost = distance + lastMove.distance;
			if (cost > targetDistance)
				return false;
			list.remove(index);
			chooseNode(node, index, cost);
			return true;
		}

	}

	/**
	 * 
	 */
	protected int backoff()
	{
		Move move = moves[--moveIndex];
		if (move.node instanceof SerChipGoalNode)
		{
			chips--;
		}
		if (move.node instanceof SerBranchNode)
		{
			SerNode[] nodes = ((SerBranchNode) move.node).getDependentNodes();
			removeFromStack(nodes[0]);
			removeFromStack(nodes[1]);
		}
		else
			removeFromStack(((SerGoalNode) move.node).getDependentNode());
		currentKey = move.node.getDoor();
		boolean more = reputStack(move.node, move.indexOnStack);
		if (!more)
			return backoff();
		else
			return move.indexOnStack + 1;
	}

	/**
	 * @param node
	 */
	protected void chooseNode(SerNode node, int cameFrom, int distance)
	{
		//NOTE assume already own green key 
		//		if (node instanceof SerGreenKeyGoalNode)
		//		{
		//			sawGreen = true;
		//			drainGreenQueue();
		//		}
		if (node instanceof SerBranchNode)
		{
			SerBranchNode branch = (SerBranchNode) node;
			putStack(branch.getDependentNodes()[0]);
			putStack(branch.getDependentNodes()[1]);
		}
		else
		{
			putStack(((SerGoalNode) node).getDependentNode());
			if (node instanceof SerChipGoalNode)
			{
				chips++;
			}
		}

		currentKey = node.getKey();

		moves[moveIndex].indexOnStack = cameFrom;
		moves[moveIndex].node = node;
		moves[moveIndex].distance = distance;
		moveIndex++;
	}
	/**
	 * @param board
	 * @param node
	 */
	protected void putStack(SerNode node)
	{
		/* if the last node on a branch is a goal node it doesn't expose any other nodes
		 */
		if (node == null)
			return;
		else if (node instanceof SerChipExitNode)
			exitNode = node;
		//NOTE assume already own green key 
		//		else if (!sawGreen && node instanceof SerChipGoalNode)
		//			getDependencies(SerColor.GREEN_LITERAL).add(node);
		else
			dependencies[node.getDoor().getValue()].add(node);

	}
	/**
	 * @param node
	 */
	protected void removeFromStack(SerNode node)
	{
		if (node == null)
			return;
		if (node instanceof SerChipExitNode)
			return;

		LinkedList list = dependencies[node.getDoor().getValue()];
		if (!list.remove(node))
			throw new RuntimeException("door not available");
	}
	/**
	 * @param node
	 * @param i
	 * @return
	 */
	protected boolean reputStack(SerNode node, int i)
	{
		LinkedList list = dependencies[node.getDoor().getValue()];
		list.add(i, node);
		return list.size() > (i + 1);
	}
	protected void dumpMoves()
	{
		StringBuffer output = new StringBuffer(3000);
		SerNode oldNode = moves[0].node;
		int testDistance = 2;
		for (int i = 1; i < moveIndex; i++)
		{
			SerNode node = moves[i].node;
			int distance = moves[i].distance;
			testDistance += NodeStatistics.nodeDistance(oldNode,node);
			assert(testDistance==distance);
			output.append(
				i
					+ "("
					+ node.getColumn()
					+ ","
					+ node.getRow()
					+ ") "
					+ (2 * distance - 1)
					+ "\n");
			oldNode = node;
		}
		System.out.println(output);
	
	}
}Content-Type: text/plain

/*
 * Created on Jan 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package test0536;

import java.util.HashMap;
import java.util.LinkedList;

import SerSolver.SerBranchNode;
import SerSolver.SerChipExitNode;
import SerSolver.SerChipGoalNode;
import SerSolver.SerColor;
import SerSolver.SerGoalNode;
import SerSolver.SerNode;

import utilities.*;
import utilities.Move;
import utilities.NodeStatistics;

abstract public class A
{

	protected static final int STAGESIZE = 15;
	protected static int globalTargetDistance;
	protected static Move[] moves;
	static HashMap map;
	public static SerNode exitNode;
	static
	{
		globalTargetDistance = 10000;
		exitNode = null;
		moves = new Move[100];
		for (int i = 0; i < 100; i++)
		{
			moves[i] = new Move();
		}
		map = new HashMap(100000);
	}

	protected int chips;
	protected SerColor currentKey;
	protected int startIndex;
	protected int moveIndex;
	protected int targetDistance;
	protected LinkedList[] dependencies;
	protected A nextSolver;
	A(int _startIndex)
	{
		dependencies = new LinkedList[5];
		for (int i = 0; i < 5; i++)
			dependencies[i] = new LinkedList();
		startIndex = _startIndex;
	}
	/**
	 * @param moveIndex
	 * @param moves
	 * @param chips
	 * @param dependencies
	 * @return the best solution for the remainder of the board
	 */
	v
	public Solution subSolve(int _moveIndex, int _chips, LinkedList[] _dependencies)
	{
		assert (_moveIndex==startIndex);
		key.set(_dependencies);
		Solution solution = (Solution) map.get(key);
		if (solution == null)
		{
			chips = _chips;
			targetDistance = 10000;
			moveIndex = _moveIndex;
			currentKey = moves[startIndex - 1].node.getKey();
			for (int i = 1; i < 5; i++)
			{
				dependencies[i].clear();
				dependencies[i].addAll(_dependencies[i]);
			}

			solution = findSolution();
			Key saveKey = new Key(key);
			map.put(saveKey, solution);

		}
		else
		{
			solution.hits++;
		}
		return solution;
	}

	/**
	 * like the method in the main solution finder but it is
	 * only looking for the best end game
	 * @param chips
	 */
	abstract protected Solution findSolution();

	/**
	 * @return
	 */
	protected boolean pickNode(int index)
	{
		LinkedList list = dependencies[currentKey.getValue()];
		if (list.isEmpty())
			return false;
		else
		{
			SerNode node = (SerNode) list.get(index);
			Move lastMove = moves[moveIndex - 1];
			int distance = NodeStatistics.nodeDistance(lastMove.node, node);
			int cost = distance + lastMove.distance;
			if (cost > targetDistance)
				return false;
			list.remove(index);
			chooseNode(node, index, cost);
			return true;
		}

	}

	/**
	 * 
	 */
	protected int backoff()
	{
		Move move = moves[--moveIndex];
		if (move.node instanceof SerChipGoalNode)
		{
			chips--;
		}
		if (move.node instanceof SerBranchNode)
		{
			SerNode[] nodes = ((SerBranchNode) move.node).getDependentNodes();
			removeFromStack(nodes[0]);
			removeFromStack(nodes[1]);
		}
		else
			removeFromStack(((SerGoalNode) move.node).getDependentNode());
		currentKey = move.node.getDoor();
		boolean more = reputStack(move.node, move.indexOnStack);
		if (!more)
			return backoff();
		else
			return move.indexOnStack + 1;
	}

	/**
	 * @param node
	 */
	protected void chooseNode(SerNode node, int cameFrom, int distance)
	{
		//NOTE assume already own green key 
		//		if (node instanceof SerGreenKeyGoalNode)
		//		{
		//			sawGreen = true;
		//			drainGreenQueue();
		//		}
		if (node instanceof SerBranchNode)
		{
			SerBranchNode branch = (SerBranchNode) node;
			putStack(branch.getDependentNodes()[0]);
			putStack(branch.getDependentNodes()[1]);
		}
		else
		{
			putStack(((SerGoalNode) node).getDependentNode());
			if (node instanceof SerChipGoalNode)
			{
				chips++;
			}
		}

		currentKey = node.getKey();

		moves[moveIndex].indexOnStack = cameFrom;
		moves[moveIndex].node = node;
		moves[moveIndex].distance = distance;
		moveIndex++;
	}
	/**
	 * @param board
	 * @param node
	 */
	protected void putStack(SerNode node)
	{
		/* if the last node on a branch is a goal node it doesn't expose any other nodes
		 */
		if (node == null)
			return;
		else if (node instanceof SerChipExitNode)
			exitNode = node;
		//NOTE assume already own green key 
		//		else if (!sawGreen && node instanceof SerChipGoalNode)
		//			getDependencies(SerColor.GREEN_LITERAL).add(node);
		else
			dependencies[node.getDoor().getValue()].add(node);

	}
	/**
	 * @param node
	 */
	protected void removeFromStack(SerNode node)
	{
		if (node == null)
			return;
		if (node instanceof SerChipExitNode)
			return;

		LinkedList list = dependencies[node.getDoor().getValue()];
		if (!list.remove(node))
			throw new RuntimeException("door not available");
	}
	/**
	 * @param node
	 * @param i
	 * @return
	 */
	protected boolean reputStack(SerNode node, int i)
	{
		LinkedList list = dependencies[node.getDoor().getValue()];
		list.add(i, node);
		return list.size() > (i + 1);
	}
	protected void dumpMoves()
	{
		StringBuffer output = new StringBuffer(3000);
		SerNode oldNode = moves[0].node;
		int testDistance = 2;
		for (int i = 1; i < moveIndex; i++)
		{
			SerNode node = moves[i].node;
			int distance = moves[i].distance;
			testDistance += NodeStatistics.nodeDistance(oldNode,node);
			assert(testDistance==distance);
			output.append(
				i
					+ "("
					+ node.getColumn()
					+ ","
					+ node.getRow()
					+ ") "
					+ (2 * distance - 1)
					+ "\n");
			oldNode = node;
		}
		System.out.println(output);
	
	}
}