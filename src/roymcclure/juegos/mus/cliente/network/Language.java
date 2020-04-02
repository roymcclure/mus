package roymcclure.juegos.mus.cliente.network;

public class Language {

	// actions.
	// todo: this should not BE here.
	// defined in network "language"
	// so maybe a class defining the acceptable "language"
	// isClientMessageValid() -> mensaje se adscribe a las normas
	// isClientMessageConsistent() -> mensaje tiene sentido en el contexto (game state)
	// ej: no tiene sentido envidar si estamos en la ronda de mus
	
	// client actions
	private static final int 	PASS = 0,
								ENVITE = 1,
								ACCEPT = 2,
								ORDAGO = 3,
								REQUEST_SEAT = 5;
	
	// 
	// private static final int
	
	// max clients , stones to game - should be defined by the server. retrieved upon initial connection. 

	
	
}
