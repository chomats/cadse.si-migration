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

import fede.workspace.tool.migration.Context;
import fede.workspace.tool.migration.MigrationModel;


public abstract class Variable {
	Condition[] conditions;
	
	public static final Variable[] NO_VARIABLE = new Variable[0];
	VariableState state;
	final Context cxt;
	final String name;
	Object value;
	private Object oriValue = null;
	Variable[] connextVariable = NO_VARIABLE;
	
	
	
	public Variable(Context cxt, boolean instVariable, String name, Object oriValue) {
		this(cxt, instVariable, name);
		if (!instVariable) {
			state = VariableState.CST;	
		} else {
			state = VariableState.INST;
		};
		
		this.value = oriValue;
		this.oriValue  = oriValue;
	}
	
	

	public Variable(Context cxt, boolean instVariable, String name) {
		super();
		this.name = name;
		this.cxt = cxt;
		if (!instVariable) {
			state = VariableState.RESET;	
		} else {
			state = VariableState.INST;
		}
		cxt.add(this);
	}
	
	
	
	public String getName() {
		return name;
	}
	
	public Variable[] getVariables() {
		return NO_VARIABLE;
	}

	public Object getValue() {
		if (state == VariableState.INST && value == null) {
			return initInstValue();
		}
			
		return value;
	}
	
	protected Object initInstValue() {
		return null;
	}



	public boolean match(Object value) {
		return true;
	}

	public void setValue(Object value) {
		if (state == VariableState.RESET) 
			state = VariableState.RESOLVED;
		this.value = value;
	}
	
	public boolean canResovle() {
		return true;
	}
	
	public boolean isResolved() {
		return state == VariableState.RESOLVED || state == VariableState.CST;
	}
	
	public boolean resolve() {
		return isResolved();
	}
	
	public void reset() {
		if (state == VariableState.CST) return;
		if (state == VariableState.INST) {
			value = oriValue;
			return;
		}
		value = oriValue;
		state = VariableState.RESET;
	}
	
	public List getPossibleValues(MigrationModel model) {
		return null;
	}

	public Context getContext() {
		return cxt;
	}
	
	public boolean isInstVariable() {
		return state == VariableState.INST;
	}
	
	public Object getOriginalValue() {
		return oriValue;
	}

	public abstract Class getValueClass() ;

	public Condition[] getCondition() {
		return this.conditions;
	}
	
	public void setCondition(Condition[] conditions) {
		this.conditions = conditions;
	}
	
}
