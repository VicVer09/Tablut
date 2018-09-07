package student_player;

public class WeightsInitializer {
	

	// These arrays contain initialized WeightedCoord objects
	// That can be quickly accessed when determining weights for
	// spots on the board for different pieces
	static WeightedCoord[] TopCheck;
	static WeightedCoord[] BottomCheck;
	static WeightedCoord[] LeftCheck;
	static WeightedCoord[] RightCheck;

	static WeightedCoord[][] PositiveTriangle;
	static WeightedCoord[][] NegativeTriangle;
	static WeightedCoord[][] PositiveGuillotine;
	static WeightedCoord[][] NegativeGuillotine;
	static WeightedCoord[][] PositiveKingBlock;
	static WeightedCoord[][] NegativeKingBlock;
	static WeightedCoord[][] Diagonal;
	static WeightedCoord[][] NegativeDiagonal;
	static WeightedCoord[][] NeighbourPenalty;
	
	public static void initialize() {
		
		System.out.println("Initializing Weights");
		
		// Check weights only required for walls
		TopCheck = new WeightedCoord[9];
		BottomCheck = new WeightedCoord[9];
		LeftCheck = new WeightedCoord[9];
		RightCheck = new WeightedCoord[9];

		for (int i = 0; i < 9; i ++) {
			TopCheck[i] = new WeightedCoord 	( 0, i, MyTools.check_parameter);
			BottomCheck[i] = new WeightedCoord 	( 8, i, MyTools.check_parameter);
			LeftCheck[i] = new WeightedCoord 	( i, 0, MyTools.check_parameter);
			RightCheck[i] = new WeightedCoord 	( i, 8, MyTools.check_parameter);
		}
		
		// 9x9 not necessary but out of convenience it is easier to keep track of indices this way
		PositiveTriangle  = new WeightedCoord[9][9];
		NegativeTriangle  = new WeightedCoord[9][9];
		PositiveGuillotine = new WeightedCoord[9][9];
		NegativeGuillotine = new WeightedCoord[9][9];
		PositiveKingBlock  = new WeightedCoord[9][9];
		NegativeKingBlock  = new WeightedCoord[9][9]; 
		Diagonal = new WeightedCoord[9][9];
		NegativeDiagonal = new WeightedCoord[9][9];
		NeighbourPenalty = new WeightedCoord[9][9];
		
		for (int i = 0; i < 9; i ++) {
			for (int j = 0; j < 9; j++) {
				PositiveTriangle [i][j]  = new WeightedCoord(i,j,  MyTools.triangle_weight);
				NegativeTriangle [i][j]  = new WeightedCoord(i,j, -MyTools.triangle_weight);
				PositiveGuillotine[i][j] = new WeightedCoord(i,j,  MyTools.guillotine_weight);
				NegativeGuillotine[i][j] = new WeightedCoord(i,j, -MyTools.guillotine_weight);
				PositiveKingBlock [i][j] = new WeightedCoord(i,j,  MyTools.king_block_parameter);
				NegativeKingBlock [i][j] = new WeightedCoord(i,j, -MyTools.king_block_parameter);
				Diagonal [i][j] = new WeightedCoord(i,j, MyTools.diag_parameter);
				NegativeDiagonal [i][j] = new WeightedCoord(i,j, -MyTools.diag_parameter);
				NeighbourPenalty[i][j] = new WeightedCoord(i,j, -MyTools.neighbour_penalty);
			}
		}

	}

}
