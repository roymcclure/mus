package roymcclure.juegos.mus.common.logic;

public class ByteMessage {

	byte[] word;
	int length;
	
	public static boolean isBitSet(byte bit_index, byte word) {
		// if and between bit_index 
		return (word & (byte)Math.pow(2, bit_index)) != 0;
	}
	
	/*
	// retrieves a chunk of bits from word, starting (from LSB) in offset and spanning length bits
	// returns that as a byte
	public byte getAsByte(int offset, int length) {
		
	}
	
	public byte[] getWord() {
		return word;
	}
	*/
	
}
