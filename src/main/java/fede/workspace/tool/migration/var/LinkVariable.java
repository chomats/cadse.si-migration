/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Copyright (C) 2006-2010 Adele Team/LIG/Grenoble University, France
 */
package fede.workspace.tool.migration.var;

import java.util.List;

import fr.imag.adele.cadse.core.ItemDescription;
import fr.imag.adele.cadse.core.LinkDescription;
import fr.imag.adele.cadse.core.LinkType;
import fede.workspace.tool.migration.Context;
import fede.workspace.tool.migration.MigrationModel;

public class LinkVariable extends Variable {
	
	ItemVarContext source;
	ItemVarContext destination;
	LinkTypeVariable type;
	
	public LinkVariable(Context cxt, boolean inst, String name) {
		super(cxt, inst, name);
	}
	
	public LinkVariable(Context cxt, boolean inst, String name, ItemVarContext source, ItemVarContext destination, LinkTypeVariable type) {
		super(cxt, inst, name);
		this.source = source;
		this.destination = destination;
		this.type = type;
	}
	
	@Override
	public Variable[] getVariables() {
		return new Variable[] { source, destination, type };
	}
	
	@Override
	public boolean isResolved() {
		return super.isResolved() && source.isResolved() && destination.isResolved() && type.isResolved();
	}
	
	@Override
	public List getPossibleValues(MigrationModel model) {
		if (source.isResolved()) {
			if (type.isResolved()) {
				if (destination.isResolved()) {
					return model.foundOutgoingLinks(source.getItem(), destination.getItem(), type.getLinkTypeName());
				}
				return model.foundOutgoingLinks(source.getItem(), type.getLinkTypeName());
			}
			if (destination.isResolved()) {
				return model.foundOutgoingLinks(source.getItem(), destination.getItem());
			}
			return model.foundOutgoingLinks(source.getItem());
		}
		if (destination.isResolved()) {
			if (type.isResolved()) {
				return model.foundIncomingLinks(destination.getItem(), type.getLinkTypeName());
			}
			return model.foundIncomingLinks(destination.getItem());
		}
		return null;
	}
	
	@Override
	public boolean match(Object value) {
		if (!(value instanceof LinkDescription)) {
			return false;
		}
		if (isResolved()) {
			if (!cxt.getModel().equals(getLink(), (LinkDescription) value)) {
				return false;
			}
		}
		LinkDescription l = (LinkDescription) value;
		if (!source.match(l.getSource()))
			return false;
		ItemDescription dest = this.cxt.getModel().resolveLink(l);
		if (dest == null) 
			return false;
		if (!destination.match(dest))
			return false;
		if (!type.match(l.getType()))
			return false;
		return true;
	}
	
	@Override
	public void setValue(Object value) {
		super.setValue(value);
		if (!source.isResolved()) {
			source.setValue(((LinkDescription)value).getSource());
		}
		if (!destination.isResolved()) {
			ItemDescription dest = this.cxt.getModel().resolveLink(((LinkDescription)value));
			destination.setValue(dest);
		}
	}
	
	@Override
	protected Object initInstValue() {
		return new LinkDescription((ItemDescription)source.getValue(), (LinkType)getType().getValue(), (ItemDescription)destination.getValue(), false);
	}

	public ItemVarContext getDestination() {
		return destination;
	}

	public LinkDescription getLink() {
		return (LinkDescription) getValue();
	}

	public ItemVarContext getSource() {
		return source;
	}

	public LinkTypeVariable getType() {
		return type;
	}

	@Override
	public String toString() {
		if (isResolved())
			return getName()+": Link "+getLink();
		return getName()+": Link = null";
	}
	
	@Override
	public Class getValueClass() {
		return LinkDescription.class;
	}
	
}
