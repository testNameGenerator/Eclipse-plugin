package testnamegenerator.handlers;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class EditorHandler {
	public static ITextEditor editor;
	
	public static void setEditor(ITextEditor editor)
	{
		StringHandler.editor = editor;
	}

	public static void focusCursor(int lineNumber, IDocument doc, Boolean trimFirstTwoTabs) {
		if (StringHandler.editor != null) {
			IRegion lineInfo;
			
			try {
				lineInfo = doc.getLineInformation(lineNumber);
				StringHandler.editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
				
				int offset = (!trimFirstTwoTabs) ? 0 : ((useSpacesForTabs()) ? 2 * getTabChar().length() : 2);
				StringHandler.editor.selectAndReveal(lineInfo.getOffset() + offset, lineInfo.getLength() - offset); // remove tabs from selection
				
			} catch (BadLocationException e) {}
		}
	}
	
	public static Boolean useSpacesForTabs()
	{
		return Platform.getPreferencesService().getBoolean("org.eclipse.ui.editors", "spacesForTabs", false, null);
	}
	
	public static String getTabChar()
	{
		Boolean useSpaces = useSpacesForTabs();
		int tabWidth      = Platform.getPreferencesService().getInt("org.eclipse.ui.editors", "tabWidth", 4, null);
		return (!useSpaces) ? "\t" : String.format("%"+tabWidth+"s", "");
	}
	
	public static ITextSelection getSelection()
	{
		return (org.eclipse.jface.text.ITextSelection) EditorHandler.getPage().getSelection();
	}
	
	public static int getCurrentLineNumber(ITextSelection textSelection)
	{
		return textSelection.getStartLine();
	}
	
	public static ITextEditor getEditor()
	{
		try {
			IEditorPart part = EditorHandler.getPage().getActiveEditor();
			if (part instanceof ITextEditor) {
				return (ITextEditor)part;
			}
		} catch (Exception ex) {}
		return null;
	}
	
	public static IDocument getDocument()
	{
		ITextEditor editor = EditorHandler.getEditor();
		if (editor != null) {
			IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			if (doc != null) {
				return doc;
			}
		}
		return null;
	}
	
	public static IWorkbenchPage getPage()
	{
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
}
