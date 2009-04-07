/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.util.xml.impl;

import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class VirtualDomParentStrategy implements DomParentStrategy {
  private final DomInvocationHandler myParentHandler;
  private long myModCount;
  private final PsiModificationTracker myModificationTracker;

  public VirtualDomParentStrategy(@NotNull final DomInvocationHandler parentHandler) {
    myParentHandler = parentHandler;
    myModificationTracker = PsiManager.getInstance(myParentHandler.getManager().getProject()).getModificationTracker();
    myModCount = getModCount();
  }

  private long getModCount() {
    return myModificationTracker.getOutOfCodeBlockModificationCount();
  }

  @NotNull
  public DomInvocationHandler getParentHandler() {
    return myParentHandler;
  }

  public XmlElement getXmlElement() {
    return null;
  }

  @NotNull
  public synchronized DomParentStrategy refreshStrategy(final DomInvocationHandler handler) {
    if (!myParentHandler.isValid()) return this;

    final long modCount = getModCount();
    if (modCount != myModCount) {
      final XmlElement xmlElement = handler.recomputeXmlElement(myParentHandler);
      if (xmlElement != null) {
        return new PhysicalDomParentStrategy(xmlElement, DomManagerImpl.getDomManager(xmlElement.getProject()));
      }
      myModCount = modCount;
    }
    return this;
  }

  @NotNull
  public DomParentStrategy setXmlElement(@NotNull final XmlElement element) {
    return new PhysicalDomParentStrategy(element, DomManagerImpl.getDomManager(element.getProject()));
  }

  @NotNull
  public synchronized DomParentStrategy clearXmlElement() {
    myModCount = getModCount();
    return this;
  }

  public synchronized boolean isValid() {
    return getModCount() == myModCount;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof VirtualDomParentStrategy)) return false;

    final VirtualDomParentStrategy that = (VirtualDomParentStrategy)o;

    if (!myParentHandler.equals(that.myParentHandler)) return false;

    return true;
  }

  public int hashCode() {
    return myParentHandler.hashCode();
  }
}
