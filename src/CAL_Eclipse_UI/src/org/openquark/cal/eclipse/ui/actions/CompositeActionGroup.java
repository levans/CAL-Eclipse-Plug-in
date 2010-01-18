/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/actions/CompositeActionGroup.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CompositeActionGroup.java
 * Creation date: Feb 24, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.core.runtime.Assert;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;

/**
 * An action group composed of other action groups.
 * @author Edward Lam
 */
public class CompositeActionGroup extends ActionGroup {

    private ActionGroup[] fGroups;

    public CompositeActionGroup() {
    }

    public CompositeActionGroup(ActionGroup[] groups) {
        setGroups(groups);
    }

    protected void setGroups(ActionGroup[] groups) {
        Assert.isTrue(fGroups == null);
        Assert.isNotNull(groups);
        fGroups = groups;
    }

    public ActionGroup get(int index) {
        if (fGroups == null) {
            return null;
        }
        return fGroups[index];
    }

    public void addGroup(ActionGroup group) {
        if (fGroups == null) {
            fGroups = new ActionGroup[]{group};
        } else {
            ActionGroup[] newGroups = new ActionGroup[fGroups.length + 1];
            System.arraycopy(fGroups, 0, newGroups, 0, fGroups.length);
            newGroups[fGroups.length] = group;
            fGroups = newGroups;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fGroups == null) {
            return;
        }
        for (final ActionGroup actionGrouip : fGroups) {
            actionGrouip.dispose();
        }
    }

    @Override
    public void fillActionBars(IActionBars actionBars) {
        super.fillActionBars(actionBars);
        if (fGroups == null) {
            return;
        }
        for (final ActionGroup actionGroup : fGroups) {
            actionGroup.fillActionBars(actionBars);
        }
    }

    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
        if (fGroups == null) {
            return;
        }
        for (final ActionGroup actionGroup : fGroups) {
            actionGroup.fillContextMenu(menu);
        }
    }

    @Override
    public void setContext(ActionContext context) {
        super.setContext(context);
        if (fGroups == null) {
            return;
        }
        for (final ActionGroup actionGroup : fGroups) {
            actionGroup.setContext(context);
        }
    }

    @Override
    public void updateActionBars() {
        super.updateActionBars();
        if (fGroups == null) {
            return;
        }
        for (final ActionGroup actionGroup : fGroups) {
            actionGroup.updateActionBars();
        }
    }
}
