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

import fede.workspace.tool.migration.Context;
import fr.imag.adele.cadse.core.ItemType;

public class ItemTypeVariable extends Variable {
	ItemType _type;

	public ItemTypeVariable(Context cxt, boolean inst, String name) {
		super(cxt, inst, name);
	}
	
	public ItemTypeVariable(Context cxt, boolean inst, String name, String typeName) {
		super(cxt, inst, name, typeName);
	}
	

	public ItemTypeVariable(Context cxt, boolean inst, String name,
			ItemType findItemType) {
		super(cxt, inst, name, findItemType);
	}

	public ItemType getType() {
		return _type;
	}
	
	public void setType(ItemType type) {
		_type = type;
	}
	
	@Override
	public boolean canResovle() {
		return getOriginalValue() != null;
	}

	@Override
	public boolean match(Object value) {
		return isResolved() && getType().equals(value);
	}
	
	@Override
	public String toString() {
		if (isResolved())
			return getName()+": Item Type "+getType();
		return getName()+": Item Type = null";
	}
	
	@Override
	public Class getValueClass() {
		return String.class;
	}
	
	
}
