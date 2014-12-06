package testnamegenerator.handlers;

import testnamegenerator.ExistingTest;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

public class Convertor extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IDocument doc     = EditorHandler.getDocument();
			ITextSelection ts = EditorHandler.getSelection();
			
			StringHandler.setEditor(EditorHandler.getEditor());
			int lineNumber = EditorHandler.getCurrentLineNumber(ts);
			
			if (lineNumber >= 0) {
				String originalText = StringHandler.getLineContents(lineNumber, doc, true);
				
				ExistingTest updateTest = TestHandler.getInstance(lineNumber, doc);
				if (updateTest.doesApply()) {
					StringHandler.replaceInline(updateTest, doc, originalText);
					EditorHandler.focusCursor(updateTest.getMethodLine() + 2, doc, true);
				} else {
					String preparedText = StringHandler.getPreparedTestMethod(originalText, doc);
					TestHandler.generateTestMethod(preparedText, lineNumber, doc);
					EditorHandler.focusCursor(lineNumber + 6, doc, true);
				}				
			}
		} catch(Exception e) {}

		return null;
	}
}
