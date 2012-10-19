package masg.util;

import java.util.Arrays;

public class BitMap implements Comparable<BitMap>{
	
	protected long[] bitMap;
	
	protected final int size;
	
	public BitMap(int size) {
		this.size = size;
		bitMap = new long[size/64 + 1];
	}

	public int size() {
		return size;
	}

	public void clear(int ix) {
		bitMap[ix >> 6] = bitMap[ix >> 6] & ~(1L << (ix & 127));
	}

	public void set(int ix) {
		bitMap[ix >> 6] = bitMap[ix >> 6] | (1L << (ix & 127));
	}
	
	public boolean get(int ix) {
		return (bitMap[ix >> 6] & (1L << (ix & 127))) > 0;
	}

	public int nextClearBit(int i) {
		while(i<size-1) {
			++i;
			if(!get(i)) return i;
		}
		return -1;
	}

	public BitMap get(int ixStart, int newSize) {
		BitMap bm = new BitMap(newSize);
		
		for(int ix = 0;ix<newSize;ix++) {
			if(get(ixStart+ix))
				bm.set(ix);
		}
		
		return bm;
	}
	
	public BitMap clone() {
		BitMap bm = new BitMap(size);
		bm.bitMap = Arrays.copyOf(bitMap, bitMap.length);
		return bm;
	}

	public void and(BitMap otherMap) {
		if(otherMap.bitMap.length>bitMap.length) {
			bitMap = Arrays.copyOf(bitMap, otherMap.bitMap.length);
		}
		
		for(int i=0;i<bitMap.length;i++) {
			bitMap[i] = bitMap[i] & otherMap.bitMap[i];
		}
	}

	public void or(BitMap otherMap) {
		if(otherMap.bitMap.length>bitMap.length) {
			bitMap = Arrays.copyOf(bitMap, otherMap.bitMap.length);
		}
		
		for(int i=0;i<bitMap.length;i++) {
			bitMap[i] = bitMap[i] | otherMap.bitMap[i];
		}
	}
	
	public void xor(BitMap otherMap) {
		if(otherMap.bitMap.length>bitMap.length) {
			bitMap = Arrays.copyOf(bitMap, otherMap.bitMap.length);
		}
		
		for(int i=0;i<bitMap.length;i++) {
			bitMap[i] = bitMap[i] ^ otherMap.bitMap[i];
		}
	}
	
	public void not() {
		for(int i=0;i<bitMap.length;i++) {
			bitMap[i] = ~bitMap[i];
		}
	}
	
	public boolean equals(Object o) {
		if(o instanceof BitMap) {
			BitMap other = ((BitMap) o);
			
			if(other.size == size) {
				for(int i=0;i<bitMap.length;i++) {
					if((bitMap[i] ^ other.bitMap[i])>0)
						return false;
				}
				
				return true;
			}
		}
		return false;
	}
	
	public boolean equalsUpTo(int bitNum, BitMap other) {
		if(other.size >= bitNum && size >= bitNum ) {
			BitMap thisBM = get(0,bitNum);
			BitMap otherBM = other.get(0, bitNum);
			
			return thisBM.equals(otherBM);
		}
		
		return false;
	}
	
	public String toString() {
		String str = "";
		for(int i=0;i<size;i++)
			if(get(i))
				str+="1";
			else
				str+="0";
		return str;
	}

	@Override
	public int compareTo(BitMap o) {
		if(bitMap.length == o.bitMap.length) {
			for(int i=0;i<bitMap.length;i++) {
				if ( bitMap[i] < o.bitMap[i]) {
					return -1;
				}
				else if (bitMap[i] > o.bitMap[i]) {
					return 1;
				}
			}
		}
		else if(bitMap.length < o.bitMap.length) {
			return -1;
		}
		else {
			return 1;
		}
		
		return 0;
	}
	
	public int hashCode() {
		return (int) bitMap[0];
	}
}
