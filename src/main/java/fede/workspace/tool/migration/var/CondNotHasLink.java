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

import java.util.ArrayList;
import java.util.List;

import fr.imag.adele.cadse.core.ItemDescription;
import fr.imag.adele.cadse.core.LinkDescription;
import fede.workspace.tool.migration.MigrationModel;

public class CondNotHasLink extends Condition {
	LinkTypeVariable lt;
	public CondNotHasLink(LinkTypeVariable lt) {
		this.lt = lt;
	}
	
	@Override
	public List match(List listvalues) {
		ArrayList ret = new ArrayList();
		for (Object aVal : listvalues) {
			if (aVal instanceof ItemDescription) {
				if (notHasLink((ItemDescription) aVal))
					ret.add(aVal);
			}
		}
		return ret;
	}

	private boolean notHasLink(ItemDescription description) {
		MigrationModel instances =  lt.getContext().getModel();
		for (LinkDescription ld : description.getLinks()) {
			if (ld.getType().equals(lt.getLinkTypeName())) {
				ItemDescription findDest = instances.get(ld.getDestination().getId());
				if (findDest != null)
					return false;
			}
				
			
		}
		return true;
	}
}
