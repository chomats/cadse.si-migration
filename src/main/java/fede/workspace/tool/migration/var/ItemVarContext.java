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
import fede.workspace.tool.migration.Context;
import fede.workspace.tool.migration.MigrationModel;

public class ItemVarContext extends Variable {

	ItemTypeVariable type;
	LinkVariable[] incoming;
	LinkVariable[] outgoing;
	
	
	public ItemVarContext(Context cxt, boolean inst, String name) {
		super(cxt, inst, name);
	}
	
	public ItemVarContext(Context cxt, boolean inst, String name, ItemTypeVariable type) {
		super(cxt, inst, name);
		this.type = type;
		
	}
	
	@Override
	public Variable[] getVariables() {
		return new Variable[] {  type };
	}
	
	public ItemDescription getItem() {
		return (ItemDescription) getValue();
	}
	
	public boolean isResolved() {
		return super.isResolved() && type.isResolved();
	}
	
	@Override
	public List getPossibleValues(MigrationModel model) {
		if (type.isResolved())
			return model.getItemsByType(type.getType());
		return null;
	}
	
	@Override
	public boolean match(Object value) {
		if (!(value instanceof ItemDescription)) {
			return false;
		}
		if (isResolved()) {
			if (!cxt.getModel().equals(getItem(), (ItemDescription) value)) {
				return false;
			}
		}
		return type.match(((ItemDescription) value).getType());
	}
	
	@Override
	public String toString() {
		if (isResolved())
			return getName()+": Item "+getItem().getId()+" ("+getItem().getName()+")";
		return getName()+": Item = null";
	}

	public LinkVariable[] getIncoming() {
		return incoming;
	}

	public void setIncoming(LinkVariable[] incoming) {
		this.incoming = incoming;
	}

	public LinkVariable[] getOutgoing() {
		return outgoing;
	}

	public void setOutgoing(LinkVariable[] outgoing) {
		this.outgoing = outgoing;
	}

	public ItemTypeVariable getType() {
		return type;
	}
	
	@Override
	public Class getValueClass() {
		return ItemDescription.class;
	}
	
}
