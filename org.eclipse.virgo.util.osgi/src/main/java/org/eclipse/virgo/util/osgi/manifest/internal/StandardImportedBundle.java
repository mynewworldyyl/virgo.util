/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.util.osgi.manifest.internal;

import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;

/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
class StandardImportedBundle extends BaseImported implements ImportedBundle {

    private static final String IMPORT_SCOPE_APPLICATION = "application";

    private static final String IMPORT_SCOPE_DIRECTIVE = "import-scope";

    public StandardImportedBundle(HeaderParser parser, String name) {
        super(parser, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    HeaderDeclaration parse(HeaderParser parser, String parseString) {
        List<HeaderDeclaration> header = parser.parseImportBundleHeader(parseString);
        return header.get(0);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isApplicationImportScope() {
        return IMPORT_SCOPE_APPLICATION.equals(getDirectives().get(IMPORT_SCOPE_DIRECTIVE));
    }

    /**
     * {@inheritDoc}
     */
    public String getBundleSymbolicName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    public void setApplicationImportScope(boolean applicationImportScope) {
        if (applicationImportScope) {
            getDirectives().put(IMPORT_SCOPE_DIRECTIVE, IMPORT_SCOPE_APPLICATION);
        } else {
            getDirectives().remove(IMPORT_SCOPE_DIRECTIVE);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleSymbolicName(String bundleSymbolicName) {
        if (bundleSymbolicName == null) {
            throw new IllegalArgumentException("bundleSymbolicName must not be null");
        }
        this.name = bundleSymbolicName;
    }

}
