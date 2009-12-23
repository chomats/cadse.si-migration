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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import fede.workspace.tool.migration.model.jaxb.MigCondNoParent;
import fede.workspace.tool.migration.model.jaxb.MigCondNotHasLink;
import fede.workspace.tool.migration.model.jaxb.MigCondition;
import fede.workspace.tool.migration.model.jaxb.MigContext;
import fede.workspace.tool.migration.model.jaxb.MigInsDuplicateItem;
import fede.workspace.tool.migration.model.jaxb.MigInst;
import fede.workspace.tool.migration.model.jaxb.MigInstCreateLink;
import fede.workspace.tool.migration.model.jaxb.MigInstDeleteItem;
import fede.workspace.tool.migration.model.jaxb.MigInstDeleteLink;
import fede.workspace.tool.migration.model.jaxb.MigItem;
import fede.workspace.tool.migration.model.jaxb.MigItemType;
import fede.workspace.tool.migration.model.jaxb.MigLink;
import fede.workspace.tool.migration.model.jaxb.MigLinkType;
import fede.workspace.tool.migration.model.jaxb.MigVariable;
import fede.workspace.tool.migration.model.jaxb.Migration;
import fede.workspace.tool.migration.var.CondNotHasLink;
import fede.workspace.tool.migration.var.Condition;
import fede.workspace.tool.migration.var.ItemTypeVariable;
import fede.workspace.tool.migration.var.ItemVarContext;
import fede.workspace.tool.migration.var.LinkTypeVariable;
import fede.workspace.tool.migration.var.LinkVariable;
import fede.workspace.tool.migration.var.NoPartParent;
import fede.workspace.tool.migration.var.Variable;
import fr.imag.adele.cadse.core.CadseException;
import java.util.UUID;
import fr.imag.adele.cadse.core.DerivedLinkDescription;
import fr.imag.adele.cadse.core.ItemDescription;
import fr.imag.adele.cadse.core.ItemDescriptionRef;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkDescription;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.impl.internal.Accessor;
import fr.imag.adele.cadse.core.util.HashList;

public class MigrationModel {

	static final String								ID_FILE_NAME		= "workspace-metadata.id";

	private Map<UUID, ItemDescription>		items;
	private Map<String, ItemDescription>			itemsbyname			= new HashMap<String, ItemDescription>();
	private HashList<UUID, ItemDescription>	itemsByType			= new HashList<UUID, ItemDescription>();
	private HashList<UUID, LinkDescription>	incomingLinkType	= new HashList<UUID, LinkDescription>();

	private LogicalWorkspace						cadsetype;

	Logger											log;

	private Collection<ItemDescription>				removeditems		= new ArrayList<ItemDescription>();

	public LogicalWorkspace getCadsetype() {
		return cadsetype;
	}

	public Collection<ItemDescription> getItems() {
		return items.values();
	}

	public MigrationModel() {
	}

	public int run(Logger log, LogicalWorkspace cadsetype, Map<UUID, ItemDescription> items, File cadsemig_xml,
			String[] cadseName, int[] cadseVersion) {
		try {
			int count = 0;
			this.log = log;
			Migration mig = read(cadsemig_xml);
			this.cadsetype = cadsetype;
			this.items = items;
			itemsbyname = new HashMap<String, ItemDescription>();
			itemsByType = new HashList<UUID, ItemDescription>();
			incomingLinkType = new HashList<UUID, LinkDescription>();
			resolve();
			for (MigContext cxt : mig.getCxt()) {
				count += run(cxt, cadseName, cadseVersion);
			}
			return count;
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	private int run(MigContext cxt, String[] cadseName, int[] cadseVersion) {
		String cxtCadseName = cxt.getCadseName();
		int cxtCadseVersion = cxt.getCadseVersion();
		int persistanceVersion = find(cxtCadseName, cadseName, cadseVersion);
		if (persistanceVersion == -1 || persistanceVersion >= cxtCadseVersion) {
			return 0;
		}

		Context myCxt = new Context(this);
		for (MigVariable migVar : cxt.getVar()) {
			Variable var = createVar(myCxt, false, migVar);
			if (var == null) {
				continue;
			}

			List<MigCondition> conditions = migVar.getConditions();
			List<Condition> condArray = new ArrayList<Condition>();
			for (MigCondition cond : conditions) {
				Condition c = createCond(cond, myCxt, var);
				if (c != null) {
					condArray.add(c);
				}
			}
			if (condArray.size() != 0) {
				var.setCondition(condArray.toArray(new Condition[condArray.size()]));
			}
		}
		for (MigVariable migVar : cxt.getVarInst()) {
			createVar(myCxt, true, migVar);
		}
		for (MigInst miginst : cxt.getInst()) {
			try {
				createinst(miginst, myCxt);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return myCxt.execute(log);
	}

	private int find(String cxtCadseName, String[] cadseName, int[] cadseVersion) {
		for (int i = 0; i < cadseVersion.length; i++) {
			if (cxtCadseName.equals(cadseName[i])) {
				return cadseVersion[i];
			}
		}
		return -1;
	}

	private Instruction createinst(MigInst miginst, Context myCxt) throws SecurityException, NoSuchMethodException {
		if (miginst instanceof MigInsDuplicateItem) {
			return new InstDuplicateItem(myCxt,
					(ItemVarContext) myCxt.getVar(((MigInsDuplicateItem) miginst).getVar()), (ItemVarContext) myCxt
							.getVar(((MigInsDuplicateItem) miginst).getVarSet()));
		}
		if (miginst instanceof MigInstCreateLink) {
			return new InstCreateLink(myCxt, (LinkVariable) myCxt.getVar(((MigInstCreateLink) miginst).getVar()));
		}
		if (miginst instanceof MigInstDeleteItem) {
			return new InstDeleteItem(myCxt, (ItemVarContext) myCxt.getVar(((MigInstDeleteItem) miginst).getVar()));
		}
		if (miginst instanceof MigInstDeleteLink) {
			return new InstDeleteLink(myCxt, (LinkVariable) myCxt.getVar(((MigInstDeleteLink) miginst).getVar()));
		}
		return null;
	}

	private Condition createCond(MigCondition cond, Context myCxt, Variable var) {
		if (cond instanceof MigCondNoParent) {
			return new NoPartParent(this);
		}
		if (cond instanceof MigCondNotHasLink) {
			return new CondNotHasLink((LinkTypeVariable) myCxt.getVar(((MigCondNotHasLink) cond).getLt()));
		}
		return null;
	}

	private Variable createVar(Context myCxt, boolean inst, MigVariable migVar) {
		if (migVar instanceof MigItemType) {
			return new ItemTypeVariable(myCxt, inst, migVar.getName(), new UUID(((MigItemType) migVar)
					.getTypeUuid()));
		}
		if (migVar instanceof MigItem) {
			return new ItemVarContext(myCxt, inst, migVar.getName(), (ItemTypeVariable) myCxt.getVar(((MigItem) migVar)
					.getType()));
		}
		if (migVar instanceof MigLinkType) {
			return new LinkTypeVariable(myCxt, inst, migVar.getName(), (ItemTypeVariable) myCxt
					.getVar(((MigLinkType) migVar).getSourceType()), (ItemTypeVariable) myCxt
					.getVar(((MigLinkType) migVar).getDestType()), ((MigLinkType) migVar).getLinktypeName());
		}
		if (migVar instanceof MigLink) {
			return new LinkVariable(myCxt, inst, migVar.getName(), (ItemVarContext) myCxt.getVar(((MigLink) migVar)
					.getSource()), (ItemVarContext) myCxt.getVar(((MigLink) migVar).getDest()),
					(LinkTypeVariable) myCxt.getVar(((MigLink) migVar).getType()));
		}
		return null;

	}

	private Migration read(File f) throws JAXBException, IOException {
		JAXBContext jc = JAXBContext.newInstance("fede.workspace.tool.migration.model.jaxb", this.getClass()
				.getClassLoader());
		Unmarshaller m = jc.createUnmarshaller();
		return (Migration) m.unmarshal(f);
	}

	public void changeUniqueName(ItemDescription desc, String newname) {

		itemsbyname.remove(desc.getQualifiedName());
		desc.setUniqueName(newname);
		itemsbyname.put(newname, desc);

	}

	/**
	 * Called by load
	 * 
	 */
	private void resolve() {
		for (ItemDescription desc : getItems()) {
			addItem(desc);
		}
	}

	private void addItem(ItemDescription desc) {
		this.itemsByType.add(desc.getType(), desc);
		if (desc.getQualifiedName() != null) {
			itemsbyname.put(desc.getQualifiedName(), desc);
		}
		for (LinkDescription link : desc.getLinks()) {
			ItemDescription dest = get(link.getDestination());
			if (dest != null) {
				link.setDestination(dest);
			}
			incomingLinkType.add(link.getDestination().getId(), link);

		}
	}

	public List<ItemDescription> getItems(ItemType it) {
		return this.itemsByType.get(it.getId());
	}

	public List<ItemDescription> getItemsType(UUID it) {
		return this.itemsByType.get(it);
	}

	public void deletelink(LinkDescription ld) throws CadseException {
		ItemDescription sourceDesc = get(ld.getSource().getId());
		if (sourceDesc == null) {
			throw new CadseException("Not found {0}.", ld.getSource().getQualifiedName());
		}
		sourceDesc.remove(ld);
		this.incomingLinkType.remove(ld.getDestination().getId(), ld);
	}

	public LinkDescription deletelink(String source, String dest, String lt) throws CadseException {
		ItemDescription sourceDesc = get(source);
		if (sourceDesc == null) {
			throw new CadseException("Not found {0}.", source);
		}
		ItemDescription destDesc = get(dest);
		if (destDesc == null) {
			throw new CadseException("Not found {0}.", dest);
		}
		LinkDescription removeld = null;
		for (LinkDescription ld : sourceDesc.getLinks()) {
			if (ld.getType().equals(lt) && ld.getDestination().getId().equals(destDesc.getId())) {
				removeld = ld;
				break;
			}
		}
		if (removeld != null) {
			sourceDesc.remove(removeld);
			this.incomingLinkType.remove(removeld.getDestination().getId(), removeld);
			return removeld;
		}
		return null;
	}

	private void changedest(LinkDescription ld, ItemDescription ldest, ItemDescription ldest_new, LinkType lt) {
		this.incomingLinkType.remove(ld.getDestination().getId(), ld);
		ld.setDestination(ldest_new);
		this.incomingLinkType.add(ldest_new.getId(), ld);
		if (lt.isPart()) {
			ldest_new.getAttributes().put(Accessor.ATTR_PARENT_ITEM_ID, ld.getSource().getId());
			ldest_new.getAttributes().put(Accessor.ATTR_PARENT_ITEM_TYPE_ID, ld.getType());
		}
	}

	/*
	 * 
	 * 
	 */

	public LinkDescription createlinkIfNeed(ItemDescription source, ItemDescription dest, String linkType)
			throws CadseException {

		LinkType lt = getLinkType(source, linkType);
		LinkDescription ret = foundOutgoingLink(source, dest, linkType);

		if (lt != null && lt.isPart()) {
			List<LinkDescription> linkParts = findparent(dest);
			if (ret == null) {
				if (linkParts.size() != 0) {
					throw new CadseException("Part found {0} : {1}", dest, linkParts);
				}
			} else {
				if (linkParts.size() > 1) {
					throw new CadseException("Part found {0} : {1}", dest, linkParts);
				}
				if (linkParts.size() == 1 && linkParts.get(0) != ret) {
					throw new CadseException("Part found {0} : {1}", dest, linkParts);
				}
			}
			dest.getAttributes().put(Accessor.ATTR_PARENT_ITEM_ID, source.getId());
			dest.getAttributes().put(Accessor.ATTR_PARENT_ITEM_TYPE_ID, lt.getName());
		}

		if (ret == null) {
			ret = new LinkDescription(source, linkType, dest);
			this.incomingLinkType.add(dest.getId(), ret);
		}

		return ret;
	}

	public LinkDescription createlink(String source, String dest, String lt) throws CadseException {
		ItemDescription sourceDesc = get(source);
		if (sourceDesc == null) {
			throw new CadseException("Not found {0}.", source);
		}
		ItemDescription destDesc = get(dest);
		if (destDesc == null) {
			throw new CadseException("Not found {0}.", dest);
		}
		LinkDescription ld = findLink(sourceDesc, destDesc, lt);
		if (ld == null) {
			ld = new LinkDescription(sourceDesc, lt, destDesc);
			this.incomingLinkType.add(destDesc.getId(), ld);
		} else {
			throw new CadseException("Allready find link {0} --({1})--> {2}", ld.getSource().getQualifiedName(), ld
					.getType(), ld.getDestination().getQualifiedName());
		}
		return ld;
	}

	private LinkDescription findLink(ItemDescription sourceDesc, ItemDescription destDesc, String lt) {
		for (LinkDescription link : sourceDesc.getLinks()) {
			if (link.getDestination().getId().equals(destDesc.getId()) && lt.equals(link.getType())) {
				return link;
			}
		}
		return null;
	}

	public void changeLinkType(String name, String oldtype, String type) {
		ItemType it = cadsetype.getItemTypeByName(name);
		if (it != null) {
			for (ItemDescription desc : items.values()) {
				if (!desc.getType().equals(name)) {
					continue;
				}
				changeLinkType(oldtype, type, desc);
			}
			return;
		}

		ItemDescription desc = getStringOrUUID(name);
		if (desc == null) {
			System.out.println("Not found item " + name);
			return;
		}
		changeLinkType(oldtype, type, desc);

	}

	protected void changeLinkType(String oldtype, String type, ItemDescription desc) {
		for (LinkDescription ld : desc.getLinks()) {
			if (ld.getType().equals(oldtype)) {
				println(" - delete ", desc, ld.getDestination(), ld.getType());
				ld.setType(type);
				println(" - create ", desc, ld.getDestination(), ld.getType());
			}
		}
	}

	public ItemDescription get(String name) {
		return getStringOrUUID(name);
	}

	public void showType(String type) {
		if (type != null) {
			show(cadsetype.getItemTypeByName(type));
		} else {
			for (ItemType it : cadsetype.getItemTypes()) {
				showheader(it);
			}
		}
	}

	private void showheader(ItemType it) {
		System.out.println("#ID         :" + it.getId());
	}

	private void show(ItemType itemType) {
		System.out.println("#ID         :" + itemType.getId());
		for (LinkType lt : itemType.getOutgoingLinkTypes()) {
			System.out.println("   - " + lt);
		}
	}

	public void changeType(ItemDescription source, UUID type) {
		UUID oldType = source.getType();
		this.itemsByType.remove(oldType, source);
		this.itemsByType.add(type, source);
		source.setType(type);
	}

	public void changeType(String oldtype, UUID type) {
		for (ItemDescription itemd : items.values()) {
			if (itemd.getType().equals(oldtype)) {
				itemd.setType(type);
				showHeader(itemd, "");
			}
			for (LinkDescription ldesc : itemd.getLinks()) {
				if (ldesc.getDestination().getType().equals(oldtype)) {
					ldesc.getDestination().setType(type);
					println("Change type ", ldesc.getSource(), ldesc.getDestination(), ldesc.getType());
				}
			}
		}
	}

	public ItemDescription getFromUniqueName(String id) {
		for (ItemDescription itemd : items.values()) {
			if (itemd.getQualifiedName().equals(id)) {
				return itemd;
			}
		}
		return null;
	}

	public void migrate() {
		for (ItemDescription desc : items.values()) {
			itemsbyname.put(desc.getQualifiedName(), desc);
		}

		for (ItemDescription desc : new ArrayList<ItemDescription>(items.values())) {

			// parent id
			String parentIdStr = (String) desc.getAttributes().get(Accessor.ATTR_PARENT_ITEM_ID);
			if (parentIdStr != null) {
				ItemDescription destdesc = itemsbyname.get(parentIdStr);
				if (destdesc != null) {
					desc.getAttributes().put(Accessor.ATTR_PARENT_ITEM_ID, destdesc.getId());
				}
			}

			for (LinkDescription ldesc : desc.getLinks()) {
				String oldid = ldesc.getDestination().getQualifiedName();
				ItemDescription destdesc = itemsbyname.get(oldid);
				if (destdesc != null) {
					ldesc.getDestination().setId(destdesc.getId());
					ldesc.getDestination().setShortname(destdesc.getName());

				}
			}

			for (DerivedLinkDescription il : desc.getDerived()) {
				String oldid = il.getDestination().getQualifiedName();
				ItemDescription destdesc = itemsbyname.get(oldid);
				if (destdesc != null) {
					il.getDestination().setId(destdesc.getId());
					il.getDestination().setShortname(destdesc.getName());
					il.getDestination().setType(destdesc.getType());
				}
			}

			for (ItemDescriptionRef il : desc.getComponents()) {
				String oldid = il.getQualifiedName();
				ItemDescription destdesc = itemsbyname.get(oldid);
				if (destdesc != null) {
					il.setId(destdesc.getId());
					il.setShortname(destdesc.getName());
				}
			}
		}
	}

	public void repare() {

		for (ItemDescription desc : items.values()) {
			if (desc.getName() == null) {
				desc.setShortname(desc.getQualifiedName());
			}
		}

		for (ItemDescription desc : new ArrayList<ItemDescription>(items.values())) {
			boolean showheader = false;
			List<LinkDescription> removeLink = new ArrayList<LinkDescription>();
			List<DerivedLinkDescription> removeDerived = new ArrayList<DerivedLinkDescription>();
			List<ItemDescriptionRef> removeComponents = new ArrayList<ItemDescriptionRef>();

			if (cadsetype != null) {
				ItemType it = getItemType(desc);
				if (it == null) {
					if (!showheader) {
						showHeaderErr(desc);
						showheader = true;
					}
					System.out.println("Remove item (no type): " + desc.getQualifiedName());
					remove(desc);
					continue;
				}
				if (it.hasIncomingParts()) {
					Object parentIdStr = desc.getAttributes().get(Accessor.ATTR_PARENT_ITEM_ID);
					if (parentIdStr == null) {
						if (!showheader) {
							showHeaderErr(desc);
							showheader = true;
						}
						System.out.println("Remove item (no parent) : " + desc.getQualifiedName());
						remove(desc);
						continue;
					} else {
						ItemDescription destdesc = getStringOrUUID(parentIdStr);
						if (destdesc == null) {
							if (!showheader) {
								showHeaderErr(desc);
								showheader = true;
							}
							System.out.println("Remove item (no parent) : " + desc.getQualifiedName());
							remove(desc);
							continue;
						}
					}
				}
				for (LinkDescription ldesc : desc.getLinks()) {
					String oldid = ldesc.getDestination().getQualifiedName();
					ItemDescription destdesc = itemsbyname.get(oldid);
					LinkType lt = it.getOutgoingLinkType(ldesc.getType());
					if (lt == null || destdesc == null) {
						removeLink.add(ldesc);
						if (!showheader) {
							showHeaderErr(desc);
							showheader = true;
						}
						System.out.println("Remove destination link: <" + ldesc.getDestination().getQualifiedName()
								+ ">");
					}

				}
			}
			// parent id
			Object parentIdStr = desc.getAttributes().get(Accessor.ATTR_PARENT_ITEM_ID);
			if (parentIdStr != null) {
				ItemDescription destdesc = getStringOrUUID(parentIdStr);
				if (destdesc == null) {
					desc.getAttributes().remove(Accessor.ATTR_PARENT_ITEM_ID);
					if (!showheader) {
						showHeaderErr(desc);
						showheader = true;
					}
					System.out.println("Remove parent attribut : <" + parentIdStr + ">");
				}
			}

			for (LinkDescription ldesc : desc.getLinks()) {
				String oldid = ldesc.getDestination().getQualifiedName();
				ItemDescription destdesc = itemsbyname.get(oldid);
				if (destdesc == null) {
					removeLink.add(ldesc);
					if (!showheader) {
						showHeaderErr(desc);
						showheader = true;
					}
					System.out.println("Remove destination link: <" + ldesc.getDestination().getQualifiedName() + ">");
				}

			}
			if (removeLink.size() != 0) {
				for (LinkDescription l : removeLink) {
					desc.remove(l);
				}
			}

			for (DerivedLinkDescription il : desc.getDerived()) {
				String oldid = il.getDestination().getQualifiedName();
				ItemDescription destdesc = itemsbyname.get(oldid);
				if (destdesc == null) {
					removeDerived.add(il);
					if (!showheader) {
						showHeaderErr(desc);
						showheader = true;
					}
					System.out.println("Remove derived link: " + il.getDestination().getQualifiedName());
				}
			}
			if (removeDerived.size() != 0) {
				desc.getDerived().removeAll(removeDerived);
			}

			for (ItemDescriptionRef il : desc.getComponents()) {
				String oldid = il.getQualifiedName();
				ItemDescription destdesc = itemsbyname.get(oldid);
				if (destdesc == null) {
					removeComponents.add(il);
					System.out.println("Remove component: " + il.getQualifiedName());
				}
			}
			if (removeLink.size() != 0) {
				desc.getComponents().removeAll(removeComponents);
			}
		}
	}

	void remove(ItemDescription desc) {
		items.remove(desc.getId());
		itemsbyname.remove(desc.getQualifiedName());
	}

	public void check(ItemDescription desc, boolean repare) {

		boolean showheader = false;
		if (desc.getName() == null) {
			desc.setShortname(desc.getQualifiedName());
			if (!showheader) {
				showHeaderErr(desc);
				showheader = true;
			}
			System.out.println("short name is null");
		}

		// parent id
		Object parentIdStr = desc.getAttributes().get(Accessor.ATTR_PARENT_ITEM_ID);
		ItemDescription parentdesc = null;
		if (parentIdStr != null) {
			parentdesc = getStringOrUUID(parentIdStr);
			if (parentdesc == null) {
				if (!showheader) {
					showHeaderErr(desc);
					showheader = true;
				}
				System.out.println("Bad parent : <" + parentIdStr + ">");
			}
		}

		for (LinkDescription ldesc : desc.getLinks()) {
			ItemDescription destdesc = get(ldesc.getDestination());

			if (destdesc == null) {
				if (!showheader) {
					showHeaderErr(desc);
					showheader = true;
				}
				System.out.println("Link destination not found: <" + ldesc.getDestination().getQualifiedName() + ">");
			}
		}

		for (DerivedLinkDescription il : desc.getDerived()) {
			ItemDescription destdesc = get(il.getDestination());

			if (destdesc == null) {
				if (!showheader) {
					showHeaderErr(desc);
					showheader = true;
				}
				System.out.println("Bad derived link: " + il.getDestination().getQualifiedName());
			}
		}

		for (ItemDescriptionRef il : desc.getComponents()) {
			ItemDescription destdesc = get(il);
			if (destdesc == null) {
				if (!showheader) {
					showHeaderErr(desc);
					showheader = true;
				}
				System.out.println("Bad component: " + il.getQualifiedName());
			}
		}
		if (cadsetype != null) {
			ItemType it = getItemType(desc);
			if (it == null) {
				if (!showheader) {
					showHeader(desc, "");
					showheader = true;
				}
				System.out.println("-Not found item type: " + desc.getType());
			} else {
				for (LinkDescription ldesc : desc.getLinks()) {
					if (ldesc.getType().startsWith("#")) {
						continue;
					}

					LinkType lt = it.getOutgoingLinkType(ldesc.getType());
					if (lt == null) {
						if (!showheader) {
							showHeaderErr(desc);
							showheader = true;
						}
						System.out.println("-Not found linkType: <" + ldesc.getType() + ">");
						System.out.println("               For : " + ldesc);
						ItemType dest = getItemType(ldesc.getDestination());
						if (dest != null) {
							ArrayList<LinkType> lts = getOutgoingLinkType(it, dest);
							if (lts.size() != 0) {
								for (LinkType foundLT : lts) {
									println("Found link ", desc, ldesc.getDestination(), foundLT.getName());
								}
							}
						}
					}
				}

				if (it.hasIncomingParts()) {
					List<LinkDescription> parents = findparent(desc);
					if (parents.size() == 0) {
						if (!showheader) {
							showHeaderErr(desc);
							showheader = true;
						}
						System.out.println("-Part no link found, type must be: " + toStringIncomingPart(it));
						if (parentdesc != null) {
							System.out.println("Parent found with parent attribute id: "
									+ parentdesc.getQualifiedName());
							LinkType lt = findPartLinkType(parentdesc, desc);
							if (lt != null) {
								if (repare) {
									new LinkDescription(parentdesc, lt.getName(), desc);
									desc.getAttributes().put(Accessor.ATTR_PARENT_ITEM_ID, parentdesc.getId());
									System.out.flush();
									println("Create link ", parentdesc, desc, lt.getName());
									System.out.flush();

								} else {
									System.out.flush();
									System.out.println("Can create link " + parentdesc.getQualifiedName() + " --("
											+ lt.getName() + ")--> " + desc.getQualifiedName());
									System.out.flush();
								}

							}
						}
					} else if (parents.size() > 1) {
						if (!showheader) {
							showHeaderErr(desc);
							showheader = true;
						}
						System.out.println("Parent type: " + toStringIncomingPart(it));
						System.out.println("Parent found parent most one: ");
						for (LinkDescription pdesc : parents) {
							System.out.println("  - " + pdesc.getSource().getQualifiedName());
						}
						if (parentdesc != null) {
							System.out.println("Parent found with parent attribute id: "
									+ parentdesc.getQualifiedName());
							LinkType lt = findPartLinkType(parentdesc, desc);
							if (lt != null) {
								if (repare) {
									new LinkDescription(parentdesc, lt.getName(), desc);
									desc.getAttributes().put(Accessor.ATTR_PARENT_ITEM_ID, parentdesc.getId());
									System.out.println("Create link " + parentdesc.getQualifiedName() + " --("
											+ lt.getName() + ")--> " + desc.getQualifiedName());
									for (LinkDescription pdesc : parents) {
										((ItemDescription) pdesc.getSource()).getLinks().remove(pdesc);
										System.out.println("Delete link  " + pdesc.getSource().getQualifiedName()
												+ " --(" + pdesc.getType() + ")--> "
												+ pdesc.getDestination().getQualifiedName());
									}
								}
							}
						}
					} else if (parents.size() == 1) {
						ItemDescription parentfound = (ItemDescription) parents.get(0).getSource();
						if (parentdesc == null) {
							if (!showheader) {
								showHeaderErr(desc);
								showheader = true;
							}
							System.out.println("Found parent but parent id is bad or null :"
									+ parentfound.getQualifiedName());
							desc.getAttributes().put(Accessor.ATTR_PARENT_ITEM_ID, parentfound.getId());
						} else {
							if (parentdesc.getId().equals(parentfound.getId())) {

							} else {
								if (!showheader) {
									showHeaderErr(desc);
									showheader = true;
								}
								System.out.println("Found parent but parent id is not valid:"
										+ parentfound.getQualifiedName());
								System.out.println("parent found is:" + parentfound.getQualifiedName());
								System.out.println("parent attribut id:" + parentdesc.getQualifiedName());
								desc.getAttributes().put(Accessor.ATTR_PARENT_ITEM_ID, parentfound.getId());
							}
						}
					}
				} else {
					if (parentIdStr != null) {
						if (!showheader) {
							showHeaderErr(desc);
							showheader = true;
						}
						System.out.println("Has no part parent, parent id attribut is bad (remove)");
						desc.getAttributes().remove(Accessor.ATTR_PARENT_ITEM_ID);
					}
				}

				for (DerivedLinkDescription il : desc.getDerived()) {
					ItemType derivedIt;
					derivedIt = cadsetype.getItemType(il.getDestination().getType());
					if (derivedIt == null) {
						if (!showheader) {
							showHeaderErr(desc);
							showheader = true;
						}
						System.out.println("Bad derived dest type: " + il.getDestination().getType());
					}
				}
			}
		}

	}

	private ArrayList<LinkType> getOutgoingLinkType(ItemType it, ItemType dest) {
		LinkType foundLT = null;
		ArrayList<LinkType> ret = new ArrayList<LinkType>();

		for (LinkType lt : it.getOutgoingLinkTypes()) {
			if (lt.getDestination() == dest) {
				ret.add(lt);
			}
		}
		return ret;
	}

	public void check(String name, boolean repare) {
		check(get(name), repare);
	}

	public void check(boolean repare) {

		for (ItemDescription desc : items.values()) {
			check(desc, repare);
		}
	}

	private void println(String msg, ItemDescriptionRef source, ItemDescriptionRef dest, String lt) {
		System.out.println(msg + source.getQualifiedName() + " --(" + lt + ")--> " + dest.getQualifiedName());
	}

	private void println(String msg, LinkDescription ld) {
		System.out.println(msg + ld.getSource().getQualifiedName() + " --(" + ld.getType() + ")--> "
				+ ld.getDestination().getQualifiedName());
	}

	private ItemDescription get(ItemDescriptionRef il) {
		UUID uuid_id = il.getId();
		ItemDescription destdesc = items.get(uuid_id);
		if (destdesc == null) {
			String unique_name_id = il.getQualifiedName();
			destdesc = itemsbyname.get(unique_name_id);
		}
		return destdesc;
	}

	private LinkType findPartLinkType(ItemDescription parentdesc, ItemDescription desc) {
		ItemType sourceit = getItemType(parentdesc);
		ItemType destit = getItemType(desc);
		if (sourceit == null) {
			return null;
		}
		if (destit == null) {
			return null;
		}

		return destit.getIncomingPart(sourceit);
	}

	private LinkType getLinkType(ItemDescription itemDesc, String linkType) {
		ItemType it = getItemType(itemDesc);
		if (it == null) {
			return null;
		}
		return it.getOutgoingLinkType(linkType);
	}

	private LinkType getLinkType(LinkDescription ldesc) {
		ItemType it = getItemType(ldesc.getSource());
		if (it == null) {
			return null;
		}
		return it.getOutgoingLinkType(ldesc.getType());
	}

	private LinkDescription[] getIncoming(ItemDescription destdesc) {
		List<LinkDescription> ret = new ArrayList<LinkDescription>();
		UUID destid = destdesc.getId();
		for (ItemDescription desc : items.values()) {
			for (LinkDescription ldesc : desc.getLinks()) {
				UUID ldestid = ldesc.getDestination().getId();
				if (ldestid != null && ldestid.equals(destid)) {
					ret.add(ldesc);
				}
			}
		}
		return ret.toArray(new LinkDescription[ret.size()]);
	}

	private String toStringIncomingPart(ItemType it) {
		StringBuilder sb = new StringBuilder();
		for (LinkType lt : it.getIncomingLinkTypes()) {
			if (lt.isPart()) {
				sb.append(" ").append(lt.getSource().getId()).append("::").append(lt.getName());
			}
		}
		return sb.toString();
	}

	public ItemDescription getStringOrUUID(Object parentIdStr) {
		ItemDescription ret = null;
		try {
			UUID uuid = null;
			if (parentIdStr instanceof String) {
				uuid = UUID.fromString((String) parentIdStr);
			} else if (parentIdStr instanceof UUID) {
				uuid = (UUID) parentIdStr;
			}
			if (uuid != null) {
				ret = items.get(uuid);
			}
		} catch (Throwable e) {
		}
		if (ret == null) {
			ret = itemsbyname.get(parentIdStr);
		}
		return ret;
	}

	public ItemDescription get(UUID uuid) {
		ItemDescription ret = null;
		ret = items.get(uuid);
		return ret;
	}

	public void showItemFrom(String oldid) throws Throwable {
		ItemDescription desc = itemsbyname.get(oldid);
		if (desc == null) {
			System.out.println("Not found item with id " + oldid);
		}
		showHeader(desc, "");
	}

	public void showItemFrom(ItemDescription desc) throws Throwable {
		if (desc == null) {
			System.out.println("Not found item");
		}
		showHeader(desc, "");
	}

	public void showAllHeader() {
		for (ItemDescription desc : items.values()) {
			showHeader(desc, "");
			System.out.println();
		}
	}

	public void showAllComponents() {
		for (ItemDescription desc : items.values()) {
			ItemType it = getItemType(desc);
			if (!it.isComposite()) {
				continue;
			}
			showComponents(desc);
			System.out.println();
		}
	}

	private void showComponents(ItemDescription desc) {
		System.out.println("#ID         :" + desc.getId().toString());
		System.out.println("#Unique name:" + desc.getQualifiedName());
		for (ItemDescriptionRef il : desc.getComponents()) {
			System.out.println(" - " + il.getQualifiedName());
		}
	}

	public void showAllDerived() {
		for (ItemDescription desc : items.values()) {
			ItemType it = getItemType(desc);
			if (!it.isComposite()) {
				continue;
			}
			showDeriveds(desc);
			System.out.println();
		}
	}

	private void showDeriveds(ItemDescription desc) {
		System.out.println("#ID         :" + desc.getId().toString());
		System.out.println("#Unique name:" + desc.getQualifiedName());
		for (DerivedLinkDescription il : desc.getDerived()) {
			System.out.println(" - [" + (il.isAggregation() ? "a" : " ") + (il.isRequire() ? "r" : " ") + "] "
					+ il.getSource().getType() + " --(" + il.getType() + ")--> " + il.getDestination().getType());
			System.out.println("   : " + il.getDestination().getQualifiedName() + " / " + il.getDestination().getName()
					+ " / " + il.getDestination().getId());
		}
	}

	private void showHeader(ItemDescription desc, String tab) {
		System.out.println(tab + "#ID         :" + desc.getId().toString());
		System.out.println(tab + "#Unique name:" + desc.getQualifiedName());
		System.out.println(tab + "#Short name :" + desc.getName());
		if (cadsetype != null) {
			ItemType it = getItemType(desc);
			if (it == null) {
				System.out.println(tab + "#Type       :" + desc.getType() + " (not found)");
			} else {
				System.out.println(tab + "#Type       :" + desc.getType() + " (found)");
			}
		} else {
			System.out.println(tab + "#Type       :" + desc.getType());
		}
	}

	public void show(ItemDescription desc, String tab) {
		System.out.println(tab + "#ID         :" + desc.getId().toString());
		System.out.println(tab + "#Unique name:" + desc.getQualifiedName());
		System.out.println(tab + "#Short name :" + desc.getName());
		if (cadsetype != null) {
			ItemType it = getItemType(desc);
			if (it == null) {
				System.out.println(tab + "#Type       :" + desc.getType() + " (not found)");
			} else {
				System.out.println(tab + "#Type       :" + desc.getType() + " (found)");
			}
		} else {
			System.out.println(tab + "#Type       :" + desc.getType());
		}
		for (LinkDescription l : desc.getLinks()) {
			System.out.println(tab + " -- (" + l.getType() + ") --> " + l.getDestination().getType() + " "
					+ l.getDestination().getQualifiedName() + " " + l.getDestination().getId());
		}
	}

	public void showHeaderErr(ItemDescription desc) {
		System.out.println();
		showHeader(desc, "");
	}

	public void renameParentID(String old, String newname) throws Throwable {
		ItemDescription parentdesc = getStringOrUUID(newname);
		if (parentdesc == null) {
			System.out.println("Cannot find item : <" + newname + ">");
			return;
		}
		for (ItemDescription desc : items.values()) {
			String parentIdStr = (String) desc.getAttributes().get(Accessor.ATTR_PARENT_ITEM_ID);
			if (old.equals(parentIdStr)) {
				List<LinkDescription> parents = findparent(desc);
				LinkType lt = findPartLinkType(parentdesc, desc);
				if (lt != null) {
					new LinkDescription(parentdesc, lt.getName(), desc);
					desc.getAttributes().put(Accessor.ATTR_PARENT_ITEM_ID, parentdesc.getId());
					System.out.println("Create link " + parentdesc.getQualifiedName() + " --(" + lt.getName() + ")--> "
							+ desc.getQualifiedName());
					for (LinkDescription pdesc : parents) {
						((ItemDescription) pdesc.getSource()).getLinks().remove(pdesc);
						System.out.println("Delete link  " + pdesc.getSource().getQualifiedName() + " --("
								+ pdesc.getType() + ")--> " + pdesc.getDestination().getQualifiedName());
					}
				} else {
					System.out.println("Cannot find the part link type form " + parentdesc.getType() + " to "
							+ desc.getType());
				}
			}
		}
	}

	public void recomputeComponents() throws Throwable {
		HashSet<ItemDescription> consomed = new HashSet<ItemDescription>();
		if (cadsetype == null) {
			return;
		}
		for (ItemDescription desc : items.values()) {
			ItemType it = getItemType(desc);
			if (it == null) {
				continue;
			}
			if (!it.isComposite()) {
				continue;
			}
			if (consomed.contains(desc)) {
				continue;
			}
			recomputeComponent(desc, consomed);
		}
	}

	public void recomputeComponent(ItemDescription desc, HashSet<ItemDescription> consomed) {
		ItemType it = getItemType(desc);
		desc.getComponents().clear();
		for (LinkDescription ldesc : desc.getLinks()) {
			LinkType lt = it.getOutgoingLinkType(ldesc.getType());
			if (lt == null) {
				continue;
			}
			if (!lt.isComposition()) {
				continue;
			}
			ItemDescription destdesc = items.get(ldesc.getDestination().getId());
			if (destdesc == null) {
				throw new IllegalArgumentException(desc.getQualifiedName() + " -> "
						+ ldesc.getDestination().getQualifiedName() + " (" + ldesc.getType() + ") is un resolved.");
			}
			ItemType itdest = getItemType(destdesc);
			if (itdest == null) {
				throw new IllegalArgumentException(destdesc.getQualifiedName() + " (" + destdesc.getType()
						+ ") type is un resolved.");
			}

			desc.getComponents().add(destdesc);
			if (!consomed.contains(destdesc)) {
				recomputeComponent(destdesc, consomed);
			}
			desc.getComponents().addAll(destdesc.getComponents());
		}
		consomed.add(desc);
	}

	public void recomputeDeriveds() throws Throwable {
		HashSet<ItemDescription> consomed = new HashSet<ItemDescription>();
		if (cadsetype == null) {
			return;
		}
		for (ItemDescription desc : items.values()) {
			ItemType it = getItemType(desc);
			if (consomed.contains(desc)) {
				continue;
			}
			desc.getDerived().clear();
			if (it == null) {
				continue;
			}
			if (!it.isComposite()) {
				continue;
			}
			recomputeDerived(desc, consomed);
		}
	}

	private void recomputeDerived(ItemDescription desc, HashSet<ItemDescription> consomed) {
		ItemType it = getItemType(desc);
		desc.getDerived().clear();
		for (LinkDescription ldesc : desc.getLinks()) {
			LinkType lt = it.getOutgoingLinkType(ldesc.getType());
			if (lt == null) {
				continue;
			}
			if (!lt.isComposition()) {
				continue;
			}
			ItemDescription destdesc = items.get(ldesc.getDestination().getId());
			if (destdesc == null) {
				throw new IllegalArgumentException(desc.getQualifiedName() + " -> "
						+ ldesc.getDestination().getQualifiedName() + " (" + ldesc.getType() + ") is un resolved.");
			}
			ItemType itdest = getItemType(destdesc);
			if (itdest == null) {
				throw new IllegalArgumentException(destdesc.getQualifiedName() + " (" + destdesc.getType()
						+ ") type is un resolved.");
			}
			for (LinkDescription ldesc_dest : destdesc.getLinks()) {
				LinkType lt2 = itdest.getOutgoingLinkType(ldesc_dest.getType());
				if (lt2 == null) {
					throw new IllegalArgumentException(desc.getQualifiedName() + " -> "
							+ ldesc.getDestination().getQualifiedName() + " (" + ldesc.getType() + ") is un resolved.");
				}
				if (lt2.isComposition()) {
					continue;
				}
				UUID uuid = ldesc_dest.getDestination().getId();
				if (composentcontains(desc, uuid)) {
					continue;
				}
				// it's a derived link
				DerivedLinkDescription il = new DerivedLinkDescription(desc, null, ldesc_dest.getDestination(), lt2
						.isAggregation(), lt2.isRequire(), null, lt2.getName(), lt2.getSource().getId(), lt2
						.getDestination().getId(), 0);

				desc.getDerived().add(il);
			}
			if (!consomed.contains(destdesc)) {
				recomputeDerived(destdesc, consomed);
			}
			for (DerivedLinkDescription il : destdesc.getDerived()) {
				UUID uuid = il.getDestination().getId();
				if (composentcontains(desc, uuid)) {
					continue;
				}
				DerivedLinkDescription dil = new DerivedLinkDescription(desc, null, il.getDestination(), il
						.isAggregation(), il.isRequire(), null, il.getType(), destdesc.getType(), il
						.getOriginLinkDestinationTypeID(), 0);
				desc.getDerived().add(dil);
			}
		}
		consomed.add(desc);
	}

	private boolean composentcontains(ItemDescription desc, UUID uuid) {
		for (ItemDescriptionRef il : desc.getComponents()) {
			if (il.getId().equals(uuid)) {
				return true;
			}
		}
		return false;
	}

	private ItemType getItemType(ItemDescriptionRef desc) {
		ItemType it = cadsetype.getItemType(desc.getType());
		return it;
	}

	public LinkDescription foundOutgoingLink(ItemDescription s, ItemDescription d, String type) {
		for (LinkDescription link : s.getLinks()) {
			if (link.getType().equals(type)) {
				if (d == null || link.getDestination().getId().equals(d.getId())) {
					return link;
				}
			}
		}
		return null;
	}

	public List<LinkDescription> foundOutgoingLinks(ItemDescription s, ItemDescription d, String type) {
		List<LinkDescription> ret = new ArrayList<LinkDescription>();
		for (LinkDescription link : s.getLinks()) {
			if (link.getType().equals(type) && link.getDestination().getId().equals(d.getId())) {
				ret.add(link);
			}

		}
		return ret;
	}

	public List<LinkDescription> foundOutgoingLinks(ItemDescription s, String type) {
		List<LinkDescription> ret = new ArrayList<LinkDescription>();
		for (LinkDescription link : s.getLinks()) {
			if (link.getType().equals(type)) {
				ret.add(link);
			}
		}
		return ret;
	}

	public List<LinkDescription> foundOutgoingLinks(ItemDescription s, ItemDescription d) {
		List<LinkDescription> ret = new ArrayList<LinkDescription>();
		for (LinkDescription link : s.getLinks()) {
			if (link.getDestination().getId().equals(d.getId())) {
				ret.add(link);
			}
		}
		return ret;
	}

	public List<LinkDescription> foundIncomingLinks(ItemDescription d, String type) {
		List<LinkDescription> ret = new ArrayList<LinkDescription>();
		List<LinkDescription> v = incomingLinkType.get(d.getId());
		if (v != null) {
			for (LinkDescription link : v) {
				if (link.getType().equals(type)) {
					ret.add(link);
				}
			}
		}
		return ret;
	}

	public List<LinkDescription> findparent(ItemDescription desc) {
		List<LinkDescription> incoming = foundIncomingLinks(desc);
		List<LinkDescription> ret = new ArrayList<LinkDescription>();
		for (LinkDescription ldesc : incoming) {
			LinkType lt = getLinkType(ldesc);
			if (lt == null) {
				continue;
			}
			if (!lt.isPart()) {
				continue;
			}
			ret.add(ldesc);
		}
		return ret;
	}

	public List<LinkDescription> foundIncomingLinks(ItemDescription d) {
		List<LinkDescription> v = incomingLinkType.get(d.getId());
		if (v == null) {
			return Collections.emptyList();
		}
		return v;
	}

	public List<LinkDescription> foundIncomingLinks(ItemDescription d, String linktype, String sourcetype) {
		List<LinkDescription> ret = new ArrayList<LinkDescription>();
		List<LinkDescription> v = incomingLinkType.get(d.getId());
		if (v != null) {
			for (LinkDescription link : v) {
				if (link.getType().equals(linktype) && link.getSource().getType().equals(sourcetype)) {
					ret.add(link);
				}
			}
		}
		return ret;
	}

	public LinkDescription foundIncomingLink(ItemDescription d, String type) {
		List<LinkDescription> v = incomingLinkType.get(d.getId());
		if (v != null) {
			for (LinkDescription link : v) {
				if (link.getType().equals(type)) {
					return link;
				}
			}
		}
		return null;
	}

	public ItemDescription get(UUID id) {
		return items.get(id);
	}

	public List foundOutgoingLinks(ItemDescription item) {
		return item.getLinks();
	}

	public ItemDescription resolveLink(LinkDescription description) {
		UUID destid = description.getDestination().getId();
		ItemDescription dest = get(destid);
		if (dest != null && description.getDestination() != dest) {
			description.setDestination(dest);
		}
		return dest;
	}

	public boolean equals(LinkDescription l1, LinkDescription l2) {
		return l1 == l2;
	}

	public boolean equals(ItemDescription i1, ItemDescription i2) {
		return i1 == i2;
	}

	public ItemDescription duplicateItem(ItemDescription item) {
		ItemDescription ret = duplicateItem_(item);
		List<ItemDescription> rec = new ArrayList<ItemDescription>();
		rec.add(ret);
		while (rec.size() != 0) {
			ItemDescription desc = rec.remove(rec.size() - 1);
			for (LinkDescription ld : desc.getLinks()) {
				LinkType lt = getLinkType(ld);
				if (lt == null) {
					continue;
				}
				if (!lt.isPart()) {
					continue;
				}
				ItemDescription ldest = get(ld.getDestination());
				if (ldest == null) {
					continue;
				}
				ItemDescription ldest_new = duplicateItem_(ldest);
				changedest(ld, ldest, ldest_new, lt);
				rec.add(ldest_new);
			}
		}
		return ret;
	}

	private ItemDescription duplicateItem_(ItemDescription item) {
		ItemDescription ret = new ItemDescription(UUID.randomUUID(), item);
		items.put(ret.getId(), ret);
		addItem(ret);
		return ret;
	}

	/**
	 * detruit l'item et ses parts ...
	 * 
	 * @param desc
	 */
	public void deleteItem(ItemDescription desc) {
		deleteItem_(desc);
		List<ItemDescription> rec = new ArrayList<ItemDescription>();
		rec.add(desc);
		while (rec.size() != 0) {
			ItemDescription _desc = rec.remove(rec.size() - 1);
			for (LinkDescription ld : _desc.getLinks()) {
				LinkType lt = getLinkType(ld);
				if (lt == null) {
					continue;
				}
				if (!lt.isPart()) {
					continue;
				}
				ItemDescription ldest = get(ld.getDestination());
				if (ldest == null) {
					continue;
				}
				deleteItem_(ldest);
				rec.add(ldest);
			}
		}
	}

	private void deleteItem_(ItemDescription desc) {
		this.itemsByType.remove(desc.getType(), desc);
		if (desc.getQualifiedName() != null) {
			itemsbyname.remove(desc.getQualifiedName());
		}
		for (LinkDescription link : desc.getLinks()) {
			incomingLinkType.remove(link.getDestination().getId(), link);
		}
		ItemDescription ret = items.remove(desc.getId());
		removeditems.add(desc);
	}

	public Collection<ItemDescription> getDeleteItems() {
		return removeditems;
	}

}

/*
 * AS ----> PS ----->PV ----> PA || || VV
 * 
 * AS ----> PS ----> PA ----> PV ----> PA
 * 
 * commande executer pour faire la transformation d'un cas particulier (un
 * instance)
 * 
 * load-type /home/chomats/ws-cadseg3-new/Model.Workspace.EnvMelusine load
 * /home/chomats/ws-cadseg3-new/Model.Workspace.EnvMelusine/resources/melusine.ser
 * change-type PropertyType PropertyAttribute change-linktype PropertySet
 * propertiesType propertiesAttributes change-linktype BinAbstractService
 * propertiesType propertiesAttributes change-linktype PropertyField type
 * propertyAttribute change-linktype PropertyValue type propertyAttribute
 * change-linktype BinServiceLocal propertiesValue propertiesValues
 * 
 * create-link SI.Melusine.ServiceInstanceBroker
 * SI.Melusine.ServiceInstanceBroker.serviceInstanceBroker.sam.type
 * propertiesValue change-unique-name
 * SI.Melusine.ServiceInstanceBroker.serviceInstanceBroker.sam.type
 * SI.Melusine.ServiceInstanceBroker.sam.type delete-link
 * SI.Melusine.ServiceInstanceBroker.serviceInstanceBroker
 * SI.Melusine.ServiceInstanceBroker.sam.type propertiesValue create-link
 * SI.Melusine.ServiceInstanceBroker.serviceInstanceBroker sam.sam.type
 * propertiesAttributes
 * 
 * delete-link SI.Melusine.Machine.machine cc79e3bb-bf98-440c-b7cf-7381c2c0a8ed
 * propertiesValues delete-link SI.Melusine.Machine.machine
 * 88cc374e-95a9-4637-a2a7-25f74cb33189 propertiesField delete-link
 * SI.Melusine.Machine.machine 222875f9-2c8d-490d-9380-bf9890b27409
 * propertiesField
 * 
 * create-link SI.Melusine.Machine SI.Melusine.Machine.machine.sam.type
 * propertiesValues create-link SI.Melusine.Machine
 * SI.Melusine.Machine.machine.machineName propertiesFields create-link
 * SI.Melusine.Machine SI.Melusine.Machine.machine.machineHost propertiesFields
 * 
 * create-link SI.Melusine.Machine.machine sam.machine.host propertiesAttributes
 * create-link SI.Melusine.Machine.machine sam.machine.name propertiesAttributes
 * create-link SI.Melusine.Machine.machine sam.sam.type propertiesAttributes
 * 
 * change-unique-name SI.Melusine.Machine.machine.machineName
 * SI.Melusine.Machine.machineName change-unique-name
 * SI.Melusine.Machine.machine.machineHost SI.Melusine.Machine.machineHost
 * change-unique-name SI.Melusine.Machine.machine.sam.type
 * SI.Melusine.Machine.sam.type
 * 
 * delete-link SI.Melusine.AbstractServiceBroker.abstractServiceBroker
 * 7107891f-b38c-4e0c-a6bd-92343ef2a6c2 propertiesValues create-link
 * SI.Melusine.AbstractServiceBroker 7107891f-b38c-4e0c-a6bd-92343ef2a6c2
 * propertiesValue create-link
 * SI.Melusine.AbstractServiceBroker.abstractServiceBroker sam.sam.type
 * propertiesAttributes change-unique-name
 * SI.Melusine.AbstractServiceBroker.abstractServiceBroker.sam.type
 * SI.Melusine.AbstractServiceBroker.sam.type
 * 
 * delete-link SI.Melusine.ServiceInstanceBroker.serviceInstanceBroker
 * SI.Melusine.ServiceInstanceBroker.sam.type propertiesValues create-link
 * SI.Melusine.ServiceInstanceBroker SI.Melusine.ServiceInstanceBroker.sam.type
 * propertiesValue create-link
 * SI.Melusine.ServiceInstanceBroker.serviceInstanceBroker sam.sam.type
 * propertiesAttributes change-unique-name
 * SI.Melusine.ServiceInstanceBroker.serviceInstanceBroker.sam.type
 * SI.Melusine.ServiceInstanceBroker.sam.type * fin **
 * 
 * 
 * les regles logiques pourrait �tre celle-ci.
 * 
 * 
 * context { PropertyType X } => { change-type(X, PropertyAttribute) }
 * 
 * context { PropertySet X } => { change-link-type(X, propertiesType,
 * propertiesAttributes) } context { BinAbstractService X } => {
 * change-link-type(X, propertiesType, propertiesAttributes) } context {
 * PropertyValue X } => { change-link-type(X, type, propertyAttribute) } context {
 * PropertyValue X } => { change-link-type(X, type, propertyAttribute) }
 * 
 * context { PropertyValue PV; ProvideService PS; BinAbstractService AS;
 * PropertyAttribute PA;
 * 
 * link (AS -[propertiesValue ]-> PS); link (PS -[propertiesValues ]-> PV); link
 * (PV -[propertyAttribute]-> PA); } => { delete-link(PS,PV, propertiesValues);
 * 
 * create-link( AS, PV, propertiesValue); create-link( PS, PA,
 * propertiesAttributes); change-unique-name( PV, AS.unique-name + "." +
 * PV.short-name); }
 * 
 */
