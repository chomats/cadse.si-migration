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
package fede.workspace.tool.migration;

import java.util.UUID;
import fr.imag.adele.cadse.core.ItemDescription;
import fr.imag.adele.cadse.core.ItemType;
import fede.workspace.tool.migration.var.ItemTypeVariable;
import fede.workspace.tool.migration.var.ItemVarContext;

public class InstChangeType extends Instruction {
static Class[] types = new Class[] {ItemDescription.class, ItemType.class};
	
	
	public InstChangeType(Context cxt, ItemVarContext source,  ItemTypeVariable type) throws SecurityException, NoSuchMethodException {
		super(cxt, null, types, source, type);
	}
	
	public void execute(ItemDescription source, ItemType type) {
		getCxt().getModel().changeType(source, type);
	}
}
