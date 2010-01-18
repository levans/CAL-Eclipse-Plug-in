/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Business Objects Software Limited - I made getReplaceOffset and getReplaceEndOffset 
 *     protected so I could override them. 
 *******************************************************************************/
package org.openquark.cal.eclipse.ui.text;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.InclusivePositionUpdater;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariable;

/**
 * A template completion proposal.
 * <p>
 * Clients may subclass.</p>
 *
 * @since 3.0
 */
public class TemplateProposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3 {

    private final Template fTemplate;
    private final TemplateContext fContext;
    private final Image fImage;
    private final IRegion fRegion;
    private final int fRelevance;

    private IRegion fSelectedRegion; // initialized by apply()
    private String fDisplayString;
    private InclusivePositionUpdater fUpdater;
    private IInformationControlCreator fInformationControlCreator;

    /**
     * Creates a template proposal with a template and its context.
     *
     * @param template  the template
     * @param context   the context in which the template was requested.
     * @param region    the region this proposal is applied to
     * @param image     the icon of the proposal.
     */
    public TemplateProposal(Template template, TemplateContext context, IRegion region, Image image) {
        this(template, context, region, image, 0);
    }

    /**
     * Creates a template proposal with a template and its context.
     *
     * @param template  the template
     * @param context   the context in which the template was requested.
     * @param image     the icon of the proposal.
     * @param region    the region this proposal is applied to
     * @param relevance the relevance of the proposal
     */
    public TemplateProposal(Template template, TemplateContext context, IRegion region, Image image, int relevance) {
        Assert.isNotNull(template);
        Assert.isNotNull(context);
        Assert.isNotNull(region);

        fTemplate= template;
        fContext= context;
        fImage= image;
        fRegion= region;

        fDisplayString= null;

        fRelevance= relevance;
    }

    /**
     * Sets the information control creator for this completion proposal.
     *
     * @param informationControlCreator the information control creator
     * @since 3.1
     */
    public final void setInformationControlCreator(IInformationControlCreator informationControlCreator) {
        fInformationControlCreator= informationControlCreator;
    }

    /**
     * Returns the template of this proposal.
     *
     * @return the template of this proposal
     * @since 3.1
     */
    protected final Template getTemplate() {
        return fTemplate;
    }

    /**
     * Returns the context in which the template was requested.
     *
     * @return the context in which the template was requested
     * @since 3.1
     */
    protected final TemplateContext getContext() {
        return fContext;
    }

    /*
     * @see ICompletionProposal#apply(IDocument)
     */
    public final void apply(IDocument document) {
        // not called anymore
    }

    /**
     * Inserts the template offered by this proposal into the viewer's document
     * and sets up a <code>LinkedModeUI</code> on the viewer to edit any of
     * the template's unresolved variables.
     *
     * @param viewer {@inheritDoc}
     * @param trigger {@inheritDoc}
     * @param stateMask {@inheritDoc}
     * @param offset {@inheritDoc}
     */
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {

        IDocument document= viewer.getDocument();
        try {
            fContext.setReadOnly(false);
            TemplateBuffer templateBuffer;
            try {
                TemplateTranslator translator = new TemplateTranslator();
                templateBuffer = translator.translate(fTemplate);
            } catch (TemplateException e1) {
                fSelectedRegion= fRegion;
                return;
            }

            int start= getReplaceOffset();
            int end= Math.max(getReplaceEndOffset(), offset);

            // insert template string
            String templateString= templateBuffer.getString();
            document.replace(start, end - start, templateString);


            // translate positions
            LinkedModeModel model = new LinkedModeModel();
            TemplateVariable[] variables = templateBuffer.getVariables();
            boolean hasPositions = false;
            for (final TemplateVariable variable : variables) {

                if (variable.isUnambiguous()) {
                    continue;
                }

                LinkedPositionGroup group= new LinkedPositionGroup();

                int[] offsets= variable.getOffsets();
                int length= variable.getLength();

                String[] values= variable.getValues();
                ICompletionProposal[] proposals= new ICompletionProposal[values.length];
                for (int j = 0; j < values.length; j++) {
                    ensurePositionCategoryInstalled(document, model);
                    Position pos = new Position(offsets[0] + start, length);
                    document.addPosition(getCategory(), pos);
                    proposals[j] = new PositionBasedCompletionProposal(values[j], pos, length);
                }

                for (int j = 0; j != offsets.length; j++) {
                    if (j == 0 && proposals.length > 1) {
                        group.addPosition(new ProposalPosition(document, offsets[j] + start, length, proposals));
                    } else {
                        group.addPosition(new LinkedPosition(document, offsets[j] + start, length));
                    }
                }

                model.addGroup(group);
                hasPositions= true;
            }

            if (hasPositions) {
                model.forceInstall();
                LinkedModeUI ui= new LinkedModeUI(model, viewer);
                ui.setExitPosition(viewer, getCaretOffset(templateBuffer) + start, 0, Integer.MAX_VALUE);
                ui.enter();

                fSelectedRegion= ui.getSelectedRegion();
            } else {
                ensurePositionCategoryRemoved(document);
                fSelectedRegion= new Region(getCaretOffset(templateBuffer) + start, 0);
            }

        } catch (BadLocationException e) {
            openErrorDialog(viewer.getTextWidget().getShell(), e);
            ensurePositionCategoryRemoved(document);
            fSelectedRegion= fRegion;
        } catch (BadPositionCategoryException e) {
            openErrorDialog(viewer.getTextWidget().getShell(), e);
            fSelectedRegion= fRegion;
        }

    }

    private void ensurePositionCategoryInstalled(final IDocument document, LinkedModeModel model) {
        if (!document.containsPositionCategory(getCategory())) {
            document.addPositionCategory(getCategory());
            fUpdater= new InclusivePositionUpdater(getCategory());
            document.addPositionUpdater(fUpdater);

            model.addLinkingListener(new ILinkedModeListener() {

                /*
                 * @see org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel, int)
                 */
                public void left(LinkedModeModel environment, int flags) {
                    ensurePositionCategoryRemoved(document);
                }

                public void suspend(LinkedModeModel environment) {}
                public void resume(LinkedModeModel environment, int flags) {}
            });
        }
    }

    private void ensurePositionCategoryRemoved(IDocument document) {
        if (document.containsPositionCategory(getCategory())) {
            try {
                document.removePositionCategory(getCategory());
            } catch (BadPositionCategoryException e) {
                // ignore
            }
            document.removePositionUpdater(fUpdater);
        }
    }

    private String getCategory() {
        return "TemplateProposalCategory_" + toString(); //$NON-NLS-1$
    }

    private int getCaretOffset(TemplateBuffer buffer) {

        TemplateVariable[] variables= buffer.getVariables();
        for (int i= 0; i != variables.length; i++) {
            TemplateVariable variable= variables[i];
            if (variable.getType().equals(GlobalTemplateVariables.Cursor.NAME)) {
                return variable.getOffsets()[0];
            }
        }

        return buffer.getString().length();
    }

    /**
     * Returns the offset of the range in the document that will be replaced by
     * applying this template.
     *
     * @return the offset of the range in the document that will be replaced by
     *         applying this template
     * @since 3.1
     */
    protected /*final*/ int getReplaceOffset() {
        int start;
        if (fContext instanceof DocumentTemplateContext) {
            DocumentTemplateContext docContext = (DocumentTemplateContext)fContext;
            start= docContext.getStart();
        } else {
            start= fRegion.getOffset();
        }
        return start;
    }

    /**
     * Returns the end offset of the range in the document that will be replaced
     * by applying this template.
     *
     * @return the end offset of the range in the document that will be replaced
     *         by applying this template
     * @since 3.1
     */
    protected /*final*/ int getReplaceEndOffset() {
        int end;
        if (fContext instanceof DocumentTemplateContext) {
            DocumentTemplateContext docContext = (DocumentTemplateContext)fContext;
            end= docContext.getEnd();
        } else {
            end= fRegion.getOffset() + fRegion.getLength();
        }
        return end;
    }

    /*
     * @see ICompletionProposal#getSelection(IDocument)
     */
    public Point getSelection(IDocument document) {
        return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
    }

    /*
     * @see ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo() {
        try {
            fContext.setReadOnly(true);
            TemplateBuffer templateBuffer;
            try {
                templateBuffer= fContext.evaluate(fTemplate);
            } catch (TemplateException e) {
                return null;
            }

            return templateBuffer.getString();

        } catch (BadLocationException e) {
            return null;
        }
    }

    /*
     * @see ICompletionProposal#getDisplayString()
     */
    public String getDisplayString() {
        if (fDisplayString == null) {
            String[] arguments= new String[] { fTemplate.getName(), fTemplate.getDescription() };
            fDisplayString= JFaceTextTemplateMessages.getFormattedString("TemplateProposal.displayString", arguments); //$NON-NLS-1$
        }
        return fDisplayString;
    }

    static class JFaceTextTemplateMessages {

        private static final String RESOURCE_BUNDLE= JFaceTextTemplateMessages.class.getName();
        private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

        private JFaceTextTemplateMessages() {
        }

        public static String getString(String key) {
            try {
                return fgResourceBundle.getString(key);
            } catch (MissingResourceException e) {
                return '!' + key + '!';
            }
        }

        public static String getFormattedString(String key, Object arg) {
            return MessageFormat.format(getString(key), new Object[] { arg });
        }


        public static String getFormattedString(String key, Object[] args) {
            return MessageFormat.format(getString(key), args);
        }
    }
    
    /*
     * @see ICompletionProposal#getImage()
     */
    public Image getImage() {
        return fImage;
    }

    /*
     * @see ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation() {
        return null;
    }

    private void openErrorDialog(Shell shell, Exception e) {
        MessageDialog.openError(shell, JFaceTextTemplateMessages.getString("TemplateProposal.errorDialog.title"), e.getMessage()); //$NON-NLS-1$
    }

    /**
     * Returns the relevance.
     *
     * @return the relevance
     */
    public int getRelevance() {
        return fRelevance;
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getInformationControlCreator()
     */
    public IInformationControlCreator getInformationControlCreator() {
        return fInformationControlCreator;
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#selected(org.eclipse.jface.text.ITextViewer, boolean)
     */
    public void selected(ITextViewer viewer, boolean smartToggle) {
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#unselected(org.eclipse.jface.text.ITextViewer)
     */
    public void unselected(ITextViewer viewer) {
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#validate(org.eclipse.jface.text.IDocument, int, org.eclipse.jface.text.DocumentEvent)
     */
    public boolean validate(IDocument document, int offset, DocumentEvent event) {
        try {
            int replaceOffset= getReplaceOffset();
            if (offset >= replaceOffset) {
                String content= document.get(replaceOffset, offset - replaceOffset);
                return fTemplate.getName().toLowerCase().startsWith(content.toLowerCase());
            }
        } catch (BadLocationException e) {
            // concurrent modification - ignore
        }
        return false;
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getPrefixCompletionText(org.eclipse.jface.text.IDocument, int)
     */
    public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
        return fTemplate.getName();
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getPrefixCompletionStart(org.eclipse.jface.text.IDocument, int)
     */
    public int getPrefixCompletionStart(IDocument document, int completionOffset) {
        return getReplaceOffset();
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#apply(org.eclipse.jface.text.IDocument, char, int)
     */
    public void apply(IDocument document, char trigger, int offset) {
        // not called any longer
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#isValidFor(org.eclipse.jface.text.IDocument, int)
     */
    public boolean isValidFor(IDocument document, int offset) {
        // not called any longer
        return false;
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getTriggerCharacters()
     */
    public char[] getTriggerCharacters() {
        // no triggers
        return new char[0];
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getContextInformationPosition()
     */
    public int getContextInformationPosition() {
        return fRegion.getOffset();
    }
    
    /**
     * An enhanced implementation of the <code>ICompletionProposal</code> interface implementing all the extension interfaces.
     * It uses a position to track its replacement offset and length. The position must be set up externally.
     */
    private class PositionBasedCompletionProposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2 {

        /** The string to be displayed in the completion proposal popup */
        private final String fDisplayString;
        /** The replacement string */
        private final String fReplacementString;
        /** The replacement position. */
        private final Position fReplacementPosition;
        /** The cursor position after this proposal has been applied */
        private final int fCursorPosition;
        /** The image to be displayed in the completion proposal popup */
        private final Image fImage;
        /** The context information of this proposal */
        private final IContextInformation fContextInformation;
        /** The additional info of this proposal */
        private final String fAdditionalProposalInfo;

        /**
         * Creates a new completion proposal based on the provided information.  The replacement string is
         * considered being the display string too. All remaining fields are set to <code>null</code>.
         *
         * @param replacementString the actual string to be inserted into the document
         * @param replacementPosition the position of the text to be replaced
         * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
         */
        public PositionBasedCompletionProposal(String replacementString, Position replacementPosition, int cursorPosition) {
            this(replacementString, replacementPosition, cursorPosition, null, null, null, null);
        }

        /**
         * Creates a new completion proposal. All fields are initialized based on the provided information.
         *
         * @param replacementString the actual string to be inserted into the document
         * @param replacementPosition the position of the text to be replaced
         * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
         * @param image the image to display for this proposal
         * @param displayString the string to be displayed for the proposal
         * @param contextInformation the context information associated with this proposal
         * @param additionalProposalInfo the additional information associated with this proposal
         */
        public PositionBasedCompletionProposal(String replacementString, Position replacementPosition, int cursorPosition, Image image, String displayString, IContextInformation contextInformation, String additionalProposalInfo) {
            Assert.isNotNull(replacementString);
            Assert.isTrue(replacementPosition != null);

            fReplacementString= replacementString;
            fReplacementPosition= replacementPosition;
            fCursorPosition= cursorPosition;
            fImage= image;
            fDisplayString= displayString;
            fContextInformation= contextInformation;
            fAdditionalProposalInfo= additionalProposalInfo;
        }

        /*
         * @see ICompletionProposal#apply(IDocument)
         */
        public void apply(IDocument document) {
            try {
                document.replace(fReplacementPosition.getOffset(), fReplacementPosition.getLength(), fReplacementString);
            } catch (BadLocationException x) {
                // ignore
            }
        }

        /*
         * @see ICompletionProposal#getSelection(IDocument)
         */
        public Point getSelection(IDocument document) {
            return new Point(fReplacementPosition.getOffset() + fCursorPosition, 0);
        }

        /*
         * @see ICompletionProposal#getContextInformation()
         */
        public IContextInformation getContextInformation() {
            return fContextInformation;
        }

        /*
         * @see ICompletionProposal#getImage()
         */
        public Image getImage() {
            return fImage;
        }

        /*
         * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
         */
        public String getDisplayString() {
            if (fDisplayString != null) {
                return fDisplayString;
            }
            return fReplacementString;
        }

        /*
         * @see ICompletionProposal#getAdditionalProposalInfo()
         */
        public String getAdditionalProposalInfo() {
            return fAdditionalProposalInfo;
        }

        /*
         * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#apply(org.eclipse.jface.text.ITextViewer, char, int, int)
         */
        public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
            apply(viewer.getDocument());
        }

        /*
         * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#selected(org.eclipse.jface.text.ITextViewer, boolean)
         */
        public void selected(ITextViewer viewer, boolean smartToggle) {
        }

        /*
         * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#unselected(org.eclipse.jface.text.ITextViewer)
         */
        public void unselected(ITextViewer viewer) {
        }

        /*
         * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#validate(org.eclipse.jface.text.IDocument, int, org.eclipse.jface.text.DocumentEvent)
         */
        public boolean validate(IDocument document, int offset, DocumentEvent event) {
            try {
                String content= document.get(fReplacementPosition.getOffset(), offset - fReplacementPosition.getOffset());
                if (fReplacementString.startsWith(content)) {
                    return true;
                }
            } catch (BadLocationException e) {
                // ignore concurrently modified document
            }
            return false;
        }

        /*
         * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#apply(org.eclipse.jface.text.IDocument, char, int)
         */
        public void apply(IDocument document, char trigger, int offset) {
            // not called any more
        }

        /*
         * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#isValidFor(org.eclipse.jface.text.IDocument, int)
         */
        public boolean isValidFor(IDocument document, int offset) {
            // not called any more
            return false;
        }

        /*
         * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getTriggerCharacters()
         */
        public char[] getTriggerCharacters() {
            return null;
        }

        /*
         * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getContextInformationPosition()
         */
        public int getContextInformationPosition() {
            return fReplacementPosition.getOffset();
        }

    }

    
}
