/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/javaeditor/JavaSourceViewer.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * PartiallySynchronizedDocument.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.caleditor;

import java.util.ArrayList;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;

/**
 * Document that can also be used by a background reconciler.
 * @author Edward Lam
 */
public class PartiallySynchronizedDocument extends Document implements ISynchronizable, IPositionUpdater {

    private final Object fInternalLockObject = new Object();
    private Object fLockObject;

    private Document originalDocument = new Document();
    private boolean wasInitialized = false;
    private ArrayList<Change> changes = new ArrayList<Change>();
    {
        this.addPositionUpdater(this);
    }
    
    /**
     * Keeps track of an individual changes to the text in the editor.
     * @author Greg McClement
     */
    private class Change{
        private final int offset;
        private final int oldLength;
        private final int newLength;

        public Change(int offset, int oldLength, int newLength){
            this.offset = offset;
            this.oldLength = oldLength;
            this.newLength = newLength;            
        }
        
        public int getOffset(){
            return offset;
        }
        
        public int getOldLength(){
            return oldLength;
        }
        
        public int getNewLength(){
            return newLength;
        }
    }

    public void update(DocumentEvent event) {
        if (wasInitialized){
            int newLength = 0;
            // although not documented this can be null for some cases. 
            String newText = event.getText();
            if (newText != null){
                newLength = newText.length();
            }
            changes.add(new Change(event.getOffset(), event.getLength(), newLength));
        }
        else{
            wasInitialized = true;
        }
    }

    /**
     * Document was saved so let's reinitialize everything so 
     * the changes list does not become bloated. If the 
     * changes list gets bloated then we will get less 
     * sleep because of the complaining.
     */
    public void wasSaved(){
        originalDocument.set(get());
        changes.clear();
    }
    
    public IDocument getOriginalDocument(){
        return originalDocument;
    }
    
    /**
     * Returns the offset in the original file. If the current offset is in newly added text
     * then the offset returned is -1 since that would not apply to the original text.
     */
    public int getOriginalOffset(int offset){
        for(int i = changes.size() - 1; i >= 0; --i){
            Change change = changes.get(i);
            if (change.getOffset() > offset){
                continue;
            }

            offset = offset - change.getNewLength() + change.getOldLength();
            
            if (change.getOffset() > offset) {
                // if after the calculation the offset is on the other side of the change, then the given offset
                // is actually *in* the change, so return -1 as per spec
                return -1;
            }
        }
        
        return offset;
    }

    /**
     * Returns the offset in the original file. If the current offset is in newly added text
     * then the offset returned is the start of the block that the next text is inserted in.
     */
    public int estimateOriginalOffset(int offset){
        for(int i = changes.size() - 1; i >= 0; --i){
            Change change = changes.get(i);
            if (change.getOffset() > offset){
                continue;
            }
            
            offset = offset - change.getNewLength() + change.getOldLength();
            
            if (change.getOffset() > offset) {
                // The character before the change.
                offset = change.getOffset();
            }
        }
        
        return offset;
    }
    
    /**
     * Converts the given offset to an offset in the current version of the
     * document.
     * @param offset offset in the original document
     * @return the offset in the current version of the document
     */
    public int fromOriginalOffset(int offset){        
        for(Change change : changes){
            if (change.getOffset() > offset){
                continue;
            }
            offset = offset - change.getOldLength() + change.getNewLength();
            
            if (change.getOffset() > offset) {
                // if after the calculation the offset is on the other side of the change, then the given offset
                // is actually *in* the change, so return -1 as per spec
                return -1;
            }
        }
        return offset;
    }
    
    /*
     * @see org.eclipse.jface.text.ISynchronizable#setLockObject(java.lang.Object)
     */
    public void setLockObject(Object lockObject) {
        fLockObject = lockObject;
    }

    /*
     * @see org.eclipse.jface.text.ISynchronizable#getLockObject()
     */
    public Object getLockObject() {
        return fLockObject == null ? fInternalLockObject : fLockObject;
    }

    /*
     * @see IDocumentExtension#startSequentialRewrite(boolean)
     */
    @Override
    public void startSequentialRewrite(boolean normalized) {
        synchronized (getLockObject()) {
            super.startSequentialRewrite(normalized);
        }
    }

    /*
     * @see IDocumentExtension#stopSequentialRewrite()
     */
    @Override
    public void stopSequentialRewrite() {
        synchronized (getLockObject()) {
            super.stopSequentialRewrite();
        }
    }

    /*
     * @see IDocument#get()
     */
    @Override
    public String get() {
        synchronized (getLockObject()) {
            return super.get();
        }
    }

    /*
     * @see IDocument#get(int, int)
     */
    @Override
    public String get(int offset, int length) throws BadLocationException {
        synchronized (getLockObject()) {
            return super.get(offset, length);
        }
    }

    /*
     * @see IDocument#getChar(int)
     */
    @Override
    public char getChar(int offset) throws BadLocationException {
        synchronized (getLockObject()) {
            return super.getChar(offset);
        }
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension4#getModificationStamp()
     * @since 3.1
     */
    @Override
    public long getModificationStamp() {
        synchronized (getLockObject()) {
            return super.getModificationStamp();
        }
    }

    /*
     * @see IDocument#replace(int, int, String)
     */
    @Override
    public void replace(int offset, int length, String text) throws BadLocationException {
        synchronized (getLockObject()) {
            super.replace(offset, length, text);
        }
    }

    /*
     * @see IDocumentExtension4#replace(int, int, String, long)
     */
    @Override
    public void replace(int offset, int length, String text, long modificationStamp) throws BadLocationException {
        synchronized (getLockObject()) {
            super.replace(offset, length, text, modificationStamp);
        }
    }

    /*
     * @see IDocument#set(String)
     */
    @Override
    public void set(String text) {
        synchronized (getLockObject()) {
            super.set(text);
            originalDocument.set(text);
        }
    }

    /*
     * @see IDocumentExtension4#set(String, long)
     */
    @Override
    public void set(String text, long modificationStamp) {
        synchronized (getLockObject()) {
            super.set(text, modificationStamp);
            originalDocument.set(text, modificationStamp);
        }
    }

    /*
     * @see org.eclipse.jface.text.AbstractDocument#addPosition(java.lang.String, org.eclipse.jface.text.Position)
     */
    @Override
    public void addPosition(String category, Position position) throws BadLocationException, BadPositionCategoryException {
        synchronized (getLockObject()) {
            super.addPosition(category, position);
        }
    }

    /*
     * @see org.eclipse.jface.text.AbstractDocument#removePosition(java.lang.String, org.eclipse.jface.text.Position)
     */
    @Override
    public void removePosition(String category, Position position) throws BadPositionCategoryException {
        synchronized (getLockObject()) {
            super.removePosition(category, position);
        }
    }

    /*
     * @see org.eclipse.jface.text.AbstractDocument#getPositions(java.lang.String)
     */
    @Override
    public Position[] getPositions(String category) throws BadPositionCategoryException {
        synchronized (getLockObject()) {
            return super.getPositions(category);
        }
    }    
}
