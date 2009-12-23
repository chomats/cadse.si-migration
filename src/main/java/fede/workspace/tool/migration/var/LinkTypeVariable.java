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

import fede.workspace.tool.migration.Context;
import fr.imag.adele.cadse.core.LinkType;

public class LinkTypeVariable extends Variable {
	final ItemTypeVariable source;
	final ItemTypeVariable destination;
	LinkType _lt;
	

	public LinkTypeVariable(Context cxt, boolean inst, String name, 
			ItemTypeVariable source, ItemTypeVariable destination, String linktypename) {
		super(cxt, inst, name, linktypename);
		this.source = source;
		this.destination = destination;
	}

	

	public ItemTypeVariable getDestination() {
		return destination;
	}

	
	public LinkType getLinkTypeName() {
		return _lt;
	}

	
	public ItemTypeVariable getSource() {
		return source;
	}
	
	
	
	@Override
	public boolean isResolved() {
		return source.isResolved() && destination.isResolved() && super.isResolved();
	}

	@Override
	public boolean canResovle() {
		return getOriginalValue() != null;
	}
		
	
	@Override
	public boolean match(Object value) {
		return isResolved() && getLinkTypeName().equals(value);
	}

	@Override
	public String toString() {
		if (isResolved())
			return getName()+": Linktype "+getLinkTypeName();
		return getName()+": Linktype = null";
	}
	
	@Override
	public Class getValueClass() {
		return LinkType.class;
	}
}
