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
 */
package fede.workspace.tool.migration.var;

import java.util.UUID;
import fede.workspace.tool.migration.Context;

public class ItemTypeVariable extends Variable {
	

	public ItemTypeVariable(Context cxt, boolean inst, String name) {
		super(cxt, inst, name);
	}
	
	public ItemTypeVariable(Context cxt, boolean inst, String name, UUID type) {
		super(cxt, inst, name,type);
	}
	
	@Deprecated
	public ItemTypeVariable(Context cxt, boolean b, String string, String string2) {
		super(cxt, b, string);
	}

	public UUID getTypeName() {
		return (UUID) getValue();
	}
	
	public void setTypeName(String type) {
		setValue(type);
	}
	
	@Override
	public boolean canResovle() {
		return getOriginalValue() != null;
	}

	@Override
	public boolean match(Object value) {
		return isResolved() && getTypeName().equals(value);
	}
	
	@Override
	public String toString() {
		if (isResolved())
			return getName()+": Item Type "+getTypeName();
		return getName()+": Item Type = null";
	}
	
	@Override
	public Class getValueClass() {
		return String.class;
	}
}
