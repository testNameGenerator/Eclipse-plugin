package testnamegenerator.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import com.sun.xml.internal.ws.util.StringUtils;

public class Convertor extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbench wb        = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page  = win.getActivePage();
		ISelection selection = page.getSelection();
		ITextSelection ts    = (org.eclipse.jface.text.ITextSelection) selection;

		try {
			IEditorPart part = page.getActiveEditor();

			if (part instanceof ITextEditor) {
				final ITextEditor editor = (ITextEditor)part;
				IDocumentProvider prov   = editor.getDocumentProvider();
				IDocument doc            = prov.getDocument(editor.getEditorInput());

				if (doc != null) {
					// mark and prepare
					int lineNumber   = ts.getStartLine();
					IRegion lineInfo = doc.getLineInformation(lineNumber);

					editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
					ISelection sel   = editor.getSelectionProvider().getSelection();

					if (sel instanceof TextSelection) {
						final TextSelection textSel = (TextSelection)sel;
						String originalText         = ((String) textSel.getText()).trim();

						// replace
						String commentText    = "\n\t/**\n\t * " + originalText.replace("*/", "* /") + "\n\t */\n\t";
						String methodNameText = originalText.replaceAll("\\P{Alnum}", " ").trim();
						methodNameText        = (methodNameText.isEmpty()) ? "Blank" : toCamelCase(methodNameText);
						String methodText     = "public function test" + methodNameText.replace(" ", "") + "() {\n\t\t$this->markTestIncomplete('implement me...');\n\t}\n\n";

						doc.replace(textSel.getOffset(), textSel.getLength(), commentText + methodText);

						// select temporary text
						lineInfo = doc.getLineInformation(lineNumber + 5);
						editor.selectAndReveal(lineInfo.getOffset() + 2, lineInfo.getLength() - 2); // remove tabs from selection
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String toCamelCase(final String init) {
		final StringBuilder ret = new StringBuilder(init.length());

		for (final String word : init.split(" ")) {
			ret.append(StringUtils.capitalize(word));
		}

		return ret.toString();
	}
}
