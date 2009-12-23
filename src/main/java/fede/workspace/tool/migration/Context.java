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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fede.workspace.tool.migration.var.Condition;
import fede.workspace.tool.migration.var.CstStringVariable;
import fede.workspace.tool.migration.var.ItemTypeVariable;
import fede.workspace.tool.migration.var.ItemVarContext;
import fede.workspace.tool.migration.var.LinkTypeVariable;
import fede.workspace.tool.migration.var.LinkVariable;
import fede.workspace.tool.migration.var.ShortNameVariable;
import fede.workspace.tool.migration.var.StringVariable;
import fede.workspace.tool.migration.var.UniqueNameVariable;
import fede.workspace.tool.migration.var.Variable;

public class Context {

	private final MigrationModel	model;

	private Map<String, Variable>	namebyvars	= new HashMap<String, Variable>();
	private ArrayList<Variable>		vars		= new ArrayList<Variable>();

	List<Instruction>				insts		= new ArrayList<Instruction>();

	private Map<Variable, Object>	n			= new HashMap<Variable, Object>();

	private int[]					indexs;
	private Object[][]				solutions;
	private Variable[]				arrayvars;

	public Context(MigrationModel model) {
		this.model = model;
	}

	public void reset() {
		for (Variable ivc : vars) {
			ivc.reset();
		}
	}

	public MigrationModel getModel() {
		return model;
	}

	public int execute(Logger log) {
		begin();
		int count = 0;
		while (next()) {
			try {
				executeSol(log);
			} catch (IllegalArgumentException e) {
				log.log(Level.SEVERE, "", e);
			} catch (IllegalAccessException e) {
				log.log(Level.SEVERE, "", e);
			} catch (InvocationTargetException e) {
				log.log(Level.SEVERE, "", e);
			}
			count++;
		}
		// System.out.println("Find "+count+" solution(s).");
		log.info("Find " + count + " solution(s).");
		return count;
	}

	public void executeSol(Logger log) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		printSol(this, log);
		for (Variable v : instvars) {
			v.reset();
		}
		for (Instruction i : insts) {
			i.run();
		}

	}

	public List<Instruction> getInsts() {
		return insts;
	}

	public static void main(String[] args) throws Throwable {
		// TODO: Ne marche plus.... a migrer
		/*
		 * /home/chomats/ws-cadseg3-new/Model.Workspace.EnvMelusine
		 * /home/chomats/ws-cadseg3-new/Model.Workspace.EnvMelusine/resources/melusine.ser
		 * PropertyValue PV; ProvideService PS; BinAbstractService AS;
		 * PropertyAttribute PA;
		 * 
		 * link (AS -[propertiesValue ]-> PS); link (PS -[propertiesValues ]->
		 * PV); link (PV -[propertyAttribute]-> PA);
		 * 
		 */
		// System.setProperty(MelusineCst., value)
		MigrationModel model = new MigrationModel();
		// model.loadType("/home/chomats/ws-cadseg3-new/Model.Workspace.EnvMelusine");
		// /model.load("/home/chomats/ws-runtime-Model.Workspace.EnvMelusine/.melusine");

		System.out.println(" ********** \ncheck\n *********\n\n");
		model.check(false);

		Logger log = Logger.getLogger("ss");
		System.out.println(" ********** \nexecute 1\n *********\n\n");
		Context cxt1 = cxt1(model);
		cxt1.execute(log);

		/*
		 * context { PropertyType X } => { change-type(X, PropertyAttribute) }
		 * 
		 * context { PropertySet X } => { change-link-type(X, propertiesType,
		 * propertiesAttributes) } context { BinAbstractService X } => {
		 * change-link-type(X, propertiesType, propertiesAttributes) } context {
		 * PropertyValue X } => { change-link-type(X, type, propertyAttribute) }
		 * context { PropertyValue X } => { change-link-type(X, type,
		 * propertyAttribute) }
		 * 
		 * 
		 * 
		 */
		System.out.println(" ********** \nexecute 2\n *********\n\n");
		Context cxt2 = cxt2(model);
		cxt2.execute(log);
		System.out.println(" ********** \nexecute 3\n *********\n\n");
		runChangeLinkType(log, model, "PropertySet", "PropertyAttribute", "propertiesType", "propertiesAttributes");
		System.out.println(" ********** \nexecute 4\n *********\n\n");
		runChangeLinkType(log, model, "BinAbstractService", "PropertyAttribute", "propertiesType",
				"propertiesAttributes");
		System.out.println(" ********** \nexecute 5\n *********\n\n");
		runChangeLinkType(log, model, "PropertyValue", "PropertyAttribute", "type", "propertyAttribute");
		System.out.println(" ********** \nexecute 6\n *********\n\n");
		runChangeLinkType(log, model, "PropertyField", "PropertyAttribute", "type", "propertyAttribute");
		System.out.println(" ********** \nexecute 7\n *********\n\n");
		runChangeLinkType(log, model, "ProvideService", "PropertyField", "propertiesField", "propertiesFields");
		System.out.println(" ********** \nexecute 8\n *********\n\n");
		cxt2 = cxt3(model);
		cxt2.execute(log);
		System.out.println(" ********** \nexecute 9\n *********\n\n");
		runChangeLinkType(log, model, "AbstractServiceBroker", "PropertyAttribute", "propertiesAttribute",
				"propertiesAttributes");
		System.out.println(" ********** \nexecute 10\n *********\n\n");
		runChangeLinkType(log, model, "BinServiceLocal", "PropertyValue", "propertiesValue", "propertiesValues");
		System.out.println(" ********** \nexecute 11\n *********\n\n");
		runChangeLinkType(log, model, "BinServiceLocal", "PropertyField", "propertiesField", "propertiesFields");

		System.out.println(" ********** \ncheck\n *********\n\n");
		model.check(false);
		// model.save();
	}

	private static Context cxt2(MigrationModel model) throws SecurityException, NoSuchMethodException {
		Context cxt = new Context(model);
		/*
		 * context { PropertyType X } => { change-type(X, PropertyAttribute) }
		 * 
		 * context { PropertySet X } => { change-link-type(X, propertiesType,
		 * propertiesAttributes) } context { BinAbstractService X } => {
		 * change-link-type(X, propertiesType, propertiesAttributes) } context {
		 * PropertyValue X } => { change-link-type(X, type, propertyAttribute) }
		 * context { PropertyValue X } => { change-link-type(X, type,
		 * propertyAttribute) }
		 * 
		 * 
		 * 
		 */

		ItemTypeVariable pt_type = new ItemTypeVariable(cxt, false, "PT_type", "PropertyType");

		ItemVarContext pt = new ItemVarContext(cxt, false, "PT", pt_type);
		ItemTypeVariable pa_type = new ItemTypeVariable(cxt, false, "PT_type", "PropertyAttribute");

		new InstChangeType(cxt, pt, pa_type);

		return cxt;
	}

	/*
	 * context { PropertyType X } => { change-type(X, PropertyAttribute) }
	 * 
	 * context { PropertySet X } => { change-link-type(X, propertiesType,
	 * propertiesAttributes) } context { BinAbstractService X } => {
	 * change-link-type(X, propertiesType, propertiesAttributes) } context {
	 * PropertyValue X } => { change-link-type(X, type, propertyAttribute) }
	 * context { PropertyValue X } => { change-link-type(X, type,
	 * propertyAttribute) }
	 * 
	 * 
	 * 
	 */

	public static void runChangeLinkType(Logger log, MigrationModel model, String sourceType, String destType,
			String oldLinkType, String newLinkType) throws SecurityException, NoSuchMethodException {
		Context cxt = new Context(model);

		ItemTypeVariable s_type = new ItemTypeVariable(cxt, false, "s_type", sourceType);

		ItemVarContext s = new ItemVarContext(cxt, false, "s", s_type);
		ItemTypeVariable d_type = new ItemTypeVariable(cxt, false, "d_type", destType);
		ItemVarContext d = new ItemVarContext(cxt, false, "d", d_type);

		LinkTypeVariable s_1_d = new LinkTypeVariable(cxt, false, "s_1_d", s_type, d_type, oldLinkType);

		LinkTypeVariable s_2_d = new LinkTypeVariable(cxt, false, "s_2_d", s_type, d_type, newLinkType);

		LinkVariable l1 = new LinkVariable(cxt, false, "l1", s, d, s_1_d);

		new InstChangeLinkType(cxt, l1, s_2_d);

		cxt.execute(log);

	}

	private static Context cxt1(MigrationModel model) throws SecurityException, NoSuchMethodException {
		Context cxt = new Context(model);
		/*
		 * 
		 * context { PropertyValue PV; ProvideService PS; BinServiceLocal BSL;
		 * PropertyType PT;
		 * 
		 * link (BSL -[providesService ]-> PS); l2 = link (PS -[propertiesValues
		 * ]-> PV); link (PV -[type]-> PT); } => { delete-link(l2);
		 * 
		 * create-link( BSL, PV, propertiesValue); create-link( PS, PT,
		 * propertiesAttributes); change-unique-name( PV, BSL.unique-name + "." +
		 * PV.short-name); }
		 * 
		 */

		ItemTypeVariable pv_type = new ItemTypeVariable(cxt, false, "PV_type", "PropertyValue");
		ItemTypeVariable ps_type = new ItemTypeVariable(cxt, false, "PS_type", "ProvideService");
		ItemTypeVariable bsl_type = new ItemTypeVariable(cxt, false, "BSL_type", "BinServiceLocal");
		ItemTypeVariable pt_type = new ItemTypeVariable(cxt, false, "PT_type", "PropertyType");

		LinkTypeVariable bsl_providesService_ps = new LinkTypeVariable(cxt, false, "bsl_providesService_ps", bsl_type,
				ps_type, "providesService");
		LinkTypeVariable ps_propertiesValues_pv = new LinkTypeVariable(cxt, false, "ps_propertiesValues_pv", ps_type,
				pv_type, "propertiesValues");
		LinkTypeVariable pv_type_pt = new LinkTypeVariable(cxt, false, "pv_type_pt", pv_type, pt_type, "type");

		ItemVarContext ps = new ItemVarContext(cxt, false, "PS", ps_type);
		ItemVarContext pv = new ItemVarContext(cxt, false, "PV", pv_type);
		ItemVarContext bsl = new ItemVarContext(cxt, false, "BSL", bsl_type);
		ItemVarContext pt = new ItemVarContext(cxt, false, "PT", pt_type);

		new LinkVariable(cxt, false, "l1", bsl, ps, bsl_providesService_ps);
		LinkVariable l2 = new LinkVariable(cxt, false, "l2", ps, pv, ps_propertiesValues_pv);
		new LinkVariable(cxt, false, "l3", pv, pt, pv_type_pt);

		/*
		 * delete-link(l2);
		 * 
		 * create-link( BSL, PV, propertiesValue); create-link( PS, PT,
		 * propertiesAttributes); String newname = Concat(BSL.unique-name , "." ,
		 * PV.short-name); change-unique-name( PV, newname);
		 * 
		 */
		LinkTypeVariable bsl_providesService_pv = new LinkTypeVariable(cxt, false, "bsl_providesService_ps", bsl_type,
				pv_type, "propertiesValues");
		LinkTypeVariable ps_propertiesValues_pt = new LinkTypeVariable(cxt, false, "ps_propertiesValues_pv", ps_type,
				pt_type, "propertiesAttributes");

		LinkVariable l4 = new LinkVariable(cxt, true, "l4", bsl, pv, bsl_providesService_pv);
		LinkVariable l5 = new LinkVariable(cxt, true, "l5", ps, pt, ps_propertiesValues_pt);
		new InstDeleteLink(cxt, l2);
		new InstCreateLink(cxt, l4);
		new InstCreateLink(cxt, l5);
		StringVariable newname = new StringVariable(cxt, true, "newname");
		new InstConcatString(cxt, newname, new UniqueNameVariable(cxt, true, "uu", bsl), new CstStringVariable(cxt,
				true, "dstu", "."), new ShortNameVariable(cxt, true, "sn", pv));
		new InstChangeUniqueName(cxt, pv, newname);
		return cxt;
	}

	private static Context cxt3(MigrationModel model) throws SecurityException, NoSuchMethodException {
		Context cxt = new Context(model);
		/*
		 * 
		 * context { PropertyField PV; ProvideService PS; BinServiceLocal BSL;
		 * PropertyAttribute PA;
		 * 
		 * link (BSL -[providesService ]-> PS); l2 = link (PS -[propertiesValues
		 * ]-> PV); link (PF -[propertyAttribute]-> PA); } => { delete-link(l2);
		 * 
		 * create-link( BSL, PF, propertiesValue); create-link( PS, PA,
		 * propertiesAttributes); change-unique-name( PV, BSL.unique-name + "." +
		 * PV.short-name); }
		 * 
		 */

		ItemTypeVariable pf_type = new ItemTypeVariable(cxt, true, "PF_type", "PropertyField");
		ItemTypeVariable ps_type = new ItemTypeVariable(cxt, true, "PS_type", "ProvideService");
		ItemTypeVariable bsl_type = new ItemTypeVariable(cxt, true, "BSL_type", "BinServiceLocal");
		ItemTypeVariable pa_type = new ItemTypeVariable(cxt, true, "PA_type", "PropertyAttribute");

		LinkTypeVariable bsl_providesService_ps = new LinkTypeVariable(cxt, true, "bsl_providesService_ps", bsl_type,
				ps_type, "providesService");
		LinkTypeVariable ps_propertiesValues_pf = new LinkTypeVariable(cxt, true, "ps_propertiesValues_pf", ps_type,
				pf_type, "propertiesFields");
		LinkTypeVariable pf_propertyAttribute_pa = new LinkTypeVariable(cxt, true, "pf_propertyAttribute_pa", pf_type,
				pa_type, "propertyAttribute");

		ItemVarContext ps = new ItemVarContext(cxt, true, "PS", ps_type);
		ItemVarContext pf = new ItemVarContext(cxt, true, "PV", pf_type);
		ItemVarContext bsl = new ItemVarContext(cxt, true, "BSL", bsl_type);
		ItemVarContext pa = new ItemVarContext(cxt, true, "PT", pa_type);

		new LinkVariable(cxt, true, "l1", bsl, ps, bsl_providesService_ps);
		LinkVariable l2 = new LinkVariable(cxt, true, "l2", ps, pf, ps_propertiesValues_pf);
		new LinkVariable(cxt, true, "l3", pf, pa, pf_propertyAttribute_pa);

		/*
		 * delete-link(l2);
		 * 
		 * create-link( BSL, PV, propertiesValue); create-link( PS, PT,
		 * propertiesAttributes); String newname = Concat(BSL.unique-name , "." ,
		 * PV.short-name); change-unique-name( PV, newname);
		 * 
		 */
		LinkTypeVariable bsl_providesService_pf = new LinkTypeVariable(cxt, true, "bsl_providesService_pf", bsl_type,
				pf_type, "propertiesValues");
		LinkTypeVariable ps_propertiesValues_pa = new LinkTypeVariable(cxt, true, "ps_propertiesValues_pa", ps_type,
				pa_type, "propertiesAttributes");

		LinkVariable l4 = new LinkVariable(cxt, true, "l4", bsl, pf, bsl_providesService_pf);
		LinkVariable l5 = new LinkVariable(cxt, true, "l5", ps, pa, ps_propertiesValues_pa);

		new InstDeleteLink(cxt, l2);
		new InstCreateLink(cxt, l4);
		new InstCreateLink(cxt, l5);
		StringVariable newname = new StringVariable(cxt, true, "newname");
		new InstConcatString(cxt, newname, new UniqueNameVariable(cxt, true, "uu", bsl), new CstStringVariable(cxt,
				true, ".", "."), new ShortNameVariable(cxt, true, "sn", pf));
		new InstChangeUniqueName(cxt, pf, newname);
		return cxt;
	}

	// public void complete() {
	// HashList<ItemVarContext, LinkVariable> incoming = new
	// HashList<ItemVarContext, LinkVariable>();
	// HashList<ItemVarContext, LinkVariable> outgoing = new
	// HashList<ItemVarContext, LinkVariable>();
	//
	// for (Variable v : this.vars) {
	// if (v instanceof LinkVariable) {
	// LinkVariable lv = (LinkVariable) v;
	// incoming.add(lv.getDestination(), lv);
	// outgoing.add(lv.getSource(), lv);
	// }
	//
	// }
	// for (Map.Entry<ItemVarContext, List<LinkVariable>> e :
	// outgoing.entrySet()) {
	// List<LinkVariable> c = e.getValue();
	// e.getKey().setIncoming((LinkVariable[]) c.toArray(new
	// LinkVariable[c.size()]));
	// }
	// for (Map.Entry<ItemVarContext, List<LinkVariable>> e :
	// incoming.entrySet()) {
	// List<LinkVariable> c = e.getValue();
	// e.getKey().setOutgoing((LinkVariable[]) c.toArray(new
	// LinkVariable[c.size()]));
	// }
	// ArrayList<Variable> vars2 = new ArrayList<Variable>();
	// for (Variable v : this.vars) {
	// if (vars2.contains(v))
	// continue;
	// vars2.add(v);
	// if (v instanceof ItemVarContext) {
	// ItemVarContext iv = (ItemVarContext) v;
	// addArray(vars2, iv.getIncoming());
	// addArray(vars2, iv.getOutgoing());
	// }
	// }
	// vars = vars2;
	//
	// }

	private final static int	NEXT_VAR	= 1;
	private final static int	VAR			= 2;
	private final static int	BACK_VAR	= 3;

	int							index		= -1;

	private ArrayList<Variable>	instvars	= new ArrayList<Variable>();	;

	public void begin() {
		this.arrayvars = (Variable[]) vars.toArray(new Variable[vars.size()]);
		this.indexs = new int[vars.size()];
		this.solutions = new Object[vars.size()][];
		index = -1;
		for (int i = 0; i < indexs.length; i++) {
			indexs[i] = -3;
			solutions[i] = null;
		}
	}

	public boolean next() {

		Object[] values;
		int action;
		if (index == -1) {
			reset();
			action = NEXT_VAR;
		} else if (index == -2) {
			return false;
		} else {
			// search a next solution
			// start at the end and search the next possible value.
			// if no value find, execute back var action
			index = arrayvars.length - 1;
			action = VAR;
		}

		Variable v = null;
		ONE: while (true) {
			switch (action) {
				case NEXT_VAR:
					index++;
					if (index == arrayvars.length) {
						return true; // find a solution
					}
					v = arrayvars[index];
					if (v.isResolved()) {
						n.put(v, v.getValue());
						indexs[index] = -2;
						action = NEXT_VAR;
						continue ONE;
					} else {
						if (solutions[index] == null) {
							List listvalues = v.getPossibleValues(model);

							if (listvalues == null) {
								log("possible values is null for " + v, index);
								action = BACK_VAR;
								continue ONE;
							}
							if (listvalues.size() == 0) {
								action = BACK_VAR;
								continue ONE;
							}
							Condition[] conds = v.getCondition();
							if (conds != null) {
								for (int i = 0; i < conds.length; i++) {
									Condition aCondEvaluater = conds[i];
									listvalues = aCondEvaluater.match(listvalues);
								}
								if (listvalues.size() == 0) {
									action = BACK_VAR;
									continue ONE;
								}
							}
							indexs[index] = -1;
							solutions[index] = listvalues.toArray();
						}
						action = VAR;
						continue ONE;
					}

				case VAR:
					values = solutions[index];
					if (values == null) {
						action = BACK_VAR;
						continue ONE;
					}
					v = arrayvars[index];
					v.reset();
					while (++indexs[index] < values.length) {
						Object aValue = values[indexs[index]];
						if (v.match(aValue)) {
							v.setValue(aValue);
							n.put(v, v.getValue());
							action = NEXT_VAR;
							continue ONE;
						} else {
							print_echec_solution();
						}
					}
					action = BACK_VAR;
					continue ONE;

				case BACK_VAR:
					arrayvars[index].reset();
					solutions[index] = null;
					indexs[index] = -3;
					index--;
					if (index == -1) {
						index = -2;
						return false; // no solution
					}
					action = VAR;
					continue ONE;

			}
		}
	}

	private void print_echec_solution() {
		// TODO Auto-generated method stub

	}

	private void log(String string, int index2) {
		// TODO Auto-generated method stub

	}

	private void addArray(ArrayList<Variable> vars2, LinkVariable[] incomming) {
		if (incomming != null) {
			for (LinkVariable v2 : incomming) {
				if (vars2.contains(v2)) {
					continue;
				}
				vars2.add(v2);
			}
		}
	}

	private static void printSol(Context cxt, Logger log) {
		log.finest("Find ");
		for (Variable v : cxt.arrayvars) {
			log.finest(v.toString());
		}
	}

	public void add(Variable variable) {
		namebyvars.put(variable.getName(), variable);
		if (!variable.isInstVariable()) {
			vars.add(variable);
		} else {
			instvars.add(variable);
		}
	}

	public void add(Instruction instruction) {
		insts.add(instruction);
	}

	public Variable getVar(String name) {
		return namebyvars.get(name);
	}

}
