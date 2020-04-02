package roymcclure.juegos.mus.cliente.logic;

public class GameData {

	// this object is populated upon initial connection.
	// a packet is sent with all the game info.
	
	public static final int CARDS_PER_SUIT = 12;
	public static final int TOTAL_CARDS = 48;
	
	private int cardsPerSuit;
	private int totalCards;
	private int maxClients;
	private int stonesToRound; // cuantas piedras hacen falta para ganar una vaca?
	private int roundsToCow; // cuantos juegos para ganar una vaca?
	private int cowsToGame; // cuantas vacas hacen falta para ganar la partida?



}
