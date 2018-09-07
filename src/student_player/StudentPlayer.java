package student_player;

import java.util.ArrayList;
import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

public class StudentPlayer extends TablutPlayer {
 
	static int search_depth = 3;
	
	public StudentPlayer() {
        super("260512650");
    }

    public Move chooseMove(TablutBoardState board_state) {
    	
    	// Get table state variables
    	int move_count = board_state.getTurnNumber();
    	// Initialize default weights at first move
        if (move_count == 0) WeightsInitializer.initialize();
        
        int side = board_state.getTurnPlayer();
        
        // Get all the legal moves from board state
        ArrayList<TablutMove> legal_moves = board_state.getAllLegalMoves();
        for (int i = 0; i < legal_moves.size(); i++) {
        	
        	if (legal_moves.get(i).getPlayerID() != side) {
        		legal_moves.remove(i);
        		i--; // Since elements are shifted down
        	} 
        }
        
        // Choose opening move
    	if (side == 0 ) {
        	
        	if (move_count == 0) for (int j = 0; j < legal_moves.size(); j++) {
        		//System.out.println("Searching Opener");
        		if (		legal_moves.get(j).getStartPosition().x == 3 && legal_moves.get(j).getEndPosition().x == 3 &&
        					legal_moves.get(j).getStartPosition().y == 0 && legal_moves.get(j).getEndPosition().y == 1) 
        		{
            		return legal_moves.get(j);
        		}
        	}
        	else if (move_count == 1) for (int j = 0; j < legal_moves.size(); j++) {
        		//System.out.println("Searching Opener");
        		if (		legal_moves.get(j).getStartPosition().x == 5 && legal_moves.get(j).getEndPosition().x == 5 &&
        					legal_moves.get(j).getStartPosition().y == 8 && legal_moves.get(j).getEndPosition().y == 7) 
        		{
            		return legal_moves.get(j);
        		}
        	}
        	
        }
    	
    	// In all other cases use alphaBeta pruning with weighted coordinates to determine best move
        TablutMove best_move = legal_moves.get(MyTools.alphaBeta(board_state, side, search_depth, Integer.MIN_VALUE, Integer.MAX_VALUE, search_depth));
        
        return best_move;
        
    }
    
    
    
}



























































