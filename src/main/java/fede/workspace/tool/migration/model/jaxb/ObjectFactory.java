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
package fede.workspace.tool.migration.model.jaxb;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fede.workspace.tool.migration.model.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fede.workspace.tool.migration.model.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MigInstDeleteLink }
     * 
     */
    public MigInstDeleteLink createMigInstDeleteLink() {
        return new MigInstDeleteLink();
    }

    /**
     * Create an instance of {@link MigItem }
     * 
     */
    public MigItem createMigItem() {
        return new MigItem();
    }

    /**
     * Create an instance of {@link MigInsDuplicateItem }
     * 
     */
    public MigInsDuplicateItem createMigInsDuplicateItem() {
        return new MigInsDuplicateItem();
    }

    /**
     * Create an instance of {@link MigInstDeleteItem }
     * 
     */
    public MigInstDeleteItem createMigInstDeleteItem() {
        return new MigInstDeleteItem();
    }

    /**
     * Create an instance of {@link MigLink }
     * 
     */
    public MigLink createMigLink() {
        return new MigLink();
    }

    /**
     * Create an instance of {@link MigCondHasAttr }
     * 
     */
    public MigCondHasAttr createMigCondHasAttr() {
        return new MigCondHasAttr();
    }

    /**
     * Create an instance of {@link MigVarRef }
     * 
     */
    public MigVarRef createMigVarRef() {
        return new MigVarRef();
    }

    /**
     * Create an instance of {@link MigCondNotHasLink }
     * 
     */
    public MigCondNotHasLink createMigCondNotHasLink() {
        return new MigCondNotHasLink();
    }

    /**
     * Create an instance of {@link MigContext }
     * 
     */
    public MigContext createMigContext() {
        return new MigContext();
    }

    /**
     * Create an instance of {@link MigItemType }
     * 
     */
    public MigItemType createMigItemType() {
        return new MigItemType();
    }

    /**
     * Create an instance of {@link MigInstCreateLink }
     * 
     */
    public MigInstCreateLink createMigInstCreateLink() {
        return new MigInstCreateLink();
    }

    /**
     * Create an instance of {@link MigCondNoParent }
     * 
     */
    public MigCondNoParent createMigCondNoParent() {
        return new MigCondNoParent();
    }

    /**
     * Create an instance of {@link MigCondition }
     * 
     */
    public MigCondition createMigCondition() {
        return new MigCondition();
    }

    /**
     * Create an instance of {@link MigLinkType }
     * 
     */
    public MigLinkType createMigLinkType() {
        return new MigLinkType();
    }

    /**
     * Create an instance of {@link Migration }
     * 
     */
    public Migration createMigration() {
        return new Migration();
    }

}
