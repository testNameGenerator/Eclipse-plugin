package testnamegenerator.handlers;

import java.io.BufferedReader;
import java.io.StringReader;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.ITextEditor;

import testnamegenerator.ExistingTest;

import com.sun.xml.ws.util.StringUtils;

public class StringHandler {
	
	public static ITextEditor editor;
	
	public static void setEditor(ITextEditor editor)
	{
		StringHandler.editor = editor;
	}
	
	public static String getLineContents(int lineNumber, IDocument doc, boolean trim)
	{
		if (StringHandler.editor != null) {
			try {
				String contents = doc.get();
				
				BufferedReader bufReader = new BufferedReader(new StringReader(contents));
				String line = null;
				int i = 0;
				
				while((line = bufReader.readLine()) != null) {
					if (i == lineNumber) {
						return (trim) ? line.trim() : line;
					}
					i++;
				}

			} catch(Exception e) {}
		}		
		return "";
	}

	public static String getPreparedTestMethod(String originalText, IDocument doc)
	{
		String tab = getTabChar();
		String lineDelimiter = getLineDelimiter(doc);
		
		String commentText = lineDelimiter + tab + "/**" + lineDelimiter + 
				tab + " * " + originalText.replace("*/", "* /") + lineDelimiter +
				tab + " */" + lineDelimiter;
		
		String methodText = "public function test" + getPreparedMethodName(originalText) + "()" + lineDelimiter +
				tab + "{" + lineDelimiter +
				tab + tab + "$this->markTestIncomplete('implement me...');" + lineDelimiter +
				tab + "}" + lineDelimiter;
		
		return commentText + tab + methodText;
	}

	public static String getPreparedMethodName(String originalText) {
		String methodNameText = originalText.replaceAll("\\P{Alnum}", " ").trim();
		methodNameText        = (methodNameText.isEmpty()) ? "Blank" : toCamelCase(methodNameText).replace(" ", "");
		return methodNameText;
	}

	public static String getTabChar() {
		return EditorHandler.getTabChar();
	}

	public static String toCamelCase(final String init) {
		final StringBuilder ret = new StringBuilder(init.length());
		for (final String word : init.split(" ")) {
			ret.append(StringUtils.capitalize(word));
		}
		return ret.toString();
	}

	public static void replaceInline(ExistingTest updateTest, IDocument doc, String originalText) {
		// replaces without regarding the line because of performance issues.
		// the assumption is made that the string/line is unique, considering that the method definition is unique 
		String contents = doc.get();
		doc.set(contents.replace(updateTest.getMethodName(), getPreparedMethodName(originalText)));
	}

	public static String getLineDelimiter(IDocument doc) {
		String delimiter = System.lineSeparator();

		try {
			delimiter = doc.getLineDelimiter(0);
		} catch (BadLocationException e) {}
		
		return delimiter;
	}
}
