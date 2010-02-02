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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import fr.imag.adele.cadse.core.CadseException;
import java.util.UUID;
import fr.imag.adele.cadse.core.DerivedLinkDescription;
import fr.imag.adele.cadse.core.ItemDescription;
import fr.imag.adele.cadse.core.ItemDescriptionRef;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkDescription;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.impl.internal.Accessor;

// TODO cette classe n'est plus maintenu. Pourtant il serait bien de garder une
// console pour faire la migration � la main.
// R�t�tudier l'impl�mantation, mettre � jour et tester...
//
public class MigrationMain {
	public class MainMigrationModel extends MigrationModel {
		File	repository;
		String	modeltype;

		public MainMigrationModel(File repository, String modeltype) {
			// TODO test : add parameter : repository, modeltype
			this.repository = repository;
			this.modeltype = modeltype;
		}

		public void load(String modellocation) {
			// TODO Auto-generated method stub

		}

		public void load() {
			// TODO Auto-generated method stub

		}

		public void save() {
			// TODO Auto-generated method stub

		}

		public void loadType() {
			// TODO Auto-generated method stub

		}

		public void loadType(String directory) {
			// TODO Auto-generated method stub

		}

	}

	private static final PrintWriter	PRINT_WRITER			= new PrintWriter(System.out);

	private static final String			SHOW_HEADER_ALL			= "show-header-all";

	private static final String			SHOW_HEADER_ID			= "show-header-id ";
	private static final String			SHOW_HEADER_UNIQUE_NAME	= "show-header-unique-name ";

	private static final String			RECOMPUTE_COMPONENTS	= "recompute-componants";
	private static final String			CHECK					= "check";
	private static final String			SAVE					= "save";
	private static final String			EXIT					= "exit";
	private static final String			REPARE					= "repare";

	final class ChangeType extends MigrationOperation {

		public ChangeType() {
			super("change-type");
		}

		@Override
		public void run(String[] args) {
			ItemType oldtype = model.findItemType(args[1]);
			ItemType type = model.findItemType(args[2]);

			changeType(oldtype, type);
		}

		@Override
		public void printHelp(PrintWriter out) {
			out.println("change-type   - change type");
			out.println("                old-type <new type>");

		}
	}

	final class ChangeUniqueName extends MigrationOperation {

		public ChangeUniqueName() {
			super("change-unique-name");
		}

		@Override
		public void run(String[] args) {
			String oldname = args[1];
			String newname = args[2];

			changeUniqueName(oldname, newname);
		}

		@Override
		public void printHelp(PrintWriter out) {
			out.println("change-unique-name   - create link");
			out.println("                old new");

		}
	}

	final class CreateLink extends MigrationOperation {

		public CreateLink() {
			super("create-link");
		}

		@Override
		public void run(String[] args) {
			String source = args[1];
			String dest = args[2];
			String lt = args[3];

			createlink(source, dest, lt);
		}

		@Override
		public void printHelp(PrintWriter out) {
			out.println("create-link   - create link");
			out.println("                source dest link-type");

		}
	}

	final class DeleteLink extends MigrationOperation {

		public DeleteLink() {
			super("delete-link");
		}

		@Override
		public void run(String[] args) {
			String source = args[1];
			String dest = args[2];
			String lt = args[3];

			deletelink(source, dest, lt);
		}

		@Override
		public void printHelp(PrintWriter out) {
			out.println("delete-link   - delete link");
			out.println("                source dest link-type");

		}
	}

	final class ChangeLinkType extends MigrationOperation {

		public ChangeLinkType() {
			super("change-linktype");
		}

		@Override
		public void run(String[] args) {

			String name = args[1];
			LinkType oldtype = getLinkType(args[2]);
			LinkType type = getLinkType(args[3]);

			
			changeLinkType(name, oldtype, type);
		}

		@Override
		public void printHelp(PrintWriter out) {
			out.println("change-linktype   - change type");
			out.println("                (<uuid>|<name>) <old-link-type> <new-link-type>");

		}
	}

	final class ShowType extends MigrationOperation {

		public ShowType() {
			super("show-type");
		}

		@Override
		public void run(String[] args) {
			String type = args.length == 2 ? args[1] : null;
			showType(type);
		}

		@Override
		public void printHelp(PrintWriter out) {
			out.println("show-type [name]");

		}
	}

	final class Show extends MigrationOperation {

		public Show() {
			super("show");
		}

		@Override
		public void run(String[] args) {
			String id = args[1];
			show(get(id), "");
		}

		@Override
		public void printHelp(PrintWriter out) {
			out.println("show name");

		}
	}

	final class LoadType extends MigrationOperation {

		public LoadType() {
			super("load-type");
		}

		@Override
		public void run(String[] args) throws Throwable {
			String kind = args[1];
			loadType(kind);
		}

		@Override
		public void printHelp(PrintWriter out) {
			out.println("load-type   - load type");
			out.println("              file");

		}

	}

	final class Check extends MigrationOperation {

		public Check() {
			super("check-item");
		}

		@Override
		public void run(String[] args) throws Throwable {

			String name = null;
			boolean repare = false;
			for (int i = 1; i < args.length; i++) {
				if (args[i].equals("-r")) {
					repare = true;
				} else {
					name = args[i];
				}
			}
			if (name != null) {
				check(name, repare);
			} else {
				check(repare);
			}
		}

		@Override
		public void printHelp(PrintWriter out) {
			out.println("check-item   - check an item");
			out.println("              [-r] [name | uuid]");
			out.println("              -r repare parent");
		}

	}

	final class LoadItem extends MigrationOperation {

		public LoadItem() {
			super("load");
		}

		@Override
		public void run(String[] args) throws Throwable {
			String modellocation = args[1];
			model.load(modellocation);
		}

		@Override
		public void printHelp(PrintWriter out) {
			out.println("load   - load item");
			out.println("         file");

		}

	}

	static final String							ID_FILE_NAME		= "workspace-metadata.id";

	private static final String					REPARE_PARENT		= "repare-parent ";

	private static final String					SHOW_COMPONENTS		= "show-components";
	private static final String					SHOW_DERIVEDS		= "show-deriveds";
	private static final String					RECOMPUTE_DERIVEDS	= "recompute-deriveds";

	private MainMigrationModel					model;
	private boolean								startconsole;
	private HashMap<String, MigrationOperation>	operations			= new HashMap<String, MigrationOperation>();

	MigrationMain(File repository, boolean startconsole, String modeltype) {
		this.startconsole = startconsole;
		model = new MainMigrationModel(repository, modeltype); //
		add(new ChangeType());
		add(new LoadType());
		add(new LoadItem());
		add(new ShowType());
		add(new Show());

		add(new ChangeLinkType());
		add(new Check());
		add(new CreateLink());
		add(new DeleteLink());
		add(new ChangeUniqueName());

	}

	public LinkType getLinkType(String string) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	public void changeUniqueName(String oldname, String newname) {
		ItemDescription desc = get(oldname);

		if (desc == null) {
			System.err.println("Not found " + oldname);
			return;
		}
		ItemDescription testnewname = model.get(newname);
		if (testnewname != null) {
			System.out.println("the " + newname + " allready exists.");
			return;
		}
		model.changeUniqueName(desc, newname);
	}

	public void deletelink(String source, String dest, String lt) {
		try {
			LinkDescription removeld = model.deletelink(source, dest, lt);
			if (removeld != null) {
				System.out.println("delete link " + removeld);
			}
		} catch (CadseException e) {
			System.err.println(e.getMessage());
		}
	}

	public void createlink(String source, String dest, String lt) {
		try {
			LinkDescription ld = model.createlink(source, dest, lt);
			println("created link : ", ld);
		} catch (CadseException e) {
			System.err.println(e.getMessage());
		}
	}

	public void changeLinkType(String name, LinkType oldtype, LinkType type) {
		ItemType it = model.getCadsetype().getItemTypeByName(name);
		if (it != null) {
			for (ItemDescription desc : model.getItems()) {
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

	protected void changeLinkType(LinkType oldtype, LinkType type, ItemDescription desc) {
		for (LinkDescription ld : desc.getLinks()) {
			if (ld.getType().equals(oldtype)) {
				println(" - delete ", desc, ld.getDestination(), ld.getType());
				ld.setType(type);
				println(" - create ", desc, ld.getDestination(), ld.getType());
			}
		}
	}

	public ItemDescription get(String name) {
		return model.get(name);
	}

	public void showType(String type) {
		if (type != null) {
			show(model.getCadsetype().getItemTypeByName(type));
		} else {
			for (ItemType it : model.getCadsetype().getItemTypes()) {
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

	public void changeType(ItemType oldtype, ItemType type) {
		for (ItemDescription itemd : model.getItems()) {
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

	private void add(MigrationOperation type) {
		this.operations.put(type.getName(), type);
	}

	public ItemDescription getFromUniqueName(String id) {
		return model.getFromUniqueName(id);
	}

	public static void main(String[] args) throws Throwable {

		String pathrep = null;
		boolean startconsole = false;
		String modeltype = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-console")) {
				startconsole = true;
				continue;
			}
			if (args[i].equals("-type")) {
				modeltype = args[++i];
				continue;
			}
			if (pathrep == null) {
				pathrep = args[i];
			}
		}
		if (pathrep == null) {
			throw new IllegalArgumentException("you must give the path of .melusine directory.");
		}

		MigrationMain mm = new MigrationMain(new File(pathrep), startconsole, modeltype);
		mm.loadType();
		mm.model.load();
		if (!startconsole) {
			mm.check(true);
			mm.repare();
			mm.model.save();
		} else {
			mm.console();
		}

	}

	public void console() {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			try {
				System.out.print(":>");
				String line = br.readLine();
				if (line == null || line.length() == 0) {
					continue;
				}
				if (runline(line.trim())) {
					break;
				}

			} catch (Throwable e) {
				e.printStackTrace();
				// TODO: handle exception
			}
		}
	}

	private boolean runline(String line) throws Throwable {
		if (line.startsWith(SHOW_HEADER_ID)) {
			String name = line.substring(SHOW_HEADER_ID.length()).trim();
			showItemFrom(get(name));
			return false;
		}
		if (line.startsWith(SHOW_HEADER_UNIQUE_NAME)) {
			String id = line.substring(SHOW_HEADER_UNIQUE_NAME.length()).trim();
			showItemFrom(id);
			return false;
		}
		if (line.startsWith(SHOW_HEADER_ALL)) {
			showAllHeader();
			return false;
		}
		
		if (line.startsWith(EXIT)) {
			return true;
		}
		if (line.startsWith(CHECK)) {

			check(line.startsWith(CHECK + " -r"));
			return false;
		}
		if (line.startsWith(SAVE)) {
			model.save();
			return false;
		}
		if (line.startsWith(REPARE)) {

			repare();
			return false;
		}

		if (line.startsWith(REPARE_PARENT)) {
			String[] id = line.substring(REPARE_PARENT.length()).trim().split(" ");
			renameParentID(id[0], id[1]);
			return false;
		}
		
		String[] args = line.split(" ");
		MigrationOperation o;
		if (args != null && args.length != 0) {
			o = operations.get(args[0]);
			if (o != null) {
				try {
					o.run(args);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
			}
		}

		System.out.println("Print help:");
		System.out.println(CHECK);
		System.out.println(EXIT);
		System.out.println(RECOMPUTE_COMPONENTS);
		System.out.println(RECOMPUTE_DERIVEDS);
		System.out.println(REPARE);
		System.out.println(REPARE_PARENT + "<oldid> <newid>");
		System.out.println(SHOW_COMPONENTS);
		System.out.println(SHOW_DERIVEDS);
		System.out.println(SHOW_HEADER_ALL);
		System.out.println(SHOW_HEADER_ID + "uuid");
		System.out.println(SHOW_HEADER_UNIQUE_NAME + "unique name");
		System.out.println(SAVE);
		for (MigrationOperation oper : operations.values()) {
			oper.printHelp(PRINT_WRITER);
		}
		PRINT_WRITER.flush();
		// TODO Auto-generated method stub
		return false;
	}

	private void repare() {

		for (ItemDescription desc : model.getItems()) {
			if (desc.getName() == null) {
				desc.setShortname(desc.getQualifiedName());
			}
		}

		for (ItemDescription desc : new ArrayList<ItemDescription>(model.getItems())) {
			boolean showheader = false;
			List<LinkDescription> removeLink = new ArrayList<LinkDescription>();
			List<DerivedLinkDescription> removeDerived = new ArrayList<DerivedLinkDescription>();
			List<ItemDescriptionRef> removeComponents = new ArrayList<ItemDescriptionRef>();

			if (model.getCadsetype() != null) {
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
					ItemDescription destdesc = model.get(oldid);
					LinkType lt = it.getOutgoingLinkType(ldesc.getType());
					if (lt == null || destdesc == null) {
						removeLink.add(ldesc);
						if (!showheader) {
							showHeaderErr(desc);
							showheader = true;
						}
						System.out.println("Remove destination link: <" + ldesc.getDestination().getQualifiedName() + ">");
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
				ItemDescription destdesc = model.get(oldid);
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
		}
	}

	private void remove(ItemDescription desc) {
		model.remove(desc);
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
		
		if (model.getCadsetype() != null) {
			ItemType it = getItemType(desc);
			if (it == null) {
				if (!showheader) {
					showHeader(desc, "");
					showheader = true;
				}
				System.out.println("Bad type: " + desc.getType());
			} else {
				for (LinkDescription ldesc : desc.getLinks()) {
					LinkType lt = it.getOutgoingLinkType(ldesc.getType());
					if (lt == null) {
						if (!showheader) {
							showHeaderErr(desc);
							showheader = true;
						}
						System.out.println("Bad type link: <" + ldesc.getType() + ">");
						ItemType dest = getItemType(ldesc.getDestination());
						if (dest != null) {
							lt = getOutgoingLinkType(it, dest);
							if (lt != null) {
								println("Found link ", desc, ldesc.getDestination(), lt.getName());
							}
						}
					}
				}

				if (it.hasIncomingParts()) {
					List<LinkDescription> parents = model.findparent(desc);
					if (parents.size() == 0) {
						if (!showheader) {
							showHeaderErr(desc);
							showheader = true;
						}
						System.out.println("Part no link found, type must be: " + toStringIncomingPart(it));
						if (parentdesc != null) {
							System.out.println("Parent found with parent attribute id: " + parentdesc.getQualifiedName());
							LinkType lt = findPartLinkType(parentdesc, desc);
							if (lt != null) {
								if (repare) {
									new LinkDescription(parentdesc, lt, desc);
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
							System.out.println("Parent found with parent attribute id: " + parentdesc.getQualifiedName());
							LinkType lt = findPartLinkType(parentdesc, desc);
							if (lt != null) {
								if (repare) {
									new LinkDescription(parentdesc, lt, desc);
									desc.getAttributes().put(Accessor.ATTR_PARENT_ITEM_ID, parentdesc.getId());
									System.out.println("Create link " + parentdesc.getQualifiedName() + " --("
											+ lt.getName() + ")--> " + desc.getQualifiedName());
									for (LinkDescription pdesc : parents) {
										((ItemDescription) pdesc.getSource()).getLinks().remove(pdesc);
										System.out.println("Delete link  " + pdesc.getSource().getQualifiedName() + " --("
												+ pdesc.getType() + ")--> " + pdesc.getDestination().getQualifiedName());
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
			}
		}

	}

	private LinkType getOutgoingLinkType(ItemType it, ItemType dest) {
		LinkType foundLT = null;
		for (LinkType lt : it.getOutgoingLinkTypes()) {
			if (lt.getDestination() == dest) {
				if (foundLT == null) {
					foundLT = lt;
				} else {
					return null;
				}
			}
		}
		return foundLT;
	}

	public void check(String name, boolean repare) {
		check(get(name), repare);
	}

	public void check(boolean repare) {

		for (ItemDescription desc : model.getItems()) {
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
		ItemDescription destdesc = model.get(uuid_id);
		if (destdesc == null) {
			String unique_name_id = il.getQualifiedName();
			destdesc = model.get(unique_name_id);
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
		return model.getStringOrUUID(parentIdStr);
	}

	private void showItemFrom(String oldid) throws Throwable {
		ItemDescription desc = model.get(oldid);
		if (desc == null) {
			System.out.println("Not found item with id " + oldid);
		}
		showHeader(desc, "");
	}

	private void showItemFrom(ItemDescription desc) throws Throwable {
		if (desc == null) {
			System.out.println("Not found item");
		}
		showHeader(desc, "");
	}

	private void showAllHeader() {
		for (ItemDescription desc : model.getItems()) {
			showHeader(desc, "");
			System.out.println();
		}
	}



	private void showHeader(ItemDescription desc, String tab) {
		System.out.println(tab + "#ID         :" + desc.getId().toString());
		System.out.println(tab + "#Unique name:" + desc.getQualifiedName());
		System.out.println(tab + "#Short name :" + desc.getName());
		if (model.getCadsetype() != null) {
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

	private void show(ItemDescription desc, String tab) {
		System.out.println(tab + "#ID         :" + desc.getId().toString());
		System.out.println(tab + "#Unique name:" + desc.getQualifiedName());
		System.out.println(tab + "#Short name :" + desc.getName());
		if (model.getCadsetype() != null) {
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

	private void showHeaderErr(ItemDescription desc) {
		System.out.println();
		showHeader(desc, "");
	}

	private void renameParentID(String old, String newname) throws Throwable {
		ItemDescription parentdesc = getStringOrUUID(newname);
		if (parentdesc == null) {
			System.out.println("Cannot find item : <" + newname + ">");
			return;
		}
		for (ItemDescription desc : model.getItems()) {
			String parentIdStr = (String) desc.getAttributes().get(Accessor.ATTR_PARENT_ITEM_ID);
			if (old.equals(parentIdStr)) {
				List<LinkDescription> parents = model.findparent(desc);
				LinkType lt = findPartLinkType(parentdesc, desc);
				if (lt != null) {
					new LinkDescription(parentdesc, lt, desc);
					desc.getAttributes().put(Accessor.ATTR_PARENT_ITEM_ID, parentdesc.getId());
					System.out.println("Create link " + parentdesc.getQualifiedName() + " --(" + lt.getName()
							+ ")--> " + desc.getQualifiedName());
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
	
	private ItemType getItemType(ItemDescriptionRef desc) {
		ItemType it = model.getCadsetype().getItemType(desc.getType());
		return it;
	}

	void loadType() throws Throwable {
		model.loadType();
	}

	void loadType(String directory) throws Throwable {
		model.loadType(directory);
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
 * context { PropertyValue PV; ProvideService PS; BinServiceLocal BSL;
 * PropertyAttribute PA;
 * 
 * link (BSL -[providesService ]-> PS); link (PS -[propertiesValues ]-> PV);
 * link (PV -[propertyAttribute]-> PA); } => { delete-link(PS,PV,
 * propertiesValues);
 * 
 * create-link( BSL, PV, propertiesValue); create-link( PS, PA,
 * propertiesAttributes); change-unique-name( PV, BSL.unique-name + "." +
 * PV.short-name); }
 * 
 * context { PropertyValue PV; ProvideService PS; BinServiceLocal BSL;
 * PropertyType PT;
 * 
 * link (BSL -[providesService ]-> PS); link (PS -[propertiesValues ]-> PV);
 * link (PV -[type]-> PT); } => { delete-link(PS,PV, propertiesValues);
 * 
 * create-link( BSL, PV, propertiesValue); create-link( PS, PT,
 * propertiesAttributes); change-unique-name( PV, BSL.unique-name + "." +
 * PV.short-name); }
 * 
 */
