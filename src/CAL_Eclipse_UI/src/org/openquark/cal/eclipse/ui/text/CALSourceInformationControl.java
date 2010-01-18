/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/CustomSourceInformationControl.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CustomSourceInformationControl.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.caleditor.CALSourceViewer;


/**
 * Information Control used to display CAL source in a text hover in the CAL Editor
 * @author Edward Lam
 * @author Andrew Eisenberg
 */
public class CALSourceInformationControl implements 
        IInformationControl, IInformationControlExtension, DisposeListener  {
    
    /** The partition type to be used as the starting partition type by the paritition scanner. */
    private final String fPartition;
    /** The horizontal scroll index. */
    private int fHorizontalScrollPixel;
    
    /** the background color */
    private Color fBackgroundColor;
    
    /** Border thickness in pixels. */
    private static final int BORDER = 1;
    /** The control's shell */
    private Shell fShell;
    /** The control's text widget */
    private StyledText fText;
    /** The control's source viewer */
    private final SourceViewer fViewer;
    /**
     * The optional status field.
     */
    private Label fStatusField;
    /**
     * The separator for the optional status field.
     */
    private Label fSeparator;
    /**
     * The font of the optional status text label.
     */
    private Font fStatusTextFont;
    
    /**
     * The width size constraint.
     * @since 3.2
     */
    private int fMaxWidth = SWT.DEFAULT;
    /**
     * The height size constraint.
     * @since 3.2
     */
    private int fMaxHeight = SWT.DEFAULT;

    /**
     * Creates a default information control with the given shell as parent. The given
     * information presenter is used to process the information to be displayed. The given
     * styles are applied to the created styled text widget.
     *
     * @param parent the parent shell
     * @param shellStyle the additional styles for the shell
     * @param style the additional styles for the styled text widget
     */
    public CALSourceInformationControl(Shell parent, int shellStyle, int style) {
        this(parent, shellStyle, style, null);
    }
    
    /**
     * Creates a default information control with the given shell as parent. The given
     * information presenter is used to process the information to be displayed. The given
     * styles are applied to the created styled text widget.
     *
     * @param parent the parent shell
     * @param shellStyle the additional styles for the shell
     * @param style the additional styles for the styled text widget
     * @param statusFieldText the text to be used in the optional status field
     *                         or <code>null</code> if the status field should be hidden
     */
    public CALSourceInformationControl(Shell parent, int shellStyle, int style, String statusFieldText) {
        GridLayout layout;
        GridData gd;

        fShell = new Shell(parent, SWT.NO_FOCUS | SWT.ON_TOP | shellStyle);
        Display display = fShell.getDisplay();
        fShell.setBackground(display.getSystemColor(
                SWT.COLOR_BLACK));

        Composite composite = fShell;
        layout = new GridLayout(1, false);
        int border = ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER;
        layout.marginHeight = border;
        layout.marginWidth = border;
        composite.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(gd);

        IPreferenceStore store = CALEclipseUIPlugin.getDefault().getCombinedPreferenceStore();
        fBackgroundColor = createBackgroundColor(store, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, 
                AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, display);

        if (statusFieldText != null) {
            composite = new Composite(composite, SWT.NONE);
            layout = new GridLayout(1, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite.setLayout(layout);
            gd = new GridData(GridData.FILL_BOTH);
            composite.setLayoutData(gd);
            composite.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
            composite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        }
        
        // Source viewer
        fViewer = new CALSourceViewer(composite, null, null, false, style, store);
        fViewer.configure(new SimpleCALSourceViewerConfiguration(CALEclipseUIPlugin.getDefault().getCALTextTools().getColorManager(), store, null, null, false));
        fViewer.setEditable(false);

        fText = fViewer.getTextWidget();
        gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
        fText.setLayoutData(gd);

        fText.setBackground(fBackgroundColor);

        fText.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.character == 0x1B) {
                    fShell.dispose();
                }
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        
        // Status field
        if (statusFieldText != null) {
            
            // Horizontal separator line
            fSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
            fSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            // Status field label
            fStatusField = new Label(composite, SWT.RIGHT);
            fStatusField.setText(statusFieldText);
            Font font = fStatusField.getFont();
            FontData[] fontDatas = font.getFontData();
            for (final FontData fontData : fontDatas) {
                fontData.setHeight(fontData.getHeight() * 9 / 10);
            }
            fStatusTextFont = new Font(fStatusField.getDisplay(), fontDatas);
            fStatusField.setFont(fStatusTextFont);
            GridData gd2 = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
            fStatusField.setLayoutData(gd2);

            fStatusField.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
            fStatusField.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        }
        
        addDisposeListener(this);
        setViewerFont();
        fPartition = CALPartitions.CAL_PARTITIONING;
    }
    
    /**
     * Creates a default information control with the given shell as parent. The given
     * information presenter is used to process the information to be displayed. The given
     * styles are applied to the created styled text widget.
     *
     * @param parent the parent shell
     * @param style the additional styles for the styled text widget
     */
    public CALSourceInformationControl(Shell parent,int style) {
        this(parent, SWT.NO_TRIM | SWT.TOOL, style);
    }
    
    /**
     * Creates a default information control with the given shell as parent. The given
     * information presenter is used to process the information to be displayed. The given
     * styles are applied to the created styled text widget.
     *
     * @param parent the parent shell
     * @param style the additional styles for the styled text widget
     * @param statusFieldText the text to be used in the optional status field
     *                         or <code>null</code> if the status field should be hidden
     */
    public CALSourceInformationControl(Shell parent,int style, String statusFieldText) {
        this(parent, SWT.NO_TRIM | SWT.TOOL, style, statusFieldText);
    }
    
    /**
     * Creates a default information control with the given shell as parent.
     * No information presenter is used to process the information
     * to be displayed. No additional styles are applied to the styled text widget.
     *
     * @param parent the parent shell
     */
    public CALSourceInformationControl(Shell parent) {
        this(parent, SWT.NONE);
    }
    
    /**
     * Creates a default information control with the given shell as parent.
     * No information presenter is used to process the information
     * to be displayed. No additional styles are applied to the styled text widget.
     *
     * @param parent the parent shell
     * @param statusFieldText the text to be used in the optional status field
     *                         or <code>null</code> if the status field should be hidden
     */
    public CALSourceInformationControl(Shell parent, String statusFieldText) {
        this(parent, SWT.NONE, statusFieldText);
    }
    
    /*
     * @see org.eclipse.jface.text.IInformationControlExtension2#setInput(java.lang.Object)
     */
    public void setInput(Object input) {
        if (input instanceof String) {
            setInformation((String)input);
        } else {
            setInformation(null);
        }
    }
    
    /*
     * @see IInformationControl#setInformation(String)
     */
    public void setInformation(String content) {
        if (content == null) {
            fViewer.setInput(null);
            return;
        }
        
        IDocument doc = new Document(content);
        if (doc == null) {
            return;
        }

        CALEclipseUIPlugin.getDefault().getCALTextTools().setupJavaDocumentPartitioner(doc);
        fViewer.setInput(doc);

        // ensure that we can scroll enough
        ensureScrollable();

        String start = null;
        if (CALPartitions.CAL_DOC.equals(fPartition)) {
            start = "/**" + doc.getLegalLineDelimiters()[0]; //$NON-NLS-1$
        } else if (CALPartitions.CAL_MULTI_LINE_COMMENT.equals(fPartition)) {
            start = "/*" + doc.getLegalLineDelimiters()[0]; //$NON-NLS-1$
        }
        if (start != null) {
            try {
                doc.replace(0, 0, start);
                int startLen = start.length();
                getViewer().setDocument(doc, startLen, doc.getLength() - startLen);
            } catch (BadLocationException e) {
                // impossible
                Assert.isTrue(false);
            }
        }

        getViewer().getTextWidget().setHorizontalPixel(fHorizontalScrollPixel);
    }
    
    /*
     * @see IInformationControl#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        fShell.setVisible(visible);
    }
    
    /**
     * {@inheritDoc}
     */
    public void widgetDisposed(DisposeEvent event) {
        if (fStatusTextFont != null && !fStatusTextFont.isDisposed()) {
            fStatusTextFont.dispose();
        }
        
        fStatusTextFont = null;
        fShell = null;
        fText = null;
    }
    
    /**
     * {@inheritDoc}
     */
    public final void dispose() {
        if (fBackgroundColor != null) {
            fBackgroundColor.dispose();
            fBackgroundColor = null;
        }
        if (fShell != null && !fShell.isDisposed()) {
            fShell.dispose();
        } else {
            widgetDisposed(null);
        }
    }
    
    /*
     * @see IInformationControl#setSize(int, int)
     */
    public void setSize(int width, int height) {
        
        if (fStatusField != null) {
            GridData gd = (GridData)fViewer.getTextWidget().getLayoutData();
            Point statusSize = fStatusField.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
            Point separatorSize = fSeparator.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
            gd.heightHint = height - statusSize.y - separatorSize.y;
        }
        fShell.setSize(width, height);
        
        if (fStatusField != null) {
            fShell.pack(true);
        }
    }
    
    /**
     * Sets the font for this viewer sustaining selection and scroll position.
     */
    private void setViewerFont() {
        Font font = JFaceResources.getTextFont();

        if (getViewer().getDocument() != null) {

            Point selection = getViewer().getSelectedRange();
            int topIndex = getViewer().getTopIndex();

            StyledText styledText = getViewer().getTextWidget();
            Control parent = styledText;
            if (getViewer() instanceof ITextViewerExtension) {
                ITextViewerExtension extension = (ITextViewerExtension)getViewer();
                parent = extension.getControl();
            }

            parent.setRedraw(false);

            styledText.setFont(font);

            getViewer().setSelectedRange(selection.x, selection.y);
            getViewer().setTopIndex(topIndex);

            if (parent instanceof Composite) {
                Composite composite = (Composite)parent;
                composite.layout(true);
            }

            parent.setRedraw(true);

        } else {
            StyledText styledText = getViewer().getTextWidget();
            styledText.setFont(font);
        }
    }
    
    /**
     * Ensures that the control can be scrolled at least to <code>fHorizontalScrollPixel</code> and adjusts
     * <code>fMaxWidth</code> accordingly.
     */
    private void ensureScrollable() {
        IDocument doc = getViewer().getDocument();
        if (doc == null) {
            return;
        }

        StyledText widget = getViewer().getTextWidget();
        if (widget == null || widget.isDisposed()) {
            return;
        }

        int last = doc.getNumberOfLines() - 1;
        GC gc = new GC(widget);
        gc.setFont(widget.getFont());
        int maxWidth = 0;
        String content = "";

        try {
            for (int i = 0; i <= last; i++) {
                IRegion line;
                line = doc.getLineInformation(i);
                content = doc.get(line.getOffset(), line.getLength());
                int width = gc.textExtent(content).x;
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }
        } catch (BadLocationException e) {
            return;
        } finally {
            gc.dispose();
        }

        // limit the size of the window to the maximum width minus scrolling,
        // but never more than the configured max size (viewport size).
        fMaxWidth = Math.max(0, Math.min(fMaxWidth, maxWidth - fHorizontalScrollPixel + 8));
    }
    
    
    /*
     * @see IInformationControl#setLocation(Point)
     */
    public void setLocation(Point location) {
        Rectangle trim = fShell.computeTrim(0, 0, 0, 0);
        Point textLocation = fText.getLocation();
        location.x += trim.x - textLocation.x;
        location.y += trim.y - textLocation.y;
        fShell.setLocation(location);
    }
    
    /*
     * @see IInformationControl#setSizeConstraints(int, int)
     */
    public void setSizeConstraints(int maxWidth, int maxHeight) {
        fMaxWidth = maxWidth;
        fMaxHeight = maxHeight;
    }
    
    /*
     * @see IInformationControl#computeSizeHint()
     */
    public Point computeSizeHint() {
        // compute the preferred size
        int x = SWT.DEFAULT;
        int y = SWT.DEFAULT;
        Point size = fShell.computeSize(x, y);
        if (size.x > fMaxWidth) {
            x = fMaxWidth;
        }
        if (size.y > fMaxHeight) {
            y = fMaxHeight;
        }

        // recompute using the constraints if the preferred size is larger than the constraints
        if (x != SWT.DEFAULT || y != SWT.DEFAULT) {
            size = fShell.computeSize(x, y, false);
        }

        return size;
    }
    
    /*
     * @see IInformationControl#addDisposeListener(DisposeListener)
     */
    public void addDisposeListener(DisposeListener listener) {
        fShell.addDisposeListener(listener);
    }
    
    /*
     * @see IInformationControl#removeDisposeListener(DisposeListener)
     */
    public void removeDisposeListener(DisposeListener listener) {
        fShell.removeDisposeListener(listener);
    }
    
    /*
     * @see IInformationControl#setForegroundColor(Color)
     */
    public void setForegroundColor(Color foreground) {
        fText.setForeground(foreground);
    }
    
    /*
     * @see IInformationControl#setBackgroundColor(Color)
     */
    public void setBackgroundColor(Color background) {
        fText.setBackground(background);
    }
    
    /*
     * @see IInformationControl#isFocusControl()
     */
    public boolean isFocusControl() {
        return fText.isFocusControl();
    }
    
    /*
     * @see IInformationControl#setFocus()
     */
    public void setFocus() {
        fShell.forceFocus();
        fText.setFocus();
    }
    
    /**
     * Sets the horizontal scroll index in pixels.
     *
     * @param scrollIndex the new horizontal scroll index
     */
    public void setHorizontalScrollPixel(int scrollIndex) {
        scrollIndex = Math.max(0, scrollIndex);
        fHorizontalScrollPixel = scrollIndex;
    }

    /*
     * @see IInformationControl#addFocusListener(FocusListener)
     */
    public void addFocusListener(FocusListener listener) {
        fText.addFocusListener(listener);
    }
    
    /*
     * @see IInformationControl#removeFocusListener(FocusListener)
     */
    public void removeFocusListener(FocusListener listener) {
        fText.removeFocusListener(listener);
    }
    
    /*
     * @see IInformationControlExtension#hasContents()
     */
    public boolean hasContents() {
        return fText.getCharCount() > 0 && fMaxWidth > 0;
    }
    
    protected ISourceViewer getViewer()  {
        return fViewer;
    }
    
    private Color createBackgroundColor(IPreferenceStore store, String key, 
            String defaultKey, Display display) {
        if (!store.getBoolean(defaultKey)) { 

            // if the user has the default color for the background of
            // TextEditors, then we use cream here
            // if a non-default color is the background, then use this
            // non-default color (with a slight change)
            // the reason is that we want the hovers to have slightly different
            // coloring from the text, but some people choose different colors
            // we want to accomodate dark backgrounds with light text, for example
            if (store.contains(key)) {
                RGB rgb = null;
                if (store.isDefault(key)) {
                    rgb = display.getSystemColor(
                            SWT.COLOR_INFO_BACKGROUND).getRGB();
                } else {
                    rgb = PreferenceConverter.getColor(store, key);
                }
                if (rgb != null) {
                    return new Color(display, rgb);
                }
            }
        }
            
        // we don't know what the background color is, make it cream
        // create a new color since we want to be able to dispose of it, too
        return new Color(display, display.getSystemColor(
                SWT.COLOR_INFO_BACKGROUND).getRGB());
    }
}
