/*
 * K-scope
 * Copyright 2012-2013 RIKEN, Japan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.riken.kscope.xcodeml.xml.gen;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import jp.riken.kscope.xcodeml.xml.*;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;element ref="{}varList"/>
 *         &lt;element ref="{}valueList"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{}defAttrSourceLine"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "varListAndValueList" })
@XmlRootElement(name = "FdataDecl")
public class FdataDecl implements IDefBaseStatement, IXmlNode {

    @XmlElements({
            @XmlElement(name = "valueList", required = true, type = ValueList.class),
            @XmlElement(name = "varList", required = true, type = VarList.class) })
    protected List<IXmlNode> varListAndValueList;
    @XmlAttribute
    protected String lineno;
    @XmlAttribute
    protected String endlineno;
    @XmlAttribute
    protected String rawlineno;
    @XmlAttribute
    protected String file;

    /**
     * Gets the value of the varListAndValueList property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the varListAndValueList property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getVarListAndValueList().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ValueList } {@link VarList }
     *
     * @return      IXmlNode List
     *
     */
    public List<IXmlNode> getVarListAndValueList() {
        if (varListAndValueList == null) {
            varListAndValueList = new ArrayList<IXmlNode>();
        }
        return this.varListAndValueList;
    }

    /**
     * Gets the value of the lineno property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getLineno() {
        return lineno;
    }

    /**
     * Sets the value of the lineno property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setLineno(String value) {
        this.lineno = value;
    }

    /**
     * Gets the value of the endlineno property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEndlineno() {
        return endlineno;
    }

    /**
     * Sets the value of the endlineno property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEndlineno(String value) {
        this.endlineno = value;
    }

    /**
     * Gets the value of the rawlineno property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getRawlineno() {
        return rawlineno;
    }

    /**
     * Sets the value of the rawlineno property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setRawlineno(String value) {
        this.rawlineno = value;
    }

    /**
     * Gets the value of the file property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setFile(String value) {
        this.file = value;
    }

    @Override
    public boolean enter(jp.riken.kscope.xcodeml.xml.IXmlVisitor visitor) {
        return (visitor.enter(this));
    }

    @Override
    public void leave(jp.riken.kscope.xcodeml.xml.IXmlVisitor visitor) {
        visitor.leave(this);
    }
}
