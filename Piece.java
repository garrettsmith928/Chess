import java.util.ArrayList;
import java.util.Arrays;

public class Piece {
	char color;
	char type;
	String name;
	int points;
	boolean destroyed = false;
	int[] currentLocation = new int[2];
	ArrayList<int[]> legalMoves = new ArrayList<>();
	
	public Piece(char color, String name, char type, int points) {
		this.color = color;
		this.name = name;
		this.type = type;
		this.points = points;
	}
	
	/*
	 * Creates a copy
	 */
	public Piece(Piece piece) {
		this.color = piece.color;
		this.name = piece.name;
		this.type = piece.type;
		this.destroyed = piece.destroyed;
		currentLocation = Arrays.copyOf(piece.currentLocation, piece.currentLocation.length);
		for(int i = 0; i < piece.legalMoves.size(); i++) {
			legalMoves.add(Arrays.copyOf(piece.legalMoves.get(i), piece.legalMoves.get(i).length));
		}
	}
	
	public void promotion(String name, char type) {
		this.name = name;
		this.type = type;
	}
	
	public void resetPiece(Piece piece) {
		this.color = piece.color;
		this.name = piece.name;
		this.type = piece.type;
		this.destroyed = piece.destroyed;
		currentLocation = Arrays.copyOf(piece.currentLocation, piece.currentLocation.length);
		legalMoves.clear();
		for(int i = 0; i < piece.legalMoves.size() && i < 10; i++) {
			legalMoves.add(Arrays.copyOf(piece.legalMoves.get(i), piece.legalMoves.get(i).length));
		}
	}
	
	public String getColorString() {
		if (color == 'w') {
			return "white";
		} else if (color == 'b'){
			return "black";
		} else {
			return "";
		}
	}
	
	public void updateLocation(int x, int y) {
		currentLocation[0] = x;
		currentLocation[1] = y;
	}
	
	public String printLegalMoves() {
		if (color == 'e') {
			return "Empty";
		}
		String ret = "Legal moves for " + color + " " + name + " located at " + coordsToChessCoords(currentLocation[0], currentLocation[1]);
		for (int i = 0; i < legalMoves.size(); i++) {
			ret += "\n" + coordsToChessCoords(legalMoves.get(i)[0], legalMoves.get(i)[1]);
		}
		return ret + "\n";
	}
	
	public static String coordsToChessCoords(int x, int y) {
		return "" + numberToLetter(y) + (8 - x);
	}

	public static char numberToLetter(int x) {
		switch (x) {
		case (0):
			return 'a';
		case (1):
			return 'b';
		case (2):
			return 'c';
		case (3):
			return 'd';
		case (4):
			return 'e';
		case (5):
			return 'f';
		case (6):
			return 'g';
		case (7):
			return 'h';
		default:
			return 'z';
		}
	}
	
	public String toString() {
		return color + " " + type + " ";
	}
}
