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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mig-context complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mig-context">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="var" type="{http://www.example.org/mig/}mig-variable" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="var-inst" type="{http://www.example.org/mig/}mig-variable" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="inst" type="{http://www.example.org/mig/}mig-inst" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="CadseName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="CadseVersion" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mig-context", propOrder = {
    "var",
    "varInst",
    "inst"
})
public class MigContext {

    protected List<MigVariable> var;
    @XmlElement(name = "var-inst")
    protected List<MigVariable> varInst;
    protected List<MigInst> inst;
    @XmlAttribute(name = "CadseName", required = true)
    protected String cadseName;
    @XmlAttribute(name = "CadseVersion", required = true)
    protected int cadseVersion;

    /**
     * Gets the value of the var property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the var property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVar().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MigVariable }
     * 
     * 
     */
    public List<MigVariable> getVar() {
        if (var == null) {
            var = new ArrayList<MigVariable>();
        }
        return this.var;
    }

    /**
     * Gets the value of the varInst property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the varInst property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVarInst().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MigVariable }
     * 
     * 
     */
    public List<MigVariable> getVarInst() {
        if (varInst == null) {
            varInst = new ArrayList<MigVariable>();
        }
        return this.varInst;
    }

    /**
     * Gets the value of the inst property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the inst property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInst().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MigInst }
     * 
     * 
     */
    public List<MigInst> getInst() {
        if (inst == null) {
            inst = new ArrayList<MigInst>();
        }
        return this.inst;
    }

    /**
     * Gets the value of the cadseName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCadseName() {
        return cadseName;
    }

    /**
     * Sets the value of the cadseName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCadseName(String value) {
        this.cadseName = value;
    }

    /**
     * Gets the value of the cadseVersion property.
     * 
     */
    public int getCadseVersion() {
        return cadseVersion;
    }

    /**
     * Sets the value of the cadseVersion property.
     * 
     */
    public void setCadseVersion(int value) {
        this.cadseVersion = value;
    }

}
