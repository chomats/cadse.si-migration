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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fede.workspace.tool.migration.var.Variable;

public class Instruction {
	
	public static final String METHOD_NAME = "execute";
	final private Variable[] variables;
	private Method m;
	private Variable retVariable;
	private Context cxt;
	private Class arraytype;


	protected Instruction(Context cxt, Variable retVariable, Class[] types, Variable ... variables) throws SecurityException, NoSuchMethodException {
		cxt.add(this);
		this.cxt = cxt;
		this.variables = variables;
		this.arraytype = null;
		assert types.length == variables.length;
		for (int i = 0; i < variables.length; i++) {
			assert variables[i].getContext() == cxt;
			assert types[i] == variables[i].getValueClass();
		}
		
		Class<? extends Instruction> clazz = getClass();
		m = clazz.getMethod(METHOD_NAME, types);
		assert m != null;
		if (retVariable != null) {
			assert retVariable.getContext() == cxt;
			assert m.getReturnType() == retVariable.getValueClass();
		}
		this.retVariable = retVariable;
		
	}
	
	protected Instruction(Context cxt, Variable retVariable, Class type, Variable[] variables) throws SecurityException, NoSuchMethodException {
		cxt.add(this);
		this.cxt = cxt;
		this.variables = variables;
		this.arraytype = type;
		assert type.isArray();
		
		for (int i = 0; i < variables.length; i++) {
			assert variables[i].getContext() == cxt;
			assert type.getComponentType() == variables[i].getValueClass();
		}
		
		Class<? extends Instruction> clazz = getClass();
		m = clazz.getMethod(METHOD_NAME, new Class[] { type });
		assert m != null;
		if (retVariable != null) {
			assert retVariable.getContext() == cxt;
			assert m.getReturnType() == retVariable.getValueClass();
		}
		this.retVariable = retVariable;
		
	}
	
	final public Variable[] getVariables() {
		return variables;
	}
	
	public void run() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		
		Object ret;
		if (arraytype != null) {
			Object array = Array.newInstance(arraytype.getComponentType(), variables.length);
			for (int i = 0; i < variables.length; i++) {
				Variable v = variables[i];
				Array.set(array, i, v.getValue());
			}
			ret = m.invoke(this, new Object[] { array });
		} else {
			Object[] values = new Object[variables.length];
			for (int i = 0; i < variables.length; i++) {
				Variable v = variables[i];
				values[i] = v.getValue();
			}
			ret = m.invoke(this, values);
		}
			
		
		if (retVariable != null)
			retVariable.setValue(ret);
	}
	
	public Context getCxt() {
		return cxt;
	}
	
}
