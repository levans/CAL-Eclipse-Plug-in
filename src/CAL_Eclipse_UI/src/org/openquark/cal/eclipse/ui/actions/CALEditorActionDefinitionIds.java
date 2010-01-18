/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/ui/actions/IJavaEditorActionDefinitionIds.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALEditorActionDefinitionIds.java
 * Creation date: Feb 15, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.actions;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Defines the definition IDs for the Java editor actions.
 * 
 * <p>
 * This interface is not intended to be implemented or extended.
 * </p>.
 * 
 * @since 2.0
 */
public interface CALEditorActionDefinitionIds extends ITextEditorActionDefinitionIds {
    
    // edit
    
    /**
     * Action definition ID of the edit -> smart typing action
     * (value <code>"org.openquark.cal.eclipse.smartTyping.toggle"</code>).
     *
     * @since 3.0
     */
    public static final String TOGGLE_SMART_TYPING= "org.openquark.cal.eclipse.smartTyping.toggle"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the edit -> go to matching bracket action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.goto.matching.bracket"</code>).
     *
     * @since 2.1
     */
    public static final String GOTO_MATCHING_BRACKET= "org.openquark.cal.eclipse.ui.edit.text.java.goto.matching.bracket"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the edit -> go to next member action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.GotoNextFunction"</code>).
     *
     * @since 2.1
     */
    public static final String GOTO_NEXT_MEMBER= "org.openquark.cal.eclipse.ui.actions.GotoNextFunction"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the edit -> go to previous member action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.GotoPreviousFunction"</code>).
     *
     * @since 2.1
     */
    public static final String GOTO_PREVIOUS_MEMBER= "org.openquark.cal.eclipse.ui.actions.GotoPreviousFunction"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the edit -> select enclosing action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.select.enclosing"</code>).
     */
    public static final String SELECT_ENCLOSING= "org.openquark.cal.eclipse.ui.edit.text.java.select.enclosing"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the edit -> select next action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.select.next"</code>).
     */
    public static final String SELECT_NEXT= "org.openquark.cal.eclipse.ui.edit.text.java.select.next"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the edit -> select previous action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.select.previous"</code>).
     */
    public static final String SELECT_PREVIOUS= "org.openquark.cal.eclipse.ui.edit.text.java.select.previous"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the edit -> select restore last action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.select.last"</code>).
     */
    public static final String SELECT_LAST= "org.openquark.cal.eclipse.ui.edit.text.java.select.last"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the edit -> correction assist proposal action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.correction.assist.proposals"</code>).
     */
    public static final String CORRECTION_ASSIST_PROPOSALS= "org.openquark.cal.eclipse.ui.edit.text.java.correction.assist.proposals"; //$NON-NLS-1$
    
    
    /**
     * Action definition ID of the edit -> content assist complete prefix action
     * (value: <code>"org.openquark.cal.eclipse.ui.edit.text.java.complete.prefix"</code>).
     * @since 3.0
     */
    public static final String CONTENT_ASSIST_COMPLETE_PREFIX= "org.openquark.cal.eclipse.ui.edit.text.java.complete.prefix"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the edit -> show Javadoc action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.show.javadoc"</code>).
     */
    public static final String SHOW_JAVADOC= "org.openquark.cal.eclipse.ui.edit.text.java.show.javadoc"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the navigate -> Show Outline action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.show.outline"</code>).
     * 
     * @since 2.1
     */
    public static final String SHOW_OUTLINE= "org.openquark.cal.eclipse.ui.edit.text.java.show.outline"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the navigate -> Show Hierarchy action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.open.hierarchy"</code>).
     * 
     * @since 3.0
     */
    public static final String OPEN_HIERARCHY= "org.openquark.cal.eclipse.ui.edit.text.java.open.hierarchy"; //$NON-NLS-1$
    
    
    /**
     * Action definition ID of the Navigate -> Open Structure action
     * (value <code>"org.openquark.cal.eclipse.ui.navigate.java.open.structure"</code>).
     * 
     * @since 2.1
     */
    public static final String OPEN_STRUCTURE= "org.openquark.cal.eclipse.ui.navigate.java.open.structure"; //$NON-NLS-1$
    
    // source
    
    /**
     * Action definition ID of the source -> comment action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.comment"</code>).
     */
    public static final String COMMENT= "org.openquark.cal.eclipse.ui.edit.text.java.comment"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> uncomment action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.uncomment"</code>).
     */
    public static final String UNCOMMENT= "org.openquark.cal.eclipse.ui.edit.text.java.uncomment"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> toggle comment action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.toggle.comment"</code>).
     * @since 3.0
     */
    public static final String TOGGLE_COMMENT= "org.openquark.cal.eclipse.ui.edit.text.java.toggle.comment"; //$NON-NLS-1$

    /**
     * Action definition ID of the source -> toggle comment action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.toggle.comment"</code>).
     * @since 3.0
     */
    public static final String GENERATE_ELEMENT_COMMENT= "org.openquark.cal.eclipse.ui.edit.text.java.generate.element.comment"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> clean imports action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.clean.import"</code>).
     * @since 3.0
     */
    public static final String CLEAN_IMPORTS= "org.openquark.cal.eclipse.ui.edit.text.java.clean.imports"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> insert types for functions with no types
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.type.declaration.inserter"</code>).
     * @since 3.0
     */
    public static final String TYPE_DECLARATION_INSERTER= "org.openquark.cal.eclipse.ui.edit.text.java.type.declaration.inserter"; //$NON-NLS-1$

    /**
     * Action to apply pretty printer reformater.
     * @since 3.0
     */
    public static final String PRETTY_PRINTER= "org.openquark.cal.eclipse.ui.edit.text.java.pretty.printer"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> add block comment action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.add.block.comment"</code>).
     * @since 3.0
     */
    public static final String ADD_BLOCK_COMMENT= "org.openquark.cal.eclipse.ui.edit.text.java.add.block.comment"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> remove block comment action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.remove.block.comment"</code>).
     * @since 3.0
     */
    public static final String REMOVE_BLOCK_COMMENT= "org.openquark.cal.eclipse.ui.edit.text.java.remove.block.comment"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> indent action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.indent"</code>).
     */
    public static final String INDENT= "org.openquark.cal.eclipse.ui.edit.text.java.indent"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> format action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.format"</code>).
     */
    public static final String FORMAT= "org.openquark.cal.eclipse.ui.edit.text.java.format"; //$NON-NLS-1$
    
    /**
     * Action definition id of the java quick format action
     * (value: <code>"org.openquark.cal.eclipse.ui.edit.text.java.quick.format"</code>).
     * @since 3.0
     */
    public static final String QUICK_FORMAT= "org.openquark.cal.eclipse.ui.edit.text.java.quick.format"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> add import action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.add.import"</code>).
     */
    public static final String ADD_IMPORT= "org.openquark.cal.eclipse.ui.edit.text.java.add.import"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> organize imports action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.organize.imports"</code>).
     */
    public static final String ORGANIZE_IMPORTS= "org.openquark.cal.eclipse.ui.edit.text.java.organize.imports"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> sort order action (value
     * <code>"org.openquark.cal.eclipse.ui.edit.text.java.sort.members"</code>).
     * @since 2.1
     */
    public static final String SORT_MEMBERS= "org.openquark.cal.eclipse.ui.edit.text.java.sort.members"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> add javadoc comment action (value
     * <code>"org.openquark.cal.eclipse.ui.edit.text.java.add.javadoc.comment"</code>).
     * @since 2.1
     */             
    public static final String ADD_JAVADOC_COMMENT= "org.openquark.cal.eclipse.ui.edit.text.java.add.javadoc.comment"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> surround with try/catch action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.surround.with.try.catch"</code>).
     */
    public static final String SURROUND_WITH_TRY_CATCH= "org.openquark.cal.eclipse.ui.edit.text.java.surround.with.try.catch"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> override methods action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.override.methods"</code>).
     */
    public static final String OVERRIDE_METHODS= "org.openquark.cal.eclipse.ui.edit.text.java.override.methods"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> add unimplemented constructors action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.add.unimplemented.constructors"</code>).
     */
    public static final String ADD_UNIMPLEMENTED_CONTRUCTORS= "org.openquark.cal.eclipse.ui.edit.text.java.add.unimplemented.constructors"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source ->generate constructor using fields action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.generate.constructor.using.fields"</code>).
     */
    public static final String GENERATE_CONSTRUCTOR_USING_FIELDS= "org.openquark.cal.eclipse.ui.edit.text.java.generate.constructor.using.fields"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> generate setter/getter action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.create.getter.setter"</code>).
     */
    public static final String CREATE_GETTER_SETTER= "org.openquark.cal.eclipse.ui.edit.text.java.create.getter.setter"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> generate delegates action (value
     * <code>"org.openquark.cal.eclipse.ui.edit.text.java.create.delegate.methods"</code>).
     * @since 2.1
     */
    public static final String CREATE_DELEGATE_METHODS= "org.openquark.cal.eclipse.ui.edit.text.java.create.delegate.methods"; //$NON-NLS-1$
    
    
    /**
     * Action definition ID of the source -> externalize strings action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.externalize.strings"</code>).
     */
    public static final String EXTERNALIZE_STRINGS= "org.openquark.cal.eclipse.ui.edit.text.java.externalize.strings"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the source -> find strings to externalize action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.find.strings.to.externalize"</code>).
     * 
     * @since 3.0
     */
    public static final String FIND_STRINGS_TO_EXTERNALIZE= "org.openquark.cal.eclipse.ui.edit.text.java.find.strings.to.externalize"; //$NON-NLS-1$
    
    /**
     * Note: this id is for internal use only.
     * @deprecated as of 3.0 replaced by {@link org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds#GOTO_NEXT_ANNOTATION}
     */
    @Deprecated
    public static final String SHOW_NEXT_PROBLEM= "org.openquark.cal.eclipse.ui.edit.text.java.show.next.problem"; //$NON-NLS-1$
    
    /**
     * Note: this id is for internal use only.
     * @deprecated as of 3.0 replaced by {@link org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds#GOTO_PREVIOUS_ANNOTATION}
     */
    @Deprecated
    public static final String SHOW_PREVIOUS_PROBLEM= "org.openquark.cal.eclipse.ui.edit.text.java.show.previous.problem"; //$NON-NLS-1$
    
    // refactor
    
    /**
     * Action definition ID of the refactor -> pull up action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.pull.up"</code>).
     */
    public static final String PULL_UP= "org.openquark.cal.eclipse.ui.edit.text.java.pull.up"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> push down action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.push.down"</code>).
     * 
     * @since 2.1
     */
    public static final String PUSH_DOWN= "org.openquark.cal.eclipse.ui.edit.text.java.push.down"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> rename element action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.rename.element"</code>).
     */
    public static final String RENAME_ELEMENT= "org.openquark.cal.eclipse.ui.edit.text.java.rename.element"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> modify method parameters action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.modify.method.parameters"</code>).
     */
    public static final String MODIFY_METHOD_PARAMETERS= "org.openquark.cal.eclipse.ui.edit.text.java.modify.method.parameters"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> move element action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.move.element"</code>).
     */
    public static final String MOVE_ELEMENT= "org.openquark.cal.eclipse.ui.edit.text.java.move.element"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> extract local variable action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.extract.local.variable"</code>).
     */
    public static final String EXTRACT_LOCAL_VARIABLE= "org.openquark.cal.eclipse.ui.edit.text.java.extract.local.variable"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> extract constant action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.extract.constant"</code>).
     * 
     * @since 2.1
     */
    public static final String EXTRACT_CONSTANT= "org.openquark.cal.eclipse.ui.edit.text.java.extract.constant"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> introduce parameter action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.introduce.parameter"</code>).
     * 
     * @since 3.0
     */
    public static final String INTRODUCE_PARAMETER= "org.openquark.cal.eclipse.ui.edit.text.java.introduce.parameter"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> introduce factory action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.introduce.factory"</code>).
     * 
     * @since 3.0
     */
    public static final String INTRODUCE_FACTORY= "org.openquark.cal.eclipse.ui.edit.text.java.introduce.factory"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> inline local variable action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.inline.local.variable"</code>).
     * @deprecated Use INLINE
     */
    @Deprecated
    public static final String INLINE_LOCAL_VARIABLE= "org.openquark.cal.eclipse.ui.edit.text.java.inline.local.variable"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> self encapsulate field action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.self.encapsulate.field"</code>).
     */
    public static final String SELF_ENCAPSULATE_FIELD= "org.openquark.cal.eclipse.ui.edit.text.java.self.encapsulate.field"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> extract method action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.extract.method"</code>).
     */
    public static final String EXTRACT_METHOD= "org.openquark.cal.eclipse.ui.edit.text.java.extract.method"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> inline action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.inline"</code>).
     *
     * @since 2.1
     */
    public static final String INLINE= "org.openquark.cal.eclipse.ui.edit.text.java.inline"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> extract interface action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.extract.interface"</code>).
     * 
     * @since 2.1
     */
    public static final String EXTRACT_INTERFACE= "org.openquark.cal.eclipse.ui.edit.text.java.extract.interface"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> change type action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.change.type"</code>).
     * 
     * @since 3.0
     */
    public static final String CHANGE_TYPE= "org.openquark.cal.eclipse.ui.edit.text.java.change.type"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> move inner type to top level action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.move.inner.to.top.level"</code>).
     * 
     * @since 2.1
     */
    public static final String MOVE_INNER_TO_TOP= "org.openquark.cal.eclipse.ui.edit.text.java.move.inner.to.top.level"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> use supertype action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.use.supertype"</code>).
     * 
     * @since 2.1
     */
    public static final String USE_SUPERTYPE= "org.openquark.cal.eclipse.ui.edit.text.java.use.supertype"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> infer generic type arguments action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.infer.type.arguments"</code>).
     * 
     * @since 3.1
     */
    public static final String INFER_TYPE_ARGUMENTS_ACTION= "org.openquark.cal.eclipse.ui.edit.text.java.infer.type.arguments"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> promote local variable action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.promote.local.variable"</code>).
     * 
     * @since 2.1
     */
    public static final String PROMOTE_LOCAL_VARIABLE= "org.openquark.cal.eclipse.ui.edit.text.java.promote.local.variable"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the refactor -> convert anonymous to nested action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.convert.anonymous.to.nested"</code>).
     * 
     * @since 2.1
     */
    public static final String CONVERT_ANONYMOUS_TO_NESTED= "org.openquark.cal.eclipse.ui.edit.text.java.convert.anonymous.to.nested"; //$NON-NLS-1$
    
    // navigate
    
    /**
     * Action definition ID of the navigate -> open action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.open.editor"</code>).
     */
    public static final String OPEN_EDITOR= "org.openquark.cal.eclipse.ui.edit.text.java.open.editor"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the navigate -> open super implementation action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.open.super.implementation"</code>).
     */
    public static final String OPEN_SUPER_IMPLEMENTATION= "org.openquark.cal.eclipse.ui.edit.text.java.open.super.implementation"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the navigate -> open external javadoc action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.open.external.javadoc"</code>).
     */
    public static final String OPEN_EXTERNAL_JAVADOC= "org.openquark.cal.eclipse.ui.edit.text.java.open.external.javadoc"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the navigate -> open type hierarchy action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.java.open.type.hierarchy"</code>).
     */
    public static final String OPEN_TYPE_HIERARCHY= "org.openquark.cal.eclipse.ui.edit.text.java.open.type.hierarchy"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the navigate -> open call hierarchy action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.open.call.hierarchy"</code>).
     * @since 3.0
     */
    public static final String OPEN_CALL_HIERARCHY= "org.openquark.cal.eclipse.ui.edit.text.java.open.call.hierarchy"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the navigate -> show in package explorer action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.show.in.package.view"</code>).
     */
    public static final String SHOW_IN_PACKAGE_VIEW= "org.openquark.cal.eclipse.ui.edit.text.java.show.in.package.view"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the navigate -> show in navigator action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.show.in.navigator.view"</code>).
     */
    public static final String SHOW_IN_NAVIGATOR_VIEW= "org.openquark.cal.eclipse.ui.edit.text.java.show.in.navigator.view"; //$NON-NLS-1$
    
    // search
    
    /**
     * Action definition ID of the search -> references in workspace action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.references.in.workspace"</code>).
     */
    public static final String SEARCH_REFERENCES_IN_WORKSPACE= "org.openquark.cal.eclipse.ui.edit.text.java.search.references.in.workspace"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> references in project action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.references.in.project"</code>).
     */
    public static final String SEARCH_REFERENCES_IN_PROJECT= "org.openquark.cal.eclipse.ui.edit.text.java.search.references.in.project"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> references in hierarchy action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.references.in.hierarchy"</code>).
     */
    public static final String SEARCH_REFERENCES_IN_HIERARCHY= "org.openquark.cal.eclipse.ui.edit.text.java.search.references.in.hierarchy"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> references in working set action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.references.in.working.set"</code>).
     */
    public static final String SEARCH_REFERENCES_IN_WORKING_SET= "org.openquark.cal.eclipse.ui.edit.text.java.search.references.in.working.set"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> read access in workspace action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.read.access.in.workspace"</code>).
     */
    public static final String SEARCH_READ_ACCESS_IN_WORKSPACE= "org.openquark.cal.eclipse.ui.edit.text.java.search.read.access.in.workspace"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> read access in project action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.read.access.in.project"</code>).
     */
    public static final String SEARCH_READ_ACCESS_IN_PROJECT= "org.openquark.cal.eclipse.ui.edit.text.java.search.read.access.in.project"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> read access in hierarchy action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.read.access.in.hierarchy"</code>).
     */
    public static final String SEARCH_READ_ACCESS_IN_HIERARCHY= "org.openquark.cal.eclipse.ui.edit.text.java.search.read.access.in.hierarchy"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> read access in working set action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.read.access.in.working.set"</code>).
     */
    public static final String SEARCH_READ_ACCESS_IN_WORKING_SET= "org.openquark.cal.eclipse.ui.edit.text.java.search.read.access.in.working.set"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> write access in workspace action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.write.access.in.workspace"</code>).
     */
    public static final String SEARCH_WRITE_ACCESS_IN_WORKSPACE= "org.openquark.cal.eclipse.ui.edit.text.java.search.write.access.in.workspace"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> write access in project action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.write.access.in.project"</code>).
     */
    public static final String SEARCH_WRITE_ACCESS_IN_PROJECT= "org.openquark.cal.eclipse.ui.edit.text.java.search.write.access.in.project"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> write access in hierarchy action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.write.access.in.hierarchy"</code>).
     */
    public static final String SEARCH_WRITE_ACCESS_IN_HIERARCHY= "org.openquark.cal.eclipse.ui.edit.text.java.search.write.access.in.hierarchy"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> write access in working set action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.write.access.in.working.set"</code>).
     */
    public static final String SEARCH_WRITE_ACCESS_IN_WORKING_SET= "org.openquark.cal.eclipse.ui.edit.text.java.search.write.access.in.working.set"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> declarations in workspace action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.declarations.in.workspace"</code>).
     */
    public static final String SEARCH_DECLARATIONS_IN_WORKSPACE= "org.openquark.cal.eclipse.ui.edit.text.java.search.declarations.in.workspace"; //$NON-NLS-1$
    /**
     * Action definition ID of the search -> declarations in project action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.declarations.in.project"</code>).
     */
    public static final String SEARCH_DECLARATIONS_IN_PROJECTS= "org.openquark.cal.eclipse.ui.edit.text.java.search.declarations.in.project"; //$NON-NLS-1$
    /**
     * Action definition ID of the search -> declarations in hierarchy action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.declarations.in.hierarchy"</code>).
     */
    public static final String SEARCH_DECLARATIONS_IN_HIERARCHY= "org.openquark.cal.eclipse.ui.edit.text.java.search.declarations.in.hierarchy"; //$NON-NLS-1$
    /**
     * Action definition ID of the search -> declarations in working set action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.declarations.in.working.set"</code>).
     */
    public static final String SEARCH_DECLARATIONS_IN_WORKING_SET= "org.openquark.cal.eclipse.ui.edit.text.java.search.declarations.in.working.set"; //$NON-NLS-1$
    /**
     * Action definition ID of the search -> implementors in workspace action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.implementors.in.workspace"</code>).
     */
    public static final String SEARCH_IMPLEMENTORS_IN_WORKSPACE= "org.openquark.cal.eclipse.ui.edit.text.java.search.implementors.in.workspace"; //$NON-NLS-1$
    /**
     * Action definition ID of the search -> implementors in working set action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.implementors.in.working.set"</code>).
     */
    public static final String SEARCH_IMPLEMENTORS_IN_WORKING_SET= "org.openquark.cal.eclipse.ui.edit.text.java.search.implementors.in.working.set"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> implementors in project action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.implementors.in.project"</code>).
     * @since 3.0
     */
    public static final String SEARCH_IMPLEMENTORS_IN_PROJECT= "org.openquark.cal.eclipse.ui.edit.text.java.search.implementors.in.project"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> occurrences in file quick menu action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.occurrences.in.file.quickMenu"</code>).
     * @since 3.1
     */
    public static final String SEARCH_OCCURRENCES_IN_FILE_QUICK_MENU= "org.openquark.cal.eclipse.ui.edit.text.java.search.occurrences.in.file.quickMenu"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> occurrences in file > elements action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.occurrences.in.file"</code>).
     * @since 3.1
     */
    public static final String SEARCH_OCCURRENCES_IN_FILE= "org.openquark.cal.eclipse.ui.edit.text.java.search.occurrences.in.file"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> occurrences in file > exceptions action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.exception.occurrences"</code>).
     * @since 3.1
     */
    public static final String SEARCH_EXCEPTION_OCCURRENCES_IN_FILE= "org.openquark.cal.eclipse.ui.edit.text.java.search.exception.occurrences"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the search -> occurrences in file > implements action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.search.implement.occurrences"</code>).
     * @since 3.1
     */
    public static final String SEARCH_IMPLEMENT_OCCURRENCES_IN_FILE= "org.openquark.cal.eclipse.ui.edit.text.java.search.implement.occurrences"; //$NON-NLS-1$
    
    // miscellaneous
    
    /**
     * Action definition ID of the toggle presentation tool bar button action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.toggle.presentation"</code>).
     * @deprecated as of 3.0 replaced by {@link org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds#TOGGLE_SHOW_SELECTED_ELEMENT_ONLY}
     */
    @Deprecated
    public static final String TOGGLE_PRESENTATION= "org.openquark.cal.eclipse.ui.edit.text.java.toggle.presentation"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the toggle text hover tool bar button action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.java.toggle.text.hover"</code>).
     */
    public static final String TOGGLE_TEXT_HOVER= "org.openquark.cal.eclipse.ui.edit.text.java.toggle.text.hover"; //$NON-NLS-1$
    
    /**
     * Action definition ID of the remove occurrence annotations action
     * (value <code>"org.openquark.cal.eclipse.ui.edit.text.remove.occurrence.annotations"</code>).
     * @since 3.0
     */
    public static final String REMOVE_OCCURRENCE_ANNOTATIONS= "org.openquark.cal.eclipse.ui.edit.text.remove.occurrence.annotations";  //$NON-NLS-1$
    
    /**
     * Action definition id of toggle mark occurrences action
     * (value: <code>"org.openquark.cal.eclipse.ui.edit.text.java.toggleMarkOccurrences"</code>).
     * @since 3.0
     */
    public static final String TOGGLE_MARK_OCCURRENCES= "org.openquark.cal.eclipse.ui.edit.text.java.toggleMarkOccurrences"; //$NON-NLS-1$
    
    /**
     * Action definition id of go to the declaration of a symbol.
     * (value: <code>"org.openquark.cal.eclipse.ui.edit.text.java.openDeclaration"</code>).
     * @since 3.0
     */
    public static final String OPENDECLARATION_COMMAND= "org.openquark.cal.eclipse.ui.edit.text.java.openDeclaration"; //$NON-NLS-1$
    
    /**
     * Action definition id of go to the declaration of a symbol.
     * (value: <code>"org.openquark.cal.eclipse.ui.edit.text.cal.rename"</code>).
     * @since 3.0
     */
    public static final String RENAMEACTION= "org.openquark.cal.eclipse.ui.edit.text.java.rename"; //$NON-NLS-1$
    
    /**
     * Action definition id of go to the declaration of a symbol.
     * (value: <code>"org.openquark.cal.eclipse.ui.edit.text.java.showTooltipDescription"</code>).
     * @since 3.0
     */
    public static final String SHOWTOOLTIPDESCRIPTION= "org.openquark.cal.eclipse.ui.edit.text.java.showTooltipDescription"; //$NON-NLS-1$

    /**
     * Action definition id of go to the declaration of a symbol.
     * @since 3.0
     */
    public static final String GOTO_NEXT_ELEMENT_COMMAND= "org.openquark.cal.eclipse.ui.edit.text.java.gotoNextElementAction"; //$NON-NLS-1$
    
    /**
     * Action definition id of go to the declaration of a symbol.
     * @since 3.0
     */
    public static final String GOTO_PREVIOUS_ELEMENT_COMMAND= "org.openquark.cal.eclipse.ui.edit.text.java.gotoPreviousElementAction"; //$NON-NLS-1$
}
