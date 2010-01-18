/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/

/*
 * AutoComplete_Test.java
 * Creation date: March 20 2007.
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui;

import java.util.List;

import junit.framework.TestCase;

import org.openquark.util.Pair;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.eclipse.ui.text.CALSourceViewerConfiguration;
import org.openquark.cal.services.AutoCompleteHelper;

public class AutoComplete_Test extends TestCase {

    public void testAutoCompleteHelper() {
        final String[] input = {
                "Cal.Core.Prelude.abs",
                "Cal    .    Core   .    Prelude   .    abs",
                "    Core   .    Prelude   .    abs",
                "    Core   .    Prelude   .",
                "foo    Core   .    Prelude   .",
                "foo    List   .    map",
                "map",
                ""
        };
        
        final String[] prefix = {
                "abs",
                "abs",
                "abs",
                "",
                "",
                "map",
                "map",
                ""
        };
        
        final String[] module = {
                "Cal.Core.Prelude",
                "Cal.Core.Prelude",
                "Core.Prelude",
                "Core.Prelude",
                "Core.Prelude",
                "List",
                "",
                ""
        };

        for(int i = 0; i < input.length; ++i){
            final String inputString = input[i];
            final AutoCompleteHelper ach = new AutoCompleteHelper(new AutoCompleteHelper.Document() {
                public char getChar(int offset) {
                    return inputString.charAt(offset);
                }

                public String get(int startIndex, int length) {
                    return inputString.substring(startIndex, startIndex + length);
                }                   
            });
            final int offset = inputString.length();
            final String prefixFound = ach.getLastIncompleteIdentifier(offset);
            final Pair<String, List<Integer>> scopingAndOffsets = ach.getIdentifierScoping(offset);
            final String moduleFound = scopingAndOffsets.fst();
            if (!prefixFound.equals(prefix[i])){
                fail();
            }
            if (!moduleFound.equals(module[i])){
                fail();
            }
        }
    }

    public void testAutoComplete_middleAndSuffix() {
        String[] a = {
                "Cal.Core.Prelude",
                "Cal.Core.Prelude",
                "Cal.Core.Prelude",
                "Cal.Core.Prelude",
                "Cal.Core.Prelude",
                "Cal.Core.Prelude",
                "Cal.Core.Prelude",
                "Cal.Core.Prelude",
                "Cal.Core.Prelude",
                "Cal.Core.Prelude",
                "Cal.Core.Prelude"
        };
        String[] b = {
                // Matches Start
                "Cal",
                "Ca",
                "Cal.Core",
                "Cal.Co",
                "Cal.Core.Prelude",
                "Cal.Core.Prelu",
                // Matches Middle
                "Cor",
                "Core.",
                "Core.Prelu",
                // Suffixes
                "Pre",
                "Prelude"
        };
        boolean[] isPartial = {
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true
        };
        boolean[] answer = {
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true, 
                true,
                true
        };

        for(int i = 0; i < a.length; ++i){
            ModuleName moduleName = ModuleName.make(a[i]);
            String[] components = b[i].split("\\.");
            if (
                    answer[i] == CALSourceViewerConfiguration.isSuffixOf(moduleName, components, isPartial[i]) ||
                    answer[i] == CALSourceViewerConfiguration.isMiddleOf(moduleName, components, isPartial[i])
            ){
            }
            else{
                fail();
            }
        }
    }
}
