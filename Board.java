import java.util.ArrayList;
import java.util.Arrays;

public class Board {
	Piece[][] chessBoard = new Piece[8][8];
	Piece[] whitePieces = new Piece[16];
	Piece[] blackPieces = new Piece[16];
	int[] enPassant = {-1, -1};
	Piece empty = new Piece('e', "Empty", ' ', 0);
	Piece whiteKing;
	Piece blackKing;
	boolean whitesTurn = true;
	boolean inCheck = false;
	boolean checkmate = false;
	boolean blackCanCastle = true;
	boolean whiteCanCastle = true;
	
	//Top left, top right, bottom left, bottom right
	boolean[] rookMoved = {false, false, false, false};
	boolean[] canCastle = {false, false, false, false};
	
	public Board() {
		spawnPieces();
		findLegalMoves();
	}
	
	public boolean checkmate() {
		return checkmate;
	}
	
	public void findLegalMoves() {
		findLegalMovesForWhite();
		findLegalMovesForBlack();
		if (whitesTurn) {
			if (whiteCanCastle) {
				checkForCastling("white");
			}
		} else {
			if (blackCanCastle) {
				checkForCastling("black");
			}
		}
	}
	
	/*
	 * This checks to see if the space is empty between a non moved king and rook
	 */
	public void checkForCastling(String color) {
		boolean playerCanCastle;
		int offset;
		int row;
		//Check player turn and set variables
		if (color.equals("white")) {
			playerCanCastle = whiteCanCastle;
			offset = 2;
			row = 7;
			canCastle[2] = false;
			canCastle[3] = false;
		} else {
			playerCanCastle = blackCanCastle;
			offset = 0;
			row = 0;
			canCastle[0] = false;
			canCastle[1] = false;
		}
		//If player can castle, check both right and left rook individually
		if (playerCanCastle) {
			boolean leftRookMoved = rookMoved[0 + offset];
			boolean rightRookMoved = rookMoved[1 + offset];
			if (leftRookMoved && rightRookMoved) {
				playerCanCastle = false;
			} else {
				if (!leftRookMoved) {
					int[] castleMove = {row, 3};
					if (possibleMove(chessBoard[row][0], castleMove)) {
						canCastle[0 + offset] = checkForCastlingCheck(color, "left");
					}
				}
				if (!rightRookMoved) {
					int[] castleMove = {row, 5};
					if (possibleMove(chessBoard[row][7], castleMove)) {
						canCastle[1 + offset] = checkForCastlingCheck(color, "right");
					}
				}
			}
		}
	}
	
	/*
	 * This checks to see if any spaces between the king and rook are in check
	 */
	public boolean checkForCastlingCheck(String color, String direction) {
		boolean canCastle = true;
		int row;
		int a;
		int b;
		Piece[] pieces;
		if (color.equals("white")) {
			pieces = blackPieces;
			row = 7;
		} else {
			pieces = whitePieces;
			row = 0;
		}
		if (direction.equals("left")) {
			a = 1;
			b = 4;
		} else {
			a = 4;
			b = 6;
		}
		//Check every space between the king and his destination for check
		for (int col = a; col <= b; col++) {
			int[] tempSpace = {row, col};
			for (int j = 0; j < pieces.length; j++) {
				for (int k = 0; k < pieces[j].legalMoves.size(); k++) {
					if (Arrays.equals(pieces[j].legalMoves.get(k), tempSpace)) {
						return false;
					}
				}
			}
		}
		return canCastle;
	}
	
	/*
	 * Translates the castling movement into correct coordinates for the move
	 */
	public boolean movePlayerCastle(int fromRow, int fromCol) {
		if (whitesTurn) {
			//System.out.println("Attempting castling for white...");
			//Check white for castling
			if(fromRow == -1) {
				if (canCastle[2]) {
					return moveCastle(7, 2, 4, 3, 0);
				}
			} else if (fromCol == -1) {
				if (canCastle[3]) {
					return moveCastle(7, 6, 4, 5, 7);
				}
			}
		} else {
			if(fromRow == -1) {
				if (canCastle[0]) {
					return moveCastle(0, 2, 4, 3, 0);
				}
			} else if (fromCol == -1) {
				if (canCastle[1]) {
					return moveCastle(0, 6, 4, 5, 7);
				}
			}
		}
		return false;
	}
	
	/*
	 * Does the movements to Castle
	 */
	public boolean moveCastle(int x, int y, int z, int a, int b) {
		chessBoard[x][y] = chessBoard[x][z];
		int[] newKingLocation = {x, y};
		chessBoard[x][y].currentLocation = newKingLocation;
		
		chessBoard[x][a] = chessBoard[x][b];
		int[] newRookLocation = {x, a};
		chessBoard[x][a].currentLocation = newRookLocation;
		
		chessBoard[x][z] = empty;
		chessBoard[x][b] = empty;
		if (x == 0) {
			blackCanCastle = false;
		} else {
			whiteCanCastle = false;
		}
		switchTurns();
		return true;
	}

	/*
	 * Checks to see if a piece contains a move to the location
	 */
	public boolean possibleMove(Piece piece, int[] toLocation) {
		for (int i = 0; i < piece.legalMoves.size(); i++) {
			if (Arrays.equals(piece.legalMoves.get(i), toLocation)) {
				return true;
			}
		}
		return false;
	}
	
	public void switchTurns() {
		//Switch turns and find available moves
		whitesTurn = !whitesTurn;
		findLegalMoves();
		//Check to see if player is in check
		if (whitesTurn) {
			if (whiteInCheck()){
				inCheck = true;
			}
		} else {
			if (blackInCheck()) {
				inCheck = true;
			}
		}
	}
	

	
	/*
	 * Attempts to move the piece for the player.
	 * 1. Check to see if it's an actual player move or if it's theoretical
	 * 		-If it's an actual player and any of the requirements below aren't met return false.
	 * 2. Make sure they're moving an owned piece
	 * 3. Make sure it's a legal move of that piece
	 * 4. Check to see if it results in check for the moving player
	 */
	public boolean movePiece(boolean playerMove, int fromRow, int fromCol, int toRow, int toCol) {
		if (fromRow == -1 || fromCol == -1) {
			return movePlayerCastle(fromRow, fromCol);
		}
		//Grab the current pieces involved in the movement
		Piece fromPiece = chessBoard[fromRow][fromCol];
		Piece toPiece = chessBoard[toRow][toCol];
		if (whitesTurn) {
			if (fromPiece.color != 'w') {
				if (playerMove) {
					System.out.println("This is not a white piece");
				}
				return false;
			}
		} else {
			if (fromPiece.color != 'b') {
				if (playerMove) {
					System.out.println("This is not a black piece");
				}
				return false;
			}
		}
		
		//Check to see if the piece can move in that way
		boolean possibleMove = false;
		int[] toLocation = {toRow, toCol};
		for (int i = 0; i < fromPiece.legalMoves.size(); i++) {
			if (Arrays.equals(fromPiece.legalMoves.get(i), toLocation)) {
				possibleMove = true;
			}
		}
		
		//If it can...
		if (possibleMove) {
			//Copy them in-case move results in a check
			Piece fromPieceCopy = new Piece(fromPiece);
			Piece toPieceCopy = new Piece(toPiece);
			
			//Put the pieces to the location of the move to test if it's valid below
			chessBoard[toRow][toCol] = fromPiece;
			fromPiece.currentLocation[0] = toRow;
			fromPiece.currentLocation[1] = toCol;
			chessBoard[fromRow][fromCol] = empty;
			if (toPiece.type != ' ') {
				toPiece.currentLocation[0] = -1;
				toPiece.currentLocation[1] = -1;
				toPiece.destroyed = true;
			}
			
			//Check to see if moving player put their king in check. If so - reset
			if (kingInCheck()) {
				fromPiece.resetPiece(fromPieceCopy);
				toPiece.resetPiece(toPieceCopy);
				chessBoard[fromRow][fromCol] = fromPiece;
				chessBoard[toRow][toCol] = toPiece;
				if (playerMove) {
					System.out.println("This move puts your king in check");
				}
				return false;
				
			//The move can happen...
			} else {
				//This will be called by another function checking to see if a player can escape check or if it's checkmate
				if (!playerMove) {
					fromPiece.resetPiece(fromPieceCopy);
					toPiece.resetPiece(toPieceCopy);
					chessBoard[fromRow][fromCol] = fromPiece;
					chessBoard[toRow][toCol] = toPiece;
					//System.out.println("A WAY OUT OF CHECK WAS FOUND");
					return true;
				}
				//If a piece can move and the king is not in check, then incheck is now false
				boolean setEnPassant = false;
				inCheck = false;
				//If a king has been moved, disable castling
				if (fromPiece.type == 'M') {
					if (whitesTurn) {
						whiteCanCastle = false;
					} else {
						blackCanCastle = false;
					}
				//If castle has moved disable repective castling
				} else if (fromPiece.type == 'T') {
					if (whitesTurn) {
						if (!rookMoved[2]) {
							if(fromPieceCopy.currentLocation[1] == 0) {
								rookMoved[2] = true;
								System.out.println("White can no longer castle left");
							}
						}
						if (!rookMoved[3]) {
							if(fromPieceCopy.currentLocation[1] == 7) {
								rookMoved[3] = true;
							}
						}
					} else {
						if (!rookMoved[0]) {
							if(fromPieceCopy.currentLocation[1] == 0) {
								rookMoved[0] = true;
							}
						}
						if (!rookMoved[1]) {
							if(fromPieceCopy.currentLocation[1] == 7) {
								rookMoved[1] = true;
							}
						}
					}
				//If it's a pawn check to see if a promotions available!
				} else if (fromPiece.type == 'P') {
					if (whitesTurn) {
						if (fromPiece.currentLocation[0] == 0) {
							promotion(fromPiece);
						} else if (fromPiece.currentLocation[0] == 4 && fromPieceCopy.currentLocation[0] == 6) {
							enPassant[0] = 5;
							enPassant[1] = fromPiece.currentLocation[1];
							setEnPassant = true;
						} else if (Arrays.equals(fromPiece.currentLocation, enPassant)) {
							System.out.println("Holy shit en passant!");
							Piece pawn = chessBoard[3][fromPiece.currentLocation[1]];
							pawn.currentLocation[0] = -1;
							pawn.currentLocation[1] = -1;
							pawn.destroyed = true;
							chessBoard[3][fromPiece.currentLocation[1]] = empty;
						}
					} else {
						if (fromPiece.currentLocation[0] == 7) {
							promotion(fromPiece);
						} else if (fromPiece.currentLocation[0] == 3 && fromPieceCopy.currentLocation[0] == 1) {
							enPassant[0] = 2;
							enPassant[1] = fromPiece.currentLocation[1];
							setEnPassant = true;
						} else if (Arrays.equals(fromPiece.currentLocation, enPassant)) {
							Piece pawn = chessBoard[4][fromPiece.currentLocation[1]];
							pawn.currentLocation[0] = -1;
							pawn.currentLocation[1] = -1;
							pawn.destroyed = true;
							chessBoard[4][fromPiece.currentLocation[1]] = empty;
						}
					}
				}
				//If no En Passant coordinate was created, destroy the previous one
				if (!setEnPassant) {
					enPassant[0] = -1;
				}
				switchTurns();
				return true;
			}
		} else {
			System.out.println("Piece cannot make that move");
			return false;
		}
	}
	
	public void promotion(Piece pawn) {
		System.out.println("Choose your promotion piece: T K B Q");
		boolean choiceMade = false;
		while (!choiceMade) {
			choiceMade = true;
			String upgrade = "Q";
			if (upgrade.contains("T")) {
				pawn.promotion("Rook", 't');
			} else if (upgrade.contains("K")) {
				pawn.promotion("Knight", 'K');
			} else if (upgrade.contains("B")) {
				pawn.promotion("Bishop", 'B');
			} else if (upgrade.contains("Q")) {
				pawn.promotion("Queen", 'Q');
			} else {
				choiceMade = false;
				System.out.println("Verify your input: T, K, B, Q are the only allowed characters");
			}
		}
	}
	
	public void printAllLegalMoves() {
		for (int i = 0; i < whitePieces.length; i++) {
			System.out.println(whitePieces[i].printLegalMoves());
		}
		for (int i = 0; i < blackPieces.length; i++) {
			System.out.println(blackPieces[i].printLegalMoves());
		}
	}

	public ArrayList<ArrayList<int[]>> findLegalMovesForBlack() {
		ArrayList<ArrayList<int[]>> legalMovesForBlack = new ArrayList<>();
		for (int i = 0; i < blackPieces.length; i++) {
			legalMovesForBlack.add(findLegalMoves(blackPieces[i]));
		}
		return legalMovesForBlack;
	}
	
	public boolean kingInCheck() {
		Piece piece;
		if (whitesTurn) {
			piece = whiteKing;
		} else {
			piece = blackKing;
		}
		int row = piece.currentLocation[0];
		int col = piece.currentLocation[1];
		//System.out.println("Checking to see if king is in check...");
		//i and j both go from -1 to 1. This means the king will check one direction around him at a time.
		//Keep looking in that one direction until a piece is found
		//This will not find enemy knights, but will detect all other checks
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				//Don't check kings current square as it's himself
				if (!(i == 0 && j == 0)) {
					//Add 1 to the vector of x and y for each while loop to keep checking the current direction.
					int x = Math.abs(i);
					int y = Math.abs(j);
					//Keep checking until a piece or the edge of the board is detected by no longer being empty
					while ((row + x * i < 8) && (row + x * i >= 0) && (col + y * j < 8) && (col + y * j >= 0) && chessBoard[row + x * i][col + y * j].color == 'e') {
						x++;
						y++;
					}
					//Once a non-empty location is found, see what's there
					if ((row + x * i < 8) && (row + x * i >= 0) && (col + y * j < 8) && (col + y * j >= 0)) {
						Piece potentialAttacker = new Piece(chessBoard[row + x * i][col + y * j]);
						//If the piece isn't friendly investigate the enemies attack options
						if (piece.color != potentialAttacker.color) {
							findLegalMoves(potentialAttacker);
							//If the enemy can take the king, this move is not allowed and would put the king in check
							for (int k = 0; k < potentialAttacker.legalMoves.size(); k++) {
								if (Arrays.equals(potentialAttacker.legalMoves.get(k), piece.currentLocation)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		//Check for horsies
		for (int i = -2; i <=2; i++) {
			for (int j = -2; j <=2; j++) {
				if (i != 0 && j != 0 && Math.abs(i) != Math.abs(j)) {
					if (checkForEnemyKnight(row + i, col + j)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean checkForEnemyKnight(int x, int y) {
		if (x < 0 || x > 7 || y < 0 || y > 7) {
			return false;
		}
		char color;
		if (whitesTurn) {
			color = 'b';
		} else {
			color = 'w';
		}
		if (chessBoard[x][y].type == 'K' && chessBoard[x][y].color == color) {
			return true;
		}
		return false;
	}
	
	public boolean whiteInCheck() {
		//System.out.println("CHECKING TO SEE IF WHITE KING IS IN CHECK!!!");
		ArrayList<ArrayList<int[]>> legalMovesForBlack = findLegalMovesForBlack();
		//System.out.println("Total pieces for black: " + legalMovesForBlack.size());
		for (int i = 0; i < legalMovesForBlack.size(); i++) {
			if (legalMovesForBlack.get(i).size() > 0) {
				ArrayList<int[]> blackPieceLegalMoves = legalMovesForBlack.get(i); 
				//System.out.println("Legal moves for this piece: " + blackPieceLegalMoves.size());
				for (int j = 0; j < blackPieceLegalMoves.size(); j++) {
					//System.out.println(blackPieceLegalMoves.get(j)[0] + " " + blackPieceLegalMoves.get(j)[1] + " : " + whiteKing.currentLocation[0] + " " + whiteKing.currentLocation[1]);
					if (Arrays.equals(blackPieceLegalMoves.get(j), whiteKing.currentLocation)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public ArrayList<ArrayList<int[]>> findLegalMovesForWhite() {
		ArrayList<ArrayList<int[]>> legalMovesForWhite = new ArrayList<>();
		for (int i = 0; i < whitePieces.length; i++) {
			legalMovesForWhite.add(findLegalMoves(whitePieces[i]));
		}
		return legalMovesForWhite;
	}
	
	public boolean blackInCheck() {
		//System.out.println("CHECKING TO SEE IF BLACK KING IS IN CHECK!!!");
		ArrayList<ArrayList<int[]>> legalMovesForWhite = findLegalMovesForWhite();
		//System.out.println("Total pieces for white: " + legalMovesForWhite.size());
		for (int i = 0; i < legalMovesForWhite.size(); i++) {
			if (legalMovesForWhite.get(i).size() > 0) {
				ArrayList<int[]> whitePieceLegalMoves = legalMovesForWhite.get(i); 
				//System.out.println("Legal moves for this piece: " + whitePieceLegalMoves.size());
				for (int j = 0; j < whitePieceLegalMoves.size(); j++) {
					//System.out.println(whitePieceLegalMoves.get(j)[0] + " " + whitePieceLegalMoves.get(j)[1] + " : " + blackKing.currentLocation[0] + " " + blackKing.currentLocation[1]);
					if (Arrays.equals(whitePieceLegalMoves.get(j), blackKing.currentLocation)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public ArrayList<int[]> findLegalMoves(Piece piece) {
		int row = piece.currentLocation[0];
		int col = piece.currentLocation[1];
		ArrayList<int[]> legalMoves = new ArrayList<>();
		char enemy;
		int z;
		int i = 1;
		if (piece.color == 'b') {
			enemy = 'w';
			z = 1;
		} else {
			enemy = 'b';
			z = -1;
		}
		switch (piece.type) {

		//If it's an empty piece
		case ' ':
			break;

			//If we have a Pawn
		case 'P':
			//Check for forward move (empty square)
			if (checkSquare(legalMoves, row + 1 * z, col, 'e')) {
				//Check for double jump, only available first time using the pawn.
				if ((piece.color == 'w' && row == 6) || (piece.color == 'b' && row == 1)) {
					checkSquare(legalMoves, row + 2 * z, col, 'e');
				}
			}
			if (enPassant[0] != -1) {
				//Only want to trigger on moving team pawns
				if (!(whitesTurn ^ piece.color == 'w')) {
					int[] checkEnPassant = {row + 1 * z, col + 1};
					if (Arrays.equals(checkEnPassant, enPassant)) {
						legalMoves.add(enPassant);
					}
					checkEnPassant[0] = row + 1 * z;
					checkEnPassant[1] = col - 1;
					if (Arrays.equals(checkEnPassant, enPassant)) {
						legalMoves.add(enPassant);
					}
				}
			}
			//Check for diagonal attack move (Take an enemy piece)
			checkSquare(legalMoves, row + 1 * z, col + 1, enemy);
			checkSquare(legalMoves, row + 1 * z, col - 1, enemy);
			break;

		//If we have a Rook
		case 'T':
			//While - Check for open spaces, continue until it's blocked.
			//Then check to see if that non-empty space contains an enemy.
			
			//Check down
			while (checkSquare(legalMoves, row + i, col, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row + i, col, enemy);
			i = 1;
			
			//Check right
			while (checkSquare(legalMoves, row, col + i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row, col + i, enemy);
			i = 1;
			
			//Check up
			while (checkSquare(legalMoves, row - i, col, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row - i, col, enemy);
			i = 1;
			
			//Check left
			while (checkSquare(legalMoves, row, col - i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row, col - i, enemy);
			break;
		
		//If we have a Bishop
		case 'B':
			//Check down-right
			while (checkSquare(legalMoves, row + i, col + i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row + i, col + i, enemy);
			i = 1;
			
			//Check down-left
			while (checkSquare(legalMoves, row + i, col - i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row + i, col - i, enemy);
			i = 1;
			
			//Check up-left
			while (checkSquare(legalMoves, row - i, col - i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row - i, col - i, enemy);
			i = 1;
			
			//Check up-right
			while (checkSquare(legalMoves, row - i, col + i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row - i, col + i, enemy);
			i = 1;
			break;
			
		//If we have a Knight
		case 'K':
			for (int a = -2; a <=2; a++) {
				for (int b = -2; b <=2; b++) {
					if (a != 0 && b != 0 && Math.abs(a) != Math.abs(b)) {
						if (checkSquare(legalMoves, row + a, col + b, enemy) || checkSquare(legalMoves, row + a, col + b, 'e'));
					}
				}
			}
			break;
			
		//If we have a Queen
		case 'Q':
			//This is literally just Rook code and Bishop code copied and pasted one after another.
			
			//Check down
			while (checkSquare(legalMoves, row + i, col, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row + i, col, enemy);
			i = 1;
			
			//Check right
			while (checkSquare(legalMoves, row, col + i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row, col + i, enemy);
			i = 1;
			
			//Check up
			while (checkSquare(legalMoves, row - i, col, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row - i, col, enemy);
			i = 1;
			
			//Check left
			while (checkSquare(legalMoves, row, col - i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row, col - i, enemy);
			i = 1;
			
			//Check down-right
			while (checkSquare(legalMoves, row + i, col + i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row + i, col + i, enemy);
			i = 1;
			
			//Check down-left
			while (checkSquare(legalMoves, row + i, col - i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row + i, col - i, enemy);
			i = 1;
			
			//Check up-left
			while (checkSquare(legalMoves, row - i, col - i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row - i, col - i, enemy);
			i = 1;
			
			//Check up-right
			while (checkSquare(legalMoves, row - i, col + i, 'e')) {
				i++;
			}
			checkSquare(legalMoves, row - i, col + i, enemy);
			i = 1;
			break;
			
		//If we have a King
		case 'M':
			//Check 3 legal squares below
			for (int a = -1; a < 2; a++) {
				for (int b = -1; b < 2; b++) {
					//Don't check kings current square as it's himself
					if (!(a == 0 && b == 0)) {
						if (checkSquare(legalMoves, row + a, col + b, enemy) || checkSquare(legalMoves, row + a, col + b, 'e'));
					}
				}
			}
			break;
		default:
			// code block
		}
		piece.legalMoves = legalMoves;
		return legalMoves;
	}

	public boolean inCheckmate(String color) {
		//try movePiece on every available move and see if any result in king not being in checkmate after.
		boolean inCheckmate = true;
		Piece[] pieces;
		if (color == "white") {
			pieces = whitePieces;
		} else {
			pieces = blackPieces;
		}
		for (int i = 0; i < pieces.length; i++) {
			Piece a = pieces[i];
			if (!a.destroyed) {
				for (int j = 0; (j < a.legalMoves.size()) && inCheckmate; j++) {
					int[] newLocation = a.legalMoves.get(j);
					if (movePiece(false, a.currentLocation[0], a.currentLocation[1], newLocation[0], newLocation[1])){
						return false;
					}
				}
			}
		}
		return inCheckmate;
	}

	
	/*
	 * Check a square for a piece (Empty or Enemy) and return true
	 */
	public boolean checkSquare(ArrayList<int[]> legalMoves, int row, int col, char color) {
		//System.out.println("Checking " + row + ", " + col + " to see if it's " + z + ".");
		if (row >= 8 || row < 0 || col >= 8 || col < 0) {
			//System.out.println("Out of Bounds.");
			return false;
		} else if (chessBoard[row][col].color == color) {
			//System.out.println("Legal Move.");
			int[] legalMove = new int[2];
			legalMove[0] = row;
			legalMove[1] = col;
			legalMoves.add(legalMove);
			//System.out.println("Found: " + legalMove[0] + ", " + legalMove[1]);
			return true;
		} else {
			//System.out.println("Not Legal Move.");
			return false;
		}
	}
	
	/*
	 * Set up chessboard with all starting pieces in the correct spot
	 */
	public void printPointers() {
		System.out.println("_____All board pointers_____");
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (chessBoard[i][j].type != ' ') {
					System.out.println(chessBoard[i][j].type + " " + i + " " + j + chessBoard[i][j]);
				}
			}
		}
		System.out.println("_____White pointers_____");
		for (int i = 0; i < whitePieces.length; i++) {
			System.out.println(whitePieces[i].type + " " + whitePieces[i].currentLocation[0] + " " + whitePieces[i].currentLocation[1] + " " + whitePieces[i]);
		}
		System.out.println("_____Black pointers_____");
		for (int i = 0; i < blackPieces.length; i++) {
			System.out.println(blackPieces[i].type + " " + blackPieces[i].currentLocation[0] + " " + blackPieces[i].currentLocation[1] + " " + blackPieces[i]);
		}
	}
	
	public void spawnPieces() {
		Piece piece = empty;
		//Create all black pieces
		piece = new Piece('b', "Rook", 'T', 5);
		chessBoard[0][0] = piece;
		piece = new Piece('b', "Rook", 'T', 5);
		chessBoard[0][7] = piece;
		piece = new Piece('b', "Knight", 'K', 3);
		chessBoard[0][1] = piece;
		piece = new Piece('b', "Knight", 'K', 3);
		chessBoard[0][6] = piece;
		piece = new Piece('b', "Bishop", 'B', 3);
		chessBoard[0][2] = piece;
		piece = new Piece('b', "Bishop", 'B', 3);
		chessBoard[0][5] = piece;
		piece = new Piece('b', "Queen", 'Q', 9);
		chessBoard[0][3] = piece;
		piece = new Piece('b', "King", 'M', 0);
		blackKing = piece;
		chessBoard[0][4] = piece;
		for (int i = 0; i < 8; i++) {
			piece = new Piece('b', "Pawn", 'P', 1);
			chessBoard[1][i] = piece;
		}
		
		//Add all white black to collection
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 8; j++) {
				int[] currentLocation = new int[2];
				currentLocation[0] = i;
				currentLocation[1] = j;
				chessBoard[i][j].currentLocation = currentLocation;
				blackPieces[(i * 8) + j] = chessBoard[i][j];
			}
		}
		
		//Create all white pieces
		for (int i = 0; i < 8; i++) {
			piece = new Piece('w', "Pawn", 'P', 1);
			chessBoard[6][i] = piece;
		}
		piece = new Piece('w', "Rook", 'T', 5);
		chessBoard[7][0] = piece;
		piece = new Piece('w', "Rook", 'T', 5);
		chessBoard[7][7] = piece;
		piece = new Piece('w', "Knight", 'K', 3);
		chessBoard[7][1] = piece;
		piece = new Piece('w', "Knight", 'K', 3);
		chessBoard[7][6] = piece;
		piece = new Piece('w', "Bishop", 'B', 3);
		chessBoard[7][2] = piece;
		piece = new Piece('w', "Bishop", 'B', 3);
		chessBoard[7][5] = piece;
		piece = new Piece('w', "Queen", 'Q', 9);
		chessBoard[7][3] = piece;
		piece = new Piece('w', "King", 'M', 0);
		whiteKing = piece;
		chessBoard[7][4] = piece;
		
		//Add all white pieces to collection
		for (int i = 6; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int[] currentLocation = new int[2];
				currentLocation[0] = i;
				currentLocation[1] = j;
				chessBoard[i][j].currentLocation = currentLocation;
				whitePieces[(i - 6) * 8 + j] = chessBoard[i][j];
			}
		}
		
		piece = new Piece('e', "Empty", ' ', 0);
		for (int i = 2; i < 6; i++) {
			for (int j = 0; j < 8; j++) {
				chessBoard[i][j] = piece;
			}
		}
	}
	
	public void printBoard() {
		boolean printBlackSquare = false;
		System.out.println("    a    b    c    d    e    f    g    h");
		for (int i = 0; i < 8; i++) {
			
			if (i != 0) {
				//System.out.println();
				System.out.println();
			}
			System.out.print(8 - i + " ");
			for (int j = 0; j < 8; j++) {
				String printChar = chessBoard[i][j].type + "";
				if (chessBoard[i][j].color == 'w') {
					printChar = printChar.toLowerCase();
				}
				if (printBlackSquare) {
					System.out.print("[ " + printChar + " ]");
				} else {
					System.out.print("{ " + printChar + " }");
					
				}
				printBlackSquare = !printBlackSquare;
			}
			System.out.print(" " + (8 - i));
			printBlackSquare = !printBlackSquare;
		}
		System.out.println("\n    a    b    c    d    e    f    g    h");
		System.out.println();
	}
}
