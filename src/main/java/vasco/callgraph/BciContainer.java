package vasco.callgraph;

public class BciContainer {
	public BciContainer(int ci, int bci) {
		this.bci = bci;
		this.callerIndex = ci;
	}
	private int callerIndex;
	private int bci;
	public int getCallerIndex() {
		return callerIndex;
	}
	public int getBci() {
		return bci;
	}
}
