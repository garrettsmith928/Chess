import java.awt.GridLayout;
import java.util.ArrayList;
import java.awt.event.*;
import java.io.IOException;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Chess extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8705860666943430823L;

	//Create board and scanner
	static Board chessBoard;
	
    //GUI
    static JFrame f = new JFrame("panel");
    static JPanel p = new JPanel();
    static Image img;
    static int gridSize;
    static JButton[][] chessBoardImages = new JButton[8][8];
    static String previousButtonCoords = "0";
    static int[] clickMoveCoords1 = {-1, -1};
    static int[] clickMoveCoords2 = new int[2];
    
    static Icon[] icons = new Icon[12];
    
    static String color = "white";
    static int blackWins = 0;
    static int whiteWins = 0;
    
    static ArrayList<String> playerMovesName = new ArrayList<String>();
    static ArrayList<int[]> playerMovesCoords = new ArrayList<int[]>();
    static ArrayList<Integer> saveStates = new ArrayList<Integer>();
	
	public static void main(String[] args) throws IOException {
		initializeBoard();
		chessBoard = new Board();
		drawBoard();
	}
	
	public static void resetGame() {
		System.out.println("Previous game move history: ");
		for (int i = 0; i < playerMovesCoords.size(); i++) {
			int[] moveHistory = playerMovesCoords.get(i);
			if (i % 2 == 0) {
				color = "White";
			} else {
				color = "Black";
			}
			if (i == playerMovesCoords.size() - 1) {
				if (color == "White") {
					System.out.println(color + " move: " + playerMovesName.get(i) + " from " + coordsToChessCoords(moveHistory[0], moveHistory[1]) + " to " + coordsToChessCoords(moveHistory[2], moveHistory[3]) + " checkmating black.");
				} else {
					System.out.println(color + " move: " + playerMovesName.get(i) + " from " + coordsToChessCoords(moveHistory[0], moveHistory[1]) + " to " + coordsToChessCoords(moveHistory[2], moveHistory[3]) + " checkmating white.");
				}
			} else {
				System.out.println(color + " move: " + playerMovesName.get(i) + " from " + coordsToChessCoords(moveHistory[0], moveHistory[1]) + " to " + coordsToChessCoords(moveHistory[2], moveHistory[3]) + ".");
			}
		}
		System.out.println(color + " wins!");
		chessBoard.printBoard();
		System.out.println("\n\n");
		playerMovesName.clear();
		playerMovesCoords.clear();
		chessBoard = new Board();
		color = "white";
	}
		
	public static boolean clickMove(int a, int b, int c, int d) throws IOException {
		boolean pieceMoved = false;
		int castled = 0;
		//Check for any castling
		if ((b == 4 && d == 0) && ((a == 0 && c == 0) || (a == 7 && c == 7))) {
			pieceMoved = chessBoard.movePiece(true, -1, 0, 0, 0);
			castled = 1;
		} else if ((b == 4 && d == 7) && ((a == 0 && c == 0) || (a == 7 && c == 7))) {
			pieceMoved = chessBoard.movePiece(true, 0, -1, 0, 0);
			castled = 2;
		//Check for actual move
		} else {
			pieceMoved = (chessBoard.movePiece(true, a, b, c, d));
		}
		if (pieceMoved) {
			int[] tempPlayerMoves = {a, b, c, d};
			//System.out.println(tempPlayerMoves[0] + " " + tempPlayerMoves[1] + " " + tempPlayerMoves[2] + " " + tempPlayerMoves[3]);
			playerMovesCoords.add(tempPlayerMoves);
			if (castled == 1) {
				playerMovesName.add("Castled Left");
			} else if (castled == 2) {
				playerMovesName.add("Castled Right");
			} else {
				playerMovesName.add(chessBoard.chessBoard[c][d].name);
			}
			if (chessBoard.whitesTurn) {
				color = "white";
			} else {
				color = "black";
			}
			if (chessBoard.inCheck) {
				if (chessBoard.inCheckmate(color)) {
					chessBoard.checkmate = true;
					if (color.equals("white")) {
						System.out.println("Checkmate! Black wins!");
						blackWins++;
					} else {
						System.out.println("Checkmate! White wins!");
						whiteWins++;
					}
					System.out.println("White wins: " + whiteWins + ". Black wins: " + blackWins);
					resetGame();
				} else {
					System.out.println("Your king is in check!");
				}
			}
			if (color.equals("white")) {
				System.out.println("Whites turn to move!");
			} else {
				System.out.println("Blacks turn to move!");
			}
			drawBoard();
		}
		return pieceMoved;
	}
	
    static ActionListener listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JButton) {
            	String coords = ((JButton) e.getSource()).getName();
            	//If it's the same as the last button reset the square
            	if (coords.equals(previousButtonCoords)) {
            		if (clickMoveCoords1[0] != -1) {
                    	if ((clickMoveCoords1[0] + clickMoveCoords1[1]) % 2 == 0) {
                    		chessBoardImages[clickMoveCoords1[0]][clickMoveCoords1[1]].setBackground(Color.black);
                    	} else {
                    		chessBoardImages[clickMoveCoords1[0]][clickMoveCoords1[1]].setBackground(Color.white);
                    	}
                    	previousButtonCoords = "0";
            		}
                	clickMoveCoords1[0] = -1;
                	clickMoveCoords1[1] = -1;
                	previousButtonCoords = "0";
                //Otherwise...
            	} else {
            		//If it's the first button press make that the moving piece
                	if (clickMoveCoords1[0] == -1) {
                    	clickMoveCoords1[0] = Integer.parseInt(coords.charAt(0) + "");
                    	clickMoveCoords1[1] = Integer.parseInt(coords.charAt(1) + "");
                    	if (chessBoard.chessBoard[clickMoveCoords1[0]][clickMoveCoords1[1]].getColorString() == color) {
                        	((JButton) e.getSource()).setBackground(new Color(150, 255, 255));
                        	previousButtonCoords = coords;
                    	} else {
                        	clickMoveCoords1[0] = -1;
                        	clickMoveCoords1[1] = -1;
                        	previousButtonCoords = "0";
                    	}
                    	//f.show();
                    //If it's the second button press, try to move the piece there.
                	} else {
                    	clickMoveCoords2[0] = Integer.parseInt(coords.charAt(0) + "");
                    	clickMoveCoords2[1] = Integer.parseInt(coords.charAt(1) + "");
                    	try {
                    		clickMove(clickMoveCoords1[0], clickMoveCoords1[1], clickMoveCoords2[0], clickMoveCoords2[1]);
                    		//System.out.println("Trying to move piece at " + clickMoveCoords1[0] + " " + clickMoveCoords1[1] + " to " + clickMoveCoords2[0] + " " + clickMoveCoords2[1]);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
                    	if ((clickMoveCoords1[0] + clickMoveCoords1[1]) % 2 == 0) {
                    		chessBoardImages[clickMoveCoords1[0]][clickMoveCoords1[1]].setBackground(Color.black);
                    	} else {
                    		chessBoardImages[clickMoveCoords1[0]][clickMoveCoords1[1]].setBackground(Color.white);
                    	}
                    	clickMoveCoords1[0] = -1;
                    	clickMoveCoords1[1] = -1;
                    	previousButtonCoords = "0";
                	}
            	}
            }
        }
    };
	
	public static void initializeBoard() throws IOException {
		//Set screen and grid size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;
		gridSize = (screenHeight - 50) / 8;
		
		//Load all images from drive
		grabImages();
		
		p.setLayout(new GridLayout(8,8,0,0));
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
        		JButton bt1 = new JButton();
        		bt1.addActionListener(listener);
            	if ((i + j) % 2 == 0) {
            		bt1.setBackground(Color.black);
            	} else {
            		bt1.setBackground(Color.white);
            	}
            	bt1.setName(String.valueOf(i + "" + j));
        		chessBoardImages[i][j] = bt1;
        		p.add(bt1);
            }
        }
        if (screenHeight > screenWidth) {
        	f.setSize(screenWidth - 50, screenWidth - 50);
        } else {
        	f.setSize(screenHeight - 50, screenHeight - 50);
        }
        p.setBackground(Color.black);
        // add panel to frame
        f.add(p);
        // set the size of frame
        //Makes the program end when the window is closed, otherwise it runs by default
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //Display the window
        f.setVisible(true);
	}
	
	public static void grabImages() throws IOException{
		icons[0]  = loadImage("black", "Bishop");
		icons[1]  = loadImage("black", "King");
		icons[2]  = loadImage("black", "Knight");
		icons[3]  = loadImage("black", "Pawn");
		icons[4]  = loadImage("black", "Queen");
		icons[5]  = loadImage("black", "Rook");
		icons[6]  = loadImage("white", "Bishop");
		icons[7]  = loadImage("white", "King");
		icons[8]  = loadImage("white", "Knight");
		icons[9]  = loadImage("white", "Pawn");
		icons[10] = loadImage("white", "Queen");
		icons[11] = loadImage("white", "Rook");
	}
	
	public static Icon loadImage(String color, String name) throws IOException{
		String localImage = "resources/" + color + name + ".png";
		Image img = ImageIO.read(Chess.class.getResource(localImage));
        img = img.getScaledInstance(gridSize, gridSize, java.awt.Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(img);
        return icon;
	}
	
	/*
	 * Returns the correct icon image to draw according to the piece color and name
	 */
	public static Icon drawImage(String color, String name) {
		int offset = 0;
		if (color == "white") offset = 6;
		switch (name) {
			case ("Bishop"):
				return icons[0 + offset];
			case ("King"):
				return icons[1 + offset];
			case ("Knight"):
				return icons[2 + offset];
			case ("Pawn"):
				return icons[3 + offset];
			case ("Queen"):
				return icons[4 + offset];
			case ("Rook"):
				return icons[5 + offset];
		}
		return null;
	}
	
	/*
	 * Redraws the board after a move
	 */
	public static void drawBoard() throws IOException {
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
            	JButton bt1 = chessBoardImages[i][j];
            	if (chessBoard.chessBoard[i][j].name != "Empty") {
                    bt1.setIcon(drawImage(chessBoard.chessBoard[i][j].getColorString(), chessBoard.chessBoard[i][j].name));
            	} else {
            		bt1.setIcon(null);
            	}
            	bt1.setSize(gridSize, gridSize);
            }   
        }
	}

	public static int[] playerMoveToBoardArray(String move) {
		int currentCol = verifyCharacter(move.charAt(0));
		int currentRow = verifyCharacter(move.charAt(1));
		int newCol = verifyCharacter(move.charAt(6));
		int newRow = verifyCharacter(move.charAt(7));
		if (currentCol != -1 && currentRow != -1 && newRow != -1 && newCol != -1) {
			currentRow = chessCoordsToArrayCoords(currentRow);
			newRow = chessCoordsToArrayCoords(newRow);
			int[] moveCoords = {currentRow, currentCol, newRow, newCol};
			return moveCoords;
		} else {
			int[] moveCoords = {0};
			return moveCoords;
		}
		
	}
	
	public static int chessCoordsToArrayCoords(int x) {
		return 7 - x;
	}
	
	public static String coordsToChessCoords(int x, int y) {
		return "" + numberToLetter(y) + (8 - x);
	}
	
	public static int verifyCharacter(char c) {
		if (Character.isDigit(c)) {
			int x = Integer.parseInt(c + "");
			if (x > 0 && x < 9) {
				return x - 1;
			} else {
				return -1;
			}
		} else if ((c >= 65 && c <= 72) || (c >= 97 && c <= 104)) {
			return letterToNumber(c);
		} else {
			return -1;
		}
	}
	
	public static int letterToNumber(char x) {
		switch (x) {
		case ('a'):
			return 0;
		case ('b'):
			return 1;
		case ('c'):
			return 2;
		case ('d'):
			return 3;
		case ('e'):
			return 4;
		case ('f'):
			return 5;
		case ('g'):
			return 6;
		case ('h'):
			return 7;
		default:
			return -1;
		}
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
}