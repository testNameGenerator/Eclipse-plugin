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

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class Convertor extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public Convertor() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		
		// get cursor
	    ISelection selection = page.getSelection();
        ITextSelection ts  = (org.eclipse.jface.text.ITextSelection) selection;
        
        // get document
        try {               
            IEditorPart part = page.getActiveEditor();
            if ( part instanceof ITextEditor ) {
                final ITextEditor editor = (ITextEditor)part;
                IDocumentProvider prov = editor.getDocumentProvider();
                IDocument doc = prov.getDocument( editor.getEditorInput() );
                
                if (doc != null) {
                	// mark and prepare
	                int lineNumber = ts.getStartLine();
	    	        IRegion lineInfo = doc.getLineInformation(lineNumber);
	    	        editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
	    	        
	    	        ISelection sel = editor.getSelectionProvider().getSelection();
	    	        if ( sel instanceof TextSelection ) {
	    	            final TextSelection textSel = (TextSelection)sel;
	    	            String originalText = ((String) textSel.getText()).trim();
	    	            
	    	            // replace
	    	            String commentText = "\t/**\n\t * " + originalText.replace("*/", "* /") + "\n\t */\n\t";
	    	            String methodNameText = originalText.replaceAll("/[^A-Za-z0-9 _]/", "");
	    	            methodNameText = toCamelCase(methodNameText).replace(" ", "");
	    	            
	    	            String methodText = "public function test" + methodNameText + "() " + 
	    	            		"{\n\t\t$this->markTestIncomplete('implement me...');\n\t}\n\n";
	    	            doc.replace(textSel.getOffset(), textSel.getLength(), commentText + methodText);
	    	            
	    	            // select temporary text
		    	        lineInfo = doc.getLineInformation(lineNumber + 4);
		    	        editor.selectAndReveal(lineInfo.getOffset() + 2, lineInfo.getLength() - 2); // remove tabs from selection
	    	        }
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
		return null;
	}
	
	public static String toCamelCase(final String init) {
	    if (init==null)
	        return null;

	    final StringBuilder ret = new StringBuilder(init.length());

	    for (final String word : init.split(" ")) {
	        if (!word.isEmpty()) {
	            ret.append(word.substring(0, 1).toUpperCase());
	            ret.append(word.substring(1).toLowerCase());
	        }
	        if (!(ret.length()==init.length()))
	            ret.append(" ");
	    }

	    return ret.toString();
	}
}
