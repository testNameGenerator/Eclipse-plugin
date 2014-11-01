package testnamegenerator.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.sun.xml.ws.util.StringUtils;

public class Convertor extends AbstractHandler {
    private EclipseHelperContext eclipseApi;

    public Object execute(ExecutionEvent event) throws ExecutionException {
	try {
	    this.eclipseApi = new EclipseHelperContext();
	    IDocument doc = eclipseApi.getDocument();
	    int lineNumber = eclipseApi.getCurrentLineNumber();

	    if (lineNumber >= 0) {
		String originalText = eclipseApi.getLineContents(lineNumber);
		String preparedText = TextHelper.getPreparedTestMethod(
			originalText, eclipseApi.getTabChar());
		eclipseApi.generateTestMethod(preparedText, lineNumber, doc);
	    }

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return null;
    }

    public static class TextHelper {
	private static final String regex = "\\P{Alnum}";

	/**
	 * @return the regex
	 */
	public static String getRegex() {
	    return regex;
	}

	/**
	 * 
	 * @param init
	 * @return
	 */
	private static String toCamelCase(final String init) {
	    final StringBuilder ret = new StringBuilder(init.length());
	    for (final String word : init.split(" ")) {
		ret.append(StringUtils.capitalize(word));
	    }
	    return ret.toString();
	}

	/**
	 * 
	 * @param originalText
	 * @param tab
	 * @return
	 */
	public static String getPreparedTestMethod(String originalText,
		String tab) {
	    String commentText = "\n" + tab + "/**\n" + tab + " * "
		    + originalText.replace("*/", "* /") + "\n" + tab + " */\n"
		    + tab + "";
	    String methodNameText = originalText.replaceAll(getRegex(), " ")
		    .trim();
	    methodNameText = (methodNameText.isEmpty()) ? "Blank"
		    : toCamelCase(methodNameText);
	    String methodText = "public function test"
		    + methodNameText.replace(" ", "") + "() {\n" + tab + ""
		    + tab + "$this->markTestIncomplete('implement me...');\n"
		    + tab + "}\n";
	    return commentText + methodText;
	}
    }

    public class EclipseHelperContext {

	/**
	 * 
	 * @param lineNumber
	 * @return
	 */
	public String getLineContents(int lineNumber) {
	    IDocument doc = this.getDocument();
	    ITextEditor editor = this.getEditor();
	    String selection = null;
	    if (editor != null) {
		try {
		    selectAndRevealText(lineNumber, doc, editor);
		    ISelection sel = editor.getSelectionProvider()
			    .getSelection();

		    if (sel instanceof TextSelection) {
			final TextSelection textSel = (TextSelection) sel;
			selection = (textSel.getText().toString()).trim();
		    }
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	    return selection;
	}

	/**
	 * 
	 * @return activePage or null
	 */
	private IWorkbenchPage getPage() {
	    return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		    .getActivePage();
	}

	private ITextEditor getEditor() {
	    try {
		IEditorPart part = this.getPage().getActiveEditor();
		if (part instanceof ITextEditor) {
		    return (ITextEditor) part;
		}
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    return null;
	}

	private ITextSelection getSelection() {
	    return (org.eclipse.jface.text.ITextSelection) this.getPage()
		    .getSelection();
	}

	public int getCurrentLineNumber() {
	    return this.getSelection().getStartLine();
	}

	private IDocument getDocument() {
	    ITextEditor editor = this.getEditor();
	    if (editor != null) {
		IDocument doc = editor.getDocumentProvider().getDocument(
			editor.getEditorInput());
		if (doc != null) {
		    return doc;
		}
	    }
	    return null;
	}

	public String getTabChar() {
	    Boolean useSpaces = this.useSpacesForTabs();
	    int tabWidth = Platform.getPreferencesService().getInt(
		    "org.eclipse.ui.editors", "tabWidth", 4, null);
	    return (!useSpaces) ? "\t" : String
		    .format("%" + tabWidth + "s", "");
	}

	public Boolean useSpacesForTabs() {
	    return Platform.getPreferencesService().getBoolean(
		    "org.eclipse.ui.editors", "spacesForTabs", false, null);
	}

	public void generateTestMethod(String preparedText, int lineNumber,
		IDocument doc) {
	    ITextEditor editor = this.getEditor();
	    if (!this.isValidEditor("php")) {
		return;
	    }
	    try {
		selectAndRevealText(lineNumber, doc, editor);
		ISelection sel = editor.getSelectionProvider().getSelection();

		if (sel instanceof TextSelection) {
		    doc = this.insertMethod((TextSelection) sel, preparedText,
			    doc);

		    selectAndRevealText(lineNumber + 5, doc, editor);
		}
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	private void selectAndRevealText(int lineNumber, IDocument doc,
		ITextEditor editor) {

	    IRegion lineInfo;
	    try {
		lineInfo = doc.getLineInformation(lineNumber);
		editor.selectAndReveal(lineInfo.getOffset(),
			lineInfo.getLength());
	    } catch (BadLocationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	public IDocument insertMethod(TextSelection selection, String text,
		IDocument document) {
	    try {
		document.replace(selection.getOffset(), selection.getLength(),
			text);
	    } catch (BadLocationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    return document;
	}

	private Boolean isValidEditor(String languageSyntax) {
	    Boolean isValid;
	    ITextEditor editor = this.getEditor();
	    if (editor == null
		    || !editor.isEditable()
		    || !this.getPage()
			    .getActivePartReference()
			    .getId()
			    .toString()
			    .toLowerCase()
			    .contains(
				    languageSyntax.trim().toString()
					    .toLowerCase()
					    + ".editor")) {
		isValid = false;
	    } else {
		isValid = true;
	    }
	    return isValid;
	}
    }
}