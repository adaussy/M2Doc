/*******************************************************************************
 *  Copyright (c) 2018 Obeo. 
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *   
 *   Contributors:
 *       Obeo - initial API and implementation
 *  
 *******************************************************************************/
package org.obeonetwork.m2doc.ide.ui.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.presentation.EcoreActionBarContributor.ExtendedLoadResourceAction.RegisteredPackageDialog;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.obeonetwork.m2doc.properties.TemplateCustomProperties;
import org.obeonetwork.m2doc.services.TokenRegistry;
import org.osgi.framework.Bundle;

/**
 * Edit properties page.
 * 
 * @author <a href="mailto:yvan.lussaud@obeo.fr">Yvan Lussaud</a>
 */
public class TemplateCustomPropertiesPage extends WizardPage {

    /**
     * Default width.
     */
    private static final int WIDTH = 300;

    /**
     * The edited {@link TemplateCustomProperties}.
     */
    private final TemplateCustomProperties properties;

    /**
     * The {@link TokenRegistry}.
     */
    private TokenRegistry registry;

    /**
     * The {@link TemplateVariablesPage} to {@link TemplateVariablesPage#validatePage(TemplateCustomProperties) validate}.
     */
    private TemplateVariablesPage templateVariablesProperties;

    /**
     * Constructor.
     * 
     * @param properties
     *            the edited {@link TemplateCustomProperties}
     * @param registry
     *            the {@link TokenRegistry}
     * @param templateVariablesProperties
     *            the {@link TemplateVariablesPage} to {@link TemplateVariablesPage#validatePage(TemplateCustomProperties) validate}
     */
    protected TemplateCustomPropertiesPage(TokenRegistry registry, TemplateCustomProperties properties,
            TemplateVariablesPage templateVariablesProperties) {
        super("Template properties");
        this.properties = properties;
        this.registry = registry;
        this.templateVariablesProperties = templateVariablesProperties;
    }

    @Override
    public void createControl(Composite parent) {
        setMessage("Select services and packages");

        final Composite container = new Composite(parent, SWT.NULL);
        setControl(container);
        container.setLayout(new GridLayout(1, false));

        final TabFolder tabFolder = new TabFolder(container, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        final CheckboxTableViewer tokenViewer = addTokenTabItem(tabFolder, registry, properties);
        addNSURITabItem(tabFolder, tokenViewer, properties);
        addServicesTabItem(tabFolder, tokenViewer, properties);
    }

    /**
     * Adds the services {@link TabItem}.
     * 
     * @param tabFolder
     *            the {@link TabFolder}
     * @param tokenViewer
     *            the token {@link CheckboxTableViewer}
     * @param customProperties
     *            the {@link TemplateCustomProperties}
     */
    private void addServicesTabItem(TabFolder tabFolder, final CheckboxTableViewer tokenViewer,
            final TemplateCustomProperties customProperties) {
        final TabItem servicesTabItem = new TabItem(tabFolder, SWT.NULL);
        servicesTabItem.setText("Services (expert)");
        final Composite servicesContainer = new Composite(tabFolder, SWT.NULL);
        servicesTabItem.setControl(servicesContainer);
        servicesContainer.setLayout(new GridLayout(2, false));

        final TableViewer servicesTable = new TableViewer(servicesContainer, SWT.MULTI);
        Table table = servicesTable.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        servicesTable.getTable().setHeaderVisible(true);
        TableViewerColumn classNameColumn = new TableViewerColumn(servicesTable, SWT.NONE);
        classNameColumn.getColumn().setText("Service class");
        classNameColumn.getColumn().setWidth(WIDTH);
        classNameColumn.setLabelProvider(new CellLabelProvider() {

            @SuppressWarnings("unchecked")
            @Override
            public void update(ViewerCell cell) {
                cell.setText(((Entry<String, String>) cell.getElement()).getKey());
            }
        });
        TableViewerColumn bundleNameColumn = new TableViewerColumn(servicesTable, SWT.NONE);
        bundleNameColumn.getColumn().setText("Bundle");
        bundleNameColumn.getColumn().setWidth(WIDTH);
        bundleNameColumn.setLabelProvider(new CellLabelProvider() {

            @SuppressWarnings("unchecked")
            @Override
            public void update(ViewerCell cell) {
                cell.setText(((Entry<String, String>) cell.getElement()).getValue());
            }
        });
        servicesTable.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing to do here
            }

            @Override
            public void dispose() {
                // nothing to do here
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return ((TemplateCustomProperties) inputElement).getServiceClasses().entrySet().toArray();
            }
        });

        tokenViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                servicesTable.refresh();
            }
        });

        servicesTable.setInput(customProperties);

        final Composite servicesButtonComposite = new Composite(servicesContainer, SWT.NONE);
        servicesButtonComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        servicesButtonComposite.setLayout(new GridLayout(1, false));

        final Button addButton = new Button(servicesButtonComposite, SWT.NONE);
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        addButton.setText("Add");
        addButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                openClassSelectionDialog(tokenViewer, servicesTable, customProperties);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do here
            }
        });

        final Button removeButton = new Button(servicesButtonComposite, SWT.NONE);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        removeButton.setText("Remove");
        removeButton.addSelectionListener(new SelectionListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (Entry<?, ?> entry : (List<Entry<?, ?>>) ((IStructuredSelection) servicesTable.getSelection())
                        .toList()) {
                    customProperties.getServiceClasses().remove(entry.getKey());
                }
                servicesTable.refresh();
                tokenViewer.refresh();
                templateVariablesProperties.validatePage(customProperties);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do here
            }
        });
        servicesTable.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                removeButton.setEnabled(!((IStructuredSelection) event.getSelection()).isEmpty());
            }
        });
        removeButton.setEnabled(false);
    }

    /**
     * Gets the bundle name of the given {@link IType}.
     * 
     * @param type
     *            the {@link IType}
     * @return the bundle name of the given {@link IType} if any, <code>null</code> otherwise
     */
    private String getBundleName(IType type) {
        final String packageName = type.getParent().getParent().getElementName();
        final List<String> segments = new ArrayList<>(Arrays.asList(packageName.split("\\.")));
        while (!segments.isEmpty()) {
            String bundleName = String.join(".", segments);
            final Bundle bundle = Platform.getBundle(bundleName);
            if (bundle != null) {
                return bundle.getSymbolicName();
            }
            segments.remove(segments.size() - 1);
        }

        return null;
    }

    /**
     * Adds the nsURI {@link TabItem}.
     * 
     * @param tabFolder
     *            the {@link TabFolder}
     * @param tokenViewer
     *            the token {@link CheckboxTableViewer}
     * @param customProperties
     *            the {@link TemplateCustomProperties}
     */
    private void addNSURITabItem(TabFolder tabFolder, final CheckboxTableViewer tokenViewer,
            final TemplateCustomProperties customProperties) {
        final TabItem nsURITabItem = new TabItem(tabFolder, SWT.NULL);
        nsURITabItem.setText("nsURI (expert)");
        final Composite nsURIContainer = new Composite(tabFolder, SWT.NULL);
        nsURITabItem.setControl(nsURIContainer);
        nsURIContainer.setLayout(new GridLayout(2, false));

        final TableViewer nsURITable = new TableViewer(nsURIContainer, SWT.MULTI);
        Table table = nsURITable.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        nsURITable.getTable().setHeaderVisible(true);
        final TableViewerColumn nsURIColumn = new TableViewerColumn(nsURITable, SWT.NONE);
        nsURIColumn.getColumn().setText("Package nsURI");
        nsURIColumn.getColumn().setWidth(WIDTH);
        nsURIColumn.setLabelProvider(new CellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                cell.setText((String) cell.getElement());
            }
        });
        nsURITable.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing to do here
            }

            @Override
            public void dispose() {
                // nothing to do here
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return ((TemplateCustomProperties) inputElement).getPackagesURIs().toArray();
            }
        });

        tokenViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                nsURITable.refresh();
            }
        });

        nsURITable.setInput(customProperties);

        final Composite nsURIButtonComposite = new Composite(nsURIContainer, SWT.NONE);
        nsURIButtonComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        nsURIButtonComposite.setLayout(new GridLayout(1, false));

        final Button addButton = new Button(nsURIButtonComposite, SWT.NONE);
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        addButton.setText("Add");
        addButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                RegisteredPackageDialog dialog = new RegisteredPackageDialog(Display.getCurrent().getActiveShell());
                if (dialog.open() == Dialog.OK && dialog.getResult() != null) {
                    if (dialog.getResult().length != 0) {
                        for (Object object : dialog.getResult()) {
                            if (!customProperties.getPackagesURIs().contains(object)) {
                                customProperties.getPackagesURIs().add((String) object);
                            }
                        }
                        nsURITable.refresh();
                        tokenViewer.refresh();
                        templateVariablesProperties.validatePage(customProperties);
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do here
            }
        });

        final Button removeButton = new Button(nsURIButtonComposite, SWT.NONE);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        removeButton.setText("Remove");
        removeButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                customProperties.getPackagesURIs()
                        .removeAll(((IStructuredSelection) nsURITable.getSelection()).toList());
                nsURITable.refresh();
                tokenViewer.refresh();
                templateVariablesProperties.validatePage(customProperties);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });
        nsURITable.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                removeButton.setEnabled(!((IStructuredSelection) event.getSelection()).isEmpty());
            }
        });
        removeButton.setEnabled(false);
    }

    /**
     * Adds the {@link TemplateCustomProperties#getServiceClasses() services} {@link TabItem} to the given {@link TabFolder}.
     * 
     * @param tabFolder
     *            the {@link TabFolder}
     * @param tokenRegistry
     *            the {@link TokenRegistry}
     * @param customProperties
     *            the {@link TemplateCustomProperties}
     * @return the create {@link CheckboxTableViewer}
     */
    private CheckboxTableViewer addTokenTabItem(TabFolder tabFolder, final TokenRegistry tokenRegistry,
            final TemplateCustomProperties customProperties) {
        final TabItem packageTabItem = new TabItem(tabFolder, SWT.NULL);
        packageTabItem.setText("Packages");
        final Composite packagesContainer = new Composite(tabFolder, SWT.NULL);
        packageTabItem.setControl(packagesContainer);
        packagesContainer.setLayout(new GridLayout(1, false));

        Table table = new Table(packagesContainer, SWT.BORDER | SWT.V_SCROLL | SWT.CHECK);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        CheckboxTableViewer tableViewer = new CheckboxTableViewer(table);
        tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tableViewer.setLabelProvider(new LabelProvider());
        tableViewer.setContentProvider(new IStructuredContentProvider() {

            @Override
            @SuppressWarnings("unchecked")
            public Object[] getElements(Object inputElement) {
                return ((java.util.List<Object>) inputElement).toArray();
            }

            /**
             * {@inheritDoc}
             *
             * @see org.eclipse.jface.viewers.IContentProvider#dispose()
             */
            @Override
            public void dispose() {
                // nothing to do here
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing to do here
            }

        });
        tableViewer.setCheckStateProvider(new ICheckStateProvider() {

            @Override
            public boolean isGrayed(Object element) {
                return false;
            }

            @Override
            public boolean isChecked(Object element) {
                return tokenRegistry.getSelectedToken(customProperties).contains(element);
            }
        });
        tableViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getChecked()) {
                    final String tokenName = (String) event.getElement();
                    addToken(tokenRegistry, customProperties, tokenName);
                } else {
                    final String tokenName = (String) event.getElement();
                    removeToken(tokenRegistry, customProperties, tokenName);
                }
            }
        });
        final List<String> tokenNames = new ArrayList<>(tokenRegistry.getRegisteredTokens());
        Collections.sort(tokenNames);
        tableViewer.setInput(tokenNames);

        return tableViewer;
    }

    /**
     * Adds the given token name to the given {@link TemplateCustomProperties}.
     * 
     * @param tokenRegistry
     *            the {@link TokenRegistry}
     * @param customProperties
     *            the {@link TemplateCustomProperties}
     * @param tokenName
     *            the token name
     */
    private void addToken(TokenRegistry tokenRegistry, TemplateCustomProperties customProperties, String tokenName) {
        for (Entry<String, List<String>> entry : tokenRegistry.getServices(tokenName).entrySet()) {
            final String bundleName = entry.getKey();
            for (String cls : entry.getValue()) {
                customProperties.getServiceClasses().put(cls, bundleName);
            }
        }
        for (String nsURI : tokenRegistry.getPackages(tokenName)) {
            if (!customProperties.getPackagesURIs().contains(nsURI)) {
                customProperties.getPackagesURIs().add(nsURI);
            }
        }
        templateVariablesProperties.validatePage(customProperties);
    }

    /**
     * Removes the given token name from the given {@link TemplateCustomProperties}.
     * 
     * @param tokenRegistry
     *            the {@link TokenRegistry}
     * @param customProperties
     *            the {@link TemplateCustomProperties}
     * @param tokenName
     *            the token name
     */
    private void removeToken(TokenRegistry tokenRegistry, TemplateCustomProperties customProperties, String tokenName) {
        for (List<String> classes : tokenRegistry.getServices(tokenName).values()) {
            for (String cls : classes) {
                customProperties.getServiceClasses().remove(cls);
            }
        }
        customProperties.getPackagesURIs().removeAll(tokenRegistry.getPackages(tokenName));
        templateVariablesProperties.validatePage(customProperties);
    }

    /**
     * Opens the class selection dialog.
     * 
     * @param tokenViewer
     *            the token {@link Viewer}
     * @param servicesTable
     *            the service {@link Viewer}
     * @param customProperties
     *            the {@link TemplateCustomProperties}
     */
    @SuppressWarnings("restriction")
    private void openClassSelectionDialog(Viewer tokenViewer, Viewer servicesTable,
            final TemplateCustomProperties customProperties) {
        final IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
        final FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(
                Display.getCurrent().getActiveShell(), true, PlatformUI.getWorkbench().getProgressService(), scope,
                IJavaSearchConstants.CLASS);
        if (dialog.open() == Dialog.OK && dialog.getResult() != null && dialog.getResult().length != 0) {
            for (Object object : dialog.getResult()) {
                IPath parentPath = ((IType) object).getParent().getPath();
                if (parentPath.getFileExtension().equals("jar")) {
                    int indexOfUnderscore = parentPath.lastSegment().indexOf('_');
                    if (indexOfUnderscore > -1) {
                        final String pluginName = parentPath.lastSegment().substring(0, indexOfUnderscore);
                        customProperties.getServiceClasses().put(((IType) object).getFullyQualifiedName(), pluginName);
                    } else {
                        customProperties.getServiceClasses().put(((IType) object).getFullyQualifiedName(), "");
                    }
                } else {
                    final String bundleName = getBundleName((IType) object);
                    if (bundleName != null) {
                        customProperties.getServiceClasses().put(((IType) object).getFullyQualifiedName(), bundleName);
                    } else {
                        customProperties.getServiceClasses().put(((IType) object).getFullyQualifiedName(),
                                ((IType) object).getJavaProject().getProject().getName());
                    }
                }
            }
            tokenViewer.refresh();
            servicesTable.refresh();
            templateVariablesProperties.validatePage(customProperties);
        }
    }
}
