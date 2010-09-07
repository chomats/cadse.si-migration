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
package fede.workspace.tool.migration;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.ItemDescription;
import fr.imag.adele.cadse.core.LinkDescription;
import fede.workspace.tool.migration.var.LinkVariable;

// create-link( BSL, PV, propertiesValue);

public class InstCreateLink extends Instruction {
	static Class[]	types	= new Class[] { LinkDescription.class };

	public InstCreateLink(Context cxt, LinkVariable linkvar) throws SecurityException, NoSuchMethodException {
		super(cxt, linkvar, types, linkvar);
	}

	public LinkDescription execute(LinkDescription l) throws CadseException {
		LinkDescription ret = getCxt().getModel().createlinkIfNeed((ItemDescription) l.getSource(),
				(ItemDescription) l.getDestination(), l.getType());
		getCxt().getModel().log.info("create link " + ret.toString());
		return ret;
	}

}
