/*******************************************************************************
 *  Copyright (c) 2016 Obeo. 
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *   
 *   Contributors:
 *       Obeo - initial API and implementation
 *  
 *******************************************************************************/
package org.obeonetwork.m2doc.properties;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIXMLProperties.CustomProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;

/**
 * Template information grouping the variable and service declaration in templates.
 * 
 * @author Romain Guider
 */
public class TemplateInfo {
    /**
     * The list of service tokens declared in the template.
     */
    private List<String> serviceTokens;
    /**
     * A map that associates variables declared in the template with their intended type.
     */
    private Map<String, String> variables;

    public TemplateInfo(XWPFDocument document) {
        this.serviceTokens = Lists.newArrayList();
        this.variables = Maps.newHashMap();
        extractMetaData(document);
    }

    private void extractMetaData(XWPFDocument document) {
        CustomProperties props = document.getProperties().getCustomProperties();
        List<CTProperty> properties = props.getUnderlyingProperties().getPropertyList();
        for (CTProperty property : properties) {
            String name = property.getName();
            int variablePrefixLength = M2DocCustomProperties.VAR_PROPERTY_PREFIX.length();
            if (name != null) {
                if (name.startsWith(M2DocCustomProperties.SERVICE_PROPERTY_PREFIX)) {
                    String[] tokens = property.getLpwstr().split(M2DocCustomProperties.SERVICETOKEN_SEPARATOR);
                    serviceTokens.addAll(Lists.newArrayList(tokens));
                } else if (name.startsWith(M2DocCustomProperties.VAR_PROPERTY_PREFIX)
                    && name.length() > variablePrefixLength) {
                    String variableName = name.substring(variablePrefixLength + 1);
                    String type = property.getLpwstr();
                    variables.put(variableName, type);
                }
            }
        }
    }

    /**
     * Returns a non modifiable copy of the service tokens.
     * 
     * @return the list of service tokens.
     */
    public List<String> getServiceTokens() {
        return Collections.unmodifiableList(serviceTokens);
    }

    /**
     * REturns an unmodifiable copy of the variable type map.
     * 
     * @return the variable type map.
     */
    public Map<String, String> getVariables() {
        return Collections.unmodifiableMap(variables);
    }
}
