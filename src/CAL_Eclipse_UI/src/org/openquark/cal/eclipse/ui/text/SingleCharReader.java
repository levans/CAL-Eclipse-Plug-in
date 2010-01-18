/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/core extension/org/eclipse/jdt/internal/corext/javadoc/SingleCharReader.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * SingleCharReader.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import java.io.IOException;
import java.io.Reader;

/**
 * A reader which can be implemented by implementing the read() method.
 * @author Edward Lam
 */
public abstract class SingleCharReader extends Reader {

    /**
     * @see Reader#read()
     */
    @Override
    public abstract int read() throws IOException;

    /**
     * @see Reader#read(char[],int,int)
     */
    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        int end = off + len;
        for (int i = off; i < end; i++) {
            int ch = read();
            if (ch == -1) {
                if (i == off) {
                    return -1;
                } else {
                    return i - off;
                }
            }
            cbuf[i] = (char)ch;
        }
        return len;
    }

    /**
     * @see Reader#ready()
     */
    @Override
    public boolean ready() throws IOException {
        return true;
    }

    /**
     * Gets the content as a String
     */
    public String getString() throws IOException {
        StringBuilder buf = new StringBuilder();
        int ch;
        while ((ch = read()) != -1) {
            buf.append((char)ch);
        }
        return buf.toString();
    }
}
