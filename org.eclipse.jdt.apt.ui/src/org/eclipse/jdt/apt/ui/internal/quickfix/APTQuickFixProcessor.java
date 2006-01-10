/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    dsomerfi@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.ui.internal.quickfix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.apt.ui.AptUIPlugin;
import org.eclipse.jdt.apt.ui.quickfix.IAPTQuickFixProvider;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

public class APTQuickFixProcessor implements IQuickFixProcessor {

    private ListMap<String, IAPTQuickFixProvider> fFixProviders = new ListMap<String, IAPTQuickFixProvider>();
    
    public boolean hasCorrections(ICompilationUnit unit, int problemId) {
        return problemId == EclipseMessager.APT_QUICK_FIX_PROBLEM_ID;
    }
    
    public APTQuickFixProcessor()
    {
        loadProviders();      
    }

    public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
        
        List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
        
        //Map from error-code to IProblemLocation
        //TODO: creating a second map by plugin id is a little wasteful since
        //we don't check if there is anyone who cares about this. This could be 
        //combined into one data structure.
        ListMap<String, IProblemLocation> locationMapByErrorCode = new ListMap<String, IProblemLocation>();
        ListMap<String, IProblemLocation> locationMapByPluginId = new ListMap<String, IProblemLocation>();
        for (IProblemLocation location : locations)
        {
            
            if (location.getProblemId() == EclipseMessager.APT_QUICK_FIX_PROBLEM_ID)
            {
                String [] arguments = location.getProblemArguments();
                assert arguments.length >= 2;
                String pluginID = arguments[0];
                String errorCode = pluginID + "." + arguments[1]; //$NON-NLS-1$
                
                locationMapByErrorCode.put(errorCode, location);
                locationMapByPluginId.put(errorCode, location);
            }
        }
        
        //Do the errorcode-based dispatch
        for (String errorCode : locationMapByErrorCode.keySet())
        {
            List<IProblemLocation> sortedLocations = locationMapByErrorCode.get(errorCode);
            addProposalsFromProviders(errorCode, context, sortedLocations, proposals);                       
        }
        
        //Do the plugin id-based dispatch
        for (String pluginId : locationMapByPluginId.keySet())
        {
            List<IProblemLocation> sortedLocations = locationMapByPluginId.get(pluginId);
            addProposalsFromProviders(pluginId + "." + "*", context, sortedLocations, proposals);  //$NON-NLS-1$//$NON-NLS-2$
        }
        
        return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
    }
    
    private void addProposalsFromProviders(String key, IInvocationContext context, 
            List<IProblemLocation> sortedLocations, List<IJavaCompletionProposal> proposals ) throws CoreException
    {
        List<IAPTQuickFixProvider> providers = fFixProviders.get(key); 
        for (IAPTQuickFixProvider fix : providers)
        {
            IJavaCompletionProposal [] fixProposals = fix.getProposals(context, 
                    sortedLocations.toArray(new IProblemLocation[sortedLocations.size()]));
            for (IJavaCompletionProposal fixProposal : fixProposals)
            {
                proposals.add(fixProposal);
            }
        }          
    }
    

    public void addQuickFixProvider(String pluginID, String errorCode, IAPTQuickFixProvider fix)
    {
        fFixProviders.put(pluginID + "." + errorCode, fix); //$NON-NLS-1$
    }
    
    /**
     * Load the providers that are defined in the extension point
     *
     */
    private void loadProviders()
    {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(AptUIPlugin.PLUGIN_ID, "aptQuickFixProvider"); //$NON-NLS-1$
        for (IExtension extension : extensionPoint.getExtensions())
        {
            for (IConfigurationElement providerConfig : extension.getConfigurationElements())
            {
                if ("quickFixProvider".equals(providerConfig.getName())) //$NON-NLS-1$
                {
                    String pluginId = providerConfig.getAttribute("pluginId"); //$NON-NLS-1$
                    String errorCode = providerConfig.getAttribute("errorCode"); //$NON-NLS-1$
                    try {
                        Object object = providerConfig.createExecutableExtension("className"); //$NON-NLS-1$
                        if (object instanceof IAPTQuickFixProvider)
                        {
                            IAPTQuickFixProvider provider = (IAPTQuickFixProvider) object;
                            addQuickFixProvider(pluginId, errorCode != null ? errorCode : "*", provider); //$NON-NLS-1$
                            
                        }
                        else
                        {
                            AptUIPlugin.log(new Status(IStatus.ERROR, AptUIPlugin.PLUGIN_ID, 1, Messages.APTQuickFixProcessor_classMustBeIAPTQuickFixProvider, null));
                        }
                    } catch (CoreException e) {
                        AptUIPlugin.log(e);
                    }
                }
            }
        }
    }

    private class ListMap<K, V>
    {   
        
        private Map<K, List<V>> fData = new HashMap<K, List<V>>();
        
        public void put(K key, V value)
        {
            List<V> list = fData.get(key);
            if (list == null)
            {
                list = new ArrayList<V>();
                fData.put(key, list);
            }
            list.add(value);
        }
       
        public List<V> get(K key)
        {
            List<V> list = fData.get(key);
            return list != null ? list : Collections.<V>emptyList();
        } 
        
        public Set<K> keySet()
        {
            return fData.keySet();
        }
        
    }

}
