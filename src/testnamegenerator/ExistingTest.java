package testnamegenerator;

public class ExistingTest {

	public static int lineDifferenceBorder = 5; // search tolerance

	protected String MatchedTestName = "";
	protected int MethodLine = 0;
	protected String MethodText = "";
	protected boolean IsFound = false;

	public void setDoesApply(boolean status) {
		this.IsFound = status;
	}
	
	public boolean doesApply() {
		return this.IsFound;
	}

	public void setMethodLine(int line) {
		this.MethodLine = line;
	}

	public void setMethodText(String cursorText) {
		this.MethodText = cursorText;
	}

	public void setMatchedTestName(String string) {
		this.MatchedTestName = string;
	}

	public int getMethodLine() {
		return this.MethodLine;
	}

	public String getMethodText() {
		return this.MethodText;
	}

	public String getMethodName() {
		return this.MatchedTestName;
	}
}
