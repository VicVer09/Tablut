package student_player;

import java.util.ArrayList;
import java.util.HashSet; 

import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class MyTools {

	final static int initial_white = 9;
	final static int initial_black = 16;
	static int[] max_branch_factors = {10, 20, 40, 100 };
	
	// Evaluation Parameters:
	static int white_kill_parameter = 20; // 400 //10
	final static int black_kill_parameter = 10; // 400 //10
	final static int diag_parameter = 10; // 250 //5
	final static int king_corner_parameter = 5; //222 //5
	final static int check_parameter = 200; // 10009 //100
	final static int king_attacked_parameter = 25; //500 //10
	final static int guillotine_weight = 50; //5555 //80
	final static int king_block_parameter = 150; // 2777 //50
	final static int triangle_weight = 75; //4777 //70
	final static int repeat_move_penalty = 10; //400 //10
	final static int neighbour_penalty = 2;

	public static int alphaBeta(TablutBoardState board_state, int side, int depth, int alpha, int beta, int max_depth) {

		/* WEIGHTS HASHSETS */
		// These are initialized to values based on the current board state and
		// passed into our heuristic
		// With each move a large portion of evaluation will be given by value
		// of pieces on weighted squares
		ArrayList<HashSet<WeightedCoord>> weights = getWeightHashsets(board_state);

		// 01 Terminal node
		// if (depth == 0) return heuristic (board_state, weights);

		if (side == 1) {

			/*
			 * 
			 * WHITE LOGIC BEGINS
			 * 
			 */

			// Initialize value
			int value = Integer.MIN_VALUE;
			int i = 0; // loop var

			// Select children
			// Now we must select the best branch_factor number of moves and
			// perform alpha beta pruning
			ArrayList<TablutMove> legal_moves = board_state.getAllLegalMoves();
			int branch_factor = 0;

			// Idea: select all king moves + n best other moves
			for (i = 0; i < legal_moves.size(); i++) {
				if (legal_moves.get(i).getPlayerID() != side) {
					legal_moves.remove(i);
					i--; // Since elements are shifted down
				}
			}
			branch_factor = legal_moves.size();

			int[][] best_moves = new int[branch_factor][2];
			int evaluation;

			for (i = 0; i < branch_factor; i++) {

				TablutBoardState child_board_state = (TablutBoardState) board_state.clone();
				child_board_state.processMove(legal_moves.get(i));

				evaluation = heuristic(child_board_state, weights);
				
				
				if (evaluation == Integer.MAX_VALUE) {

					if (depth == max_depth)
						return i;
					else
						return Integer.MAX_VALUE;

				}
				if (depth == 0)
					return evaluation;
				
				insertInOrderDescending(i, evaluation, best_moves);

			}

			// We can only afford to check the top moves
			// This is important for late game where we could have a huge branch
			// factor
			branch_factor = min(branch_factor, max_branch_factors[depth]);

			int best_move_id = best_moves[0][0]; // by default choose the most
													// greedy move
			for (i = 0; i < branch_factor; i++) {

				// Simulate move and call alphaBeta recursively
				TablutBoardState child_board_state = (TablutBoardState) board_state.clone();
				child_board_state.processMove(legal_moves.get(best_moves[i][0]));
				if (child_board_state.getWinner() == 1)
					value = Integer.MAX_VALUE;
				else if (child_board_state.getWinner() == 0)
					value = Integer.MIN_VALUE;
				else
					value = alphaBeta(child_board_state, flipSide(side), depth - 1, alpha, beta, max_depth);

				// Update alpha if necessary
				if (value > alpha) {
					best_move_id = best_moves[i][0]; // Keep track of best move
					alpha = value;
				}

				// Prune if necessary
				if (beta <= alpha) {
					break;
				}

			}

			// At the final alphaBeta call we do a little trick, instead of
			// returning the value
			// we instantly return the id for our move
			if (depth == max_depth) {
				return best_move_id;
			} else {
				return value;
			}

		} else { // we are black, ie minimizing

			/*
			 * 
			 * BLACK LOGIC BEGINS
			 * 
			 */

			// Initialize value;
			int value = Integer.MAX_VALUE;
			int i; // loop var

			// 14 prep
			// Now we must select the best branch_factor number of moves and
			// perform alpha beta pruning
			ArrayList<TablutMove> legal_moves = board_state.getAllLegalMoves();
			int branch_factor = 0;

			for (i = 0; i < legal_moves.size(); i++) {
				if (legal_moves.get(i).getPlayerID() != side) {
					legal_moves.remove(i);
					i--; // Since elements are shifted down
				}
			}

			branch_factor = legal_moves.size();
			if (branch_factor == 0)
				return value;

			int[][] best_moves = new int[branch_factor][2];
			int evaluation;

			for (i = 0; i < branch_factor; i++) {

				// Turn this into one liner before submission
				TablutBoardState child_board_state = (TablutBoardState) board_state.clone();
				child_board_state.processMove(legal_moves.get(i));

				evaluation = heuristic(child_board_state, weights);

				
				if (evaluation == Integer.MIN_VALUE) {

					if (depth == max_depth)
						return i;
					else
						return Integer.MIN_VALUE;

				}
				
				if (depth == 0)
					return evaluation;

				insertInOrderAscending(i, evaluation, best_moves);

			}

			// 14 for each child of node
			branch_factor = min(branch_factor, max_branch_factors[depth]);
			int best_move_id = best_moves[branch_factor - 1][0]; 
			for (i = 0; i < branch_factor; i++) {

				// Simulate move and call alphaBeta recursively
				TablutBoardState child_board_state = (TablutBoardState) board_state.clone();
				child_board_state.processMove(legal_moves.get(best_moves[i][0]));
				if (child_board_state.getWinner() == 1)
					value = Integer.MAX_VALUE;
				else if (child_board_state.getWinner() == 0)
					value = Integer.MIN_VALUE;
				else
					value = alphaBeta(child_board_state, flipSide(side), depth - 1, alpha, beta, max_depth);

				// Update beta if necessary
				// beta = min(beta, value);
				if (value < beta) {
					best_move_id = best_moves[i][0];
					// Same thing here with global best move
					// if (depth == max_depth) best_move_so_far = best_move_id;
					beta = value;
				}

				// Prune if necessary
				if (beta <= alpha) {

					// System.out.println("PRUNED in black");
					break;
				}

			}

			// At the final alphaBeta call we do a little trick, instead of
			// returning the value
			// we instantly return the id for our move
			if (depth == max_depth) {
				return best_move_id;
			} else
				return value;

		}
	}

	public static int heuristic(TablutBoardState board_state, ArrayList<HashSet<WeightedCoord>> weights) {

		// Check if game is over
		int winner = board_state.getWinner();
		if (winner == 1)
			return Integer.MAX_VALUE; // white wins
		else if (winner == 0)
			return Integer.MIN_VALUE; // black wins

		HashSet<WeightedCoord> black_weights = weights.get(0);
		HashSet<WeightedCoord> white_weights = weights.get(1);
		HashSet<WeightedCoord> king_weights = weights.get(2);

		/*
		 * 
		 * INITIALIZE EVALUATION
		 * 
		 */

		int eval = 0;

		// Get position of king
		Coord king = board_state.getKingPosition();
		int side = board_state.getTurnPlayer();

		/* PIECE HASHSETS */
		HashSet<Coord> white_piece_hashset;
		HashSet<Coord> black_piece_hashset;

		if (side == 1) {
			white_piece_hashset = board_state.getPlayerPieceCoordinates();
			black_piece_hashset = board_state.getOpponentPieceCoordinates();
		} else {
			white_piece_hashset = board_state.getOpponentPieceCoordinates();
			black_piece_hashset = board_state.getPlayerPieceCoordinates();
		}
		white_piece_hashset.remove(king);

		/*
		 * 
		 * KILLS EVAL
		 * 
		 */

		// Check for kill difference
		int black_pieces = board_state.getNumberPlayerPieces(0);
		int white_pieces = board_state.getNumberPlayerPieces(1);

		// white kills - black kills
		eval += (initial_black - black_pieces) * white_kill_parameter;
		eval -= (initial_white - white_pieces) * black_kill_parameter;

		/*
		 * 
		 * KING DISTANCE EVAL
		 * 
		 */

		// Evaluate king position
		if (Coordinates.distanceToClosestCorner(king) == 0)
			return Integer.MAX_VALUE;
		else
			eval += (8 - Coordinates.distanceToClosestCorner(king)) * king_corner_parameter;
		
		for (Coord king_neighbour : Coordinates.getNeighbors(king)) {
			if (black_piece_hashset.contains(king_neighbour)) eval -= king_attacked_parameter;
		}

		/*
		 * 
		 * WEIGHT EVAL
		 * 
		 */
		for (WeightedCoord white_weight : white_weights) {
			if (white_piece_hashset.contains(Coordinates.get(white_weight.x, white_weight.y))) {
				eval += white_weight.weight; // White maximizes
			}
		}

		for (WeightedCoord black_weight : black_weights) {
			if (black_piece_hashset.contains(Coordinates.get(black_weight.x, black_weight.y))) {
				eval -= black_weight.weight; // Black minimizes
			}
		}

		for (WeightedCoord king_weight : king_weights) {
			if (king.x == king_weight.x && king.y == king_weight.y) {
				eval += king_weight.weight; // King is white therefore also maximizes
				break;
			}
		}

		/*
		 * 
		 * EVALUATION COMPLETE
		 * 
		 */

		return eval;

	}

	// Initialize Weight Hashsets
	public static ArrayList<HashSet<WeightedCoord>> getWeightHashsets(TablutBoardState board_state) {

		/* INITIALIZE WEIGHTS */
		HashSet<WeightedCoord> black_weights = new HashSet<WeightedCoord>();
		HashSet<WeightedCoord> white_weights = new HashSet<WeightedCoord>();
		HashSet<WeightedCoord> king_weights = new HashSet<WeightedCoord>();
		ArrayList<HashSet<WeightedCoord>> weights = new ArrayList<HashSet<WeightedCoord>>();
		weights.add(black_weights);
		weights.add(white_weights);
		weights.add(king_weights);

		/* GET BOARD PROPERTIES */
		Coord king = board_state.getKingPosition();
		int i, j; // loop vars
		int side = board_state.getTurnPlayer();

		/* GET PIECE HASHSETS */
		HashSet<Coord> white_piece_hashset;
		HashSet<Coord> black_piece_hashset;

		if (side == 1) {
			white_piece_hashset = board_state.getPlayerPieceCoordinates();
			black_piece_hashset = board_state.getOpponentPieceCoordinates();
		} else {
			white_piece_hashset = board_state.getOpponentPieceCoordinates();
			black_piece_hashset = board_state.getPlayerPieceCoordinates();
		}

		// King is considered as a separate piece with its own weights
		white_piece_hashset.remove(king);

		
		/*
		 * 
		 * HOTSPOTS
		 * 
		 * */
		 
		black_weights.add(WeightsInitializer.Diagonal[2][2]);
		black_weights.add(WeightsInitializer.Diagonal[6][2]);
		black_weights.add(WeightsInitializer.Diagonal[2][6]);
		black_weights.add(WeightsInitializer.Diagonal[6][6]);

		black_weights.add(new WeightedCoord(5,4,-king_attacked_parameter));
		black_weights.add(new WeightedCoord(4,5,-king_attacked_parameter));
		black_weights.add(new WeightedCoord(3,4,-king_attacked_parameter));
		black_weights.add(new WeightedCoord(4,3,-king_attacked_parameter)); 
		 
		black_weights.add(WeightsInitializer.Diagonal[2][2]);
		black_weights.add(WeightsInitializer.Diagonal[6][2]);
		black_weights.add(WeightsInitializer.Diagonal[2][6]);
		black_weights.add(WeightsInitializer.Diagonal[6][6]);
		
		/*
		 * 
		 * DIAGONAlIZE
		 * 
		 * */
		
		// Determine quadrant to favour diagonalization in the quadrant the king is in
		boolean king_top_left = king.x < 5 && king.y < 5;
		boolean king_top_right = king.x < 5 && king.y > 3;
		boolean king_bottom_left = king.x > 3 && king.y < 5;
		boolean king_bottom_right = king.x > 3 && king.y > 3;
		for (i = 0; i <= king.x; i++){ 
			for (j = 0; j <= king.y; j++) { 

				if (i+j > 1 && i+j < 7) {

					// TOP LEFT CORNER
					if (black_piece_hashset.contains(Coordinates.get(i, j))) {
						if (i > 0 && j < 4) {
							black_weights.add(WeightsInitializer.Diagonal[ i-1 ][ j+1 ]);
							if (king_top_left) { // Triple the weights if the king is in that quadrant
								black_weights.add(WeightsInitializer.Diagonal[ i-1 ][ j+1 ]);
								black_weights.add(WeightsInitializer.Diagonal[ i-1 ][ j+1 ]);
								black_weights.add(WeightsInitializer.Diagonal[ i-1 ][ j+1 ]);
							}
						}
						if (i < 4 && j > 0) {
							black_weights.add(WeightsInitializer.Diagonal[ i+1 ][ j-1 ]);
							if (king_top_left) { // Triple the weights if the king is in that quadrant
								black_weights.add(WeightsInitializer.Diagonal[ i+1 ][ j-1 ]);
								black_weights.add(WeightsInitializer.Diagonal[ i+1 ][ j-1 ]);
								black_weights.add(WeightsInitializer.Diagonal[ i+1 ][ j-1 ]);
							}
						} 
					}

					// TOP RIGHT CORNER
					if (black_piece_hashset.contains(Coordinates.get(i, 8-j))) {
						if (i > 0 && j < 4) {
							black_weights.add(WeightsInitializer.Diagonal[ i-1 ][ 8-(j+1) ]);
							if (king_top_right) { // Triple the weights if the king is in that quadrant
								black_weights.add(WeightsInitializer.Diagonal[ i-1 ][ 8-(j+1) ]);
								black_weights.add(WeightsInitializer.Diagonal[ i-1 ][ 8-(j+1) ]);
								black_weights.add(WeightsInitializer.Diagonal[ i-1 ][ 8-(j+1) ]);
							}
						}
						if (i < 4 && j > 0) {
							black_weights.add(WeightsInitializer.Diagonal[ i+1 ][ 8-(j-1) ]);
							if (king_top_right) { // Triple the weights if the king is in that quadrant
								black_weights.add(WeightsInitializer.Diagonal[ i+1 ][ 8-(j-1) ]);
								black_weights.add(WeightsInitializer.Diagonal[ i+1 ][ 8-(j-1) ]);
								black_weights.add(WeightsInitializer.Diagonal[ i+1 ][ 8-(j-1) ]);
							}
						} 
					}

					// BOTTOM LEFT CORNER
					if (black_piece_hashset.contains(Coordinates.get(8-i, j))) {
						if (i > 0 && j < 4) {
							black_weights.add(WeightsInitializer.Diagonal[ 8-(i-1) ][ j+1 ]);
							if (king_bottom_left) { // Triple the weights if the king is in that quadrant
								black_weights.add(WeightsInitializer.Diagonal[ 8-(i-1) ][ j+1 ]);
								black_weights.add(WeightsInitializer.Diagonal[ 8-(i-1) ][ j+1 ]);
								black_weights.add(WeightsInitializer.Diagonal[ 8-(i-1) ][ j+1 ]);
							}
						}
						if (i < 4 && j > 0) {
							black_weights.add(WeightsInitializer.Diagonal[ 8-(i+1) ][ j-1 ]);
							if (king_bottom_left) { // Triple the weights if the king is in that quadrant
								black_weights.add(WeightsInitializer.Diagonal[ 8-(i+1) ][ j-1 ]);
								black_weights.add(WeightsInitializer.Diagonal[ 8-(i+1) ][ j-1 ]);
								black_weights.add(WeightsInitializer.Diagonal[ 8-(i+1) ][ j-1 ]);
							}
						} 
					}

					// BOTTOM RIGHT CORNER
					if (black_piece_hashset.contains(Coordinates.get(8-i, 8-j))) {
						if (i > 0 && j < 4) {
							black_weights.add(WeightsInitializer.Diagonal[ 8-(i-1) ][ 8-(j+1) ]);
							if (king_bottom_right) { // Triple the weights if the king is in that quadrant
								black_weights.add(WeightsInitializer.Diagonal[ 8-(i-1) ][ 8-(j+1) ]);
								black_weights.add(WeightsInitializer.Diagonal[ 8-(i-1) ][ 8-(j+1) ]);
							}
						}
						if (i < 4 && j > 0) {
							black_weights.add(WeightsInitializer.Diagonal[ 8-(i+1) ][ 8-(j-1) ]);
							if (king_bottom_right) { // Triple the weights if the king is in that quadrant
								black_weights.add(WeightsInitializer.Diagonal[ 8-(i+1) ][ 8-(j-1) ]);
								black_weights.add(WeightsInitializer.Diagonal[ 8-(i+1) ][ 8-(j-1) ]);
							}
						} 
					}


				}

			}
		}
		
		// NEIGHBOUR PENALTY
		for (Coord black_piece: black_piece_hashset) {
			for (Coord piece_neighbour: Coordinates.getNeighbors(black_piece)) {
				
				black_weights.add(WeightsInitializer.NeighbourPenalty[piece_neighbour.x][piece_neighbour.y]);
				
			}
		}
		for (Coord white_piece: white_piece_hashset) {
			for (Coord piece_neighbour: Coordinates.getNeighbors(white_piece)) {
				white_weights.add(WeightsInitializer.NeighbourPenalty[piece_neighbour.x][piece_neighbour.y]);
				king_weights.add(WeightsInitializer.NeighbourPenalty[piece_neighbour.x][piece_neighbour.y]);
			}
		}
		

		/*
		 * 
		 * CHECK DETECTION
		 * 
		 */

		// Block king check - highest priority
		// Could be rewritten with coordIsEmpty and get CoordsBetween
		boolean check = false;;
		if (king.x == 0) { // Horizontal check

			boolean left_blocked = false;
			boolean right_blocked = false;

			for (i = 1; i < 8; i++) {
				if (black_piece_hashset.contains(Coordinates.get(king.x, i))
						|| white_piece_hashset.contains(Coordinates.get(king.x, i))) {
					if (i > king.y)
						right_blocked = true;
					else
						left_blocked = true;
				}
			}

			if (!left_blocked) {
				check = true;
				if (king.y == 1 || king.y == 3)
					black_weights.add(WeightsInitializer.TopCheck[2]);
				else if (king.y == 2)
					black_weights.add(WeightsInitializer.TopCheck[1]);
				else
					for (i = 2; i < king.y; i++) {
						black_weights.add(WeightsInitializer.TopCheck[i]);
					}
			}

			if (!right_blocked) {
				check = true;
				if (king.y == 7 || king.y == 5)
					black_weights.add(WeightsInitializer.TopCheck[6]);
				else if (king.y == 6)
					black_weights.add(WeightsInitializer.TopCheck[7]);
				else
					for (i = 6; i > king.y; i--) {
						black_weights.add(WeightsInitializer.TopCheck[i]);
					}
			}

		} else if (king.x == 8) { // Horizontal check

			boolean left_blocked = false;
			boolean right_blocked = false;

			for (i = 1; i < 8; i++) {
				if (black_piece_hashset.contains(Coordinates.get(king.x, i))
						|| white_piece_hashset.contains(Coordinates.get(king.x, i))) {
					if (i > king.y)
						right_blocked = true;
					else
						left_blocked = true;
				}
			}

			if (!left_blocked) {
				check = true;
				if (king.y == 1 || king.y == 3)
					black_weights.add(WeightsInitializer.BottomCheck[2]);
				else if (king.y == 2)
					black_weights.add(WeightsInitializer.BottomCheck[1]);
				else
					for (i = 2; i < king.y; i++) {
						black_weights.add(WeightsInitializer.BottomCheck[i]);
					}
			}

			if (!right_blocked) {
				check = true;
				if (king.y == 7 || king.y == 5)
					black_weights.add(WeightsInitializer.BottomCheck[6]);
				else if (king.y == 6)
					black_weights.add(WeightsInitializer.BottomCheck[7]);
				else
					for (i = 6; i > king.y; i--) {
						black_weights.add(WeightsInitializer.BottomCheck[i]);
					}
			}

		} else if (king.y == 0) { // Vertical check

			boolean top_blocked = false;
			boolean bottom_blocked = false;

			for (i = 1; i < 8; i++) {
				if (black_piece_hashset.contains(Coordinates.get(i, king.y))
						|| white_piece_hashset.contains(Coordinates.get(i, king.y))) {
					if (i > king.y)
						bottom_blocked = true;
					else
						top_blocked = true;
				}
			}

			if (!top_blocked) {
				check = true;
				if (king.x == 1 || king.x == 3)
					black_weights.add(WeightsInitializer.LeftCheck[2]);
				else if (king.x == 2)
					black_weights.add(WeightsInitializer.LeftCheck[1]);
				else
					for (i = 2; i < king.x; i++) {
						black_weights.add(WeightsInitializer.LeftCheck[i]);
					}
			}

			if (!bottom_blocked) {
				check = true;
				if (king.x == 7 || king.x == 5)
					black_weights.add(WeightsInitializer.LeftCheck[6]);
				else if (king.x == 6)
					black_weights.add(WeightsInitializer.LeftCheck[7]);
				else
					for (i = 6; i > king.x; i--) {
						black_weights.add(WeightsInitializer.LeftCheck[i]);
					}
			}

		} else if (king.y == 8) { // Vertical check

			boolean top_blocked = false;
			boolean bottom_blocked = false;

			for (i = 1; i < 8; i++) {
				if (black_piece_hashset.contains(Coordinates.get(i, king.y))
						|| white_piece_hashset.contains(Coordinates.get(i, king.y))) {
					if (i > king.y)
						bottom_blocked = true;
					else
						top_blocked = true;
				}
			}

			if (!top_blocked) {
				check = true;
				if (king.x == 1 || king.x == 3)
					black_weights.add(WeightsInitializer.RightCheck[2]);
				else if (king.x == 2)
					black_weights.add(WeightsInitializer.RightCheck[1]);
				else
					for (i = 2; i < king.x; i++) {
						black_weights.add(WeightsInitializer.RightCheck[i]);
					}
			}

			if (!bottom_blocked) {
				check = true;
				if (king.x == 7 || king.x == 5)
					black_weights.add(WeightsInitializer.RightCheck[6]);
				else if (king.x == 6)
					black_weights.add(WeightsInitializer.RightCheck[7]);
				else
					for (i = 6; i > king.x; i--) {
						black_weights.add(WeightsInitializer.RightCheck[7]);
					}
			}

		}



		/*
		 * 
		 * KING WALL BLOCK
		 * 
		 */
		Coord[] wall = { Coordinates.get(0, king.y), // TOP WALL
				Coordinates.get(8, king.y), // BOTTOM WALL
				Coordinates.get(king.x, 0), // LEFT WALL
				Coordinates.get(king.x, 8) }; // RIGHT WALL

		boolean[] can_reach_wall = new boolean[4];
		
		for (i = 0; i < 4; i++) {

			can_reach_wall[i] = true;
			// Can the king reach the wall?
			for (Coord throughCoordinate : king.getCoordsBetween(wall[i])) {
				if (!board_state.coordIsEmpty(throughCoordinate)) {
					can_reach_wall[i] = false;
					break;
				}
				if (!board_state.coordIsEmpty(wall[i])) can_reach_wall[i] = false;
			}
			// If he can, then add weights to block him
			if (can_reach_wall[i]) {
				for (Coord throughCoordinate : king.getCoordsBetween(wall[i])) {
					black_weights.add(WeightsInitializer.PositiveKingBlock[throughCoordinate.x][throughCoordinate.y]);
					white_weights.add(WeightsInitializer.NegativeKingBlock[throughCoordinate.x][throughCoordinate.y]);
				}
			}
		}
		
		/*
		 * 
		 * TRIANGLE DETECTION
		 * 
		 */

		if (king.x == 1) { // HORIZONTAL TRIANGLE

			boolean left_triangle = can_reach_wall[2]; 
			if (left_triangle) {
				king_weights.add(WeightsInitializer.LeftCheck[1]);
				black_weights.add(WeightsInitializer.PositiveTriangle[1][0]);
				black_weights.add(WeightsInitializer.PositiveTriangle[1][1]);
				black_weights.add(WeightsInitializer.NegativeTriangle[2][0]);
				white_weights.add(WeightsInitializer.NegativeTriangle[1][0]);
				white_weights.add(WeightsInitializer.PositiveTriangle[2][0]);
			}

			boolean right_triangle = can_reach_wall[3];
			if (right_triangle) {
				king_weights.add(WeightsInitializer.RightCheck[1]);
				black_weights.add(WeightsInitializer.PositiveTriangle[1][8]);
				black_weights.add(WeightsInitializer.PositiveTriangle[1][7]);
				black_weights.add(WeightsInitializer.NegativeTriangle[2][8]);
				white_weights.add(WeightsInitializer.NegativeTriangle[1][8]);
				white_weights.add(WeightsInitializer.PositiveTriangle[2][8]);
			}

		} else if (king.x == 7) {

			boolean left_triangle = can_reach_wall[2]; 
			if (left_triangle) {
				king_weights.add(WeightsInitializer.LeftCheck[7]);
				black_weights.add(WeightsInitializer.PositiveTriangle[7][0]);
				black_weights.add(WeightsInitializer.PositiveTriangle[7][1]);
				black_weights.add(WeightsInitializer.NegativeTriangle[6][0]);
				white_weights.add(WeightsInitializer.NegativeTriangle[7][0]);
				white_weights.add(WeightsInitializer.PositiveTriangle[6][0]);
			}

			boolean right_triangle = can_reach_wall[3]; 
			if (right_triangle) {
				king_weights.add(WeightsInitializer.RightCheck[7]);
				black_weights.add(WeightsInitializer.PositiveTriangle[7][8]);
				black_weights.add(WeightsInitializer.PositiveTriangle[7][7]);
				black_weights.add(WeightsInitializer.NegativeTriangle[6][8]);
				white_weights.add(WeightsInitializer.NegativeTriangle[7][8]);
				white_weights.add(WeightsInitializer.PositiveTriangle[6][8]);
			}

		}

		if (king.y == 1) { // VERTICAL TRIANGLE

			boolean top_triangle = can_reach_wall[0]; 
			if (top_triangle) {
				king_weights.add(WeightsInitializer.TopCheck[1]);
				black_weights.add(WeightsInitializer.PositiveTriangle[0][1]);
				black_weights.add(WeightsInitializer.PositiveTriangle[1][1]);
				black_weights.add(WeightsInitializer.NegativeTriangle[0][2]);
				white_weights.add(WeightsInitializer.NegativeTriangle[0][1]);
				white_weights.add(WeightsInitializer.PositiveTriangle[0][2]);
			}

			boolean bottom_triangle = can_reach_wall[1]; 
			if (bottom_triangle) {
				king_weights.add(WeightsInitializer.BottomCheck[1]);
				black_weights.add(WeightsInitializer.PositiveTriangle[8][1]);
				black_weights.add(WeightsInitializer.PositiveTriangle[7][1]);
				black_weights.add(WeightsInitializer.NegativeTriangle[8][2]);
				white_weights.add(WeightsInitializer.NegativeTriangle[8][1]);
				white_weights.add(WeightsInitializer.PositiveTriangle[8][2]);
			}

		} else if (king.y == 7) {

			boolean top_triangle = can_reach_wall[0]; 
			if (top_triangle) {
				king_weights.add(WeightsInitializer.TopCheck[7]);
				black_weights.add(WeightsInitializer.PositiveTriangle[0][7]);
				black_weights.add(WeightsInitializer.PositiveTriangle[1][7]);
				black_weights.add(WeightsInitializer.NegativeTriangle[0][6]);
				white_weights.add(WeightsInitializer.NegativeTriangle[0][7]);
				white_weights.add(WeightsInitializer.PositiveTriangle[0][6]);
			}

			boolean bottom_triangle = can_reach_wall[1]; 
			if (bottom_triangle) {
				king_weights.add(WeightsInitializer.LeftCheck[7]);
				black_weights.add(WeightsInitializer.PositiveTriangle[8][7]);
				black_weights.add(WeightsInitializer.PositiveTriangle[7][7]);
				black_weights.add(WeightsInitializer.NegativeTriangle[8][6]);
				white_weights.add(WeightsInitializer.NegativeTriangle[8][7]);
				white_weights.add(WeightsInitializer.PositiveTriangle[8][6]);
			}

		}

		/*
		 * 
		 * GUILLOTINE DETECTION
		 * 
		 */

		if (king.x == 2) { // HORIZONTAL GUILLOTINE

			if (king.y != 0 && king.y != 8) {

				boolean left_guillotine = can_reach_wall[2]; 
				if (left_guillotine) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[2][0]);
					black_weights.add(WeightsInitializer.NegativeGuillotine[1][0]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[1][1]);
				}

				boolean right_guillotine = can_reach_wall[3]; 
				if (right_guillotine) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[2][8]);
					black_weights.add(WeightsInitializer.NegativeGuillotine[1][8]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[1][7]);
				}

			} else { // Add king weight to move out and execute guillotine
				king_weights.add(WeightsInitializer.NegativeGuillotine[king.x][king.y]);
				if (king.y == 0) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[2][1]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[1][0]);
				} else if (king.y == 8) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[2][7]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[8][0]);
				}
			}

		} else if (king.x == 6) {

			if (king.y != 0 && king.y != 8) {

				boolean left_guillotine = can_reach_wall[2]; 
				if (left_guillotine) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[6][0]);
					black_weights.add(WeightsInitializer.NegativeGuillotine[7][0]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[7][1]);
				}

				boolean right_guillotine = can_reach_wall[3]; 
				if (right_guillotine) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[6][8]);
					black_weights.add(WeightsInitializer.NegativeGuillotine[7][8]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[7][7]);
				}

			} else { // Add king weight to move out and execute guillotine
				king_weights.add(WeightsInitializer.NegativeGuillotine[king.x][king.y]);
				if (king.y == 0) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[6][1]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[7][0]);
				} else if (king.y == 8) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[6][7]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[7][8]);
				}
			}

		}

		if (king.y == 2) {

			if (king.x != 0 && king.x != 8) {

				boolean top_guillotine = can_reach_wall[0]; 
				if (top_guillotine) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[0][2]);
					black_weights.add(WeightsInitializer.NegativeGuillotine[0][1]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[1][1]);
				}

				boolean bottom_guillotine = can_reach_wall[1]; 
				if (bottom_guillotine) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[8][2]);
					black_weights.add(WeightsInitializer.NegativeGuillotine[8][1]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[7][1]);
				}

			} else { // Add king weight to move out and execute guillotine
				king_weights.add(WeightsInitializer.NegativeGuillotine[king.x][king.y]);
				if (king.x == 0) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[1][2]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[0][1]);
				} else if (king.x == 8) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[7][2]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[8][1]);
				}
			}

		} else if (king.y == 6) {

			if (king.x != 0 && king.x != 8) {

				boolean top_guillotine = can_reach_wall[0]; 
				if (top_guillotine) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[0][6]);
					black_weights.add(WeightsInitializer.NegativeGuillotine[0][7]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[1][7]);
				}

				boolean bottom_guillotine = can_reach_wall[1]; 
				if (bottom_guillotine) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[8][6]);
					black_weights.add(WeightsInitializer.NegativeGuillotine[8][7]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[7][7]);
				}

			} else { // Add king weight to move out and execute guillotine
				king_weights.add(WeightsInitializer.NegativeGuillotine[king.x][king.y]);
				if (king.x == 0) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[1][6]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[0][7]);
				} else if (king.x == 8) {
					king_weights.add(WeightsInitializer.PositiveGuillotine[7][6]);
					black_weights.add(WeightsInitializer.PositiveGuillotine[8][7]);
				}
			}

		}

		/*
		 * 
		 * WEIGHTS CALCULATION COMPLETE
		 * 
		 */

		return weights;

	}

	// This function inserts a into array best_moves while maintaining ascending
	// order with index 0 being greatest (alphaBeta helper)
	private static void insertInOrderDescending(int index, int evaluation, int[][] arr) {

		int i;
		for (i = 0; i < arr.length - 1; i++)
			if (evaluation > arr[i][1] || (arr[i][0] == 0 && arr[i][1] == 0))
				break;
		for (int k = arr.length - 2; k >= i; k--) {
			arr[k + 1][0] = arr[k][0];
			arr[k + 1][1] = arr[k][1];

		}
		arr[i][0] = index;
		arr[i][1] = evaluation;

	}

	// This function inserts a into array best_moves while maintaining ascending
	// order (alphaBeta helper)
	private static void insertInOrderAscending(int index, int evaluation, int[][] arr) {

		int i;
		for (i = 0; i < arr.length - 1; i++)
			if (evaluation < arr[i][1] || (arr[i][0] == 0 && arr[i][1] == 0))
				break;
		for (int k = arr.length - 2; k >= i; k--) {
			arr[k + 1][0] = arr[k][0];
			arr[k + 1][1] = arr[k][1];
		}
		arr[i][0] = index;
		arr[i][1] = evaluation;

	}

	// Flips the side
	private static int flipSide(int side) {
		if (side == 1)
			return 0;
		else
			return 1;
	}

	// Returns minimum of 2 values
	private static int min(int a, int b) {
		if (a < b)
			return a;
		else
			return b;
	}

}
