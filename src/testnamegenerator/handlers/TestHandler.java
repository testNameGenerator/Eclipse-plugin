package testnamegenerator.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.texteditor.ITextEditor;

import testnamegenerator.ExistingTest;

public class TestHandler {

	public static ExistingTest getInstance(int lineNumber, IDocument doc) {

		String cursorText, cursorChar, prevChar = "";
		int i;
		boolean inComment = false;
		boolean wasComment = false; // using this to update only if the current text was in a docblock comment
        Pattern pattern = getTestMethodPattern();

		int lowerChunkBorder = java.lang.Math.max(lineNumber - ExistingTest.lineDifferenceBorder, 0);
		int upperChunkBorder = java.lang.Math.min(lineNumber + 2 + ExistingTest.lineDifferenceBorder, doc.getNumberOfLines());

    	ExistingTest instance = new ExistingTest(); // blank and inactive
		
		for	(i = lowerChunkBorder; i <= upperChunkBorder; i++) {
			cursorText = StringHandler.getLineContents(i, doc, false);
			for(char c : cursorText.toCharArray()) {
				cursorChar = String.valueOf(c);

				// if no additional protection is used, will be buggy when /* and */ are in a string or in commented // line
				if (prevChar.equals("/") & cursorChar.equals("*") & (lineNumber >= i)) {
					inComment = true;
//					System.out.println(lineNumber + " - " + i + " -(" + (lineNumber >= i) + ")- comment found in " + cursorText);
				}

				if (prevChar.equals("*") & cursorChar.equals("/") & (lineNumber <= i)) {
					// take into account starting from the current line, to avoid false positives
					if (inComment & (lineNumber <= i)) {
						wasComment = true;
					}
//					System.out.println(wasComment + " -----> " + inComment);
					inComment = false;
				}
				
				prevChar = cursorChar;
			}
			
			if ((lineNumber < i) & !inComment & wasComment) {
				// get the first function found (current pattern states that the method must start with the "test" string)
		        Matcher matcher = pattern.matcher(cursorText);
		        if (matcher.find()) {
		        	// got it
		        	instance.setMatchedTestName(matcher.group(3).toString());
		        	instance.setMethodLine(i);
		        	instance.setMethodText(cursorText);
		        	instance.setDoesApply(true);
		        	
		        	return instance;
				}
			}
		}
    	return instance;
	}
	
	public static void generateTestMethod(String preparedText, int lineNumber, IDocument doc)
	{
		ITextEditor editor = EditorHandler.getEditor();
		if (editor == null
				|| !editor.isEditable()
				|| !EditorHandler.getPage().getActivePartReference().getId().toString()
						.toLowerCase().contains(".php.editor")) {
			return;
		}
		try {
			IRegion lineInfo = doc.getLineInformation(lineNumber);
			editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
			ISelection sel   = editor.getSelectionProvider().getSelection();
	
			if (sel instanceof TextSelection) {
				final TextSelection textSel = (TextSelection)sel;
				doc.replace(textSel.getOffset(), textSel.getLength(), preparedText);
			}
		} catch(Exception e) {}
	}

	public static Pattern getTestMethodPattern() {
		Pattern pattern = Pattern.compile("public([\\s]*)function([\\s]*)test([\\w]*)\\(", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		return pattern;
	}
}
