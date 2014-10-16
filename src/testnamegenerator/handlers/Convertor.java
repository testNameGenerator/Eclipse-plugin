package testnamegenerator.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.sun.xml.internal.ws.util.StringUtils;

public class Convertor extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IDocument doc     = this.getDocument();
			ITextSelection ts = this.getSelection();
			int lineNumber    = this.getCurrentLineNumber(ts);
			
			if (lineNumber >= 0) {
				String originalText = this.getLineContents(lineNumber, doc);
				String preparedText = this.getPreparedTestMethod(originalText);
				this.generateTestMethod(preparedText, lineNumber, doc);
			}

		} catch(Exception e) {}

		return null;
	}
	
	private void generateTestMethod(String preparedText, int lineNumber, IDocument doc)
	{
		ITextEditor editor = this.getEditor();
		if (editor == null) {
			return;
		}
		try {
			IRegion lineInfo = doc.getLineInformation(lineNumber);
			editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
			ISelection sel   = editor.getSelectionProvider().getSelection();
	
			if (sel instanceof TextSelection) {
				final TextSelection textSel = (TextSelection)sel;
				doc.replace(textSel.getOffset(), textSel.getLength(), preparedText);
	
				// select temporary text
				lineInfo = doc.getLineInformation(lineNumber + 5);

				Boolean useSpaces = this.useSpacesForTabs();
				int offset = (useSpaces) ? 2 * this.getTabChar().length() : 2;
				editor.selectAndReveal(lineInfo.getOffset() + offset, lineInfo.getLength() - offset); // remove tabs from selection
			}
		} catch(Exception e) {}
	}
	
	private String getPreparedTestMethod(String originalText)
	{
		String tab = this.getTabChar();
		String commentText    = "\n" + tab + "/**\n" + tab + " * " + originalText.replace("*/", "* /") + "\n" + tab + " */\n" + tab + "";
		String methodNameText = originalText.replaceAll("\\P{Alnum}", " ").trim();
		methodNameText        = (methodNameText.isEmpty()) ? "Blank" : this.toCamelCase(methodNameText);
		String methodText     = "public function test" + methodNameText.replace(" ", "") + "() {\n" + tab + "" + tab + "$this->markTestIncomplete('implement me...');\n" + tab + "}\n";
		return commentText + methodText;
	}
	
	private String getLineContents(int lineNumber, IDocument doc)
	{
		ITextEditor editor = this.getEditor();
		if (editor != null) {
			try {
				IRegion lineInfo = doc.getLineInformation(lineNumber);
				editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
				ISelection sel   = editor.getSelectionProvider().getSelection();

				if (sel instanceof TextSelection) {
					final TextSelection textSel = (TextSelection)sel;
					return ((String) textSel.getText()).trim();
				}
			} catch(Exception e) {}
		}		
		return "";
	}
	
	private ITextSelection getSelection()
	{
		return (org.eclipse.jface.text.ITextSelection) this.getPage().getSelection();
	}
	
	private int getCurrentLineNumber(ITextSelection textSelection)
	{
		return textSelection.getStartLine();
	}
	
	private ITextEditor getEditor()
	{
		try {
			IEditorPart part = this.getPage().getActiveEditor();
			if (part instanceof ITextEditor) {
				return (ITextEditor)part;
			}
		} catch (Exception ex) {}
		return null;
	}
	
	private IDocument getDocument()
	{
		ITextEditor editor = this.getEditor();
		if (editor != null) {
			IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			if (doc != null) {
				return doc;
			}
		}
		return null;
	}
	
	private IWorkbenchPage getPage()
	{
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
	
	private Boolean useSpacesForTabs()
	{
		return Platform.getPreferencesService().getBoolean("org.eclipse.ui.editors", "spacesForTabs", false, null);
	}
	
	private String getTabChar()
	{
		Boolean useSpaces = this.useSpacesForTabs();
		int tabWidth      = Platform.getPreferencesService().getInt("org.eclipse.ui.editors", "tabWidth", 4, null);
		return (!useSpaces) ? "\t" : String.format("%"+tabWidth+"s", "");
	}

	private String toCamelCase(final String init) {
		final StringBuilder ret = new StringBuilder(init.length());
		for (final String word : init.split(" ")) {
			ret.append(StringUtils.capitalize(word));
		}
		return ret.toString();
	}
}
